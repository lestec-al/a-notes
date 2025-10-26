package com.yurhel.alex.anotes.feature_board.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class LineObj(
    val noteId: Int,
    val start: Offset,
    val end: Offset,
    val color: Color,
    val strokeWidth: Float
)