package com.yurhel.alex.anotes.ui.screen_tasks.utils

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

internal val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

@Composable
internal fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }