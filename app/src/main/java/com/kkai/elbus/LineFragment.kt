package com.kkai.elbus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView

class LineFragment : Fragment() {
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_line, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById(R.id.blistView)

        val busList =
            parseBusData() // Implement a function to parse your JSON data into a List<Bus>
        val adapter = BusAdapter(requireContext(), busList)
        listView.adapter = adapter

    }
}

    class BusAdapter(
        private val context: Context,
        private val buses: List<busesClass>):
        ArrayAdapter<busesClass>(context, 0, buses) {

        @SuppressLint("SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = TextView(context)
                view.setPadding(16, 16, 16, 16) // Add padding if needed
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            }

            val bus = getItem(position)
            val busNumber = view as TextView

            busNumber.text = "Bus Number: ${bus?.nom_comer}, Color: ${bus?.color_linea}"

            return view
        }
    }