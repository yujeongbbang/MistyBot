package com.example.mistybot

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceSearchActivity : AppCompatActivity() {

    private lateinit var deviceStatus: TextView
    private lateinit var connectButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_search)

        deviceStatus = findViewById(R.id.deviceStatus)
        connectButton = findViewById(R.id.connectButton)

        connectButton.setOnClickListener {
            // 버튼 클릭 시 UDP 서버에 연결 요청
            CoroutineScope(Dispatchers.IO).launch {
                val response = connectToUDPServer()
                withContext(Dispatchers.Main) {
                    if (response != null) {
                        deviceStatus.text = "TurtleBot 응답: $response"
                    } else {
                        deviceStatus.text = "연결 실패"
                    }
                }
            }
        }
    }

    private suspend fun connectToUDPServer(): String? {
        val message = "앱에서 터틀봇한테 보내는 연결 신호"
        return UdpClient.sendMessage(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        UdpClient.close() // Activity가 종료될 때 소켓을 닫습니다.
    }
}
