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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AskDialog(
    onDismissRequest: () -> Unit,
    isVisible: Boolean,
    infoText: StringResource,
    leftButton: Pair<StringResource, () -> Unit>,
    rightButton: Pair<StringResource, () -> Unit>
) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(infoText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = leftButton.second) {
                        Text(text = stringResource(leftButton.first))
                    }
                    TextButton(onClick = rightButton.second) {
                        Text(text = stringResource(rightButton.first))
                    }
                }
            }
        }
    }
}