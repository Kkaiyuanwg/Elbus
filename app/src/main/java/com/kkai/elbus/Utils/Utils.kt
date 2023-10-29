package com.kkai.elbus.Utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kkai.elbus.client
import com.kkai.elbus.getNomComerById
import com.kkai.elbus.timeUrl
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
import kotlin.math.*

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

    } catch (e: JSONException) {
        obj = mutableListOf(Triple("0", "?", "?"))
    }
    return obj
}

fun requestLocationUpdates(thiss: Context, callback: (Pair<Double, Double>?) -> Unit) {
    var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(thiss)
    var coordinates: Pair<Double, Double>?
    if (ActivityCompat.checkSelfPermission(
            thiss,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            thiss,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                coordinates = Pair(location.latitude, location.longitude)
                println("Latitude: ${coordinates!!.first}, Longitude: ${coordinates!!.second}")
                callback(coordinates)
            } ?: run {
                println("Location is null")
                callback(null)
            }
    }.addOnFailureListener { exception ->
            println("Location retrieval failed: $exception")
            callback(null)
        }
}

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Radius of the Earth in kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return R * c
}