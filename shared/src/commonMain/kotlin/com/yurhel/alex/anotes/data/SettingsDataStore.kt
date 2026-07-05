package com.yurhel.alex.anotes.data

import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class SettingsDataStore private constructor (private val dataStore: DataStore<Preferences>) {

    companion object {
        @Volatile
        private var instance: SettingsDataStore? = null

        fun getInstance(block: () -> DataStore<Preferences>): SettingsDataStore = instance ?: synchronized(this) {
            instance ?: SettingsDataStore(block()).also { instance = it }
        }
    }

    private suspend fun <T> updatePref(key: Preferences.Key<T>, value: T) {
        dataStore.updateData {
            it.toMutablePreferences().also { preferences -> preferences[key] = value }
        }
    }

    
    private val keyDarkTheme = booleanPreferencesKey("darkTheme")
    suspend fun getDarkTheme() = dataStore.data.first()[keyDarkTheme]
    suspend fun setDarkTheme(it: Boolean?) {
        if (it != null) updatePref(keyDarkTheme, it) else dataStore.edit { pref ->
            pref.remove(keyDarkTheme)
        }
    }
    

    private val keyDataReceivedDate = longPreferencesKey("dataReceivedDate")
    suspend fun getDataReceivedDate(): Long? {
        val it = dataStore.data.first()[keyDataReceivedDate]
        return if (it == 0L) null else it
    }
    suspend fun setDataReceivedDate(it: Long?) = updatePref(keyDataReceivedDate, it ?: 0)


    private val keyIsNotesEdited = booleanPreferencesKey("isNotesEdited")
    suspend fun getIsNotesEdited() = dataStore.data.first()[keyIsNotesEdited] == true
    suspend fun setIsNotesEdited(it: Boolean) = updatePref(keyIsNotesEdited, it)


    private val keyViewMode = stringPreferencesKey("viewMode")
    suspend fun getViewMode() = dataStore.data.first()[keyViewMode] ?: "col"
    suspend fun setViewMode(it: String) = updatePref(keyViewMode, it)


    private val keyDataShowing = stringPreferencesKey("dataShowing")
    suspend fun getDataShowing() = dataStore.data.first()[keyDataShowing] ?: "0"
    suspend fun setDataShowing(it: String) = updatePref(keyDataShowing, it)


    private val keySortType = stringPreferencesKey("sortType")
    suspend fun getSortType() = dataStore.data.first()[keySortType] ?: "dateUpdate"
    suspend fun setSortType(it: String) = updatePref(keySortType, it)


    private val keySortArrow = stringPreferencesKey("sortArrow")
    suspend fun getSortArrow() = dataStore.data.first()[keySortArrow] ?: "ascending"
    suspend fun setSortArrow(it: String) = updatePref(keySortArrow, it)


    private val keyScreenWidth = longPreferencesKey("screenWidth")
    private val keyScreenHeight = longPreferencesKey("screenHeight")
    private val keyScreenPosX = longPreferencesKey("screenPosX")
    private val keyScreenPosY = longPreferencesKey("screenPosY")
    suspend fun getScreen(): WinScreenSettings {
        val it = dataStore.data.first()
        return WinScreenSettings(
            width = it[keyScreenWidth]?.toInt()?.dp ?: 600.dp,
            height = it[keyScreenHeight]?.toInt()?.dp ?: 600.dp,
            posX = it[keyScreenPosX]?.toInt()?.dp ?: 20.dp,
            posY = it[keyScreenPosY]?.toInt()?.dp ?: 20.dp
        )
    }
    suspend fun setScreen(width: Long, height: Long, posX: Long, posY: Long) {
        updatePref(keyScreenWidth, width)
        updatePref(keyScreenHeight, height)
        updatePref(keyScreenPosX, posX)
        updatePref(keyScreenPosY, posY)
    }


    private val keySyncType = stringPreferencesKey("syncType")
    suspend fun getSyncType() = dataStore.data.first()[keySyncType] ?: "drive"
    suspend fun setSyncType(it: String) = updatePref(keySyncType, it)


    private val keyMainName = stringPreferencesKey("mainName")
    suspend fun getMainName() = dataStore.data.first()[keyMainName] ?: ""
    suspend fun setMainName(it: String) = updatePref(keyMainName, it)
}