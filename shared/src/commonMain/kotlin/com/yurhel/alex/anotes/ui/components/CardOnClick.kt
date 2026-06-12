package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CardOnClick(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    cardColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val color = CardDefaults.cardColors(containerColor = cardColor ?: Color.Transparent)
    val border = if (borderColor == null) null else BorderStroke(width = 1.5.dp, color = borderColor)
    if (onClick != null) {
        Card(
            onClick = onClick,
            colors = color,
            modifier = modifier,
            border = border,
            content = content
        )
    } else {
        Card(
            colors = color,
            modifier = modifier,
            border = border,
            content = content
        )
    }
}