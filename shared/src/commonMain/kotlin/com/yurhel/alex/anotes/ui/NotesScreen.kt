package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.draw
import com.yurhel.alex.anotes.shared.empty_text
import com.yurhel.alex.anotes.shared.sync_collision
import com.yurhel.alex.anotes.shared.sync_drive
import com.yurhel.alex.anotes.shared.sync_local
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.data.Note
import com.yurhel.alex.anotes.ui.components.CustomScaffold
import com.yurhel.alex.anotes.ui.components.DropFloatingActionButton
import com.yurhel.alex.anotes.ui.components.AskDialog
import com.yurhel.alex.anotes.ui.screen_tasks.components.TaskCard
import com.yurhel.alex.anotes.ui.screen_swipes.components.SwipeNotesCard
import com.yurhel.alex.anotes.ui.screen_swipes.utils.getSwipesTitle
import com.yurhel.alex.anotes.ui.screen_swipes.utils.importSwipesFromText
import com.yurhel.alex.anotes.ui.utils.NoteType
import com.yurhel.alex.anotes.ui.utils.SyncActionTypes
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesScreen(
    vm: MainViewModel,
    newNoteClicked: (type: NoteType) -> Unit,
    openExistingNoteClicked: (note: Note) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberLazyStaggeredGridState()
    LaunchedEffect(Unit) {
        vm.initNotesScreen()
        scrollState.scrollToItem(
            index = vm.notesScreenSavedScroll.first,
            scrollOffset = vm.notesScreenSavedScroll.second
        )
    }
    BackHandlerCustom(onBack = onBack)

    val appSettingsView by vm.appSettingsView.collectAsState()
    val allNotes: List<Note> by vm.allNotes.collectAsState()
    val allTasks by vm.allTasks.collectAsState()
    val allStatuses by vm.allStatuses.collectAsState()
    val isSyncDialogOpen by vm.isSyncDialogOpen.collectAsState()
    val widgetId = remember { vm.platform.getWidgetIdWhenCreated() }
    val notNeedChooseWidget = widgetId == 0

    CustomScaffold(
        floatingActionButton = {
            DropFloatingActionButton(
                vm.getNotesScreenDropMenuItems(scrollState, newNoteClicked)
            )
        },
        bottomBar = {
            if (notNeedChooseWidget) NotesBottomBar(vm, appSettingsView)
        }
    ) { bottomPadding, topPadding ->
        // Empty text
        if (allNotes.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = stringResource(Res.string.empty_text))
            }
        }
        // Notes - projects
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (appSettingsView == "grid") 2 else 1),
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .padding(horizontal = 5.dp)
        ) {
            // Status bar spacer
            item(span = StaggeredGridItemSpan.FullLine) {
                Spacer(Modifier.height(topPadding))
            }
            // Notes
            items(items = allNotes) { note: Note ->
                val img = vm.tryGetImage(note.id)
                val isSwipes = note.type == NoteType.Swipe.name
                val title = if (isSwipes) getSwipesTitle(note.text) else note.text

                Card(
                    onClick = {
                        if (notNeedChooseWidget) {
                            openExistingNoteClicked(note)
                            vm.updateNotesScreenScrollItem(scrollState)
                        } else {
                            vm.platform.callInitUpdateWidget(
                                isInitAction = true,
                                widgetId = widgetId,
                                noteCreated = note.dateCreate.toString(),
                                note = note,
                                db = vm.db
                            )
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (img != null) Color.White else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 350.dp)
                        .padding(5.dp)
                ) {
                    // Normal text
                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 10,
                            color = if (img != null) Color.Black else Color.Unspecified,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                    // Swipes
                    if (isSwipes) {
                        var leftColor = Color.Red
                        var rightColor = Color.Green
                        importSwipesFromText(note.text) { _, lColor, _, rColor ->
                            leftColor = lColor
                            rightColor = rColor
                        }.forEach {
                            SwipeNotesCard(
                                onClick = null,
                                onDragStopped = null,
                                leftColor = leftColor,
                                rightColor = rightColor,
                                obj = it
                            )
                        }
                    }
                    // Tasks for this note
                    allTasks.forEach { task ->
                        if (task.note == note.id) {
                            TaskCard(
                                task = task,
                                cardColor = Color.Transparent,
                                onClick = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp),
                                tasksTextPadding = 2,
                                statuses = allStatuses,
                                onBackgroundColor = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    // Try to draw an image
                    if (img != null) {
                        Image(
                            bitmap = img,
                            contentDescription = stringResource(Res.string.draw),
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
    AskDialog(
        onDismissRequest = vm::syncDialogVisibility,
        isVisible = isSyncDialogOpen,
        infoText = Res.string.sync_collision,
        leftButton = Pair(Res.string.sync_drive) {
            vm.syncData(SyncActionTypes.ManualImport)
            vm.syncDialogVisibility()
        },
        rightButton = Pair(Res.string.sync_local) {
            vm.syncData(SyncActionTypes.ManualExport)
            vm.syncDialogVisibility()
        }
    )
}