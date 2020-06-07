package ru.nsu.fit.android.drawalk

import androidx.multidex.MultiDexApplication
import ru.nsu.fit.android.drawalk.utils.PreferencesManager

class DraWalkApplication : MultiDexApplication() {
    companion object {
        val preferencesManager =
            PreferencesManager()
        fun preferencesManager() =
            preferencesManager
    }

    override fun onCreate() {
        super.onCreate()
        preferencesManager.init(this)
    }
}