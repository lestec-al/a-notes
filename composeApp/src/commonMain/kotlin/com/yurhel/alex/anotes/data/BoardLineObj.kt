package com.yurhel.alex.anotes.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class BoardLineObj(
    val noteId: Int,
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float
)