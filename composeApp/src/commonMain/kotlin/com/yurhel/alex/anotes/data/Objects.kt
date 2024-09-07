package com.yurhel.alex.anotes.data

data class NoteObj(
    val id: Int = 0,
    val withTasks: Boolean = false,
    val text: String,
    // Date
    val dateUpdate: Long,
    val dateCreate: Long
)

data class SettingsObj(
    val dataReceivedDate: Long?,
    val isNotesEdited: Boolean,
    var viewMode: String
)

data class StatusObj(
    val id: Int = 0,
    val title: String,
    val color: Int,
    val note: Int
)

data class TasksObj(
    val id: Int = 0,
    var position: Int = -1,
    val description: String,
    // Foreign keys
    val status: Int,
    val note: Int,
    // Date
    val dateCreate: Long,
    val dateUpdate: Long,
    val dateUpdateStatus: Long
)

data class WidgetObj(
    val id: Int = 0,
    val widgetId: Int,
    val noteCreated: String
)