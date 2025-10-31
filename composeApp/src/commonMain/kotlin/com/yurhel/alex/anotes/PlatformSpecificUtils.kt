package com.yurhel.alex.anotes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.ImageBitmap
import com.yurhel.alex.anotes.data.DriveObj
import com.yurhel.alex.anotes.ui.OrientationObj

expect class Drive {
    suspend fun getData(): DriveObj
    suspend fun sendData(localData: String)
}

@Composable
expect fun BackHandlerCustom(onBack: ()-> Unit)

@Composable
expect fun getOrientation(): OrientationObj

@Composable
expect fun keyboardAsState(): State<Boolean>

@Composable
expect fun getColorScheme(
    dynamicColor: Boolean,
    darkTheme: Boolean
): ColorScheme

@Composable
expect fun SetStatusBarColor(setIsLight: Boolean)

expect fun ImageBitmap.toBase64(): String?

expect fun String.toImageBitmap(): ImageBitmap?
