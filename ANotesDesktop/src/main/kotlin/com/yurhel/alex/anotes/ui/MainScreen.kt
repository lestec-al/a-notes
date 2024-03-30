package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yurhel.alex.anotes.data.local.NoteObj
import com.yurhel.alex.anotes.ui.components.Tooltip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: MainViewModel,
    openNoteClicked: () -> Unit
) {
    vm.getDbNotes("")

    val searchText by vm.searchText.collectAsState()
    val allNotes: List<NoteObj> by vm.allNotes.collectAsState()
    val isSyncNow by vm.isSyncNow.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                // Bottom bar
                BottomAppBar(modifier = Modifier.height(50.dp)) {
                    // Add new note button
                    val newNoteText = vm.getString("create") + " " + vm.getString("note")
                    Tooltip(
                        tooltipText = newNoteText
                    ) {
                        IconButton(
                            onClick = {
                                vm.selectNote(null)
                                openNoteClicked()
                            }
                        ) {
                            Icon(Icons.Default.Add, newNoteText)
                        }
                    }

                    // Sync indicator / button
                    if (isSyncNow) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(14.dp)
                                .size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Tooltip(
                            tooltipText = "Sync with cloud"
                        ) {
                            IconButton(onClick = { vm.driveSyncAuto() }) {
                                Icon(Icons.Default.Refresh, "Sync with cloud")
                            }
                        }
                    }

                    // Search
                    val interactionSource = remember { MutableInteractionSource() }
                    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
                    Box(modifier = Modifier
                        .padding(5.dp, 5.dp, 15.dp, 5.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .drawBehind { drawRect(surfaceColor) }
                    ) {
                        Row {
                            // Edit text
                            BasicTextField(
                                value = searchText,
                                onValueChange = { vm.getDbNotes(it) },
                                modifier = Modifier
                                    .padding(10.dp, 0.dp)
                                    .fillMaxWidth(),
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                keyboardOptions = KeyboardOptions.Default,
                                keyboardActions = KeyboardActions.Default,
                                interactionSource = interactionSource,
                                singleLine = true
                            ) { innerTextField ->
                                // Places text field with placeholder and appropriate bottom padding
                                TextFieldDefaults.TextFieldDecorationBox(
                                    value = searchText,
                                    visualTransformation = VisualTransformation.None,
                                    innerTextField = innerTextField,
                                    placeholder = {
                                        Text(text = vm.getString("search_text_hint"))
                                    },
                                    singleLine = true,
                                    enabled = true,
                                    isError = false,
                                    interactionSource = interactionSource,
                                    colors = TextFieldDefaults.textFieldColors(
                                        textColor = Color.Gray,
                                        disabledTextColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp, 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->

            // Empty text
            if (allNotes.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = vm.getString("empty_text"))
                }
            }

            // Notes - projects
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.FixedSize(300.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp, paddingValues.calculateTopPadding(), 5.dp, 50.dp)
            ) {
                // Notes
                items(items = allNotes) { note: NoteObj ->
                    Card(
                        onClick = {
                            vm.selectNote(note)
                            openNoteClicked()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        // Project indicator
                        if (note.withTasks) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "",
                                modifier = Modifier.padding(10.dp, 10.dp, 10.dp, 2.dp)
                            )
                        }
                        Text(
                            text = note.text,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 15,
                            modifier = Modifier.padding(10.dp, 2.dp, 10.dp, 10.dp)
                        )
                    }
                }
            }
        }
    }

    // Sync choose dialog
    val isSyncDialogOpen by vm.isSyncDialogOpen.collectAsState()
    if (isSyncDialogOpen) {
        Dialog(onDismissRequest = { vm.openSyncDialog(false) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                // Title
                Text(
                    text = vm.getString("sync_collision"),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                // Buttons
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        vm.driveSyncManualThread(false)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = vm.getString("sync_drive"))
                    }
                    TextButton(onClick = {
                        vm.driveSyncManualThread(true)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = vm.getString("sync_local"))
                    }
                }
            }
        }
    }
}