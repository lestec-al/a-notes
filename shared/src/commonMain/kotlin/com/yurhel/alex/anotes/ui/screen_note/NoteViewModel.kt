package com.yurhel.alex.anotes.ui.screen_note

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.mohamedrejeb.richeditor.model.HeadingStyle
import com.mohamedrejeb.richeditor.model.RichTextState
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.add_img
import com.yurhel.alex.anotes.shared.add_link
import com.yurhel.alex.anotes.shared.align_center
import com.yurhel.alex.anotes.shared.align_justify
import com.yurhel.alex.anotes.shared.align_left
import com.yurhel.alex.anotes.shared.align_right
import com.yurhel.alex.anotes.shared.back_color
import com.yurhel.alex.anotes.shared.bold
import com.yurhel.alex.anotes.shared.del_img
import com.yurhel.alex.anotes.shared.format_h
import com.yurhel.alex.anotes.shared.italic
import com.yurhel.alex.anotes.shared.line_through
import com.yurhel.alex.anotes.shared.list_bulleted
import com.yurhel.alex.anotes.shared.list_numbered
import com.yurhel.alex.anotes.shared.text_color
import com.yurhel.alex.anotes.shared.underline
import com.yurhel.alex.anotes.shared.undo
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.screen_note.icons.format_h1
import com.yurhel.alex.anotes.ui.screen_note.icons.format_h2
import com.yurhel.alex.anotes.ui.screen_note.icons.format_h3
import com.yurhel.alex.anotes.ui.screen_note.icons.format_h4
import com.yurhel.alex.anotes.ui.screen_note.icons.format_h5
import com.yurhel.alex.anotes.ui.screen_note.icons.format_h6
import com.yurhel.alex.anotes.ui.utils.BottomBarButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

class NoteViewModel(val vm: MainViewModel): ViewModel() {
    class Factory(private val vm: MainViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            NoteViewModel(vm = vm) as T
    }

    private val db = vm.db
    private val selectedNote = vm.selectedNote
    private val platform = vm.platform


    val state = RichTextState()
    var colorSheetOpenFor: StringResource? by mutableStateOf(null)
        private set
    var linkSheetOpenFor: Pair<String, String>? by mutableStateOf(null)
        private set

    fun closeColorSheet() { colorSheetOpenFor = null }

    fun updateLinkSheetFor(field: Pair<String, String>?) { linkSheetOpenFor = field }

    fun sheetChooseColor(isForText: Boolean, newColor: Color?) {
        if (isForText) {
            state.toggleSpanStyle(
                SpanStyle(color = newColor ?: state.currentSpanStyle.color)
            )
        } else {
            state.toggleSpanStyle(
                SpanStyle(background = newColor ?: state.currentSpanStyle.background)
            )
        }
    }

    fun toggleH(s: HeadingStyle) {
        if (state.currentHeadingStyle == s) {
            // Remove heading level (back to a normal paragraph)
            state.setHeadingStyle(HeadingStyle.Normal)
        } else {
            // Make the current paragraph on level
            state.setHeadingStyle(s)
        }
    }

    fun getToolButtons(): List<BottomBarButton?> {
        val textColorStr = Res.string.text_color
        val backColorStr = Res.string.back_color

        return listOf(
            BottomBarButton(
                onClick = state.history::undo,
                icon = Icons.AutoMirrored.Filled.Undo,
                contentDescription = Res.string.undo,
                enabled = state.history.canUndo
            ),
            null,
            BottomBarButton(
                onClick = { if (isAddImage) addImage() else delImage() },
                icon = Icons.Outlined.Image,
                contentDescription = if (isAddImage) Res.string.add_img else Res.string.del_img,
                toggled = !isAddImage
            ),
            null,
            BottomBarButton(
                onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                icon = Icons.Default.FormatBold,
                contentDescription = Res.string.bold,
                toggled = state.currentSpanStyle.fontWeight == FontWeight.Bold
            ),
            BottomBarButton(
                onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                icon = Icons.Default.FormatItalic,
                contentDescription = Res.string.italic,
                toggled = state.currentSpanStyle.fontStyle == FontStyle.Italic
            ),
            BottomBarButton(
                onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                icon = Icons.Default.FormatUnderlined,
                contentDescription = Res.string.underline,
                toggled = state.currentSpanStyle.textDecoration == TextDecoration.Underline
            ),
            BottomBarButton(
                onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) },
                icon = Icons.Default.FormatStrikethrough,
                contentDescription = Res.string.line_through,
                toggled = state.currentSpanStyle.textDecoration == TextDecoration.LineThrough
            ),
            null,
            BottomBarButton(
                onClick = { state.addParagraphStyle(ParagraphStyle(textAlign = TextAlign.Left)) },
                icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                contentDescription = Res.string.align_left,
                toggled = state.currentParagraphStyle.textAlign == TextAlign.Left
            ),
            BottomBarButton(
                onClick = { state.addParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) },
                icon = Icons.Default.FormatAlignCenter,
                contentDescription = Res.string.align_center,
                toggled = state.currentParagraphStyle.textAlign == TextAlign.Center
            ),
            BottomBarButton(
                onClick = { state.addParagraphStyle(ParagraphStyle(textAlign = TextAlign.Right)) },
                icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                contentDescription = Res.string.align_right,
                toggled = state.currentParagraphStyle.textAlign == TextAlign.Right
            ),
            BottomBarButton(
                onClick = { state.addParagraphStyle(ParagraphStyle(textAlign = TextAlign.Justify)) },
                icon = Icons.Default.FormatAlignJustify,
                contentDescription = Res.string.align_justify,
                toggled = state.currentParagraphStyle.textAlign == TextAlign.Justify
            ),
            null,
            BottomBarButton(
                onClick = { toggleH(HeadingStyle.H1) },
                icon = format_h1,
                contentDescription = Res.string.format_h,
                toggled = state.currentHeadingStyle == HeadingStyle.H1
            ),
            BottomBarButton(
                onClick = { toggleH(HeadingStyle.H2) },
                icon = format_h2,
                contentDescription = Res.string.format_h,
                toggled = state.currentHeadingStyle == HeadingStyle.H2
            ),
            BottomBarButton(
                onClick = { toggleH(HeadingStyle.H3) },
                icon = format_h3,
                contentDescription = Res.string.format_h,
                toggled = state.currentHeadingStyle == HeadingStyle.H3
            ),
            BottomBarButton(
                onClick = { toggleH(HeadingStyle.H4) },
                icon = format_h4,
                contentDescription = Res.string.format_h,
                toggled = state.currentHeadingStyle == HeadingStyle.H4
            ),
            BottomBarButton(
                onClick = { toggleH(HeadingStyle.H5) },
                icon = format_h5,
                contentDescription = Res.string.format_h,
                toggled = state.currentHeadingStyle == HeadingStyle.H5
            ),
            BottomBarButton(
                onClick = { toggleH(HeadingStyle.H6) },
                icon = format_h6,
                contentDescription = Res.string.format_h,
                toggled = state.currentHeadingStyle == HeadingStyle.H6
            ),
            null,
            BottomBarButton(
                onClick = state::toggleOrderedList,
                icon = Icons.Default.FormatListNumbered,
                contentDescription = Res.string.list_numbered,
                toggled = state.isOrderedList
            ),
            BottomBarButton(
                onClick = state::toggleUnorderedList,
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = Res.string.list_bulleted,
                toggled = state.isUnorderedList
            ),
            null,
            BottomBarButton(
                onClick = { linkSheetOpenFor = Pair(state.toText(state.selection), "") },
                icon = Icons.Default.Link,
                contentDescription = Res.string.add_link,
                enabled = state.selection.length > 0
            ),
            null,
            BottomBarButton(
                onClick = { colorSheetOpenFor = textColorStr },
                icon = Icons.Default.FormatColorText,
                contentDescription = textColorStr,
                iconColor = state.currentSpanStyle.color
            ),
            BottomBarButton(
                onClick = { colorSheetOpenFor = backColorStr },
                icon = Icons.Default.FormatColorFill,
                contentDescription = backColorStr,
                iconColor = state.currentSpanStyle.background
            )
        )
    }


    var noteImage by mutableStateOf<ImageBitmap?>(null)
        private set
    var isAddImage by mutableStateOf(true)
        private set

    private fun addImage() = viewModelScope.launch {
        platform.importImage { base64Str ->
            val noteId = selectedNote?.id
            if (noteId != null) {
                db.board.addUpdateImage(noteId, base64Str)
                updateImageData()
            }
        }
    }

    private fun delImage() {
        selectedNote?.id?.let { db.board.delImage(it) }
        updateImageData()
    }

    private fun updateImageData() {
        noteImage = selectedNote?.id?.let {
            val imgStr = db.board.getImage(it)
            if (imgStr == null) null else {
                val img = platform.toImageBitmap(db.board.getImage(it), false)
                img ?: ImageBitmap(50, 50)
            }
        }
        isAddImage = noteImage == null
    }


    fun openLink(link: String) = vm.platform.openLink(link)
    fun saveNote() = vm.saveNote(text = state.toHtml())

    init {
        val isHtml = vm.selectedNote?.format == 10
        val text = vm.editText.text.toString()
        if (isHtml) state.setHtml(text) else state.setText(text)
        updateImageData()
    }
}