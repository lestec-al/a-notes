package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.data.local.StatusObj
import com.yurhel.alex.anotes.data.local.TasksObj
import com.yurhel.alex.anotes.ui.components.EditDialog
import com.yurhel.alex.anotes.ui.components.StatusCard
import com.yurhel.alex.anotes.ui.components.Tooltip
import com.yurhel.alex.anotes.ui.components.TooltipText
import com.yurhel.alex.anotes.ui.components.Task
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
    // For info: tasks pos & idx starts from 1
    val lastTaskPos = tasks.size

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    val lazyListState = rememberLazyListState()

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
                            vm.selectStatus(0)
                            vm.clearTasks()
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
                                vm.selectStatus(0)
                                vm.clearTasks()
                                vm.deleteNote()
                                vm.changeEditTexValue("")
                                onBack()
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
                                vm.selectStatus(0)
                                vm.clearTasks()
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
            // Need update tasks (ids) after drag drop change position
            key(tasks) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {

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
                    }

                    // Tasks
                    itemsIndexed(items = tasks) {  idx: Int, task: TasksObj ->
                        // For drag & drop
                        var offsetY by remember { mutableFloatStateOf(0f) }
                        var posTop = 0f
                        var posBottom = 0f
                        val itemIdx = idx + 1

                        Task(
                            task = task,
                            onClick = {
                                vm.onEvent(Event.ShowEditDialog(Types.Task, ActionTypes.Update, task))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                                // For drag & drop
                                .onGloballyPositioned {
                                    posTop = it.positionInParent().y
                                    posBottom = it.positionInParent().y + it.size.height
                                }
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
                                                offsetY = 0f
                                            },
                                            onDragCancel = {
                                                offsetY = 0f
                                            },
                                            onDrag = { _, dragAmount ->
                                                //change.consume()
                                                offsetY += dragAmount.y

                                                val posTopDynamic = (posTop + offsetY).toInt()
                                                val posBottomDynamic = (posBottom + offsetY).toInt()

                                                // Check offsets of all items
                                                // Try to find that touching item
                                                var foundedIdx: Int? = null
                                                for (it in lazyListState.layoutInfo.visibleItemsInfo) {
                                                    if (itemIdx != it.index) {
                                                        for (offset in (it.offset + 20)..(it.offsetEnd - 20)) {
                                                            if (offset in (posTopDynamic + 20)..(posBottomDynamic - 20)) {
                                                                foundedIdx = it.index
                                                                break
                                                            }
                                                        }
                                                    }
                                                    if (foundedIdx != null) break
                                                }
                                                if (foundedIdx != null) {
                                                    if (foundedIdx > itemIdx && task.position != 1) {
                                                        // Move item to down
                                                        vm.onEvent(
                                                            Event.ChangePos(
                                                                pos = Pos.Prev,
                                                                task = task
                                                            )
                                                        )
                                                    } else if (foundedIdx < itemIdx && task.position != lastTaskPos) {
                                                        // Move item to up
                                                        vm.onEvent(
                                                            Event.ChangePos(
                                                                pos = Pos.Next,
                                                                task = task
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                },
                            tasksTextPadding = 10,
                            statuses = statuses,
                            onBackgroundColor = onBackgroundColor
                        )
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

private val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size