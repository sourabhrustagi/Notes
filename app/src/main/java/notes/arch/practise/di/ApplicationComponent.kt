package notes.arch.practise.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import notes.arch.practise.TodoApplication
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        AndroidSupportInjectionModule::class,
        TasksModule::class,
        AddEditTaskModule::class,
        TaskDetailModule::class,
        StatisticsModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<TodoApplication> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }
}