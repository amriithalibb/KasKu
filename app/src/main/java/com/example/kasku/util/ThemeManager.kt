package com.example.kasku.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "kasku_settings_prefs"
    private const val KEY_DARK_MODE = "key_is_dark_mode"
    private const val KEY_BATAS_ANGGARAN = "key_batas_anggaran"

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _batasAnggaran = MutableStateFlow(2000000L) // Default Rp 2.000.000
    val batasAnggaran: StateFlow<Long> = _batasAnggaran.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val prefs = getPrefs(context)
            _isDarkMode.value = prefs.getBoolean(KEY_DARK_MODE, false)
            _batasAnggaran.value = prefs.getLong(KEY_BATAS_ANGGARAN, 2000000L)
            isInitialized = true
        } catch (_: Exception) {
            // Defensive handling
        }
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        _isDarkMode.value = enabled
        try {
            getPrefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        } catch (_: Exception) { }
    }

    fun setBatasAnggaran(context: Context, limit: Long) {
        if (limit <= 0L) return
        _batasAnggaran.value = limit
        try {
            getPrefs(context).edit().putLong(KEY_BATAS_ANGGARAN, limit).apply()
        } catch (_: Exception) { }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}