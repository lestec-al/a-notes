package com.yurhel.alex.anotes.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun Screens(vm: MainViewModel) {
    val chosenScreen = remember { mutableStateOf(ScreenObj.Main) }

    when(chosenScreen.value) {
        ScreenObj.Main -> {
            MainScreen(
                vm = vm,
                openNoteClicked = {
                    if (vm.selectedNote.value != null && vm.selectedNote.value!!.withTasks) {
                        vm.updateTasksData(withStatuses = true, withNoteSave = false)
                        chosenScreen.value = ScreenObj.Tasks
                    } else {
                        chosenScreen.value = ScreenObj.Note
                    }
                }
            )
        }
        ScreenObj.Note -> {
            NoteScreen(
                vm = vm,
                onBack = {
                    chosenScreen.value = ScreenObj.Main
                },
                toTasks = {
                    vm.updateTasksData(withStatuses = true, withNoteSave = false)
                    chosenScreen.value = ScreenObj.Tasks
                }
            )
        }
        ScreenObj.Tasks -> {
            TasksScreen(
                vm = vm,
                onBack = {
                    chosenScreen.value = ScreenObj.Main
                },
                toNote = {
                    chosenScreen.value = ScreenObj.Note
                }
            )
        }
    }
}