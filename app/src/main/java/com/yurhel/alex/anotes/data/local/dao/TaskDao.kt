package com.yurhel.alex.anotes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.yurhel.alex.anotes.data.local.obj.TasksObj

@Dao
interface TaskDao {
    @Upsert
    suspend fun upsert(obj: TasksObj)


    @Delete
    suspend fun delete(obj: TasksObj)

    @Query("DELETE FROM TasksObj")
    suspend fun deleteAll()

    @Query("DELETE FROM TasksObj WHERE status = :statusId")
    suspend fun deleteManyByStatus(statusId: Int)

    @Query("DELETE FROM TasksObj WHERE note = :noteId")
    suspend fun deleteManyByNote(noteId: Int)


    @Query("SELECT * FROM TasksObj WHERE note = :noteId AND status = :statusId ORDER BY position ASC") //DESC
    fun getManyByNoteAndStatus(noteId: Int, statusId: Int): List<TasksObj>

    @Query("SELECT * FROM TasksObj WHERE note = :noteId ORDER BY position ASC") //DESC
    fun getManyByNote(noteId: Int): List<TasksObj>

    @Query("SELECT * FROM TasksObj")
    fun getAll(): List<TasksObj>


    @Query("SELECT * FROM TasksObj ORDER BY id DESC LIMIT 1")
    fun getLast(): TasksObj

    @Query("SELECT * FROM TasksObj WHERE position = :pos")
    fun getByPosition(pos: Int): TasksObj?
}