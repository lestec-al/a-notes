package com.yurhel.alex.anotes.ui.screen_notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale as Scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.draw
import com.yurhel.alex.anotes.shared.empty_text
import com.yurhel.alex.anotes.shared.sync_collision
import com.yurhel.alex.anotes.shared.drive_data
import com.yurhel.alex.anotes.shared.local_data
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.data.Note
import com.yurhel.alex.anotes.shared.note
import com.yurhel.alex.anotes.shared.swipe_notes
import com.yurhel.alex.anotes.shared.tasks
import com.yurhel.alex.anotes.ui.MainViewModel
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
    toSettings: () -> Unit,
    onBack: () -> Unit
) {
    var notesScroll by remember { mutableStateOf(Pair(0,0)) }
    val scrollState = rememberLazyStaggeredGridState()
    LaunchedEffect(Unit) {
        vm.initNotesScreen() // To sep viewModel ???
        scrollState.scrollToItem(
            index = notesScroll.first,
            scrollOffset = notesScroll.second
        )
    }
    fun updateScrollItem(scrollState: LazyStaggeredGridState) {
        notesScroll = Pair(
            scrollState.firstVisibleItemIndex,
            scrollState.firstVisibleItemScrollOffset
        )
    }

    BackHandlerCustom(onBack)

    val widgetId = remember { vm.platform.getWidgetIdWhenCreated() }
    val notNeedChooseWidget = widgetId == 0
    val isGrid = vm.appSettingsView == "grid"

    CustomScaffold(
        floatingActionButton = {
            DropFloatingActionButton(
                listOf(
                    Triple(Res.string.swipe_notes, Icons.Outlined.Swipe) {
                        newNoteClicked(NoteType.Swipe)
                        updateScrollItem(scrollState)
                    },
                    Triple(Res.string.draw, Icons.Outlined.Brush) {
                        newNoteClicked(NoteType.Draw)
                        updateScrollItem(scrollState)
                    },
                    Triple(Res.string.tasks, Icons.AutoMirrored.Outlined.FormatListBulleted) {
                        newNoteClicked(NoteType.Tasks)
                        updateScrollItem(scrollState)
                    },
                    Triple(Res.string.note, Icons.Outlined.TextFields) {
                        newNoteClicked(NoteType.Note)
                        updateScrollItem(scrollState)
                    }
                )
            )
        },
        bottomBar = {
            if (notNeedChooseWidget) NotesBottomBar(vm, vm.appSettingsView, toSettings)
        }
    ) { bottomPadding, topPadding ->
        // Empty text
        if (vm.allNotes.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = stringResource(Res.string.empty_text))
            }
        }
        // Notes
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (isGrid) 2 else 1),
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
            items(items = vm.allNotes) { note: Note ->
                val img = vm.tryGetImage(note.id)
                val isDraw = note.type == NoteType.Draw.name
                val isSwipes = note.type == NoteType.Swipe.name
                val title = if (isSwipes) getSwipesTitle(note.text) else note.text
                val containerColor = if (isDraw) Color.White else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                Card(
                    onClick = {
                        if (notNeedChooseWidget) {
                            openExistingNoteClicked(note)
                            updateScrollItem(scrollState)
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
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = if (!isDraw) null else {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp, max = 350.dp)
                        .padding(5.dp)
                ) {
                    // Image
                    if (img != null) {
                        Image(
                            bitmap = img,
                            contentDescription = stringResource(Res.string.draw),
                            contentScale = Scale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(containerColor)
                                .clip(CardDefaults.shape)
                        )
                    }
                    // Normal text
                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 10,
                            color = if (isDraw) Color.Black else Color.Unspecified,
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
                    vm.allTasks.forEach { task ->
                        if (task.note == note.id) {
                            TaskCard(
                                task = task,
                                cardColor = Color.Transparent,
                                onClick = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp),
                                tasksTextPadding = 2,
                                statuses = vm.allStatuses,
                                onBackgroundColor = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
    AskDialog(
        onDismissRequest = vm::setSyncDialogVisibility,
        isVisible = vm.isSyncDialogOpen,
        infoText = Res.string.sync_collision,
        leftButton = Pair(Res.string.drive_data) {
            vm.syncData(SyncActionTypes.ManualImport)
            vm.setSyncDialogVisibility()
        },
        rightButton = Pair(Res.string.local_data) {
            vm.syncData(SyncActionTypes.ManualExport)
            vm.setSyncDialogVisibility()
        }
    )
    LocalSyncSheet(vm)
}