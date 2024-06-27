package com.yurhel.alex.anotes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.ui.components.Tooltip
import com.yurhel.alex.anotes.ui.components.TooltipText
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    toTasks: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (vm.editText.text.isEmpty()) {
            vm.prepareNote(redirectToNotesScreen = onBack)
        }
    }

    BackHandler {
        vm.saveNote()
        vm.editText.clearText()
        onBack()
    }

    // Fixing a bug with BasicTextField2, when keyboard not showed second time
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible) focusManager.clearFocus()
    }

    val scroll = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    var globalViewHeight = 0f

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBar(modifier = Modifier.height(50.dp)) {
                    // Delete note
                    val deleteNoteText = context.getString(R.string.delete) + " " + context.getString(R.string.note)
                    Tooltip(
                        tooltipText = deleteNoteText
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = {
                                vm.deleteNote()
                                vm.editText.clearText()
                                onBack()
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, deleteNoteText)
                        }
                    }

                    // Open tasks
                    val editTasksText = context.getString(R.string.edit_tasks)
                    Tooltip(
                        tooltipText = editTasksText
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = {
                                vm.saveNote(true)
                                vm.editText.clearText()
                                toTasks()
                            }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_tasks),
                                contentDescription = editTasksText,
                                colorFilter = ColorFilter.tint(LocalContentColor.current)
                            )
                        }
                    }

                    // Note updated text
                    TooltipText(
                        text = "${context.getString(R.string.updated)}: ${vm.getNoteDate(context)}",
                        tooltipText = "${context.getString(R.string.created)}: ${vm.getNoteDate(context, true)}",
                        coroutineScope = coroutineScope
                    )
                }
            }
        ) { padding ->
            // Edit text field
            BasicTextField2(
                state = vm.editText,
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
                            val cursor = it()?.getCursorRect(vm.editText.text.selectionInChars.end)
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
                    .padding(20.dp, 20.dp, 20.dp, 0.dp)
                    .verticalScroll(scroll)
                    .onGloballyPositioned {
                        val i = it.boundsInWindow()
                        globalViewHeight = i.bottom - i.top
                    }
                    .focusRequester(focusRequester)
            )
        }
    }
}