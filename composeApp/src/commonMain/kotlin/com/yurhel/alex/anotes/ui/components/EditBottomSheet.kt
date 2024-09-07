package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.created
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.save
import anotes.composeapp.generated.resources.status
import anotes.composeapp.generated.resources.task
import anotes.composeapp.generated.resources.text
import anotes.composeapp.generated.resources.updated
import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj
import com.yurhel.alex.anotes.ui.ActionTypes
import com.yurhel.alex.anotes.ui.Event
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Types
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBottomSheet(
    vm: MainViewModel
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val edit = remember {
        mutableStateOf(
            if (vm.editDialogObj != null) {
                when (vm.editDialogDataType) {
                    Types.Status -> (vm.editDialogObj as StatusObj).title
                    Types.Task -> (vm.editDialogObj as TasksObj).description
                }
            } else {
                ""
            }
        )
    }
    val selectedStatus = remember {
        mutableIntStateOf(
            if (vm.editDialogDataType == Types.Task) {
                when (vm.editDialogActionType) {
                    ActionTypes.Create -> vm.selectedStatus.value
                    ActionTypes.Update -> (vm.editDialogObj as TasksObj).status
                }
            } else {
                0
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = { vm.onEvent(Event.HideEditDialog) },
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize()
    ) {
        // Edit
        val focusRequester = remember { FocusRequester() }
        val value = remember {
            mutableStateOf(TextFieldValue(text = edit.value, selection = TextRange(edit.value.length)))
        }

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

        // Color picker for status
        val statusColor = if (vm.editDialogDataType == Types.Status && vm.editDialogActionType == ActionTypes.Update) {
            Color((vm.editDialogObj as StatusObj).color)
        } else {
            null
        }
        var redColor by remember {
            mutableIntStateOf(statusColor?.red?.times(255.0)?.toInt() ?: Random.nextInt(256))
        }
        var greenColor by remember {
            mutableIntStateOf(statusColor?.green?.times(255.0)?.toInt() ?: Random.nextInt(256))
        }
        var blueColor by remember {
            mutableIntStateOf(statusColor?.blue?.times(255.0)?.toInt() ?: Random.nextInt(256))
        }
        if (vm.editDialogDataType == Types.Status) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Slider(
                    value = redColor.toFloat(),
                    onValueChange = { redColor = it.toInt() },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(redColor, greenColor, blueColor),
                        activeTrackColor = Color(redColor, greenColor, blueColor)
                    )
                )
                Slider(
                    value = greenColor.toFloat(),
                    onValueChange = { greenColor = it.toInt() },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(redColor, greenColor, blueColor),
                        activeTrackColor = Color(redColor, greenColor, blueColor)
                    )
                )
                Slider(
                    value = blueColor.toFloat(),
                    onValueChange = { blueColor = it.toInt() },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(redColor, greenColor, blueColor),
                        activeTrackColor = Color(redColor, greenColor, blueColor)
                    )
                )
            }
        }

        // Info about task
        if (vm.editDialogDataType == Types.Task) {
            if (vm.editDialogActionType == ActionTypes.Update) {
                val task = vm.editDialogObj as TasksObj
                Text(
                    text = """
                            ${stringResource(Res.string.created)}: ${vm.formatDate(task.dateCreate)}
                            ${stringResource(Res.string.updated)}: ${vm.formatDate(task.dateUpdate)}
                            ${stringResource(Res.string.status)} ${stringResource(Res.string.updated)}: ${vm.formatDate(task.dateUpdateStatus)}
                        """.trimIndent(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Statuses
            val statuses: List<StatusObj> by vm.statuses.collectAsState()
            LazyHorizontalStaggeredGrid(
                rows = StaggeredGridCells.Fixed(1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(50.dp)
            ) {
                items(items = statuses) { status: StatusObj ->
                    StatusCard(
                        selectedStatusId = selectedStatus.intValue,
                        status = status,
                        onClick = {
                            if (selectedStatus.intValue != status.id) {
                                selectedStatus.intValue = status.id
                            } else {
                                selectedStatus.intValue = 0
                            }
                        }
                    )
                }
            }
        }

        // Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (vm.editDialogActionType == ActionTypes.Update) {
                // Delete button
                val deleteText = stringResource(Res.string.delete) + " " + when (vm.editDialogDataType) {
                    Types.Status -> stringResource(Res.string.status)
                    Types.Task -> stringResource(Res.string.task)
                }
                Tooltip(
                    tooltipText = deleteText
                ) {
                    IconButton(
                        onClick = {
                            // Action
                            when (vm.editDialogDataType) {
                                Types.Status -> vm.onEvent(Event.DeleteStatus(vm.editDialogObj as StatusObj))
                                Types.Task -> vm.onEvent(Event.DeleteTask(vm.editDialogObj as TasksObj))
                            }
                            vm.onEvent(Event.HideEditDialog)
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = deleteText)
                    }
                }
            }

            // Save button
            val saveText = stringResource(Res.string.save) + " " + when (vm.editDialogDataType) {
                Types.Status -> stringResource(Res.string.status)
                Types.Task -> stringResource(Res.string.task)
            }
            Tooltip(
                tooltipText = saveText
            ) {
                IconButton(
                    onClick = {
                        // Validation
                        val valid = when {
                            edit.value.isBlank() -> false
                            (vm.editDialogDataType != Types.Task && edit.value.length > 100) -> false
                            else -> true
                        }

                        // Actions
                        if (valid) {
                            when (vm.editDialogActionType) {
                                ActionTypes.Create -> {
                                    when (vm.editDialogDataType) {
                                        Types.Status -> {
                                            vm.onEvent(
                                                Event.UpsertStatus(
                                                    StatusObj(
                                                        title = edit.value,
                                                        color = Color(redColor, greenColor, blueColor).toArgb(),
                                                        note = vm.selectedNote.value!!.id
                                                    )
                                                )
                                            )
                                        }
                                        Types.Task -> {
                                            val dateNow = System.currentTimeMillis()

                                            vm.onEvent(
                                                Event.UpsertTask(
                                                    TasksObj(
                                                        description = edit.value,
                                                        status = selectedStatus.intValue,
                                                        note = vm.selectedNote.value!!.id,
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
                                    when (vm.editDialogDataType) {
                                        Types.Status -> {
                                            vm.onEvent(
                                                Event.UpsertStatus(
                                                    (vm.editDialogObj as StatusObj).copy(
                                                        title = edit.value,
                                                        color = Color(redColor, greenColor, blueColor).toArgb()
                                                    )
                                                )
                                            )
                                        }
                                        Types.Task -> {
                                            val oldTask = vm.editDialogObj as TasksObj
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
                    }
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = saveText)
                }
            }
        }
    }
}