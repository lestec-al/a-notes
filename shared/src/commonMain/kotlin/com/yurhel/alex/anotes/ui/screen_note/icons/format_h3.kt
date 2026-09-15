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
val format_h3: ImageVector
    get() {
        if (_format_h3 != null) {
            return _format_h3!!
        }
        _format_h3 =
            ImageVector.Builder(
                name = "format_h3",
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
                        moveTo(3f, 17f)
                        verticalLineTo(7f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(4f)
                        horizontalLineTo(9f)
                        verticalLineTo(7f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(17f)
                        horizontalLineTo(9f)
                        verticalLineTo(13f)
                        horizontalLineTo(5f)
                        verticalLineToRelative(4f)
                        horizontalLineTo(3f)
                        close()
                        moveToRelative(10f, 0f)
                        verticalLineTo(15f)
                        horizontalLineToRelative(6f)
                        verticalLineTo(13f)
                        horizontalLineTo(15f)
                        verticalLineTo(11f)
                        horizontalLineToRelative(4f)
                        verticalLineTo(9f)
                        horizontalLineTo(13f)
                        verticalLineTo(7f)
                        horizontalLineToRelative(6f)
                        quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(21f, 9f)
                        verticalLineToRelative(6f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(19f, 17f)
                        horizontalLineTo(13f)
                        close()
                    }
                }
                .build()
        return _format_h3!!
    }

private var _format_h3: ImageVector? = null