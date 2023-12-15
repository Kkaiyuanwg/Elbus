package com.kkai.elbus.Utils

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kkai.elbus.R
import com.kkai.elbus.getColorByNom

class CustomPagerAdapter(
    private val context: Context,
    private val viewPager: ViewPager2,
    private val pageTitles: MutableList<Pair<String, MutableList<MutableList<String>>>>
) : RecyclerView.Adapter<CustomPagerAdapter.PagerViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView1: TextView = itemView.findViewById(R.id.bMainLabel)
        val textView2: TextView = itemView.findViewById(R.id.bSubLabel)
        val textView3: TextView = itemView.findViewById(R.id.bMainSubLabel)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.what_bus, parent, false)
        return PagerViewHolder(view)
    }

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {

            fun getSecondElement(inputFirst: String, pairs: MutableList<Pair<String, MutableList<MutableList<String>>>>): MutableList<MutableList<String>>? {
                println(pairs)
                val matchingPair = pairs.find { it.first == inputFirst }
                return matchingPair?.second
            }

            try {
                holder.textView1.text = pageTitles[position].second[0][1]
                holder.textView3.text = pageTitles[position].second[1][1]
            } catch (e:IndexOutOfBoundsException) {
                holder.textView1.text = "?"
                holder.textView3.text = "?"
            }

            val linea = pageTitles[position].first
            val text = "Línea  $linea "
            val spannableString = SpannableString(text)

            val endIndex = text.length

            val backgroundColorSpan = BackgroundColorSpan(getColorByNom(linea))
            val textColorSpan = ForegroundColorSpan(Color.WHITE)

            spannableString.setSpan(backgroundColorSpan, 6, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(textColorSpan, 6, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            holder.textView2.text = spannableString
            holder.itemView.setOnClickListener {
                if (linea != "0") {
                    val buses = getSecondElement(linea, pageTitles).toString()
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("Linea ${linea}")
                        .setMessage(buses)
                        .show()
                } else {
                    MaterialAlertDialogBuilder(holder.itemView.context)
                        .setTitle("No hay buses")
                        .setMessage("Actualmente no hay buses para esta parada")
                        .show()
                }
                onItemClickListener?.onItemClick(position)
            }
        }

    override fun getItemCount(): Int = pageTitles.size

    fun setCurrentItem(position: Int) {
        viewPager.currentItem = position
    }
    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.onItemClickListener = listener
    }
}