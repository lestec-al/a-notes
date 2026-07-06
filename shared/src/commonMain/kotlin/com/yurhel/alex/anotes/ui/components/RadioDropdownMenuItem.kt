package com.yurhel.alex.anotes.ui.components

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun RadioDropdownMenuItem(
    onClick: () -> Unit,
    text: String,
    isSelected: Boolean
) {
    DropdownMenuItem(
        text = {
            Text(text)
        },
        leadingIcon = {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        },
        onClick = onClick
    )
}