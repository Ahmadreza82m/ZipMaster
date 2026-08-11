package com.example.zipmaster

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import java.util.Locale

class ZipMasterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applyLanguage()
    }

    fun applyLanguage() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val lang = prefs.getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
