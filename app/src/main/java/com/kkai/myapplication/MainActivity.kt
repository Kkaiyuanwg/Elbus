package com.kkai.myapplication

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString

import java.io.IOException

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jsoup.Jsoup

private val client = OkHttpClient()
const val linesUrl = "https://itranvias.com/queryitr_v3.php?func=1"
const val timeUrl = "https://itranvias.com/queryitr_v3.php?&dato="
const val stopUrl = "https://itranvias.com/queryitr_v3.php?&dato=20160101T00322_es_0_"

class MainActivity : AppCompatActivity() {
    private lateinit var bMainLabel: TextView
    private lateinit var bSubLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bMainLabel = findViewById(R.id.bMainLabel)
        bSubLabel = findViewById(R.id.bSubLabel)

        CoroutineScope(Dispatchers.Main).launch {
            bMainLabel.text = getLeastTime("251").toString()
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun apiQuery(url: String): Any? = withContext(Dispatchers.IO) {
    val connection = URL(url).openConnection() as HttpURLConnection
    val json = Json { isLenient = true; ignoreUnknownKeys = true }
    connection.requestMethod = "GET"
    connection.connect()

    if (connection.responseCode == 200) {
        val data = connection.inputStream.bufferedReader().use { it.readText() }
        val obj = json.decodeFromString<Map<String, Any>>(data)
        println(obj)
    } else {
        null
    }
}

suspend fun getLeastTime(stop: String): Any? {
    val timeq = apiQuery("$timeUrl$stop&func=0")
    println(timeq)
    return timeq
}
