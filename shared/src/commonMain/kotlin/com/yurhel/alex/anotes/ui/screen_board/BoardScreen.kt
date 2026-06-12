package com.yurhel.alex.anotes.ui.screen_board

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.disable_all_actions
import com.yurhel.alex.anotes.shared.edit_note
import com.yurhel.alex.anotes.shared.enable_draw
import com.yurhel.alex.anotes.shared.undo
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.ui.screen_board.components.ActionButton
import com.yurhel.alex.anotes.ui.screen_board.components.EditBoardBottomSheet
import com.yurhel.alex.anotes.SetStatusBarColor
import com.yurhel.alex.anotes.ui.components.CustomScaffold
import com.yurhel.alex.anotes.ui.NoteBottomBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun BoardScreen(
    vm: BoardViewModel,
    onBack: () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    val boardState = rememberTransformableState { _, _, panChange, _ ->
        vm.updateBoardOffsets(panChange)
    }
    BackHandlerCustom { vm.saveDrawToDB(graphicsLayer, onBack) }
    SetStatusBarColor(true, vm.vm.darkTheme)

    CustomScaffold(
        bottomBar = {
            NoteBottomBar(
                vm = vm.vm,
                scope = rememberCoroutineScope(),
                onBackAfterDelete = onBack,
                onBackButtonClick = {
                    vm.saveDrawToDB(graphicsLayer, onBack)
                },
                onGetTextButtonClick = null,
                editNoteStr = stringResource(Res.string.edit_note),
                additionalButtons = listOf(
                    Triple(stringResource(Res.string.undo), Icons.AutoMirrored.Filled.Undo, vm::undo)
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Open draw settings
                if (vm.isDraw) {
                    SmallFloatingActionButton(onClick = vm::updateIsEditBoardSheetOpen) {
                        Canvas(Modifier) {
                            drawCircle(
                                color = vm.drawColor.copy(alpha = vm.drawColorAlpha),
                                radius = if (vm.strokeWidth > 14.dp.toPx()) 14.dp.toPx() else vm.strokeWidth
                            )
                        }
                    }
                }
                // Enable draw
                ActionButton(
                    onClick = { vm.enableDisableDraw(true) },
                    isActive = vm.isDraw,
                    icon = Icons.Outlined.Brush,
                    contentDescription = stringResource(Res.string.enable_draw)
                )
                // Disable all actions
                ActionButton(
                    onClick = { vm.enableDisableDraw(false) },
                    isActive = !vm.isDraw,
                    icon = Icons.Outlined.BackHand,
                    contentDescription = stringResource(Res.string.disable_all_actions)
                )
            }
        },
        statusBarColorAlpha = 0f
    ) { bottomPadding, _ ->
        // Board
        Canvas(
            modifier = Modifier
                .padding(bottom = bottomPadding)
                .fillMaxSize()
                .background(Color.White)
                // Move board (making board look infinite)
                .transformable(state = boardState)
                // Drawing
                .pointerInput(vm.isDraw) {
                    if (vm.isDraw) {
                        detectDragGestures(onDrag = vm::onDrawDrag)
                    }
                }
                // For saving as image (bitmap)
                .drawWithContent {
                    // Call record to capture the content in the graphics layer
                    graphicsLayer.record {
                        // Draw the contents of the composable into the graphics layer
                        this@drawWithContent.drawContent()
                    }
                    // Draw the graphics layer on the visible canvas
                    drawLayer(graphicsLayer)
                }
                .testTag("draw_canvas")
        ) {
            vm.localDraw.forEach {
                drawLine(
                    color = it.color,
                    start = it.start + vm.boardOffset,
                    end = it.end + vm.boardOffset,
                    strokeWidth = it.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        // Loading indicator
        if (vm.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // Bottom sheet
        if (vm.isEditBoardSheetOpen) {
            EditBoardBottomSheet(vm)
        }
    }
}