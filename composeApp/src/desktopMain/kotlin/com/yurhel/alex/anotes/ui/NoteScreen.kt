package com.yurhel.alex.anotes.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.edit_tasks
import com.yurhel.alex.anotes.ui.components.BottomAppBarNote
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun NoteScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    toTasks: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (vm.editText.value.isEmpty()) {
            vm.prepareNote(redirectToNotesScreen = onBack, redirectToTasksScreen = toTasks)
        }
    }
    val editText by vm.editText.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBarNote(
                    vm = vm,
                    coroutineScope = rememberCoroutineScope(),
                    onBack = onBack,
                    onBackButtonClick = {
                        vm.saveNote()
                        onBack()
                    },
                    onSecondButtonClick = {
                        vm.saveNote(true)
                        toTasks()
                    },
                    secondButtonIcon = Icons.Outlined.Menu,
                    secondButtonText = stringResource(Res.string.edit_tasks)
                )
            }
        ) { padding ->
            BasicTextField(
                value = editText,
                onValueChange = {
                    vm.changeEditTextValue(it)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp, 20.dp, 20.dp, 0.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}