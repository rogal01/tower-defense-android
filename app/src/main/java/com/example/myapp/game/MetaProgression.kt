package com.example.myapp.game

import android.content.SharedPreferences
import com.example.myapp.SoundManager
import com.example.myapp.SfxType

internal class MetaProgression(
    private val engine: GameEngine,
    private val prefs: SharedPreferences
) {

    fun checkAchievement(id: String) {
        val achievement = engine.achievements.find { it.id == id } ?: return
        if (achievement.unlocked) return

        achievement.unlocked = true
        prefs.edit().putBoolean("ach_$id", true).apply()
        engine.newAchievement = achievement
        engine.achievementBannerTimer = 3f
        SoundManager.play(SfxType.ACHIEVEMENT)
    }

    fun checkUpgradeAll() {
        if (
            engine.playerDamageLevel >= 2 &&
            engine.playerSpeedLevel >= 2 &&
            engine.playerHpLevel >= 2 &&
            engine.baseHpLevel >= 2
        ) {
            checkAchievement("upgrade_all")
        }
    }
}
