package notes.arch.practise

import dagger.Module
import dagger.Provides
import notes.arch.practise.data.source.FakeRepository
import notes.arch.practise.data.source.TasksRepository
import javax.inject.Singleton

/**
 * A replacement for [ApplicationModule] to be used in tests. It simply creates a [FakeRepository].
 */
@Module
class TestApplicationModule {
    @Singleton
    @Provides
    fun provideRepository(): TasksRepository = FakeRepository()
}