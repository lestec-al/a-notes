package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.edit_note
import anotes.composeapp.generated.resources.status
import anotes.composeapp.generated.resources.task
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.ui.components.DropFloatingActionButton
import com.yurhel.alex.anotes.ui.components.EditBottomSheet
import com.yurhel.alex.anotes.ui.components.NoteBottomBar
import com.yurhel.alex.anotes.ui.components.SimpleEditBottomSheet
import com.yurhel.alex.anotes.ui.components.StatusCard
import com.yurhel.alex.anotes.ui.components.Task
import com.yurhel.alex.anotes.ui.components.pxToDp
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    BackHandlerCustom {
        vm.selectStatus(0)
        vm.clearTasks()
        onBack()
    }

    val selectedStatus by vm.selectedStatus.collectAsState()
    val selectedNote by vm.selectedNote.collectAsState()
    val statuses: List<StatusObj> by vm.statuses.collectAsState()
    val tasks: List<TasksObj> by vm.tasks.collectAsState()

    var lastFoundedIdx: Int? by remember { mutableStateOf(null) }
    var draggingObj: Int? by remember { mutableStateOf(null) }

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val lazyListState = rememberLazyListState()
    val scrollOffset = if (getOrientation() == OrientationObj.Desktop) 20 else 50

    val isDialogVisible by vm.editDialogVisibility.collectAsState()

    Scaffold(
        floatingActionButton = {
            DropFloatingActionButton(
                listOf(
                    // Create new status button
                    Triple(stringResource(Res.string.status), Icons.AutoMirrored.Outlined.Sort) {
                        vm.onEvent(Event.ShowEditDialog(Types.Status, ActionTypes.Create))
                    },
                    // Add new task button
                    Triple(stringResource(Res.string.task), Icons.AutoMirrored.Outlined.StickyNote2) {
                        vm.onEvent(Event.ShowEditDialog(Types.Task, ActionTypes.Create))
                    }
                )
            )
        },
        bottomBar = {
            NoteBottomBar(
                vm = vm,
                coroutineScope = rememberCoroutineScope(),
                onBackAfterDelete = onBack,
                onBackButtonClick = {
                    vm.selectStatus(0)
                    vm.clearTasks()
                    onBack()
                },
                onGetTextButtonClick = vm::getTaskTextForNote,
                additionalButtons = listOf(
                    Triple(stringResource(Res.string.edit_note), Icons.Outlined.DriveFileRenameOutline) {
                        vm.updateIsEditSheetOpen(true)
                    }
                )
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
                    if (selectedNote != null && selectedNote!!.text.isNotEmpty()) {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                scrolledContainerColor = MaterialTheme.colorScheme.background,
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                            windowInsets = WindowInsets(0,0,0,0),
                            title = {
                                Text(
                                    text = selectedNote!!.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                                )
                            }
                        )
                    }
                }
                // Statuses
                item {
                    if (statuses.isNotEmpty()) {
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
                                        // Change status
                                        if (selectedStatus != status.id) vm.selectStatus(status.id) else vm.selectStatus(0)
                                        vm.updateTasksData(false)
                                    },
                                    onLongClicked = {
                                        vm.onEvent(Event.ShowEditDialog(Types.Status, ActionTypes.Update, status))
                                    }
                                )
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
                    val itemIdx = idx + 2 // Add topBar & statuses

                    Task(
                        task = task,
                        cardColor = MaterialTheme.colorScheme.background,
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
                            .pointerInput(selectedStatus) {
                                // If status is not selected (all tasks shown)
                                if (selectedStatus == 0) {
                                    // Drag & drop
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            if (draggingObj == null) draggingObj = idx
                                        },
                                        onDragEnd = {
                                            offsetY = 0f
                                            if (draggingObj == idx) {
                                                draggingObj = null
                                                val lastFoundedIdxSt = lastFoundedIdx
                                                if (lastFoundedIdxSt != null) {
                                                    vm.onEvent(Event.ChangePos(pos = lastFoundedIdxSt, task = task))
                                                    lastFoundedIdx = null
                                                }
                                            }
                                        },
                                        onDragCancel = {
                                            offsetY = 0f
                                            if (draggingObj == idx) draggingObj = null
                                        },
                                        onDrag = { _, dragAmount ->
                                            if (draggingObj != idx) return@detectDragGesturesAfterLongPress
                                            offsetY += dragAmount.y
                                            val posTopDynamic = (posTop + offsetY).toInt()
                                            val posBottomDynamic = (posBottom + offsetY).toInt()
                                            // Check offsets of all items
                                            // Try to find that touching item
                                            for (it in lazyListState.layoutInfo.visibleItemsInfo) {
                                                if (itemIdx != it.index) {
                                                    for (offset in (it.offset + scrollOffset)..(it.offsetEnd - scrollOffset)) {
                                                        if (offset in (posTopDynamic + scrollOffset)..(posBottomDynamic - scrollOffset)) {
                                                            lastFoundedIdx = it.index
                                                        }
                                                    }
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
    // Bottom sheets
    if (isDialogVisible) EditBottomSheet(vm = vm)
    if (vm.isEditTextSheetOpen) {
        SimpleEditBottomSheet(
            onDismissRequest = vm::updateIsEditSheetOpen,
            onSave = {
                vm.updateEditTextValue(it)
                vm.saveNote()
            },
            infoText = stringResource(Res.string.edit_note),
            initText = vm.editText.text.toString()
        )
    }
}

private val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size