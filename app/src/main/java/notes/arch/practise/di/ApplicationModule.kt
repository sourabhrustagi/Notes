package notes.arch.practise.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import notes.arch.practise.data.source.DefaultTasksRepository
import notes.arch.practise.data.source.TasksDataSource
import notes.arch.practise.data.source.TasksRepository
import notes.arch.practise.data.source.local.TasksLocalDataSource
import notes.arch.practise.data.source.local.ToDoDatabase
import notes.arch.practise.data.source.remote.TasksRemoteDataSource
import javax.inject.Qualifier
import javax.inject.Singleton

@Module(includes = [ApplicationModuleBinds::class])
object ApplicationModule {
    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TasksRemoteDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TasksLocalDataSource

    @JvmStatic
    @Singleton
    @TasksRemoteDataSource
    @Provides
    fun provideTasksRemoteDataSource(): TasksDataSource {
        return TasksRemoteDataSource
    }

    @JvmStatic
    @Singleton
    @TasksLocalDataSource
    @Provides
    fun provideTasksLocalDataSource(
        database: ToDoDatabase,
        ioDispatcher: CoroutineDispatcher
    ): TasksDataSource {
        return TasksLocalDataSource(
            database.taskDao(), ioDispatcher
        )
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDataBase(context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        ).build()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideIoDispatcher() = Dispatchers.IO
}

@Module
abstract class ApplicationModuleBinds {
    @Singleton
    @Binds
    abstract fun bindRepository(repo: DefaultTasksRepository): TasksRepository
}