package com.yurhel.alex.anotes.data.dao

import com.yurhel.alex.anotes.data.TasksObj
import db.TasksQueries

class TasksDao(private val db: TasksQueries) {

    fun insert(tasksObj: TasksObj) {
        db.insert(
            tasksObj.position.toLong(),
            tasksObj.description,
            tasksObj.status.toLong(),
            tasksObj.note.toLong(),
            tasksObj.dateCreate.toString(),
            tasksObj.dateUpdate.toString(),
            tasksObj.dateUpdateStatus.toString()
        )
    }

    fun update(tasksObj: TasksObj) {
        db.update(
            tasksObj.position.toLong(),
            tasksObj.description,
            tasksObj.status.toLong(),
            tasksObj.dateUpdate.toString(),
            tasksObj.dateUpdateStatus.toString(),
            tasksObj.id.toLong()
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

    fun getManyByNoteAndStatus(note: Int, status: Int): List<TasksObj> {
        val list = mutableListOf<TasksObj>()
        val result = db.getManyByNoteAndStatus(
            note.toLong(),
            status.toLong()
        ).executeAsList()
        for (i in result) {
            list.add(
                TasksObj(
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

    fun getManyByNote(note: Int): List<TasksObj> {
        val list = mutableListOf<TasksObj>()
        val result = db.getManyByNote(note.toLong()).executeAsList()
        for (i in result) {
            list.add(
                TasksObj(
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

    fun getAll(): List<TasksObj> {
        val list = mutableListOf<TasksObj>()
        val result = db.getAll().executeAsList()
        for (i in result) {
            list.add(
                TasksObj(
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