package com.example.myapp.game

/** Sound effect types used throughout the game */
enum class SfxType {
    ARROW_FIRE, MAGIC_FIRE, CANNON_FIRE, POISON_FIRE, TESLA_FIRE, ICE_FIRE,
    FLAME_FIRE, NECRO_FIRE, BALLISTA_FIRE, VORTEX_FIRE, HEALER_FIRE,
    ENEMY_DIE, BOSS_APPEAR,
    POWER_FIREBALL, POWER_FREEZE, POWER_HEAL, POWER_LIGHTNING,
    WAVE_START, WAVE_COMPLETE, COMBO, ACHIEVEMENT,
    GAME_OVER, VICTORY,
    TOWER_PLACE, TOWER_UPGRADE, TOWER_SELL, TOWER_ABILITY,
    BASE_HIT, PLAYER_ATTACK,
    BOSS_CHARGE, BOSS_SUMMON, BOSS_HEAL, BOSS_AOE,
    BOSS_SHIELD, BOSS_ROAR, BOSS_TELEPORT, BOSS_DRAIN, BOSS_QUAKE, BOSS_SPLIT,
    DIAMOND_DROP, PLAYER_UPGRADE, UI_CLICK,
    WEATHER_THUNDER, WEATHER_BLOOD_MOON, WEATHER_ECLIPSE,
    HERO_SPECIAL, REWARD_CHEST, STUN_ZAP
}

/** Platform-agnostic audio interface — implemented per platform */
interface GameAudio {
    fun play(sfx: SfxType)
}

/** No-op audio for tests or platforms without sound */
object SilentAudio : GameAudio {
    override fun play(sfx: SfxType) {}
}
