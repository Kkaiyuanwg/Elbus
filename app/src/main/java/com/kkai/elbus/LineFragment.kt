package com.kkai.elbus

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class LineFragment : Fragment() {
    private lateinit var listView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_line, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.blistView)

        val busList = parseBusData()
        val adapter = BusAdapter(requireContext(), busList)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->
            // Get the clicked item
            val bus = adapter.getItem(position)

            // Create a new fragment and pass the clicked item data to it
            val detailFragment = DetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("bus", bus)
                }
            }

            // Replace the current fragment with the new one
            parentFragmentManager.beginTransaction()
                .replace(R.id.LineFragment, detailFragment)
                .addToBackStack(null)
                .commit()
        }
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
                    view = LayoutInflater.from(context).inflate(R.layout.line_item, parent, false)
                }

                val bus = getItem(position)
                val busNumber = view?.findViewById<TextView>(R.id.busNumber)
                val squareText = view?.findViewById<TextView>(R.id.square)
                val rounded = ContextCompat.getDrawable(context, R.drawable.rounded_bg)

                squareText?.text = bus?.nom_comer

                rounded?.colorFilter = PorterDuffColorFilter(
                    getColorByNom(bus?.nom_comer.toString()),
                    PorterDuff.Mode.SRC_IN
                )

                squareText?.background = rounded

                busNumber?.text = "${bus?.orig_linea}\n${bus?.dest_linea}"

                view?.setOnClickListener {
                    val busi = getItem(position)

                    // Create a new fragment and pass the clicked item data to it
                    val detailFragment = DetailFragment().apply {
                        arguments = Bundle().apply {
                            putSerializable("bus", busi)
                        }
                    }

                    // Replace the current fragment with the new one
                    (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit()
                }

                return view!!
            }
        }