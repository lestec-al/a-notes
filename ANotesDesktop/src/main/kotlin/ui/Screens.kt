package ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

enum class Screens { Notes, Note }

@Composable
fun Screens(vm: MainViewModel) {
    val chosenScreen = remember { mutableStateOf(Screens.Notes) }

    if (chosenScreen.value == Screens.Notes) {
        NotesScreen(
            vm = vm,
            newNoteClicked = {
                // Open new note
                vm.editNote = null
                chosenScreen.value = Screens.Note
            },
            openNoteClicked = { note ->
                // Open existing note
                vm.editNote = note
                chosenScreen.value = Screens.Note
            },
        )
    } else if (chosenScreen.value == Screens.Note) {
        NoteScreen(
            vm = vm,
            onBack = { isActionDel, isForceRedirect ->
                if (!isForceRedirect) {
                    if (isActionDel) vm.deleteNote() else vm.saveNote()
                    vm.changeEditTexValue("") // clear editText for the next note appear correctly
                }
                chosenScreen.value = Screens.Notes
            }
        )
    }
}