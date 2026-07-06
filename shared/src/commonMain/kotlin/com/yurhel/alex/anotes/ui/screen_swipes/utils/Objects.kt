package com.yurhel.alex.anotes.ui.screen_swipes.utils

enum class Edit { None, NoteText, SwipeText, Left, Right }

enum class SwipeTextPos { Left, Right }

data class SwipeTextObj(
    val id: Int,
    val pos: SwipeTextPos,
    val text: String
)