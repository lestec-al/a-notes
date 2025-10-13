package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.edit_tasks
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.keyboardAsState
import com.yurhel.alex.anotes.ui.components.BottomAppBarNote
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: (isSaved: Boolean) -> Unit,
    toTasks: () -> Unit
) {
    val editText = rememberTextFieldState("")

    LaunchedEffect(Unit) {
        if (vm.editText.value.isEmpty()) {
            vm.prepareNote(redirectToNotesScreen = { onBack(true) }, redirectToTasksScreen = toTasks) {
                editText.edit {
                    append(vm.editText.value)
                    try { placeCursorAfterCharAt(0) } catch (_: Exception) {}
                }
            }
        } else {
            editText.edit {
                append(vm.editText.value)
                try { placeCursorAfterCharAt(0) } catch (_: Exception) {}
            }
        }
    }

    BackHandlerCustom {
        // Save text from state to main value
        vm.changeEditTextValue(editText.text.toString())
        onBack(vm.saveNote())
    }

    // Fixing a bug with BasicTextField2, when keyboard not showed second time
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) focusManager.clearFocus()
    }

    val scroll = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var globalViewHeight by remember { mutableFloatStateOf(0f) }

    Scaffold(
        bottomBar = {
            BottomAppBarNote(
                vm = vm,
                coroutineScope = coroutineScope,
                onBackAfterDelete = {
                    onBack(true)
                },
                onBackButtonClick = {
                    // Save text from state to main value
                    vm.changeEditTextValue(editText.text.toString())
                    onBack(vm.saveNote())
                },
                onSecondButtonClick = {
                    // Save text from state to main value
                    vm.changeEditTextValue(editText.text.toString())
                    vm.saveNote()
                    toTasks()
                },
                secondButtonIcon = Icons.AutoMirrored.Outlined.ListAlt,
                secondButtonText = stringResource(Res.string.edit_tasks),
                onGetTextButtonClick = {
                    editText.text.toString()
                }
            )
        }
    ) { padding ->
        BasicTextField(
            state = editText,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            onTextLayout = {
                coroutineScope.launch {
                    try {
                        val cursor = it()?.getCursorRect(editText.selection.end)
                        if (cursor != null) {
                            val bottomOffset = scroll.value + globalViewHeight
                            if (cursor.top < scroll.value) scroll.scrollTo(cursor.top.toInt())
                            else if (cursor.bottom > bottomOffset) scroll.scrollBy(cursor.bottom - bottomOffset)
                        }
                    } catch (_: Exception) {}
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 5.dp)
                .verticalScroll(scroll)
                .onGloballyPositioned {
                    val i = it.boundsInWindow()
                    globalViewHeight = i.bottom - i.top
                }
                .focusRequester(focusRequester)
        )
    }
}