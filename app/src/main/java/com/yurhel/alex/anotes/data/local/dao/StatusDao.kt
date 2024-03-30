package com.yurhel.alex.anotes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.yurhel.alex.anotes.data.local.obj.StatusObj

@Dao
interface StatusDao {
    // STATUSES
    @Upsert
    suspend fun upsert(obj: StatusObj)

    @Delete
    suspend fun delete(obj: StatusObj)

    @Query("DELETE FROM StatusObj")
    suspend fun deleteAll()

    @Query("DELETE FROM StatusObj WHERE note = :noteId")
    suspend fun deleteManyByNote(noteId: Int)

    @Query("SELECT * FROM StatusObj WHERE note = :noteId")
    fun getManyByNote(noteId: Int): List<StatusObj>

    @Query("SELECT * FROM StatusObj")
    fun getAll(): List<StatusObj>
}