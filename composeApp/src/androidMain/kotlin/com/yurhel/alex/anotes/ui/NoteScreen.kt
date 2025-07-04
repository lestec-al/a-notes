package com.yurhel.alex.anotes.ui

import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.edit_tasks
import com.yurhel.alex.anotes.ui.components.BottomAppBarNote
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun NoteScreen(
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

    BackHandler {
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
                secondButtonIcon = Icons.Outlined.Menu,
                secondButtonText = stringResource(Res.string.edit_tasks),
                onGetTextButtonClick = { editText.text.toString() }
            )
        }
    ) { paddingValues ->
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
                .padding(paddingValues)
                .padding(horizontal = 10.dp)
                .verticalScroll(scroll)
                .onGloballyPositioned {
                    val i = it.boundsInWindow()
                    globalViewHeight = i.bottom - i.top
                }
                .focusRequester(focusRequester)
        )
    }
}


@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }

    DisposableEffect(LocalWindowInfo.current) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            isImeVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) == true
            true
        }
        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}