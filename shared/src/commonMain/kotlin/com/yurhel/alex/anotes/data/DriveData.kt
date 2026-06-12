package com.yurhel.alex.anotes.data

import kotlinx.serialization.json.JsonArray

data class DriveData(
    val data: JsonArray,
    val modifiedTime: Long?,
    val isServiceOK: Boolean
)