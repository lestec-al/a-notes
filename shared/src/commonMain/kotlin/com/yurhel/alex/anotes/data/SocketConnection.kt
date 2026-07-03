package com.yurhel.alex.anotes.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

object SocketConnection {
    private const val PORT = 8834

    suspend fun getData(onClientOn: () -> Unit): DriveData? {
        return try {
            var result = ""
            withContext(Dispatchers.IO) {
                ServerSocket(PORT).use { serverSocket ->
                    serverSocket.soTimeout = 12000
                    serverSocket.accept().use { clientSocket ->
                        onClientOn()
                        PrintWriter(clientSocket.getOutputStream(), true).use {
                            BufferedReader(InputStreamReader(clientSocket.getInputStream())).use { input ->
                                result = input.readLine()
                            }
                        }
                    }
                }
                val obj = Json.decodeFromString<JsonObject>(result)
                DriveData(
                    data = obj["data"]!!.jsonArray,
                    modifiedTime = try {
                        obj["modifiedTime"]?.jsonPrimitive?.long
                    } catch (_: Exception) {
                        null
                    },
                    isServiceOK = true
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun shareData(
        hostNumber: String,
        localData: String,
        localIpAddress: String,
        onClientOn: () -> Unit
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val address = localIpAddress.run {
                    val firstPart = this.split('.').take(3).joinToString(".")
                    "$firstPart.$hostNumber"
                }
                Socket(address, PORT).use { kkSocket ->
                    onClientOn()
                    PrintWriter(kkSocket.getOutputStream(), true).use { output ->
                        BufferedReader(InputStreamReader(kkSocket.getInputStream())).use {
                            output.println(localData)
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}