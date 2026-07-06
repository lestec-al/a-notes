package com.yurhel.alex.anotes.data.local_db_dao

import com.yurhel.alex.anotes.data.Note
import db.NotesQueries
import db.NotesTable

class NotesDao(private val db: NotesQueries) {

    fun insert(note: Note) {
        db.insert(
            folder = note.folder.toLong(),
            type = note.type,
            text = note.text,
            dateUpdate = note.dateUpdate.toString(),
            dateCreate = note.dateCreate.toString()
        )
    }

    fun update(note: Note) {
        db.update(
            folder = note.folder.toLong(),
            type = note.type,
            text = note.text,
            dateUpdate = note.dateUpdate.toString(),
            id = note.id.toLong()
        )
    }

    fun delete(id: Int) {
        db.delete(id.toLong())
    }

    fun getLast(): Note? {
        val note = try {
            val i = db.getLast().executeAsOne()
            createNote(i)
        } catch (_: Exception) {
            null
        }
        return note
    }

    fun getAll(query: String = ""): List<Note> {
        val list = mutableListOf<Note>()
        for (i in db.getQuery(query).executeAsList()) {
            list.add(createNote(i))
        }
        return list
    }

    fun getById(id: Int): Note? {
        val note = try {
            val i = db.getById(id.toLong()).executeAsOne()
            createNote(i)
        } catch (_: Exception) {
            null
        }
        return note
    }
}

private fun createNote(i: NotesTable) = Note(
    id = i.id.toInt(),
    text = i.text ?: "",
    type = i.type ?: "",
    folder = try { i.folder?.toInt() ?: 0 } catch (_: Exception) { 0 },
    dateUpdate = i.dateUpdate?.toLong() ?: 0,
    dateCreate = i.dateCreate?.toLong() ?: 0
)