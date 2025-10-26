package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import anotes.composeapp.generated.resources.Res
import anotes.composeapp.generated.resources.ascending
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.change_view
import anotes.composeapp.generated.resources.date_create
import anotes.composeapp.generated.resources.date_update
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.descending
import anotes.composeapp.generated.resources.search_text_hint
import anotes.composeapp.generated.resources.show_archive_notes
import anotes.composeapp.generated.resources.show_main_notes
import anotes.composeapp.generated.resources.sorting
import anotes.composeapp.generated.resources.sync_drive_action
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.OrientationObj
import com.yurhel.alex.anotes.ui.SyncActionTypes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainBottomBar(
    vm: MainViewModel,
    appSettingsView: String
) {
    val searchText by vm.searchText.collectAsState()
    val isSyncNow by vm.isSyncNow.collectAsState()

    val keyboard = LocalSoftwareKeyboardController.current
    var isSearchOn by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val textFocusRequester = remember { FocusRequester() }
    val orientation = getOrientation()
    val isSearchOnMobile = isSearchOn && orientation != OrientationObj.Desktop

    BaseBottomBar {
        if (isSearchOnMobile) {
            // Search OFF button
            IconButton(
                onClick = {
                    isSearchOn = false
                    focusManager.clearFocus()
                }
            ) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(Res.string.back), Modifier.size(30.dp))
            }
        }
        // Search text field
        val interactionSource = remember { MutableInteractionSource() }
        val surfaceColor = MaterialTheme.colorScheme.background
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = if (isSearchOnMobile) 0.dp else 10.dp)
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .drawBehind { drawRect(surfaceColor) }
        ) {
            // Additional in search field (clear text button or search icon)
            if (!isSearchOnMobile) {
                val searchIsEmpty = searchText.isEmpty()
                Icon(
                    imageVector = if (searchIsEmpty) Icons.Outlined.Search else Icons.Default.Close,
                    contentDescription = if (searchIsEmpty) "search" else "delete",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = !searchIsEmpty,
                            onClick = {
                                isSearchOn = false
                                focusManager.clearFocus()
                                vm.getDbNotes("")
                            }
                        )
                )
            }
            // Text field
            BasicTextField(
                value = searchText,
                onValueChange = { vm.getDbNotes(it) },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(textFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) isSearchOn = true
                    },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontStyle = MaterialTheme.typography.bodyLarge.fontStyle
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions { keyboard?.hide() },
                interactionSource = interactionSource,
                singleLine = true
            ) { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = searchText,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(text = stringResource(Res.string.search_text_hint))
                    },
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(if (isSearchOnMobile) 8.dp else 0.dp, 4.dp)
                )
            }
        }
        if (isSearchOnMobile) {
            // Search OFF + Clear text button ???
            IconButton(
                onClick = {
                    isSearchOn = false
                    focusManager.clearFocus()
                    vm.getDbNotes("")
                }
            ) {
                Icon(Icons.Default.Close, stringResource(Res.string.delete), Modifier.size(30.dp))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Sync indicator / button
                if (isSyncNow) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(12.dp, 0.dp)
                            .size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = {
                            vm.syncData(SyncActionTypes.Auto, vm)
                        }
                    ) {
                        Icon(Icons.Default.Refresh, stringResource(Res.string.sync_drive_action), Modifier.size(30.dp))
                    }
                }
                // Change notes view button
                IconButton(onClick = vm::changeNotesView) {
                    Icon(
                        if (appSettingsView == "grid") Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                        stringResource(Res.string.change_view),
                        Modifier.size(30.dp)
                    )
                }
                // Change sort button
                var expandedMenu by remember { mutableStateOf(false) }
                val sorting = stringResource(Res.string.sorting)
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        sorting,
                        Modifier.size(30.dp)
                    )
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier
                        .wrapContentSize()
                        .width(IntrinsicSize.Max)
                ) {
                    var isShowArchive by remember { mutableStateOf(vm.db.getDataShowing() == "archive") }
                    RadioDropdownMenuItem(
                        onClick = {
                            isShowArchive = false
                            vm.db.updateDataShowing("all")
                            vm.getDbNotes("")
                        },
                        text = stringResource(Res.string.show_main_notes),
                        isSelected = !isShowArchive
                    )
                    RadioDropdownMenuItem(
                        onClick = {
                            isShowArchive = true
                            vm.db.updateDataShowing("archive")
                            vm.getDbNotes("")
                        },
                        text = stringResource(Res.string.show_archive_notes),
                        isSelected = isShowArchive
                    )
                    Text(
                        text = sorting,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    var sortType by remember { mutableStateOf(vm.db.getSortType()) }
                    var sortArrow by remember { mutableStateOf(vm.db.getSortArrow()) }
                    for (i in listOf("dateUpdate","dateCreate")) {
                        RadioDropdownMenuItem(
                            onClick = {
                                sortType = i
                                vm.db.updateSortType(i)
                                vm.getDbNotes("")
                            },
                            text = stringResource(
                                when(i) {
                                    "dateUpdate" -> Res.string.date_update
                                    else -> Res.string.date_create
                                }
                            ),
                            isSelected = sortType == i
                        )
                    }
                    for (i in listOf("ascending","descending")) {
                        RadioDropdownMenuItem(
                            onClick = {
                                sortArrow = i
                                vm.db.updateSortArrow(i)
                                vm.getDbNotes("")
                            },
                            text = stringResource(
                                when(i) {
                                    "ascending" -> Res.string.ascending
                                    else -> Res.string.descending
                                }
                            ),
                            isSelected = sortArrow == i
                        )
                    }
                }
            }
        }
    }
}