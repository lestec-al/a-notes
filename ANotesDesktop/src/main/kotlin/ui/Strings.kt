package ui

import java.util.*

val search_text_hint = when(Locale.getDefault().language) {
    "be" -> "Пошук"
    else -> "Search"
}
val sync_collision = when(Locale.getDefault().language) {
    "be" -> "Знойдзены дзве розныя копіі даных. Выберыце, які з іх выкарыстоўваць"
    else -> "Two different copies of the data were found. Please choose which one to use"
}
val sync_drive = when(Locale.getDefault().language) {
    "be" -> "Воблачныя даныя"
    else -> "Drive data"
}
val sync_local = when(Locale.getDefault().language) {
    "be" -> "Лакальныя даныя"
    else -> "Local data"
}
val updated = when(Locale.getDefault().language) {
    "be" -> "Абноўлена"
    else -> "Updated"
}

val empty_text = when(Locale.getDefault().language) {
    "be" -> "Тут яшчэ пуста"
    else -> "It's empty here yet"
}