package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yurhel.alex.anotes.Platform
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.Note
import com.yurhel.alex.anotes.data.SettingsDataStore
import com.yurhel.alex.anotes.data.SocketConnection
import com.yurhel.alex.anotes.data.Status
import com.yurhel.alex.anotes.data.SyncType
import com.yurhel.alex.anotes.data.Task
import com.yurhel.alex.anotes.ui.utils.DriveUtils
import com.yurhel.alex.anotes.ui.utils.NoteType
import com.yurhel.alex.anotes.ui.utils.SyncActionTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Date
import kotlin.reflect.KClass

class MainViewModel(
    val platform: Platform,
    val settings: SettingsDataStore,
    val db: LocalDB
) : ViewModel() {
    class Factory(
        private val platform: Platform,
        private val settings: SettingsDataStore,
        private val db: LocalDB
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            MainViewModel(platform, settings, db) as T
    }


    var statusesInTopBar = false
        private set
    suspend fun updateStatusesInTopBar(it: Boolean) {
        statusesInTopBar = it
        settings.setStatusesInTopBar(it)
    }


    var darkTheme by mutableStateOf<Boolean?>(null)
        private set
    fun updateDarkTheme(it: Boolean?) = viewModelScope.launch {
        darkTheme = it
        settings.setDarkTheme(it)
    }


    var isSyncOn by mutableStateOf(false)
        private set
    fun updateSyncNow(isSyncNow: Boolean) {
        this.isSyncOn = isSyncNow
    }

    var isSyncSourceDialogOpen by mutableStateOf(false)
        private set
    fun setSyncSourceDialogVisibility(isOpen: Boolean = false) {
        isSyncSourceDialogOpen = isOpen
    }

    var isLocalSyncSheetOpen by mutableStateOf(false)
        private set
    fun localSyncDialogVisibility(isOpen: Boolean = false) {
        isLocalSyncSheetOpen = isOpen
    }

    private suspend fun getLocalDataWithTime(): String {
        return buildJsonObject {
            put("modifiedTime", settings.getDataReceivedDate())
            put("data", db.exportDB())
        }.toString()
    }

    var localAddressCode by mutableStateOf("")
        private set
    var isSocketOn by mutableStateOf(false)
        private set

    fun createSocket() = viewModelScope.launch(Dispatchers.IO) {
        localAddressCode = platform.getLocalIpAddress().split('.').last()
        isSocketOn = true
        val res = SocketConnection.shareData(
            localData = getLocalDataWithTime(),
            port = settings.getLocalSyncPort(),
            onClientOn = {
                updateSyncNow(true)
                localSyncDialogVisibility(false)
            }
        )
        if (!res) {
            localSyncDialogVisibility(true)
        }
        isSocketOn = false
        updateSyncNow(false)
    }

    fun cancelSocket() {
        isSocketOn = false
    }

    fun connectToSocket(hostNumber: String) = viewModelScope.launch(Dispatchers.IO) {
        localAddressCode = ""
        isSocketOn = true
        val data = SocketConnection.getData(
            hostNumber = hostNumber,
            localIpAddress = platform.getLocalIpAddress(),
            port = settings.getLocalSyncPort(),
            onClientOn = {
                updateSyncNow(true)
                localSyncDialogVisibility(false)
            }
        )
        if (data != null) {
            db.importDB(data.data.toString())
            settings.setDataReceivedDate(data.modifiedTime)
            getDbNotes("")
            getAllTasks()
            getAllStatuses()
        } else {
            localSyncDialogVisibility(true)
        }
        isSocketOn = false
        updateSyncNow(false)
    }

    fun syncData(syncActionType: SyncActionTypes) = viewModelScope.launch(Dispatchers.IO) {
        if (settings.getSyncType() == SyncType.drive.name) {
            val sync = DriveUtils(this@MainViewModel, platform.getDrive())
            when (syncActionType) {
                SyncActionTypes.Auto -> sync.syncAuto()
                SyncActionTypes.ManualExport -> sync.syncManual(true)
                SyncActionTypes.ManualImport -> sync.syncManual(false)
            }
        } else {
            if (!isLocalSyncSheetOpen) {
                localSyncDialogVisibility(true)
            }
        }
    }


    var selectedNote by mutableStateOf<Note?>(null)
        private set

    var allStatuses by mutableStateOf<List<Status>>(emptyList())
        private set

    var appSettingsView by mutableStateOf("col")
        private set

    var allNotes by mutableStateOf<List<Note>>(emptyList())
        private set

    var allTasks by mutableStateOf<List<Task>>(emptyList())
        private set

    var searchText by mutableStateOf("")
        private set

    var isSearchOn by mutableStateOf(false)
        private set

    var chosenFolder by mutableIntStateOf(0)
        private set

    var sortType by mutableStateOf("")
        private set

    var sortArrow by mutableStateOf("")
        private set

    val scrollState by mutableStateOf(LazyStaggeredGridState())

    fun updateSortArrow(it: String) = viewModelScope.launch {
        sortArrow = it
        settings.setSortArrow(it)
        getDbNotes("")
    }

    fun updateSortType(it: String) = viewModelScope.launch {
        sortType = it
        settings.setSortType(it)
        getDbNotes("")
    }

    fun chooseViewFolder(f: Int) = viewModelScope.launch {
        chosenFolder = f
        settings.setDataShowing("$f")
        getDbNotes("")
    }

    var mainFolderName by mutableStateOf("")
        private set
    suspend fun getMainFolderName() {
        mainFolderName = settings.getMainName()
    }

    fun updateIsSearchOn(it: Boolean) {
        isSearchOn = it
    }

    fun changeNotesView() = viewModelScope.launch(Dispatchers.Default) {
        val value = if (settings.getViewMode() == "grid") "col" else "grid"
        settings.setViewMode(value)
        appSettingsView = value
    }

    suspend fun getDbNotes(query: String) {
        searchText = query.replace("\n", "")
        val sortType = settings.getSortType()
        val sortArrow = settings.getSortArrow()
        val notChoosingWidget = platform.getWidgetIdWhenCreated() == 0
        allNotes = db.note.getAll(query)
            .filter { if (notChoosingWidget) it.folder == chosenFolder else true }
            .sortedBy { if (sortType == "dateUpdate") it.dateUpdate else it.dateCreate }
            .let { if (sortArrow == "ascending") it.reversed() else it }
            .map { it.copy(type = checkNoteType(it).name) }
    }

    fun getDbNotesScope(query: String) = viewModelScope.launch { getDbNotes(query) }

    fun getAllStatuses() {
        allStatuses = db.status.getAll()
    }

    fun getAllTasks() {
        allTasks = db.task.getAll().sortedBy { it.position }
    }

    /**
     * Creates a new note or selects/unselects an existing one.
     **/
    fun selectNote(
        note: Note? = null,
        newNoteType: NoteType? = null
    ) {
        selectedNote = if (newNoteType != null) {
            // New note is opened. Create new note
            val date = Date().time
            db.note.insert(
                Note(
                    text = "",
                    folder = chosenFolder,
                    dateCreate = date,
                    dateUpdate = date,
                    type = newNoteType.name,
                    format = 10
                )
            )
            viewModelScope.launch {
                settings.setIsNotesEdited(true)
            }
            db.note.getLast()
        } else {
            note
        }
    }

    fun deleteNote(after: () -> Unit) {
        selectedNote?.let { note ->
            db.note.delete(note.id)
            selectNote()
            // Delete tasks
            db.status.deleteManyByNote(note.id)
            db.task.deleteManyByNote(note.id)
            // Delete draws
            db.board.delDraws(note.id)
            db.board.delImage(note.id)

            viewModelScope.launch {
                settings.setIsNotesEdited(true)
                initNotesScreen()
                after()
            }
        }
    }

    fun saveNote(
        updateDate: Boolean = false,
        text: String? = null
    ) {
        selectedNote?.let { note ->
            val editedNote = note.copy(
                text = text ?: note.text,
                dateUpdate = if (updateDate || text != note.text) Date().time else note.dateUpdate,
                format = 10
            )
            selectNote(editedNote)
            db.note.update(editedNote)
            db.widget.getByCreated(noteCreated = note.dateCreate.toString())?.widgetId?.let { wId ->
                platform.callInitUpdateWidget(
                    isInitAction = false,
                    widgetId = wId,
                    noteCreated = note.dateCreate.toString(),
                    note = editedNote,
                    db = db
                )
            }
            viewModelScope.launch {
                settings.setIsNotesEdited(true)
                initNotesScreen()
            }
        }
    }

    fun setNoteFolder(
        folder: Int,
        toastMsg: String
    ) {
        val note = selectedNote
        if (note != null && note.folder != folder) {
            val editedNote = note.copy(folder = folder)
            selectNote(editedNote)
            db.note.update(editedNote)
            viewModelScope.launch { settings.setIsNotesEdited(true) }
            platform.showToast(toastMsg)
        }
    }

    fun getNoteDate(created: Boolean = false) = platform.formatDate(
        if (selectedNote != null) {
            if (created) selectedNote!!.dateCreate else selectedNote!!.dateUpdate
        } else {
            Date().time
        }
    )

    fun checkNoteType(note: Note): NoteType {
        return when(note.type) {
            NoteType.Note.name -> NoteType.Note
            NoteType.Tasks.name -> NoteType.Tasks
            NoteType.Draw.name -> NoteType.Draw
            NoteType.Swipe.name -> NoteType.Swipe
            else -> {
                val foundStatus = allStatuses.find { it.note == note.id } != null
                val foundTask = allTasks.find { it.note == note.id } != null
                if (foundStatus || foundTask) {
                    NoteType.Tasks
                } else if (db.board.getDraws(note.id).isNotEmpty()) {
                    NoteType.Draw
                } else {
                    NoteType.Note
                }
            }
        }
    }

    fun tryGetImage(noteId: Int) = platform.toImageBitmap(db.board.getImage(noteId), true)

    private suspend fun initNotesScreen() {
        getDbNotes("")
        getAllTasks()
        getAllStatuses()
        getMainFolderName()
    }

    init {
        viewModelScope.launch {
            darkTheme = settings.getDarkTheme()
            val dataShowing = settings.getDataShowing()
            chosenFolder = try { dataShowing.toInt() } catch (_: Exception) { 0 }
            sortType = settings.getSortType()
            sortArrow = settings.getSortArrow()
            appSettingsView = settings.getViewMode()
            statusesInTopBar = settings.getStatusesInTopBar()
            initNotesScreen()
        }
    }
}