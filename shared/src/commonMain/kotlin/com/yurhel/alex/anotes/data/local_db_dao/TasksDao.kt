package com.yurhel.alex.anotes.data.local_db_dao

import com.yurhel.alex.anotes.data.Task
import db.TasksQueries

class TasksDao(private val db: TasksQueries) {

    fun insert(task: Task) {
        db.insert(
            task.position.toLong(),
            task.description,
            task.status.toLong(),
            task.note.toLong(),
            task.dateCreate.toString(),
            task.dateUpdate.toString(),
            task.dateUpdateStatus.toString()
        )
    }

    fun update(task: Task) {
        db.update(
            task.position.toLong(),
            task.description,
            task.status.toLong(),
            task.dateUpdate.toString(),
            task.dateUpdateStatus.toString(),
            task.id.toLong()
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

    fun getManyByNoteAndStatus(note: Int, status: Int): List<Task> {
        val list = mutableListOf<Task>()
        val result = db.getManyByNoteAndStatus(
            note.toLong(),
            status.toLong()
        ).executeAsList()
        for (i in result) {
            list.add(
                Task(
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

    fun getManyByNote(note: Int): List<Task> {
        val list = mutableListOf<Task>()
        val result = db.getManyByNote(note.toLong()).executeAsList()
        for (i in result) {
            list.add(
                Task(
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

    fun getNextPosition(note: Int): Int {
        val result = try {
            db.getNextPosition(note.toLong()).executeAsOne().position?.toInt()
        } catch (_: Exception) {
            null
        }
        return if (result != null) result + 1 else 0
    }

    fun getAll(): List<Task> {
        val list = mutableListOf<Task>()
        val result = db.getAll().executeAsList()
        for (i in result) {
            list.add(
                Task(
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