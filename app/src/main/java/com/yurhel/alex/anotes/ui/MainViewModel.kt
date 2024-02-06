package com.yurhel.alex.anotes.ui

import android.app.Application
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.yurhel.alex.anotes.data.DB
import com.yurhel.alex.anotes.data.Drive
import com.yurhel.alex.anotes.data.NoteObj
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class MainViewModel(
    private val app: Application,
    val callTrySighIn: () -> Unit,
    var widgetIdWhenCreated: Int,
    var noteCreatedDateFromWidget: String,
    val callInitUpdateWidget: (isInitAction: Boolean, widgetId: Int, noteCreated: String, noteText: String) -> Unit
) : AndroidViewModel(app) {

    class Factory(
        private val application: Application,
        private val callTrySighIn: () -> Unit,
        private var widgetIdWhenCreated: Int,
        private var noteCreatedDateFromWidget: String,
        private val callInitUpdateWidget: (isInitAction: Boolean, widgetId: Int, noteCreated: String, noteText: String) -> Unit
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(application, callTrySighIn, widgetIdWhenCreated, noteCreatedDateFromWidget, callInitUpdateWidget) as T
    }

    // Init variables
    private val db = DB(app.applicationContext)
    private val drive = Drive(app.applicationContext)
    var editNote: NoteObj? = null

    // NOTES SCREEN
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _appSettingsView = MutableStateFlow(db.getSettings().viewMode)
    val appSettingsView = _appSettingsView.asStateFlow()

    fun changeNotesView() {
        val value = if (db.getSettings().viewMode == "grid") "col" else "grid"
        _appSettingsView.value = value
        db.updateViewMode(value)
    }

    private val _allNotes = MutableStateFlow(db.getNotes())
    val allNotes = _allNotes.asStateFlow()

    fun updateNotesFromDB(
        query: String,
        sort: String = "dateUpdate", // dateCreate, dateUpdate
        sortArrow: String = "ascending" // descending, ascending
    ) {
        _searchText.value = query.replace("\n", "")

        if (sortArrow == "ascending") {
            _allNotes.value = db.getNotes(query).sortedBy {
                if (sort == "dateUpdate") it.dateUpdate.toLong()
                else it.dateCreate.toLong()
            }.reversed()
        } else {
            _allNotes.value = db.getNotes(query).sortedBy {
                if (sort == "dateUpdate") it.dateUpdate.toLong()
                else it.dateCreate.toLong()
            }
        }
    }


    // NOTE SCREEN
    private var origNoteText = ""

    private val _editText = MutableStateFlow("")
    val editText = _editText.asStateFlow()

    /**
     * Creates new note or opens existed. Also if screen open from widget but note deleted (from DB) - redirect to notes screen.
     **/
    suspend fun prepareNote(redirectToNotesScreen: () -> Unit) {
        val noteText: String? = if (noteCreatedDateFromWidget != "") {
            // Note open from widget
            val noteFromWidget = db.getNote(created = noteCreatedDateFromWidget)
            if (noteFromWidget == null) {
                // Note doesn't exist. Open widget settings
                val widgetId = db.getWidgetIds(noteCreated = noteCreatedDateFromWidget)
                if (widgetId != null) {
                    widgetIdWhenCreated = widgetId.toInt()
                    delay(500L)
                    redirectToNotesScreen()
                }
                null
            } else {
                // Note exist
                editNote = noteFromWidget
                noteFromWidget.text
            }
        } else if (editNote == null) {
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
        _editText.value = noteText ?: ""
        origNoteText = noteText ?: ""
    }

    fun changeEditTexValue(text: String) {
        _editText.value = text
    }

    fun getNoteDate(): String {
        return if (editNote != null) {
            val dateLong = editNote!!.dateUpdate.toLong()
            if (DateUtils.isToday(dateLong)) DateFormat.getTimeFormat(app.baseContext).format(Date(dateLong))
            else DateFormat.getMediumDateFormat(app.baseContext).format(Date(dateLong))
        } else {
            DateFormat.getTimeFormat(app.baseContext).format(Date())
        }
    }

    fun deleteNote() {
        db.deleteNote(editNote?.id ?: -1)
        editNote = null
        // ???
        db.updateEdit(true)
        // Sync note
        tryHiddenDriveSync {}
    }

    fun saveNote() {
        // Check if the note exists and if its value has changed
        val edit = editNote
        if (edit != null && origNoteText != _editText.value) {
            // Update note
            db.updateNote(edit.id, _editText.value, Date().time.toString())
            // ???
            db.updateEdit(true)
            // Update widget if it exist
            val widgetId = db.getWidgetIds(noteCreated = edit.dateCreate)
            if (widgetId != null) callInitUpdateWidget(false, widgetId.toInt(), edit.dateCreate, _editText.value)
            // Sync note
            tryHiddenDriveSync {}
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

    fun tryHiddenDriveSync(after: () -> Unit) {
        if (GoogleSignIn.getLastSignedInAccount(app.applicationContext) != null) driveSyncAuto(before = {}, after = after)
        else after()
    }

    fun driveSyncAuto(
        before: () -> Unit = { _isSyncNow.value = true },
        after: () -> Unit = { _isSyncNow.value = false }
    ) {
        before()
        callTrySighIn()
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
                    updateNotesFromDB("")
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
            updateNotesFromDB("")
        }
        db.updateReceived(data.second)
    }
}