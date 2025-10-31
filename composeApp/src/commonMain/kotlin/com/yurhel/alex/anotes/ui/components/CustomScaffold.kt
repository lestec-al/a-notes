package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun CustomScaffold(
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    statusBarColorAlpha: Float = 0.5f,
    content: @Composable (bottom: Dp, top: Dp) -> Unit
) {
    Scaffold(
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        val topPadding = paddingValues.calculateTopPadding()
        val bottomPadding = paddingValues.calculateBottomPadding()
        content(bottomPadding, topPadding)
        // Top bar placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topPadding)
                .background(color = MaterialTheme.colorScheme.background.copy(alpha = statusBarColorAlpha))
        )
    }
}