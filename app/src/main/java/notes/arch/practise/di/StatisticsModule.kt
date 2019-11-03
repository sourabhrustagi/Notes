package notes.arch.practise.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import notes.arch.practise.statistics.StatisticsFragment
import notes.arch.practise.statistics.StatisticsViewModel

/**
 * Dagger module for the statistics feature
 */
@Module
abstract class StatisticsModule {
    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun statisticsFragment(): StatisticsFragment

    @Binds
    @IntoMap
    @ViewModelKey(StatisticsViewModel::class)
    abstract fun bindViewModel(viewModel: StatisticsViewModel): ViewModel
}