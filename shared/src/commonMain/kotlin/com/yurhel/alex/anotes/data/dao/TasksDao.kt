package com.yurhel.alex.anotes.data.dao

import com.yurhel.alex.anotes.data.Tasks
import db.TasksQueries

class TasksDao(private val db: TasksQueries) {

    fun insert(tasks: Tasks) {
        db.insert(
            tasks.position.toLong(),
            tasks.description,
            tasks.status.toLong(),
            tasks.note.toLong(),
            tasks.dateCreate.toString(),
            tasks.dateUpdate.toString(),
            tasks.dateUpdateStatus.toString()
        )
    }

    fun update(tasks: Tasks) {
        db.update(
            tasks.position.toLong(),
            tasks.description,
            tasks.status.toLong(),
            tasks.dateUpdate.toString(),
            tasks.dateUpdateStatus.toString(),
            tasks.id.toLong()
        )
    }

    fun delete(id: Int) {
        db.delete(id.toLong())
    }

    fun deleteManyByStatus(status: Int) {
        db.deleteManyByStatus(status.toLong())
    }

    fun deleteManyByNote(note: Int) {
        db.deleteManyByNote(note.toLong())
    }

    fun getManyByNoteAndStatus(note: Int, status: Int): List<Tasks> {
        val list = mutableListOf<Tasks>()
        val result = db.getManyByNoteAndStatus(
            note.toLong(),
            status.toLong()
        ).executeAsList()
        for (i in result) {
            list.add(
                Tasks(
                    id = i.id.toInt(),
                    position = i.position?.toInt() ?: 0,
                    description = i.description ?: "",
                    status = i.status?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0,
                    dateCreate = i.dateCreate?.toLong() ?: 0,
                    dateUpdate = i.dateUpdate?.toLong() ?: 0,
                    dateUpdateStatus = i.dateUpdateStatus?.toLong() ?: 0
                )
            )
        }
        return list
    }

    fun getManyByNote(note: Int): List<Tasks> {
        val list = mutableListOf<Tasks>()
        val result = db.getManyByNote(note.toLong()).executeAsList()
        for (i in result) {
            list.add(
                Tasks(
                    id = i.id.toInt(),
                    position = i.position?.toInt() ?: 0,
                    description = i.description ?: "",
                    status = i.status?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0,
                    dateCreate = i.dateCreate?.toLong() ?: 0,
                    dateUpdate = i.dateUpdate?.toLong() ?: 0,
                    dateUpdateStatus = i.dateUpdateStatus?.toLong() ?: 0
                )
            )
        }
        return list
    }

    fun getHowManyTasksNoteHas(note: Int): Int {
        return db.getManyByNoteCount(note.toLong()).executeAsOne().toInt()
    }

    fun getAll(): List<Tasks> {
        val list = mutableListOf<Tasks>()
        val result = db.getAll().executeAsList()
        for (i in result) {
            list.add(
                Tasks(
                    id = i.id.toInt(),
                    position = i.position?.toInt() ?: 0,
                    description = i.description ?: "",
                    status = i.status?.toInt() ?: 0,
                    note = i.note?.toInt() ?: 0,
                    dateCreate = i.dateCreate?.toLong() ?: 0,
                    dateUpdate = i.dateUpdate?.toLong() ?: 0,
                    dateUpdateStatus = i.dateUpdateStatus?.toLong() ?: 0
                )
            )
        }
        return list
    }
}