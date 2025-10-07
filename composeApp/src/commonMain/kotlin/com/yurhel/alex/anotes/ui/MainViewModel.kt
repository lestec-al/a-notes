package com.yurhel.alex.anotes.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.NoteObj
import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.reflect.KClass

class MainViewModel(
    val showToast: (text: String) -> Unit,
    val db: LocalDB,
    val formatDate: (Long) -> String,
    val syncData: (SyncActionTypes, MainViewModel) -> Unit,
    // Next used only in Android
    val callExit: () -> Unit,
    var widgetIdWhenCreated: Int,
    var noteCreatedDateFromWidget: String,
    val callInitUpdateWidget: (isInitAction: Boolean, widgetId: Int, noteCreated: String, note: NoteObj) -> Unit
) : ViewModel() {

    class Factory(
        private val showToast: (text: String) -> Unit,
        private val db: LocalDB,
        private val formatDate: (Long) -> String,
        private val syncData: (SyncActionTypes, MainViewModel) -> Unit,
        private val callExit: () -> Unit,
        private var widgetIdWhenCreated: Int,
        private var noteCreatedDateFromWidget: String,
        private val callInitUpdateWidget: (isInitAction: Boolean, widgetId: Int, noteCreated: String, note: NoteObj) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            MainViewModel(
                showToast,
                db,
                formatDate,
                syncData,
                callExit,
                widgetIdWhenCreated,
                noteCreatedDateFromWidget,
                callInitUpdateWidget
            ) as T
    }


    // DRIVE SYNC
    private val _isSyncNow = MutableStateFlow(false)
    val isSyncNow = _isSyncNow.asStateFlow()
    fun changeSyncNow(isSyncNow: Boolean) {
        _isSyncNow.value = isSyncNow
    }

    private val _isSyncDialogOpen = MutableStateFlow(false)
    val isSyncDialogOpen = _isSyncDialogOpen.asStateFlow()
    fun openSyncDialog(isOpen: Boolean) {
        _isSyncDialogOpen.value = isOpen
    }


    private val _selectedNote: MutableStateFlow<NoteObj?> = MutableStateFlow(null)
    val selectedNote = _selectedNote.asStateFlow()
    fun selectNote(note: NoteObj?) {
        _selectedNote.value = note
    }


    // NOTES - MAIN SCREEN
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _appSettingsView: MutableStateFlow<String> = MutableStateFlow("col")
    val appSettingsView = _appSettingsView.asStateFlow()

    fun getNotesView() {
        viewModelScope.launch(Dispatchers.Default) {
            _appSettingsView.value = db.getSettings().viewMode
        }
    }

    fun changeNotesView() {
        viewModelScope.launch(Dispatchers.Default) {
            val settings = db.getSettings()
            val value = if (settings.viewMode == "grid") "col" else "grid"
            db.updateViewMode(value)
            _appSettingsView.value = value
        }
    }

    private val _allNotes: MutableStateFlow<List<NoteObj>> = MutableStateFlow(emptyList())
    val allNotes = _allNotes.asStateFlow()

    fun getDbNotes(query: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _searchText.value = query.replace("\n", "")

            val dataShowing = db.getDataShowing()
            val sortType = db.getSortType()
            val sortArrow = db.getSortArrow()

            _allNotes.value = db.getNotes(query)
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
    }

    private val _allStatuses: MutableStateFlow<List<StatusObj>> = MutableStateFlow(emptyList())
    val allStatuses = _allStatuses.asStateFlow()

    fun getAllStatuses() {
        viewModelScope.launch(Dispatchers.Default) {
            _allStatuses.value = db.getAllStatuses()
        }
    }

    private val _allTasks: MutableStateFlow<List<TasksObj>> = MutableStateFlow(emptyList())
    val allTasks = _allTasks.asStateFlow()

    fun getAllTasks() {
        viewModelScope.launch(Dispatchers.Default) {
            _allTasks.value = db.getAllTasks().sortedBy { it.position }
        }
    }


    // NOTE SCREEN
    private var origNoteText = ""

    private val _editText = MutableStateFlow("")
    val editText = _editText.asStateFlow()

    fun changeEditTextValue(text: String) {
        _editText.value = text
    }

    private fun clearEditText() {
        _editText.value = ""
    }

    /**
     * Creates new note or opens existed. Also if screen open from widget but note deleted (from DB) - redirect to notes screen.
     **/
    fun prepareNote(
        redirectToNotesScreen: () -> Unit,
        redirectToTasksScreen: () -> Unit,
        after: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val noteText: String? = if (noteCreatedDateFromWidget != "") {
                // Note open from widget
                val noteFromWidget = db.getByCreatedNote(noteCreatedDateFromWidget)
                if (noteFromWidget == null) {
                    // Note doesn't exist. Open widget settings
                    val widgetId = db.getByCreatedWidget(noteCreatedDateFromWidget)?.widgetId
                    if (widgetId != null) {
                        widgetIdWhenCreated = widgetId.toInt()
                        delay(500L)
                        launch(Dispatchers.Main) { redirectToNotesScreen() }
                    }
                    null
                } else {
                    // Note exist
                    selectNote(noteFromWidget)

                    if (checkIfNoteHaveTasks(noteFromWidget)) {
                        delay(500L)
                        // When the app is open from widget - noteScreen opens
                        // For tasks, need redirection to taskScreen
                        launch(Dispatchers.Main) { redirectToTasksScreen() }
                        null
                    } else {
                        noteFromWidget.text
                    }
                }
            } else if (_selectedNote.value == null) {
                // New note is opened. Create new note
                val date = Date().time
                db.createNote(NoteObj(text = "", isArchived = false, dateCreate = date, dateUpdate = date))
                // ???
                db.updateEdit(true)
                // Get new note
                selectNote(db.getLastNote())
                ""
            } else {
                // Existed note open
                _selectedNote.value!!.text
            }

            _editText.value = noteText ?: ""
            origNoteText = noteText ?: ""

            after()
        }
    }

    fun deleteNote() {
        clearEditText()

        viewModelScope.launch(Dispatchers.Default) {
            val note = _selectedNote.value
            if (note != null) {
                db.deleteNote(note.id)
                selectNote(null)
                // Delete tasks
                db.deleteManyByNoteStatuses(note.id)
                db.deleteManyByNoteTasks(note.id)
                // For sync
                db.updateEdit(true)
            }
        }
    }

    fun saveNote(isEditDateForcedUpdate: Boolean = false): Boolean {
        val edit = _selectedNote.value
        val editTextStr = _editText.value
        clearEditText()
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
                db.updateNote(newEdit)
                // For sync
                db.updateEdit(true)
                // Update widget if it exist
                val widgetId = db.getByCreatedWidget(noteCreated = edit.dateCreate.toString())?.widgetId
                if (widgetId != null) callInitUpdateWidget(false, widgetId.toInt(), edit.dateCreate.toString(), newEdit)
            }
        }
        return editTextStr != origNoteText
    }

    fun archiveOrUnarchiveNote(isArchived: Boolean) {
        val edit = _selectedNote.value
        viewModelScope.launch(Dispatchers.Default) {
            if (edit != null) {
                val newEdit = edit.copy(isArchived = isArchived)
                selectNote(newEdit)
                db.updateNote(newEdit)
                // For sync
                db.updateEdit(true)
            }
        }
    }

    fun getIsSelectedNoteArchived(): Boolean {
        return if (_selectedNote.value != null) _selectedNote.value!!.isArchived else false
    }

    fun getNoteDate(isCreatedDate: Boolean = false): Long {
        return if (_selectedNote.value != null) {
            if (isCreatedDate) {
                _selectedNote.value!!.dateCreate
            } else {
                _selectedNote.value!!.dateUpdate
            }
        } else {
            Date().time
        }
    }


    // TASK SCREEN
    // Statuses
    private val _statuses: MutableStateFlow<List<StatusObj>> = MutableStateFlow(emptyList())
    val statuses = _statuses.asStateFlow()

    private fun getStatuses(noteId: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            _statuses.value = db.getManyByNoteStatuses(noteId)
        }
    }

    private val _selectedStatus: MutableStateFlow<Int> = MutableStateFlow(0)
    val selectedStatus = _selectedStatus.asStateFlow()

    fun selectStatus(statusId: Int) {
        _selectedStatus.value = statusId
    }

    // Tasks
    private val _tasks: MutableStateFlow<List<TasksObj>> = MutableStateFlow(emptyList())
    val tasks = _tasks.asStateFlow()

    fun clearTasks() {
        _tasks.value = emptyList()
    }

    private fun getTasks(noteId: Int, statusId: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            _tasks.value = if (statusId == 0) {
                db.getManyByNoteTasks(noteId)
            } else {
                db.getManyByNoteAndStatusTasks(noteId, statusId)
            }.sortedBy { it.position }
        }
    }

    // Edit dialog
    private val _editDialogVisibility = MutableStateFlow(false)
    val editDialogVisibility = _editDialogVisibility.asStateFlow()

    var editDialogDataType = Types.Task
    var editDialogActionType = ActionTypes.Create
    var editDialogObj: Any? = null


    fun updateTasksData(
        isSaveNote: Boolean
    ) {
        getStatuses(_selectedNote.value!!.id)
        getTasks(noteId = _selectedNote.value!!.id, statusId = _selectedStatus.value)
        if (isSaveNote) saveNote(isEditDateForcedUpdate = true)
    }


    fun onEvent(event: Event) {
        when (event) {
            // Status
            is Event.UpsertStatus -> {
                viewModelScope.launch(Dispatchers.Default) {
                    if (event.status.id == 0) db.insertStatus(event.status) else db.updateStatus(event.status)
                    updateTasksData(true)
                }
            }
            is Event.DeleteStatus -> {
                viewModelScope.launch(Dispatchers.Default) {
                    if (_selectedNote.value != null) {
                        db.deleteStatus(event.status.id)
                        db.deleteManyByStatusTasks(event.status.id)
                        updateTasksData(true)
                    }
                }
            }
            // Task
            is Event.UpsertTask -> {
                viewModelScope.launch(Dispatchers.Default) {
                    if (event.task.id == 0) {
                        db.insertTask(
                            event.task.copy(
                                position = db.getHowManyTasksNoteHas(event.task.note)
                            )
                        )
                    } else {
                        db.updateTask(event.task)
                    }
                    delay(200)
                    updateTasksData(true)
                }
            }
            is Event.DeleteTask -> {
                viewModelScope.launch(Dispatchers.Default) {
                    db.deleteTask(event.task.id)
                    delay(200)
                    updateTasksData(true)
                }
            }
            is Event.ChangePos -> {
                viewModelScope.launch(Dispatchers.Default) {
                    val newIdx = event.pos - 2
                    val newPos = when {
                        newIdx < 0 -> 0
                        else -> newIdx
                    }
                    val oldPos = event.task.position
                    _tasks.value
                        .sortedBy { it.position }
                        .forEach {
                            if (newPos > oldPos) {
                                // Next
                                if (it.position in (oldPos + 1)..newPos) {
                                    db.updateTask(it.copy(position = it.position - 1))
                                }
                            } else {
                                // Prev
                                if (it.position in newPos..<oldPos) {
                                    db.updateTask(it.copy(position = it.position + 1))
                                }
                            }
                        }
                    db.updateTask(event.task.copy(position = newPos))
                    updateTasksData(false)
                    // ??????
                    // Prevent of having problems while drag/drop, because of not unique position vars
                    // Just set them to unique values
                    launch {
                        delay(1000)
                        _tasks.value
                            .sortedBy { it.position }
                            .forEachIndexed { idx, it ->
                                db.updateTask(it.copy(position = idx))
                            }
                    }
                }
            }
            // Others
            is Event.ShowEditDialog -> {
                editDialogDataType = event.dataType
                editDialogActionType = event.actionType
                editDialogObj = event.selectedObj

                _editDialogVisibility.value = true
            }
            Event.HideEditDialog -> {
                _editDialogVisibility.value = false
            }
        }
    }

    var notesScreenSavedScroll by mutableStateOf(Pair(0,0))
        private set
    fun updateNotesScreenScrollItem(value: Pair<Int,Int>) {
        notesScreenSavedScroll = value
    }

    fun checkIfNoteHaveTasks(note: NoteObj): Boolean {
        val foundStatus = _allStatuses.value.find { it.note == note.id } != null
        val foundTask = _allTasks.value.find { it.note == note.id } != null
        return foundStatus || foundTask
    }

    fun getTaskTextForNote(): String {
        return buildString {
            append(_selectedNote.value?.text ?: "")
            appendLine()
            _tasks.value.forEach {
                append(it.description)
                appendLine()
                appendLine()
            }
        }
    }
}