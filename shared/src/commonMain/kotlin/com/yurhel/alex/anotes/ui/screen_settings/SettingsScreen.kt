package com.yurhel.alex.anotes.ui.screen_settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.data.SyncType
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.add_folder
import com.yurhel.alex.anotes.shared.app_code
import com.yurhel.alex.anotes.shared.app_ver
import com.yurhel.alex.anotes.shared.back
import com.yurhel.alex.anotes.shared.code_link
import com.yurhel.alex.anotes.shared.del_folder
import com.yurhel.alex.anotes.shared.drive_sync
import com.yurhel.alex.anotes.shared.edit_folder
import com.yurhel.alex.anotes.shared.email_link
import com.yurhel.alex.anotes.shared.folders
import com.yurhel.alex.anotes.shared.local_sync
import com.yurhel.alex.anotes.shared.main
import com.yurhel.alex.anotes.shared.privacy_link
import com.yurhel.alex.anotes.shared.privacy_policy
import com.yurhel.alex.anotes.shared.settings
import com.yurhel.alex.anotes.shared.support
import com.yurhel.alex.anotes.shared.sync_option
import com.yurhel.alex.anotes.ui.components.RadioDropdownMenuItem
import com.yurhel.alex.anotes.ui.components.SimpleEditSheet
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    vm: SettingsViewModel
) {
    BackHandlerCustom(onBack)

    val modifier = Modifier
        .padding(horizontal = 20.dp, vertical = 5.dp)
        .fillMaxWidth()

    val emailLink = stringResource(Res.string.email_link)
    val privacyLink = stringResource(Res.string.privacy_link)
    val codeLink = stringResource(Res.string.code_link)
    val aboutSection = listOf(
        Pair(stringResource(Res.string.app_ver) + ": " + vm.getAppVersion(), null),
        Pair(stringResource(Res.string.support)) { vm.openLink(emailLink) },
        Pair(stringResource(Res.string.privacy_policy)) { vm.openLink(privacyLink) },
        Pair(stringResource(Res.string.app_code)) { vm.openLink(codeLink) }
    )

    val clipboard = LocalClipboard.current
    val mainStr = stringResource(Res.string.main)
    val addFolderStr = stringResource(Res.string.add_folder)
    val editFolderStr = stringResource(Res.string.edit_folder)
    val delFolderStr = stringResource(Res.string.del_folder)
    var isEditFolderOpen by remember { mutableStateOf(false) }
    if (isEditFolderOpen) {
        SimpleEditSheet(
            onDismissRequest = { isEditFolderOpen = false },
            onSave = vm::onSaveFolder,
            copyToClipboard = { vm.copyToClipboard(it, clipboard) },
            infoText = if (vm.editedFolder != null) editFolderStr else addFolderStr,
            initText = if (vm.editedFolder != null) vm.editedFolder?.title ?: "" else ""
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(Res.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(count = 3) { s ->
                ElevatedCard(Modifier.padding(horizontal = 10.dp)) {
                    Spacer(Modifier.height(10.dp))
                    // Sync section
                    if (s == 0) {
                        Text(
                            text = stringResource(Res.string.sync_option),
                            modifier = modifier,
                            style = MaterialTheme.typography.titleMedium
                        )
                        RadioDropdownMenuItem(
                            onClick = { vm.chooseSyncType(SyncType.drive.name) },
                            text = stringResource(Res.string.drive_sync),
                            isSelected = vm.syncType == SyncType.drive.name
                        )
                        RadioDropdownMenuItem(
                            onClick = { vm.chooseSyncType(SyncType.local.name) },
                            text = stringResource(Res.string.local_sync),
                            isSelected = vm.syncType == SyncType.local.name
                        )
                    }
                    // Folders section
                    if (s == 1) {
                        Text(
                            text = stringResource(Res.string.folders),
                            modifier = modifier,
                            style = MaterialTheme.typography.titleMedium
                        )
                        vm.folders.forEachIndexed { idx, f ->
                            Row(
                                modifier = modifier.padding(start = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = f.title.takeIf { it.isNotEmpty() } ?: mainStr,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        vm.updateEditedFolder(f)
                                        isEditFolderOpen = true
                                    },
                                    content = {
                                        Icon(Icons.Outlined.Edit, editFolderStr)
                                    }
                                )
                                IconButton(
                                    onClick = { vm.deleteFolder(f) },
                                    enabled = idx != 0
                                ) {
                                    Icon(Icons.Outlined.Delete, delFolderStr)
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                vm.updateEditedFolder(null)
                                isEditFolderOpen = true
                            },
                            modifier = modifier,
                            border = BorderStroke(
                                width = 1.dp,
                                color = ButtonDefaults.outlinedButtonColors().contentColor
                            ),
                            shape = CardDefaults.shape
                        ) {
                            Icon(Icons.Outlined.Add, addFolderStr)
                        }
                    }
                    // About section
                    if (s == 2) {
                        aboutSection.forEach {
                            if (it.second == null) {
                                Text(
                                    text = it.first,
                                    modifier = Modifier
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                        .fillMaxWidth(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            } else {
                                Text(
                                    text = it.first,
                                    modifier = Modifier
                                        .clickable { it.second?.invoke() }
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                        .fillMaxWidth(),
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}