package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.getValue
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
import com.yurhel.alex.anotes.data.Status
import com.yurhel.alex.anotes.data.Task
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.draw
import com.yurhel.alex.anotes.shared.note
import com.yurhel.alex.anotes.shared.swipe_notes
import com.yurhel.alex.anotes.shared.tasks
import com.yurhel.alex.anotes.ui.utils.DriveUtils
import com.yurhel.alex.anotes.ui.utils.NoteType
import com.yurhel.alex.anotes.ui.utils.SyncActionTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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


    var isSyncNow by mutableStateOf(false)
        private set
    fun updateSyncNow(isSyncNow: Boolean) {
        this.isSyncNow = isSyncNow
    }

    private val _isSyncDialogOpen = MutableStateFlow(false)
    val isSyncDialogOpen = _isSyncDialogOpen.asStateFlow()
    fun syncDialogVisibility(isOpen: Boolean = false) {
        _isSyncDialogOpen.value = isOpen
    }

    fun syncData(syncActionType: SyncActionTypes) {
        val driveUtils = DriveUtils(this@MainViewModel, platform.getDrive())
        when (syncActionType) {
            SyncActionTypes.Auto -> driveUtils.driveSyncAuto()
            SyncActionTypes.ManualExport -> driveUtils.driveSyncManualThread(true)
            SyncActionTypes.ManualImport -> driveUtils.driveSyncManualThread(false)
        }
    }


    private var origNoteText = ""

    var selectedNote by mutableStateOf<Note?>(null)
        private set

    private val _allStatuses: MutableStateFlow<List<Status>> = MutableStateFlow(emptyList())
    val allStatuses = _allStatuses.asStateFlow()

    private val _appSettingsView: MutableStateFlow<String> = MutableStateFlow("col")
    val appSettingsView = _appSettingsView.asStateFlow()

    private val _allNotes: MutableStateFlow<List<Note>> = MutableStateFlow(emptyList())
    val allNotes = _allNotes.asStateFlow()

    private val _allTasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val allTasks = _allTasks.asStateFlow()

    var editText by mutableStateOf(TextFieldState(""))
        private set

    var notesScreenSavedScroll by mutableStateOf(Pair(0,0))
        private set


    var searchText by mutableStateOf("")
        private set

    var isSearchOn by mutableStateOf(false)
        private set

    var isNotesMenuExpanded by mutableStateOf(false)
        private set

    var isShowArchive by mutableStateOf(false)
        private set

    var sortType by mutableStateOf("")
        private set

    var sortArrow by mutableStateOf("")
        private set


    init {
        viewModelScope.launch {
            isShowArchive = settings.getDataShowing() == "archive"
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

    fun hideArchive() = viewModelScope.launch {
        isShowArchive = false
        settings.setDataShowing("all")
        getDbNotes("")
    }

    fun showArchive() = viewModelScope.launch {
        isShowArchive = true
        settings.setDataShowing("archive")
        getDbNotes("")
    }

    fun openMenu() {
        isNotesMenuExpanded = true
    }
    fun closeMenu() {
        isNotesMenuExpanded = false
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
        _appSettingsView.value = value
    }

    fun getDbNotes(query: String) = viewModelScope.launch(Dispatchers.Default) {
        searchText = query.replace("\n", "")

        val dataShowing = settings.getDataShowing()
        val sortType = settings.getSortType()
        val sortArrow = settings.getSortArrow()

        _allNotes.value = db.note.getAll(query)
            .filter {
                if (dataShowing == "archive") it.isArchived else !it.isArchived
            }
            .sortedBy {
                if (sortType == "dateUpdate") it.dateUpdate else it.dateCreate
            }
            .let {
                if (sortArrow == "ascending") it.reversed() else it
            }
    }

    fun getAllStatuses() = viewModelScope.launch(Dispatchers.Default) {
        _allStatuses.value = db.status.getAll()
    }

    fun getAllTasks() = viewModelScope.launch(Dispatchers.Default) {
        _allTasks.value = db.task.getAll().sortedBy { it.position }
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
                    isArchived = false,
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

    fun archiveOrUnarchiveNote(isArchived: Boolean) {
        val edit = selectedNote
        viewModelScope.launch(Dispatchers.Default) {
            if (edit != null) {
                val newEdit = edit.copy(isArchived = isArchived)
                selectNote(newEdit)
                db.note.update(newEdit)
                // For sync
                settings.setIsNotesEdited(true)
            }
        }
    }

    fun getIsSelectedNoteArchived(): Boolean {
        return if (selectedNote != null) selectedNote!!.isArchived else false
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
                val foundStatus = _allStatuses.value.find { it.note == note.id } != null
                val foundTask = _allTasks.value.find { it.note == note.id } != null
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

    fun updateNotesScreenScrollItem(scrollState: LazyStaggeredGridState) {
        notesScreenSavedScroll = Pair(
            scrollState.firstVisibleItemIndex,
            scrollState.firstVisibleItemScrollOffset
        )
    }

    fun getNotesScreenDropMenuItems(
        scrollState: LazyStaggeredGridState,
        newNoteClicked: (type: NoteType) -> Unit
    ) = listOf(
        Triple(Res.string.swipe_notes, Icons.Outlined.Swipe) {
            newNoteClicked(NoteType.Swipe)
            updateNotesScreenScrollItem(scrollState)
        },
        Triple(Res.string.draw, Icons.Outlined.Brush) {
            newNoteClicked(NoteType.Draw)
            updateNotesScreenScrollItem(scrollState)
        },
        Triple(Res.string.tasks, Icons.AutoMirrored.Outlined.FormatListBulleted) {
            newNoteClicked(NoteType.Tasks)
            updateNotesScreenScrollItem(scrollState)
        },
        Triple(Res.string.note, Icons.Outlined.TextFields) {
            newNoteClicked(NoteType.Note)
            updateNotesScreenScrollItem(scrollState)
        }
    )

    fun initNotesScreen() {
        getDbNotes("")
        getAllTasks()
        getAllStatuses()
        viewModelScope.launch {
            _appSettingsView.value = settings.getViewMode()
        }
    }


    // NOTE EDIT (for tasks, board screens)
    var isNoteEditSheetOpen by mutableStateOf(false)
        private set
    fun updateNoteEditSheet(open: Boolean = false) {
        isNoteEditSheetOpen = open
    }
    fun onSaveNoteText(it: String) {
        updateEditTextValue(it)
        saveNote()
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