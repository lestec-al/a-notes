package com.yurhel.alex.anotes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.yurhel.alex.anotes.data.local.obj.NoteObj

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsert(note: NoteObj)

    @Delete
    suspend fun delete(note: NoteObj)

    @Query("DELETE FROM NoteObj")
    suspend fun deleteAll()


    @Query("SELECT * FROM NoteObj")
    fun getAll(): List<NoteObj>

    @Query("SELECT * FROM NoteObj WHERE text LIKE '%' || :query || '%'")
    fun getByQuery(query: String): List<NoteObj>


    // Unused
    @Query("SELECT * FROM NoteObj WHERE id = :id")
    fun getById(id: Int): NoteObj?

    @Query("SELECT * FROM NoteObj WHERE dateCreate = :created")
    fun getByCreated(created: String): NoteObj?

    @Query("SELECT * FROM NoteObj ORDER BY id DESC LIMIT 1")
    fun getLast(): NoteObj?
}