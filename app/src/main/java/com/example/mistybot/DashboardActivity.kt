package com.example.mistybot

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        private const val RASPBERRY_PI_IP = "192.168.212.109" // 라즈베리파이 IP 주소
        private const val RASPBERRY_PI_PORT = 8081 // 라즈베리파이 포트
        private const val WATER_SENSOR_PORT = 5006 // 수위 센서 데이터 포트
    }

    private lateinit var mapImageView: ImageView
    private lateinit var temperatureValue: TextView
    private lateinit var humidityValue: TextView
    private lateinit var batteryValue: TextView
    private lateinit var settingsIcon: ImageView
    private lateinit var waterLevelStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // View 초기화
        mapImageView = findViewById(R.id.mapImageView)
        temperatureValue = findViewById(R.id.temperatureValue)
        humidityValue = findViewById(R.id.humidityValue)
        batteryValue = findViewById(R.id.batteryValue)
        settingsIcon = findViewById(R.id.settingsIcon)
        waterLevelStatus = findViewById(R.id.waterLevelStatus)
        // waterQualityStatus = findViewById(R.id.waterQualityStatus)

        // settingsIcon 클릭 시 SettingActivity로 이동
        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        // 데이터 수신 시작
        receiveImageData()
        receiveSensorData()
        receiveWaterLevelData()
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
                    // Toast.makeText(this@DashboardActivity, "서버 연결이 끊어졌습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun receiveWaterLevelData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = DatagramSocket(WATER_SENSOR_PORT)
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                while (true) {
                    socket.receive(packet)
                    val message = String(packet.data, 0, packet.length).trim()

                    withContext(Dispatchers.Main) {
                        updateWaterStatus(message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                }
            }
        }
    }

    private fun updateWaterStatus(message: String) {
        when (message) {
            "Water: Full" -> {
                waterLevelStatus.text = "물이 충분합니다."
                waterLevelStatus.setTextColor(ContextCompat.getColor(this, R.color.waterLevelSufficient))
            }
            "Water: Low" -> {
                waterLevelStatus.text = "물이 부족합니다."
                waterLevelStatus.setTextColor(ContextCompat.getColor(this, R.color.waterLevelInsufficient))
            }
            else -> {
                waterLevelStatus.text = "알 수 없는 상태: $message"
                waterLevelStatus.setTextColor(ContextCompat.getColor(this, R.color.unknownStatus))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 리소스 해제
    }
}
