package notes.arch.practise.data.source

import notes.arch.practise.data.Result
import notes.arch.practise.data.Task

/**
 * Main entry point for accessing tasks data.
 */
interface TasksDataSource {
    suspend fun getTasks(): Result<List<Task>>

    suspend fun getTask(taskId: String): Result<Task>

    suspend fun completeTask(task: Task)

    suspend fun completeTask(taskId: String)

    suspend fun activateTask(task: Task)

    suspend fun activateTask(taskId: String)

    suspend fun deleteAllTasks()

    suspend fun saveTask(task: Task)

    suspend fun clearCompletedTasks()

    suspend fun deleteTask(taskId: String)
}