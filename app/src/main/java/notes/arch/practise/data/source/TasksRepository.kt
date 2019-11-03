package notes.arch.practise.data.source

import notes.arch.practise.data.Result
import notes.arch.practise.data.Task

interface TasksRepository {
    suspend fun getTasks(forceUpdate: Boolean = false): Result<List<Task>>

    suspend fun getTask(taskId: String, forceUpdate: Boolean = false): Result<Task>

    suspend fun completeTask(task: Task)

    suspend fun completeTask(taskId: String)

    suspend fun activateTask(task: Task)

    suspend fun activateTask(taskId: String)

    suspend fun clearCompletedTasks()

    suspend fun saveTask(task: Task)

    suspend fun deleteAllTasks()

    suspend fun deleteTask(taskId: String)
}