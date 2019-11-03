package notes.arch.practise.addedittask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import notes.arch.practise.Event
import notes.arch.practise.R
import notes.arch.practise.data.Result
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksRepository
import javax.inject.Inject

/**
 * ViewModel for the Add/Edit screen.
 */
class AddEditTaskViewModel @Inject constructor(
    private val tasksRepository: TasksRepository
) : ViewModel() {
    val title = MutableLiveData<String>()

    val description = MutableLiveData<String>()

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarMessage: LiveData<Event<Int>> = _snackbarText

    private val _taskUpdated = MutableLiveData<Event<Unit>>()
    val taskUpdatedEvent: LiveData<Event<Unit>> = _taskUpdated

    private var taskId: String? = null

    private var isNewTask: Boolean = false

    private var isDataLoaded = false

    private var taskCompleted = false

    fun start(taskId: String?) {
        if (_dataLoading.value == true) {
            return
        }

        this.taskId = taskId
        if (taskId == null) {
            // No need to populate, it's a new task
            isNewTask = true
            return
        }
        if (isDataLoaded) {
            return
        }
        isNewTask = false
        _dataLoading.value = true

        viewModelScope.launch {
            tasksRepository.getTask(taskId).let { result ->
                if (result is Result.Success) {
                    onTaskLoaded(result.data)
                } else {
                    onDataNotAvailable()
                }
            }
        }
    }

    private fun onTaskLoaded(task: Task) {
        title.value = task.title
        description.value = task.description
        taskCompleted = task.isCompleted
        _dataLoading.value = false
        isDataLoaded = true
    }

    fun onDataNotAvailable() {
        _dataLoading.value = false
    }

    // Called when clicked on fab
    fun saveTask() {
        val currentTitle = title.value
        val currentDescription = description.value
        if (currentTitle == null || currentDescription == null) {
            _snackbarText.value = Event(R.string.empty_task_message)
        }
        currentTitle?.let { _currentTitle ->
            currentDescription?.let { _currentDescription ->
                if (Task(_currentTitle, _currentDescription).isEmpty) {
                    _snackbarText.value = Event(R.string.empty_task_message)
                    return
                }

                val currentTaskId = taskId
                if (isNewTask || currentTaskId == null) {
                    createTask(Task(_currentTitle, _currentDescription))
                } else {
                    val task = Task(currentTitle, currentDescription, taskCompleted, currentTaskId)
                    updateTask(task)
                }
            }
        }
    }

    private fun createTask(newTask: Task) = viewModelScope.launch {
        tasksRepository.saveTask(newTask)
        _taskUpdated.value = Event(Unit)
    }

    private fun updateTask(task: Task) {
        if (isNewTask) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        viewModelScope.launch {
            tasksRepository.saveTask(task)
            _taskUpdated.value = Event(Unit)
        }
    }
}