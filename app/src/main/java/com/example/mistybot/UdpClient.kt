package com.example.mistybot

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object UdpClient {
    private const val SERVER_IP = "192.168.134.106"
    private const val SERVER_PORT = 8080

    private var socket: DatagramSocket? = null

    init {
        try {
            socket = DatagramSocket()
        } catch (e: Exception) {
            Log.e("UDP", "소켓 생성 실패: ", e)
        }
    }

    suspend fun sendMessage(message: String): String? {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                val serverAddr = InetAddress.getByName(SERVER_IP)

                // 메시지 송신
                val sendData = message.toByteArray()
                val sendPacket = DatagramPacket(sendData, sendData.size, serverAddr, SERVER_PORT)
                socket?.send(sendPacket)

                // 서버로부터 응답 수신
                val receiveData = ByteArray(1024)
                val receivePacket = DatagramPacket(receiveData, receiveData.size)
                socket?.receive(receivePacket)

                String(receivePacket.data, 0, receivePacket.length).trim()
            } catch (e: Exception) {
                Log.e("UDP", "통신 에러: ", e)
                null
            }
        }
    }

    fun close() {
        socket?.close()
    }
}
