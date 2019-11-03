package notes.arch.practise.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import notes.arch.practise.addedittask.AddEditTaskFragment
import notes.arch.practise.addedittask.AddEditTaskViewModel

/**
 * Dagger module for the Add/Edit feature.
 */
@Module
abstract class AddEditTaskModule {
    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun addEditTaskFragment(): AddEditTaskFragment

    @Binds
    @IntoMap
    @ViewModelKey(AddEditTaskViewModel::class)
    internal abstract fun bindViewModel(viewModel: AddEditTaskViewModel): ViewModel
}