package com.kkai.elbus

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat.getSerializable
import androidx.fragment.app.Fragment

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.line_item_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val busNumber = view.findViewById<TextView>(R.id.busNumber)
        val squareText = view.findViewById<TextView>(R.id.square)
        val rounded = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)

        val bus = arguments?.getSerializable("bus", busesClass::class.java)

        squareText?.text = "Linea " + bus?.nom_comer

        rounded?.colorFilter = PorterDuffColorFilter(
            getColorByNom(bus?.nom_comer.toString()),
            PorterDuff.Mode.SRC_IN
        )
        squareText?.background = rounded

        val closeButton = view.findViewById<Button>(R.id.closelinebutton)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }
}