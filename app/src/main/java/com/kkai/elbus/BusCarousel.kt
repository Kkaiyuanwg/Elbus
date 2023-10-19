package com.kkai.elbus

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.CoroutineScope

class CustomPagerAdapter(
    private val context: Context,
    private val viewPager: ViewPager2,
    private val pageTitles: MutableList<Pair<String, String>>
) : RecyclerView.Adapter<CustomPagerAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView1: TextView = itemView.findViewById(R.id.bMainLabel)
        val textView2: TextView = itemView.findViewById(R.id.bSubLabel)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.what_bus, parent, false)
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.textView1.text = pageTitles[position].second
        val linea = pageTitles[position].first
        holder.textView2.text = "Línea $linea"
    }

    override fun getItemCount(): Int = pageTitles.size

    fun setCurrentItem(position: Int) {
        viewPager.currentItem = position
    }
}