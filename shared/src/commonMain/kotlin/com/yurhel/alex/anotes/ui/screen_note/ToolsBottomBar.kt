package com.yurhel.alex.anotes.ui.screen_note

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yurhel.alex.anotes.ui.components.BaseBottomBar
import com.yurhel.alex.anotes.ui.components.NoteBottomBar
import com.yurhel.alex.anotes.ui.screen_note.icons.ArrowDirection
import com.yurhel.alex.anotes.ui.screen_note.icons.ArrowHorizontalDivider
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.stringResource

@Composable
fun ToolsBottomBar(
    vm: NoteViewModel,
    scope: CoroutineScope,
    onBack: () -> Unit
) {
    val toolsScrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            // Rich text tools
            BaseBottomBar(withInsets = false) {
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .fillMaxWidth()
                        .horizontalScroll(toolsScrollState)
                ) {
                    vm.getToolButtons().forEach {
                        if (it == null) {
                            VerticalDivider()
                        } else {
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
            // Standard bottom bar
            NoteBottomBar(
                vm = vm.vm,
                scope = scope,
                onBack = onBack,
                onGetTextButtonClick = vm.state::toText
            )
        }
        // Scroll indicators
        Row(
            modifier = Modifier
                .padding(top = 50.dp)
                .fillMaxWidth()
        ) {
            if (toolsScrollState.canScrollBackward) {
                ArrowHorizontalDivider(
                    modifier = Modifier.width(40.dp),
                    direction = ArrowDirection.LEFT
                )
            }
            Spacer(Modifier.weight(1f))
            if (toolsScrollState.canScrollForward) {
                ArrowHorizontalDivider(
                    modifier = Modifier.width(40.dp),
                    direction = ArrowDirection.RIGHT
                )
            }
        }
    }
}