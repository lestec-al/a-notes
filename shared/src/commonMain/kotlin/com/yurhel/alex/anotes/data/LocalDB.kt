package com.yurhel.alex.anotes.data

import app.cash.sqldelight.db.SqlDriver
import com.yurhel.alex.anotes.data.local_db_dao.BoardDao
import com.yurhel.alex.anotes.data.local_db_dao.NotesDao
import com.yurhel.alex.anotes.data.local_db_dao.StatusesDao
import com.yurhel.alex.anotes.data.local_db_dao.TasksDao
import com.yurhel.alex.anotes.data.local_db_dao.WidgetDao
import db.Database
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

class LocalDB private constructor(sqlDriver: SqlDriver) {

    companion object {
        @Volatile
        private var instance: LocalDB? = null

        fun getInstance(sqlDriver: SqlDriver): LocalDB {
            return instance ?: synchronized(this) {
                instance ?: LocalDB(sqlDriver).also { instance = it }
            }
        }
    }

    val driver: SqlDriver = sqlDriver

    val note = NotesDao(Database(driver).notesQueries)
    val status = StatusesDao(Database(driver).statusesQueries)
    val task = TasksDao(Database(driver).tasksQueries)
    val widget = WidgetDao(Database(driver).widgetsQueries)
    val board = BoardDao(Database(driver).drawingsQueries)

    fun exportDB(): JsonArray {
        try {
            val db = Database(driver)
            return buildJsonArray {
                for (i in db.notesQueries.getAll().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("id", i.id.toInt())
                            put("folder", i.folder?.toInt() ?: 0)
                            put("type", i.type ?: "")
                            put("text", i.text ?: "")
                            put("dateUpdate", i.dateUpdate ?: "")
                            put("dateCreate", i.dateCreate ?: "")
                        }
                    )
                }
                for (i in db.statusesQueries.getAll().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("id", i.id.toInt())
                            put("title", i.title ?: "")
                            put("color", i.color?.toInt() ?: 0)
                            put("note", i.note?.toInt() ?: 0)
                        }
                    )
                }
                for (i in db.tasksQueries.getAll().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("id", i.id.toInt())
                            put("position", i.position?.toInt() ?: 0)
                            put("description", i.description ?: "")
                            put("status", i.status?.toInt() ?: 0)
                            put("note", i.note?.toInt() ?: 0)
                            put("dateUpdate", i.dateUpdate ?: "")
                            put("dateCreate", i.dateCreate ?: "")
                            put("dateUpdateStatus", i.dateUpdateStatus ?: "")
                        }
                    )
                }
                for (i in db.drawingsQueries.getAllDraws().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("id", i.id.toInt())
                            put("noteId", i.noteId!!.toInt())
                            put("startX", i.startX ?: "")
                            put("startY", i.startY ?: "")
                            put("endX", i.endX ?: "")
                            put("endY", i.endY ?: "")
                            put("color", i.color ?: "")
                            put("strokeWidth", i.strokeWidth ?: "")
                        }
                    )
                }
                for (i in db.drawingsQueries.getAllImages().executeAsList()) {
                    add(
                        buildJsonObject {
                            put("noteId", i.noteId!!.toInt())
                            put("base64Str", i.base64Str!!)
                        }
                    )
                }
            }
        }  catch (e: Exception) {
            e.printStackTrace()
            return JsonArray(emptyList())
        }
    }

    fun importDB(data: String): Boolean {
        try {
            val db = Database(driver)
            db.notesQueries.deleteAll()
            db.statusesQueries.deleteAll()
            db.tasksQueries.deleteAll()
            db.drawingsQueries.deleteAllDraw()
            db.drawingsQueries.deleteAllImg()
            // Loop data
            val jsonData = Json.decodeFromString<JsonArray>(data)
            for (i in jsonData) {
                val obj = i.jsonObject
                // Check object type
                val draw = try {
                    obj["startX"]?.jsonPrimitive?.content
                } catch (_: Exception) {
                    null
                }
                val image = try {
                    obj["base64Str"]?.jsonPrimitive?.content
                } catch (_: Exception) {
                    null
                }
                val status = try {
                    obj["title"]?.jsonPrimitive?.content
                } catch (_: Exception) {
                    null
                }
                val task = try {
                    obj["description"]?.jsonPrimitive?.content
                } catch (_: Exception) {
                    null
                }
                // Insert obj to DB
                when {
                    image != null -> {
                        db.drawingsQueries.insertImage(
                            noteId = obj["noteId"]?.jsonPrimitive?.long,
                            base64Str = obj["base64Str"]?.jsonPrimitive?.content
                        )
                    }
                    draw != null -> {
                        db.drawingsQueries.insertDrawWithId(
                            id = obj["id"]?.jsonPrimitive?.long,
                            noteId = obj["noteId"]?.jsonPrimitive?.long,
                            startX = obj["startX"]?.jsonPrimitive?.content,
                            startY = obj["startY"]?.jsonPrimitive?.content,
                            endX = obj["endX"]?.jsonPrimitive?.content,
                            endY = obj["endY"]?.jsonPrimitive?.content,
                            color = obj["color"]?.jsonPrimitive?.content,
                            strokeWidth = obj["strokeWidth"]?.jsonPrimitive?.content
                        )
                    }
                    status != null -> {
                        db.statusesQueries.insertWithId(
                            id = obj["id"]?.jsonPrimitive?.long,
                            title = obj["title"]?.jsonPrimitive?.content,
                            color = obj["color"]?.jsonPrimitive?.long,
                            note = obj["note"]?.jsonPrimitive?.long
                        )
                    }
                    task != null -> {
                        db.tasksQueries.insertWithId(
                            id = obj["id"]?.jsonPrimitive?.long,
                            position = obj["position"]?.jsonPrimitive?.long,
                            description = obj["description"]?.jsonPrimitive?.content,
                            status = obj["status"]?.jsonPrimitive?.long,
                            note = obj["note"]?.jsonPrimitive?.long,
                            dateCreate = obj["dateCreate"]?.jsonPrimitive?.content,
                            dateUpdate = obj["dateUpdate"]?.jsonPrimitive?.content,
                            dateUpdateStatus = obj["dateUpdateStatus"]?.jsonPrimitive?.content
                        )
                    }
                    else -> {
                        db.notesQueries.insertWithId(
                            id = obj["id"]?.jsonPrimitive?.long,
                            folder = try {
                                obj["folder"]?.jsonPrimitive?.long ?: 0
                            } catch (_: Exception) { 0 },
                            type = obj["type"]?.jsonPrimitive?.contentOrNull ?: "",
                            text = obj["text"]?.jsonPrimitive?.content,
                            dateUpdate = obj["dateUpdate"]?.jsonPrimitive?.content,
                            dateCreate = obj["dateCreate"]?.jsonPrimitive?.content
                        )
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}