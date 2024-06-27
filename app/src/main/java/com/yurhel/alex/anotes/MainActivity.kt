package com.yurhel.alex.anotes

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.yurhel.alex.anotes.data.local.DB
import com.yurhel.alex.anotes.data.local.obj.WidgetObj
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.Screens
import com.yurhel.alex.anotes.ui.theme.ANotesTheme
import com.yurhel.alex.anotes.ui.widget.NoteWidget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            DB::class.java,
            "notes.db"
        ).build()

        // For get status of keyboard
        // Fixing a bug with BasicTextField2, when keyboard not showed second time
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Init viewModel
            val intent = intent
            val vm: MainViewModel by viewModels {
                MainViewModel.Factory(
                    db = db,
                    callExit = {
                        finishAffinity()
                    },
                    callTrySighIn = {
                        // Google auth
                        if (GoogleSignIn.getLastSignedInAccount(this) == null) {
                            // SighIn
                            val signInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .requestScopes(Scope(Scopes.DRIVE_APPFOLDER))
                                .build()
                            val signInClient = GoogleSignIn.getClient(this, signInOption)
                            val task = signInClient.silentSignIn()
                            if (!task.isSuccessful) resultAuth.launch(signInClient.signInIntent)
                        }
                    },
                    // Widget stuff
                    widgetIdWhenCreated = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
                    noteCreatedDateFromWidget = intent.getStringExtra("noteCreated") ?: "",
                    callInitUpdateWidget = { isInitAction, widgetId, noteCreated, noteText ->
                        lifecycleScope.launch {
                            // Update widget
                            val glanceId = GlanceAppWidgetManager(this@MainActivity).getGlanceIdBy(widgetId)
                            updateAppWidgetState(this@MainActivity, glanceId) {
                                it[stringPreferencesKey("noteCreated")] = noteCreated
                                it[stringPreferencesKey("noteText")] = noteText
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
                Screens(vm = vm, nav = rememberNavController())
            }
        }
    }

    private val resultAuth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = result.data
        if (intent != null) GoogleSignIn.getSignedInAccountFromIntent(intent)
    }
}