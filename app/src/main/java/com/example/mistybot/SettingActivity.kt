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

    // 습도 값을 입력할 EditText와 저장 버튼 변수 선언
    private lateinit var humidityInput: EditText
    private lateinit var saveButton: Button
    private val turtleBotPort = 8000  // TurtleBot의 포트 번호

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // 뷰 초기화 (레이아웃의 EditText와 Button 연결)
        humidityInput = findViewById(R.id.humidityInput)
        saveButton = findViewById(R.id.saveButton)

        // 저장 버튼 클릭 시 습도 값 전송 로직 실행
        saveButton.setOnClickListener {
            val humidity = humidityInput.text.toString()  // 입력된 습도 값 가져오기

            // 입력된 값이 있는지 확인
            if (humidity.isNotEmpty()) {
                // 습도 값이 입력되어 있으면 TurtleBot으로 전송
                sendHumidityToTurtleBot(humidity)
            } else {
                // 습도 값이 비어 있을 경우 사용자에게 알림
                Toast.makeText(this, "습도 값을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendHumidityToTurtleBot(humidity: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var socket: DatagramSocket? = null
            try {
                // 습도 값을 정수로 변환
                val humidityValue = humidity.toIntOrNull() ?: 0
                val message = humidityValue.toString().toByteArray()  // 정수 값을 바이트 배열로 변환

                // 서버와 라즈베리파이 IP 주소 설정
                val serverAddr = InetAddress.getByName("192.168.134.106") // 서버 IP
                val raspberryPiAddr = InetAddress.getByName("192.168.134.109") // 라즈베리파이 IP

                // 지정한 포트 번호로 UDP 소켓 생성
                socket = DatagramSocket()

                // 서버 IP로 습도 값 전송
                val sendPacketServer =
                    DatagramPacket(message, message.size, serverAddr, turtleBotPort)
                socket.send(sendPacketServer)  // 데이터 전송

                // 라즈베리파이 IP로 습도 값 전송
                val sendPacketRaspberryPi =
                    DatagramPacket(message, message.size, raspberryPiAddr, turtleBotPort)
                socket.send(sendPacketRaspberryPi)  // 데이터 전송

                // TurtleBot으로부터의 응답 수신
                val receiveData = ByteArray(1024)
                val receivePacket = DatagramPacket(receiveData, receiveData.size)
                socket.receive(receivePacket)  // 응답 수신

                val response = String(receivePacket.data, 0, receivePacket.length).trim()  // 응답 메시지 변환

                // 메인 스레드에서 응답 메시지 UI에 표시
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingActivity, "응답 받음: $response", Toast.LENGTH_SHORT)
                        .show()
                }

            } catch (e: Exception) {
                // 예외 발생 시 사용자에게 실패 메시지 표시
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
