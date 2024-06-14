package com.kkai.elbus

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ConfFragment : Fragment() {

    private lateinit var themeButton: MaterialButton
    private lateinit var settingsbg : ScrollView
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_conf, container, false)
        themeButton = view.findViewById(R.id.button2)
        settingsbg = view.findViewById(R.id.settings_bg)
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        updateThemeButton(isDarkMode)

        themeButton.setOnClickListener {
            val newIsDarkMode = !isDarkMode
            sharedPref.edit().putBoolean("isDarkMode", newIsDarkMode).apply()
            updateThemeButton(newIsDarkMode)
            if (newIsDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun updateThemeButton(isDarkMode: Boolean) {
        if (isDarkMode) {
            themeButton.text = getString(R.string.set_theme_button_dark)
            themeButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.sic_dark_mode)
        } else {
            themeButton.text = getString(R.string.set_theme_button_light)
            themeButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.sic_light_mode)
        }
    }
}