package notes.arch.practise.taskdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import notes.arch.practise.LiveDataTestUtil.getValue
import notes.arch.practise.MainCoroutineRule
import notes.arch.practise.R
import notes.arch.practise.assertSnackbarMessage
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.FakeRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for the implementation of [TaskDetailViewModel]
 */
@ExperimentalCoroutinesApi
class TaskDetailViewModelTest {

    // Subject under test
    private lateinit var taskDetailViewModel: TaskDetailViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val task = Task("Title1", "Description1")

    @Before
    fun setupViewModel() {
        tasksRepository = FakeRepository()
        tasksRepository.addTasks(task)

        taskDetailViewModel = TaskDetailViewModel(tasksRepository)
    }

    @Test
    fun getActiveTaskFromRepositoryAndLoadIntoView() {
        taskDetailViewModel.start(task.id)

        assertThat(getValue(taskDetailViewModel.task).title).isEqualTo(task.title)
        assertThat(getValue(taskDetailViewModel.task).description).isEqualTo(task.description)
    }

    @Test
    fun completeTask() {
        taskDetailViewModel.start(task.id)

        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted).isFalse()

        taskDetailViewModel.setCompleted(true)

        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted).isTrue()
        assertSnackbarMessage(taskDetailViewModel.snackbarMessage, R.string.task_marked_complete)
    }

    @Test
    fun activateTask() {
        task.isCompleted = true

        taskDetailViewModel.start(task.id)

        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted).isTrue()

        taskDetailViewModel.setCompleted(false)

        assertThat(tasksRepository.tasksServiceData[task.id]?.isCompleted).isFalse()
        assertSnackbarMessage(taskDetailViewModel.snackbarMessage, R.string.task_marked_active)
    }

    @Test
    fun taskDetailViewModel_repositoryError() {
        tasksRepository.setReturnError(true)

        taskDetailViewModel.start(task.id)

        assertThat(getValue(taskDetailViewModel.isDataAvailable)).isFalse()
    }

    @Test
    fun updateSnackbar_nullValue() {
        val snackbarText = taskDetailViewModel.snackbarMessage.value

        assertThat(snackbarText).isNull()
    }

    @Test
    fun clickOnEditTask_SetsEvent() {
        taskDetailViewModel.editTask()

        val value = getValue(taskDetailViewModel.editTaskCommand)
        assertThat(value.getContentIfNotHandled()).isNotNull()
    }

    @Test
    fun deleteTask() {
        assertThat(tasksRepository.tasksServiceData.containsValue(task)).isTrue()
        taskDetailViewModel.start(task.id)

        taskDetailViewModel.deleteTask()

        assertThat(tasksRepository.tasksServiceData.containsValue(task)).isFalse()
    }

    @Test
    fun loadTask_loading() {
        mainCoroutineRule.pauseDispatcher()

        taskDetailViewModel.start(task.id)

        assertThat(getValue(taskDetailViewModel.dataLoading)).isTrue()

        mainCoroutineRule.resumeDispatcher()

        assertThat(getValue(taskDetailViewModel.dataLoading)).isFalse()
    }
}