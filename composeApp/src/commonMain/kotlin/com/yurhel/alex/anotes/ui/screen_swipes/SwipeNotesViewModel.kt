package com.yurhel.alex.anotes.ui.screen_swipes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.screen_swipes.utils.Edit
import com.yurhel.alex.anotes.ui.screen_swipes.utils.SwipeTextObj
import com.yurhel.alex.anotes.ui.screen_swipes.utils.SwipeTextPos
import com.yurhel.alex.anotes.ui.screen_swipes.utils.importSwipesFromText
import com.yurhel.alex.anotes.ui.screen_swipes.utils.getSwipesTitle
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlin.reflect.KClass

class SwipeNotesViewModel(val vm: MainViewModel): ViewModel() {
    class Factory(val vm: MainViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            SwipeNotesViewModel(vm = vm) as T
    }

    var editedSwipeTextId = -1

    var leftColor by mutableStateOf(Color.Red)
        private set
    var rightColor by mutableStateOf(Color.Green)
        private set
    var edit by mutableStateOf(Edit.None)
        private set
    var editedText by mutableStateOf("")
        private set
    var leftText by mutableStateOf("")
        private set
    var rightText by mutableStateOf("")
        private set
    var noteText by mutableStateOf(getSwipesTitle(vm.selectedNote?.text ?: ""))
        private set

    var data by mutableStateOf(listOf<SwipeTextObj>().let {
        importSwipesFromText(vm.selectedNote?.text ?: "") { leftText, leftColor, rightText, rightColor ->
            this.leftText = leftText
            this.leftColor = leftColor
            this.rightText = rightText
            this.rightColor = rightColor
        }
    })
        private set

    fun updateEdit(
        target: Edit,
        text: String = "",
        swipeTextId: Int = -1
    ) {
        edit = target
        editedText = text
        editedSwipeTextId = swipeTextId
    }

    fun getEditedColorValue() = when (edit) {
        Edit.Left -> leftColor
        Edit.Right -> rightColor
        else -> rightColor
    }

    fun saveNote() {
        vm.updateEditTextValue(noteText + exportDataToText())
        vm.saveNote()
    }

    fun deleteSwipeText() {
        if (editedSwipeTextId != -1) {
            data = data.filter { it.id != editedSwipeTextId }
        }
    }

    fun onSaveEdit(
        newText: String,
        color: Color
    ) {
        when (edit) {
            Edit.NoteText -> {
                noteText = newText
                saveNote()
            }
            Edit.SwipeText -> {
                if (editedSwipeTextId != -1) {
                    data = data.map {
                        if (it.id == editedSwipeTextId) {
                            it.copy(text = newText)
                        } else {
                            it
                        }
                    }
                } else {
                    if (newText.isNotEmpty()) {
                        val tempData = data.toMutableList()
                        tempData.add(
                            SwipeTextObj(
                                id = data.size,
                                pos = SwipeTextPos.Left,
                                text = newText
                            )
                        )
                        data = tempData
                    }
                }
            }
            Edit.Left -> {
                leftText = newText
                leftColor = color
            }
            Edit.Right -> {
                rightText = newText
                rightColor = color
            }
            Edit.None -> {}
        }
        updateEdit(Edit.None)
    }

    fun onDragStoppedUpdateData(
        offsetX: Float,
        obj: SwipeTextObj
    ) {
        data = data.map {
            if (it.id == obj.id) it.copy(pos = if (offsetX > 0) SwipeTextPos.Right else SwipeTextPos.Left) else it
        }.toMutableList()
    }

    private fun exportDataToText() = buildJsonArray {
        addJsonObject {
            put("leftText", leftText)
            put("leftColor", leftColor.toArgb())
            put("rightText", rightText)
            put("rightColor", rightColor.toArgb())
        }
        data.forEach {
            addJsonObject {
                put("id", it.id)
                put("pos", it.pos.name)
                put("text", it.text)
            }
        }
    }.toString()
}