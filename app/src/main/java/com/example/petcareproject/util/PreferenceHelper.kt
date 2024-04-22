package com.example.petcareproject.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceHelper(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AuthPreferences", Context.MODE_PRIVATE)

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("FirstLaunch", true)
        set(value) = prefs.edit().putBoolean("FirstLaunch", value).apply()
}
