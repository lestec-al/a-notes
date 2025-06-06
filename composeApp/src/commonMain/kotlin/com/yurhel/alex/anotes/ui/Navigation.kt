package com.yurhel.alex.anotes.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
                    nav.navigate(ScreenObj.Note.name)
                },
                openNoteClicked = {
                    if (vm.checkIfNoteHaveTasks(vm.selectedNote.value!!)) {
                        vm.updateTasksData(false)
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
                    // When an app in opened from widget (only on Android)
                    // Enable normal app functionality after returning from note
                    if (vm.noteCreatedDateFromWidget != "") vm.noteCreatedDateFromWidget = ""
                    if (it) vm.updateNotesScreenScrollItem(Pair(0,0))
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
                    // When an app in opened from widget
                    // Enable normal app functionality after returning from note
                    if (vm.noteCreatedDateFromWidget != "") vm.noteCreatedDateFromWidget = ""

                    nav.navigate(ScreenObj.Main.name)
                },
                toNote = {
                    nav.navigate(ScreenObj.Note.name)
                }
            )
        }
    }
}