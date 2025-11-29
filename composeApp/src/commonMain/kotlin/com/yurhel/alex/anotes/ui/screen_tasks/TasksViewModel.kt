package com.yurhel.alex.anotes.ui.screen_tasks

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.status
import anotes.composeapp.generated.resources.task
import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.screen_tasks.utils.ActionTypes
import com.yurhel.alex.anotes.ui.screen_tasks.utils.EditDialogObj
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Event
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Types
import com.yurhel.alex.anotes.ui.screen_tasks.utils.offsetEnd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class TasksViewModel(val vm: MainViewModel): ViewModel() {
    class Factory(val vm: MainViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T = TasksViewModel(vm = vm) as T
    }

    private val db = vm.db

    val taskScreenDropMenuItems = listOf(
        Triple(Res.string.status, Icons.AutoMirrored.Outlined.Sort) {
            onEvent(Event.ShowEditDialog(Types.Status, ActionTypes.Create))
        },
        Triple(Res.string.task, Icons.AutoMirrored.Outlined.StickyNote2) {
            onEvent(Event.ShowEditDialog(Types.Task, ActionTypes.Create))
        }
    )

    var statuses by mutableStateOf<List<StatusObj>>(listOf())
        private set
    var tasks by mutableStateOf<List<TasksObj>>(listOf())
        private set
    var selectedStatus by mutableStateOf(0)
        private set
    var draggingObj by mutableStateOf<Int?>(null)
        private set
    var lastFoundedIdx by mutableStateOf<Int?>(null)
        private set
    var isEditTextSheetOpen by mutableStateOf(false)
        private set
    var editDialogObj by mutableStateOf<EditDialogObj?>(null)
        private set

    fun changeStatus(status: StatusObj) {
        selectedStatus = if (selectedStatus != status.id) status.id else 0
        updateTasksData(false)
    }
    fun editStatus(status: StatusObj) {
        onEvent(
            Event.ShowEditDialog(
                Types.Status,
                ActionTypes.Update,
                status
            )
        )
    }

    fun updateIsEditSheetOpen(value: Boolean = false) {
        isEditTextSheetOpen = value
    }

    fun onSaveSimpleEditSheetInTasksScreen(it: String) {
        vm.updateEditTextValue(it)
        vm.saveNote()
    }

    fun onDragStart(idx: Int) {
        if (draggingObj == null) draggingObj = idx
    }
    fun onDragEnd(
        idx: Int,
        task: TasksObj
    ) {
        if (draggingObj == idx) {
            draggingObj = null
            val lastFoundedIdxSt = lastFoundedIdx
            if (lastFoundedIdxSt != null) {
                onEvent(Event.ChangePos(pos = lastFoundedIdxSt, task = task))
                lastFoundedIdx = null
            }
        }
    }
    fun onDrag(
        offsetY: Float,
        posTop: Float,
        posBottom: Float,
        itemIdx: Int,
        scrollOffset: Int,
        visibleItemsInfo: List<LazyListItemInfo>
    ) {
        val posTopDynamic = (posTop + offsetY).toInt()
        val posBottomDynamic = (posBottom + offsetY).toInt()
        // Check offsets of all items
        // Try to find that touching item
        for (it in visibleItemsInfo) {
            if (itemIdx != it.index) {
                for (offset in (it.offset + scrollOffset)..(it.offsetEnd - scrollOffset)) {
                    if (offset in (posTopDynamic + scrollOffset)..(posBottomDynamic - scrollOffset)) {
                        lastFoundedIdx = it.index
                    }
                }
            }
        }
    }

    private fun updateTasksData(isSaveNote: Boolean) {
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
        }
        if (isSaveNote) vm.saveNote(isEditDateForcedUpdate = true)
    }

    fun onEvent(event: Event) {
        viewModelScope.launch(Dispatchers.Default) {
            when (event) {
                // Status
                is Event.UpsertStatus -> {
                    if (event.status.id == 0) db.status.insert(event.status) else db.status.update(event.status)
                    updateTasksData(true)
                }
                is Event.DeleteStatus -> {
                    if (vm.selectedNote != null) {
                        db.status.delete(event.status.id)
                        db.task.deleteManyByStatus(event.status.id)
                        updateTasksData(true)
                    }
                }
                // Task
                is Event.UpsertTask -> {
                    if (event.task.id == 0) {
                        db.task.insert(
                            event.task.copy(
                                position = db.task.getHowManyTasksNoteHas(event.task.note)
                            )
                        )
                    } else {
                        db.task.update(event.task)
                    }
                    delay(200)
                    updateTasksData(true)
                }
                is Event.DeleteTask -> {
                    db.task.delete(event.task.id)
                    delay(200)
                    updateTasksData(true)
                }
                is Event.ChangePos -> {
                    val newIdx = event.pos - 2
                    val newPos = when {
                        newIdx < 0 -> 0
                        else -> newIdx
                    }
                    val oldPos = event.task.position
                    tasks
                        .sortedBy { it.position }
                        .forEach {
                            if (newPos > oldPos) {
                                // Next
                                if (it.position in (oldPos + 1)..newPos) {
                                    db.task.update(it.copy(position = it.position - 1))
                                }
                            } else {
                                // Prev
                                if (it.position in newPos..<oldPos) {
                                    db.task.update(it.copy(position = it.position + 1))
                                }
                            }
                        }
                    db.task.update(event.task.copy(position = newPos))
                    updateTasksData(true)
                    // Prevent of having problems while drag/drop, because of not unique position vars
                    // Just set them to unique values
                    launch {
                        delay(1000)
                        tasks
                            .sortedBy { it.position }
                            .forEachIndexed { idx, it ->
                                db.task.update(it.copy(position = idx))
                            }
                    }
                }
                // Others
                is Event.ShowEditDialog -> {
                    editDialogObj = EditDialogObj(event.dataType, event.actionType, event.selectedObj)
                }
                Event.HideEditDialog -> {
                    editDialogObj = null
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

    init {
        updateTasksData(false)
    }
}