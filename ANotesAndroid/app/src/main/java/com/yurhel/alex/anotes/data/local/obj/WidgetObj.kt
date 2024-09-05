package com.yurhel.alex.anotes.data.local.obj

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WidgetObj(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "widgetId") val widgetId: Int,
    @ColumnInfo(name = "noteCreated") val noteCreated: String
)