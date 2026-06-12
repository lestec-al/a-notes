package com.yurhel.alex.anotes.data.dao

import com.yurhel.alex.anotes.data.Status
import db.StatusesQueries

class StatusesDao(private val db: StatusesQueries) {

    fun insert(status: Status) {
        db.insert(
            status.title,
            status.color.toLong(),
            status.note.toLong()
        )
    }

    fun update(status: Status) {
        db.update(
            status.title,
            status.color.toLong(),
            status.note.toLong(),
            status.id.toLong()
        )
    }

    fun delete(id: Int) {
        db.delete(id.toLong())
    }

    fun deleteManyByNote(note: Int) {
        db.deleteManyByNote(note.toLong())
    }

    fun getManyByNote(note: Int): List<Status> {
        val list = mutableListOf<Status>()
        val result = db.getManyByNote(note.toLong()).executeAsList()
        for (i in result) {
            list.add(
                Status(
                    id = i.id.toInt(),
                    title = i.title ?: "",
                    color = i.color?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0
                )
            )
        }
        return list
    }
    fun getAll(): List<Status> {
        val list = mutableListOf<Status>()
        val result = db.getAll().executeAsList()
        for (i in result) {
            list.add(
                Status(
                    id = i.id.toInt(),
                    title = i.title ?: "",
                    color = i.color?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0
                )
            )
        }
        return list
    }
}