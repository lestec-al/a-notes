package com.yurhel.alex.anotes.ui

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.yurhel.alex.anotes.data.local.DB
import com.yurhel.alex.anotes.data.local.obj.NoteObj
import com.yurhel.alex.anotes.data.local.obj.SettingsObj
import com.yurhel.alex.anotes.data.local.obj.StatusObj
import com.yurhel.alex.anotes.data.local.obj.TasksObj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
class MainViewModel(
    val db: DB,
    val callExit: () -> Unit,
    val callTrySighIn: () -> Unit,
    var widgetIdWhenCreated: Int,
    var noteCreatedDateFromWidget: String,
    val callUpdateWidget: (isInitAction: Boolean, widgetId: Int, noteCreated: String, noteText: String) -> Unit
) : ViewModel() {

    class Factory(
        private val db: DB,
        private val callExit: () -> Unit,
        private val callTrySighIn: () -> Unit,
        private var widgetIdWhenCreated: Int,
        private var noteCreatedDateFromWidget: String,
        private val callInitUpdateWidget: (
            isInitAction: Boolean,
            widgetId: Int,
            noteCreated: String,
            noteText: String
        ) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(
                db,
                callExit,
                callTrySighIn,
                widgetIdWhenCreated,
                noteCreatedDateFromWidget,
                callInitUpdateWidget
            ) as T
    }

    init {
        // Init settings
        viewModelScope.launch(Dispatchers.Default) {
            if (db.setting.getS() == null) db.setting.upsert(SettingsObj())
        }
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
            _appSettingsView.value = db.setting.getS()?.viewMode ?: "col"
        }
    }

    fun changeNotesView() {
        viewModelScope.launch(Dispatchers.Default) {
            val settings = db.setting.getS()
            _appSettingsView.value = if (settings != null) {
                val value = if (settings.viewMode == "grid") "col" else "grid"
                db.setting.upsert(settings.copy(viewMode = value))
                value
            } else {
                val value = if (_appSettingsView.value == "grid") "col" else "grid"
                db.setting.upsert(SettingsObj(viewMode = value))
                value
            }
        }
    }

    private val _allNotes: MutableStateFlow<List<NoteObj>> = MutableStateFlow(emptyList())
    val allNotes = _allNotes.asStateFlow()

    fun getDbNotes(
        query: String,
        sort: String = "dateUpdate", // dateCreate, dateUpdate
        sortArrow: String = "ascending" // descending, ascending
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            _searchText.value = query.replace("\n", "")

            _allNotes.value = db.note.getByQuery(query).sortedBy {
                if (sort == "dateUpdate") it.dateUpdate else it.dateCreate
            }.let {
                if (sortArrow == "ascending") it.reversed() else it
            }
        }
    }


    // NOTE SCREEN
    private var origNoteText = ""

    val editText = TextFieldState("")

    /**
     * Creates new note or opens existed. Also if screen open from widget but note deleted (from DB) - redirect to notes screen.
     **/
    fun prepareNote(
        redirectToNotesScreen: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            val noteText: String? = if (noteCreatedDateFromWidget != "") {
                // Note open from widget
                val noteFromWidget = db.note.getByCreated(created = noteCreatedDateFromWidget)
                if (noteFromWidget == null) {
                    // Note doesn't exist. Open widget settings
                    val widgetId = db.widget.getByCreated(noteCreated = noteCreatedDateFromWidget)?.widgetId
                    if (widgetId != null) {
                        widgetIdWhenCreated = widgetId.toInt()
                        delay(500L)
                        launch(Dispatchers.Main) { redirectToNotesScreen() }
                    }
                    null
                } else {
                    // Note exist
                    selectNote(noteFromWidget)
                    noteFromWidget.text
                }
            } else if (_selectedNote.value == null) {
                // New note is opened. Create new note
                val date = Date().time
                db.note.upsert(NoteObj(text = "", dateCreate = date, dateUpdate = date))
                // ???
                db.setting.upsert(db.setting.getS()?.copy(isNotesEdited = true) ?: SettingsObj(isNotesEdited = true))
                // Get new note
                selectNote(db.note.getLast())
                ""
            } else {
                // Existed note open
                _selectedNote.value!!.text
            }
            editText.edit {
                this.append(noteText ?: "")
                this.placeCursorBeforeCharAt(0)
            }
            origNoteText = noteText ?: ""
        }
    }

    fun deleteNote() {
        viewModelScope.launch(Dispatchers.Default) {
            val note = _selectedNote.value
            if (note != null) {
                db.note.delete(note)
                selectNote(null)
                // Delete tasks
                db.status.deleteManyByNote(note.id)
                db.task.deleteManyByNote(note.id)
                // For sync
                db.setting.upsert(db.setting.getS()?.copy(isNotesEdited = true) ?: SettingsObj(isNotesEdited = true))
            }
        }
    }

    fun saveNote(
        showTasksState: Boolean? = null,
        withoutNoteTextUpdate: Boolean = false
    ) {
        val edit = _selectedNote.value
        val editTextStr = editText.text.toString()

        viewModelScope.launch(Dispatchers.Default) {
            // Check if the note exists and if its value has changed
            if (edit != null && (origNoteText != editTextStr || showTasksState != null)) {
                // ???
                val newEdit = if (showTasksState != null) {
                    if (withoutNoteTextUpdate) {
                        edit.copy(dateUpdate = Date().time, withTasks = showTasksState)
                    } else {
                        edit.copy(text = editTextStr, dateUpdate = Date().time, withTasks = showTasksState)
                    }
                } else {
                    edit.copy(text = editTextStr, dateUpdate = Date().time)
                }
                // Update selected note
                selectNote(newEdit)
                // Update note db
                db.note.upsert(newEdit)
                // For sync
                db.setting.upsert(db.setting.getS()?.copy(isNotesEdited = true) ?: SettingsObj(isNotesEdited = true))
                // Update widget if it exist
                val widgetId = db.widget.getByCreated(noteCreated = edit.dateCreate.toString())?.widgetId
                if (widgetId != null) callUpdateWidget(false, widgetId.toInt(), edit.dateCreate.toString(), editTextStr)
            }
        }
    }

    fun getNoteDate(
        context: Context,
        isCreatedDate: Boolean = false
    ): String {
        return if (_selectedNote.value != null) {
            val dateLong = if (isCreatedDate) {
                _selectedNote.value!!.dateCreate
            } else {
                _selectedNote.value!!.dateUpdate
            }
            if (DateUtils.isToday(dateLong)) DateFormat.getTimeFormat(context).format(Date(dateLong))
            else DateFormat.getMediumDateFormat(context).format(Date(dateLong))
        } else {
            DateFormat.getTimeFormat(context).format(Date())
        }
    }

    fun formatDate(
        context: Context,
        dateLong: Long?
    ): String {
        return if (dateLong != null) {
            if (DateUtils.isToday(dateLong)) {
                DateFormat.getTimeFormat(context).format(Date(dateLong))
            } else {
                DateFormat.getMediumDateFormat(context).format(Date(dateLong))
            }
        } else ""
    }


    // TASK SCREEN
    // Statuses
    private val _statuses: MutableStateFlow<List<StatusObj>> = MutableStateFlow(emptyList())
    val statuses = _statuses.asStateFlow()

    private fun getStatuses(noteId: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            _statuses.value = db.status.getManyByNote(noteId)
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
            val data = if (statusId == 0) {
                val data = db.task.getManyByNote(noteId)
                var idx = 1
                for (i in data) {
                    i.position = idx
                    db.task.upsert(i)
                    idx++
                }
                data
            } else {
                db.task.getManyByNoteAndStatus(noteId, statusId)
            }
            _tasks.value = data.reversed()
        }
    }

    // Edit dialog
    private val _editDialogVisibility = MutableStateFlow(false)
    val editDialogVisibility = _editDialogVisibility.asStateFlow()

    var editDialogDataType = Types.Task
    var editDialogActionType = ActionTypes.Create
    var editDialogObj: Any? = null


    fun updateTasksData(
        withStatuses: Boolean,
        withNoteSave: Boolean = true
    ) {
        if (withStatuses) getStatuses(_selectedNote.value!!.id)
        getTasks(noteId = _selectedNote.value!!.id, statusId = _selectedStatus.value)
        if (withNoteSave) saveNote(showTasksState = true, withoutNoteTextUpdate = true)
    }


    fun onEvent(event: Event) {
        when (event) {
            // Status
            is Event.UpsertStatus -> {
                viewModelScope.launch(Dispatchers.Default) {
                    db.status.upsert(event.status)
                    updateTasksData(true)
                }
            }
            is Event.DeleteStatus -> {
                viewModelScope.launch(Dispatchers.Default) {
                    if (_selectedNote.value != null) {
                        db.status.delete(event.status)
                        db.task.deleteManyByStatus(event.status.id)
                        updateTasksData(true)
                    }
                }
            }
            // Task
            is Event.UpsertTask -> {
                viewModelScope.launch(Dispatchers.Default) {
                    db.task.upsert(event.task)
                    // Set position from generated id ???
                    if (event.task.id == 0) {
                        try {
                            db.withTransaction {
                                val id = db.task.getLast().id
                                db.task.upsert(event.task.copy(id = id, position = id))
                            }
                        } catch (_: Exception) {}
                    }
                    updateTasksData(false)
                }
            }
            is Event.DeleteTask -> {
                viewModelScope.launch(Dispatchers.Default) {
                    db.task.delete(event.task)
                    updateTasksData(false)
                }
            }
            is Event.ChangePos -> {
                // Is this good enough ???
                viewModelScope.launch(Dispatchers.Default) {
                    val targetPos = when(event.pos) {
                        Pos.Prev -> event.task.position - 1
                        Pos.Next -> event.task.position + 1
                    }
                    val targetTask = db.task.getByPosition(targetPos)
                    if (targetTask != null) {
                        db.task.upsert(targetTask.copy(position = event.task.position))
                    }
                    delay(100) // ???
                    db.task.upsert(event.task.copy(position = targetPos))
                    updateTasksData(false)
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
}