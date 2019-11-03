package notes.arch.practise.utils

import kotlinx.coroutines.runBlocking
import notes.arch.practise.data.Task
import notes.arch.practise.data.source.TasksRepository

/**
 * A blocking version of TasksRepository.saveTask to minimize the number of times we have to
 * explicitly and <code>runBlocking { ... }</code> in our tests
 */
fun TasksRepository.saveTaskBlocking(task: Task) = runBlocking {
    this@saveTaskBlocking.saveTask(task)
}

fun TasksRepository.getTasksBlocking(forceUpdate: Boolean) = runBlocking {
    this@getTasksBlocking.getTasks(forceUpdate)
}

fun TasksRepository.deleteAllTasksBlocking() = runBlocking {
    this@deleteAllTasksBlocking.deleteAllTasks()
}