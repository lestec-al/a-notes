package com.yurhel.alex.anotes.data

data class NoteObj(val id: Int, val text: String, val dateUpdate: String, val dateCreate: String)

data class SettingsObj(val dataReceivedDate: Long?, val isNotesEdited: Boolean, var viewMode: String)