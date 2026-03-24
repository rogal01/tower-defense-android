package com.example.myapp

import android.content.Context

/**
 * Centralized audio settings manager.
 * Reads volume preferences so game code can query them.
 */
object SoundManager {

    var masterVolume: Float = 0.8f
        private set
    var musicVolume: Float = 0.7f
        private set
    var sfxVolume: Float = 0.8f
        private set

    /** Effective SFX volume (master × sfx) in 0..1 range */
    val effectiveSfx: Float get() = masterVolume * sfxVolume

    /** Effective music volume (master × music) in 0..1 range */
    val effectiveMusic: Float get() = masterVolume * musicVolume

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)
        masterVolume = prefs.getInt("master_volume", 80) / 100f
        musicVolume = prefs.getInt("music_volume", 70) / 100f
        sfxVolume = prefs.getInt("sfx_volume", 80) / 100f
    }

    /** Whether screen shake is enabled */
    fun isShakeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("screen_shake", true)
    }

    /** Whether FPS counter should be shown */
    fun isShowFps(context: Context): Boolean {
        val prefs = context.getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("show_fps", false)
    }
}
