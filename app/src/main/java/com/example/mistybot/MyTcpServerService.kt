package com.example.mistybot

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket

class MyTcpServerService : Service() {

    private val TCP_PORT = 8082

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread(ServerThread()).start() // 서버 스레드 시작
        return START_STICKY
    }

    private inner class ServerThread : Runnable {
        override fun run() {
            try {
                val serverSocket = ServerSocket(TCP_PORT)
                while (true) {
                    val clientSocket: Socket = serverSocket.accept() // 클라이언트 연결 수락
                    handleClient(clientSocket) // 클라이언트 처리
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun handleClient(clientSocket: Socket) {
            val inputStream: InputStream = clientSocket.getInputStream()
            // 이미지 수신 및 처리 코드 작성
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
