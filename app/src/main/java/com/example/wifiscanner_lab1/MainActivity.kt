package com.example.wifiscanner_lab1

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint

class MainActivity : AppCompatActivity() {

    private lateinit var wifiManager: WifiManager
    private lateinit var adapter: ArrayAdapter<String>
    private val scanResults = mutableListOf<ScanResult>()

    private val PERM_REQUEST = 123

    // Приёмник широковещательных сообщений о готовности результатов
    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    refreshList()
                } else {
                    Toast.makeText(context, "Scan Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val sr = scanResults[position]
            val i = Intent(this, DetailActivity::class.java).apply {
                putExtra("SSID", sr.SSID)
                putExtra("BSSID", sr.BSSID)
                putExtra("LEVEL", sr.level)
                putExtra("FREQ", sr.frequency)
                putExtra("CAPS", sr.capabilities)
            }
            startActivity(i)
        }

        findViewById<Button>(R.id.btnScan).setOnClickListener {
            checkPermissionAndScan()
        }

        // Регистрация приемника
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scanReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(scanReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(scanReceiver)
    }

    private fun checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERM_REQUEST
            )
        } else {
            doScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doScan()
        } else {
            Toast.makeText(this, "Cannot scan without geolocation permission",
                Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun doScan() {
        val started = wifiManager.startScan()
        if (!started) {
            Toast.makeText(this, "Scan denied, using previous results",
                Toast.LENGTH_SHORT).show()
            refreshList()
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshList() {
        scanResults.clear()
        scanResults.addAll(wifiManager.scanResults)
        val names = scanResults.map { it.SSID.ifEmpty { "<Hidden network>" } }
        adapter.clear()
        adapter.addAll(names)
        adapter.notifyDataSetChanged()
    }
}