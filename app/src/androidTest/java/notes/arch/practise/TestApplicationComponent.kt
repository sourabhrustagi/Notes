package notes.arch.practise

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import notes.arch.practise.data.source.TasksRepository
import notes.arch.practise.di.*
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        TestApplicationModule::class,
        AndroidSupportInjectionModule::class,
        ViewModelBuilder::class,
        TasksModule::class,
        TaskDetailModule::class,
        AddEditTaskModule::class,
        StatisticsModule::class
    ]
)
interface TestApplicationComponent : AndroidInjector<TestTodoApplication> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): TestApplicationComponent
    }

    val tasksRepository: TasksRepository
}