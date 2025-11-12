package com.yurhel.alex.anotes.feature_board.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.checked
import anotes.composeapp.generated.resources.color
import anotes.composeapp.generated.resources.opacity
import anotes.composeapp.generated.resources.thickness
import com.yurhel.alex.anotes.feature_board.ui.BoardViewModel
import com.yurhel.alex.anotes.ui.theme.predefinedColors
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBoardBottomSheet(vmBoard: BoardViewModel) {
    val sheetState = rememberModalBottomSheetState()
    var redColor by remember { mutableIntStateOf(vmBoard.drawColor.red.times(255.0).toInt()) }
    var greenColor by remember { mutableIntStateOf(vmBoard.drawColor.green.times(255.0).toInt()) }
    var blueColor by remember { mutableIntStateOf(vmBoard.drawColor.blue.times(255.0).toInt()) }
    val fullColor = Color(redColor, greenColor, blueColor)

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
        // Predefined colors picker
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            predefinedColors.forEach { c ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(2.dp)
                        .height(50.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(c.copy(alpha = vmBoard.drawColorAlpha))
                        .clickable {
                            redColor = c.red.times(255.0).toInt()
                            greenColor = c.green.times(255.0).toInt()
                            blueColor = c.blue.times(255.0).toInt()
                            vmBoard.onColorChooserClick(c)
                        }
                ) {
                    if (c == vmBoard.drawColor) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(Res.string.checked),
                            tint = if (c == Color.White) Color.Black else Color.White
                        )
                    }
                }
            }
        }
        // Color picker
        Column(modifier = Modifier.padding(20.dp)) {
            Slider(
                value = redColor.toFloat(),
                onValueChange = {
                    redColor = it.toInt()
                    vmBoard.onColorChooserClick(fullColor)
                },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = fullColor,
                    activeTrackColor = fullColor
                )
            )
            Slider(
                value = greenColor.toFloat(),
                onValueChange = {
                    greenColor = it.toInt()
                    vmBoard.onColorChooserClick(fullColor)
                },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = fullColor,
                    activeTrackColor = fullColor
                )
            )
            Slider(
                value = blueColor.toFloat(),
                onValueChange = {
                    blueColor = it.toInt()
                    vmBoard.onColorChooserClick(fullColor)
                },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = fullColor,
                    activeTrackColor = fullColor
                )
            )
        }
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