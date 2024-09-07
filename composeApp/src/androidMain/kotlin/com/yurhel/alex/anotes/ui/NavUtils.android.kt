package com.yurhel.alex.anotes.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
actual fun BackHandlerCustom(onBack: () -> Unit) {
    BackHandler(true, onBack)
}

@Composable
actual fun getOrientation() = if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) OrientationObj.Landscape else OrientationObj.Portrait