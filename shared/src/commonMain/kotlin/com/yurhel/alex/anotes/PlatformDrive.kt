package com.yurhel.alex.anotes

import com.yurhel.alex.anotes.data.DriveData

expect class PlatformDrive {
    suspend fun getData(): DriveData
    @Suppress("unused")
    suspend fun sendData(localData: String)
}