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
    if (vm.editDialogObj == null) return

    val clipboard = LocalClipboard.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }
    val editDialogObj = vm.editDialogObj!!
    var edit by remember {
        mutableStateOf(
            if (editDialogObj.obj == null) "" else {
                when (editDialogObj.dataType) {
                    Types.Status -> (editDialogObj.obj as Status).title
                    Types.Task -> (editDialogObj.obj as Task).description
                }
            }
        )
    }
    var status by remember {
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
        mutableStateOf(TextFieldValue(text = edit, selection = TextRange(edit.length)))
    }

    BaseBottomSheet(
        onDismissRequest = vm::hideEditDialog,
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
            saveAction = {
                vm.editTaskSheetOnSave(edit, status, statusColor)
            },
            copyAction = if (editDialogObj.actionType == ActionTypes.Update && editDialogObj.dataType == Types.Task) {
                {
                    vm.viewModelScope.launch {
                        vm.vm.platform.copyToClipboard(edit, clipboard)
                    }
                }
            } else null,
            deleteAction = if (editDialogObj.actionType == ActionTypes.Update) {
                {
                    when (editDialogObj.dataType) {
                        Types.Status -> vm.onEvent(Event.DeleteStatus(editDialogObj.obj as Status))
                        Types.Task -> vm.onEvent(Event.DeleteTask(editDialogObj.obj as Task))
                    }
                    vm.hideEditDialog()
                }
            } else null
        )
        // Info about task && Statuses
        if (editDialogObj.dataType == Types.Task) {
            // Info about task
            if (editDialogObj.actionType == ActionTypes.Update) {
                val task = editDialogObj.obj as Task
                val createdStr = stringResource(Res.string.created)
                val updatedStr = stringResource(Res.string.updated)
                val statusStr = stringResource(Res.string.status)
                Text(
                    text = """
                        $createdStr: ${vm.formatDate(task.dateCreate)}
                        $updatedStr: ${vm.formatDate(task.dateUpdate)}
                        $statusStr $updatedStr: ${vm.formatDate(task.dateUpdateStatus)}
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
                            selectedStatusId = status,
                            status = it,
                            onClick = { _ ->
                                status = if (status != it.id) it.id else 0
                            }
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
                edit = it.text
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