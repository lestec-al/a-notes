package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.sync_collision
import anotes.composeapp.generated.resources.sync_drive
import anotes.composeapp.generated.resources.sync_local
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.SyncActionTypes
import org.jetbrains.compose.resources.stringResource

@Composable
fun SyncDialog(
    isVisible: Boolean,
    vm: MainViewModel
) {
    if (isVisible) {
        Dialog(onDismissRequest = { vm.openSyncDialog(false) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                // Title
                Text(
                    text = stringResource(Res.string.sync_collision),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                // Buttons
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        vm.syncData(SyncActionTypes.ManualImport, vm)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = stringResource(Res.string.sync_drive))
                    }
                    TextButton(onClick = {
                        vm.syncData(SyncActionTypes.ManualExport, vm)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = stringResource(Res.string.sync_local))
                    }
                }
            }
        }
    }
}