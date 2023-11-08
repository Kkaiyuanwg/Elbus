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
import androidx.recyclerview.widget.RecyclerView
import com.kkai.elbus.Utils.CustomPagerAdapter

class LineFragment : Fragment() {
    private lateinit var lineView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_line, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lineView = view.findViewById(R.id.bLineView)

        val busList =
            parseBusData() // Implement a function to parse your JSON data into a List<Bus>
        val adapter = BusAdapter(requireContext(), busList)
        lineView.adapter = adapter

    }
}