package notes.arch.practise.data.source.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import notes.arch.practise.data.Result
import notes.arch.practise.data.Result.Error
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksDataSource

/**
 * Concrete implementation of a data source as a db.
 */
class TasksLocalDataSource internal constructor(
    private val tasksDao: TasksDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksDataSource {
    override suspend fun getTask(taskId: String) = withContext(ioDispatcher) {
        try {
            val task = tasksDao.getTaskById(taskId)
            if (task != null) {
                return@withContext Result.Success(task)
            } else {
                return@withContext Error(Exception("Task not found!"))
            }
        } catch (e: java.lang.Exception) {
            return@withContext Error(e)
        }
    }

    override suspend fun clearCompletedTasks() = withContext<Unit>(ioDispatcher) {
        tasksDao.deleteCompletedTasks()
    }

    override suspend fun saveTask(task: Task) = withContext(ioDispatcher) {
        tasksDao.insertTask(task)
    }

    override suspend fun deleteAllTasks() = withContext(ioDispatcher) {
        tasksDao.deleteTasks()
    }

    override suspend fun getTasks(): Result<List<Task>> = withContext(ioDispatcher) {
        return@withContext try {
            Result.Success(tasksDao.getTasks())
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun completeTask(task: Task) = withContext(ioDispatcher) {
        tasksDao.updateCompleted(task.id, true)
    }

    override suspend fun activateTask(task: Task) = withContext(ioDispatcher) {
        tasksDao.updateCompleted(task.id, false)
    }

    override suspend fun deleteTask(taskId: String) = withContext<Unit>(ioDispatcher) {
        tasksDao.deleteTaskById(taskId)
    }
}