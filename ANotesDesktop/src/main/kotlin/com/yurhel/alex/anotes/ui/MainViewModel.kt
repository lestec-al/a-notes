package com.yurhel.alex.anotes.ui

import com.yurhel.alex.anotes.data.drive.Drive
import com.yurhel.alex.anotes.data.local.DB
import com.yurhel.alex.anotes.data.local.NoteObj
import com.yurhel.alex.anotes.data.local.StatusObj
import com.yurhel.alex.anotes.data.local.TasksObj
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class MainViewModel {

    // Init variables
    val db = DB()
    private val drive = Drive()
    private val res = Res()

    fun getString(key: String) = res.strings.getValue(key)

    // NOTES - MAIN SCREEN
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _allNotes = MutableStateFlow(db.getNotes())
    val allNotes = _allNotes.asStateFlow()

    fun getDbNotes(
        query: String,
        sort: String = "dateUpdate", // dateCreate, dateUpdate
        sortArrow: String = "ascending" // descending, ascending
    ) {
        _searchText.value = query.replace("\n", "")

        _allNotes.value = db.getNotes(query).sortedBy {
            if (sort == "dateUpdate") it.dateUpdate else it.dateCreate
        }.let {
            if (sortArrow == "ascending") it.reversed() else it
        }
    }

    private val _allStatuses: MutableStateFlow<List<StatusObj>> = MutableStateFlow(emptyList())
    val allStatuses = _allStatuses.asStateFlow()

    fun getAllStatuses() {
        _allStatuses.value = db.getAllStatuses()
    }

    private val _allTasks: MutableStateFlow<List<TasksObj>> = MutableStateFlow(emptyList())
    val allTasks = _allTasks.asStateFlow()

    fun getAllTasks() {
        _allTasks.value = db.getAllTasks().reversed()
    }


    // NOTE SCREEN
    private var origNoteText = ""

    private val _editText = MutableStateFlow("")
    val editText = _editText.asStateFlow()

    fun changeEditTexValue(text: String) {
        _editText.value = text
    }

    /**
     * Creates new note or opens existed. Also, if screen open from widget but note deleted (from DB) - redirect to notes screen.
     **/
    fun prepareNote() {
        val noteText = if (_selectedNote.value == null) {
            // New note is opened. Create new note
            val date = Date().time
            db.createNote(NoteObj(text = "", dateCreate = date, dateUpdate = date))
            // ???
            db.updateEdit(true)
            // Get new note
            selectNote(db.getLastNote())
            ""
        } else {
            // Existed note open
            _selectedNote.value!!.text
        }
        _editText.value = noteText
        origNoteText = noteText
    }

    fun deleteNote() {
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

    fun saveNote(
        showTasksState: Boolean? = null,
        withoutNoteTextUpdate: Boolean = false
    ) {
        val edit = _selectedNote.value
        val editTextStr = editText.value

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
            db.updateNote(newEdit)
            // For sync
            db.updateEdit(true)
        }
    }

    fun getNoteDate(
        isCreatedDate: Boolean = false
    ): String {
        return if (_selectedNote.value != null) {
            val date = Date(
                if (isCreatedDate) {
                    _selectedNote.value!!.dateCreate
                } else {
                    _selectedNote.value!!.dateUpdate
                }
            )
            // Check if is today
            val today = Calendar.Builder().build()
            today.time = Date()
            val check = Calendar.Builder().build()
            check.time = date

            if (today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR) && today.get(Calendar.YEAR) == check.get(Calendar.YEAR)) {
                DateFormat.getTimeInstance().format(date)
            } else {
                DateFormat.getDateInstance().format(date)
            }
        } else {
            DateFormat.getTimeInstance().format(Date())
        }
    }

    fun formatDate(
        dateLong: Long?
    ): String {
        return if (dateLong != null) {
            val date = Date(dateLong)

            // Check if is today
            val today = Calendar.Builder().build()
            today.time = Date()
            val check = Calendar.Builder().build()
            check.time = date

            if (today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR) && today.get(Calendar.YEAR) == check.get(Calendar.YEAR)) {
                DateFormat.getTimeInstance().format(date)
            } else {
                DateFormat.getDateInstance().format(date)
            }
        } else ""
    }


    // TASK SCREEN
    // Statuses
    private val _statuses: MutableStateFlow<List<StatusObj>> = MutableStateFlow(emptyList())
    val statuses = _statuses.asStateFlow()

    private fun getStatuses(noteId: Int) {
        _statuses.value = db.getManyByNoteStatuses(noteId)
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
        val data = if (statusId == 0) {
            val data = db.getManyByNoteTasks(noteId)
            var idx = 1
            for (i in data) {
                i.position = idx
                db.updateTask(i)
                idx++
            }
            data
        } else {
            db.getManyByNoteAndStatusTasks(noteId, statusId)
        }
        _tasks.value = data.reversed()
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
                if (event.status.id == 0) db.insertStatus(event.status) else db.updateStatus(event.status)
                updateTasksData(true)
            }
            is Event.DeleteStatus -> {
                if (_selectedNote.value != null) {
                    db.deleteStatus(event.status.id)
                    db.deleteManyByStatusTasks(event.status.id)
                    updateTasksData(true)
                }
            }
            // Task
            is Event.UpsertTask -> {
                if (event.task.id == 0) db.insertTask(event.task) else db.updateTask(event.task)
                // Set position from generated id ???
                if (event.task.id == 0) {
                    try {
                        val id = db.getLastTask()?.id
                        if (id != null) db.updateTask(event.task.copy(id = id, position = id))
                    } catch (_: Exception) {}
                }
                updateTasksData(false)
            }
            is Event.DeleteTask -> {
                db.deleteTask(event.task.id)
                updateTasksData(false)
            }
            is Event.ChangePos -> {
                // Is this good enough ???
                val targetPos = when(event.pos) {
                    Pos.Prev -> event.task.position - 1
                    Pos.Next -> event.task.position + 1
                }
                val targetTask = db.getByPositionTask(targetPos)
                if (targetTask != null) {
                    db.updateTask(targetTask.copy(position = event.task.position))
                }
                db.updateTask(event.task.copy(position = targetPos))
                updateTasksData(false)
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


    // DRIVE SYNC
    private val _isSyncNow = MutableStateFlow(false)
    val isSyncNow = _isSyncNow.asStateFlow()

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

//    fun tryDriveSync() {
//        if (java.io.File("token").isDirectory) driveSyncAuto()
//    }

    fun driveSyncAuto(
        before: () -> Unit = { _isSyncNow.value = true },
        after: () -> Unit = { _isSyncNow.value = false }
    ) {
        Thread {
            try {
                before()

                val appSettings = db.getSettings()
                val data = drive.getData()

                if (!appSettings.isNotesEdited) {
                    // Data not edited
                    if (data.modifiedTime != null) {
                        // Update local
                        db.importDB(data.data.toString())
                        db.updateReceived(data.modifiedTime)
                        getDbNotes("")
                        getAllTasks()
                        getAllStatuses()
                    } else {
                        // If drive empty -> send data
                        driveSyncManual(true)
                    }
                } else {
                    // Data edited
                    if (data.modifiedTime == appSettings.dataReceivedDate || data.modifiedTime == null) {
                        // Send data
                        driveSyncManual(true)
                    } else {
                        // Get user to choose
                        openSyncDialog(true)
                    }
                }
            } catch (_: Exception) {
            } finally {
                after()
            }
        }.start()
    }

    fun driveSyncManualThread(
        isExport: Boolean,
        before: () -> Unit = { _isSyncNow.value = true },
        after: () -> Unit = { _isSyncNow.value = false }
    ) {
        Thread {
            before()
            driveSyncManual(isExport)
            after()
        }.start()
    }

    private fun driveSyncManual(isExport: Boolean) {
        if (isExport) {
            // Send data
            drive.sendData(db.exportDB().toString())
            db.updateEdit(false)
        }
        // Get data
        val data = drive.getData()
        if (!isExport && data.modifiedTime != null) {
            // Update local
            db.importDB(data.data.toString())
            getDbNotes("")
            getAllTasks()
            getAllStatuses()
        }
        db.updateReceived(data.modifiedTime)
    }
}