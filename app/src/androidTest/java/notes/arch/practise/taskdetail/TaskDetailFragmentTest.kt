package notes.arch.practise.taskdetail

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import notes.arch.practise.DaggerTestApplicationRule
import notes.arch.practise.R
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksRepository
import notes.arch.practise.utils.deleteAllTasksBlocking
import notes.arch.practise.utils.saveTaskBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for the Task Details screen.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class TaskDetailFragmentTest {

    private lateinit var repository: TasksRepository

    /**
     * Sets up Dagger components for testing.
     */
    @get:Rule
    val rule = DaggerTestApplicationRule()

    /**
     * Gets a reference to the [TasksRepository] exposed by the [DaggerTestApplicationRule].
     */
    @Before
    fun setupDaggerComponent() {
        repository = rule.component.tasksRepository
        repository.deleteAllTasksBlocking()
    }

    @Test
    fun activeTaskDetails_DisplayedUi() {
        // Given - Add active (incomplete) task to the Db
        val activeTask = Task("Active Task", "AndroidX Rocks", false)
        repository.saveTaskBlocking(activeTask)

        // When - Details fragment launched to display task
        val bundle = TaskDetailFragmentArgs(activeTask.id).toBundle()
        launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)

        // Then - Task details are displayed on the screen
        // make sure that the title/description are both shown and correct
        onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))
        onView(withId(R.id.task_detail_title)).check(matches(withText("Active Task")))
        onView(withId(R.id.task_detail_description)).check(matches(isDisplayed()))
        onView(withId(R.id.task_detail_description)).check(matches(withText("AndroidX Rocks")))
        // and make sure the "active" checkbox is shown unchecked
        onView(withId(R.id.task_detail_complete)).check(matches(isDisplayed()))
        onView(withId(R.id.task_detail_complete)).check(matches(not(isChecked())))
    }

    @Test
    fun completeTaskDetails_DisplayedUi() {
        // Given - Add completed task to the DB
        val completedTask = Task("Completed Task", "AndroidX Rocks", true)
        repository.saveTaskBlocking(completedTask)

        // When - Details fragment launched to display task
        val bundle = TaskDetailFragmentArgs(completedTask.id).toBundle()
        launchFragmentInContainer<TaskDetailFragment>(bundle, R.style.AppTheme)

        // Then - Task details are displayed on the screen
        // make sure that the title/description are both shown and correct
        onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))
        onView(withId(R.id.task_detail_title)).check(matches(withText("Completed Task")))
        onView(withId(R.id.task_detail_description)).check(matches(isDisplayed()))
        onView(withId(R.id.task_detail_description)).check(matches(withText("AndroidX Rocks")))
        // and make sure the "active" checkbox is shown unchecked
        onView(withId(R.id.task_detail_complete)).check(matches(isDisplayed()))
        onView(withId(R.id.task_detail_complete)).check(matches(isChecked()))
    }
}