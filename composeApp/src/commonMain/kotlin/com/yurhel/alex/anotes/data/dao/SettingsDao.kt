package com.yurhel.alex.anotes.data.dao

import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.data.SettingsObj
import com.yurhel.alex.anotes.data.WinScreenSettingsObj
import db.SettingsQueries

class SettingsDao(private val db: SettingsQueries) {

    fun updateReceived(dataReceivedDate: Long?) {
        db.updateReceived("${dataReceivedDate ?: ""}")
    }

    fun updateEdit(isNotesEdited: Boolean) {
        db.updateEdit(if (isNotesEdited) 1 else 0)
    }

    fun updateViewMode(viewMode: String) {
        db.updateViewMode(viewMode)
    }

    fun getSettings(): SettingsObj {
        val result = db.selectAll().executeAsList()[0]

        var dataReceivedDate: Long? = null
        val x = result.dataReceivedDate
        if (x != null && x != "") dataReceivedDate = x.toLong()

        return SettingsObj(
            dataReceivedDate,
            result.isNotesEdited?.toInt() == 1,
            result.viewMode ?: "col"
        )
    }

    fun getDataShowing() = db.getDataShowing().executeAsOne()
        .dataShowing ?: "all"

    fun getSortType() = db.getSortType().executeAsOne()
        .sortType ?: "dateUpdate"

    fun getSortArrow() = db.getSortArrow().executeAsOne()
        .sortArrow ?: "ascending"

    fun updateDataShowing(value: String) {
        db.updateDataShowing(value)
    }

    fun updateSortType(value: String) {
        db.updateSortType(value)
    }

    fun updateSortArrow(value: String) {
        db.updateSortArrow(value)
    }

    fun getScreen(): WinScreenSettingsObj {
        val result = db.getScreen().executeAsOne()
        return WinScreenSettingsObj(
            width = result.screenWidth?.toInt()?.dp ?: 600.dp,
            height = result.screenHeight?.toInt()?.dp ?: 600.dp,
            posX = result.screenPosX?.toInt()?.dp ?: 20.dp,
            posY = result.screenPosY?.toInt()?.dp ?: 20.dp
        )
    }

    fun setScreen(
        width: Long,
        height: Long,
        posX: Long,
        posY: Long
    ) {
        db.setScreen(
            screenWidth = width,
            screenHeight = height,
            screenPosX = posX,
            screenPosY = posY
        )
    }
}