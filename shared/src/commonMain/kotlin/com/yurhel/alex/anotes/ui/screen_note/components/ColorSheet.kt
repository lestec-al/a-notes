package com.yurhel.alex.anotes.ui.screen_note.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.reset
import com.yurhel.alex.anotes.ui.components.BaseBottomSheet
import com.yurhel.alex.anotes.ui.components.ColorPicker
import com.yurhel.alex.anotes.ui.screen_note.icons.reset_color
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSheet(
    onDismissRequest: () -> Unit,
    onColorReset: () -> Unit,
    onColorChoose: (Color) -> Unit,
    colorSheetFor: String,
    textColorStr: String,
    span: SpanStyle
) {
    BaseBottomSheet(onDismissRequest = onDismissRequest) {
        // Top
        Row(
            modifier = Modifier
                .padding(start = 15.dp, end = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = colorSheetFor,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onColorReset) {
                Icon(reset_color, stringResource(Res.string.reset))
            }
        }
        // Content
        ColorPicker(
            onColorChooserClick = onColorChoose,
            initColor = if (colorSheetFor == textColorStr) {
                span.color
            } else {
                span.background
            }
        )
    }
}