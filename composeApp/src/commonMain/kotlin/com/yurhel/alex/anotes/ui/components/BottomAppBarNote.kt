package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.copy
import anotes.composeapp.generated.resources.created
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.delete_info
import anotes.composeapp.generated.resources.ic_archive
import anotes.composeapp.generated.resources.ic_copy
import anotes.composeapp.generated.resources.ic_unarchive
import anotes.composeapp.generated.resources.note_archived
import anotes.composeapp.generated.resources.note_restored
import anotes.composeapp.generated.resources.restore_note_from_archive
import anotes.composeapp.generated.resources.sent_note_to_archive
import anotes.composeapp.generated.resources.updated
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.OrientationObj
import com.yurhel.alex.anotes.ui.getOrientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomAppBarNote(
    vm: MainViewModel,
    coroutineScope: CoroutineScope,
    onBackAfterDelete: () -> Unit,
    onBackButtonClick: () -> Unit,
    onSecondButtonClick: () -> Unit,
    secondButtonIcon: ImageVector,
    secondButtonText: String,
    onGetTextButtonClick: () -> String
) {
    val clipboardManager = LocalClipboardManager.current
    var isInfoBottomSheetOpen by remember { mutableStateOf(false) }
    var infoBottomSheetText by remember { mutableStateOf("") }
    val orientation = getOrientation()

    BottomAppBar(
        modifier = Modifier.height(50.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (orientation == OrientationObj.Desktop) {
                // Back button
                val backText = stringResource(Res.string.back)
                Tooltip(backText) {
                    IconButton(onClick = onBackButtonClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, backText, Modifier.size(30.dp))
                    }
                }
            }
            // Delete note
            val delText = stringResource(Res.string.delete)
            Tooltip(delText) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            infoBottomSheetText = getString(Res.string.delete_info)
                            isInfoBottomSheetOpen = true
                        }
                    }
                ) {
                    Icon(Icons.Outlined.Delete, delText, Modifier.size(30.dp))
                }
            }
            // Sent note to archive
            var isNoteArchived by remember { mutableStateOf(vm.getIsSelectedNoteArchived()) }
            val archive1 = stringResource(
                if (isNoteArchived) Res.string.restore_note_from_archive else Res.string.sent_note_to_archive
            )
            val archive2 = stringResource(
                if (isNoteArchived) Res.string.note_restored else Res.string.note_archived
            )
            Tooltip(archive1) {
                IconButton(onClick = {
                    vm.archiveOrUnarchiveNote(!isNoteArchived)
                    isNoteArchived = !isNoteArchived
                    vm.showToast(archive2)
                }) {
                    Icon(
                        imageVector = vectorResource(
                            if (isNoteArchived) Res.drawable.ic_unarchive else Res.drawable.ic_archive
                        ),
                        contentDescription = archive1,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            // Second button
            Tooltip(secondButtonText) {
                IconButton(onClick = onSecondButtonClick) {
                    Icon(secondButtonIcon, secondButtonText, Modifier.size(30.dp))
                }
            }
            // Copy button
            val copyText = stringResource(Res.string.copy)
            Tooltip(copyText) {
                IconButton(
                    onClick = {
                        val noteStr = onGetTextButtonClick()
                        clipboardManager.setText(buildAnnotatedString { append(text = noteStr) })
                    }
                ) {
                    Icon(vectorResource(Res.drawable.ic_copy), copyText, Modifier.size(30.dp))
                }
            }
        }

        // Note updated text ???
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(bottom = 5.dp, end = 10.dp)
                .fillMaxWidth()
        ) {
            if (orientation == OrientationObj.Desktop) {
                // Only for desktop
                Text(
                    text = "${stringResource(Res.string.updated)}: ${vm.formatDate(vm.getNoteDate())}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${stringResource(Res.string.created)}: ${vm.formatDate(vm.getNoteDate(true))}",
                    style = MaterialTheme.typography.labelMedium
                )

            } else {
                // Only for android
                Tooltip(stringResource(Res.string.updated)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                // Open info about note
                                coroutineScope.launch {
                                    infoBottomSheetText = """
                                        ${getString(Res.string.updated)}: ${vm.formatDate(vm.getNoteDate())}
                                        ${getString(Res.string.created)}: ${vm.formatDate(vm.getNoteDate(true))}
                                    """.trimIndent()
                                    isInfoBottomSheetOpen = true
                                }
                            }
                    ) {
                        Text(
                            text = vm.formatDate(vm.getNoteDate()),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }

    // BottomSheet for asking for deletion
    if (isInfoBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                isInfoBottomSheetOpen = false
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = infoBottomSheetText,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // If delete text - show buttons
            if (infoBottomSheetText == stringResource(Res.string.delete_info)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                ) {
                    IconButton(
                        onClick = {
                            isInfoBottomSheetOpen = false
                        }
                    ) {
                        Icon(Icons.Default.Close, "No", Modifier.size(30.dp))
                    }

                    IconButton(
                        onClick = {
                            vm.deleteNote()
                            onBackAfterDelete()
                        }
                    ) {
                        Icon(Icons.Default.Check, "Yes", Modifier.size(30.dp))
                    }
                }
            }

            Spacer(Modifier.fillMaxWidth().height(50.dp))
        }
    }
}