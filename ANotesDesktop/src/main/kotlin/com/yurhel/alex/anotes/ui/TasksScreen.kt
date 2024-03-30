package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.data.local.StatusObj
import com.yurhel.alex.anotes.data.local.TasksObj
import com.yurhel.alex.anotes.ui.components.EditDialog
import com.yurhel.alex.anotes.ui.components.StatusCard
import com.yurhel.alex.anotes.ui.components.Tooltip
import com.yurhel.alex.anotes.ui.components.TooltipText
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    toNote: () -> Unit
) {
    val selectedStatus by vm.selectedStatus.collectAsState()
    val selectedNote by vm.selectedNote.collectAsState()
    val statuses: List<StatusObj> by vm.statuses.collectAsState()
    val tasks: List<TasksObj> by vm.tasks.collectAsState()

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    backgroundColor = MaterialTheme.colorScheme.background,
                    title = {
                        Text(
                            text = if (selectedNote != null) selectedNote!!.text else "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                        )
                    }
                )
            },
            floatingActionButton = {
                // Add new task
                val addNewTaskText = vm.getString("create") + " " + vm.getString("task")
                Tooltip(
                    tooltipText = addNewTaskText
                ) {
                    FloatingActionButton(
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        onClick = {
                            vm.onEvent(Event.ShowEditDialog(Types.Task, ActionTypes.Create))
                        }
                    ) {
                        Icon(Icons.Default.Add, addNewTaskText)
                    }
                }
            },
            bottomBar = {
                BottomAppBar(modifier = Modifier.height(50.dp)) {
                    // Back button
                    Tooltip(
                        tooltipText = "Go back"
                    ) {
                        IconButton(onClick = {
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

                    // Edit note
                    val editNoteText = vm.getString("edit_note")
                    Tooltip(
                        tooltipText = editNoteText
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = {
                                vm.saveNote(showTasksState = false, withoutNoteTextUpdate = true)
                                toNote()
                            }
                        ) {
                            Icon(Icons.Outlined.Edit, editNoteText)
                        }
                    }

                    // Note updated text
                    TooltipText(
                        text = "${vm.getString("updated")}: ${vm.formatDate(selectedNote?.dateUpdate)}",
                        tooltipText = "${vm.getString("created")}: ${vm.formatDate(selectedNote?.dateCreate)}"
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                // Statuses
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    items(items = statuses) { status: StatusObj ->
                        StatusCard(
                            selectedStatusId = selectedStatus,
                            status = status,
                            onClick = {
                                if (selectedStatus != status.id) vm.selectStatus(status.id) else vm.selectStatus(0)
                                vm.updateTasksData(withStatuses = true, withNoteSave = false)
                            },
                            onLongClicked = {
                                vm.onEvent(Event.ShowEditDialog(Types.Status, ActionTypes.Update, status))
                            }
                        )
                    }

                    item {
                        // Create new status
                        val addNewStatusText = vm.getString("create") + " " + vm.getString("status")
                        Tooltip(
                            tooltipText = addNewStatusText
                        ) {
                            SmallFloatingActionButton(
                                modifier = Modifier.padding(5.dp),
                                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                                onClick = {
                                    vm.onEvent(Event.ShowEditDialog(Types.Status, ActionTypes.Create))
                                }
                            ) {
                                Icon(Icons.Default.Add, addNewStatusText)
                            }
                        }
                    }
                }

                key(tasks) {
                    // Tasks
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items = tasks) {task: TasksObj ->
                            // For drag & drop
                            var isMoved by remember { mutableStateOf(false) }
                            var offsetY by remember { mutableFloatStateOf(0f) }

                            Card(
                                onClick = {
                                    vm.onEvent(Event.ShowEditDialog(Types.Task, ActionTypes.Update, task))
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                                    // For drag & drop
                                    .absoluteOffset(
                                        y = offsetY
                                            .roundToInt()
                                            .pxToDp()
                                    )
                                    .pointerInput(Unit) {
                                        // If status not selected (all tasks shown)
                                        if (selectedStatus == 0) {
                                            // Drag & drop
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    isMoved = true
                                                },
                                                onDragEnd = {
                                                    isMoved = false
                                                    // Save
                                                    if (offsetY > 0) {
                                                        // Scroll down
                                                        if (offsetY > 100) {
                                                            vm.onEvent(
                                                                Event.ChangePos(
                                                                    pos = Pos.Prev,
                                                                    task = task
                                                                )
                                                            )
                                                        }
                                                    } else {
                                                        // Scroll Up
                                                        if (offsetY < -100) {
                                                            vm.onEvent(
                                                                Event.ChangePos(
                                                                    pos = Pos.Next,
                                                                    task = task
                                                                )
                                                            )
                                                        }
                                                    }
                                                    offsetY = 0f
                                                },
                                                onDragCancel = {
                                                    isMoved = false
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    offsetY += dragAmount.y
                                                }
                                            )
                                        }
                                    }
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Color indicator
                                    Canvas(
                                        modifier = Modifier.size(10.dp)
                                    ) {
                                        drawCircle(
                                            color = try {
                                                Color(statuses.find { it.id == task.status }!!.color)
                                            } catch (e: Exception) {
                                                onBackgroundColor
                                            }
                                        )
                                    }

                                    // Description
                                    Text(
                                        text = task.description,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Sync choose dialog
    val isDialogVisible by vm.editDialogVisibility.collectAsState()
    if (isDialogVisible) EditDialog(vm = vm)
}

// ???
@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }