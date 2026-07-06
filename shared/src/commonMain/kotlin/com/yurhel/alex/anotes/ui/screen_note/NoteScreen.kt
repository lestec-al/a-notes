package com.yurhel.alex.anotes.ui.screen_note

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.keyboardAsState
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.add_img
import com.yurhel.alex.anotes.shared.del_img
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.components.NoteBottomBar
import com.yurhel.alex.anotes.ui.components.CustomScaffold
import com.yurhel.alex.anotes.ui.utils.BottomBarButton
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteScreen(
    vm: MainViewModel,
    onBack: () -> Unit
) {
    BackHandlerCustom {
        vm.saveNote()
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
    var globalViewHeight by remember { mutableFloatStateOf(0f) }

    // Need to create a sep viewModel and move stuff to init ???
    LaunchedEffect(Unit) {
        vm.updateImageData()
    }

    CustomScaffold(
        bottomBar = {
            NoteBottomBar(
                vm = vm,
                scope = coroutineScope,
                onBackAfterDelete = onBack,
                onBackButtonClick = {
                    vm.saveNote()
                    onBack()
                },
                onGetTextButtonClick = vm.editText.text::toString,
                additionalButtons = listOf(
                    if (vm.isAddImage) {
                        BottomBarButton(
                            onClick = vm::addImage,
                            icon = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = stringResource(Res.string.add_img)
                        )
                    } else {
                        BottomBarButton(
                            onClick = vm::delImage,
                            icon = Icons.Outlined.HideImage,
                            contentDescription = stringResource(Res.string.del_img)
                        )
                    }
                )
            )
        }
    ) { bottomPadding, topPadding ->
        BasicTextField(
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
                        val cursor = it()?.getCursorRect(vm.editText.selection.end)
                        if (cursor != null) {
                            val bottomOffset = scroll.value + globalViewHeight
                            if (cursor.top < scroll.value) scroll.scrollTo(cursor.top.toInt())
                            else if (cursor.bottom > bottomOffset) scroll.scrollBy(cursor.bottom - bottomOffset)
                        }
                    } catch (_: Exception) {}
                }
            },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorator = { innerTextField ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Status bar spacer
                    Spacer(Modifier.height(topPadding))
                    // Image
                    val image = vm.noteImage
                    if (image != null) {
                        Image(
                            bitmap = image,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // Text
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .padding(horizontal = 5.dp)
                .verticalScroll(scroll)
                .onGloballyPositioned {
                    val i = it.boundsInWindow()
                    globalViewHeight = i.bottom - i.top
                }
                .focusRequester(focusRequester)
        )
    }
}