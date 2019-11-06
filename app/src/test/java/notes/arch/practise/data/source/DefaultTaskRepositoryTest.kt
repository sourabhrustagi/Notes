package notes.arch.practise.data.source

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import notes.arch.practise.data.Result
import notes.arch.practise.data.Task
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the implementation of the in-memory repository with cache.
 */
@ExperimentalCoroutinesApi
class DefaultTaskRepositoryTest {
    private val task1 = Task("Title1", "Description1")
    private val task2 = Task("Title2", "Description2")
    private val task3 = Task("Title3", "Description3")
    private val newTask = Task("Title new", "Description new")
    private val remoteTasks = listOf(task1, task2).sortedBy { it.id }
    private val localTasks = listOf(task3).sortedBy { it.id }
    private val newTasks = listOf(task3).sortedBy { it.id }
    private lateinit var tasksRemoteDataSource: FakeDataSource
    private lateinit var tasksLocalDataSource: FakeDataSource

    // Class under test
    private lateinit var tasksRepository: DefaultTasksRepository

    @ExperimentalCoroutinesApi
    @Before
    fun createRepository() {
        tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeDataSource(localTasks.toMutableList())

        tasksRepository = DefaultTasksRepository(
            tasksRemoteDataSource, tasksLocalDataSource, Dispatchers.Unconfined
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun getTasks_emptyRepositoryAndUninitializedCache() = runBlockingTest {
        val emptySource = FakeDataSource()
        val tasksRepository = DefaultTasksRepository(
            emptySource, emptySource, Dispatchers.Unconfined
        )
        assertThat(tasksRepository.getTasks() is Result.Success).isTrue()
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() = runBlockingTest {
        val initial = tasksRepository.getTasks()

        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        val second = tasksRepository.getTasks()

        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getTasks_requestsAllTasksFromRemoteDataSource() = runBlockingTest {
        val tasks = tasksRepository.getTasks() as Result.Success

        assertThat(tasks.data).isEqualTo(remoteTasks)
    }

    @Test
    fun saveTask_savesToCacheLocalAndRemote() = runBlockingTest {
        assertThat(tasksRemoteDataSource.tasks).doesNotContain(newTask)
        assertThat(tasksLocalDataSource.tasks).doesNotContain(newTask)
        assertThat((tasksRepository.getTasks() as? Result.Success)?.data).doesNotContain(newTask)

        tasksRepository.saveTask(newTask)

        assertThat(tasksRemoteDataSource.tasks).contains(newTask)
        assertThat(tasksLocalDataSource.tasks).contains(newTask)

        val result = tasksRepository.getTasks() as? Result.Success
        assertThat(result?.data).contains(newTask)
    }

    @Test
    fun getTasks_WithDirtyCache_tasksAreRetrievedFromRemote() = runBlockingTest {
        val tasks = tasksRepository.getTasks()

        tasksRemoteDataSource.tasks = newTasks.toMutableList()

        val cachedTasks = tasksRepository.getTasks()
        assertThat(cachedTasks).isEqualTo(tasks)

        val refreshedTasks = tasksRepository.getTasks(true) as Result.Success

        assertThat(refreshedTasks.data).isEqualTo(newTasks)
    }

    @Test
    fun getTasks_WithDirtyCache_remoteUnavailable_error() = runBlockingTest {
        tasksRemoteDataSource.tasks = null

        val refreshedTasks = tasksRepository.getTasks(true)

        assertThat(refreshedTasks).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getTasks_WithRemoteDataSourceUnavailable_tasksAreRetrievedFromLocal() = runBlockingTest {
        // When the remote data source is unavailable
        tasksRemoteDataSource.tasks = null

        // The repository fetches from the local source
        assertThat((tasksRepository.getTasks() as Result.Success).data).isEqualTo(localTasks)
    }

    @Test
    fun getTasks_WithBothDataSourcesUnavailable_returnsError() = runBlockingTest {
        // When both sources are unavailable
        tasksRemoteDataSource.tasks = null
        tasksLocalDataSource.tasks = null

        // The repository returns an error
        assertThat(tasksRepository.getTasks()).isInstanceOf(Result.Error::class.java)
    }

    @Test
    fun getTasks_refreshesLocalDataSource() = runBlockingTest {
        val initialLocal = tasksLocalDataSource.tasks!!.toList()

        // First load will fetch from remote
        val newTasks = (tasksRepository.getTasks() as Result.Success).data

        assertThat(newTasks).isEqualTo(remoteTasks)
        assertThat(newTasks).isEqualTo(tasksLocalDataSource.tasks)
        assertThat(tasksLocalDataSource.tasks).isNotEqualTo(initialLocal)
    }

    @Test
    fun saveTask_savesTaskToRemoteAndUpdatesCache() = runBlockingTest {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Verify it's in all the data sources
        assertThat(tasksLocalDataSource.tasks).contains(newTask)
        assertThat(tasksRemoteDataSource.tasks).contains(newTask)

        // Verify it's in the cache
        tasksLocalDataSource.deleteAllTasks() // Make sure they don't come from local
        tasksRemoteDataSource.deleteAllTasks() // Make sure they don't come from remote
        val result = tasksRepository.getTasks() as Result.Success
        assertThat(result.data).contains(newTask)
    }

    @Test
    fun completeTask_completesTaskToServiceAPIUpdatesCache() = runBlockingTest {
        // Save a task
        tasksRepository.saveTask(newTask)

        // Make sure it's active
        assertThat((tasksRepository.getTask(newTask.id) as Result.Success).data.isCompleted).isFalse()

        // Mark is as complete
        tasksRepository.completeTask(newTask.id)

        // Verify it's now completed
        assertThat((tasksRepository.getTask(newTask.id) as Result.Success).data.isCompleted).isTrue()
    }

    @Test
    fun completeTask_activeTaskToServiceAPIUpdatesCache() = runBlockingTest {
        // Save a task
        tasksRepository.saveTask(newTask)
        tasksRepository.completeTask(newTask.id)

        // Make sure it's completed
        assertThat((tasksRepository.getTask(newTask.id) as Result.Success).data.isActive).isFalse()

        // Mark is as active
        tasksRepository.activateTask(newTask.id)

        // Verify it's now activated
        val result = tasksRepository.getTask(newTask.id) as Result.Success
        assertThat(result.data.isActive).isTrue()
    }

    @Test
    fun getTask_repositoryCachesAfterFirstApiCall() = runBlockingTest {
        // Trigger the repository to load data, which loads from remote
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        tasksRepository.getTask(task1.id)

        // Configure the remote data source to store a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        val task1SecondTime = tasksRepository.getTask(task1.id) as Result.Success
        val task2SecondTime = tasksRepository.getTask(task2.id) as Result.Success

        // Both work because one is in remote and the other in cache
        assertThat(task1SecondTime.data.id).isEqualTo(task1.id)
        assertThat(task2SecondTime.data.id).isEqualTo(task2.id)
    }

    @Test
    fun getTask_forceRefresh() = runBlockingTest {
        // Trigger the repository to load data, which loads from remote and caches
        tasksRemoteDataSource.tasks = mutableListOf(task1)
        tasksRepository.getTask(task1.id)

        // Configure the remote data source to return a different task
        tasksRemoteDataSource.tasks = mutableListOf(task2)

        // Force refresh
        val task1SecondTime = tasksRepository.getTask(task1.id, true)
        val task2SecondTime = tasksRepository.getTask(task2.id, true)

        // Only task2 works because the cache and local were invalidated
        assertThat((task1SecondTime as? Result.Success)?.data?.id).isNull()
        assertThat((task2SecondTime as? Result.Success)?.data?.id).isEqualTo(task2.id)
    }

    @Test
    fun clearCompletedTasks() = runBlockingTest {
        val completedTask = task1.copy().apply { isCompleted = true }
        tasksRemoteDataSource.tasks = mutableListOf(completedTask, task2)
        tasksRepository.clearCompletedTasks()

        val tasks = (tasksRepository.getTasks() as? Result.Success)?.data

        assertThat(tasks).hasSize(1)
        assertThat(tasks).contains(task2)
        assertThat(tasks).doesNotContain(completedTask)
    }

    @Test
    fun deleteAllTasks() = runBlockingTest {
        val initialTasks = (tasksRepository.getTasks() as? Result.Success)?.data

        // Delete all tasks
        tasksRepository.deleteAllTasks()

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Result.Success)?.data

        // Verify tasks are empty now
        assertThat(initialTasks).isNotEmpty()
        assertThat(afterDeleteTasks).isEmpty()
    }

    @Test
    fun deleteSingleTask() = runBlockingTest {
        val initialTasks = (tasksRepository.getTasks() as? Result.Success)?.data

        // Delete first task
        tasksRepository.deleteTask(task1.id)

        // Fetch data again
        val afterDeleteTasks = (tasksRepository.getTasks() as? Result.Success)?.data

        // Verify only one task was deleted
        assertThat(afterDeleteTasks?.size).isEqualTo(initialTasks!!.size - 1)
        assertThat(afterDeleteTasks).doesNotContain(task1)
    }
}