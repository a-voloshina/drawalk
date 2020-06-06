package ru.nsu.fit.android.drawalk.modules.common

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager {

    private var sharedPreference: SharedPreferences? = null
    private lateinit var preferencesName: String

    fun init(context: Context) {
        if (!this::preferencesName.isInitialized) {
            preferencesName = "SharedPrefs"
        }
        sharedPreference = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }

    fun contains(key: String): Boolean {
        return sharedPreference?.contains(key)
            ?: throw Exception("SharedPreferences is not initialized")
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreference?.let {
            val editor = it.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }
    }

    fun getBoolean(key: String, defValue: Boolean = true): Boolean {
        return sharedPreference?.getBoolean(key, defValue) ?: defValue
    }
}