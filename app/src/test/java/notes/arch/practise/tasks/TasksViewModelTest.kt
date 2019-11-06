package notes.arch.practise.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import notes.arch.practise.*
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.FakeRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TasksViewModel]
 */
@ExperimentalCoroutinesApi
class TasksViewModelTest {

    // Subject under test
    private lateinit var tasksViewModel: TasksViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        tasksRepository = FakeRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        tasksRepository.addTasks(task1, task2, task3)

        tasksViewModel = TasksViewModel(tasksRepository)
    }

    @Test
    fun loadAllTasksFromRepository_loadingTogglesAndDataLoaded() {
        mainCoroutineRule.pauseDispatcher()

        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        tasksViewModel.loadTasks(true)

        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isTrue()

        mainCoroutineRule.resumeDispatcher()

        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(3)
    }


    @Test
    fun loadActiveTasksFromRepositoryAndLoadIntoView() {
        // Given an initialized TasksViewModel with initialized tasks
        // When loading of Tasks is requested
        tasksViewModel.setFiltering(TasksFilterType.ACTIVE_TASKS)

        // Load tasks
        tasksViewModel.loadTasks(true)

        // Then progress indicator is hidden
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        // And data correctly loaded
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(1)
    }

    @Test
    fun loadCompletedTasksFromRepositoryAndLoadIntoView() {
        tasksViewModel.setFiltering(TasksFilterType.COMPLETED_TASKS)

        tasksViewModel.loadTasks(true)

        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).hasSize(2)
    }

    @Test
    fun loadTasks_error() {
        tasksRepository.setReturnError(true)

        tasksViewModel.loadTasks(true)

        assertThat(LiveDataTestUtil.getValue(tasksViewModel.dataLoading)).isFalse()

        assertThat(LiveDataTestUtil.getValue(tasksViewModel.items)).isEmpty()

        assertSnackbarMessage(tasksViewModel.snackbarMessage, R.string.loading_tasks_error)
    }

    @Test
    fun clickOnFab_showsAddTaskUi() {
        tasksViewModel.addNewTask()

        val value = LiveDataTestUtil.getValue(tasksViewModel.newTaskEvent)
        assertThat(value.getContentIfNotHandled()).isNotNull()
    }

    @Test
    fun clickOnOpenTask_setsEvent() {
        val taskId = "42"

        tasksViewModel.openTask(taskId)

        assertLiveDataEventTriggered(tasksViewModel.openTaskEvent, taskId)
    }

    @Test
    fun clearCompletedTasks_clearsTasks() = mainCoroutineRule.runBlockingTest {
        tasksViewModel.clearCompletedTasks()

        tasksViewModel.loadTasks(true)

        val allTasks = LiveDataTestUtil.getValue(tasksViewModel.items)
        val completedTasks = allTasks.filter { it.isCompleted }

        assertThat(completedTasks).isEmpty()

        assertThat(allTasks).hasSize(1)

        assertSnackbarMessage(
            tasksViewModel.snackbarMessage, R.string.completed_tasks_cleared
        )
    }

    @Test
    fun showEditResultMessages_editOk_snackbarUpdated() {
        tasksViewModel.showEditResultMessage(EDIT_RESULT_OK)

        assertSnackbarMessage(
            tasksViewModel.snackbarMessage, R.string.successfully_saved_task_message
        )
    }

    @Test
    fun showEditResultMessages_addOk_snackbarUpdated() {
        tasksViewModel.showEditResultMessage(ADD_EDIT_RESULT_OK)

        assertSnackbarMessage(
            tasksViewModel.snackbarMessage, R.string.successfully_added_task_message
        )
    }

    @Test
    fun showEditResultMessages_deleteOk_snackbarUpdated() {
        tasksViewModel.showEditResultMessage(DELETE_RESULT_OK)

        assertSnackbarMessage(
            tasksViewModel.snackbarMessage, R.string.successfully_deleted_task_message
        )
    }

    @Test
    fun completeTask_dataAndSnackbarUpdated() {
        // With a repository that has an active task
        val task = Task("Title", "Description")
        tasksRepository.addTasks(task)

        // Complete task
        tasksViewModel.completeTask(task, true)

        // Verify the task is completed
        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted).isTrue()

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarMessage, R.string.task_marked_complete
        )
    }

    @Test
    fun activateTask_dataAndSnackbarUpdated() {
        // With a repository that has a completed task
        val task = Task("Title", "Description", true)
        tasksRepository.addTasks(task)

        // Activate task
        tasksViewModel.completeTask(task, false)

        // Verify the task is active
        assertThat(tasksRepository.tasksServiceData[task.id]?.isActive).isTrue()

        // The snackbar is updated
        assertSnackbarMessage(
            tasksViewModel.snackbarMessage, R.string.task_marked_active
        )
    }

    @Test
    fun getTasksAddViewVisible() {
        // When the filter type is ALL_TASKS
        tasksViewModel.setFiltering(TasksFilterType.ALL_TASKS)

        // Then the "Add task" action is visible
        assertThat(LiveDataTestUtil.getValue(tasksViewModel.tasksAddViewVisible)).isTrue()
    }
}