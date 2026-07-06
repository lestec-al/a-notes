package com.yurhel.alex.anotes.ui.screen_tasks.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.created
import com.yurhel.alex.anotes.shared.edit_task
import com.yurhel.alex.anotes.shared.status
import com.yurhel.alex.anotes.shared.text
import com.yurhel.alex.anotes.shared.updated
import com.yurhel.alex.anotes.data.Status
import com.yurhel.alex.anotes.data.Task
import com.yurhel.alex.anotes.shared.create_task
import com.yurhel.alex.anotes.shared.create_status
import com.yurhel.alex.anotes.shared.edit_status
import com.yurhel.alex.anotes.ui.components.BaseBottomSheet
import com.yurhel.alex.anotes.ui.components.BottomSheetTopRow
import com.yurhel.alex.anotes.ui.screen_tasks.utils.ActionTypes
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Event
import com.yurhel.alex.anotes.ui.components.ColorPicker
import com.yurhel.alex.anotes.ui.screen_tasks.TasksViewModel
import com.yurhel.alex.anotes.ui.screen_tasks.utils.Types
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTasksSheet(vm: TasksViewModel) {
    val clipboard = LocalClipboard.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    // MOVE TO VIEWMODEL
    val editDialogObj = vm.editDialogObj!!
    val edit = remember {
        mutableStateOf(
            if (editDialogObj.obj == null) "" else {
                when (editDialogObj.dataType) {
                    Types.Status -> (editDialogObj.obj as Status).title
                    Types.Task -> (editDialogObj.obj as Task).description
                }
            }
        )
    }
    val selectedStatus = remember {
        mutableIntStateOf(
            if (editDialogObj.dataType != Types.Task) 0 else {
                when (editDialogObj.actionType) {
                    ActionTypes.Create -> vm.selectedStatus
                    ActionTypes.Update -> (editDialogObj.obj as Task).status
                }
            }
        )
    }
    var statusColor by remember {
        mutableStateOf(
            if (editDialogObj.dataType == Types.Status && editDialogObj.actionType == ActionTypes.Update) {
                Color((editDialogObj.obj as Status).color)
            } else {
                primaryColor
            }
        )
    }
    val value = remember {
        mutableStateOf(TextFieldValue(text = edit.value, selection = TextRange(edit.value.length)))
    }
    fun saveButtonOnClick() {
        val valid = when {
            edit.value.isBlank() -> false
            (editDialogObj.dataType != Types.Task && edit.value.length > 100) -> false
            else -> true
        }
        if (!valid) return
        when (editDialogObj.actionType) {
            ActionTypes.Create -> {
                when (editDialogObj.dataType) {
                    Types.Status -> {
                        vm.onEvent(
                            Event.UpsertStatus(
                                Status(
                                    title = edit.value,
                                    color = statusColor.toArgb(),
                                    note = vm.vm.selectedNote!!.id
                                )
                            )
                        )
                    }
                    Types.Task -> {
                        val dateNow = System.currentTimeMillis()
                        vm.onEvent(
                            Event.UpsertTask(
                                Task(
                                    description = edit.value,
                                    status = selectedStatus.intValue,
                                    note = vm.vm.selectedNote!!.id,
                                    dateCreate = dateNow,
                                    dateUpdate = dateNow,
                                    dateUpdateStatus = dateNow
                                )
                            )
                        )
                    }
                }
            }
            ActionTypes.Update -> {
                when (editDialogObj.dataType) {
                    Types.Status -> {
                        vm.onEvent(
                            Event.UpsertStatus(
                                (editDialogObj.obj as Status).copy(
                                    title = edit.value,
                                    color = statusColor.toArgb()
                                )
                            )
                        )
                    }
                    Types.Task -> {
                        val oldTask = editDialogObj.obj as Task
                        val dateNow = System.currentTimeMillis()
                        vm.onEvent(
                            Event.UpsertTask(
                                oldTask.copy(
                                    description = edit.value,
                                    status = selectedStatus.intValue,
                                    dateUpdate = if (edit.value != oldTask.description) {
                                        dateNow
                                    } else {
                                        oldTask.dateUpdate
                                    },
                                    dateUpdateStatus = if (selectedStatus.intValue != oldTask.status) {
                                        dateNow
                                    } else {
                                        oldTask.dateUpdateStatus
                                    }
                                )
                            )
                        )
                    }
                }
            }
        }
        vm.onEvent(Event.HideEditDialog)
    }
    fun onStatusClick(status: Status) {
        if (selectedStatus.intValue != status.id) {
            selectedStatus.intValue = status.id
        } else {
            selectedStatus.intValue = 0
        }
    }

    BaseBottomSheet(
        onDismissRequest = { vm.onEvent(Event.HideEditDialog) },
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize()
    ) {
        // Top row
        BottomSheetTopRow(
            infoText = when (editDialogObj.actionType) {
                ActionTypes.Create -> {
                    when (editDialogObj.dataType) {
                        Types.Status -> stringResource(Res.string.create_status)
                        Types.Task -> stringResource(Res.string.create_task)
                    }
                }
                ActionTypes.Update -> {
                    when (editDialogObj.dataType) {
                        Types.Status -> stringResource(Res.string.edit_status)
                        Types.Task -> stringResource(Res.string.edit_task)
                    }
                }
            },
            saveAction = ::saveButtonOnClick,
            copyAction = if (editDialogObj.actionType == ActionTypes.Update && editDialogObj.dataType == Types.Task) {
                {
                    vm.viewModelScope.launch {
                        vm.vm.platform.copyToClipboard(edit.value, clipboard)
                    }
                }
            } else null,
            deleteAction = if (editDialogObj.actionType == ActionTypes.Update) {
                {
                    when (editDialogObj.dataType) {
                        Types.Status -> vm.onEvent(Event.DeleteStatus(editDialogObj.obj as Status))
                        Types.Task -> vm.onEvent(Event.DeleteTask(editDialogObj.obj as Task))
                    }
                    vm.onEvent(Event.HideEditDialog)
                }
            } else null
        )
        // Info about task && Statuses
        if (editDialogObj.dataType == Types.Task) {
            // Info about task
            if (editDialogObj.actionType == ActionTypes.Update) {
                val task = editDialogObj.obj as Task
                val dateUpdated = vm.vm.getNoteDate()
                val dateCreated = vm.vm.getNoteDate(true)
                val dateStatusUpdated = vm.vm.platform.formatDate(task.dateUpdateStatus)
                Text(
                    text = """
                            ${stringResource(Res.string.created)}: $dateCreated
                            ${stringResource(Res.string.updated)}: $dateUpdated
                            ${stringResource(Res.string.status)} ${stringResource(Res.string.updated)}: $dateStatusUpdated
                        """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }
            // Statuses
            if (vm.statuses.isNotEmpty()) {
                LazyHorizontalStaggeredGrid(
                    rows = StaggeredGridCells.Fixed(1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(50.dp)
                ) {
                    items(items = vm.statuses) {
                        StatusCard(
                            selectedStatusId = selectedStatus.intValue,
                            status = it,
                            onClick = ::onStatusClick
                        )
                    }
                }
            }
        }
        if (editDialogObj.dataType == Types.Status) {
            ColorPicker(
                onColorChooserClick = { statusColor = it },
                initColor = statusColor
            )
        }
        // Edit text
        TextField(
            value = value.value,
            onValueChange = {
                edit.value = it.text
                value.value = it
            },
            label = {
                Text(text = stringResource(Res.string.text))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            )
        )
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}