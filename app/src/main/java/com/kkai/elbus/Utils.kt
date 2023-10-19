package com.kkai.elbus

import android.content.res.TypedArray
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
suspend fun getLeastTime(stop: String): MutableList<Pair<String, String>>? {
    var obj: MutableList<Pair<String, String>>
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
        val lineasTiemposList = mutableListOf<Pair<String, String>>()

        // Iterate through the 'lineas' array
        for (i in 0 until lineasArray.length()) {
            val lineaObject = lineasArray.getJSONObject(i)
            val linea = getNomComerById(lineaObject.getString("linea")).toString()

            // Access the 'buses' array for the current linea
            val busesArray = lineaObject.getJSONArray("buses")

            // Check if there are buses for the current linea
            if (busesArray.length() > 0) {
                val tiempo = busesArray.getJSONObject(0).getString("tiempo")
                lineasTiemposList.add(linea to tiempo)
            }
        }

        // Convert the list to an array
        obj = lineasTiemposList

        // Print the resulting array
        obj.forEach { (linea, tiempo) ->
            println("Linea: $linea, Tiempo: $tiempo")
        }
    } catch (e: JSONException) {
        obj = mutableListOf(Pair("a", "?"))
    }
    return obj
}

