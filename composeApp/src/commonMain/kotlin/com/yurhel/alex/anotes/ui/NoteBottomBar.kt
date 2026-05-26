package com.yurhel.alex.anotes.ui

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
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Unarchive
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.copy
import anotes.composeapp.generated.resources.created
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.delete_info
import anotes.composeapp.generated.resources.no
import anotes.composeapp.generated.resources.note_archived
import anotes.composeapp.generated.resources.note_restored
import anotes.composeapp.generated.resources.restore_note_from_archive
import anotes.composeapp.generated.resources.sent_note_to_archive
import anotes.composeapp.generated.resources.updated
import anotes.composeapp.generated.resources.yes
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.showToast
import com.yurhel.alex.anotes.ui.components.BaseBottomBar
import com.yurhel.alex.anotes.ui.utils.OrientationObj
import com.yurhel.alex.anotes.formatDate
import com.yurhel.alex.anotes.copyToClipboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteBottomBar(
    vm: MainViewModel,
    scope: CoroutineScope,
    onBackAfterDelete: () -> Unit,
    onBackButtonClick: () -> Unit,
    onGetTextButtonClick: (() -> String)?,
    additionalButtons: List<Triple<String, ImageVector, () -> Unit>>
) {
    val clipboard = LocalClipboard.current
    var isInfoBottomSheetOpen by remember { mutableStateOf(false) }
    var infoBottomSheetText by remember { mutableStateOf("") }
    var isNoteArchived by remember { mutableStateOf(vm.getIsSelectedNoteArchived()) }
    val archive2 = stringResource(
        if (isNoteArchived) Res.string.note_restored else Res.string.note_archived
    )
    val orientation = getOrientation()
    val dateUpdated = formatDate(vm.getNoteDate())
    val dateCreated = formatDate(vm.getNoteDate(true))

    BaseBottomBar {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (orientation == OrientationObj.Desktop || vm.isTest) {
                // Back button
                IconButton(onClick = onBackButtonClick) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        stringResource(Res.string.back),
                        Modifier.size(30.dp)
                    )
                }
            }
            // Delete note
            IconButton(
                onClick = {
                    scope.launch {
                        infoBottomSheetText = getString(Res.string.delete_info)
                        isInfoBottomSheetOpen = true
                    }
                }
            ) {
                Icon(Icons.Outlined.Delete, stringResource(Res.string.delete), Modifier.size(30.dp))
            }
            // Sent note to archive
            IconButton(
                onClick = {
                    vm.archiveOrUnarchiveNote(!isNoteArchived)
                    isNoteArchived = !isNoteArchived
                    showToast(archive2)
                }
            ) {
                Icon(
                    imageVector = if (isNoteArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                    contentDescription = stringResource(
                        if (isNoteArchived) Res.string.restore_note_from_archive else Res.string.sent_note_to_archive
                    ),
                    modifier = Modifier.size(30.dp)
                )
            }
            // Copy button
            if (onGetTextButtonClick != null) {
                IconButton(
                    onClick = {
                        scope.launch { onGetTextButtonClick().copyToClipboard(clipboard) }
                    }
                ) {
                    Icon(
                        Icons.Default.CopyAll,
                        stringResource(Res.string.copy),
                        Modifier.size(30.dp)
                    )
                }
            }
            // Additional buttons
            additionalButtons.forEach {
                IconButton(onClick = it.third) {
                    Icon(it.second, it.first, Modifier.size(30.dp))
                }
            }
        }
        // Note updated text
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
                    text = "${stringResource(Res.string.updated)}: $dateUpdated",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${stringResource(Res.string.created)}: $dateCreated",
                    style = MaterialTheme.typography.labelMedium
                )
            } else {
                // Only for android
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            // Open info about note
                            scope.launch {
                                infoBottomSheetText = """
                                    ${getString(Res.string.updated)}: $dateUpdated
                                    ${getString(Res.string.created)}: $dateCreated
                                """.trimIndent()
                                isInfoBottomSheetOpen = true
                            }
                        }
                ) {
                    Text(
                        text = formatDate(vm.getNoteDate()),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
    // BottomSheet
    if (isInfoBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isInfoBottomSheetOpen = false },
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
                    IconButton(onClick = { isInfoBottomSheetOpen = false }) {
                        Icon(Icons.Default.Close, stringResource(Res.string.no), Modifier.size(30.dp))
                    }
                    IconButton(
                        onClick = {
                            vm.deleteNote()
                            onBackAfterDelete()
                        }
                    ) {
                        Icon(Icons.Default.Check, stringResource(Res.string.yes), Modifier.size(30.dp))
                    }
                }
            }
            Spacer(Modifier.fillMaxWidth().height(50.dp))
        }
    }
}