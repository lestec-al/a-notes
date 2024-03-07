package com.yurhel.alex.anotes.ui

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yurhel.alex.anotes.data.DB
import com.yurhel.alex.anotes.data.NoteObj
import com.yurhel.alex.anotes.data.SettingsObj
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
class MainViewModel(
    val db: DB,
    val callTrySighIn: () -> Unit,
    var widgetIdWhenCreated: Int,
    var noteCreatedDateFromWidget: String,
    val callUpdateWidget: (isInitAction: Boolean, widgetId: Int, noteCreated: String, noteText: String) -> Unit
) : ViewModel() {

    class Factory(
        private val db: DB,
        private val callTrySighIn: () -> Unit,
        private var widgetIdWhenCreated: Int,
        private var noteCreatedDateFromWidget: String,
        private val callInitUpdateWidget: (isInitAction: Boolean, widgetId: Int, noteCreated: String, noteText: String) -> Unit
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MainViewModel(db, callTrySighIn, widgetIdWhenCreated, noteCreatedDateFromWidget, callInitUpdateWidget) as T
    }

    var editNote: NoteObj? = null

    init {
        // Init settings
        viewModelScope.launch(Dispatchers.Default) {
            if (db.settings.getS() == null) db.settings.upsert(SettingsObj())
        }
    }

    // NOTES SCREEN
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _appSettingsView: MutableStateFlow<String> = MutableStateFlow("col").also {
        viewModelScope.launch(Dispatchers.Default) {
            it.value = db.settings.getS()?.viewMode ?: ""
        }
    }
    val appSettingsView = _appSettingsView.asStateFlow()

    fun changeNotesView() {
        viewModelScope.launch(Dispatchers.Default) {
            val settings = db.settings.getS()
            _appSettingsView.value = if (settings != null) {
                val value = if (settings.viewMode == "grid") "col" else "grid"
                db.settings.upsert(settings.copy(viewMode = value))
                value
            } else {
                val value = if (_appSettingsView.value == "grid") "col" else "grid"
                db.settings.upsert(SettingsObj(viewMode = value))
                value
            }
        }
    }

    private val _allNotes: MutableStateFlow<List<NoteObj>> = MutableStateFlow<List<NoteObj>>(emptyList()).also { getDbNotes("") }
    val allNotes = _allNotes.asStateFlow()

    fun getDbNotes(
        query: String,
        sort: String = "dateUpdate", // dateCreate, dateUpdate
        sortArrow: String = "ascending" // descending, ascending
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            _searchText.value = query.replace("\n", "")

            _allNotes.value = db.note.getByQuery(query).sortedBy {
                if (sort == "dateUpdate") it.dateUpdate.toLong()
                else it.dateCreate.toLong()
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
    suspend fun prepareNote(redirectToNotesScreen: () -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            val noteText: String? = if (noteCreatedDateFromWidget != "") {
                // Note open from widget
                val noteFromWidget = db.note.getByCreated(created = noteCreatedDateFromWidget)
                if (noteFromWidget == null) {
                    // Note doesn't exist. Open widget settings
                    val widgetId = db.widgets.getByCreated(noteCreated = noteCreatedDateFromWidget)?.widgetId
                    if (widgetId != null) {
                        widgetIdWhenCreated = widgetId.toInt()
                        delay(500L)
                        launch(Dispatchers.Main) { redirectToNotesScreen() }
                    }
                    null
                } else {
                    // Note exist
                    editNote = noteFromWidget
                    noteFromWidget.text
                }
            } else if (editNote == null) {
                // New note is opened. Create new note
                val date = Date().time.toString()
                db.note.upsert(NoteObj(text = "", dateCreate = date, dateUpdate = date))
                // ???
                db.settings.upsert(db.settings.getS()?.copy(isNotesEdited = true) ?: SettingsObj(isNotesEdited = true))
                // Get new note
                editNote = db.note.getLast()
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
    }

    fun getNoteDate(context: Context): String {
        return if (editNote != null) {
            val dateLong = editNote!!.dateUpdate.toLong()
            if (DateUtils.isToday(dateLong)) DateFormat.getTimeFormat(context).format(Date(dateLong))
            else DateFormat.getMediumDateFormat(context).format(Date(dateLong))
        } else {
            DateFormat.getTimeFormat(context).format(Date())
        }
    }

    fun deleteNote() {
        viewModelScope.launch(Dispatchers.Default) {
            if (editNote != null) {
                db.note.delete(editNote!!)
                editNote = null
                // ???
                db.settings.upsert(db.settings.getS()?.copy(isNotesEdited = true) ?: SettingsObj(isNotesEdited = true))
            }
        }
    }

    fun saveNote() {
        val edit = editNote
        val editTextStr = editText.text.toString()

        viewModelScope.launch(Dispatchers.Default) {
            // Check if the note exists and if its value has changed
            if (edit != null && origNoteText != editTextStr) {
                // Update note
                db.note.upsert(edit.copy(text = editTextStr, dateUpdate = Date().time.toString()))
                // ???
                db.settings.upsert(db.settings.getS()?.copy(isNotesEdited = true) ?: SettingsObj(isNotesEdited = true))
                // Update widget if it exist
                val widgetId = db.widgets.getByCreated(noteCreated = edit.dateCreate)?.widgetId
                if (widgetId != null) callUpdateWidget(false, widgetId.toInt(), edit.dateCreate, editTextStr)
            }
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
}