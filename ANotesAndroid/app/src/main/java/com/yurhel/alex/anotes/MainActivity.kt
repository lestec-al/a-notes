package com.yurhel.alex.anotes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.yurhel.alex.anotes.data.local.DB
import com.yurhel.alex.anotes.data.local.obj.WidgetObj
import com.yurhel.alex.anotes.ui.MainScreen
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.NoteScreen
import com.yurhel.alex.anotes.ui.ScreenObj
import com.yurhel.alex.anotes.ui.TasksScreen
import com.yurhel.alex.anotes.ui.theme.ANotesTheme
import com.yurhel.alex.anotes.ui.widget.NoteWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            DB::class.java,
            "notes.db"
        ).build()

        setContent {
            // Init viewModel
            val nav = rememberNavController()
            val vm: MainViewModel by viewModels {
                MainViewModel.Factory(
                    db = db,
                    callExit = {
                        finishAffinity()
                    },
                    // Widget stuff
                    widgetIdWhenCreated = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                    noteCreatedDateFromWidget = intent.getStringExtra("noteCreated") ?: "",
                    callInitUpdateWidget = { isInitAction, widgetId, noteCreated, note ->
                        lifecycleScope.launch(Dispatchers.Default) {
                            // Update widget
                            val glanceId = GlanceAppWidgetManager(this@MainActivity).getGlanceIdBy(widgetId)
                            updateAppWidgetState(this@MainActivity, glanceId) {
                                it[stringPreferencesKey("noteCreated")] = noteCreated
                                it[stringPreferencesKey("noteText")] = note.text
                                it[intPreferencesKey("noteId")] = note.id
                                it[booleanPreferencesKey("withTasks")] = note.withTasks
                            }
                            NoteWidget().update(this@MainActivity, glanceId)
                            // Initialize widget
                            if (isInitAction) {
                                // Log widget to DB
                                db.widget.insert(WidgetObj(widgetId = widgetId, noteCreated = noteCreated))
                                // Create the return intent, set it with the activity result, finish the activity
                                setResult(RESULT_OK, Intent())
                                finish()
                            }
                        }
                    }
                )
            }

            ANotesTheme {
                // SCREENS NAVIGATION
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
                                // When an app in opened from widget
                                // Enable normal app functionality after returning from note
                                if (vm.noteCreatedDateFromWidget != "") vm.noteCreatedDateFromWidget = ""

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
        }
    }
}