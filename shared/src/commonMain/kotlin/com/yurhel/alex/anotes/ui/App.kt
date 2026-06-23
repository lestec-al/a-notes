package com.yurhel.alex.anotes.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yurhel.alex.anotes.SetStatusBarColor
import com.yurhel.alex.anotes.ui.screen_board.BoardScreen
import com.yurhel.alex.anotes.ui.screen_board.BoardViewModel
import com.yurhel.alex.anotes.ui.screen_note.NoteScreen
import com.yurhel.alex.anotes.ui.screen_notes.NotesScreen
import com.yurhel.alex.anotes.ui.screen_swipes.SwipeNotesScreen
import com.yurhel.alex.anotes.ui.screen_swipes.SwipeNotesViewModel
import com.yurhel.alex.anotes.ui.screen_tasks.TasksScreen
import com.yurhel.alex.anotes.ui.screen_tasks.TasksViewModel
import com.yurhel.alex.anotes.ui.theme.ANotesTheme
import com.yurhel.alex.anotes.ui.utils.NoteType

@Composable
fun App(vm: MainViewModel) {
    val nav = rememberNavController()
    ANotesTheme(darkTheme = vm.darkTheme) {
        SetStatusBarColor(null, vm.darkTheme)
        NavHost(
            navController = nav,
            startDestination = "notes",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
            modifier = Modifier.fillMaxSize()
        ) {
            composable(route = "notes") {
                NotesScreen(
                    vm = vm,
                    newNoteClicked = {
                        vm.prepareNote(it)
                        nav.navigate(it.name)
                    },
                    openExistingNoteClicked = {
                        vm.selectNote(it)
                        vm.prepareNote(null)
                        nav.navigate(vm.checkNoteType(vm.selectedNote!!).name)
                    },
                    onBack = vm.platform::callExit
                )
            }
            composable(route = NoteType.Note.name) {
                NoteScreen(
                    vm = vm,
                    onBack = nav::popBackStack
                )
            }
            composable(route = NoteType.Tasks.name) {
                TasksScreen(
                    vm = viewModel(factory = TasksViewModel.Factory(vm)),
                    onBack = nav::popBackStack
                )
            }
            composable(route = NoteType.Draw.name) {
                BoardScreen(
                    vm = viewModel(factory = BoardViewModel.Factory(vm)),
                    onBack = nav::popBackStack
                )
            }
            composable(route = NoteType.Swipe.name) {
                SwipeNotesScreen(
                    vm = viewModel(factory = SwipeNotesViewModel.Factory(vm)),
                    onBack = nav::popBackStack
                )
            }
        }
    }
}