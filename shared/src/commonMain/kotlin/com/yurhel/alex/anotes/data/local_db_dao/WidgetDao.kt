package com.yurhel.alex.anotes.data.local_db_dao

import com.yurhel.alex.anotes.data.Widget
import db.WidgetsQueries

class WidgetDao(private val db: WidgetsQueries) {

    fun insert(w: Widget) {
        db.insert(w.widgetId.toLong(), w.noteCreated)
    }

    fun deleteById(widgetId: Int) {
        db.deleteById(widgetId.toLong())
    }

    fun getByCreated(noteCreated: String): Widget? {
        val res = db.getByCreated(noteCreated).executeAsList()
        var widget: Widget? = null
        for (i in res) {
            widget = Widget(
                id = i.id.toInt(),
                widgetId = i.widgetId?.toInt() ?: 0,
                noteCreated = i.noteCreated ?: ""
            )
        }
        return widget
    }
}