package com.yurhel.alex.anotes.ui

import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.data.drive.Drive
import com.yurhel.alex.anotes.data.local.obj.NoteObj
import com.yurhel.alex.anotes.ui.components.Tooltip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: MainViewModel,
    openNoteClicked: () -> Unit
) {
    BackHandler(onBack = vm.callExit)

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

    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val keyboard = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground


    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                if (vm.widgetIdWhenCreated == AppWidgetManager.INVALID_APPWIDGET_ID) {
                    // Bottom bar
                    BottomAppBar(modifier = Modifier.height(50.dp)) {
                        // Add new note button
                        val newNoteText = context.getString(R.string.create) + " " + context.getString(R.string.note)
                        Tooltip(
                            tooltipText = newNoteText
                        ) {
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
                        val syncText = context.getString(R.string.sync_drive_action)
                        if (isSyncNow) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(17.dp, 5.dp, 17.dp, 10.dp)
                                    .size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Tooltip(
                                tooltipText = syncText
                            ) {
                                IconButton(
                                    modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                                    onClick = {
                                        DriveViewModel(vm, Drive(context)).driveSyncAuto()
                                    }
                                ) {
                                    Icon(Icons.Default.Refresh, syncText)
                                }
                            }
                        }

                        // Change notes view button
                        val changeViewText = context.getString(R.string.change_view)
                        Tooltip(
                            tooltipText = changeViewText
                        ) {
                            IconButton(
                                modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                                onClick = {
                                    vm.changeNotesView()
                                }
                            ) {
                                Image(
                                    painter = painterResource(
                                        if (appSettingsView == "grid") R.drawable.ic_list else R.drawable.ic_grid
                                    ),
                                    contentDescription = changeViewText,
                                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                                )
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
                                        .fillMaxWidth(),
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
                                            Text(text = context.getString(R.string.search_text_hint))
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
                    Text(text = LocalContext.current.getString(R.string.empty_text))
                }
            }

            // Notes - projects
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(if (appSettingsView == "grid") if (isLandscape) 3 else 2 else 1),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp, paddingValues.calculateTopPadding(), 5.dp, 50.dp)
            ) {
                // Notes
                items(items = allNotes) { note: NoteObj ->
                    Card(
                        onClick = {
                            vm.selectNote(note)
                            if (vm.widgetIdWhenCreated == AppWidgetManager.INVALID_APPWIDGET_ID) {
                                // Open existing note
                                openNoteClicked()
                            } else {
                                // Init widget
                                vm.callUpdateWidget(true, vm.widgetIdWhenCreated, note.dateCreate.toString(), note.text)
                            }
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min = 60.dp,
                                max = if (note.withTasks) 500.dp else Dp.Unspecified
                            )
                            .padding(5.dp)
                    ) {
                        Text(
                            text = note.text,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 10,
                            modifier = Modifier.padding(10.dp, 2.dp, 10.dp, if (note.withTasks) 2.dp else 10.dp)
                        )

                        // Tasks for this note
                        if (note.withTasks) {
                            for (task in allTasks) {
                                if (task.note == note.id) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 5.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.Center) {
                                            // Color indicator
                                            Canvas(
                                                modifier = Modifier
                                                    .padding(top = 10.dp) // 2.dp + 8.dp (text native padding?)
                                                    .size(10.dp)
                                            ) {
                                                drawCircle(
                                                    color = try {
                                                        Color(allStatuses.find { it.id == task.status }!!.color)
                                                    } catch (e: Exception) {
                                                        onBackgroundColor
                                                    }
                                                )
                                            }

                                            // Description
                                            Text(
                                                text = task.description,
                                                modifier = Modifier.padding(
                                                    horizontal = 5.dp,
                                                    vertical = 2.dp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sync choose dialog
    val isSyncDialogOpen by vm.isSyncDialogOpen.collectAsState()
    if (isSyncDialogOpen) {
        val driveVM = DriveViewModel(vm, Drive(context))

        Dialog(onDismissRequest = { vm.openSyncDialog(false) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                // Title
                Text(
                    text = LocalContext.current.getString(R.string.sync_collision),
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
                        driveVM.driveSyncManualThread(false)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = LocalContext.current.getString(R.string.sync_drive))
                    }
                    TextButton(onClick = {
                        driveVM.driveSyncManualThread(true)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = LocalContext.current.getString(R.string.sync_local))
                    }
                }
            }
        }
    }
}