package com.yurhel.alex.anotes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.yurhel.alex.anotes.ui.OrientationObj
import com.yurhel.alex.anotes.ui.theme.darkColorScheme
import com.yurhel.alex.anotes.ui.theme.lightColorScheme
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream
import java.util.Base64
import javax.imageio.ImageIO

@Composable
actual fun BackHandlerCustom(onBack: () -> Unit) {}

@Composable
actual fun getOrientation() = OrientationObj.Desktop

@Composable
actual fun keyboardAsState(): State<Boolean> {
    return rememberUpdatedState(false)
}

@Composable
actual fun getColorScheme(
    dynamicColor: Boolean,
    darkTheme: Boolean
): ColorScheme {
    return when {
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }
}

actual fun ImageBitmap.toBase64(): String? {
    val bufferedImage = this.toAwtImage()
    val outputStream = ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "PNG", outputStream)
    return Base64.getEncoder().encodeToString(outputStream.toByteArray())
}

actual fun String.toImageBitmap(): ImageBitmap? {
    val byteArray = Base64.getDecoder().decode(this)
    return Image.makeFromEncoded(byteArray).toComposeImageBitmap()
}