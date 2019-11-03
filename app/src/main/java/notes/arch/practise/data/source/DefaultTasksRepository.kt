package notes.arch.practise.data.source

import kotlinx.coroutines.*
import notes.arch.practise.data.Result
import notes.arch.practise.data.Result.Error
import notes.arch.practise.data.Task
import notes.arch.practise.di.ApplicationModule
import notes.arch.practise.util.EspressoIdlingResource
import notes.arch.practise.util.wrapEspressoIdlingResource
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 *
 * To simplify the sample, this repository only uses the local data source only if the remote
 * data source fails. Remote is the source of truth.
 */
class DefaultTasksRepository @Inject constructor(
    @ApplicationModule.TasksRemoteDataSource private val tasksRemoteDataSource: TasksDataSource,
    @ApplicationModule.TasksLocalDataSource private val tasksLocalDataSource: TasksDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksRepository {
    private var cachedTasks: ConcurrentHashMap<String, Task>? = null

    override suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { tasksRemoteDataSource.clearCompletedTasks() }
            launch { tasksLocalDataSource.clearCompletedTasks() }
        }
        withContext(ioDispatcher) {
            cachedTasks?.entries?.removeAll { it.value.isCompleted }
        }
    }

    override suspend fun completeTask(task: Task) {
        cacheAndPerform(task) {
            it.isCompleted = true
            coroutineScope {
                launch { tasksRemoteDataSource.completeTask(it) }
                launch { tasksLocalDataSource.completeTask(it) }
            }
        }
    }

    override suspend fun activateTask(task: Task) {
        cacheAndPerform(task) {
            it.isCompleted = false
            coroutineScope {
                launch { tasksRemoteDataSource.activateTask(it) }
                launch { tasksLocalDataSource.activateTask(it) }
            }
        }
    }

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
        wrapEspressoIdlingResource {
            return withContext(ioDispatcher) {
                // Respond immediately with cache if available and not dirty
                if (!forceUpdate) {
                    cachedTasks?.let { cachedTasks ->
                        return@withContext Result.Success(cachedTasks.values.sortedBy { it.id })
                    }
                }
                val newTasks = fetchTasksFromRemoteOrLocal(forceUpdate)

                // Refresh the cache with the new tasks
                (newTasks as? Result.Success)?.let { refreshCache(it.data) }

                cachedTasks?.values?.let { tasks ->
                    return@withContext Result.Success(tasks.sortedBy { it.id })
                }

                (newTasks as? Result.Success)?.let {
                    if (it.data.isEmpty()) {
                        return@withContext Result.Success(it.data)
                    }
                }
                return@withContext Error(Exception("Illegal state"))
            }
        }
    }

    private suspend fun refreshLocalDataSource(tasks: List<Task>) {
        tasksLocalDataSource.deleteAllTasks()
        for (task in tasks) {
            tasksLocalDataSource.saveTask(task)
        }
    }

    private suspend fun refreshLocalDataSource(task: Task) {
        tasksLocalDataSource.saveTask(task)
    }

    private suspend fun fetchTasksFromRemoteOrLocal(forceUpdate: Boolean): Result<List<Task>> {
        // Remote first
        when (val remoteTasks = tasksRemoteDataSource.getTasks()) {
            is Error -> Timber.w("Remote data source fetch failed")
            is Result.Success -> {
                refreshLocalDataSource(remoteTasks.data)
                return remoteTasks
            }
            else -> throw IllegalStateException()
        }

        // Don't read form local if it's forced
        if (forceUpdate) {
            return Error(Exception("Can't force refresh: remote data source is unavailable"))
        }

        // Local if remote fails
        val localTasks = tasksLocalDataSource.getTasks()
        if (localTasks is Result.Success) return localTasks
        return Error(java.lang.Exception("Error fetching from remote and local"))
    }

    private fun cacheTask(task: Task): Task {
        val cachedTask = Task(task.title, task.description, task.isCompleted, task.id)
        // Create if it doesn't exist.
        if (cachedTasks == null) {
            cachedTasks = ConcurrentHashMap()
        }
        cachedTasks?.put(cachedTask.id, cachedTask)
        return cachedTask
    }

    private inline fun cacheAndPerform(task: Task, perform: (Task) -> Unit) {
        val cachedTask = cacheTask(task)
        perform(cachedTask)
    }

    private fun refreshCache(tasks: List<Task>) {
        cachedTasks?.clear()
        tasks.sortedBy { it.id }.forEach {
            cacheAndPerform(it) {}
        }
    }

    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> {

        wrapEspressoIdlingResource {

            return withContext(ioDispatcher) {
                // Respond immediately with cache if available
                if (!forceUpdate) {
                    getTaskWithId(taskId)?.let {
                        EspressoIdlingResource.decrement() // Set app as idle.
                        return@withContext Result.Success(it)
                    }
                }

                val newTask = fetchTaskFromRemoteOrLocal(taskId, forceUpdate)

                // Refresh the cache with the new tasks
                (newTask as? Result.Success)?.let { cacheTask(it.data) }

                return@withContext newTask
            }
        }
    }

    private suspend fun fetchTaskFromRemoteOrLocal(
        taskId: String,
        forceUpdate: Boolean
    ): Result<Task> {
        when (val remoteTask = tasksRemoteDataSource.getTask(taskId)) {
            is Error -> Timber.w("Remote data source fetch failed")
            is Result.Success -> {
                refreshLocalDataSource(remoteTask.data)
                return remoteTask
            }
            else -> throw IllegalStateException()
        }

        // Don't read from local if it's forced
        if (forceUpdate) {
            return Error(java.lang.Exception("Refresh failed"))
        }

        // Local if remote fails
        val localTasks = tasksLocalDataSource.getTask(taskId)
        if (localTasks is Result.Success) return localTasks
        return Error(java.lang.Exception("Error fetching from remote and local"))
    }

    override suspend fun saveTask(task: Task) {
        cacheAndPerform(task) {
            coroutineScope {
                launch { tasksRemoteDataSource.saveTask(it) }
                launch { tasksLocalDataSource.saveTask(it) }
            }
        }
    }

    private fun getTaskWithId(id: String) = cachedTasks?.get(id)

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.deleteTask(taskId) }
            launch { tasksLocalDataSource.deleteTask(taskId) }
        }

        cachedTasks?.remove(taskId)
    }

    override suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            getTaskWithId(taskId)?.let {
                completeTask(taskId)
            }
        }
    }

    override suspend fun activateTask(taskId: String) {
        withContext(ioDispatcher) {
            getTaskWithId(taskId)?.let {
                activateTask(it)
            }
        }
    }

    override suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { tasksRemoteDataSource.deleteAllTasks() }
                launch { tasksLocalDataSource.deleteAllTasks() }
            }
        }

        cachedTasks?.clear()
    }
}