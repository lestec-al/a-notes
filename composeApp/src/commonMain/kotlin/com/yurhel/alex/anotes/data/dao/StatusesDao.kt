package com.yurhel.alex.anotes.data.dao

import com.yurhel.alex.anotes.data.StatusObj
import db.StatusesQueries

class StatusesDao(private val db: StatusesQueries) {

    fun insert(statusObj: StatusObj) {
        db.insert(
            statusObj.title,
            statusObj.color.toLong(),
            statusObj.note.toLong()
        )
    }

    fun update(statusObj: StatusObj) {
        db.update(
            statusObj.title,
            statusObj.color.toLong(),
            statusObj.note.toLong(),
            statusObj.id.toLong()
        )
    }

    fun delete(id: Int) {
        db.delete(id.toLong())
    }

    fun deleteManyByNote(note: Int) {
        db.deleteManyByNote(note.toLong())
    }

    fun getManyByNote(note: Int): List<StatusObj> {
        val list = mutableListOf<StatusObj>()
        val result = db.getManyByNote(note.toLong()).executeAsList()
        for (i in result) {
            list.add(
                StatusObj(
                    id = i.id.toInt(),
                    title = i.title ?: "",
                    color = i.color?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0
                )
            )
        }
        return list
    }
    fun getAll(): List<StatusObj> {
        val list = mutableListOf<StatusObj>()
        val result = db.getAll().executeAsList()
        for (i in result) {
            list.add(
                StatusObj(
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