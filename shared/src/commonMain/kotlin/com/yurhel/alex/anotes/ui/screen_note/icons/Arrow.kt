package com.yurhel.alex.anotes.ui.screen_note.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DividerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ArrowDirection { LEFT, RIGHT, BOTH }

@Composable
fun ArrowHorizontalDivider(
    modifier: Modifier,
    direction: ArrowDirection,
    color: Color = DividerDefaults.color,
    thickness: Dp = DividerDefaults.Thickness,
    arrowSize: Dp = 6.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(arrowSize)
    ) {
        val arrowSizePx = arrowSize.toPx()
        val centerY = size.height / 2f

        // Calculate dynamic line boundaries based on arrow presence
        val lineStartX = if (direction == ArrowDirection.LEFT || direction == ArrowDirection.BOTH) arrowSizePx else 0f
        val lineEndX = if (direction == ArrowDirection.RIGHT || direction == ArrowDirection.BOTH) size.width - arrowSizePx else size.width

        // Draw the horizontal line
        drawLine(
            color = color,
            start = Offset(lineStartX, centerY),
            end = Offset(lineEndX, centerY),
            strokeWidth = thickness.toPx()
        )

        // Draw Left Arrow Head if needed
        if (direction == ArrowDirection.LEFT || direction == ArrowDirection.BOTH) {
            val leftArrowPath = Path().apply {
                moveTo(0f, centerY)
                lineTo(arrowSizePx, centerY - arrowSizePx / 1.5f)
                lineTo(arrowSizePx, centerY + arrowSizePx / 1.5f)
                close()
            }
            drawPath(path = leftArrowPath, color = color)
        }

        // Draw Right Arrow Head if needed
        if (direction == ArrowDirection.RIGHT || direction == ArrowDirection.BOTH) {
            val rightArrowPath = Path().apply {
                moveTo(size.width, centerY)
                lineTo(size.width - arrowSizePx, centerY - arrowSizePx / 1.5f)
                lineTo(size.width - arrowSizePx, centerY + arrowSizePx / 1.5f)
                close()
            }
            drawPath(path = rightArrowPath, color = color)
        }
    }
}