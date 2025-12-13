package com.yurhel.alex.anotes.ui.screen_tasks.utils

import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj

interface Event {
    data class UpsertStatus(val status: StatusObj): Event
    data class DeleteStatus(val status: StatusObj): Event
    data class UpsertTask(val task: TasksObj): Event
    data class DeleteTask(val task: TasksObj): Event
    data class ChangePos(
        val pos: Int,
        val task: TasksObj
    ): Event
    data class ShowEditDialog(
        val dataType: Types,
        val actionType: ActionTypes,
        val selectedObj: Any? = null
    ): Event
    object HideEditDialog: Event
}

enum class Types { Status, Task }

enum class ActionTypes { Create, Update }

data class EditDialogObj(
    val dataType: Types,
    val actionType: ActionTypes,
    var obj: Any?
)