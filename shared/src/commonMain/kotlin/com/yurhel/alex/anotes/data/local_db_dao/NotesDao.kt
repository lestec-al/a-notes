package com.yurhel.alex.anotes.data.local_db_dao

import com.yurhel.alex.anotes.data.Note
import db.NotesQueries

class NotesDao(private val db: NotesQueries) {

    fun insert(note: Note) {
        db.insert(
            withTasks = if (note.isArchived) 1 else 0,
            type = note.type,
            text = note.text,
            dateUpdate = note.dateUpdate.toString(),
            dateCreate = note.dateCreate.toString()
        )
    }

    fun update(note: Note) {
        db.update(
            withTasks = if (note.isArchived) 1 else 0,
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
            Note(
                id = i.id.toInt(),
                text = i.text ?: "",
                type = i.type ?: "",
                isArchived = i.withTasks?.toInt() == 1,
                dateUpdate = i.dateUpdate?.toLong() ?: 0,
                dateCreate = i.dateCreate?.toLong() ?: 0
            )
        } catch (_: Exception) {
            null
        }
        return note
    }

    fun getAll(query: String = ""): List<Note> {
        val list = mutableListOf<Note>()
        for (i in db.getQuery(query).executeAsList()) {
            list.add(
                Note(
                    id = i.id.toInt(),
                    text = i.text ?: "",
                    type = i.type ?: "",
                    isArchived = i.withTasks?.toInt() == 1,
                    dateUpdate = i.dateUpdate?.toLong() ?: 0,
                    dateCreate = i.dateCreate?.toLong() ?: 0
                )
            )
        }
        return list
    }

    fun getById(id: Int): Note? {
        val note = try {
            val i = db.getById(id.toLong()).executeAsOne()
            Note(
                id = i.id.toInt(),
                text = i.text ?: "",
                type = i.type ?: "",
                isArchived = i.withTasks?.toInt() == 1,
                dateUpdate = i.dateUpdate?.toLong() ?: 0,
                dateCreate = i.dateCreate?.toLong() ?: 0
            )
        } catch (_: Exception) {
            null
        }
        return note
    }
}