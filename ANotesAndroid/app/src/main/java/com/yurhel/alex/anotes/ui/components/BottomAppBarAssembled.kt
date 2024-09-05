package com.yurhel.alex.anotes.ui.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BottomAppBarAssembled(
    context: Context,
    vm: MainViewModel,
    coroutineScope: CoroutineScope,
    onBack: () -> Unit,
    secondButtonAction: () -> Unit,
    secondButtonIcon: Painter,
    secondButtonText: String
) {
    BottomAppBar(
        modifier = Modifier.height(50.dp)
    ) {
        // Delete note
        val tooltipState = remember { TooltipState() }
        val popupProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()

        TooltipBox(
            positionProvider = popupProvider,
            tooltip = {
                Card {
                    Text(
                        text = context.getString(R.string.delete_press_info),
                        modifier = Modifier.padding(5.dp)
                    )
                }
            },
            state = tooltipState,
            focusable = false,
            enableUserInput = false
        ) {
            Card(
                modifier = Modifier
                    .padding(5.dp, 5.dp, 5.dp, 10.dp)
                    .fillMaxHeight()
                    .width(40.dp)
                    .clip(IconButtonDefaults.outlinedShape)
                    .combinedClickable(
                        onClick = {
                            coroutineScope.launch {
                                tooltipState.show()
                            }
                        },
                        onLongClick = {
                            vm.deleteNote()
                            vm.editText.clearText()
                            onBack()
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Outlined.Delete, context.getString(R.string.delete))
                }
            }
        }

        // Second button
        Tooltip(
            tooltipText = secondButtonText
        ) {
            IconButton(
                modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                onClick = secondButtonAction
            ) {
                Image(
                    painter = secondButtonIcon,
                    contentDescription = secondButtonText,
                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                )
            }
        }

        // Note updated text
        TooltipText(
            text = "${context.getString(R.string.updated)}: ${vm.getNoteDate(context)}",
            tooltipText = "${context.getString(R.string.created)}: ${
                vm.getNoteDate(
                    context,
                    true
                )
            }",
            coroutineScope = coroutineScope
        )
    }
}