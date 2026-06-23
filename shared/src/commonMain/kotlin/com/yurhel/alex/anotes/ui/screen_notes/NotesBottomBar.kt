package com.yurhel.alex.anotes.ui.screen_notes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.app_ver
import com.yurhel.alex.anotes.shared.ascending
import com.yurhel.alex.anotes.shared.back
import com.yurhel.alex.anotes.shared.change_view
import com.yurhel.alex.anotes.shared.date_create
import com.yurhel.alex.anotes.shared.date_update
import com.yurhel.alex.anotes.shared.delete
import com.yurhel.alex.anotes.shared.descending
import com.yurhel.alex.anotes.shared.filtering
import com.yurhel.alex.anotes.shared.privacy_link
import com.yurhel.alex.anotes.shared.privacy_policy
import com.yurhel.alex.anotes.shared.search_text_hint
import com.yurhel.alex.anotes.shared.show_archive_notes
import com.yurhel.alex.anotes.shared.show_main_notes
import com.yurhel.alex.anotes.shared.sorting
import com.yurhel.alex.anotes.shared.sync_drive_action
import com.yurhel.alex.anotes.getOrientation
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.components.BaseBottomBar
import com.yurhel.alex.anotes.ui.components.RadioDropdownMenuItem
import com.yurhel.alex.anotes.ui.utils.Orientation
import com.yurhel.alex.anotes.ui.utils.Sort
import com.yurhel.alex.anotes.ui.utils.SortArrow
import com.yurhel.alex.anotes.ui.utils.SyncActionTypes
import org.jetbrains.compose.resources.stringResource

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

    val isSearchOnMobile = vm.isSearchOn && getOrientation() != Orientation.Desktop
    val searchIsEmpty = vm.searchText.isEmpty()

    val borderModifier = Modifier.border(
        width = 1.dp,
        color = MaterialTheme.colorScheme.primary,
        shape = IconButtonDefaults.filledShape
    )

    var showAdditional by remember { mutableStateOf(false) }
    val privacyLink = stringResource(Res.string.privacy_link)

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
                IconButton(onClick = vm::openMenu) {
                    Icon(Icons.Default.MoreVert, "more", Modifier.size(30.dp))
                }
                DropdownMenu(
                    expanded = vm.isNotesMenuExpanded,
                    onDismissRequest = vm::closeMenu,
                    properties = PopupProperties(
                        focusable = true,
                        clippingEnabled = false
                    )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { vm.updateDarkTheme(false) },
                            modifier = if (vm.darkTheme == false) borderModifier else Modifier
                        ) {
                            Icon(Icons.Outlined.LightMode, "light mode")
                        }
                        IconButton(
                            onClick = { vm.updateDarkTheme(null) },
                            modifier = if (vm.darkTheme == null) borderModifier else Modifier
                        ) {
                            Icon(Icons.Outlined.Contrast, "auto mode")
                        }
                        IconButton(
                            onClick = { vm.updateDarkTheme(true) },
                            modifier = if (vm.darkTheme == true) borderModifier else Modifier
                        ) {
                            Icon(Icons.Outlined.DarkMode, "dark mode")
                        }
                        VerticalDivider(Modifier.padding(horizontal = 5.dp))
                        IconButton(
                            onClick = { showAdditional = !showAdditional },
                            modifier = if (showAdditional) borderModifier else Modifier
                        ) {
                            Icon(Icons.Outlined.Info, "info")
                        }
                    }
                    if (showAdditional) {
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        Text(
                            text = stringResource(Res.string.privacy_policy),
                            modifier = Modifier
                                .clickable { vm.platform.openLink(privacyLink) }
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                                .fillMaxWidth(),
                            textDecoration = TextDecoration.Underline
                        )
                        Text(
                            text = stringResource(Res.string.app_ver) + ": " + vm.platform.getAppVersion(),
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, top = 5.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}