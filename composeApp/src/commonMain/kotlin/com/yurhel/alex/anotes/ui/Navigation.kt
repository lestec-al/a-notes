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
import com.yurhel.alex.anotes.ui.feature_board.BoardScreen
import com.yurhel.alex.anotes.ui.feature_board.BoardViewModel
import com.yurhel.alex.anotes.ui.feature_swipes.SwipeNotesScreen
import com.yurhel.alex.anotes.ui.feature_swipes.SwipeNotesViewModel
import com.yurhel.alex.anotes.ui.utils.NoteType
import com.yurhel.alex.anotes.ui.utils.ScreenObj

@Composable
fun Navigation(vm: MainViewModel) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = ScreenObj.Main.name,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        modifier = Modifier.fillMaxSize()
    ) {

        composable(route = ScreenObj.Main.name) {
            NotesScreen(
                vm = vm,
                newNoteClicked = {
                    vm.prepareNote(it)
                    when (it) {
                        NoteType.Note.name -> nav.navigate(ScreenObj.Note.name)
                        NoteType.Tasks.name -> {
                            vm.updateTasksData(false)
                            nav.navigate(ScreenObj.Tasks.name)
                        }
                        NoteType.Draw.name -> nav.navigate(ScreenObj.Draw.name)
                        NoteType.Swipe.name -> nav.navigate(ScreenObj.Swipe.name)
                    }
                },
                openNoteClicked = {
                    vm.prepareNote(null)
                    when (vm.checkNoteType(vm.selectedNote.value!!)) {
                        NoteType.Note -> nav.navigate(ScreenObj.Note.name)
                        NoteType.Tasks -> {
                            vm.updateTasksData(false)
                            nav.navigate(ScreenObj.Tasks.name)
                        }
                        NoteType.Draw -> nav.navigate(ScreenObj.Draw.name)
                        NoteType.Swipe -> nav.navigate(ScreenObj.Swipe.name)
                    }
                }
            )
        }

        composable(route = ScreenObj.Note.name) {
            NoteScreen(
                vm = vm,
                onBack = {
                    nav.navigate(ScreenObj.Main.name)
                }
            )
        }

        composable(route = ScreenObj.Tasks.name) {
            TasksScreen(
                vm = vm,
                onBack = {
                    nav.navigate(ScreenObj.Main.name)
                }
            )
        }

        composable(route = ScreenObj.Draw.name) {
            BoardScreen(
                vm = vm,
                vmBoard = viewModel(factory = BoardViewModel.Factory(vm)),
                onBack = {
                    nav.navigate(ScreenObj.Main.name)
                }
            )
        }

        composable(route = ScreenObj.Swipe.name) {
            SwipeNotesScreen(
                vm = viewModel(factory = SwipeNotesViewModel.Factory(vm)),
                onBack = {
                    nav.navigate(ScreenObj.Main.name)
                }
            )
        }
    }
}