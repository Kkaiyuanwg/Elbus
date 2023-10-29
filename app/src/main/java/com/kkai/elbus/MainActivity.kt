package com.kkai.elbus

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout

import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.kkai.elbus.Utils.CustomPagerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var bMainLabel: TextView
    private lateinit var bSubLabel: TextView
    private lateinit var bStopLabel: TextView
    private lateinit var bTimerLabel: TextView
    private lateinit var bTextInput: AutoCompleteTextView
    private lateinit var bCarousel: ViewPager2

    private var isTimerRunning = false
    private var firstExecution = true
    private var updateDelay: Long = 30 * 1000
    private var countDownTimer: CountDownTimer? = null

    private var stopTimes: MutableList<Triple<String, String, String>> = mutableListOf(Triple("0", "?", "?"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base)

        val btn_click_me = findViewById(R.id.button) as Button
        val mDrawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        val navView: NavigationView = findViewById(R.id.nav_view)

        val menu = navView.menu // Get the menu of the NavigationView
        menuInflater.inflate(R.layout.drawer_menu, menu)

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_item1 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MainFragment())
                        .commit()
                    mDrawerLayout.closeDrawer(findViewById(R.id.nav_view))
                    true
                }
                R.id.nav_item2 -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, LineFragment())
                        .commit()
                    mDrawerLayout.closeDrawer(findViewById(R.id.nav_view))
                    true
                }
                // Add more cases as needed

                else -> false
            }
        }
        btn_click_me.setOnClickListener{
            println("pressed")
            mDrawerLayout.openDrawer(findViewById(R.id.nav_view));
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MainFragment())
                .commit()
        }

    }
}
