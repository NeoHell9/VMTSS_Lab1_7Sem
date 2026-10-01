package com.example.wifiscanner_lab1

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Получаем данные из Intent
        val ssid = intent.getStringExtra("SSID") ?: "-"
        val bssid = intent.getStringExtra("BSSID") ?: "-"
        val level = intent.getIntExtra("LEVEL", 0)
        val freq = intent.getIntExtra("FREQ", 0)
        val caps = intent.getStringExtra("CAPS") ?: ""

        // Формируем список строк для вывода
        val items = listOf(
            "SSID: $ssid",
            "BSSID: $bssid",
            "Signal level: $level dBm (${signalDescription(level)})",
            "Frequency: $freq МГц",
            "Encryption: ${parseEncryption(caps)}",
            "Technologies: ${parseTechnologies(caps)}"
        )

        // Находим ListView и устанавливаем адаптер
        findViewById<ListView>(R.id.detailList).adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
    }

    // Словесное описание уровня сигнала
    private fun signalDescription(level: Int): String = when {
        level > -50 -> "Excellent"
        level in -60..-50 -> "Good"
        level in -70..-60 -> "Fair"
        level in -90..-70 -> "Weak"
        else -> "No signal"
    }

    // Определяем тип шифрования по строке capabilities
    private fun parseEncryption(caps: String): String = when {
        caps.contains("WPA3") -> "WPA3"
        caps.contains("WPA2") -> "WPA2"
        caps.contains("WPA")  -> "WPA"
        caps.contains("WEP")  -> "WEP"
        else -> "Open"
    }

    // Извлекаем поддерживаемые технологии из квадратных скобок: [ESS][WPS] -> ESS, WPS
    private fun parseTechnologies(caps: String): String {
        val regex = Regex("\\[([^\\]]+)]")
        val techs = regex.findAll(caps).map { it.groupValues[1] }.toList()
        return if (techs.isEmpty()) "—" else techs.joinToString(", ")
    }
}