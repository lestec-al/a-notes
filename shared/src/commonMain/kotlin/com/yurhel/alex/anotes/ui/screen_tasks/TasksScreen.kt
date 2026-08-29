package com.yurhel.alex.anotes.ui.screen_tasks

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.edit_note
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.data.Task
import com.yurhel.alex.anotes.ui.components.CustomScaffold
import com.yurhel.alex.anotes.ui.components.DropFloatingActionButton
import com.yurhel.alex.anotes.ui.components.NoteBottomBar
import com.yurhel.alex.anotes.ui.screen_tasks.components.TaskCard
import com.yurhel.alex.anotes.ui.screen_tasks.components.EditTasksSheet
import com.yurhel.alex.anotes.ui.screen_tasks.components.StatusCard
import com.yurhel.alex.anotes.ui.screen_tasks.utils.ActionTypes
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Event
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Types
import com.yurhel.alex.anotes.ui.screen_tasks.utils.overlaps
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun TasksScreen(
    vm: TasksViewModel,
    onBack: () -> Unit
) {
    BackHandlerCustom(onBack)
    val lazyListState = rememberLazyListState()

    CustomScaffold(
        bottomBar = {
            NoteBottomBar(
                vm = vm.vm,
                scope = rememberCoroutineScope(),
                onBackAfterDelete = onBack,
                onBackButtonClick = onBack,
                onGetTextButtonClick = vm::getTaskTextForNote,
                editNoteStr = stringResource(Res.string.edit_note)
            )
        },
        floatingActionButton = {
            DropFloatingActionButton(vm.taskScreenDropMenuItems)
        }
    ) { bottomPadding, topPadding ->
        // Need update tasks (ids) after drag drop change position
        key(vm.tasks) {
            Column(
                modifier = Modifier
                    .padding(bottom = bottomPadding, top = topPadding)
                    .fillMaxSize()
            ) {
                // Top bar
                if (vm.vm.selectedNote != null && vm.vm.selectedNote!!.text.isNotEmpty()) {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        windowInsets = WindowInsets(0,0,0,0),
                        title = {
                            Text(
                                text = vm.vm.selectedNote!!.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                            )
                        }
                    )
                }
                // Statuses
                if (vm.statuses.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        items(items = vm.statuses) {
                            StatusCard(
                                selectedStatusId = vm.selectedStatus,
                                status = it,
                                onClick = vm::changeStatus,
                                onLongClicked = vm::editStatus
                            )
                        }
                    }
                }
                // Tasks
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.padding(5.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(items = vm.tasks) { idx: Int, task: Task ->
                        // For drag & drop
                        var y by remember { mutableFloatStateOf(0f) }
                        var z by remember { mutableFloatStateOf(0f) }
                        var thisIt: IntRange? = null

                        TaskCard(
                            task = task,
                            cardColor = MaterialTheme.colorScheme.background,
                            onClick = {
                                vm.onEvent(Event.ShowEditDialog(Types.Task, ActionTypes.Update, task))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                // For drag & drop
                                .zIndex(z)
                                .offset { IntOffset(x = 0, y = y.roundToInt()) }
                                .pointerInput(vm.selectedStatus) {
                                    // If status is not selected (all tasks shown)
                                    if (vm.selectedStatus == 0) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                z = 1f
                                                vm.onDragStart(idx)
                                            },
                                            onDragEnd = {
                                                y = 0f
                                                z = 0f
                                                thisIt = null
                                                vm.onDragEnd(idx, task)
                                            },
                                            onDragCancel = {
                                                y = 0f
                                                z = 0f
                                                thisIt = null
                                                vm.onDragEnd(idx, task)
                                            },
                                            onDrag = { _, dragAmount ->
                                                if (vm.dragIdx != idx) return@detectDragGesturesAfterLongPress
                                                y += dragAmount.y
                                                // Check if this item overlaps with other items
                                                lazyListState.layoutInfo.visibleItemsInfo.forEach {
                                                    if (idx == it.index) {
                                                        val start = (it.offset + y).roundToInt()
                                                        val end = ((it.offset + it.size) + y).roundToInt()
                                                        thisIt = start..end
                                                    } else {
                                                        thisIt?.apply {
                                                            if (
                                                                (it.offset..(it.offset + it.size))
                                                                    .overlaps(this)
                                                            ) {
                                                                vm.updateLastFoundIdx(it.index)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                },
                            tasksTextPadding = 10,
                            statuses = vm.statuses,
                            onBackgroundColor = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
    // Bottom sheet
    if (vm.editDialogObj != null) EditTasksSheet(vm = vm)
}