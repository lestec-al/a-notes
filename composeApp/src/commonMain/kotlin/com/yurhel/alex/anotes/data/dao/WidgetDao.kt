package com.yurhel.alex.anotes.data.dao

import com.yurhel.alex.anotes.data.WidgetObj
import db.WidgetsQueries

class WidgetDao(private val db: WidgetsQueries) {

    fun insert(w: WidgetObj) {
        db.insert(w.widgetId.toLong(), w.noteCreated)
    }

    fun deleteById(widgetId: Int) {
        db.deleteById(widgetId.toLong())
    }

    fun getByCreated(noteCreated: String): WidgetObj? {
        val res = db.getByCreated(noteCreated).executeAsList()
        var widget: WidgetObj? = null
        for (i in res) {
            widget = WidgetObj(
                id = i.id.toInt(),
                widgetId = i.widgetId?.toInt() ?: 0,
                noteCreated = i.noteCreated ?: ""
            )
        }
        return widget
    }
}