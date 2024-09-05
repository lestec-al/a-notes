package com.yurhel.alex.anotes.data.local.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TasksObj(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "position") var position: Int = -1, // ???
    @ColumnInfo(name = "description") val description: String,
    // Foreign keys
    @ColumnInfo(name = "status") val status: Int,
    @ColumnInfo(name = "note") val note: Int,
    // Date
    @ColumnInfo(name = "dateCreate") val dateCreate: Long,
    @ColumnInfo(name = "dateUpdate") val dateUpdate: Long,
    @ColumnInfo(name = "dateUpdateStatus") val dateUpdateStatus: Long
)