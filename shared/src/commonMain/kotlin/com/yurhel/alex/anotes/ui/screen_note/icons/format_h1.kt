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
val format_h1: ImageVector
    get() {
        if (_format_h1 != null) {
            return _format_h1!!
        }
        _format_h1 =
            ImageVector.Builder(
                name = "format_h1",
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
                        moveTo(5f, 17f)
                        verticalLineTo(7f)
                        horizontalLineTo(7f)
                        verticalLineToRelative(4f)
                        horizontalLineToRelative(4f)
                        verticalLineTo(7f)
                        horizontalLineToRelative(2f)
                        verticalLineTo(17f)
                        horizontalLineTo(11f)
                        verticalLineTo(13f)
                        horizontalLineTo(7f)
                        verticalLineToRelative(4f)
                        horizontalLineTo(5f)
                        close()
                        moveToRelative(12f, 0f)
                        verticalLineTo(9f)
                        horizontalLineTo(15f)
                        verticalLineTo(7f)
                        horizontalLineToRelative(4f)
                        verticalLineTo(17f)
                        horizontalLineTo(17f)
                        close()
                    }
                }
                .build()
        return _format_h1!!
    }

private var _format_h1: ImageVector? = null