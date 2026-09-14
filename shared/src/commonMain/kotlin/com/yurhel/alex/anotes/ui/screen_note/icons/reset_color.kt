package com.yurhel.alex.anotes.ui.screen_note.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val reset_color: ImageVector
    get() {
        if (_reset_color != null) {
            return _reset_color!!
        }
        _reset_color = ImageVector.Builder(
            name = "reset_colors",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
            .apply {
                path(
                    fill = SolidColor(Color.Black),
                    fillAlpha = 1f,
                    stroke = null,
                    strokeAlpha = 1f,
                    strokeLineWidth = 1f,
                    strokeLineCap = StrokeCap.Butt,
                    strokeLineJoin = StrokeJoin.Bevel,
                    strokeLineMiter = 1f,
                    pathFillType = PathFillType.NonZero,
                ) {
                    moveTo(16.95f, 22.89f)
                    quadToRelative(-0.27f, -0.11f, -0.5f, -0.34f)
                    lineToRelative(-4f, -4f)
                    quadTo(12f, 18.13f, 12f, 17.5f)
                    reflectiveQuadToRelative(0.45f, -1.07f)
                    lineTo(16.1f, 12.8f)
                    lineTo(14.15f, 10.85f)
                    lineToRelative(1.4f, -1.4f)
                    lineToRelative(7.03f, 6.98f)
                    quadToRelative(0.22f, 0.22f, 0.34f, 0.5f)
                    reflectiveQuadToRelative(0.11f, 0.55f)
                    reflectiveQuadToRelative(-0.11f, 0.56f)
                    reflectiveQuadToRelative(-0.34f, 0.51f)
                    lineToRelative(-4f, 4f)
                    quadToRelative(-0.22f, 0.22f, -0.51f, 0.34f)
                    quadTo(17.78f, 23f, 17.5f, 23f)
                    quadToRelative(-0.27f, 0f, -0.55f, -0.11f)
                    close()
                    moveTo(14.2f, 17.5f)
                    horizontalLineToRelative(6.6f)
                    lineTo(17.5f, 14.2f)
                    lineToRelative(-3.3f, 3.3f)
                    close()
                    moveTo(4f, 21f)
                    verticalLineTo(19f)
                    horizontalLineTo(6.35f)
                    quadTo(4.8f, 17.75f, 3.9f, 15.94f)
                    reflectiveQuadTo(3f, 12f)
                    quadTo(3f, 10.13f, 3.71f, 8.49f)
                    reflectiveQuadTo(5.64f, 5.64f)
                    quadTo(6.85f, 4.42f, 8.49f, 3.71f)
                    reflectiveQuadTo(12f, 3f)
                    quadToRelative(3.23f, 0f, 5.66f, 1.99f)
                    quadTo(20.1f, 6.97f, 20.78f, 10f)
                    horizontalLineTo(18.7f)
                    quadTo(18.05f, 7.8f, 16.23f, 6.4f)
                    reflectiveQuadTo(12f, 5f)
                    quadTo(9.08f, 5f, 7.04f, 7.04f)
                    reflectiveQuadTo(5f, 12f)
                    quadToRelative(0f, 1.8f, 0.81f, 3.3f)
                    reflectiveQuadTo(8f, 17.75f)
                    verticalLineTo(15f)
                    horizontalLineToRelative(2f)
                    verticalLineToRelative(6f)
                    horizontalLineTo(4f)
                    close()
                }
            }
            .build()
        return _reset_color!!
    }

private var _reset_color: ImageVector? = null