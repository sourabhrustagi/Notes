package notes.arch.practise.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import notes.arch.practise.data.Result
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksRepository
import notes.arch.practise.util.wrapEspressoIdlingResource
import javax.inject.Inject

/**
 * ViewModel for the statistics screen.
 */
class StatisticsViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val statsUtils: StatisticsUtils
) : ViewModel() {
    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    /**
     * Controls whether the stats are shown or a "No data" message.
     */
    private val _empty = MutableLiveData<Boolean>()
    val empty: LiveData<Boolean> = _empty

    private val _activeTasksPercent = MutableLiveData<Float>()
    val activeTasksPresent: LiveData<Float> = _activeTasksPercent

    private val _completedTaskPercent = MutableLiveData<Float>()
    val completedTasksPercent: LiveData<Float> = _completedTaskPercent

    private var activeTasks = 0
    private var completedTasks = 0

    init {
        start()
    }

    fun start() {
        if (_dataLoading.value == true) {
            return
        }
        _dataLoading.value = true
        wrapEspressoIdlingResource {
            viewModelScope.launch {
                tasksRepository.getTasks().let { result ->
                    if (result is Result.Success) {
                        _error.value = false
                        computeStats(result.data)
                    } else {
                        _error.value = true
                        activeTasks = 0
                        completedTasks = 0
                        computeStats(null)
                    }
                }
            }
        }
    }

    fun refresh() {
        start()
    }

    /**
     * Called when new data is ready.
     */
    private fun computeStats(tasks: List<Task>?) {
        statsUtils.getActiveAndCompletedStats(tasks).let {
            _activeTasksPercent.value = it.activeTasksPercent
            _completedTaskPercent.value = it.completedTasksPercent
        }
        _empty.value = tasks.isNullOrEmpty()
        _dataLoading.value = false
    }
}