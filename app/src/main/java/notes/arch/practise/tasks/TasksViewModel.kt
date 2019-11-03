package notes.arch.practise.tasks

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import notes.arch.practise.Event
import notes.arch.practise.R
import notes.arch.practise.data.Result
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksRepository
import notes.arch.practise.util.wrapEspressoIdlingResource
import javax.inject.Inject

class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
) : ViewModel() {
    private val _items = MutableLiveData<List<Task>>().apply { value = emptyList() }
    val items: LiveData<List<Task>> = _items

    private val _openTaskEvent = MutableLiveData<Event<String>>()
    val openTaskEvent: LiveData<Event<String>> = _openTaskEvent

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private var _currentFiltering = TasksFilterType.ALL_TASKS

    private val isDataLoadingError = MutableLiveData<Boolean>()

    private val _currentFilteringLabel = MutableLiveData<Int>()
    val currentFilteringLabel: LiveData<Int> = _currentFilteringLabel

    private val _noTaskIconRes = MutableLiveData<Int>()
    val noTaskIconRes: LiveData<Int> = _noTaskIconRes

    private val _noTasksLabel = MutableLiveData<Int>()
    val noTasksLabel: LiveData<Int> = _noTasksLabel

    private val _tasksAddViewVisible = MutableLiveData<Boolean>()
    val tasksAddViewVisible: LiveData<Boolean> = _tasksAddViewVisible

    private val _newTaskEvent = MutableLiveData<Event<Unit>>()
    val newTaskEvent: LiveData<Event<Unit>> = _newTaskEvent

    init {
        setFiltering(TasksFilterType.ALL_TASKS)
        loadTasks(true)
    }

    // This LiveData depends on another so we can use a transformation.
    val empty: LiveData<Boolean> = Transformations.map(_items) {
        it.isEmpty()
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be [TasksFilterType.ALL_TASKS],
     * [TasksFilterType.COMPLETED_TASKS] or
     * [TasksFilterType.ACTIVE_TASKS]
     */
    fun setFiltering(requestType: TasksFilterType) {
        _currentFiltering = requestType

        // Depending on the filter type, set the filtering label, icon drawables, etc.
        when (requestType) {
            TasksFilterType.ALL_TASKS -> {
                setFilter(
                    R.string.label_all, R.string.no_tasks_all,
                    R.drawable.logo_no_fill, true
                )
            }
            TasksFilterType.ACTIVE_TASKS -> {
                setFilter(
                    R.string.label_active, R.string.no_tasks_active,
                    R.drawable.ic_check_circle_96dp, false
                )
            }
            TasksFilterType.COMPLETED_TASKS -> {
                setFilter(
                    R.string.label_completed, R.string.no_tasks_completed,
                    R.drawable.ic_verified_user_96dp, false
                )
            }
        }
    }

    private fun setFilter(
        @StringRes filteringLabelString: Int, @StringRes noTasksLabelString: Int,
        @DrawableRes noTaskIconDrawable: Int, taskAddVisible: Boolean
    ) {
        _currentFilteringLabel.value = filteringLabelString
        _noTasksLabel.value = noTasksLabelString
        _noTaskIconRes.value = noTaskIconDrawable
        _tasksAddViewVisible.value = taskAddVisible
    }

    /**
     * Called by Data Binding.
     */
    fun openTask(taskId: String) {
        _openTaskEvent.value = Event(taskId)
    }

    fun completeTask(task: Task, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            tasksRepository.completeTask(task)
            showSnackbarMessage(R.string.task_marked_complete)
        } else {
            tasksRepository.activateTask(task)
            showSnackbarMessage(R.string.task_marked_active)
        }
        loadTasks(false)
    }

    fun showSnackbarMessage(message: Int) {
        _snackbarText.value = Event(message)
    }

    /**
     * @param forceUpdate Pass in true to refresh the data in the [TasksDataSource]
     */
    fun loadTasks(forceUpdate: Boolean) {
        _dataLoading.value = true

        wrapEspressoIdlingResource {
            viewModelScope.launch {
                val tasksResult = tasksRepository.getTasks(forceUpdate)

                if (tasksResult is Result.Success) {
                    val tasks = tasksResult.data

                    val tasksToShow = ArrayList<Task>()
                    // We filter the tasks based on the requestType
                    for (task in tasks) {
                        when (_currentFiltering) {
                            TasksFilterType.ALL_TASKS -> tasksToShow.add(task)
                            TasksFilterType.ACTIVE_TASKS -> if (task.isActive) {
                                tasksToShow.add(task)
                            }
                            TasksFilterType.COMPLETED_TASKS -> if (task.isCompleted) {
                                tasksToShow.add(task)
                            }
                        }
                    }
                    isDataLoadingError.value = false
                    _items.value = ArrayList(tasksToShow)
                } else {
                    isDataLoadingError.value = false
                    _items.value = emptyList()
                    showSnackbarMessage(R.string.loading_tasks_error)
                }
                _dataLoading.value = false
            }
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            tasksRepository.clearCompletedTasks()
            showSnackbarMessage(R.string.completed_tasks_cleared)
            // Refresh list to show the new state
            loadTasks(false)
        }
    }

    fun refresh() {
        loadTasks(true)
    }

    fun showEditResultMessage(result: Int) {
        when (result) {
            EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_task_message)
            ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_task_message)
            DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_task_message)
        }
    }

    /**
     * Called by the Data Binding library and the FAB's click listener.
     */
    fun addNewTask() {
        _newTaskEvent.value = Event(Unit)
    }
}