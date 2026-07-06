package com.yurhel.alex.anotes.ui.utils

import androidx.compose.ui.graphics.vector.ImageVector

enum class SyncActionTypes { Auto, ManualExport, ManualImport }

enum class Orientation { Portrait, Landscape, Desktop }

enum class NoteType { Note, Tasks, Draw, Swipe }

// I need them to be small-cased
enum class Sort { dateUpdate, dateCreate }
enum class SortArrow { ascending, descending }

data class BottomBarButton(
    val onClick: () -> Unit,
    val icon: ImageVector,
    val contentDescription: String?,
    val enabled: Boolean = false
)