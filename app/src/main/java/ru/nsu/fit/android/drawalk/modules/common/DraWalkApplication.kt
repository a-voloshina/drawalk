package ru.nsu.fit.android.drawalk.modules.common

import android.app.Application

class DraWalkApplication : Application() {
    companion object {
        val preferencesManager = PreferencesManager()
        fun preferencesManager() = preferencesManager
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager.init(this)
    }
}