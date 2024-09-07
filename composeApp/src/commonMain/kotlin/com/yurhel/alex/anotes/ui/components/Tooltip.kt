package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(
    tooltipText: String,
    content: @Composable () -> Unit
) {
    val tooltipState = remember { TooltipState() }
    val popupProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()

    TooltipBox(
        positionProvider = popupProvider,
        tooltip = {
            Card {
                Text(
                    text = tooltipText,
                    modifier = Modifier.padding(5.dp)
                )
            }
        },
        state = tooltipState,
        content = content
    )
}