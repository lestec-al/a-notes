package com.yurhel.alex.anotes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.SettingsDataStore
import com.yurhel.alex.anotes.data.WidgetObj
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = LocalDB.getInstance(getSqlDriver())

        setContent {
            val vm: MainViewModel by viewModels {
                MainViewModel.Factory(
                    db = db,
                    settings = SettingsDataStore.getInstance { createDataStorePlatform() },
                    callExit = ::finishAffinity,
                    widgetIdWhenCreated = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                    callInitUpdateWidget = { isInitAction, widgetId, noteCreated, note ->
                        lifecycleScope.launch(Dispatchers.Default) {
                            // Update widget
                            val glanceId = GlanceAppWidgetManager(this@MainActivity).getGlanceIdBy(widgetId)
                            updateAppWidgetState(this@MainActivity, glanceId) {
                                it[stringPreferencesKey("noteCreated")] = noteCreated
                                it[stringPreferencesKey("noteText")] = note.text
                                it[intPreferencesKey("noteId")] = note.id
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
            Navigation(vm)
        }
    }
}