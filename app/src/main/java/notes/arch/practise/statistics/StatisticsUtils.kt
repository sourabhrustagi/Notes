package notes.arch.practise.statistics

import notes.arch.practise.data.Task
import javax.inject.Inject

/**
 * Class used to showcase dependency injection scoped to on module ([StatisticsModule])
 */
class StatisticsUtils @Inject constructor() {
    /**
     * Function that does some trivial computation. Used to showcase unit tests.
     */
    fun getActiveAndCompletedStats(tasks: List<Task>?): StatsResult {
        return if (tasks == null || tasks.isEmpty()) {
            StatsResult(0f, 0f)
        } else {
            val totalTasks = tasks.size
            val numberOfActiveTasks = tasks.count { it.isActive }
            StatsResult(
                activeTasksPercent = 100f * numberOfActiveTasks / tasks.size,
                completedTasksPercent = 100f * (totalTasks - numberOfActiveTasks) / tasks.size
            )
        }
    }
}

data class StatsResult(val activeTasksPercent: Float, val completedTasksPercent: Float)