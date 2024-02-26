package com.yurhel.alex.anotes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: (isActionDel: Boolean, isForceRedirect: Boolean) -> Unit
) {
    LaunchedEffect(Unit) { vm.prepareNote { onBack(false, true) } }
    BackHandler { onBack(false, false) }

    val editText = remember { vm.editText }
    val scroll = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var globalViewHeight = 0f

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
            BasicTextField2(
                state = editText,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ),
                onTextLayout = {
                    coroutineScope.launch {
                        try {
                            val cursor = it()?.getCursorRect(editText.text.selectionInChars.end)
                            if (cursor != null) {
                                val bottomOffset = scroll.value + globalViewHeight
                                if (cursor.top < scroll.value) scroll.scrollTo(cursor.top.toInt())
                                else if (cursor.bottom > bottomOffset) scroll.scrollBy(cursor.bottom - bottomOffset)
                            }
                        } catch (_: Exception) {}
                    }
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp, 20.dp, 20.dp, 0.dp)
                    .verticalScroll(scroll)
                    .onGloballyPositioned {
                        val i = it.boundsInWindow()
                        globalViewHeight = i.bottom - i.top
                    }
            )
        }
    }
}