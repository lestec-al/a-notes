package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: (isActionDel: Boolean, isForceRedirect: Boolean) -> Unit
) {
    LaunchedEffect(Unit) { vm.prepareNote() }
    val editText by vm.editText.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBar(modifier = Modifier.height(50.dp)) {
                    // Back button
                    IconButton(onClick = { onBack(false, false) }) {
                        Icon(Icons.Outlined.ArrowBack, "")
                    }
                    // Delete note
                    IconButton(onClick = { onBack(true, false) }) {
                        Icon(Icons.Outlined.Delete, "")
                    }
                    // ui.Note updated text
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .padding(0.dp, 0.dp, 15.dp, 10.dp)
                    ) {
                        Text(
                            text = "${updated}: ${vm.getNoteDate()}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        ) { padding ->
            // Edit text field
            CustomTextField(
                value = editText,
                onValueChange = { vm.changeEditTexValue(it) },
                mainPadding = padding
            )
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit = {},
    mainPadding: PaddingValues
) {
//    val scope = rememberCoroutineScope()
//    val yourBringIntoViewRequester = BringIntoViewRequester()

    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it) },
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
            .padding(mainPadding)
            .padding(20.dp, 20.dp, 20.dp, 0.dp)
            .verticalScroll(rememberScrollState())
//            .bringIntoViewRequester(yourBringIntoViewRequester)
//            .onFocusChanged {
//                if (it.isFocused) {
//                    scope.launch {
//                        delay(200)
//                        yourBringIntoViewRequester.bringIntoView()
//                    }
//                }
//            }
    )
}