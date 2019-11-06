package notes.arch.practise

import notes.arch.practise.data.Result
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksDataSource

object FakeFailingTasksRemoteDataSource : TasksDataSource {
    override suspend fun getTasks(): Result<List<Task>> {
        return Result.Error(Exception("Test"))
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        return Result.Error(Exception("Test"))
    }

    override suspend fun completeTask(task: Task) {
        // Empty
    }

    override suspend fun completeTask(taskId: String) {
        // Empty
    }

    override suspend fun activateTask(task: Task) {
        // Empty
    }

    override suspend fun activateTask(taskId: String) {
        // Empty
    }

    override suspend fun deleteAllTasks() {
        // Empty
    }

    override suspend fun saveTask(task: Task) {
        // Empty
    }

    override suspend fun clearCompletedTasks() {
        // Empty
    }

    override suspend fun deleteTask(taskId: String) {
        // Empty
    }
}