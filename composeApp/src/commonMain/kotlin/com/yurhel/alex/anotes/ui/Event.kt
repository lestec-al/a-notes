package com.yurhel.alex.anotes.ui

import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj

interface Event {
    // Status
    data class UpsertStatus(val status: StatusObj): Event
    data class DeleteStatus(val status: StatusObj): Event
    // Task
    data class UpsertTask(val task: TasksObj): Event
    data class DeleteTask(val task: TasksObj): Event
    data class ChangePos(
        val pos: Pos,
        val task: TasksObj
    ): Event
    // Others
    data class ShowEditDialog(
        val dataType: Types,
        val actionType: ActionTypes,
        val selectedObj: Any? = null
    ): Event
    object HideEditDialog: Event
}