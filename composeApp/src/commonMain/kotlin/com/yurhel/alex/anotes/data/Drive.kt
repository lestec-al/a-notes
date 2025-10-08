package com.yurhel.alex.anotes.data

expect class Drive {
    suspend fun getData(): DriveObj
    suspend fun sendData(localData: String)
}