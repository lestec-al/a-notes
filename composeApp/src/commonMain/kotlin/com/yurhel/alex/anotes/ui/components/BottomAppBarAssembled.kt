package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    BottomAppBar(modifier = Modifier.height(50.dp)) {

        // Back button
        val backText = stringResource(Res.string.back)
        Tooltip(tooltipText = backText) {
            IconButton(onClick = onBackButtonClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, backText)
            }
        }

        // Delete note
        Tooltip(tooltipText = stringResource(Res.string.delete)) {
            IconButton(
                modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                onClick = {
                    isInfoBottomSheetOpen.value = true
                }
            ) {
                Icon(Icons.Outlined.Delete, stringResource(Res.string.delete))
            }
        }

        // Second button
        Tooltip(tooltipText = secondButtonText) {
            IconButton(
                modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                onClick = onSecondButtonClick
            ) {
                Icon(secondButtonIcon, secondButtonText)
            }
        }

        // Note updated text ???
        if (orientation == OrientationObj.Desktop) {
            // Only for desktop
            Box(
                contentAlignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 15.dp, 10.dp)
            ) {
                Tooltip(
                    tooltipText = "${stringResource(Res.string.created)}: ${
                        vm.formatDate(vm.getNoteDate(true))
                    }"
                ) {
                    Text(
                        text = "${stringResource(Res.string.updated)}: ${vm.formatDate(vm.getNoteDate())}",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        } else {
            // Only for android
            TooltipText(
                text = "${stringResource(Res.string.updated)}: ${vm.formatDate(vm.getNoteDate())}",
                tooltipText = "${stringResource(Res.string.created)}: ${
                    vm.formatDate(vm.getNoteDate(true))
                }",
                coroutineScope = coroutineScope
            )
        }


        // BottomSheet for asking for deletion
        if (isInfoBottomSheetOpen.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    isInfoBottomSheetOpen.value = false
                }
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
                        Icon(Icons.Default.Close, "No")
                    }

                    IconButton(
                        onClick = {
                            vm.deleteNote()
                            onBack()
                        }
                    ) {
                        Icon(Icons.Default.Check, "Yes")
                    }
                }
            }
        }
    }
}