package com.yurhel.alex.anotes.ui

import android.appwidget.AppWidgetManager
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

enum class Screens { Notes, Note }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Screens(
    vm: MainViewModel,
    callExit: () -> Unit,
    nav: NavHostController
) {

    NavHost(
        navController = nav,
        startDestination = if (vm.noteCreatedDateFromWidget != "") Screens.Note.name else Screens.Notes.name,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        modifier = Modifier.fillMaxSize()
    ) {

        composable(route = Screens.Notes.name) {
            NotesScreen(
                vm = vm,
                onBackButtonClicked = callExit,
                newNoteClicked = {
                    // Open new note
                    vm.editNote = null
                    nav.navigate(Screens.Note.name)
                },
                openNoteClicked = { note ->
                    if (vm.widgetIdWhenCreated == AppWidgetManager.INVALID_APPWIDGET_ID) {
                        // Open existing note
                        vm.editNote = note
                        nav.navigate(Screens.Note.name)
                    } else {
                        // Init widget
                        vm.callInitUpdateWidget(true, vm.widgetIdWhenCreated, note.dateCreate, note.text)
                    }
                },
            )
        }

        composable(route = Screens.Note.name) {
            NoteScreen(
                vm = vm,
                onBack = { isActionDel, isForceRedirect ->
                    if (isForceRedirect) {
                        nav.navigate(Screens.Notes.name)
                    } else {
                        if (isActionDel) vm.deleteNote() else vm.saveNote()
                        vm.editText.clearText() // clear editText for the next note appear correctly
                        if (vm.noteCreatedDateFromWidget != "") callExit() else nav.navigate(Screens.Notes.name)
                    }
                }
            )
        }
    }
}