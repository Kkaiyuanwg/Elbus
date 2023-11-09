package com.kkai.elbus

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.kkai.elbus.Utils.CustomPagerAdapter
import com.kkai.elbus.Utils.getLeastTime
import com.kkai.elbus.Utils.requestLocationUpdates
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


class MainFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bStopLabel = view.findViewById(R.id.bStopLabel)
        bTimerLabel = view.findViewById(R.id.bTimerLabel)
        bTextInput = view.findViewById(R.id.bStopInput)
        bCarousel = view.findViewById(R.id.bCarousel)

        println("hi")
        requestLocationPermission()

        requestLocationUpdates(requireActivity()) { coor ->
            stopNumber = getClosestLocation(coor)?.id.toString()
        }

        val pAdapter = CustomPagerAdapter(requireActivity(), bCarousel, stopTimes)
        bCarousel.adapter = pAdapter

        val aAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_dropdown_item_1line,
            getParadasArray()
        )
        bTextInput.setAdapter(aAdapter)
        bTextInput.threshold = 1

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            requestLocationUpdates(requireActivity()) { coor ->
                stopNumber = getClosestLocation(coor)?.id.toString()
                startCountdownTimer(updateDelay, stopNumber)
                bStopLabel.text = "$stopNumber - ${stopName(stopNumber)}"
            }
        } else
        {
            ActivityCompat.requestPermissions(
                requireActivity() as Activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123
            )
        }

        bTextInput.setOnEditorActionListener { _, actionId, event ->
            if ((actionId == EditorInfo.IME_ACTION_DONE) || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                val suggestions = bTextInput.adapter?.count ?: 0
                if (suggestions > 0) {
                    bTextInput.setText("")
                    bTextInput.clearFocus()
                    val imm: InputMethodManager =
                        requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(bTextInput.windowToken, 0)
                    val firstSuggestion = bTextInput.adapter?.getItem(0).toString()
                    stopNumber = getFirstNumbers(firstSuggestion).toString()
                    getTime(stopNumber, requireContext())
                    startCountdownTimer(updateDelay, stopNumber)
                    bStopLabel.text = "$stopNumber - ${stopName(stopNumber)}"
                }
                return@setOnEditorActionListener true
            }
            false
        }

        if (!isTimerRunning)
        {
            startCountdownTimer(updateDelay, stopNumber)
        }
        bStopLabel.text = "$stopNumber - ${stopName(stopNumber)}"
    }

    private fun requestLocationPermission() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Location Services Required")
        builder.setMessage("Please enable location services to use this feature.")
        builder.setPositiveButton("Go to Settings") { _, _ ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { _, _ -> }
        builder.show()
    }

    private fun startCountdownTimer(millisInFuture: Long, number: String) {
        if (!isTimerRunning && isAdded) {
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
            getTime(stopNumber, requireContext())
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
