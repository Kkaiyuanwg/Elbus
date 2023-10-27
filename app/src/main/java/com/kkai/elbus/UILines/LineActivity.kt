package com.kkai.elbus.UILines

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.kkai.elbus.R

class LineActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("hi")
    }
}