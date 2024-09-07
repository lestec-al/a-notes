package com.yurhel.alex.anotes.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandlerCustom(onBack: () -> Unit) {}

@Composable
actual fun getOrientation() = OrientationObj.Desktop