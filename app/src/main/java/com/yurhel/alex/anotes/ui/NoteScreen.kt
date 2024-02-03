package com.yurhel.alex.anotes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: (isActionDel: Boolean, isForceRedirect: Boolean) -> Unit
) {
    // If note open not from widget
    if (vm.noteCreatedDateFromWidget == "") LaunchedEffect(Unit) { vm.prepareNote { onBack(false, true) } }

    BackHandler { onBack(false, false) }
    val editText by vm.editText.collectAsState()

    val scroll = rememberScrollState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBar(modifier = Modifier.height(50.dp)) {
                    // Delete note
                    IconButton(
                        modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 10.dp),
                        onClick = { onBack(true, false) }
                    ) {
                        Icon(Icons.Outlined.Delete, "")
                    }

                    // Note updated text
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp, 0.dp, 15.dp, 10.dp)
                    ) {
                        Text(
                            text = "${LocalContext.current.getString(R.string.updated)}: ${vm.getNoteDate()}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        ) { padding ->
            // Edit text field
            BasicTextField(
                value = editText,
                onValueChange = {
                    vm.changeEditTexValue(it)
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
                    .verticalScroll(scroll)
            )
        }
    }
}