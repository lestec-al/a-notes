package com.yurhel.alex.anotes.ui.feature_swipes.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.ui.components.CardOnClick
import com.yurhel.alex.anotes.ui.feature_swipes.utils.SwipeTextObj
import com.yurhel.alex.anotes.ui.feature_swipes.utils.SwipeTextPos
import kotlin.math.roundToInt

@Composable
fun SwipeNotesCard(
    onClick: (() -> Unit)?,
    onDragStopped: ((Float) -> Unit)?,
    leftColor: Color,
    rightColor: Color,
    obj: SwipeTextObj
) {
    var offsetX by remember { mutableStateOf(0f) }
    var modifier = Modifier
        .padding(5.dp)
        .fillMaxWidth(if (obj.pos == SwipeTextPos.Left) 0.9f else 1f)
    if (onDragStopped != null) {
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val offsetTemp = offsetX + delta
                    offsetX = offsetTemp
                },
                onDragStopped = { _ ->
                    onDragStopped(offsetX)
                    offsetX = 0f
                }
            )
    }
    Row {
        if (obj.pos == SwipeTextPos.Right) {
            Spacer(Modifier.fillMaxWidth(0.1f))
        }
        CardOnClick(
            modifier = modifier,
            onClick = onClick,
            cardColor = if (obj.pos == SwipeTextPos.Left) leftColor else rightColor
        ) {
            Text(
                text = obj.text,
                modifier = Modifier.padding(5.dp),
                color = ButtonDefaults.buttonColors().contentColor,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}