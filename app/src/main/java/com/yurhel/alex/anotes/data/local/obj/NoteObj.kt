package com.yurhel.alex.anotes.data.local.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteObj(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "withTasks") val withTasks: Boolean = false,
    @ColumnInfo(name = "text") val text: String,
    // Date
    @ColumnInfo(name = "dateUpdate") val dateUpdate: Long,
    @ColumnInfo(name = "dateCreate") val dateCreate: Long
)