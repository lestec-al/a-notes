package com.yurhel.alex.anotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yurhel.alex.anotes.data.local.dao.NoteDao
import com.yurhel.alex.anotes.data.local.dao.SettingsDao
import com.yurhel.alex.anotes.data.local.dao.StatusDao
import com.yurhel.alex.anotes.data.local.dao.TaskDao
import com.yurhel.alex.anotes.data.local.dao.WidgetDao
import com.yurhel.alex.anotes.data.local.obj.NoteObj
import com.yurhel.alex.anotes.data.local.obj.SettingsObj
import com.yurhel.alex.anotes.data.local.obj.StatusObj
import com.yurhel.alex.anotes.data.local.obj.TasksObj
import com.yurhel.alex.anotes.data.local.obj.WidgetObj

@Database(
    entities = [
        NoteObj::class,
        SettingsObj::class,
        WidgetObj::class,
        StatusObj::class,
        TasksObj::class
    ],
    version = 1
)
abstract class DB : RoomDatabase() {
    abstract val note: NoteDao
    abstract val setting: SettingsDao
    abstract val widget: WidgetDao
    abstract val status: StatusDao
    abstract val task: TaskDao
}