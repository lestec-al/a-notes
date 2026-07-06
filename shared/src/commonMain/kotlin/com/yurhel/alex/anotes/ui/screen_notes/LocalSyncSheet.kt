package com.yurhel.alex.anotes.ui.screen_notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.shared.Res
import com.yurhel.alex.anotes.shared.back
import com.yurhel.alex.anotes.shared.data_replace_info
import com.yurhel.alex.anotes.shared.enter_code
import com.yurhel.alex.anotes.shared.get_data
import com.yurhel.alex.anotes.shared.lan_code
import com.yurhel.alex.anotes.shared.share_code
import com.yurhel.alex.anotes.shared.share_data
import com.yurhel.alex.anotes.ui.MainViewModel
import com.yurhel.alex.anotes.ui.components.BaseBottomSheet
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalSyncSheet(vm: MainViewModel) {
    if (vm.isLocalSyncSheetOpen) {
        val modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
        val focusRequester = remember { FocusRequester() }
        var text by remember { mutableStateOf(TextFieldState("")) }
        var isShowTextField by remember { mutableStateOf(false) }

        val getStr = stringResource(Res.string.get_data)
        val codeStr = stringResource(Res.string.lan_code)
        val codeInfoStr = stringResource(Res.string.share_code)
        val replaceInfoStr = stringResource(Res.string.data_replace_info)
        val shareStr = stringResource(Res.string.share_data)
        val enderCodeStr = stringResource(Res.string.enter_code)
        val backStr = stringResource(Res.string.back)

        BaseBottomSheet(onDismissRequest = vm::localSyncDialogVisibility) {
            if (vm.isSocketOn) {
                if (!vm.isSyncOn && vm.localAddressCode.isNotEmpty()) {
                    Text(
                        text = getStr,
                        modifier = modifier,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("$codeStr ")
                            withStyle(style = SpanStyle(
                                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                fontWeight = FontWeight.Bold
                            )) {
                                append(vm.localAddressCode)
                            }
                        },
                        modifier = modifier,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = codeInfoStr,
                        modifier = modifier,
                        style = MaterialTheme.typography.labelSmall
                    )
                    HorizontalDivider(
                        modifier = modifier,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = replaceInfoStr,
                        modifier = modifier,
                        style = MaterialTheme.typography.labelSmall
                    )
                    BackIconButton(
                        onClick = vm::cancelSocket,
                        modifier = modifier,
                        label = backStr
                    )
                }
            } else {
                if (!isShowTextField) {
                    // Main options for choosing
                    Button(
                        onClick = vm::createSocket,
                        modifier = modifier,
                        shape = CardDefaults.shape
                    ) {
                        Text(text = getStr)
                    }
                    Button(
                        onClick = { isShowTextField = true },
                        modifier = modifier,
                        shape = CardDefaults.shape
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = shareStr)
                        }
                    }
                } else {
                    // Stage, where I need to get code from the user
                    Text(
                        text = shareStr,
                        modifier = modifier,
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        state = text,
                        modifier = modifier.focusRequester(focusRequester),
                        label = { Text(enderCodeStr) },
                        trailingIcon = {
                            FilledIconButton(
                                onClick = { vm.connectToSocket(text.text.toString()) },
                                shape = CardDefaults.shape,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Outlined.Check, shareStr)
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        onKeyboardAction = { vm.connectToSocket(text.text.toString()) },
                        shape = CardDefaults.shape
                    )
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                    BackIconButton(
                        onClick = { isShowTextField = false },
                        modifier = modifier,
                        label = backStr
                    )
                }
            }
        }
    }
}

@Composable
private fun BackIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    label: String
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(
            width = 1.dp,
            color = ButtonDefaults.outlinedButtonColors().contentColor
        ),
        shape = CardDefaults.shape
    ) {
        Icon(Icons.Outlined.Close, label)
    }
}