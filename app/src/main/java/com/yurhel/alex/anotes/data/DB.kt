package com.yurhel.alex.anotes.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

class DB(context: Context) : SQLiteOpenHelper(context, "notes.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        if (db != null) {
            // Notes
            db.execSQL("""CREATE TABLE notesTable (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                text TEXT,
                dateUpdate TEXT,
                dateCreate TEXT)""".trimIndent()
            )
            // Settings
            db.execSQL("""CREATE TABLE settingsTable (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dataReceivedDate TEXT,
                isNotesEdited INT,
                viewMode TEXT)""".trimIndent()
            )
            val cv = ContentValues()
            cv.put("viewMode", "linear")
            cv.put("dataReceivedDate", "")
            cv.put("isNotesEdited", 0)
            db.insert("settingsTable", null, cv)

            // Active widgets
            db.execSQL("""CREATE TABLE widgetsTable (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                widgetId INT,
                noteCreated TEXT)""".trimIndent()
            )
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    // NOTES
    fun createNote(text: String, date: String) {
        // Data
        val cv = ContentValues()
        cv.put("text", text)
        cv.put("dateUpdate", date)
        cv.put("dateCreate", date)
        // DB
        val db = this.writableDatabase
        db.insert("notesTable", null, cv)
        db.close()
    }

    fun updateNote(id: Int, text: String, date: String) {
        // Data
        val cv = ContentValues()
        cv.put("text", text)
        cv.put("dateUpdate", date)
        // DB
        val db = this.writableDatabase
        db.update("notesTable", cv, "id = $id", null)
        db.close()
    }

    fun deleteNote(id: Int) {
        val db = this.writableDatabase
        db.delete("notesTable", "id = $id", null)
        db.close()
    }

    fun getNote(id: Int = -1, created: String = ""): NoteObj? {
        val db = this.readableDatabase
        val cur = db.rawQuery(
            if (id != -1) "SELECT * FROM notesTable WHERE id = $id" else "SELECT * FROM notesTable WHERE dateCreate = $created",
            null
        )
        return try {
            if (cur.moveToFirst()) {
                NoteObj(
                    cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3)
                )
            } else {
                null
            }
        } finally {
            cur.close()
            db.close()
        }
    }

    fun getLastNote(): NoteObj? {
        val db = this.readableDatabase
        val cur = db.rawQuery("SELECT * FROM notesTable", null)
        return try {
            if (cur.moveToLast()) {
                NoteObj(
                    cur.getInt(0),
                    cur.getString(1),
                    cur.getString(2),
                    cur.getString(3)
                )
            } else {
                null
            }
        } finally {
            cur.close()
            db.close()
        }
    }

    fun getNotes(query: String = ""): List<NoteObj> {
        val list = mutableListOf<NoteObj>()
        // DB
        val db = this.readableDatabase
        val cur = db.rawQuery("SELECT * FROM notesTable", null)
        if (cur.moveToFirst()) {
            do {
                if (query == "" || query.lowercase() in cur.getString(1).lowercase()) {
                    list.add(
                        NoteObj(
                        cur.getInt(0),
                        cur.getString(1),
                        cur.getString(2),
                        cur.getString(3)
                    )
                    )
                }
            } while (cur.moveToNext())
        }
        cur.close()
        db.close()
        //
        return list
    }

    // SETTINGS
    /**
     * @param viewMode 'grid' or 'linear'
     */
    fun updateViewMode(viewMode: String) {
        val db = this.writableDatabase
        db.execSQL("UPDATE settingsTable SET viewMode = '$viewMode' WHERE id = 1")
        db.close()
    }

    fun updateReceived(dataReceivedDate: Long?) {
        val db = this.writableDatabase
        db.execSQL("UPDATE settingsTable SET dataReceivedDate = '${dataReceivedDate ?: ""}' WHERE id = 1")
        db.close()
    }

    fun updateEdit(isNotesEdited: Boolean) {
        val db = this.writableDatabase
        db.execSQL("UPDATE settingsTable SET isNotesEdited = ${if (isNotesEdited) 1 else 0} WHERE id = 1")
        db.close()
    }

    fun getSettings(): SettingsObj {
        val db = this.readableDatabase
        val cur = db.rawQuery("SELECT * FROM settingsTable WHERE id = 1", null)
        return if (cur.moveToFirst()) {
            var dataReceivedDate: Long? = null
            val x = cur.getString(1)
            if (x != "") dataReceivedDate = x.toLong()

            val settings = SettingsObj(
                dataReceivedDate = dataReceivedDate,
                isNotesEdited = cur.getInt(2) == 1,
                viewMode = cur.getString(3)
            )
            cur.close()
            db.close()
            settings
        } else {
            cur.close()
            db.close()
            SettingsObj(dataReceivedDate = null, isNotesEdited = false, viewMode = "linear")
        }
    }

    // WIDGETS
    fun createWidgetEntry(widgetId: Int, noteCreated: String) {
        // Data
        val cv = ContentValues()
        cv.put("widgetId", widgetId)
        cv.put("noteCreated", noteCreated)
        // DB
        val db = this.writableDatabase
        db.insert("widgetsTable", null, cv)
        db.close()
    }

    /**
     * Delete widget entry (not widget itself) from DB if it exists.
     * @param widgetId required if another param not provided
     * @param noteCreated required if another param not provided
     */
    fun deleteWidgetEntry(widgetId: Int = -1, noteCreated: String = "") {
        val db = this.writableDatabase
        db.delete("widgetsTable", if (noteCreated != "") "noteCreated = $noteCreated" else "widgetId = $widgetId", null)
        db.close()
    }

    /**
     * Get IDs from widget entry.
     * @param widgetId required if another param not provided
     * @param noteCreated required if another param not provided
     * @return If you specify noteCreated, widgetID will be returned and vice versa. If none are specified null is returned.
    */
    fun getWidgetIds(widgetId: Int = -1, noteCreated: String = ""): String? {
        // DB
        val db = this.readableDatabase
        val cur = db.rawQuery(
            "SELECT * FROM widgetsTable WHERE ${if (noteCreated != "") "noteCreated = $noteCreated" else "widgetId = $widgetId"}", null
        )

        return try {
            if (cur.moveToFirst())
                // 1 - widgetId, 2 - noteCreated
                if (noteCreated != "") cur.getInt(1).toString() else cur.getString(2)
            else
                null
        } finally {
            cur.close()
            db.close()
        }
    }

    // EXPORT / IMPORT
    fun exportDB(): JSONArray {
        try {
            val jArray = JSONArray()
            val dbImport = writableDatabase
            val c = dbImport.rawQuery("SELECT * FROM notesTable", null)
            if (c.moveToFirst()) {
                do {
                    val jObj = JSONObject()
                    jObj.put("id", c.getInt(0))
                    jObj.put("text", c.getString(1))
                    jObj.put("dateUpdate", c.getString(2))
                    jObj.put("dateCreate", c.getString(3))
                    jArray.put(jObj)
                } while (c.moveToNext())
            }
            c.close()
            dbImport.close()
            return jArray
        }  catch (e: Exception) {
            e.printStackTrace()
            return JSONArray()
        }
    }

    fun importDB(data: String): Boolean {
        //val oldData = exportDB()
        try {
            cleanDatabase()
            // Loop data
            val jsonData = JSONArray(data)
            for (i in 0..<jsonData.length()) {
                val obj = jsonData.getJSONObject(i)
                // Insert obj to DB
                val cvDB = ContentValues()
                //cvDB.put("id", obj.getInt("id"))
                cvDB.put("text", obj.getString("text"))
                cvDB.put("dateUpdate", obj.getString("dateUpdate"))
                cvDB.put("dateCreate", obj.getString("dateCreate"))
                val db = writableDatabase
                db.insert("notesTable", null, cvDB)
                db.close()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            //cleanDatabase()
            //importDB(oldData.toString())
            return false
        }
    }

    private fun cleanDatabase() {
        val db = this.readableDatabase
        db.execSQL("DELETE FROM notesTable")
        db.close()
    }
}