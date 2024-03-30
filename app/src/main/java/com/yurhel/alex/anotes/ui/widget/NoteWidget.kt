package com.yurhel.alex.anotes.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.yurhel.alex.anotes.MainActivity
import com.yurhel.alex.anotes.data.local.DB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteWidget : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        // Delete widget entry from db
        withContext(Dispatchers.Default) {
            val db = Room.databaseBuilder(
                context,
                DB::class.java,
                "notes.db"
            ).build()
            db.widget.deleteById(widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId))
            db.close()
        }
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running operations.

        provideContent {
            val noteCreated = currentState<Preferences>()[stringPreferencesKey("noteCreated")] ?: ""
            val noteText = currentState<Preferences>()[stringPreferencesKey("noteText")] ?: "" // ???

            LazyColumn(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(androidx.glance.R.color.glance_colorBackground)
            ) {
                item {
                    Text(
                        text = noteText,
                        style = TextStyle(
                            color = ColorProvider(androidx.glance.R.color.glance_colorOnSurface),
                            fontSize = 20.sp
                        ),
                        modifier = GlanceModifier
                            .padding(10.dp, 0.dp, 10.dp, 0.dp)
                            .fillMaxSize()
                            .clickable {
                                Intent(context, MainActivity::class.java)
                                    .putExtra("noteCreated", noteCreated)
                                    .apply{
                                        flags = FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(this)
                                    }
                            }
                    )
                }
            }
        }
    }
}