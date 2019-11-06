package notes.arch.practise.addedittask

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
 * Unit tests for the implementation of [AddEditTaskViewModel].
 */
@ExperimentalCoroutinesApi
class AddEditTaskViewModelTest {
    // Subject under test
    private lateinit var addEditTaskViewModel: AddEditTaskViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var tasksRepository: FakeRepository

    // Set the main couroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val task = Task("Title1", "Description")

    @Before
    fun setupViewModel() {
        // We initialise the repository with no tasks
        tasksRepository = FakeRepository()

        // Create class under test
        addEditTaskViewModel = AddEditTaskViewModel(tasksRepository)
    }

    @Test
    fun saveNewTaskToRepository_showsSuccessMessageUi() {
        val newTitle = "New Task Title"
        val newDescription = "Some Task Description"
        (addEditTaskViewModel).apply {
            title.value = newTitle
            description.value = newDescription
        }
        addEditTaskViewModel.saveTask()

        val newTask = tasksRepository.tasksServiceData.values.first()

        // The a task is saved in the repository and the view updated
        assertThat(newTask.title).isEqualTo(newTitle)
        assertThat(newTask.description).isEqualTo(newDescription)
    }

    @Test
    fun loadTasks_loading() {
        mainCoroutineRule.pauseDispatcher()

        addEditTaskViewModel.start(task.id)

        assertThat(getValue(addEditTaskViewModel.dataLoading)).isTrue()

        mainCoroutineRule.resumeDispatcher()

        assertThat(getValue(addEditTaskViewModel.dataLoading)).isFalse()
    }

    @Test
    fun loadTasks_taskShown() {
        // Add task to repository
        tasksRepository.addTasks(task)

        // Load the task with the viewmodel
        addEditTaskViewModel.start(task.id)

        // Verify a task is loaded
        assertThat(getValue(addEditTaskViewModel.title)).isEqualTo(task.title)
        assertThat(getValue(addEditTaskViewModel.description)).isEqualTo(task.description)
        assertThat(getValue(addEditTaskViewModel.dataLoading)).isFalse()
    }

    @Test
    fun saveNewTaskToRepository_emptyTitle_error() {
        saveTaskAndAssertSnackbarError("", "Some Task Description")
    }

    @Test
    fun saveNewTaskToRepository_nullTitle_error() {
        saveTaskAndAssertSnackbarError(null, "Some Task Description")
    }

    @Test
    fun saveNewTaskToRepository_emptyDescription_error() {
        saveTaskAndAssertSnackbarError("Title", "")
    }

    @Test
    fun saveNewTaskToRepository_nullDescription_error() {
        saveTaskAndAssertSnackbarError("Title", null)
    }

    @Test
    fun saveNewTaskToRepository_nullDescriptionNullTitle_error() {
        saveTaskAndAssertSnackbarError(null, null)
    }

    @Test
    fun saveNewTaskToRepository_emptyDescriptionEmptyTitle_error() {
        saveTaskAndAssertSnackbarError("", "")
    }

    private fun saveTaskAndAssertSnackbarError(title: String?, description: String?) {
        (addEditTaskViewModel).apply {
            this.title.value = title
            this.description.value = description
        }

        addEditTaskViewModel.saveTask()

        assertSnackbarMessage(addEditTaskViewModel.snackbarMessage, R.string.empty_task_message)
    }
}
