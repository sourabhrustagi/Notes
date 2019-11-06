package notes.arch.practise.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import notes.arch.practise.FakeFailingTasksRemoteDataSource
import notes.arch.practise.LiveDataTestUtil
import notes.arch.practise.MainCoroutineRule
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.DefaultTasksRepository
import notes.arch.practise.data.source.FakeRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [StatisticsViewModel]
 */
@ExperimentalCoroutinesApi
class StatisticsViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Subject under test
    private lateinit var statisticsViewModel: StatisticsViewModel

    // Use a fake repository to be injected into the viewmodel
    private val tasksRepository = FakeRepository()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupStatisticsViewModel() {
        statisticsViewModel = StatisticsViewModel(tasksRepository, StatisticsUtils())
    }

    @Test
    fun loadEmptyTasksFromRepository_EmptyResults() = mainCoroutineRule.runBlockingTest {
        // Given an initialized StatisticsViewModel with no tasks

        // WHen loading of tasks is requested
        statisticsViewModel.start()

        // Then the results are empty
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.empty)).isTrue()
    }

    @Test
    fun loadNonEmptyTasksFromRepository_NonEmptyResults() {
        // We initialise the tasks to 3, with one active and two completed
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        val task4 = Task("Title4", "Description4", true)
        tasksRepository.addTasks(task1, task2, task3, task4)

        statisticsViewModel.start()

        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.empty)).isFalse()
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.activeTasksPresent))
            .isEqualTo(25f)
        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.completedTasksPercent))
            .isEqualTo(75f)
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_CallErrorToDisplay() =
        mainCoroutineRule.runBlockingTest {
            val errorViewModel = StatisticsViewModel(
                DefaultTasksRepository(
                    FakeFailingTasksRemoteDataSource,
                    FakeFailingTasksRemoteDataSource,
                    Dispatchers.Main
                ),
                StatisticsUtils()
            )

            errorViewModel.start()

            assertThat(LiveDataTestUtil.getValue(errorViewModel.empty)).isTrue()
            assertThat(LiveDataTestUtil.getValue(errorViewModel.error)).isTrue()
        }

    @Test
    fun loadTasks_loading() {
        mainCoroutineRule.pauseDispatcher()

        statisticsViewModel.start()

        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.dataLoading)).isTrue()

        mainCoroutineRule.resumeDispatcher()

        assertThat(LiveDataTestUtil.getValue(statisticsViewModel.dataLoading)).isFalse()
    }
}