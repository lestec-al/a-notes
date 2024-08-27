package com.yurhel.alex.anotes.ui

import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.clearText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yurhel.alex.anotes.R
import com.yurhel.alex.anotes.ui.components.BottomAppBarAssembled
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: () -> Unit,
    toTasks: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (vm.editText.text.isEmpty()) {
            vm.prepareNote(redirectToNotesScreen = onBack, redirectToTasksScreen = toTasks)
        }
    }

    BackHandler {
        vm.saveNote()
        vm.editText.clearText()
        onBack()
    }

    // Fixing a bug with BasicTextField2, when keyboard not showed second time
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isKeyboardOpen by keyboardAsState()
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) focusManager.clearFocus()
    }

    val scroll = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    var globalViewHeight = 0f

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBarAssembled(
                    context = context,
                    vm = vm,
                    coroutineScope = coroutineScope,
                    onBack = onBack,
                    secondButtonAction = {
                        vm.saveNote(true)
                        vm.editText.clearText()
                        toTasks()
                    },
                    secondButtonIcon = painterResource(R.drawable.ic_tasks),
                    secondButtonText = context.getString(R.string.edit_tasks)
                )
            }
        ) { padding ->
            // Edit text field
            BasicTextField2(
                state = vm.editText,
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
                            val cursor = it()?.getCursorRect(vm.editText.text.selectionInChars.end)
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
                    .focusRequester(focusRequester)
            )
        }
    }
}


@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }

    DisposableEffect(LocalWindowInfo.current) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            isImeVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) == true
            true
        }
        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}