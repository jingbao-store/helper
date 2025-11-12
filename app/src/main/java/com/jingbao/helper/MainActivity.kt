package com.jingbao.helper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.widget.TextView
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.LinkProperties
import androidx.core.content.getSystemService
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
                Toast.makeText(this, getString(R.string.tip_open_wifi_settings), Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_bluetooth).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.tip_open_bt_settings), Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_key_sound).setOnClickListener {
            toggleKeySound()
        }

        findViewById<MaterialButton>(R.id.btn_system_settings).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.tip_open_system_settings), Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_dev_options).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.tip_open_dev_settings), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateWifiIpIfConnected()
    }

    private fun updateWifiIpIfConnected() {
        val tv = findViewById<TextView>(R.id.tv_ip)
        val cm: ConnectivityManager? = getSystemService()
        if (cm == null) {
            tv.text = ""
            return
        }
        val active = cm.activeNetwork ?: run {
            tv.text = ""
            return
        }
        val caps = cm.getNetworkCapabilities(active)
        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        if (!isWifi) {
            tv.text = ""
            return
        }
        val link: LinkProperties? = cm.getLinkProperties(active)
        val ipv4 = link?.linkAddresses?.firstOrNull { it.address.hostAddress?.contains(":") == false }?.address?.hostAddress
        tv.text = ipv4 ?: ""
    }

    private fun toggleKeySound() {
        val canWrite = Settings.System.canWrite(this)
        if (!canWrite) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            Toast.makeText(this, getString(R.string.tip_write_settings_request), Toast.LENGTH_LONG).show()
            return
        }
        try {
            val enabled = Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) == 1
            val newValue = if (enabled) 0 else 1
            Settings.System.putInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, newValue)
            Toast.makeText(this, if (newValue == 1) getString(R.string.key_sound_on) else getString(R.string.key_sound_off), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.key_sound_toggle_failed), Toast.LENGTH_SHORT).show()
        }
    }
}


