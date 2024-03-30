package com.yurhel.alex.anotes.data.local.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StatusObj(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "color") val color: Int,
    @ColumnInfo(name = "note") val note: Int
)