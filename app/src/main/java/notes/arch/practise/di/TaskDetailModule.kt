package notes.arch.practise.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import notes.arch.practise.taskdetail.TaskDetailFragment
import notes.arch.practise.taskdetail.TaskDetailViewModel

/**
 * Dagger module for the Detail feature.
 */
@Module
abstract class TaskDetailModule {
    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun taskDetailFragment(): TaskDetailFragment

    @Binds
    @IntoMap
    @ViewModelKey(TaskDetailViewModel::class)
    abstract fun bindViewModel(viewModel: TaskDetailViewModel): ViewModel
}