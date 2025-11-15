package com.yurhel.alex.anotes.ui.feature_swipes.utils

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

const val swipesCode = "[{\""

fun importSwipesFromText(
    it: String,
    block: (String, Color, String, Color) -> Unit
): List<SwipeTextObj> {
    val tempData = mutableListOf<SwipeTextObj>()
    val strings = it.split(swipesCode)
    val jsonData = it.replace(strings[0], "")
    if (jsonData != "") {
        Json.decodeFromString<JsonArray>(jsonData).forEach { jsonEl ->
            val obj = jsonEl.jsonObject
            val leftTextT = try { obj["leftText"]?.jsonPrimitive?.content } catch (_: Exception) { null }
            if (leftTextT != null) {
                block(
                    leftTextT,
                    obj["leftColor"]?.jsonPrimitive?.int?.let { it1 -> Color(it1) } ?: Color.Red,
                    obj["rightText"]?.jsonPrimitive?.content ?: "",
                    obj["rightColor"]?.jsonPrimitive?.int?.let { it1 -> Color(it1) } ?: Color.Green
                )
            } else {
                val id = obj["id"]?.jsonPrimitive?.int
                val pos = obj["pos"]?.jsonPrimitive?.content
                val text = obj["text"]?.jsonPrimitive?.content
                if (id != null && pos != null && text != null) {
                    tempData.add(
                        SwipeTextObj(
                            id = id,
                            pos = when (pos) {
                                SwipeTextPos.Left.name -> SwipeTextPos.Left
                                else -> SwipeTextPos.Right
                            },
                            text = text
                        )
                    )
                }
            }
        }
    }
    return tempData.toList()
}