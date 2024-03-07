package ui

import data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class MainViewModel {

    // Init variables
    private val db = DB()
    private val drive = Drive()
    var editNote: NoteObj? = null


    // NOTES SCREEN
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
            if (sort == "dateUpdate") it.dateUpdate.toLong()
            else it.dateCreate.toLong()
        }.let {
            if (sortArrow == "ascending") it.reversed() else it
        }
    }


    // NOTE SCREEN
    private var origNoteText = ""

    private val _editText = MutableStateFlow("")
    val editText = _editText.asStateFlow()

    /**
     * Creates new note or opens existed.
     **/
    fun prepareNote() {
        val noteText = if (editNote == null) {
            // New note is opened. Create new note
            db.createNote("", Date().time.toString())
            // ???
            db.updateEdit(true)
            // Get new note
            editNote = db.getLastNote()
            ""
        } else {
            // Existed note open
            editNote!!.text
        }
        _editText.value = noteText
        origNoteText = noteText
    }

    fun changeEditTexValue(text: String) {
        _editText.value = text
    }

    fun getNoteDate(): String {
        return if (editNote != null) {
            val date = Date(editNote!!.dateUpdate.toLong())

            // Check if is today
            val today = Calendar.Builder().build()
            today.time = Date()
            val check = Calendar.Builder().build()
            check.time = date

            if (today.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR) && today.get(Calendar.YEAR) == check.get(Calendar.YEAR))
                DateFormat.getTimeInstance().format(date)
            else
                DateFormat.getDateInstance().format(date)
        } else {
            DateFormat.getTimeInstance().format(Date())
        }
    }

    fun deleteNote() {
        db.deleteNote(editNote?.id ?: -1)
        editNote = null
        // ???
        db.updateEdit(true)
        // Sync note
        tryDriveSync()
    }

    fun saveNote() {
        // Check if the note exists and if its value has changed
        if (editNote != null && origNoteText != _editText.value) {
            // Update note
            db.updateNote(editNote!!.id, _editText.value, Date().time.toString())
            // ???
            db.updateEdit(true)
            // Sync note
            tryDriveSync()
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

    fun tryDriveSync() {
        if (java.io.File("token").isDirectory) driveSyncAuto()
    }

    fun driveSyncAuto(
        before: () -> Unit = { _isSyncNow.value = true },
        after: () -> Unit = { _isSyncNow.value = false }
    ) {
        before()
        Thread {
            val appSettings = db.getSettings()
            val data = drive.getData()
            val driveData = data.first
            val dataModifiedTime = data.second

            if (!appSettings.isNotesEdited) {
                // Data not edited
                if (driveData.length() > 0) {
                    // Update local
                    db.importDB(driveData.toString())
                    db.updateReceived(dataModifiedTime)
                    getDbNotes("")
                } else {
                    // If drive empty -> send data
                    driveSyncManual(isExport = true)
                }
            } else {
                // Data edited
                val dataReceived = appSettings.dataReceivedDate != null
                val serverDataNotChanged = dataModifiedTime == null || dataModifiedTime == appSettings.dataReceivedDate

                if ((dataReceived && serverDataNotChanged) || (!dataReceived && driveData.length() < 1)) {
                    // Send data
                    driveSyncManual(isExport = true)
                } else {
                    // Get user to choose
                    openSyncDialog(true)
                }
            }
            after()
        }.start()
    }

    fun driveSyncManualThread(
        isExport: Boolean,
        before: () -> Unit = { _isSyncNow.value = true },
        after: () -> Unit = { _isSyncNow.value = false }
    ) {
        before()
        Thread {
            driveSyncManual(isExport = isExport)
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
        if (!isExport && data.first.length() > 0) {
            // Update local
            db.importDB(data.first.toString())
            getDbNotes("")
        }
        db.updateReceived(data.second)
    }
}