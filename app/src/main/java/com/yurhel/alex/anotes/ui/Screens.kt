package com.yurhel.alex.anotes.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun Screens(
    vm: MainViewModel,
    nav: NavHostController
) {

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
            MainScreen(
                vm = vm,
                openNoteClicked = {
                    if (vm.selectedNote.value != null && vm.selectedNote.value!!.withTasks) {
                        vm.updateTasksData(withStatuses = true, withNoteSave = false)
                        nav.navigate(ScreenObj.Tasks.name)
                    } else {
                        nav.navigate(ScreenObj.Note.name)
                    }
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
                    vm.updateTasksData(withStatuses = true, withNoteSave = false)
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
    }
}