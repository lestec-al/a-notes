package com.yurhel.alex.anotes.ui.screen_swipes.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.color
import com.yurhel.alex.anotes.shared.text
import com.yurhel.alex.anotes.ui.components.BaseBottomSheet
import com.yurhel.alex.anotes.ui.components.BottomSheetTopRow
import com.yurhel.alex.anotes.ui.components.ColorPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeNotesSheet(
    onDismissRequest: () -> Unit,
    onSave: (String, Color) -> Unit,
    onDeleteSwipeText: () -> Unit,
    copyToClipboard: suspend (str: String, clipboard: Clipboard) -> Unit,
    infoText: String = "",
    initText: String = "",
    forbiddenString: String = "",
    initColor: Color,
    isColorPickerShow: Boolean,
    isDeleteButtonOn: Boolean
) {
    val clipboard = LocalClipboard.current
    val focusRequester = remember { FocusRequester() }
    var editText by remember { mutableStateOf(initText) }
    var editColor by remember { mutableStateOf(initColor) }
    val scope = rememberCoroutineScope()

    BaseBottomSheet(onDismissRequest = onDismissRequest) {
        // Top row
        BottomSheetTopRow(
            infoText = infoText,
            saveAction = {
                onSave(editText, editColor)
                onDismissRequest()
            },
            copyAction = {
                scope.launch { copyToClipboard(editText, clipboard) }
            },
            deleteAction = if (isDeleteButtonOn) {
                {
                    onDeleteSwipeText()
                    onDismissRequest()
                }
            } else null
        )
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
                .padding(10.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            )
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
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