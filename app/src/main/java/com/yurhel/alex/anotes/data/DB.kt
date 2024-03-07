package com.yurhel.alex.anotes.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteObj::class, SettingsObj::class, WidgetObj::class],
    version = 1
)
abstract class DB : RoomDatabase() {
    abstract val note: NoteDao
    abstract val settings: SettingsDao
    abstract val widgets: WidgetDao
}