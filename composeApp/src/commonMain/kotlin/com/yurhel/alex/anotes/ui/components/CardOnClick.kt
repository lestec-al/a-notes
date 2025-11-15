package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CardOnClick(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    cardColor: Color,
    content: @Composable (ColumnScope.() -> Unit)
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = modifier,
            content = content
        )
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = modifier,
            content = content
        )
    }
}