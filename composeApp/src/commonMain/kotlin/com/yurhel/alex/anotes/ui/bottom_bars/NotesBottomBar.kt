package com.yurhel.alex.anotes.ui.bottom_bars

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import anotes.composeapp.generated.resources.filtering
import anotes.composeapp.generated.resources.search_text_hint
import anotes.composeapp.generated.resources.show_archive_notes
import anotes.composeapp.generated.resources.show_main_notes
import anotes.composeapp.generated.resources.sorting
import anotes.composeapp.generated.resources.sync_drive_action
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.components.BaseBottomBar
import com.yurhel.alex.anotes.ui.components.RadioDropdownMenuItem
import com.yurhel.alex.anotes.ui.utils.OrientationObj
import com.yurhel.alex.anotes.ui.utils.Sort
import com.yurhel.alex.anotes.ui.utils.SortArrow
import com.yurhel.alex.anotes.ui.utils.SyncActionTypes
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesBottomBar(
    vm: MainViewModel,
    appSettingsView: String
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val textFocusRequester = remember { FocusRequester() }
    val textInteractionSource = remember { MutableInteractionSource() }
    val surfaceColor = MaterialTheme.colorScheme.background

    val isSearchOnMobile = vm.isSearchOn && getOrientation() != OrientationObj.Desktop
    val searchIsEmpty = vm.searchText.isEmpty()

    BaseBottomBar {
        if (isSearchOnMobile) {
            // Search off button
            IconButton(
                onClick = {
                    vm.updateIsSearchOn(false)
                    focusManager.clearFocus()
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Default.ArrowBack,
                    stringResource(Res.string.back),
                    Modifier.size(30.dp)
                )
            }
        }
        // Search text field
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
                Icon(
                    imageVector = if (searchIsEmpty) Icons.Outlined.Search else Icons.Default.Close,
                    contentDescription = if (searchIsEmpty) stringResource(Res.string.search_text_hint) else stringResource(Res.string.delete),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = !searchIsEmpty,
                            onClick = {
                                vm.updateIsSearchOn(false)
                                focusManager.clearFocus()
                                vm.getDbNotes("")
                            }
                        )
                )
            }
            // Text field
            BasicTextField(
                value = vm.searchText,
                onValueChange = vm::getDbNotes,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(textFocusRequester)
                    .onFocusChanged {
                        if (it.isFocused) vm.updateIsSearchOn(true)
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
                interactionSource = textInteractionSource,
                singleLine = true
            ) { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = vm.searchText,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = {
                        Text(text = stringResource(Res.string.search_text_hint))
                    },
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    interactionSource = textInteractionSource,
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
            // Search off + clear text button
            IconButton(
                onClick = {
                    vm.updateIsSearchOn(false)
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
                if (vm.isSyncNow) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(12.dp, 0.dp)
                            .size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = { vm.syncData(SyncActionTypes.Auto) }) {
                        Icon(
                            Icons.Default.Refresh,
                            stringResource(Res.string.sync_drive_action),
                            Modifier.size(30.dp)
                        )
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
                // More button
                IconButton(onClick = { vm.updateIsNotesMenuExpanded(true) }) {
                    Icon(Icons.Default.MoreVert, "more", Modifier.size(30.dp))
                }
                DropdownMenu(
                    expanded = vm.isNotesMenuExpanded,
                    onDismissRequest = { vm.updateIsNotesMenuExpanded(false) },
                    modifier = Modifier
                        .wrapContentSize()
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = stringResource(Res.string.filtering),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    RadioDropdownMenuItem(
                        onClick = vm::hideArchive,
                        text = stringResource(Res.string.show_main_notes),
                        isSelected = !vm.isShowArchive
                    )
                    RadioDropdownMenuItem(
                        onClick = vm::showArchive,
                        text = stringResource(Res.string.show_archive_notes),
                        isSelected = vm.isShowArchive
                    )
                    Text(
                        text = stringResource(Res.string.sorting),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    for (i in listOf(Sort.dateUpdate, Sort.dateCreate)) {
                        RadioDropdownMenuItem(
                            onClick = { vm.updateSortType(i.name) },
                            text = stringResource(
                                when (i) {
                                    Sort.dateUpdate -> Res.string.date_update
                                    Sort.dateCreate -> Res.string.date_create
                                }
                            ),
                            isSelected = vm.sortType == i.name
                        )
                    }
                    for (i in listOf(SortArrow.ascending, SortArrow.descending)) {
                        RadioDropdownMenuItem(
                            onClick = { vm.updateSortArrow(i.name) },
                            text = stringResource(
                                when (i) {
                                    SortArrow.ascending -> Res.string.ascending
                                    SortArrow.descending -> Res.string.descending
                                }
                            ),
                            isSelected = vm.sortArrow == i.name
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val modifier = Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = IconButtonDefaults.filledShape
                        )
                        IconButton(
                            onClick = { vm.updateDarkTheme(false) },
                            modifier = if (vm.darkTheme == false) modifier else Modifier
                        ) {
                            Icon(Icons.Outlined.LightMode, "light mode")
                        }
                        IconButton(
                            onClick = { vm.updateDarkTheme(null) },
                            modifier = if (vm.darkTheme == null) modifier else Modifier
                        ) {
                            Icon(Icons.Outlined.Contrast, "auto mode")
                        }
                        IconButton(
                            onClick = { vm.updateDarkTheme(true) },
                            modifier = if (vm.darkTheme == true) modifier else Modifier
                        ) {
                            Icon(Icons.Outlined.DarkMode, "dark mode")
                        }
                    }
                }
            }
        }
    }
}