package com.yurhel.alex.anotes.ui.feature_board

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yurhel.alex.anotes.data.BoardLineObj
import com.yurhel.alex.anotes.toBase64
import com.yurhel.alex.anotes.ui.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class BoardViewModel(val vm: MainViewModel) : ViewModel() {

    class Factory(val vm: MainViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T = BoardViewModel(
            vm = vm
        ) as T
    }

    var localDraw = mutableStateListOf<BoardLineObj>()
        private set
    var isDraw by mutableStateOf(false)
        private set
    var isEditTextSheetOpen by mutableStateOf(false)
        private set
    var isEditBoardSheetOpen by mutableStateOf(false)
        private set
    var strokeWidth by mutableFloatStateOf(4f)
        private set
    var drawColorAlpha by mutableFloatStateOf(1f)
        private set
    var drawColor by mutableStateOf(Color.Black)
        private set
    var boardOffset by mutableStateOf(Offset(0f, 0f))
        private set
    var noteName by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set

    private var noteId = vm.selectedNote.value!!.id

    init {
        launch {
            updateNoteName(vm.editText.text.toString())
            localDraw.addAll(vm.db.board.getDraws(noteId))
        }
    }

    fun saveDrawToDB(
        graphicsLayer: GraphicsLayer,
        onBack: () -> Unit
    ) {
        launch {
            if (isDraw) {
                enableDisableDraw(false)
                delay(500)
            }
            val base64img = graphicsLayer.toImageBitmap().toBase64()
            if (base64img != null) {
                vm.db.board.addUpdateImage(noteId, base64img)
            }
            localDraw.clear()
            vm.updateEditTextValue(noteName)
            vm.saveNote()
            onBack()
        }
    }

    fun updateNoteName(value: String) {
        noteName = value
    }

    fun updateBoardOffsets(offsetChange: Offset) {
        boardOffset += offsetChange
    }

    fun undo() {
        launch {
            // Undo local
            for (i in 1..10) {
                try {
                    localDraw.removeLast()
                } catch (_: Exception) {}
            }
            // Undo in DB
            viewModelScope.launch {
                for (i in 1..10) {
                    vm.db.board.delLastDraw()
                }
            }
        }
    }

    fun updateStrokeWidth(value: Float) {
        strokeWidth = value
    }

    fun updateDrawColorAlpha(value: Float) {
        drawColorAlpha = value
    }

    fun updateIsEditSheetOpen(value: Boolean = false) {
        isEditTextSheetOpen = value
    }

    fun updateIsEditBoardSheetOpen(value: Boolean = true) {
        isEditBoardSheetOpen = value
    }

    fun onColorChooserClick(color: Color) {
        drawColor = color
    }

    fun onDrawDrag(
        change: PointerInputChange,
        dragAmount: Offset
    ) {
        change.consume()
        val obj = BoardLineObj(
            noteId = noteId,
            start = (change.position - dragAmount) - boardOffset,
            end = change.position - boardOffset,
            color = drawColor.copy(alpha = drawColorAlpha),
            strokeWidth = strokeWidth
        )
        // Add local
        localDraw.add(obj)
        // Add to DB
        viewModelScope.launch {
            vm.db.board.addDraw(obj)
        }
    }

    fun enableDisableDraw(enable: Boolean) {
        isDraw = enable
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            isLoading = true
            block()
            isLoading = false
        }
    }
}