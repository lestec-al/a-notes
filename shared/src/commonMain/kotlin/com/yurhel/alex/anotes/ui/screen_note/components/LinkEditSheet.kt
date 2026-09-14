package com.yurhel.alex.anotes.ui.screen_note.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.delete
import com.yurhel.alex.anotes.shared.save
import com.yurhel.alex.anotes.ui.components.BaseBottomSheet
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkEditSheet(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onSave: (String) -> Unit,
    linkSheetFor: Pair<String, String>,
    editLinkStr: String
) {
    var editText by remember { mutableStateOf(linkSheetFor.first) }
    var editUrl by remember { mutableStateOf(linkSheetFor.second) }

    BaseBottomSheet(onDismissRequest = onDismissRequest) {
        // Top
        Row(
            modifier = Modifier
                .padding(start = 15.dp, end = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = editLinkStr,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            if (editText.isNotEmpty() && editUrl.isNotEmpty() ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        stringResource(Res.string.delete)
                    )
                }
            }
            IconButton(onClick = { onSave(editUrl) }) {
                Icon(
                    Icons.Outlined.Save,
                    stringResource(Res.string.save)
                )
            }
        }
        // Content
        TextField(
            value = editUrl,
            onValueChange = { editUrl = it },
            label = { Text(text = editText) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .fillMaxWidth()
        )
    }
}