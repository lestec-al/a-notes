package com.yurhel.alex.anotes.ui.screen_tasks.utils

fun IntRange.overlaps(other: IntRange): Boolean {
    return this.first <= other.last && other.first <= this.last
}