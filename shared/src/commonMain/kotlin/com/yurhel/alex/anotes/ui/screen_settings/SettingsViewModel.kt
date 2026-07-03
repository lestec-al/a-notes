package com.yurhel.alex.anotes.ui.screen_settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.yurhel.alex.anotes.data.Status
import com.yurhel.alex.anotes.ui.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

class SettingsViewModel(private val vm: MainViewModel): ViewModel() {
    class Factory(private val vm: MainViewModel) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            SettingsViewModel(vm = vm) as T
    }

    fun openLink(it: String) = vm.platform.openLink(it)

    fun getAppVersion() = vm.platform.getAppVersion()

    fun copyToClipboard(it: String, clipboard: Clipboard) = viewModelScope.launch {
        vm.platform.copyToClipboard(it, clipboard)
    }


    var folders by mutableStateOf<List<Status>>(emptyList())
        private set

    var editedFolder by mutableStateOf<Status?>(null)
        private set

    private fun getDbFolders() = viewModelScope.launch(Dispatchers.Default) {
        val mainFolder = listOf(Status(title = vm.settings.getMainName(), color = 0, note = 0))
        val foldersDb = vm.db.status.getAll().filter { it.note == 0 }
        folders = mainFolder + foldersDb
        // Will not be needed when notesScreen (& noteScreen) will have a sep viewModel
        vm.getAllStatuses()
        vm.getMainFolderName()
    }

    fun onSaveFolder(name: String) = viewModelScope.launch {
        editedFolder.also {
            if (it != null) {
                if (it.id == 0) {
                    // Update name for Main predefined folder
                    vm.settings.setMainName(name)
                } else {
                    // Update
                    vm.db.status.update(it.copy(title = name))
                    // For sync
                    vm.settings.setIsNotesEdited(true)
                }
            } else {
                // Add
                vm.db.status.insert(Status(title = name, color = 0, note = 0))
                // For sync
                vm.settings.setIsNotesEdited(true)
            }
        }
        getDbFolders()
    }

    fun deleteFolder(f: Status) {
        vm.db.note.getAll().forEach {
            if (f.id == it.folder) {
                vm.db.note.update(it.copy(folder = 0))
            }
        }
        vm.db.status.delete(f.id)
        if (f.id == vm.chosenFolder) {
            vm.chooseViewFolder(0)
        }
        // For sync
        viewModelScope.launch { vm.settings.setIsNotesEdited(true) }
        getDbFolders()
    }

    fun updateEditedFolder(f: Status?) {
        editedFolder = f
    }


    var syncType by mutableStateOf<String?>(null)
        private set

    private fun getDbSyncType() = viewModelScope.launch {
        syncType = vm.settings.getSyncType()
    }

    fun chooseSyncType(t: String) = viewModelScope.launch {
        syncType = t
        vm.settings.setSyncType(t)
    }


    init {
        getDbFolders()
        getDbSyncType()
    }
}