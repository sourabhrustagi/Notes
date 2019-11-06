package notes.arch.practise.tasks

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.hamcrest.core.IsNot
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.TextLayoutMode

/**
 * Integration test for the Task List screen.
 */
// TODO - Use FragmentScenario, see: https://github.com/android/android-test/issues/291
@RunWith(AndroidJUnit4::class)
@MediumTest
@LooperMode(LooperMode.Mode.PAUSED)
@TextLayoutMode(TextLayoutMode.Mode.REALISTIC)
@ExperimentalCoroutinesApi
class TasksFragmentTest {

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
    fun displayTask_whenRepositoryHasData() {
        // Given - One task already in the repository
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        launchActivity()
        onView(withText("TITLE1")).check(matches(isDisplayed()))
    }

    @Test
    fun displayActiveTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        launchActivity()

        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("TITLE1")).check(matches(not(isDisplayed())))
    }

    @Test
    fun displayCompletedTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1", true))

        launchActivity()

        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("TITLE1")).check(matches(IsNot.not(isDisplayed())))

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
    }

    @Test
    fun deleteOneTask() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        launchActivity()

        onView(withText("TITLE1")).perform(click())

        onView(withId(R.id.menu_delete)).perform(click())

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(doesNotExist())
    }

    @Test
    fun deleteOneOfTwoTasks() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2"))

        launchActivity()

        onView(withText("TITLE1")).perform(click())

        onView(withId(R.id.menu_delete)).perform(click())

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(doesNotExist())

        onView(withText("TITLE2")).check(matches(isDisplayed()))
    }

    @Test
    fun markTaskAsComplete() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))

        launchActivity()

        onView(checkboxWithText("TITLE1")).perform(click())

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("TITLE1")).check(matches(not(isDisplayed())))
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
    }

    @Test
    fun markTaskAsActive() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1", true))

        launchActivity()

        onView(checkboxWithText("TITLE1")).perform(click())

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("TITLE1")).check(matches(not(isDisplayed())))
    }

    @Test
    fun showAllTasks() {
        // Add one active task and one completed task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2", true))

        launchActivity()

        // Verify that both of our tasks are shown
        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("TITLE2")).check(matches(isDisplayed()))
    }

    @Test
    fun showActiveTasks() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2"))
        repository.saveTaskBlocking(Task("TITLE3", "DESCRIPTION3", true))

        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("TITLE2")).check(matches(isDisplayed()))
        onView(withText("TITLE3")).check(doesNotExist())
    }

    @Test
    fun showCompletedTasks() {
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2", true))
        repository.saveTaskBlocking(Task("TITLE3", "DESCRIPTION3", true))

        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())
        onView(withText("TITLE1")).check(doesNotExist())
        onView(withText("TITLE2")).check(matches(isDisplayed()))
        onView(withText("TITLE3")).check(matches(isDisplayed()))
    }

    @Test
    fun clearCompletedTasks() {
        // Add one active task and one completed task
        repository.saveTaskBlocking(Task("TITLE1", "DESCRIPTION1"))
        repository.saveTaskBlocking(Task("TITLE2", "DESCRIPTION2", true))

        launchActivity()

        // Click clear completed in menu
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText(R.string.menu_clear)).perform(click())

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())
        // Verify that only the active task is shown
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("TITLE2")).check(doesNotExist())
    }

    @Test
    fun noTasks_AllTasksFilter_AddTaskViewVisible() {
        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_all)).perform(click())

        onView(withText("You have no tasks!")).check(matches(isDisplayed()))
    }

    @Test
    fun noTasks_CompletedTasksFilter_AddTaskViewNotVisible() {
        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_completed)).perform(click())

        onView(withText("You have no completed tasks!")).check(matches((isDisplayed())))
    }

    @Test
    fun noTasks_ActiveTasksFilter_AddTaskViewNotVisible() {
        launchActivity()

        onView(withId(R.id.menu_filter)).perform(click())
        onView(withText(R.string.nav_active)).perform(click())

        // Verify the "You have no active tasks!" text is shown
        onView(withText("You have no active tasks!")).check(matches((isDisplayed())))
    }

    @Test
    fun clickAddTaskButton_navigateToAddEditFragment() {
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.fab_add_task)).perform(click())

        verify(navController).navigate(
            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                null, getApplicationContext<Context>().getString(R.string.add_task)
            )
        )
    }

    private fun launchActivity(): ActivityScenario<TasksActivity>? {
        val activityScenario = launch(TasksActivity::class.java)
        activityScenario.onActivity { activity ->
            //Disable animations in RecyclerView
            (activity.findViewById(R.id.tasks_list) as RecyclerView).itemAnimator = null
        }
        return activityScenario
    }

    private fun checkboxWithText(text: String): Matcher<View> {
        return allOf(withId(R.id.complete), hasSibling(withText(text)))
    }
}