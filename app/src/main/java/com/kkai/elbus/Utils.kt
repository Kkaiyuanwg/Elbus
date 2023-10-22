package com.kkai.elbus

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.serialization.ExperimentalSerializationApi
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
suspend fun getLeastTime(stop: String): MutableList<Triple<String, String, String>>? {
    var obj: MutableList<Triple<String, String, String>>
    try {
        val data = apiQuery("$timeUrl$stop&func=0").toString()

        println(data)
        // Parse the JSON response
        val jsonObject = JSONObject(data)

        // Access the 'buses' object
        val busesObject = jsonObject.getJSONObject("buses")

        // Access the 'lineas' array
        val lineasArray = busesObject.getJSONArray("lineas")

        // Initialize an empty list to store lineas and tiempos
        val lineasTiemposList = mutableListOf<Triple<String, String, String>>()

        // Iterate through the 'lineas' array
        for (i in 0 until lineasArray.length()) {
            val lineaObject = lineasArray.getJSONObject(i)
            val linea = getNomComerById(lineaObject.getString("linea")).toString()

            // Access the 'buses' array for the current linea
            val busesArray = lineaObject.getJSONArray("buses")

            // Check if there are buses for the current linea
            if (busesArray.length() > 0) {
                val tiempo = busesArray.getJSONObject(0).getString("tiempo")
                val subtiempo = if (busesArray.length() > 1) {
                    busesArray.getJSONObject(1).getString("tiempo")
                } else {
                    "?"
                }
                lineasTiemposList.add(Triple(linea, tiempo, subtiempo))
            }
        }

        // Convert the list to an array
        obj = lineasTiemposList
        println(obj)

    } catch (e: JSONException) {
        obj = mutableListOf(Triple("0", "?", "?"))
    }
    return obj
}

fun getCoordinates(thiss: Context): Pair<Double, Double>? {
    var colordinates: Pair<Double, Double>? = null
    requestLocationUpdates(thiss) { coordinates ->
        colordinates = coordinates
    }
    return colordinates
}
fun requestLocationUpdates(thiss: Context, callback: (Pair<Double, Double>?) -> Unit) {
    var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(thiss)
    var coordinates: Pair<Double, Double>?
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            location?.let {
                coordinates = Pair(location.latitude, location.longitude)
                callback(coordinates)
            }
        }
}

fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    thiss: Context
) {
    if (requestCode == 123) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates(thiss) { coordinates ->
                println(coordinates)
            }
        } else {
            println("no loc")
        }
    }
}