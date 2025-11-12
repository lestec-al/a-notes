package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.yurhel.alex.anotes.ui.theme.predefinedColors
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColorPicker(
    onColorChooserClick: (Color) -> Unit,
    initColor: Color
) {
    var redColor by remember { mutableIntStateOf(initColor.red.times(255.0).toInt()) }
    var greenColor by remember { mutableIntStateOf(initColor.green.times(255.0).toInt()) }
    var blueColor by remember { mutableIntStateOf(initColor.blue.times(255.0).toInt()) }
    val fullColor = Color(redColor, greenColor, blueColor)

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
                    .background(c)
                    .clickable {
                        redColor = c.red.times(255.0).toInt()
                        greenColor = c.green.times(255.0).toInt()
                        blueColor = c.blue.times(255.0).toInt()
                        onColorChooserClick(c)
                    }
            ) {
                if (c == fullColor) {
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
                onColorChooserClick(fullColor)
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
                onColorChooserClick(fullColor)
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
                onColorChooserClick(fullColor)
            },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = fullColor,
                activeTrackColor = fullColor
            )
        )
    }
}