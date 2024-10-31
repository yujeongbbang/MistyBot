package com.example.mistybot

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val findDeviceButton: Button = findViewById(R.id.findDeviceButton)
        findDeviceButton.setOnClickListener {
            // DeviceSearchActivity로 이동하는 Intent 생성
            val intent = Intent(this, DeviceSearchActivity::class.java)
            startActivity(intent)
        }
    }
}
