package com.example.mistybot

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class SettingActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var autoModeButton: RadioButton
    private lateinit var customModeButton: RadioButton
    private lateinit var humidityInput: EditText
    private lateinit var humidityInputLayout: LinearLayout
    private lateinit var saveButton: Button

    private val turtleBotPort = 8000 // TurtleBot의 포트 번호

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // UI 요소 초기화
        radioGroup = findViewById(R.id.radioGroup)
        autoModeButton = findViewById(R.id.autoModeButton)
        customModeButton = findViewById(R.id.customModeButton)
        humidityInput = findViewById(R.id.humidityInput)
        humidityInputLayout = findViewById(R.id.linearLayout2)
        saveButton = findViewById(R.id.saveButton)

        // 초기 상태 설정
        autoModeButton.isChecked = true
        humidityInputLayout.visibility = View.GONE // 기본으로 사용자 적정 습도 입력 숨김

        // RadioGroup 체크 상태 변경 리스너
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.autoModeButton -> {
                    humidityInputLayout.visibility = View.GONE // AUTO 모드 시 입력 필드 숨김
                }
                R.id.customModeButton -> {
                    humidityInputLayout.visibility = View.VISIBLE // 사용자 적정 모드 시 입력 필드 보이기
                }
            }
        }

        // 저장 버튼 동작
        saveButton.setOnClickListener {
            val selectedMode = if (autoModeButton.isChecked) "AUTO 모드" else "사용자 적정 모드"

            // 사용자 적정 모드일 때 습도 값 가져오기
            val humidityValue = if (customModeButton.isChecked) {
                humidityInput.text.toString()
            } else {
                "N/A"
            }

            if (customModeButton.isChecked && humidityValue.isEmpty()) {
                Toast.makeText(this, "습도 값을 입력해 주세요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "$selectedMode 선택됨. 적정 습도: $humidityValue", Toast.LENGTH_SHORT)
                    .show()

                // 사용자 적정 모드일 경우 습도 값 전송
                if (customModeButton.isChecked) {
                    sendHumidityToTurtleBot(humidityValue)
                }
            }
        }
    }

    private fun sendHumidityToTurtleBot(humidity: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var socket: DatagramSocket? = null
            try {
                // 현재 선택된 모드에 따라 "auto" 또는 "manual" 설정
                val mode = if (autoModeButton.isChecked) "auto" else "manual"

                // 모드와 습도 값을 조합하여 메시지 생성 (형식: "mode:value")
                val humidityValue = humidity.toIntOrNull() ?: 0
                val message = "$mode:$humidityValue".toByteArray()

                // 서버와 라즈베리파이 IP 주소 설정
                val serverAddr = InetAddress.getByName("192.168.134.106") // 서버 IP
                val raspberryPiAddr = InetAddress.getByName("192.168.134.109") // 라즈베리파이 IP

                // 지정한 포트 번호로 UDP 소켓 생성
                socket = DatagramSocket()

                // 서버 IP로 메시지 전송
                val sendPacketServer = DatagramPacket(message, message.size, serverAddr, turtleBotPort)
                socket.send(sendPacketServer)

                // 라즈베리파이 IP로 메시지 전송
                val sendPacketRaspberryPi =
                    DatagramPacket(message, message.size, raspberryPiAddr, turtleBotPort)
                socket.send(sendPacketRaspberryPi)

                // TurtleBot으로부터의 응답 수신
                val receiveData = ByteArray(1024)
                val receivePacket = DatagramPacket(receiveData, receiveData.size)
                socket.receive(receivePacket)

                val response = String(receivePacket.data, 0, receivePacket.length).trim()

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
