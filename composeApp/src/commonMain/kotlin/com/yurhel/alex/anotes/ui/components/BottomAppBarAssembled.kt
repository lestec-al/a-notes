package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.created
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.delete_info
import anotes.composeapp.generated.resources.updated
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.OrientationObj
import com.yurhel.alex.anotes.ui.getOrientation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomAppBarAssembled(
    vm: MainViewModel,
    coroutineScope: CoroutineScope,
    onBack: () -> Unit,
    onBackButtonClick: () -> Unit,
    onSecondButtonClick: () -> Unit,
    secondButtonIcon: ImageVector,
    secondButtonText: String
) {
    val isInfoBottomSheetOpen = remember { mutableStateOf(false) }
    val orientation = getOrientation()

    BottomAppBar(
        modifier = Modifier.height(50.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Back button
            val backText = stringResource(Res.string.back)
            Tooltip(tooltipText = backText) {
                IconButton(onClick = onBackButtonClick) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, backText, Modifier.size(30.dp))
                }
            }

            // Delete note
            Tooltip(tooltipText = stringResource(Res.string.delete)) {
                IconButton(
                    onClick = {
                        isInfoBottomSheetOpen.value = true
                    }
                ) {
                    Icon(Icons.Outlined.Delete, stringResource(Res.string.delete), Modifier.size(30.dp))
                }
            }

            // Second button
            Tooltip(tooltipText = secondButtonText) {
                IconButton(onClick = onSecondButtonClick) {
                    Icon(secondButtonIcon, secondButtonText, Modifier.size(30.dp))
                }
            }

            // Note updated text ???
            if (orientation == OrientationObj.Desktop) {
                // Only for desktop
                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "${stringResource(Res.string.updated)}: ${vm.formatDate(vm.getNoteDate())}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "${stringResource(Res.string.created)}: ${vm.formatDate(vm.getNoteDate(true))}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            } else {
                // Only for android
                // Tooltip text
                val tooltipState = remember { TooltipState() }
                val popupProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()
                Box(contentAlignment = Alignment.CenterEnd) {
                    TooltipBox(
                        positionProvider = popupProvider,
                        tooltip = {
                            Card {
                                Text(
                                    text = "${stringResource(Res.string.created)}: ${vm.formatDate(vm.getNoteDate(true))}",
                                    modifier = Modifier.padding(5.dp)
                                )
                            }
                        },
                        state = tooltipState,
                        focusable = false,
                        enableUserInput = false
                    ) {
                        Text(
                            text = "${stringResource(Res.string.updated)}: ${vm.formatDate(vm.getNoteDate())}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .clip(shape = CardDefaults.shape)
                                .clickable {
                                    coroutineScope.launch {
                                        tooltipState.show()
                                    }
                                }
                        )
                    }
                }
            }
        }
    }

    // BottomSheet for asking for deletion
    if (isInfoBottomSheetOpen.value) {
        ModalBottomSheet(
            onDismissRequest = {
                isInfoBottomSheetOpen.value = false
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = stringResource(Res.string.delete_info),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 50.dp)
            ) {
                IconButton(
                    onClick = {
                        isInfoBottomSheetOpen.value = false
                    }
                ) {
                    Icon(Icons.Default.Close, "No", Modifier.size(30.dp))
                }

                IconButton(
                    onClick = {
                        vm.deleteNote()
                        onBack()
                    }
                ) {
                    Icon(Icons.Default.Check, "Yes", Modifier.size(30.dp))
                }
            }
        }
    }
}


//@Composable
//fun Modifier.topBorder(thickness: Dp, color: Color): Modifier {
//    val density = LocalDensity.current
//    val strokeWidthPx = density.run { thickness.toPx() }
//    return this then Modifier.drawBehind {
//        val width = size.width
//
//        drawLine(
//            color = color,
//            start = Offset(x = 0f, y = 0f),
//            end = Offset(x = width, y = 0f),
//            strokeWidth = strokeWidthPx
//        )
//    }
//}