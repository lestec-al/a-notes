package com.yurhel.alex.anotes.data.dao

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.yurhel.alex.anotes.data.BoardLineObj
import db.DrawingsQueries

class BoardDao(private val db: DrawingsQueries) {

    fun addUpdateImage(noteId: Int, base64Str: String) {
        delImage(noteId)
        db.insertImage(
            noteId = noteId.toLong(),
            base64Str = base64Str
        )
    }

    fun getImage(noteId: Int): String? {
        return try {
            db.getImagesByNoteId(noteId.toLong()).executeAsOne().base64Str
        } catch (_: Exception) { null }
    }

    fun delImage(noteId: Int) {
        db.deleteImagesByNoteId(noteId.toLong())
    }

    fun addDraw(it: BoardLineObj) {
        db.insertDraw(
            noteId = it.noteId.toLong(),
            startX = it.start.x.toString(),
            startY = it.start.y.toString(),
            endX = it.end.x.toString(),
            endY = it.end.y.toString(),
            color = it.color.toArgb().toString(),
            strokeWidth = it.strokeWidth.toString()
        )
    }

    fun getDraws(noteId: Int): List<BoardLineObj> {
        return db.getDrawsByNoteId(noteId.toLong()).executeAsList().map {
            BoardLineObj(
                noteId = noteId,
                start = Offset(x = it.startX!!.toFloat(), y = it.startY!!.toFloat()),
                end = Offset(x = it.endX!!.toFloat(), y = it.endY!!.toFloat()),
                color = Color(it.color!!.toInt()),
                strokeWidth = it.strokeWidth!!.toFloat()
            )
        }
    }

    fun delDraws(noteId: Int) {
        db.deleteDrawsByNoteId(noteId.toLong())
    }

    fun delLastDraw() {
        val rowId = db.getLastDrawId().executeAsOne().MAX
        if (rowId != null) {
            db.deleteDrawById(rowId)
        }
    }
}