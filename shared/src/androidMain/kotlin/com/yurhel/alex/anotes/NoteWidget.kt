package com.yurhel.alex.anotes

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.R
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
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.yurhel.alex.anotes.data.LocalDB
import com.yurhel.alex.anotes.data.Note
import com.yurhel.alex.anotes.data.Status
import com.yurhel.alex.anotes.data.Task
import com.yurhel.alex.anotes.ui.screen_swipes.utils.SwipeTextPos
import com.yurhel.alex.anotes.ui.screen_swipes.utils.getSwipesTitle
import com.yurhel.alex.anotes.ui.screen_swipes.utils.importSwipesFromText
import com.yurhel.alex.anotes.ui.utils.NoteType
import db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteWidget : GlanceAppWidget() {

    override var stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        // Delete widget entry from db
        withContext(Dispatchers.Default) {
            LocalDB.getInstance(AndroidSqliteDriver(Database.Schema, context, "notes.db")).widget.deleteById(GlanceAppWidgetManager(context).getAppWidgetId(glanceId))
        }
    }

    private fun updateWidgetData(context: Context, noteId: Int): Triple<List<Status>, List<Task>, Note>? {
        val db = LocalDB.getInstance(AndroidSqliteDriver(Database.Schema, context, "notes.db"))
        return db.note.getById(noteId)?.let {
            Triple(
                db.status.getManyByNote(noteId),
                db.task.getManyByNote(noteId),
                it
            )
        }
    }

    @SuppressLint("RestrictedApi", "PrivateResource")
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running operations.
        provideContent {
            val noteId = currentState<Preferences>()[intPreferencesKey("noteId")] ?: 0
            var statuses: List<Status> by remember { mutableStateOf(emptyList()) }
            var tasks: List<Task> by remember { mutableStateOf(emptyList()) }
            var note: Note? by remember { mutableStateOf(null) }
            val isSwipes = note?.type == NoteType.Swipe.name
            val text = note?.text?.run { if (isSwipes) getSwipesTitle(this) else this } ?: ""

            LaunchedEffect(key1 = noteId) {
                launch(Dispatchers.Default) {
                    val res = updateWidgetData(context, noteId)
                    if (res != null) {
                        statuses = res.first
                        tasks = res.second
                        note = res.third
                    }
                }
            }

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(R.color.glance_colorBackground)
            ) {
                // Widget data
                LazyColumn {
                    // Text
                    item {
                        if (text.isNotEmpty()) {
                            Text(
                                text = text,
                                style = TextStyle(
                                    color = ColorProvider(R.color.glance_colorOnSurface),
                                    fontSize = 20.sp
                                ),
                                modifier = GlanceModifier
                                    .padding(10.dp, 0.dp, 10.dp, 0.dp)
                                    .fillMaxSize()
                            )
                        }
                    }
                    // Swipes
                    if (isSwipes) {
                        var leftColor = Color.Red
                        var rightColor = Color.Green
                        items(
                            items = importSwipesFromText(note?.text ?: "") { _, lColor, _, rColor ->
                                leftColor = lColor
                                rightColor = rColor
                            }
                        ) {
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
                                                    if (it.pos == SwipeTextPos.Left) {
                                                        leftColor
                                                    } else {
                                                        rightColor
                                                    }
                                                } catch (_: Exception) {
                                                    Color(
                                                        ColorProvider(R.color.glance_colorOnBackground)
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
                                    text = it.text,
                                    style = TextStyle(
                                        color = ColorProvider(R.color.glance_colorOnSurface),
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
                                            } catch (_: Exception) {
                                                Color(
                                                    ColorProvider(R.color.glance_colorOnBackground)
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
                                    color = ColorProvider(R.color.glance_colorOnSurface),
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
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    CircleIconButton(
                        imageProvider = ImageProvider(com.yurhel.alex.anotes.shared.R.drawable.ic_refresh),
                        contentDescription = "",
                        backgroundColor = null,
                        contentColor = GlanceTheme.colors.outline,
                        onClick = {
                            val res = updateWidgetData(context, noteId)
                            if (res != null) {
                                statuses = res.first
                                tasks = res.second
                                note = res.third
                            }
                        }
                    )
                }
            }
        }
    }
}