package com.example.myapplication.data.network

import android.util.Log
import com.example.myapplication.domain.entity.TestRequest
import com.example.myapplication.domain.entity.TestResponse
import java.io.DataInputStream
import java.net.Socket
import java.nio.ByteBuffer
import com.google.gson.Gson
import java.io.*


private const val TAG = "SocketManager"

class SocketManager(
    private val address: String,
    private val port: Int
) {
    private var socket: Socket? = null
    private val gson = Gson()

    fun connect() {
        socket = Socket(address, port)

        Log.d(TAG, "connected: ${socket?.isConnected}")
    }

    fun send(request: TestRequest) {
        val orderedMap = linkedMapOf(
            "gender" to request.gender,
            "age" to request.age
        )
        val message = gson.toJson(orderedMap)
        Log.i(TAG, "sending: $message")
        val messageBytes = message.toByteArray()
        val lengthBytes = ByteBuffer.allocate(4).putInt(messageBytes.size).array()
        val outputStream = socket?.getOutputStream()
        outputStream?.write(lengthBytes)
        outputStream?.write(messageBytes)
        outputStream?.flush()
    }

    fun receive(): TestResponse {
        return try {
            val inputStream = socket?.getInputStream() ?: throw IOException("Input stream is null")
            val dataInputStream = DataInputStream(BufferedInputStream(inputStream))

            val lengthBytes = ByteArray(4)
            dataInputStream.readFully(lengthBytes)
            val length = ByteBuffer.wrap(lengthBytes).int

            val buffer = ByteArray(length)
            dataInputStream.readFully(buffer)
            val message = String(buffer, Charsets.UTF_8)

            Log.d(TAG, "received: $message")

            gson.fromJson(message, TestResponse::class.java)
        } catch (e: Exception) {
            TestResponse(false)
        }
    }

    fun close() {
        socket?.close()
        socket = null
    }
}
