package com.yurhel.alex.anotes.data.dao

import com.yurhel.alex.anotes.data.NoteObj
import db.NotesQueries

class NotesDao(private val db: NotesQueries) {

    fun insert(noteObj: NoteObj) {
        db.insert(
            withTasks = if (noteObj.isArchived) 1 else 0,
            type = noteObj.type,
            text = noteObj.text,
            dateUpdate = noteObj.dateUpdate.toString(),
            dateCreate = noteObj.dateCreate.toString()
        )
    }

    fun update(noteObj: NoteObj) {
        db.update(
            withTasks = if (noteObj.isArchived) 1 else 0,
            type = noteObj.type,
            text = noteObj.text,
            dateUpdate = noteObj.dateUpdate.toString(),
            id = noteObj.id.toLong()
        )
    }

    fun delete(id: Int) {
        db.delete(id.toLong())
    }

    fun getLast(): NoteObj? {
        val note = try {
            val i = db.getLast().executeAsOne()
            NoteObj(
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

    fun getAll(query: String = ""): List<NoteObj> {
        val list = mutableListOf<NoteObj>()
        for (i in db.getQuery(query).executeAsList()) {
            list.add(
                NoteObj(
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

    fun getById(id: Int): NoteObj? {
        val note = try {
            val i = db.getById(id.toLong()).executeAsOne()
            NoteObj(
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