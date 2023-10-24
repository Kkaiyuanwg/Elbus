package com.kkai.elbus

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.Manifest
import android.location.Location

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.internal.Objects.ToStringHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import okhttp3.OkHttpClient

internal val client = OkHttpClient()

const val linesUrl = "https://itranvias.com/queryitr_v3.php?func=1"
const val timeUrl = "https://itranvias.com/queryitr_v3.php?&dato="
const val stopUrl = "https://itranvias.com/queryitr_v3.php?&dato=20160101T00322_es_0_"

var stopNumber: String = "1"
class CustomAutoCompleteTextView(context: Context) : androidx.appcompat.widget.AppCompatAutoCompleteTextView(context) {
    override fun enoughToFilter(): Boolean {
        return true
    }
}


class MainActivity : AppCompatActivity() {
    private lateinit var bMainLabel: TextView
    private lateinit var bSubLabel: TextView
    private lateinit var bStopLabel: TextView
    private lateinit var bTimerLabel: TextView
    private lateinit var bTextInput: AutoCompleteTextView
    private lateinit var bCarousel: ViewPager2

    private var isTimerRunning = false
    private var firstExecution = true
    private var updateDelay: Long = 30 * 1000
    private var countDownTimer: CountDownTimer? = null

    private var stopTimes: MutableList<Triple<String, String, String>> = mutableListOf(Triple("0", "?", "?"))

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates(this) { coordinates ->
                    println(coordinates)
                }
            } else {
                println("no loc")
            }
        }
    }

    @SuppressLint("MissingInflatedId", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val otherLayout = layoutInflater.inflate(R.layout.what_bus, null)

        bMainLabel = otherLayout.findViewById(R.id.bMainLabel)
        bSubLabel = otherLayout.findViewById(R.id.bSubLabel)
        bStopLabel = findViewById(R.id.bStopLabel)
        bTimerLabel = findViewById(R.id.bTimerLabel)
        bTextInput = findViewById(R.id.bStopInput)
        bCarousel = findViewById(R.id.bCarousel)

        requestLocationUpdates(this) {coor ->
            stopNumber = getClosestLocation(coor)?.id.toString()
        }

        val pAdapter = CustomPagerAdapter(this, bCarousel, stopTimes)
        bCarousel.adapter = pAdapter

        val aAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, getParadasArray())
        bTextInput.setAdapter(aAdapter)
        bTextInput.threshold = 1

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates(this) {coor ->
                stopNumber = getClosestLocation(coor)?.id.toString()
                startCountdownTimer(updateDelay, stopNumber)
                bStopLabel.text = "$stopNumber - ${stopName(stopNumber)}"
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
        }

        bTextInput.setOnEditorActionListener { _, actionId, event ->
            if ((actionId == EditorInfo.IME_ACTION_DONE) || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                val suggestions = bTextInput.adapter?.count ?: 0
                if (suggestions > 0) {
                    bTextInput.setText("")
                    bTextInput.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(bTextInput.windowToken, 0)
                    val firstSuggestion = bTextInput.adapter?.getItem(0).toString()
                    stopNumber = getFirstNumbers(firstSuggestion).toString()
                    getTime(stopNumber, this)
                    startCountdownTimer(updateDelay, stopNumber)
                    bStopLabel.text = "$stopNumber - ${stopName(stopNumber)}"
                }
                return@setOnEditorActionListener true
            }
            false
        }

        println("hi $stopNumber")
        startCountdownTimer(updateDelay, stopNumber)
        bStopLabel.text = "$stopNumber - ${stopName(stopNumber)}"
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
                    isTimerRunning = false
                    startCountdownTimer(updateDelay, number)
                }
            }.start()
            isTimerRunning = true
            getTime(stopNumber, this)
        }
    }

    private fun getTime(number: String, context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            val least = getLeastTime(number)
            if (least != null) {
                stopTimes = least
            }
            val pAdapter = CustomPagerAdapter(context, bCarousel, stopTimes)
            bCarousel.adapter = pAdapter

            if (least.toString() == "<1" || least.toString() == "0") {
                bSubLabel.text = "No da tiempo"
            } else if (least.toString() == "?") {
                bSubLabel.text = "No hay buses a esta hora"
            } else {
                try {
                    val lnum = least
                } catch (e: NumberFormatException) {
                    bSubLabel.text = "No hay buses a esta hora"
                }
            }

            startCountdownTimer(updateDelay, number)
        }
    }
}
