package ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import data.NoteObj

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    vm: MainViewModel,
    newNoteClicked: () -> Unit,
    openNoteClicked: (NoteObj) -> Unit
) {
    vm.getDbNotes("")
    val searchText by vm.searchText.collectAsState()
    val allNotes: List<NoteObj> by vm.allNotes.collectAsState()
    val isSyncNow by vm.isSyncNow.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                // Bottom bar
                BottomAppBar(modifier = Modifier.height(50.dp)) {
                    // Add new note button
                    IconButton(onClick = newNoteClicked) {
                        Icon(Icons.Default.Add, "")
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
                        IconButton(onClick = { vm.driveSyncAuto() }) {
                            Icon(Icons.Default.Refresh, "")
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
                                    placeholder = { Text(text = search_text_hint) },
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
                    Text(text = empty_text)
                }
            }

            // All notes in grid lazy view
            LazyVerticalGrid(
                columns = GridCells.FixedSize(200.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp, paddingValues.calculateTopPadding(), 5.dp, 50.dp)
            ) {
                items(items = allNotes, key = { it.id }) { note: NoteObj ->
                    Card(
                        onClick = { openNoteClicked(note) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(5.dp)
                    ) {
                        Text(
                            text = if (note.text.length > 300) note.text.substring(0, 301) else note.text,
                            modifier = Modifier.padding(10.dp)
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
                    text = sync_collision,
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
                        Text(text = sync_drive)
                    }
                    TextButton(onClick = {
                        vm.driveSyncManualThread(true)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = sync_local)
                    }
                }
            }
        }
    }
}