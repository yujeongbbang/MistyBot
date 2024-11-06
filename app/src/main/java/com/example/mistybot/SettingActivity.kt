package com.example.mistybot

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SettingActivity : AppCompatActivity() {

    private lateinit var humidityInput: EditText
    private lateinit var saveButton: Button
    private val turtleBotPort = 8000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // 뷰 초기화
        humidityInput = findViewById(R.id.humidityInput)
        saveButton = findViewById(R.id.saveButton)

        // 저장 버튼 클릭 리스너 설정
        saveButton.setOnClickListener {
            val humidity = humidityInput.text.toString()

            if (humidity.isNotEmpty()) {
                sendHumidityToTurtleBot(humidity)
            } else {
                Toast.makeText(this, "습도 값을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 새로운 포트로 메시지 전송
    private fun sendHumidityToTurtleBot(humidity: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var socket: DatagramSocket? = null
            try {
                // 메시지 형식 정의
                val message = "SET_HUMIDITY:$humidity%"
                val serverAddr = InetAddress.getByName("192.168.134.106") // 서버 IP
                val sendData = message.toByteArray()

                // 지정한 포트 번호로 소켓 생성 및 메시지 전송
                socket = DatagramSocket()
                val sendPacket = DatagramPacket(sendData, sendData.size, serverAddr, turtleBotPort)
                socket.send(sendPacket)

                // 응답 수신
                val receiveData = ByteArray(1024)
                val receivePacket = DatagramPacket(receiveData, receiveData.size)
                socket.receive(receivePacket)

                val response = String(receivePacket.data, 0, receivePacket.length).trim()

                // 메인 스레드에서 결과 처리
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingActivity, "응답 받음: $response", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingActivity, "전송 실패", Toast.LENGTH_SHORT).show()
                }
            } finally {
                // 소켓 닫기
                socket?.close()
            }
        }
    }
}
