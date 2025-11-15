package com.yurhel.alex.anotes.ui.screen_board.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.color
import anotes.composeapp.generated.resources.opacity
import anotes.composeapp.generated.resources.thickness
import com.yurhel.alex.anotes.ui.screen_board.BoardViewModel
import com.yurhel.alex.anotes.ui.components.ColorPicker
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBoardBottomSheet(vmBoard: BoardViewModel) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { vmBoard.updateIsEditBoardSheetOpen(false) },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(Res.string.color),
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        ColorPicker(
            onColorChooserClick = vmBoard::onColorChooserClick,
            initColor = vmBoard.drawColor
        )
        // Thickness & opacity
        Column(modifier = Modifier.padding(20.dp)) {
            // Change thickness (strokeWidth)
            Text(text = stringResource(Res.string.thickness))
            Slider(
                value = vmBoard.strokeWidth,
                onValueChange = vmBoard::updateStrokeWidth,
                valueRange = 2f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            // Change opacity (alpha)
            Text(text = stringResource(Res.string.opacity))
            Slider(
                value = vmBoard.drawColorAlpha,
                onValueChange = vmBoard::updateDrawColorAlpha,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}