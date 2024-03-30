package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.ui.components.Tooltip
import com.yurhel.alex.anotes.ui.components.TooltipText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    toTasks: () -> Unit
) {
    LaunchedEffect(Unit) { vm.prepareNote() }
    val editText by vm.editText.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBar(modifier = Modifier.height(50.dp)) {
                    // Back button
                    Tooltip(
                        tooltipText = "Go back"
                    ) {
                        IconButton(onClick = {
                            vm.saveNote()
                            vm.changeEditTexValue("")
                            onBack()
                        }) {
                            Icon(Icons.Outlined.ArrowBack, "Go back")
                        }
                    }

                    // Delete note
                    val deleteNoteText = vm.getString("delete") + " " + vm.getString("note")
                    Tooltip(
                        tooltipText = deleteNoteText
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = {
                                onBack()
                                vm.changeEditTexValue("")
                                vm.deleteNote()
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, deleteNoteText)
                        }
                    }

                    // Open tasks
                    val editTasksText = vm.getString("edit_tasks")
                    Tooltip(
                        tooltipText = editTasksText
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = {
                                vm.saveNote(true)
                                vm.changeEditTexValue("")
                                toTasks()
                            }
                        ) {
                            Icon(Icons.Outlined.Menu, editTasksText)
                        }
                    }

                    // Note updated text
                    TooltipText(
                        text = "${vm.getString("updated")}: ${vm.getNoteDate()}",
                        tooltipText = "${vm.getString("created")}: ${vm.getNoteDate(true)}"
                    )
                }
            }
        ) { padding ->
            // Edit text field
            BasicTextField(
                value = editText,
                onValueChange = {
                    vm.changeEditTexValue(it)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp, 20.dp, 20.dp, 0.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}