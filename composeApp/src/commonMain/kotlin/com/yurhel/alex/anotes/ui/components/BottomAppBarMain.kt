package com.yurhel.alex.anotes.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import anotes.composeapp.generated.resources.back
import anotes.composeapp.generated.resources.change_view
import anotes.composeapp.generated.resources.create
import anotes.composeapp.generated.resources.delete
import anotes.composeapp.generated.resources.ic_grid
import anotes.composeapp.generated.resources.ic_list
import anotes.composeapp.generated.resources.note
import anotes.composeapp.generated.resources.search_text_hint
import anotes.composeapp.generated.resources.sync_drive_action
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.OrientationObj
import com.yurhel.alex.anotes.ui.SyncActionTypes
import com.yurhel.alex.anotes.ui.getOrientation
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomAppBarMain(
    vm: MainViewModel,
    openNoteClicked: () -> Unit,
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

    BottomAppBar(
        modifier = Modifier.height(50.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        if (isSearchOnMobile) {
            // Search OFF button
            val backText = stringResource(Res.string.back)
            Tooltip(backText) {
                IconButton(
                    onClick = {
                        isSearchOn = false
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, backText, Modifier.size(30.dp))
                }
            }

        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                // For mobile wider space between buttons ???
                modifier = if (orientation == OrientationObj.Desktop) {
                    Modifier
                } else {
                    Modifier.fillMaxWidth(0.5f)
                }
            ) {
                // Add new note button
                val newNoteText = stringResource(Res.string.create) + " " + stringResource(Res.string.note)
                Tooltip(newNoteText) {
                    IconButton(
                        onClick = {
                            vm.selectNote(null)
                            openNoteClicked()
                        }
                    ) {
                        Icon(Icons.Default.Add, newNoteText, Modifier.size(30.dp))
                    }
                }

                // Sync indicator / button
                val syncText = stringResource(Res.string.sync_drive_action)
                if (isSyncNow) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(12.dp, 0.dp)
                            .size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
                    )
                } else {
                    Tooltip(syncText) {
                        IconButton(
                            onClick = {
                                vm.syncData(SyncActionTypes.Auto, vm)
                            }
                        ) {
                            Icon(Icons.Default.Refresh, syncText, Modifier.size(30.dp))
                        }
                    }
                }

                // Change notes view button
                val changeViewText = stringResource(Res.string.change_view)
                Tooltip(changeViewText) {
                    IconButton(
                        onClick = {
                            vm.changeNotesView()
                        }
                    ) {
                        Icon(
                            vectorResource(if (appSettingsView == "grid") Res.drawable.ic_list else Res.drawable.ic_grid),
                            changeViewText,
                            Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        // Search text field
        val interactionSource = remember { MutableInteractionSource() }
        val surfaceColor = MaterialTheme.colorScheme.background
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(end = if (isSearchOnMobile) 0.dp else 10.dp)
                .weight(1f)
                .clip(RoundedCornerShape(30.dp))
                .drawBehind { drawRect(surfaceColor) }
        ) {
            BasicTextField(
                value = searchText,
                onValueChange = { vm.getDbNotes(it) },
                modifier = Modifier
                    .padding(start = 10.dp)
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
                    contentPadding = PaddingValues(0.dp, 4.dp)
                )
            }
            // Clear text button
            if (searchText.isNotEmpty() && !isSearchOnMobile) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.delete),
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .clickable {
                            isSearchOn = false
                            focusManager.clearFocus()
                            vm.getDbNotes("")
                        }
                )
            }
        }

        if (isSearchOnMobile) {
            // Search OFF + Clear text button ???
            val clearText = stringResource(Res.string.delete)
            Tooltip(clearText) {
                IconButton(
                    onClick = {
                        isSearchOn = false
                        focusManager.clearFocus()
                        vm.getDbNotes("")
                    }
                ) {
                    Icon(Icons.Default.Close, clearText, Modifier.size(30.dp))
                }
            }
        }
    }
}