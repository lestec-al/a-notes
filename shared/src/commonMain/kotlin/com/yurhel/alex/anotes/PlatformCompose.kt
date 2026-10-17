package com.yurhel.alex.anotes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.yurhel.alex.anotes.ui.utils.Orientation

@Composable
expect fun BackHandlerCustom(onBack: ()-> Unit)

@Composable
expect fun getOrientation(): Orientation

@Composable
expect fun getColorScheme(
    dynamicColor: Boolean,
    darkTheme: Boolean
): ColorScheme

@Composable
expect fun SetStatusBarColor(setIsLight: Boolean?, darkTheme: Boolean?)

@Composable
expect fun getPlatform(): Platform

@Composable
expect fun FixCutBugCompositionProvider(getSelection: () -> String?, content: @Composable () -> Unit)