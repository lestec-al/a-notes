package com.yurhel.alex.anotes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.data.local.obj.StatusObj
import com.yurhel.alex.anotes.data.local.obj.TasksObj
import com.yurhel.alex.anotes.ui.components.EditDialog
import com.yurhel.alex.anotes.ui.components.StatusCard
import com.yurhel.alex.anotes.ui.components.Tooltip
import com.yurhel.alex.anotes.ui.components.TooltipText
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    toNote: () -> Unit
) {
    BackHandler {
        vm.selectStatus(0)
        vm.clearTasks()
        onBack()
    }

    val selectedStatus by vm.selectedStatus.collectAsState()
    val selectedNote by vm.selectedNote.collectAsState()
    val statuses: List<StatusObj> by vm.statuses.collectAsState()
    val tasks: List<TasksObj> by vm.tasks.collectAsState()
    // For info: tasks pos & idx starts from 1
    val lastTaskPos = tasks.size

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            floatingActionButton = {
                // Add new task
                val addNewTaskText = context.getString(R.string.create) + " " + context.getString(R.string.task)
                Tooltip(
                    tooltipText = addNewTaskText
                ) {
                    FloatingActionButton(
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        shape = CardDefaults.shape,
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
                    // Delete note
                    val deleteNoteText = context.getString(R.string.delete) + " " + context.getString(R.string.note)
                    Tooltip(
                        tooltipText = deleteNoteText
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = {
                                vm.selectStatus(0)
                                vm.clearTasks()
                                vm.deleteNote()
                                vm.editText.clearText()
                                onBack()
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, deleteNoteText)
                        }
                    }

                    // Edit note
                    val editNoteText = context.getString(R.string.edit_note)
                    Tooltip(
                        tooltipText = editNoteText
                    ) {
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = {
                                vm.saveNote(showTasksState = false, withoutNoteTextUpdate = true)
                                vm.selectStatus(0)
                                vm.clearTasks()
                                toNote()
                            }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.ic_edit_note),
                                contentDescription = editNoteText,
                                colorFilter = ColorFilter.tint(LocalContentColor.current)
                            )
                        }
                    }

                    // Note updated text
                    TooltipText(
                        text = "${context.getString(R.string.updated)}: ${vm.formatDate(context, selectedNote?.dateUpdate)}",
                        tooltipText = "${context.getString(R.string.created)}: ${vm.formatDate(context, selectedNote?.dateCreate)}",
                        coroutineScope = coroutineScope
                    )
                }
            }
        ) { paddingValues ->
            // Need update tasks (ids) after drag drop change position
            key(tasks) {
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    // Top bar
                    item {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                scrolledContainerColor = Color.Transparent,
                                containerColor = Color.Transparent,
                            ),
                            title = {
                                Text(
                                    text = if (selectedNote != null) selectedNote!!.text else "",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                                )
                            }
                        )
                    }

                    // Statuses
                    item {
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
                                val addNewStatusText = context.getString(R.string.create) + " " + context.getString(R.string.status)
                                Tooltip(
                                    tooltipText = addNewStatusText
                                ) {
                                    SmallFloatingActionButton(
                                        modifier = Modifier.padding(5.dp),
                                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                                        shape = CardDefaults.shape,
                                        onClick = {
                                            vm.onEvent(Event.ShowEditDialog(Types.Status, ActionTypes.Create))
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, addNewStatusText)
                                    }
                                }
                            }
                        }
                    }

                    // Tasks
                    items(items = tasks) {task: TasksObj ->
                        // For drag & drop
                        var offsetY by remember { mutableFloatStateOf(0f) }

                        Card(
                            onClick = {
                                vm.onEvent(Event.ShowEditDialog(Types.Task, ActionTypes.Update, task))
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
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
                                            onDragStart = {},
                                            onDragEnd = {
                                                savePos(offsetY, vm, task, lastTaskPos) {
                                                    offsetY = 0f
                                                }
                                            },
                                            onDragCancel = {
                                                savePos(offsetY, vm, task, lastTaskPos) {
                                                    offsetY = 0f
                                                }
                                            },
                                            onDrag = { _, dragAmount ->
                                                //change.consume()
                                                offsetY += dragAmount.y
                                            }
                                        )
                                    }
                                }
                        ) {
                            Row(horizontalArrangement = Arrangement.Center) {
                                // Color indicator
                                Canvas(
                                    modifier = Modifier
                                        .padding(top = 18.dp) // 10.dp + 8.dp (text native padding?)
                                        .size(10.dp)
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
                                    modifier = Modifier.padding(
                                        horizontal = 5.dp,
                                        vertical = 10.dp
                                    )
                                )
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


@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }


private fun savePos(
    offsetY: Float,
    vm: MainViewModel,
    task: TasksObj,
    lastTaskPos: Int,
    updateUI: () -> Unit
) {
    if (offsetY > 200) {
        if (task.position == 1) {
            updateUI()
        } else {
            // Scroll down
            vm.onEvent(
                Event.ChangePos(
                    pos = Pos.Prev,
                    task = task
                )
            )
        }
    } else if (offsetY < -200) {
        if (task.position == lastTaskPos) {
            updateUI()
        } else {
            // Scroll up
            vm.onEvent(
                Event.ChangePos(
                    pos = Pos.Next,
                    task = task
                )
            )
        }
    } else {
        updateUI()
    }
}