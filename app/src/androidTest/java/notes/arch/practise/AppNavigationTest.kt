package notes.arch.practise

import android.view.Gravity
import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.NavigationViewActions.navigateTo
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksRepository
import notes.arch.practise.tasks.TasksActivity
import notes.arch.practise.util.EspressoIdlingResource
import notes.arch.practise.utils.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest {
    private lateinit var tasksRepository: TasksRepository

    // An Idling Resource that waits for Data Binding to have no pending bindings
    private val dataBindingIdlingResource = DataBindingIdlingResource()

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
        tasksRepository = rule.component.tasksRepository
        tasksRepository.deleteAllTasksBlocking()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun drawerNavigationFromTasksToStatistics() {
        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START)))
            .perform(open()) // Open Drawer

        // Start statistics screen.
        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.statisticsFragment))

        // Check that statistics screen was opened.
        onView(withId(R.id.statistics)).check(matches(isDisplayed()))

        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START))) // Left Drawer should be closed.
            .perform(open()) // Open Drawer

        // Start tasks screen.
        onView(withId(R.id.nav_view))
            .perform(navigateTo(R.id.tasksFragment))

        // Check that tasks screen was opened.
        onView(withId(R.id.tasksContainer)).check(matches(isDisplayed()))
    }

    @Test
    fun tasksScreen_clickOnAndroidHomeIcon_OpensNavigation() {
        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check that left drawer is closed at startup
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START))) // Left drawer should be closed.

        // Open Drawer
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Check if drawer is open
        onView(withId(R.id.drawer_layout))
            .check(matches(isOpen(Gravity.START))) // Left drawer is open open.
    }

    @Test
    fun statsScreen_clickOnAndroidHomeIcon_OpensNavigation() {
        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When the user navigates to the stats screen
        activityScenario.onActivity {
            it.findNavController(R.id.nav_host_fragment).navigate(R.id.statisticsFragment)
        }

        // Then check that left drawer is closed at startup
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START)))

        // When the drawer is opened
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // Then check that the drawer is open
        onView(withId(R.id.drawer_layout))
            .check(matches(isOpen(Gravity.START))) // Left drawer is open open.
    }

    @Test
    fun taskDetailScreen_doubleUIBackButton() {
        val task = Task("UI <- Button", "Description")
        tasksRepository.saveTaskBlocking(task)

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check on the task on the list
        onView(withText("UI <- Button")).perform(click())
        // Click on the edit task screen
        onView(withId(R.id.fab_edit_task)).perform(click())

        // Confirm that if we click "<-" once, we end up back at the task details page
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())
        onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))

        // Confirm that if we click "<-" a second time, we end up back at the home screen
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())
        onView(withId(R.id.tasksContainer)).check(matches(isDisplayed()))
    }

    @Test
    fun testDetailScreen_doubleBackButton() {
        val task = Task("Back button", "Description")
        tasksRepository.saveTaskBlocking(task)

        // start up Tasks screen
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on the task on the list
        onView(withText("Back button")).perform(click())
        // Click on the edit task button
        onView(withId(R.id.fab_edit_task)).perform(click())

        // Confirm that if we click back once, we end up back at the task details page
        pressBack()
        onView(withId(R.id.task_detail_title)).check(matches(isDisplayed()))

        // Confirm that if we click back a second time, we end up back at the home screen
        pressBack()
        onView(withId(R.id.tasksContainer)).check(matches(isDisplayed()))
    }
}
