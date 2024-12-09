package com.example.mistybot

import android.content.Intent
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
    private lateinit var tempNavigateButton: Button // 임시 버튼 변수 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_search)

        deviceStatus = findViewById(R.id.deviceStatus)
//        connectButton = findViewById(R.id.connectButton)
        tempNavigateButton = findViewById(R.id.tempNavigateButton) // 임시 버튼 초기화

        // 임시 버튼 클릭 이벤트 추가
        tempNavigateButton.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }


//        // 처음에는 버튼을 비활성화
//        connectButton.isEnabled = false


        // UDP 서버에 연결 상태를 확인하여 버튼 활성화
        checkUDPConnection()
    }

    // UDP 서버 연결 상태 확인
    private fun checkUDPConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = connectToUDPServer()
            withContext(Dispatchers.Main) {
                if (response != null) {
                    deviceStatus.text = "TurtleBot 응답: $response"
//                    connectButton.isEnabled = true // 연결 가능 시 버튼 활성화
                } else {
                    deviceStatus.text = "연결됨"
//                    connectButton.isEnabled = false // 연결 불가 시 버튼 비활성화
                }
            }
        }
    }

    private suspend fun connectToUDPServer(): String? {
        val message = "앱에서 터틀봇한테 보내는 연결 신호"
        return UdpClient.sendMessage(message)
    }

    override fun onStart() {
        super.onStart()

        // 버튼이 활성화된 상태에서 클릭 시 DashBoardActivity로 이동
//        connectButton.setOnClickListener {
//            val intent = Intent(this, DashboardActivity::class.java)
//            startActivity(intent)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        UdpClient.close() // Activity가 종료될 때 소켓을 닫습니다.
    }
}
