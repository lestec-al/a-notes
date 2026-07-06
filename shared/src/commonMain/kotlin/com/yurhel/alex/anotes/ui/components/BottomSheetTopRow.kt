package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.copy
import com.yurhel.alex.anotes.shared.delete
import com.yurhel.alex.anotes.shared.save
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomSheetTopRow(
    infoText: String,
    saveAction: () -> Unit,
    copyAction: (() -> Unit)? = null,
    deleteAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .padding(start = 15.dp, end = 10.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = infoText,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.weight(1f))
        if (copyAction != null) {
            IconButton(onClick = { copyAction() }) {
                Icon(Icons.Default.CopyAll, stringResource(Res.string.copy))
            }
        }
        if (deleteAction != null) {
            IconButton(onClick = { deleteAction() }) {
                Icon(Icons.Outlined.Delete, stringResource(Res.string.delete))
            }
        }
        IconButton(onClick = { saveAction() }) {
            Icon(Icons.Outlined.Save, stringResource(Res.string.save))
        }
    }
}