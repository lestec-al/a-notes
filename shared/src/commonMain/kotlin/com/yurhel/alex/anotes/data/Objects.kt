package com.yurhel.alex.anotes.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.serialization.json.JsonArray

data class Note(
    val id: Int = 0,
    val text: String,
    val folder: Int,
    val dateUpdate: Long,
    val dateCreate: Long,
    val type: String
)

data class Status(
    val id: Int = 0,
    val title: String,
    val color: Int,
    val note: Int
)

data class Task(
    val id: Int = 0,
    var position: Int = 0,
    val description: String,
    // Foreign keys
    val status: Int,
    val note: Int,
    // Date
    val dateCreate: Long,
    val dateUpdate: Long,
    val dateUpdateStatus: Long
)

data class Widget(
    val id: Int = 0,
    val widgetId: Int,
    val noteCreated: String
)

data class WinScreenSettings(
    val width: Dp,
    val height: Dp,
    val posX: Dp,
    val posY: Dp
)

data class BoardLine(
    val noteId: Int,
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float
)

data class DriveData(
    val data: JsonArray,
    val modifiedTime: Long?,
    val isServiceOK: Boolean
)

enum class SyncType { drive, local }