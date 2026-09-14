package com.yurhel.alex.anotes.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

enum class SyncActionTypes { Auto, ManualExport, ManualImport }

enum class Orientation { Portrait, Landscape, Desktop }

enum class NoteType { Note, Tasks, Draw, Swipe }

// I need them to be small-cased
enum class Sort { dateUpdate, dateCreate }
enum class SortArrow { ascending, descending }

data class BottomBarButton(
    val onClick: () -> Unit,
    val icon: ImageVector,
    val contentDescription: StringResource?,
    val toggled: Boolean = false,
    val iconColor: Color? = null,
    val enabled: Boolean = true
)