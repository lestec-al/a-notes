package com.yurhel.alex.anotes.ui.screen_tasks

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.edit_note
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.data.Tasks
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.ui.components.CustomScaffold
import com.yurhel.alex.anotes.ui.components.DropFloatingActionButton
import com.yurhel.alex.anotes.ui.NoteBottomBar
import com.yurhel.alex.anotes.ui.screen_tasks.components.TaskCard
import com.yurhel.alex.anotes.ui.screen_tasks.components.EditTasksSheet
import com.yurhel.alex.anotes.ui.screen_tasks.components.StatusCard
import com.yurhel.alex.anotes.ui.screen_tasks.utils.ActionTypes
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Event
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Types
import com.yurhel.alex.anotes.ui.screen_tasks.utils.pxToDp
import com.yurhel.alex.anotes.ui.utils.Orientation
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    vm: TasksViewModel,
    onBack: () -> Unit
) {
    BackHandlerCustom(onBack = onBack)
    val lazyListState = rememberLazyListState()
    val scrollOffset = if (getOrientation() == Orientation.Desktop) 20 else 50

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
                LazyColumn(state = lazyListState) {
                    itemsIndexed(items = vm.tasks) { idx: Int, task: Tasks ->
                        // For drag & drop
                        var offsetY by remember { mutableFloatStateOf(0f) }
                        var posTop = 0f
                        var posBottom = 0f

                        TaskCard(
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
                                .pointerInput(vm.selectedStatus) {
                                    // If status is not selected (all tasks shown)
                                    if (vm.selectedStatus == 0) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                vm.onDragStart(idx)
                                            },
                                            onDragEnd = {
                                                offsetY = 0f
                                                vm.onDragEnd(idx, task)
                                            },
                                            onDragCancel = {
                                                offsetY = 0f
                                                vm.onDragEnd(idx, task)
                                            },
                                            onDrag = { _, dragAmount ->
                                                if (vm.draggingObj != idx) return@detectDragGesturesAfterLongPress
                                                offsetY += dragAmount.y
                                                vm.onDrag(
                                                    offsetY, posTop, posBottom, idx, scrollOffset,
                                                    lazyListState.layoutInfo.visibleItemsInfo
                                                )
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