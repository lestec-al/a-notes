package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DropFloatingActionButton(dropDownMenuItems: List<Triple<String, ImageVector, () -> Unit>>) {
    var isOpened: Boolean by remember { mutableStateOf(false) }
    if (isOpened) {
        Column(horizontalAlignment = Alignment.End) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = {
                    isOpened = false
                },
                shape = CardDefaults.shape
            ) {
                dropDownMenuItems.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = it.first,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        onClick = {
                            isOpened = false
                            it.third()
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = it.second,
                                contentDescription = it.first
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
            // Close buttons
            FloatingActionButton(
                onClick = {
                    isOpened = false
                },
                shape = CardDefaults.shape,
                content = {
                    Icon(Icons.Default.Close, null)
                }
            )
        }
    } else {
        FloatingActionButton(
            shape = CardDefaults.shape,
            onClick = {
                isOpened = true
            },
            content = {
                Icon(Icons.Default.Add, "")
            }
        )
    }
}