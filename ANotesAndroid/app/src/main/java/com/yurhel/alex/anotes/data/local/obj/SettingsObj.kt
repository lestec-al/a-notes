package com.yurhel.alex.anotes.data.local.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SettingsObj(
    @PrimaryKey(autoGenerate = false) val id: Int = 1,
    @ColumnInfo(name = "dataReceivedDate") val dataReceivedDate: Long? = null,
    @ColumnInfo(name = "isNotesEdited") val isNotesEdited: Boolean = false,
    @ColumnInfo(name = "viewMode") var viewMode: String = "col"
)