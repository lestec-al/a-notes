package com.yurhel.alex.anotes.ui.components

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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.text
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleEditSheet(
    onDismissRequest: () -> Unit,
    onSave: (String) -> Unit,
    copyToClipboard: suspend (str: String) -> Unit,
    infoText: String = "",
    initText: String = ""
) {
    val focusRequester = remember { FocusRequester() }
    var edit by remember { mutableStateOf(initText) }
    val scope = rememberCoroutineScope()

    BaseBottomSheet(onDismissRequest = onDismissRequest) {
        // Top row
        BottomSheetTopRow(
            infoText = infoText,
            saveAction = {
                if (edit.isNotEmpty()) {
                    onSave(edit)
                    onDismissRequest()
                }
            },
            copyAction = {
                scope.launch { copyToClipboard(edit) }
            }
        )
        // Edit text
        TextField(
            value = edit,
            onValueChange = {
                edit = it
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
    }
}