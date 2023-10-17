package com.kkai.myapplication

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
    private lateinit var bTimerLabel: TextView
    private lateinit var bTextInput: TextInputEditText

    private var isTimerRunning = false
    private var firstExecution = true
    private var updateDelay: Long = 30 * 1000
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bMainLabel = findViewById(R.id.bMainLabel)
        bSubLabel = findViewById(R.id.bSubLabel)
        bTimerLabel = findViewById(R.id.bTimerLabel)
        bTextInput = findViewById(R.id.bTextInput)

        bTextInput.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                // Acción a realizar cuando se presiona "Enter"
                val inputValue = bTextInput.text.toString()
                println(inputValue)
                startCountdownTimer(updateDelay, inputValue)
                // Realizar acciones con 'least'
                true // Indica que el evento ha sido manejado
            } else {
                false // Indica que el evento no ha sido manejado
            }
        })

        startCountdownTimer(updateDelay, "251")

    }

    private fun startCountdownTimer(millisInFuture: Long, number: String) {
        if (!isTimerRunning) {
            countDownTimer = object : CountDownTimer(millisInFuture, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if ((millisUntilFinished / 1000) > 0) {
                        val secondsRemaining = millisUntilFinished / 1000
                        bTimerLabel.text = "Próxima actualización en ${secondsRemaining}s"
                    } else {
                        bTimerLabel.text = "Actualizando..."
                    }
                }

                override fun onFinish() {
                    bTimerLabel.text = "Actualizando..."
                    if (firstExecution) {
                        firstExecution = false
                        getTime(number)
                    }
                    isTimerRunning = false
                    startCountdownTimer(updateDelay, number)
                }
            }.start()
            isTimerRunning = true
            getTime(number)
        }
    }

    fun getTime(number: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val least = getLeastTime(number)
            bMainLabel.text = least.toString()
            if (least?.toString() != "<1") {
                val lnum = least?.toInt()
                if (lnum != null) {
                    when {
                        lnum < 6 -> bSubLabel.text = "No te da tiempo"
                        lnum in 6 until 10 -> bSubLabel.text = "Corre"
                        lnum >= 10 -> bSubLabel.text = "Chill"
                    }
                }
            } else {
                bSubLabel.text = "OMG!"
            }

            startCountdownTimer(updateDelay, number)
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
