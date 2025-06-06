package com.yurhel.alex.anotes.ui

import androidx.compose.runtime.Composable

@Composable
expect fun NoteScreen(
    vm: MainViewModel,
    onBack: (isSaved: Boolean) -> Unit,
    toTasks: () -> Unit
)

@Composable
expect fun BackHandlerCustom(onBack: ()-> Unit)

@Composable
expect fun getOrientation(): OrientationObj