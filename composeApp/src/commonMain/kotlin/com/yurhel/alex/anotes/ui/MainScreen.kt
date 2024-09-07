package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.change_view
import anotes.composeapp.generated.resources.create
import anotes.composeapp.generated.resources.empty_text
import anotes.composeapp.generated.resources.ic_grid
import anotes.composeapp.generated.resources.ic_list
import anotes.composeapp.generated.resources.note
import anotes.composeapp.generated.resources.search_text_hint
import anotes.composeapp.generated.resources.sync_collision
import anotes.composeapp.generated.resources.sync_drive
import anotes.composeapp.generated.resources.sync_drive_action
import anotes.composeapp.generated.resources.sync_local
import com.yurhel.alex.anotes.data.NoteObj
import com.yurhel.alex.anotes.ui.components.Task
import com.yurhel.alex.anotes.ui.components.Tooltip
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: MainViewModel,
    openNoteClicked: () -> Unit
) {
    BackHandlerCustom(onBack = vm.callExit)

    vm.getDbNotes("")
    vm.getNotesView()
    vm.getAllTasks()
    vm.getAllStatuses()

    val searchText by vm.searchText.collectAsState()
    val appSettingsView by vm.appSettingsView.collectAsState()
    val allNotes: List<NoteObj> by vm.allNotes.collectAsState()
    val isSyncNow by vm.isSyncNow.collectAsState()
    val allTasks by vm.allTasks.collectAsState()
    val allStatuses by vm.allStatuses.collectAsState()

    val orientation = getOrientation()
    val keyboard = LocalSoftwareKeyboardController.current

    var isSearchOn by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // Check if not need choose widget
    // Opposed possibly only on Android
    val notNeedChooseWidget = vm.widgetIdWhenCreated == 0


    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (notNeedChooseWidget) {
                    // Bottom bar
                    BottomAppBar(modifier = Modifier.height(50.dp)) {
                        if (isSearchOn && orientation != OrientationObj.Desktop) {
                            // Only for Android
                            // Search OFF button
                            val backText = stringResource(Res.string.back)
                            Tooltip(tooltipText = backText) {
                                IconButton(
                                    modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                                    onClick = {
                                        isSearchOn = false
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Icon(Icons.Default.Close, backText)
                                }
                            }

                        } else {
                            // Add new note button
                            val newNoteText = stringResource(Res.string.create) + " " + stringResource(Res.string.note)
                            Tooltip(tooltipText = newNoteText) {
                                IconButton(
                                    modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                                    onClick = {
                                        vm.selectNote(null)
                                        openNoteClicked()
                                    }
                                ) {
                                    Icon(Icons.Default.Add, newNoteText)
                                }
                            }

                            // Sync indicator / button
                            val syncText = stringResource(Res.string.sync_drive_action)
                            if (isSyncNow) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(17.dp, 5.dp, 17.dp, 10.dp)
                                        .size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Tooltip(tooltipText = syncText) {
                                    IconButton(
                                        modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                                        onClick = {
                                            vm.syncData(SyncActionTypes.Auto, vm)
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, syncText)
                                    }
                                }
                            }

                            // Change notes view button
                            val changeViewText = stringResource(Res.string.change_view)
                            Tooltip(tooltipText = changeViewText) {
                                IconButton(
                                    modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                                    onClick = {
                                        vm.changeNotesView()
                                    }
                                ) {
                                    Icon(
                                        imageVector = vectorResource(
                                            if (appSettingsView == "grid") Res.drawable.ic_list else Res.drawable.ic_grid
                                        ),
                                        contentDescription = changeViewText
                                    )
                                }
                            }
                        }

                        // Search
                        val interactionSource = remember { MutableInteractionSource() }
                        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .padding(5.dp, 5.dp, 15.dp, 10.dp)
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
                                        .fillMaxWidth()
                                        .onFocusChanged {
                                            if (it.isFocused) isSearchOn = true
                                        },
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions { keyboard?.hide() },
                                    interactionSource = interactionSource,
                                    singleLine = true
                                ) { innerTextField ->
                                    TextFieldDefaults.DecorationBox(
                                        value = searchText,
                                        visualTransformation = VisualTransformation.None,
                                        innerTextField = innerTextField,
                                        placeholder = {
                                            Text(text = stringResource(Res.string.search_text_hint))
                                        },
                                        singleLine = true,
                                        enabled = true,
                                        isError = false,
                                        interactionSource = interactionSource,
                                        colors = TextFieldDefaults.colors(
                                            focusedTextColor = Color.Gray,
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
            }
        ) { paddingValues ->
            // Empty text
            if (allNotes.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = stringResource(Res.string.empty_text))
                }
            }

            // Notes - projects
            LazyVerticalStaggeredGrid(
                // StaggeredGridCells.FixedSize(300.dp)
                columns = StaggeredGridCells.Fixed(
                    if (appSettingsView == "grid") if (orientation == OrientationObj.Landscape) 3 else 2 else 1
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp, paddingValues.calculateTopPadding(), 5.dp, 50.dp)
            ) {
                // Notes
                items(items = allNotes) { note: NoteObj ->
                    Card(
                        onClick = {
                            vm.selectNote(note)
                            if (notNeedChooseWidget) {
                                // Open existing note
                                openNoteClicked()
                            } else {
                                // Init widget
                                vm.callUpdateWidget(true, vm.widgetIdWhenCreated, note.dateCreate.toString(), note)
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 60.dp,
                                max = 350.dp
                            )
                            .padding(5.dp)
                    ) {
                        Text(
                            text = note.text,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 10,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )

                        // Tasks for this note
                        for (task in allTasks) {
                            if (task.note == note.id) {
                                Task(
                                    task = task,
                                    cardColor = Color.Transparent,
                                    onClick = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 5.dp),
                                    tasksTextPadding = 2,
                                    statuses = allStatuses,
                                    onBackgroundColor = onBackgroundColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
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
                    text = stringResource(Res.string.sync_collision),
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
                        vm.syncData(SyncActionTypes.ManualImport, vm)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = stringResource(Res.string.sync_drive))
                    }
                    TextButton(onClick = {
                        vm.syncData(SyncActionTypes.ManualExport, vm)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = stringResource(Res.string.sync_local))
                    }
                }
            }
        }
    }
}

