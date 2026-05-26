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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.draw
import anotes.composeapp.generated.resources.note
import anotes.composeapp.generated.resources.swipe_notes
import anotes.composeapp.generated.resources.tasks
import com.yurhel.alex.anotes.Drive
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.NoteObj
import com.yurhel.alex.anotes.data.SettingsDataStore
import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj
import com.yurhel.alex.anotes.toImageBitmap
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
    val isTest: Boolean,
    // Next used only in Android
    val callExit: () -> Unit,
    var widgetIdWhenCreated: Int,
    val callInitUpdateWidget: (Boolean, Int, String, NoteObj) -> Unit
) : ViewModel() {

    class Factory(
        private val db: LocalDB,
        private val settings: SettingsDataStore,
        private val isTest: Boolean = false,
        private val callExit: () -> Unit,
        private var widgetIdWhenCreated: Int,
        private val callInitUpdateWidget: (Boolean, Int, String, NoteObj) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            MainViewModel(db, settings, isTest, callExit, widgetIdWhenCreated, callInitUpdateWidget) as T
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
    fun openSyncDialog(isOpen: Boolean) {
        _isSyncDialogOpen.value = isOpen
    }

    fun syncData(syncActionType: SyncActionTypes) {
        val driveUtils = DriveUtils(this, Drive())
        when (syncActionType) {
            SyncActionTypes.Auto -> driveUtils.driveSyncAuto()
            SyncActionTypes.ManualExport -> driveUtils.driveSyncManualThread(true)
            SyncActionTypes.ManualImport -> driveUtils.driveSyncManualThread(false)
        }
    }


    private var origNoteText = ""

    var selectedNote by mutableStateOf<NoteObj?>(null)
        private set

    private val _allStatuses: MutableStateFlow<List<StatusObj>> = MutableStateFlow(emptyList())
    val allStatuses = _allStatuses.asStateFlow()

    private val _appSettingsView: MutableStateFlow<String> = MutableStateFlow("col")
    val appSettingsView = _appSettingsView.asStateFlow()

    private val _allNotes: MutableStateFlow<List<NoteObj>> = MutableStateFlow(emptyList())
    val allNotes = _allNotes.asStateFlow()

    private val _allTasks: MutableStateFlow<List<TasksObj>> = MutableStateFlow(emptyList())
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

    fun updateIsNotesMenuExpanded(it: Boolean) {
        isNotesMenuExpanded = it
    }

    fun updateIsSearchOn(it: Boolean) {
        isSearchOn = it
    }

    fun selectNote(note: NoteObj?) {
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
                NoteObj(
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
                // Update widget if it exist
                val widgetId = db.widget.getByCreated(noteCreated = edit.dateCreate.toString())?.widgetId
                if (widgetId != null) {
                    callInitUpdateWidget(false, widgetId.toInt(), edit.dateCreate.toString(), newEdit)
                }
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

    fun getNoteDate(created: Boolean = false): Long {
        return if (selectedNote != null) {
            if (created) selectedNote!!.dateCreate else selectedNote!!.dateUpdate
        } else {
            Date().time
        }
    }

    fun checkNoteType(note: NoteObj): NoteType {
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

    fun tryGetImage(noteId: Int) = db.board.getImage(noteId)?.toImageBitmap()

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
}