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
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.close_drop_buttons
import com.yurhel.alex.anotes.shared.open_drop_buttons
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DropFloatingActionButton(
    dropDownMenuItems: List<Triple<StringResource, ImageVector, () -> Unit>>
) {
    var isOpened: Boolean by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.End) {
        if (isOpened) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { isOpened = false },
                shape = CardDefaults.shape
            ) {
                dropDownMenuItems.forEach {
                    val text = stringResource(it.first)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = text,
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
                                contentDescription = text
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(5.dp))
        }
        FloatingActionButton(
            onClick = { isOpened = !isOpened },
            shape = CardDefaults.shape,
            content = {
                Icon(
                    imageVector = if (isOpened) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = stringResource(
                        if (isOpened) Res.string.close_drop_buttons else Res.string.open_drop_buttons
                    )
                )
            }
        )
    }
}