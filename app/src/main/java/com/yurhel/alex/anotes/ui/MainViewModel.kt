package com.yurhel.alex.anotes.ui

import android.app.Application
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurhel.alex.anotes.data.DB
import com.yurhel.alex.anotes.data.Drive
import com.yurhel.alex.anotes.data.NoteObj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
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

    val editText = TextFieldState("")

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
        editText.edit {
            this.append(noteText ?: "")
            this.placeCursorBeforeCharAt(0)
        }
        origNoteText = noteText ?: ""
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
    }

    fun saveNote() {
        // Check if the note exists and if its value has changed
        val edit = editNote
        if (edit != null && origNoteText != editText.text.toString()) {
            // Update note
            db.updateNote(edit.id, editText.text.toString(), Date().time.toString())
            // ???
            db.updateEdit(true)
            // Update widget if it exist
            val widgetId = db.getWidgetIds(noteCreated = edit.dateCreate)
            if (widgetId != null) callInitUpdateWidget(false, widgetId.toInt(), edit.dateCreate, editText.text.toString())
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

    fun driveSyncAuto(
        before: () -> Unit = { _isSyncNow.value = true },
        after: () -> Unit = { _isSyncNow.value = false }
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                before()
                callTrySighIn()

                val appSettings = db.getSettings()
                val data = drive.getData()
                val driveData = data.first
                val dataModifiedTime = data.second

                if (!appSettings.isNotesEdited) {
                    // Data not edited
                    if (dataModifiedTime != null) {
                        // Update local
                        db.importDB(driveData.toString())
                        db.updateReceived(dataModifiedTime)
                        updateNotesFromDB("")
                    } else {
                        // If drive empty -> send data
                        driveSyncManual(true)
                    }
                } else {
                    // Data edited
                    if (dataModifiedTime == appSettings.dataReceivedDate || dataModifiedTime == null) {
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
        }
    }

    fun driveSyncManualThread(
        isExport: Boolean,
        before: () -> Unit = { _isSyncNow.value = true },
        after: () -> Unit = { _isSyncNow.value = false }
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            before()
            driveSyncManual(isExport)
            after()
        }
    }

    private fun driveSyncManual(isExport: Boolean) {
        if (isExport) {
            // Send data
            drive.sendData(db.exportDB().toString())
            db.updateEdit(false)
        }
        // Get data
        val data = drive.getData()
        if (!isExport && data.second != null) {
            // Update local
            db.importDB(data.first.toString())
            updateNotesFromDB("")
        }
        db.updateReceived(data.second)
    }
}