package com.example.mistybot

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class DashboardActivity : AppCompatActivity() {

    companion object {
        private const val RASPBERRY_PI_IP = "192.168.134.109"  // 라즈베리파이 IP 주소
        private const val RASPBERRY_PI_PORT = 8081  // 라즈베리파이 포트
    }

    private lateinit var mapImageView: ImageView
    private lateinit var temperatureValue: TextView
    private lateinit var humidityValue: TextView
    private lateinit var batteryValue: TextView
    private lateinit var settingsIcon: ImageView
    private lateinit var autoButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard)

        // View 초기화
        mapImageView = findViewById(R.id.mapImageView)
        temperatureValue = findViewById(R.id.temperatureValue)
        humidityValue = findViewById(R.id.humidityValue)
        batteryValue = findViewById(R.id.batteryValue)
        settingsIcon = findViewById(R.id.settingsIcon)
        autoButton = findViewById(R.id.autoButton)

        // settingsIcon 클릭 시 SettingActivity로 이동
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        // 버튼 클릭 리스너 설정
        autoButton.setOnClickListener {
            startAutoHumidification()
        }

        // 데이터 수신 시작
        receiveImageData()
        receiveSensorData()
    }

    private fun receiveImageData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = DatagramSocket(8082)
                val buffer = ByteArray(65536)
                val packet = DatagramPacket(buffer, buffer.size)

                while (true) {
                    socket.receive(packet)
                    val imageData = packet.data.copyOf(packet.length)

                    val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

                    // UI 업데이트
                    runOnUiThread {
                        mapImageView.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun receiveSensorData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = DatagramSocket(RASPBERRY_PI_PORT)
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(
                    buffer,
                    buffer.size,
                    InetAddress.getByName(RASPBERRY_PI_IP),
                    RASPBERRY_PI_PORT
                )

                while (true) {
                    socket.receive(packet)
                    val jsonData = String(packet.data, 0, packet.length).trim()

                    val jsonObject = JSONObject(jsonData)
                    val temperature = jsonObject.getString("temperature")
                    val humidity = jsonObject.getString("humidity")
                    val battery = jsonObject.getString("battery")

                    withContext(Dispatchers.Main) {
                        temperatureValue.text = "$temperature°C"
                        humidityValue.text = "$humidity%"
                        batteryValue.text = "$battery%"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "서버 연결이 끊어졌습니다", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }


    // 자동 가습 시작하는 메서드
    private fun startAutoHumidification() {
        CoroutineScope(Dispatchers.IO).launch {
            val command = "start_auto_humidification" // 자동 가습 명령 메시지
            val response = UdpClient.sendMessage(command) // UdpClient를 사용하여 명령 전송

            withContext(Dispatchers.Main) {
                if (response != null) {
                    Toast.makeText(this@DashboardActivity, "자동 가습 프로세스 시작: $response", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DashboardActivity, "자동 가습 프로세스 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


