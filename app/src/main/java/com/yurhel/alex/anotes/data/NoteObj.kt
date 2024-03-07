package com.yurhel.alex.anotes.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteObj(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "dateUpdate") val dateUpdate: String,
    @ColumnInfo(name = "dateCreate") val dateCreate: String
)