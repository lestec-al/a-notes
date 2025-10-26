package com.yurhel.alex.anotes.feature_board.ui

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
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.disable_all_actions
import anotes.composeapp.generated.resources.edit_note
import anotes.composeapp.generated.resources.enable_draw
import anotes.composeapp.generated.resources.undo
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.feature_board.ui.components.ActionButton
import com.yurhel.alex.anotes.feature_board.ui.components.EditBoardBottomSheet
import com.yurhel.alex.anotes.feature_board.ui.components.SimpleEditBottomSheet
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.components.NoteBottomBar
import org.jetbrains.compose.resources.stringResource

@Composable
fun BoardScreen(
    vm: MainViewModel,
    vmBoard: BoardViewModel,
    onBack: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val boardState = rememberTransformableState { _, offsetChange, _ ->
        vmBoard.updateBoardOffsets(offsetChange)
    }
    BackHandlerCustom { vmBoard.saveDrawToDB(graphicsLayer, onBack) }

    Scaffold(
        bottomBar = {
            NoteBottomBar(
                vm = vm,
                coroutineScope = rememberCoroutineScope(),
                onBackAfterDelete = onBack,
                onBackButtonClick = {
                    vmBoard.saveDrawToDB(graphicsLayer, onBack)
                },
                onGetTextButtonClick = null,
                additionalButtons = listOf(
                    Triple(stringResource(Res.string.edit_note), Icons.Outlined.DriveFileRenameOutline) {
                        vmBoard.updateIsEditSheetOpen(true)
                    },
                    Triple(stringResource(Res.string.undo), Icons.AutoMirrored.Filled.Undo, vmBoard::undo)
                )
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Open draw settings
                if (vmBoard.isDraw) {
                    SmallFloatingActionButton(onClick = vmBoard::updateIsEditBoardSheetOpen) {
                        Canvas(Modifier) {
                            drawCircle(
                                color = vmBoard.drawColor.copy(alpha = vmBoard.drawColorAlpha),
                                radius = if (vmBoard.strokeWidth > 14.dp.toPx()) 14.dp.toPx() else vmBoard.strokeWidth
                            )
                        }
                    }
                }
                // Enable draw
                ActionButton(
                    onClick = { vmBoard.enableDisableDraw(true) },
                    isActive = vmBoard.isDraw,
                    icon = Icons.Outlined.Edit,
                    contentDescription = stringResource(Res.string.enable_draw)
                )
                // Disable all actions
                ActionButton(
                    onClick = { vmBoard.enableDisableDraw(false) },
                    isActive = !vmBoard.isDraw,
                    icon = Icons.Outlined.BackHand,
                    contentDescription = stringResource(Res.string.disable_all_actions)
                )
            }
        }
    ) { paddingValues ->
        // Board
        Canvas(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
                // Move board (making board look infinite)
                .transformable(state = boardState)
                // Drawing
                .pointerInput(vmBoard.isDraw) {
                    if (vmBoard.isDraw) {
                        detectDragGestures(onDrag = vmBoard::onDrawDrag)
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
        ) {
            vmBoard.localDraw.forEach {
                drawLine(
                    color = it.color,
                    start = it.start + vmBoard.boardOffset,
                    end = it.end + vmBoard.boardOffset,
                    strokeWidth = it.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        // Loading indicator
        if (vmBoard.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        // Bottom sheets
        if (vmBoard.isEditTextSheetOpen) {
            SimpleEditBottomSheet(
                onDismissRequest = vmBoard::updateIsEditSheetOpen,
                onSave = vmBoard::updateNoteName,
                infoText = stringResource(Res.string.edit_note),
                initText = vmBoard.noteName
            )
        }
        if (vmBoard.isEditBoardSheetOpen) {
            EditBoardBottomSheet(vmBoard)
        }
    }
}