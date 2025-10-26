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
import com.yurhel.alex.anotes.feature_board.ui.BoardScreen
import com.yurhel.alex.anotes.feature_board.ui.BoardViewModel

@Composable
fun Navigation(vm: MainViewModel) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = if (vm.noteCreatedDateFromWidget != "") {
            ScreenObj.Note.name
        } else {
            ScreenObj.Main.name
        },
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
                    vm.prepareNote(true)
                    nav.navigate(ScreenObj.Note.name)
                },
                openNoteClicked = {
                    vm.prepareNote(false)
                    if (vm.checkIfNoteHaveTasks(vm.selectedNote.value!!)) {
                        vm.updateTasksData(false)
                        nav.navigate(ScreenObj.Tasks.name)
                    } else if (vm.checkIfNoteIsDraw(vm.selectedNote.value!!.id)) {
                        nav.navigate(ScreenObj.Draw.name)
                    } else {
                        nav.navigate(ScreenObj.Note.name)
                    }
                },
                newDrawClicked = {
                    vm.prepareNote(true)
                    nav.navigate(ScreenObj.Draw.name)
                }
            )
        }

        composable(route = ScreenObj.Note.name) {
            NoteScreen(
                vm = vm,
                onBack = {
                    nav.navigate(ScreenObj.Main.name)
                },
                toTasks = {
                    vm.updateTasksData(false)
                    nav.navigate(ScreenObj.Tasks.name)
                }
            )
        }

        composable(route = ScreenObj.Tasks.name) {
            TasksScreen(
                vm = vm,
                onBack = {
                    nav.navigate(ScreenObj.Main.name)
                },
                toNote = {
                    nav.navigate(ScreenObj.Note.name)
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
    }
}