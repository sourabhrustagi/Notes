package notes.arch.practise.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import notes.arch.practise.data.Task

/**
 * The Room Database that contains the Task table.
 */
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class ToDoDatabase : RoomDatabase() {
    abstract fun taskDao(): TasksDao
}