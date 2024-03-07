package data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import db.Database
import org.json.JSONArray
import org.json.JSONObject

class DB {

    // INIT DB
    private fun tryInitDB(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:database.db")
        try {
            Database.Schema.create(driver)
        } catch (_: Exception) {}
        return driver
    }
    private val driver = tryInitDB()

    // NOTES DAO
    fun createNote(text: String, date: String) {
        Database(driver).notesQueries.insert(text = text, dateUpdate = date, dateCreate = date)
    }

    fun updateNote(id: Int, text: String, date: String) {
        Database(driver).notesQueries.update(text, date, id.toLong())
    }

    fun deleteNote(id: Int) {
        Database(driver).notesQueries.delete(id.toLong())
    }

//    fun getNote(id: Int): NoteObj? {
//        val result = Database(driver).notesQueries.selectOne(id.toLong()).executeAsOneOrNull()
//        return if (result != null) {
//            NoteObj(
//                result.id.toInt(),
//                result.text ?: "",
//                result.dateUpdate ?: "",
//                result.dateCreate ?: ""
//            )
//        } else {
//            null
//        }
//    }

    fun getLastNote(): NoteObj? {
        val notes = getNotes()
        return if (notes.isNotEmpty()) notes.last() else null
    }

    fun getNotes(query: String = ""): List<NoteObj> {
        val list = mutableListOf<NoteObj>()
        val result = Database(driver).notesQueries.selectAll().executeAsList()
        for (i in result) {
            if (query == "" || i.text?.lowercase()?.contains(query.lowercase()) == true ) {
                list.add(
                    NoteObj(
                        i.id.toInt(),
                        i.text ?: "",
                        i.dateUpdate ?: "",
                        i.dateCreate ?: ""
                    )
                )
            }
        }
        return list
    }

    // SETTINGS DAO
    fun updateReceived(dataReceivedDate: Long?) {
        Database(driver).settingsQueries.updateReceived("${dataReceivedDate ?: ""}")
    }

    fun updateEdit(isNotesEdited: Boolean) {
        Database(driver).settingsQueries.updateEdit(if (isNotesEdited) 1 else 0)
    }

    fun getSettings(): SettingsObj {
        val result = Database(driver).settingsQueries.selectAll().executeAsList()[0]

        var dataReceivedDate: Long? = null
        val x = result.dataReceivedDate
        if (x != null && x != "") dataReceivedDate = x.toLong()

        return SettingsObj(
            dataReceivedDate,
            result.isNotesEdited?.toInt() == 1
        )
    }

    // EXPORT / IMPORT
    fun exportDB(): JSONArray {
        try {
            val jArray = JSONArray()
            for (i in Database(driver).notesQueries.selectAll().executeAsList()) {
                val jObj = JSONObject()
                jObj.put("id", i.id.toInt())
                jObj.put("text", i.text ?: "")
                jObj.put("dateUpdate", i.dateUpdate ?: "")
                jObj.put("dateCreate", i.dateCreate ?: "")
                jArray.put(jObj)
            }
            return jArray
        }  catch (e: Exception) {
            e.printStackTrace()
            return JSONArray()
        }
    }

    fun importDB(data: String): Boolean {
        //val oldData = exportDB()
        try {
            val db = Database(driver)
            db.notesQueries.deleteAll()
            // Loop data
            val jsonData = JSONArray(data)
            for (i in 0..<jsonData.length()) {
                val obj = jsonData.getJSONObject(i)
                db.notesQueries.insert(
                    text = obj.getString("text"),
                    dateUpdate = obj.getString("dateUpdate"),
                    dateCreate = obj.getString("dateCreate")
                )
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            //importDB(oldData.toString())
            return false
        }
    }
}