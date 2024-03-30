package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TooltipText(
    text: String,
    tooltipText: String,
) {
    Box(
        contentAlignment = Alignment.CenterEnd,
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 0.dp, 15.dp, 10.dp)
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
            delayMillis = 1000
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}