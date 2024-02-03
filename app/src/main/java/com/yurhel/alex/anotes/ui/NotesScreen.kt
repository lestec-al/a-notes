package com.yurhel.alex.anotes.ui

import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.data.NoteObj
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotesScreen(
    vm: MainViewModel,
    onBackButtonClicked: () -> Unit,
    newNoteClicked: () -> Unit,
    openNoteClicked: (NoteObj) -> Unit
) {
    BackHandler { onBackButtonClicked() }
    vm.updateNotesFromDB("")
    val searchText by vm.searchText.collectAsState()
    val appSettingsView by vm.appSettingsView.collectAsState()
    val allNotes: List<NoteObj> by vm.allNotes.collectAsState()
    val isSyncNow by vm.isSyncNow.collectAsState()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            bottomBar = {
                if (vm.widgetIdWhenCreated == AppWidgetManager.INVALID_APPWIDGET_ID) {
                    // Bottom bar
                    BottomAppBar(modifier = Modifier.height(50.dp)) {
                        // Add new note button
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = newNoteClicked
                        ) {
                            Icon(Icons.Default.Add, "")
                        }

                        // Change notes view button
                        IconButton(
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                            onClick = { vm.changeNotesView() }
                        ) {
                            Image(
                                painter = painterResource(if (appSettingsView == "grid") R.drawable.ic_list else R.drawable.ic_grid),
                                contentDescription = "",
                                colorFilter = ColorFilter.tint(LocalContentColor.current)
                            )
                        }

                        // Search
                        val interactionSource = remember { MutableInteractionSource() }
                        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .padding(5.dp, 5.dp, 15.dp, 5.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .drawBehind { drawRect(surfaceColor) }
                        ) {
                            Row {
                                // Edit text
                                BasicTextField(
                                    value = searchText,
                                    onValueChange = { vm.updateNotesFromDB(it) },
                                    modifier = Modifier
                                        .padding(10.dp, 0.dp)
                                        .fillMaxWidth(),
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                    ),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions.Default,
                                    keyboardActions = KeyboardActions.Default,
                                    interactionSource = interactionSource,
                                    singleLine = true
                                ) { innerTextField ->
                                    // Places text field with placeholder and appropriate bottom padding
                                    TextFieldDefaults.TextFieldDecorationBox(
                                        value = searchText,
                                        visualTransformation = VisualTransformation.None,
                                        innerTextField = innerTextField,
                                        placeholder = { Text(text = LocalContext.current.getString(R.string.search_text_hint)) },
                                        singleLine = true,
                                        enabled = true,
                                        isError = false,
                                        interactionSource = interactionSource,
                                        colors = TextFieldDefaults.textFieldColors(
                                            textColor = Color.Gray,
                                            disabledTextColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            disabledIndicatorColor = Color.Transparent
                                        ),
                                        contentPadding = PaddingValues(0.dp, 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            // Pull to refresh
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isSyncNow,
                onRefresh = { vm.driveSyncAuto() },
                refreshThreshold = 150.dp
            )

            // Empty text
            if (allNotes.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(text = LocalContext.current.getString(R.string.empty_text))
                }
            }

            Column(
                modifier = Modifier
                    .padding(PaddingValues(5.dp, paddingValues.calculateTopPadding(), 5.dp, 0.dp))
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                // Sync indicator
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .height(if (isSyncNow) 50.dp else (pullRefreshState.progress * 100).roundToInt().dp)
                ) {
                    if (isSyncNow) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(30.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = pullRefreshState.progress,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(30.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                    }
                }

                // All notes in grid/col lazy view
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (appSettingsView == "grid") if (isLandscape) 4 else 2 else 1),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp, 0.dp, 0.dp, 50.dp)
                ) {
                    items(items = allNotes, key = {it.id}) { note: NoteObj ->
                        Card(
                            onClick = { openNoteClicked(note) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(5.dp)
                        ) {
                            Text(
                                text = if (note.text.length > 300) note.text.substring(0, 301) else note.text,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Sync choose dialog
    val isSyncDialogOpen by vm.isSyncDialogOpen.collectAsState()
    if (isSyncDialogOpen) {
        Dialog(onDismissRequest = { vm.openSyncDialog(false) }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                // Title
                Text(
                    text = LocalContext.current.getString(R.string.sync_collision),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                // Buttons
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        vm.driveSyncManual(false)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = LocalContext.current.getString(R.string.sync_drive))
                    }
                    TextButton(onClick = {
                        vm.driveSyncManual(true)
                        vm.openSyncDialog(false)
                    }) {
                        Text(text = LocalContext.current.getString(R.string.sync_local))
                    }
                }
            }
        }
    }
}