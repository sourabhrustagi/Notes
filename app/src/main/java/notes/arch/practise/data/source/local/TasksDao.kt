package notes.arch.practise.data.source.local

import androidx.room.*
import notes.arch.practise.data.Task

/**
 * Data Access Object for the tasks table.
 */
@Dao
interface TasksDao {
    /**
     * Select all tasks from the tasks table.
     *
     * @return all tasks.
     */
    @Query("SELECT * from tasks")
    suspend fun getTasks(): List<Task>

    /**
     * Select a task by id.
     *
     * @param taskId the task id.
     * @return the task with taskId.
     */
    @Query("SELECT * from tasks where entryid = :taskId")
    suspend fun getTaskById(taskId: String): Task?

    /**
     * Delete all completed tasks from the table.
     *
     * @return the number of tasks deleted.
     */
    @Query("DELETE FROM tasks WHERE completed=1")
    suspend fun deleteCompletedTasks(): Int

    /**
     * Insert a task in the database. If the task already exists, replace it.
     *
     * @param task the task to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    /**
     * Delete all tasks.
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteTasks()

    /**
     * Update the complete status of a task
     *
     * @param taskId    id of the task
     * @param completed status of the updated
     */
    @Query("UPDATE tasks SET completed = :completed WHERE entryid= :taskId")
    fun updateCompleted(taskId: String, completed: Boolean)

    /**
     * Delete a task by id.
     *
     * @return the number of tasks deleted. This should always be 1.
     */
    @Query("DELETE FROM tasks WHERE entryid=:taskId")
    suspend fun deleteTaskById(taskId: String): Int

    /**
     * Update a task.
     *
     * @param task task to be updated
     * @return the number of tasks updated. This should always be 1.
     */
    @Update
    suspend fun updateTask(task: Task): Int
}