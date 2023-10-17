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
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jsoup.Jsoup
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
            val least = getLeastTime("251")?.toInt()
            bMainLabel.text = least.toString()
            if (least != null) {
                when {
                    least < 6 -> bSubLabel.text = "No te da bro"
                    least in 6 until 10 -> bSubLabel.text = "Corre"
                    least >= 10 -> bSubLabel.text = "Chill bro"
                }
            } else {
                bSubLabel.text = "OMG!"
            }
        }
    }
}

suspend fun apiQuery(url: String): Any? = suspendCoroutine { continuation ->
    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            continuation.resume(null) // Resume with null in case of failure
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) {
                    continuation.resume(null) // Resume with null in case of non-successful response
                } else {
                    continuation.resume(response.body!!.string()) // Resume with the response body
                }
            }
        }
    })
}

@OptIn(ExperimentalSerializationApi::class)
suspend fun getLeastTime(stop: String): String? {
    val data = apiQuery("$timeUrl$stop&func=0").toString()
    val obj = JSONObject(data)
        .getJSONObject("buses")
        .getJSONArray("lineas")
        .getJSONObject(0)
        .getJSONArray("buses")
        .getJSONObject(0)
        .getString("tiempo")
    return obj
}
