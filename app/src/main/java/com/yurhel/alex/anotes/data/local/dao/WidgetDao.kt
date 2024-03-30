package com.yurhel.alex.anotes.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.yurhel.alex.anotes.data.local.obj.WidgetObj

@Dao
interface WidgetDao {
    @Insert
    suspend fun insert(note: WidgetObj)


    @Query("DELETE FROM WidgetObj WHERE widgetId = :widgetId")
    suspend fun deleteById(widgetId: Int)

    @Query("DELETE FROM WidgetObj WHERE noteCreated = :noteCreated")
    suspend fun deleteByCreated(noteCreated: String)


    @Query("SELECT * FROM WidgetObj WHERE widgetId = :widgetId")
    fun getById(widgetId: Int): WidgetObj?

    @Query("SELECT * FROM WidgetObj WHERE noteCreated = :noteCreated")
    fun getByCreated(noteCreated: String): WidgetObj?
}