package com.jingbao.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        findViewById<MaterialButton>(R.id.btn_wifi).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "无法打开 Wi-Fi 设置", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_bluetooth).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "无法打开蓝牙设置", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_key_sound).setOnClickListener {
            toggleKeySound()
        }

        findViewById<MaterialButton>(R.id.btn_system_settings).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "无法打开系统设置", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleKeySound() {
        val canWrite = Settings.System.canWrite(this)
        if (!canWrite) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Toast.makeText(this, "请授予“修改系统设置”权限后重试", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val enabled = Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) == 1
            val newValue = if (enabled) 0 else 1
            Settings.System.putInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, newValue)
            Toast.makeText(this, if (newValue == 1) "按键音：开" else "按键音：关", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "切换按键音失败", Toast.LENGTH_SHORT).show()
        }
    }
}


