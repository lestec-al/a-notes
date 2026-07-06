package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BaseBottomBar(
    height: Dp = 50.dp,
    withInsets: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val modifier = if (withInsets) Modifier.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)) else Modifier
    BottomAppBar(
        modifier = modifier
            .height(height)
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        windowInsets = WindowInsets(0, 0, 0, 0),
        content = content
    )
}