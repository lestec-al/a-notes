package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.back
import com.yurhel.alex.anotes.shared.copy
import com.yurhel.alex.anotes.shared.created
import com.yurhel.alex.anotes.shared.delete
import com.yurhel.alex.anotes.shared.delete_info
import com.yurhel.alex.anotes.shared.no
import com.yurhel.alex.anotes.shared.updated
import com.yurhel.alex.anotes.shared.yes
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.shared.located_in
import com.yurhel.alex.anotes.shared.main
import com.yurhel.alex.anotes.shared.move_to_folder
import com.yurhel.alex.anotes.shared.moved_to
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.utils.Orientation
import com.yurhel.alex.anotes.ui.utils.BottomBarButton
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
    editNoteStr: String? = null,
    additionalButtons: List<BottomBarButton> = listOf()
) {
    val clipboard = LocalClipboard.current
    var isInfoBottomSheetOpen by remember { mutableStateOf(false) }
    var infoBottomSheetText by remember { mutableStateOf("") }
    var isEditSheetOpen by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    val movedTo = stringResource(Res.string.moved_to)
    val mainStr = vm.mainFolderName.takeIf { it.isNotEmpty() } ?: stringResource(Res.string.main)
    val orientation = getOrientation()
    val dateUpdated = vm.getNoteDate()
    val dateCreated = vm.getNoteDate(true)
    val showBackBtn = orientation == Orientation.Desktop || orientation == Orientation.Landscape || vm.platform.showBackButtonTest

    BaseBottomBar {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (showBackBtn) {
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
            // Sent note to folder
            IconButton(onClick = { isMenuOpen = true }) {
                Icon(
                    imageVector = Icons.Outlined.FilterAlt,
                    contentDescription = stringResource(Res.string.move_to_folder),
                    modifier = Modifier.size(30.dp)
                )
            }
            DropdownMenu(
                expanded = isMenuOpen,
                onDismissRequest = { isMenuOpen = false },
                properties = PopupProperties(
                    focusable = true,
                    clippingEnabled = false
                )
            ) {
                Text(
                    text = stringResource(Res.string.located_in),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                RadioDropdownMenuItem(
                    onClick = {
                        vm.setNoteFolder(0) {
                            isMenuOpen = false
                            vm.platform.showToast("$movedTo $mainStr")
                        }
                    },
                    text = mainStr,
                    isSelected = vm.selectedNote?.folder == 0
                )
                vm.allStatuses.forEach { st ->
                    if (st.note == 0) {
                        RadioDropdownMenuItem(
                            onClick = {
                                vm.setNoteFolder(st.id) {
                                    isMenuOpen = false
                                    vm.platform.showToast("$movedTo ${st.title}")
                                }
                            },
                            text = st.title.takeIf { it.isNotEmpty() } ?: st.id.toString(),
                            isSelected = vm.selectedNote?.folder == st.id
                        )
                    }
                }
            }
            // Copy button
            if (onGetTextButtonClick != null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            vm.platform.copyToClipboard(
                                onGetTextButtonClick(),
                                clipboard
                            )
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.CopyAll,
                        stringResource(Res.string.copy),
                        Modifier.size(30.dp)
                    )
                }
            }
            // Edit note button
            if (editNoteStr != null) {
                IconButton(onClick = { isEditSheetOpen = true }) {
                    Icon(Icons.Outlined.DriveFileRenameOutline, editNoteStr, Modifier.size(30.dp))
                }
            }
            // Additional buttons
            additionalButtons.forEach {
                IconButton(
                    onClick = it.onClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (it.enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            LocalContentColor.current
                        }
                    )
                ) {
                    Icon(it.icon, it.contentDescription, Modifier.size(30.dp))
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
            if (orientation == Orientation.Desktop) {
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
                        text = vm.getNoteDate(),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }
    }
    // BottomSheet (info || delete note)
    if (isInfoBottomSheetOpen) {
        BaseBottomSheet(onDismissRequest = { isInfoBottomSheetOpen = false }) {
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
                    OutlinedButton(
                        onClick = { isInfoBottomSheetOpen = false },
                        shape = CardDefaults.shape
                    ) {
                        Icon(Icons.Default.Close, stringResource(Res.string.no), Modifier.size(30.dp))
                    }
                    OutlinedButton(
                        onClick = {
                            vm.deleteNote()
                            onBackAfterDelete()
                        },
                        shape = CardDefaults.shape
                    ) {
                        Icon(Icons.Default.Check, stringResource(Res.string.yes), Modifier.size(30.dp))
                    }
                }
            }
        }
    }
    // BottomSheet (edit note)
    if (editNoteStr != null && isEditSheetOpen) {
        SimpleEditSheet(
            onDismissRequest = { isEditSheetOpen = false },
            onSave = {
                vm.updateEditTextValue(it)
                vm.saveNote()
            },
            copyToClipboard = { vm.platform.copyToClipboard(it, clipboard) },
            infoText = editNoteStr,
            initText = vm.editText.text.toString()
        )
    }
}