package com.yurhel.alex.anotes.ui.screen_tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yurhel.alex.anotes.data.Status
import com.yurhel.alex.anotes.data.Task
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.status
import com.yurhel.alex.anotes.shared.task
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.screen_tasks.utils.ActionTypes
import com.yurhel.alex.anotes.ui.screen_tasks.utils.EditDialogObj
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Event
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.milliseconds

class TasksViewModel(val vm: MainViewModel): ViewModel() {
    class Factory(val vm: MainViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T = TasksViewModel(vm = vm) as T
    }

    private val db = vm.db

    val dropDownMenuItems = listOf(
        Triple(Res.string.status, Icons.AutoMirrored.Outlined.Sort) {
            showEditDialog(Types.Status, ActionTypes.Create)
        },
        Triple(Res.string.task, Icons.AutoMirrored.Outlined.StickyNote2) {
            showEditDialog(Types.Task, ActionTypes.Create)
        }
    )

    var statuses by mutableStateOf<List<Status>>(listOf())
        private set
    var tasks by mutableStateOf<List<Task>>(listOf())
        private set
    var selectedStatus by mutableIntStateOf(0)
        private set
    var editDialogObj by mutableStateOf<EditDialogObj?>(null)
        private set
    var dragIdx: Int? = null
        private set
    var foundIdx: Int? = null
        private set

    fun showEditDialog(
        type: Types,
        actionType: ActionTypes,
        selectedObj: Any? = null
    ) {
        editDialogObj = EditDialogObj(type, actionType, selectedObj)
    }

    fun hideEditDialog() { editDialogObj = null }

    fun changeStatus(status: Status) {
        selectedStatus = if (selectedStatus != status.id) status.id else 0
        updateTasksData(isSaveNote = false, isSetTasksIds = false)
    }

    fun editStatus(status: Status) {
        showEditDialog(Types.Status, ActionTypes.Update, status)
    }

    fun updateLastFoundIdx(index: Int) {
        foundIdx = index
    }

    fun onDragStart(idx: Int) {
        if (dragIdx == null) dragIdx = idx
    }

    fun onDragEnd(
        idx: Int,
        task: Task
    ) {
        if (dragIdx == idx) {
            dragIdx = null
            foundIdx?.let {
                onEvent(Event.ChangePos(pos = it, task = task))
                foundIdx = null
            }
        }
    }

    private fun updateTasksData(
        isSaveNote: Boolean = true,
        isSetTasksIds: Boolean = true
    ) {
        val noteId = vm.selectedNote!!.id
        // Get statuses
        viewModelScope.launch(Dispatchers.Default) {
            statuses = db.status.getManyByNote(noteId)
        }
        // Get tasks
        val statusId = selectedStatus
        viewModelScope.launch(Dispatchers.Default) {
            tasks = if (statusId == 0) {
                db.task.getManyByNote(noteId)
            } else {
                db.task.getManyByNoteAndStatus(noteId, statusId)
            }.sortedBy { it.position }
            // Prevent of having problems with drag/drop, because of not unique position vars
            // Just set them to unique values
            if (isSetTasksIds) {
                val sortedTasks = tasks.sortedBy { it.position }
                sortedTasks.forEachIndexed { idx, it ->
                    db.task.update(it.copy(position = idx))
                }
                tasks = sortedTasks
            }
        }
        if (isSaveNote) vm.saveNote(isEditDateForcedUpdate = true)
    }

    fun onEvent(event: Event) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                // Status
                is Event.UpsertStatus -> {
                    val status = event.status
                    if (status.id == 0) db.status.insert(status) else db.status.update(status)
                    updateTasksData()
                }
                is Event.DeleteStatus -> {
                    if (vm.selectedNote != null) {
                        db.status.delete(event.status.id)
                        db.task.deleteManyByStatus(event.status.id)
                        updateTasksData()
                    }
                }
                // Task
                is Event.UpsertTask -> {
                    val task = event.task
                    if (task.id == 0) {
                        db.task.insert(task.copy(position = db.task.getNextPosition(task.note)))
                    } else {
                        db.task.update(task)
                    }
                    delay(200.milliseconds)
                    updateTasksData()
                }
                is Event.DeleteTask -> {
                    db.task.delete(event.task.id)
                    delay(200.milliseconds)
                    updateTasksData()
                }
                is Event.ChangePos -> {
                    val newPos = if (event.pos < 0) 0 else event.pos
                    val oldPos = event.task.position
                    // Change target task position
                    db.task.update(event.task.copy(position = newPos))
                    // Change positions of the tasks that in between
                    val isAfter = newPos > oldPos
                    val range = if (isAfter) (oldPos + 1)..newPos else newPos..<oldPos
                    tasks
                        .sortedBy { it.position }
                        .forEach {
                            if (it.position in range) {
                                db.task.update(it.copy(
                                    // Move objects up or down
                                    position = if (isAfter) it.position - 1 else it.position + 1
                                ))
                            }
                        }
                    updateTasksData()
                }
            }
        }
    }

    fun getTaskTextForNote(): String {
        return buildString {
            append(vm.selectedNote?.text ?: "")
            appendLine()
            tasks.forEach {
                append(it.description)
                appendLine()
                appendLine()
            }
        }
    }

    fun formatDate(date: Long) = vm.platform.formatDate(date)

    fun editTaskSheetOnSave(
        edit: String,
        status: Int,
        statusColor: Color
    ) {
        val initObj = editDialogObj!!

        val valid = when {
            edit.isBlank() -> false
            (initObj.dataType != Types.Task && edit.length > 100) -> false
            else -> true
        }
        if (!valid) return

        when (initObj.actionType) {
            ActionTypes.Create -> {
                when (initObj.dataType) {
                    Types.Status -> {
                        onEvent(
                            Event.UpsertStatus(
                                Status(
                                    title = edit,
                                    color = statusColor.toArgb(),
                                    note = vm.selectedNote!!.id
                                )
                            )
                        )
                    }
                    Types.Task -> {
                        val dateNow = System.currentTimeMillis()
                        onEvent(
                            Event.UpsertTask(
                                Task(
                                    description = edit,
                                    status = status,
                                    note = vm.selectedNote!!.id,
                                    dateCreate = dateNow,
                                    dateUpdate = dateNow,
                                    dateUpdateStatus = dateNow
                                )
                            )
                        )
                    }
                }
            }
            ActionTypes.Update -> {
                when (initObj.dataType) {
                    Types.Status -> {
                        onEvent(
                            Event.UpsertStatus(
                                (initObj.obj as Status).copy(
                                    title = edit,
                                    color = statusColor.toArgb()
                                )
                            )
                        )
                    }
                    Types.Task -> {
                        val oldTask = initObj.obj as Task
                        val dateNow = System.currentTimeMillis()
                        onEvent(
                            Event.UpsertTask(
                                oldTask.copy(
                                    description = edit,
                                    status = status,
                                    dateUpdate = if (edit != oldTask.description) {
                                        dateNow
                                    } else {
                                        oldTask.dateUpdate
                                    },
                                    dateUpdateStatus = if (status != oldTask.status) {
                                        dateNow
                                    } else {
                                        oldTask.dateUpdateStatus
                                    }
                                )
                            )
                        )
                    }
                }
            }
        }
        hideEditDialog()
    }

    init {
        updateTasksData(isSaveNote = false)
    }
}