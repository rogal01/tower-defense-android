package com.example.myapp.game

/**
 * Dynamic atmospheric weather events that occur during battle waves.
 * Alters visuals, audio ambience, enemy combat traits, and battle rewards.
 */
enum class WeatherEvent(
    val emoji: String,
    val displayName: String,
    val description: String,
    val goldMultiplier: Float = 1.0f,
    val enemySpeedMultiplier: Float = 1.0f,
    val enemyDamageMultiplier: Float = 1.0f,
    val spellCooldownMultiplier: Float = 1.0f,
    val heroAttackSpeedMultiplier: Float = 1.0f,
    val hasHeavenlyLightning: Boolean = false,
    val bonusDiamondsOnClear: Int = 0
) {
    CLEAR(
        emoji = "🌤️",
        displayName = "Clear Skies",
        description = "Calm atmospheric conditions."
    ),

    BLOOD_MOON(
        emoji = "🩸",
        displayName = "Blood Moon",
        description = "Monsters are enraged (+15% Spd, +10% Dmg)! Kills yield 2× Gold and wave clear awards bonus Diamonds!",
        goldMultiplier = 2.0f,
        enemySpeedMultiplier = 1.15f,
        enemyDamageMultiplier = 1.10f,
        bonusDiamondsOnClear = 2
    ),

    THUNDERSTORM(
        emoji = "⚡",
        displayName = "Thunderstorm",
        description = "Violent tempest skies! Heavenly lightning periodically blasts enemy clusters with massive shockwaves!",
        hasHeavenlyLightning = true
    ),

    SOLAR_ECLIPSE(
        emoji = "☀️",
        displayName = "Solar Eclipse",
        description = "Celestial corona energizes your forces! Spell cooldowns -35% and Hero attack speed +30%!",
        spellCooldownMultiplier = 0.65f,
        heroAttackSpeedMultiplier = 1.30f
    )
}
