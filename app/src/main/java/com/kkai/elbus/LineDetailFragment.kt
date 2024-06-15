package com.kkai.elbus

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class DetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_line_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val busNumber = view.findViewById<TextView>(R.id.busNumber)
        val squareText = view.findViewById<TextView>(R.id.square)
        val rounded = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bg)

        val layout = view.findViewById<LinearLayout>(R.id.innerLayout)

        val bus = arguments?.getSerializable("bus", busesClass::class.java)

        val routesList: List<rutasClass>? = getStopsByBusId(bus?.id)
        val firstroute = routesList?.get(0)
        val secondroute = routesList?.get(1)

        squareText?.text = bus?.nom_comer

        rounded?.colorFilter = PorterDuffColorFilter(
            getColorByNom(bus?.nom_comer.toString()),
            PorterDuff.Mode.SRC_IN
        )
        squareText?.background = rounded

        val closeButton = view.findViewById<Button>(R.id.closelinebutton)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        firstroute?.paradas?.forEach { stopId ->
            val inflater = LayoutInflater.from(context)
            val views = inflater.inflate(R.layout.line_item_detail, layout, false)

            val textView = views.findViewById<TextView>(R.id.busStop)

            textView.text = "${stopName(stopId.toString())} ${getLinksFromStopId(stopId)}"

            layout.addView(views)
        }

    }
}