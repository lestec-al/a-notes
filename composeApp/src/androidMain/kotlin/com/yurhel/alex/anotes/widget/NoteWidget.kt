package com.yurhel.alex.anotes.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.yurhel.alex.anotes.MainActivity
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.StatusObj
import com.yurhel.alex.anotes.data.TasksObj
import com.yurhel.alex.anotes.data.getSqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteWidget : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        // Delete widget entry from db
        withContext(Dispatchers.Default) {
            LocalDB.getInstance(getSqlDriver(context)).deleteByIdWidget(GlanceAppWidgetManager(context).getAppWidgetId(glanceId))
        }
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running operations.
        provideContent {
            val noteCreated = currentState<Preferences>()[stringPreferencesKey("noteCreated")] ?: ""
            val noteText = currentState<Preferences>()[stringPreferencesKey("noteText")] ?: ""
            val noteId = currentState<Preferences>()[intPreferencesKey("noteId")] ?: 0

            var statuses: List<StatusObj> by remember { mutableStateOf(emptyList()) }
            var tasks: List<TasksObj> by remember { mutableStateOf(emptyList()) }

            LaunchedEffect(key1 = noteText) {
                launch(Dispatchers.Default) {
                    val db = LocalDB.getInstance(getSqlDriver(context))
                    statuses = db.getManyByNoteStatuses(noteId)
                    tasks = db.getManyByNoteTasks(noteId).reversed()
                }
            }

            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(androidx.glance.R.color.glance_colorBackground)
            ) {
                // Widget data
                LazyColumn {
                    // Text
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
                        )
                    }
                    // Tasks for this note
                    items(items = tasks) { task ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp),
                        ) {
                            // Color indicator
                            Box(
                                contentAlignment = Alignment.BottomCenter,
                                modifier = GlanceModifier
                                    .height(22.dp)
                                    .width(10.dp)
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .background(
                                            try {
                                                Color(statuses.find { it.id == task.status }!!.color)
                                            } catch (e: Exception) {
                                                Color(
                                                    ColorProvider(androidx.glance.R.color.glance_colorOnBackground)
                                                        .getColor(context)
                                                        .toArgb()
                                                )
                                            }
                                        )
                                        .cornerRadius(5.dp)
                                        .size(10.dp)
                                ) {}
                            }
                            // Description
                            Text(
                                text = task.description,
                                style = TextStyle(
                                    color = ColorProvider(androidx.glance.R.color.glance_colorOnSurface),
                                    fontSize = 20.sp
                                ),
                                modifier = GlanceModifier.padding(
                                    horizontal = 5.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }
                // Open note/task button
                CircleIconButton(
                    imageProvider = ImageProvider(R.drawable.ic_edit),
                    contentDescription = "",
                    backgroundColor = null,
                    contentColor = GlanceTheme.colors.outline,
                    onClick = {
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