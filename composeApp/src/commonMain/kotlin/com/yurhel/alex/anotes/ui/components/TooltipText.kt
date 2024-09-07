package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TooltipText(
    text: String,
    tooltipText: String,
    coroutineScope: CoroutineScope
) {
    val tooltipState = remember { TooltipState() }
    val popupProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()

    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 0.dp, 15.dp, 10.dp)
    ) {
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
            focusable = false,
            enableUserInput = false
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .clip(shape = CardDefaults.shape)
                    .clickable {
                        coroutineScope.launch {
                            tooltipState.show()
                        }
                    }
            )
        }
    }
}