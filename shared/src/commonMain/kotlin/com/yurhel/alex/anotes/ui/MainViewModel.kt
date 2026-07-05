package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
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
    val db: LocalDB,
    val settings: SettingsDataStore,
    val platform: Platform
) : ViewModel() {
    class Factory(
        private val platform: Platform,
        private val db: LocalDB = LocalDB.getInstance(platform.getSqlDriver()),
        private val settings: SettingsDataStore = SettingsDataStore.getInstance { platform.createDataStorePlatform() }
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            MainViewModel(db, settings, platform) as T
    }


    var darkTheme by mutableStateOf<Boolean?>(null)
        private set
    init {
        viewModelScope.launch { darkTheme = settings.getDarkTheme() }
    }
    fun updateDarkTheme(it: Boolean?) = viewModelScope.launch {
        darkTheme = it
        settings.setDarkTheme(it)
    }


    var isSyncOn by mutableStateOf(false)
        private set
    fun updateSyncNow(isSyncNow: Boolean) {
        this.isSyncOn = isSyncNow
    }

    var isSyncDialogOpen by mutableStateOf(false)
        private set
    fun setSyncDialogVisibility(isOpen: Boolean = false) {
        isSyncDialogOpen = isOpen
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
        val data = SocketConnection.getData(
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

    fun cancelSocket() {
        isSocketOn = false
    }

    fun connectToSocket(hostNumber: String) = viewModelScope.launch(Dispatchers.IO) {
        localAddressCode = ""
        isSocketOn = true
        val res = SocketConnection.shareData(
            hostNumber = hostNumber,
            localData = getLocalDataWithTime(),
            localIpAddress = platform.getLocalIpAddress(),
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


    private var origNoteText = ""

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

    var editText by mutableStateOf(TextFieldState(""))
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

    init {
        viewModelScope.launch {
            val dataShowing = settings.getDataShowing()
            chosenFolder = try { dataShowing.toInt() } catch (_: Exception) { 0 }
            sortType = settings.getSortType()
            sortArrow = settings.getSortArrow()
        }
    }

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
    fun getMainFolderName() = viewModelScope.launch {
        mainFolderName = settings.getMainName()
    }

    fun updateIsSearchOn(it: Boolean) {
        isSearchOn = it
    }

    fun selectNote(note: Note?) {
        selectedNote = note
    }

    fun changeNotesView() = viewModelScope.launch(Dispatchers.Default) {
        val value = if (settings.getViewMode() == "grid") "col" else "grid"
        settings.setViewMode(value)
        appSettingsView = value
    }

    fun getDbNotes(query: String) = viewModelScope.launch(Dispatchers.Default) {
        searchText = query.replace("\n", "")
        val sortType = settings.getSortType()
        val sortArrow = settings.getSortArrow()
        val notChoosingWidget = platform.getWidgetIdWhenCreated() == 0
        allNotes = db.note.getAll(query)
            .filter { if (notChoosingWidget) it.folder == chosenFolder else true }
            .sortedBy { if (sortType == "dateUpdate") it.dateUpdate else it.dateCreate }
            .let { if (sortArrow == "ascending") it.reversed() else it }
    }

    fun getAllStatuses() = viewModelScope.launch(Dispatchers.Default) {
        allStatuses = db.status.getAll()
    }

    fun getAllTasks() = viewModelScope.launch(Dispatchers.Default) {
        allTasks = db.task.getAll().sortedBy { it.position }
    }

    fun updateEditTextValue(
        text: String?,
        cursorIdx: Int? = null
    ) {
        editText.clearText()
        if (text == null) return
        editText.edit {
            append(text)
            if (cursorIdx != null) {
                try { placeCursorAfterCharAt(cursorIdx) } catch (_: Exception) {}
            }
        }
    }

    /**
     * Creates a new note or selects an existing one.
     **/
    fun prepareNote(newNoteType: NoteType?) {
        val noteText: String = if (newNoteType != null) {
            // New note is opened. Create new note
            val date = Date().time
            db.note.insert(
                Note(
                    text = "",
                    folder = chosenFolder,
                    dateCreate = date,
                    dateUpdate = date,
                    type = newNoteType.name
                )
            )
            viewModelScope.launch {
                settings.setIsNotesEdited(true)
            }
            selectNote(db.note.getLast())
            ""
        } else {
            // Existed note open
            selectedNote!!.text
        }
        updateEditTextValue(noteText, 0)
        origNoteText = noteText
    }

    fun deleteNote() {
        updateEditTextValue(null)
        viewModelScope.launch(Dispatchers.Default) {
            val note = selectedNote
            if (note != null) {
                db.note.delete(note.id)
                selectNote(null)
                // Delete tasks
                db.status.deleteManyByNote(note.id)
                db.task.deleteManyByNote(note.id)
                // Delete draws
                db.board.delDraws(note.id)
                db.board.delImage(note.id)
                // For sync
                settings.setIsNotesEdited(true)
            }
        }
    }

    fun saveNote(isEditDateForcedUpdate: Boolean = false) {
        val edit = selectedNote
        val editTextStr = editText.text.toString()
        updateEditTextValue(null)
        viewModelScope.launch(Dispatchers.Default) {
            // Check if the note exists
            if (edit != null) {
                var text = edit.text
                var dateUpdate = if (isEditDateForcedUpdate) Date().time else edit.dateUpdate
                if (!isEditDateForcedUpdate && editTextStr != origNoteText) {
                    text = editTextStr
                    dateUpdate = Date().time
                }
                val newEdit = edit.copy(text = text, dateUpdate = dateUpdate)
                // Update selected note
                selectNote(newEdit)
                // Update note db
                db.note.update(newEdit)
                // For sync
                settings.setIsNotesEdited(true)
                // Update widget if it exists
                val widgetId = db.widget.getByCreated(noteCreated = edit.dateCreate.toString())?.widgetId
                if (widgetId != null) {
                    platform.callInitUpdateWidget(
                        false,
                        widgetId,
                        edit.dateCreate.toString(),
                        newEdit,
                        db
                    )
                }
                // Set new note text (required when saved not on the exit)
                updateEditTextValue(text, 0)
            }
        }
    }

    fun setNoteFolder(f: Int, after: () -> Unit) {
        val edit = selectedNote
        viewModelScope.launch {
            if (edit != null && edit.folder != f) {
                val newEdit = edit.copy(folder = f)
                selectNote(newEdit)
                db.note.update(newEdit)
                // For sync
                settings.setIsNotesEdited(true)
            }
            after()
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
                } else if (db.board.getImage(note.id) != null) {
                    NoteType.Draw
                } else {
                    NoteType.Note
                }
            }
        }
    }

    fun tryGetImage(noteId: Int) = platform.toImageBitmap(db.board.getImage(noteId), true)

    fun initNotesScreen() {
        getDbNotes("")
        getAllTasks()
        getAllStatuses()
        getMainFolderName()
        viewModelScope.launch {
            appSettingsView = settings.getViewMode()
        }
    }


    // PICTURE (note screen)
    var noteImage by mutableStateOf<ImageBitmap?>(null)
        private set
    var isAddImage by mutableStateOf(true)
        private set

    fun addImage() = viewModelScope.launch {
        platform.importImage { base64Str ->
            val noteId = selectedNote?.id
            if (noteId != null) {
                db.board.addUpdateImage(noteId, base64Str)
                updateImageData()
            }
        }
    }

    fun delImage() {
        selectedNote?.id?.let { db.board.delImage(it) }
        updateImageData()
    }

    fun updateImageData() {
        noteImage = selectedNote?.id?.let {
            val imgStr = db.board.getImage(it)
            if (imgStr == null) null else {
                val img = platform.toImageBitmap(db.board.getImage(it), false)
                img ?: ImageBitmap(50, 50)
            }
        }
        isAddImage = noteImage == null
    }
}