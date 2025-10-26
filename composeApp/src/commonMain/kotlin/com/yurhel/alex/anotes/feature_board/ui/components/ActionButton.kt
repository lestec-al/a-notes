package com.yurhel.alex.anotes.feature_board.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    onClick: () -> Unit,
    isActive: Boolean,
    icon: ImageVector,
    contentDescription: String = ""
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CardDefaults.shape,
        containerColor = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            FloatingActionButtonDefaults.containerColor
        },
        contentColor = if (isActive) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            contentColorFor(FloatingActionButtonDefaults.containerColor)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(30.dp)
        )
    }
}