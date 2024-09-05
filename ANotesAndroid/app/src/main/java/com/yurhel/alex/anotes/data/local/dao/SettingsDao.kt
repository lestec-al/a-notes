package com.yurhel.alex.anotes.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.yurhel.alex.anotes.data.local.obj.SettingsObj

@Dao
interface SettingsDao {
    @Upsert
    suspend fun upsert(settings: SettingsObj)

    @Query("SELECT * FROM SettingsObj WHERE id = 1")
    fun getS(): SettingsObj?
}