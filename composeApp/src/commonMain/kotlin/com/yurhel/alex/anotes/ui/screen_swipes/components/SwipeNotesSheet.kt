package com.yurhel.alex.anotes.ui.screen_swipes.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.color
import anotes.composeapp.generated.resources.copy
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.save
import anotes.composeapp.generated.resources.text
import com.yurhel.alex.anotes.ui.components.ColorPicker
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeNotesSheet(
    onDismissRequest: () -> Unit,
    onSave: (String, Color) -> Unit,
    onDeleteSwipeText: () -> Unit,
    infoText: String = "",
    initText: String = "",
    forbiddenString: String = "",
    initColor: Color,
    isColorPickerShow: Boolean,
    isDeleteButtonOn: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState()
    var editText by remember { mutableStateOf(initText) }
    var editColor by remember { mutableStateOf(initColor) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info text
            Text(
                text = infoText,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.weight(1f))
            // Copy content button
            IconButton(
                onClick = {
                    clipboardManager.setText(buildAnnotatedString { append(text = editText) })
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CopyAll,
                    contentDescription = stringResource(Res.string.copy)
                )
            }
            // Delete button
            if (isDeleteButtonOn) {
                IconButton(
                    onClick = {
                        onDeleteSwipeText()
                        onDismissRequest()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.delete)
                    )
                }
            }
            // Save button
            IconButton(
                onClick = {
                    onSave(editText, editColor)
                    onDismissRequest()
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Save,
                    contentDescription = stringResource(Res.string.save)
                )
            }
        }
        // Edit text
        TextField(
            value = editText,
            onValueChange = {
                if (forbiddenString !in it) {
                    editText = it
                }
            },
            label = {
                Text(text = stringResource(Res.string.text))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            )
        )
        // Color picker
        if (isColorPickerShow) {
            Text(
                text = stringResource(Res.string.color),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            ColorPicker(
                onColorChooserClick = {
                    editColor = it
                },
                initColor = initColor
            )
        }
    }
}