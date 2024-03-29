package notes.arch.practise.addedittask

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import notes.arch.practise.DaggerTestApplicationRule
import notes.arch.practise.R
import notes.arch.practise.data.Result
import notes.arch.practise.data.source.TasksRepository
import notes.arch.practise.tasks.ADD_EDIT_RESULT_OK
import notes.arch.practise.utils.deleteAllTasksBlocking
import notes.arch.practise.utils.getTasksBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode
import timber.log.Timber

/**
 * Integration test for the Add Task screen.
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@ExperimentalCoroutinesApi
class AddEditTaskFragmentTest {
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
    fun emptyTask_isNotSaved() {
        // GIVEN - On the "Add Task" screen.
        val bundle = AddEditTaskFragmentArgs(
            null,
            getApplicationContext<Context>().getString(R.string.add_task)
        ).toBundle()
        launchFragmentInContainer<AddEditTaskFragment>(bundle, R.style.AppTheme)

        // WHEN - Enter invalid title and description combination and click save.
        onView(withId(R.id.add_task_title)).perform(clearText())
        onView(withId(R.id.add_task_description)).perform(clearText())
        onView(withId(R.id.fab_save_task)).perform(click())

        // THEN - Entered Task is still displayed (a correct task would close if).
        onView(withId(R.id.add_task_title)).check(matches(isDisplayed()))
    }

    @Test
    fun validTask_navigationBack() {
        // Given - On the "Add Task" screen.
        val navController = mock(NavController::class.java)
        launchFragment(navController)

        // When - Valid title and description combination and click save
        onView(withId(R.id.add_task_title)).perform(replaceText("title"))
        onView(withId(R.id.add_task_description)).perform(replaceText("description"))
        onView(withId(R.id.fab_save_task)).perform(click())

        // Then - Verify that we navigated back to the tasks screen
        verify(navController).navigate(
            AddEditTaskFragmentDirections
                .actionAddEditTaskFragmentToTasksFragment(ADD_EDIT_RESULT_OK)
        )
    }

    @Test
    fun validTask_isSaved() {
        // Given - On the "Add Task" screen.
        val navController = mock(NavController::class.java)
        launchFragment(navController)

        // When - Valid title and description combination and click save
        onView(withId(R.id.add_task_title)).perform(replaceText("title"))
        onView(withId(R.id.add_task_description)).perform(replaceText("description"))
        onView(withId(R.id.fab_save_task)).perform(click())

        // Then - Verify that the repository saved the task
        val tasks = (repository.getTasksBlocking(true) as Result.Success).data
        Timber.d(tasks.size.toString())
        assertEquals(tasks.size, 1)
        assertEquals(tasks[0].title, "title")
        assertEquals(tasks[0].description, "description")
    }

    private fun launchFragment(navController: NavController) {
        val bundle = AddEditTaskFragmentArgs(
            null,
            getApplicationContext<Context>().getString(R.string.add_task)
        ).toBundle()
        val scenario = launchFragmentInContainer<AddEditTaskFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
    }
}
