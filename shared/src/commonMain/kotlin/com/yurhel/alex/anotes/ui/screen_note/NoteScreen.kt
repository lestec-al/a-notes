package com.yurhel.alex.anotes.ui.screen_note

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.mohamedrejeb.richeditor.ui.BasicRichTextEditor
import com.yurhel.alex.anotes.BackHandlerCustom
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.edit_link
import com.yurhel.alex.anotes.shared.see_link
import com.yurhel.alex.anotes.shared.text_color
import com.yurhel.alex.anotes.ui.components.BaseBottomBar
import com.yurhel.alex.anotes.ui.components.NoteBottomBar
import com.yurhel.alex.anotes.ui.components.CustomScaffold
import com.yurhel.alex.anotes.ui.screen_note.components.ColorSheet
import com.yurhel.alex.anotes.ui.screen_note.components.LinkEditSheet
import com.yurhel.alex.anotes.ui.utils.Orientation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

@Composable
fun NoteScreen(
    vm: NoteViewModel,
    onBack: () -> Unit
) {
    BackHandlerCustom(onBack)
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        vm.saveNote()
    }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val orientation = getOrientation()
    var prevSelection by remember { mutableStateOf(TextRange(0)) }
    var isScrollOn by remember { mutableStateOf(true) }
    val snackBarHostState = remember { SnackbarHostState() }

    val textColorStrId = Res.string.text_color
    val editLinkStr = stringResource(Res.string.edit_link)

    if (vm.colorSheetOpenFor != null) {
        ColorSheet(
            onDismissRequest = vm::closeColorSheet,
            onColorReset = {
                vm.sheetChooseColor(vm.colorSheetOpenFor == textColorStrId, null)
                vm.closeColorSheet()
            },
            onColorChoose = {
                vm.sheetChooseColor(vm.colorSheetOpenFor == textColorStrId, it)
            },
            colorSheetFor = stringResource(vm.colorSheetOpenFor!!),
            textColorStr = stringResource(textColorStrId),
            span = vm.state.currentSpanStyle,
        )
    }

    if (vm.linkSheetOpenFor != null) {
        LinkEditSheet(
            onDismissRequest = { vm.updateLinkSheetFor(null) },
            onDelete = {
                vm.state.removeLink()
                vm.updateLinkSheetFor(null)
            },
            onSave = { editUrl ->
                if (vm.linkSheetOpenFor!!.second.isEmpty()) {
                    vm.state.addLinkToSelection(editUrl)
                } else {
                    vm.state.updateLink(editUrl)
                }
                vm.updateLinkSheetFor(null)
            },
            linkSheetFor = vm.linkSheetOpenFor!!,
            editLinkStr = editLinkStr,
        )
    }

    LaunchedEffect(vm.state.selection) {
        if (vm.state.isLink) {
            scope.launch {
                snackBarHostState.showSnackbar(
                    message = vm.state.selectedLinkUrl ?: "",
                    duration = SnackbarDuration.Short
                )
            }
        }
        if (!vm.state.selection.collapsed) {
            prevSelection = vm.state.selection
        }
    }

    CustomScaffold(
        bottomBar = {
            Column {
                BaseBottomBar(withInsets = false) {
                    Row(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        vm.getToolButtons().forEach {
                            if (it == null) VerticalDivider() else {
                                IconButton(
                                    onClick = it.onClick,
                                    enabled = it.enabled,
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = if (it.toggled) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            it.iconColor ?: LocalContentColor.current
                                        }
                                    )
                                ) {
                                    Icon(
                                        it.icon,
                                        it.contentDescription?.let { r -> stringResource(r) },
                                        Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                NoteBottomBar(
                    vm = vm.vm,
                    scope = scope,
                    onBackAfterDelete = onBack,
                    onBackButtonClick = onBack,
                    onGetTextButtonClick = vm.state::toText
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) {
                Snackbar {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = it.visuals.message,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            vm.updateLinkSheetFor(
                                Pair(
                                    vm.state.selectedLinkText ?: "",
                                    vm.state.selectedLinkUrl ?: ""
                                )
                            )
                            it.dismiss()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = editLinkStr
                            )
                        }
                        IconButton(onClick = {
                            vm.openLink(it.visuals.message)
                            it.dismiss()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Send,
                                contentDescription = stringResource(Res.string.see_link)
                            )
                        }
                    }
                }
            }
        }
    ) { bottomPadding, topPadding ->
        BasicRichTextEditor(
            state = vm.state,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            state = scrollState,
                            enabled = isScrollOn
                        )
                ) {
                    // Status bar spacer
                    Spacer(Modifier.height(topPadding))
                    // Image
                    vm.noteImage?.also { image ->
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
                .padding(start = 5.dp, end = 5.dp, bottom = bottomPadding)
                .fillMaxSize()
                // Fixing issue where initial click on text run scroll to previous selection
                .nestedScroll(object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return if (!isScrollOn) available else super.onPreScroll(available, source)
                    }
                })
                .onFocusChanged {
                    // Fixing desktop issue where selection is collapsed after UI btn is clicked
                    if (orientation == Orientation.Desktop) {
                        if (!prevSelection.collapsed && vm.state.selection.max == prevSelection.max) {
                            vm.state.selection = prevSelection
                        }
                    }
                    // Fixing issue where initial click on text run scroll to previous selection
                    if (it.isFocused) {
                        val lastScrollPosition = scrollState.value
                        isScrollOn = false
                        scope.launch {
                            delay(1.seconds)
                            withFrameMillis {}
                            withFrameMillis {}
                            isScrollOn = true
                            scrollState.scrollTo(lastScrollPosition)
                        }
                    }
                }
        )
    }
}