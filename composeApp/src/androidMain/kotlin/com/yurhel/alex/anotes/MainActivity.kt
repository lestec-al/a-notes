package com.yurhel.alex.anotes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.Drive
import com.yurhel.alex.anotes.data.WidgetObj
import com.yurhel.alex.anotes.data.getSqlDriver
import com.yurhel.alex.anotes.ui.ANotesTheme
import com.yurhel.alex.anotes.ui.DriveUtils
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import com.yurhel.alex.anotes.ui.SyncActionTypes
import com.yurhel.alex.anotes.widget.NoteWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = LocalDB.getInstance(getSqlDriver(applicationContext))

        setContent {
            val vm: MainViewModel by viewModels {
                MainViewModel.Factory(
                    db = db,
                    formatDate = { dateLong ->
                        if (DateUtils.isToday(dateLong)) {
                            DateFormat.getTimeFormat(applicationContext).format(Date(dateLong))
                        } else {
                            DateFormat.getMediumDateFormat(applicationContext).format(Date(dateLong))
                        }
                    },
                    syncData = { syncActionType, vm ->
                        when (syncActionType) {
                            SyncActionTypes.Auto -> {
                                DriveUtils.getInstance(vm, Drive.getInstance()).driveSyncAuto(this)
                            }
                            SyncActionTypes.ManualExport -> {
                                DriveUtils.getInstance(vm, Drive.getInstance()).driveSyncManualThread(true, this)
                            }
                            SyncActionTypes.ManualImport -> {
                                DriveUtils.getInstance(vm, Drive.getInstance()).driveSyncManualThread(false, this)
                            }
                        }
                    },
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
                                db.insertWidget(WidgetObj(widgetId = widgetId, noteCreated = noteCreated))
                                // Create the return intent, set it with the activity result, finish the activity
                                setResult(RESULT_OK, Intent())
                                finish()
                            }
                        }
                    }
                )
            }

            ANotesTheme {
                Navigation(vm = vm)
            }
        }
    }
}