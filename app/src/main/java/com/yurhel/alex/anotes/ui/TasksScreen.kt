package com.yurhel.alex.anotes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.data.local.obj.StatusObj
import com.yurhel.alex.anotes.data.local.obj.TasksObj
import com.yurhel.alex.anotes.ui.components.BottomAppBarAssembled
import com.yurhel.alex.anotes.ui.components.EditDialog
import com.yurhel.alex.anotes.ui.components.StatusCard
import com.yurhel.alex.anotes.ui.components.Task
import com.yurhel.alex.anotes.ui.components.Tooltip
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

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
                BottomAppBarAssembled(
                    context = context,
                    vm = vm,
                    coroutineScope = coroutineScope,
                    onBack = onBack,
                    secondButtonAction = {
                        vm.saveNote(showTasksState = false, withoutNoteTextUpdate = true)
                        vm.selectStatus(0)
                        vm.clearTasks()
                        toNote()
                    },
                    secondButtonIcon = painterResource(R.drawable.ic_edit_note),
                    secondButtonText = context.getString(R.string.edit_note)
                )
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
                    itemsIndexed(items = tasks) { idx: Int, task: TasksObj ->
                        // For drag & drop
                        var offsetY by remember { mutableFloatStateOf(0f) }
                        var posTop = 0f
                        var posBottom = 0f
                        val itemIdx = idx + 2

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
                                                        for (offset in (it.offset + 50)..(it.offsetEnd - 50)) {
                                                            if (offset in (posTopDynamic + 50)..(posBottomDynamic - 50)) {
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