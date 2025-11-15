package com.yurhel.alex.anotes.ui.feature_swipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.edit
import anotes.composeapp.generated.resources.edit_note
import anotes.composeapp.generated.resources.edit_side
import anotes.composeapp.generated.resources.left_side
import anotes.composeapp.generated.resources.right_side
import anotes.composeapp.generated.resources.task
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.ui.components.CustomScaffold
import com.yurhel.alex.anotes.ui.components.NoteBottomBar
import com.yurhel.alex.anotes.ui.feature_swipes.components.SwipeNotesCard
import com.yurhel.alex.anotes.ui.feature_swipes.components.SwipeNotesSheet
import com.yurhel.alex.anotes.ui.feature_swipes.utils.Edit
import com.yurhel.alex.anotes.ui.feature_swipes.utils.swipesCode
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeNotesScreen(
    vm: SwipeNotesViewModel,
    onBack: () -> Unit
) {
    //val haptic = LocalHapticFeedback.current
    BackHandlerCustom {
        vm.saveNote()
        onBack()
    }

    CustomScaffold(
        bottomBar = {
            NoteBottomBar(
                vm = vm.vm,
                coroutineScope = rememberCoroutineScope(),
                onBackAfterDelete = onBack,
                onBackButtonClick = {
                    vm.saveNote()
                    onBack()
                },
                onGetTextButtonClick = null,
                additionalButtons = listOf(
                    Triple(stringResource(Res.string.edit_note), Icons.Outlined.DriveFileRenameOutline) {
                        vm.updateEdit(Edit.NoteText, vm.noteText)
                    },
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    vm.updateEdit(Edit.SwipeText)
                },
                shape = CardDefaults.shape,
                content = {
                    Icon(Icons.Default.Add, null)
                }
            )
        }
    ) { bottomPadding, topPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(bottom = bottomPadding)
                .fillMaxWidth()
        ) {
            // Status bar spacer
            item { Spacer(Modifier.height(topPadding)) }
            // Top bar
            if (vm.noteText.isNotEmpty()) {
                item {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            scrolledContainerColor = MaterialTheme.colorScheme.background,
                            containerColor = MaterialTheme.colorScheme.background,
                        ),
                        windowInsets = WindowInsets(0,0,0,0),
                        title = {
                            Text(
                                text = vm.noteText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                            )
                        }
                    )
                }
            }
            // Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Left row color button
                    Button(
                        onClick = {
                            vm.updateEdit(Edit.Left, vm.leftText)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = vm.leftColor
                        )
                    ) {
                        Text(text = vm.leftText.ifEmpty { stringResource(Res.string.left_side) })
                    }
                    // Right row color button
                    Button(
                        onClick = {
                            vm.updateEdit(Edit.Right, vm.rightText)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = vm.rightColor
                        )
                    ) {
                        Text(text = vm.rightText.ifEmpty { stringResource(Res.string.right_side) })
                    }
                }
            }
            // Swipes
            items(items = vm.data) { obj ->
                SwipeNotesCard(
                    onClick = {
                        vm.updateEdit(Edit.SwipeText, obj.text, obj.id)
                    },
                    onDragStopped = {
                        vm.onDragStoppedUpdateData(it, obj)
                        //haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    },
                    leftColor = vm.leftColor,
                    rightColor = vm.rightColor,
                    obj = obj
                )
            }
        }
        // Bottom sheet
        if (vm.edit != Edit.None) {
            SwipeNotesSheet(
                onDismissRequest = {
                    vm.updateEdit(Edit.None)
                },
                onSave = vm::onSaveEdit,
                onDeleteSwipeText = vm::deleteSwipeText,
                infoText = when (vm.edit) {
                    Edit.NoteText -> stringResource(Res.string.edit_note)
                    Edit.SwipeText -> stringResource(Res.string.edit) + " " + stringResource(Res.string.task)
                    else -> stringResource(Res.string.edit_side)
                },
                initText = vm.editedText,
                forbiddenString = swipesCode,
                initColor = vm.getEditedColorValue(),
                isColorPickerShow = vm.edit == Edit.Left || vm.edit == Edit.Right,
                isDeleteButtonOn = vm.edit == Edit.SwipeText && vm.editedSwipeTextId != -1
            )
        }
    }
}