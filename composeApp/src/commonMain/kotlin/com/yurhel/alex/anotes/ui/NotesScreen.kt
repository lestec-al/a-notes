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
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.create
import anotes.composeapp.generated.resources.draw
import anotes.composeapp.generated.resources.empty_text
import anotes.composeapp.generated.resources.note
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.data.NoteObj
import com.yurhel.alex.anotes.ui.components.DropFloatingActionButton
import com.yurhel.alex.anotes.ui.components.MainBottomBar
import com.yurhel.alex.anotes.ui.components.SyncDialog
import com.yurhel.alex.anotes.ui.components.Task
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesScreen(
    vm: MainViewModel,
    newNoteClicked: () -> Unit,
    openNoteClicked: () -> Unit,
    newDrawClicked: () -> Unit
) {
    BackHandlerCustom(onBack = vm.callExit)

    val scrollState = rememberLazyStaggeredGridState()

    LaunchedEffect(Unit) {
        vm.getDbNotes("")
        vm.getNotesView()
        vm.getAllTasks()
        vm.getAllStatuses()
        scrollState.scrollToItem(
            index = vm.notesScreenSavedScroll.first,
            scrollOffset = vm.notesScreenSavedScroll.second
        )
    }

    val appSettingsView by vm.appSettingsView.collectAsState()
    val allNotes: List<NoteObj> by vm.allNotes.collectAsState()
    val allTasks by vm.allTasks.collectAsState()
    val allStatuses by vm.allStatuses.collectAsState()

    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // Check if no need to choosing widget (possible only on Android)
    val notNeedChooseWidget = vm.widgetIdWhenCreated == 0

    Scaffold(
        floatingActionButton = {
            DropFloatingActionButton(
                listOf(
                    // Add new note button
                    Pair(stringResource(Res.string.create) + " " + stringResource(Res.string.note)) {
                        vm.selectNote(null)
                        newNoteClicked()
                        vm.updateNotesScreenScrollItem(
                            Pair(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset)
                        )
                    },
                    // Add new drawing button
                    Pair(stringResource(Res.string.create) + " " + stringResource(Res.string.draw)) {
                        newDrawClicked()
                        vm.updateNotesScreenScrollItem(
                            Pair(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset)
                        )
                    }
                )
            )
        },
        bottomBar = {
            if (notNeedChooseWidget) MainBottomBar(vm, appSettingsView)
        }
    ) { paddingValues ->
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
                .padding(paddingValues)
                .padding(horizontal = 5.dp)
        ) {
            // Notes
            items(items = allNotes) { note: NoteObj ->
                val image = vm.tryGetImage(note.id)
                Card(
                    onClick = {
                        vm.selectNote(note)
                        if (notNeedChooseWidget) {
                            // Open existing note
                            openNoteClicked()
                            vm.updateNotesScreenScrollItem(
                                Pair(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset)
                            )
                        } else {
                            // Init widget
                            vm.callInitUpdateWidget(true, vm.widgetIdWhenCreated, note.dateCreate.toString(), note)
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (image != null) Color.White else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 60.dp,
                            max = 350.dp
                        )
                        .padding(5.dp)
                ) {
                    // Text
                    if (note.text.isNotEmpty()) {
                        Text(
                            text = note.text,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 10,
                            color = if (image != null) Color.Black else Color.Unspecified,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                    }
                    // Tasks for this note
                    for (task in allTasks) {
                        if (task.note == note.id) {
                            Task(
                                task = task,
                                cardColor = Color.Transparent,
                                onClick = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 5.dp),
                                tasksTextPadding = 2,
                                statuses = allStatuses,
                                onBackgroundColor = onBackgroundColor
                            )
                        }
                    }
                    // Try to draw an image
                    if (image != null) {
                        Image(
                            bitmap = image,
                            contentDescription = "note drawing",
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
    // Sync choose dialog
    val isSyncDialogOpen by vm.isSyncDialogOpen.collectAsState()
    SyncDialog(
        isVisible = isSyncDialogOpen,
        vm = vm
    )
}