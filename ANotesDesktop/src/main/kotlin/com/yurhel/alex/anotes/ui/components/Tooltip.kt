package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Tooltip(
    tooltipText: String,
    content: @Composable () -> Unit
) {
    TooltipArea(
        tooltip = {
            Card {
                Text(
                    text = tooltipText,
                    modifier = Modifier.padding(5.dp)
                )
            }
        },
        tooltipPlacement = TooltipPlacement.ComponentRect(alignment = Alignment.TopCenter, anchor = Alignment.TopCenter),
        delayMillis = 1000,
        content = content
    )
}