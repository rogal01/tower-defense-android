package com.example.myapp.game

import kotlin.math.hypot

/**
 * Damage categories — each enemy archetype has its own resistance/weakness profile
 * (see GameData.getResistance). Roughly speaking, physical works against unarmored,
 * magic ignores armor, and ice/fire shine against opposite-affinity enemies.
 */
enum class DamageType {
    PHYSICAL,   // Arrow, Ballista
    MAGIC,      // Magic, Vortex
    EXPLOSIVE,  // Cannon
    POISON,     // Poison
    ELECTRIC,   // Tesla
    ICE,        // Ice
    FIRE,       // Flame
    DARK        // Necro
}

/**
 * Targeting priority when a tower has multiple enemies in range.
 *  - CLOSE  picks the nearest enemy (good against fast rushers).
 *  - FIRST  picks the enemy furthest along the path (default — finishes off escapees).
 *  - LAST   picks the enemy closest to spawn (good for chip damage).
 *  - STRONG picks the highest-HP enemy (good for bosses/elites).
 */
enum class TargetingMode(val label: String) {
    CLOSE("Close"),
    FIRST("First"),
    LAST("Last"),
    STRONG("Strong");

    /** Cycles to the next mode — wired to the long-press cycle button in the HUD. */
    fun next(): TargetingMode = entries[(ordinal + 1) % entries.size]
}

/**
 * Runtime state for a placed tower. We use a `data class` so equality and toString
 * fall out for free, which makes save/restore and unit tests cleaner.
 *
 * Note: positional fields ([x], [y], [type], [size]) are immutable — once placed,
 * a tower never moves. Everything else mutates as the game ticks.
 */
data class Tower(
    val x: Float,
    val y: Float,
    var level: Int = 1,
    var range: Float = 200f,
    var damage: Float = 8f,
    var fireRate: Float = 1.0f,
    var fireTimer: Float = 0f,
    val size: Float = 35f,
    val type: TowerType = TowerType.ARROW,
    var targetingMode: TargetingMode = TargetingMode.CLOSE,
    var abilityTimer: Float = 0f,
    /** Thorns jam: when > 0 the tower's effective fire rate is halved. */
    var thornJamTimer: Float = 0f,
    /** Recoil animation timer — set briefly on every shot for a satisfying kick. */
    var recoilTimer: Float = 0f,
    // --- Stats tracking (shown on the end-of-run summary screen) ---
    var totalKills: Int = 0,
    var totalDamageDealt: Float = 0f
) {
    /** Advance all per-tower timers by [dt] seconds. Called once per game tick. */
    fun update(dt: Float) {
        if (fireTimer > 0) fireTimer -= dt
        if (abilityTimer > 0) abilityTimer -= dt
        if (thornJamTimer > 0) thornJamTimer -= dt
        if (recoilTimer > 0) recoilTimer -= dt
    }

    /** Returns true when the tower is off cooldown AND not currently jammed by thorns. */
    fun canFire(): Boolean = fireTimer <= 0f && thornJamTimer <= 0f

    /** Resets the fire cooldown and triggers the recoil animation. */
    fun fire() {
        fireTimer = 1f / fireRate
        recoilTimer = 0.15f
    }

    /**
     * Euclidean distance to a point. Uses [hypot] instead of manual sqrt so it stays
     * platform-pure for any future iOS/JS commonMain consumers and dodges overflow
     * for very large coordinates.
     */
    fun distanceTo(ex: Float, ey: Float): Float = hypot(x - ex, y - ey)

    /** Gold cost of the next upgrade. Scales linearly — predictable for the player. */
    fun upgradeCost(): Int = level * 50

    /** Apply a single level-up. Numbers tuned for ~10 levels worth of headroom. */
    fun upgrade() {
        level++
        damage *= 1.4f
        range += 15f
        fireRate *= 1.15f
    }

    /** Refund value if the player sells this tower. 60% base + small per-level bonus. */
    fun sellValue(): Int = (type.baseCost * 0.6f).toInt() + (level - 1) * 15

    fun canUseAbility(): Boolean = abilityTimer <= 0f

    fun useAbility() {
        abilityTimer = type.abilityCooldown
    }
}

enum class TowerType(
    val emoji: String,
    val baseCost: Int,
    val baseDamage: Float,
    val baseRange: Float,
    val baseFireRate: Float,
    val damageType: DamageType = DamageType.PHYSICAL,
    val abilityCooldown: Float = 30f,
    val abilityName: String = ""
) {
    ARROW("\uD83C\uDFF9", 30, 8f, 200f, 1.2f, DamageType.PHYSICAL, 25f, "Volley"),
    MAGIC("\uD83E\uDDE8", 60, 14f, 220f, 0.8f, DamageType.MAGIC, 30f, "Arcane Blast"),
    CANNON("\uD83D\uDCA3", 100, 30f, 180f, 0.5f, DamageType.EXPLOSIVE, 35f, "Napalm"),
    POISON("\u2620\uFE0F", 80, 6f, 210f, 1.0f, DamageType.POISON, 28f, "Plague"),
    TESLA("\u26A1", 120, 20f, 250f, 0.7f, DamageType.ELECTRIC, 32f, "Overcharge"),
    ICE("\u2744\uFE0F", 70, 0f, 230f, 0f, DamageType.ICE, 25f, "Deep Freeze"),
    FLAME("\uD83D\uDD25", 90, 12f, 190f, 0.9f, DamageType.FIRE, 30f, "Inferno"),
    NECRO("\uD83D\uDC80", 110, 18f, 200f, 0.6f, DamageType.DARK, 35f, "Soul Harvest"),
    BALLISTA("\uD83C\uDFAF", 140, 50f, 300f, 0.3f, DamageType.PHYSICAL, 40f, "Siege Shot"),
    VORTEX("\uD83C\uDF00", 100, 4f, 240f, 1.5f, DamageType.MAGIC, 28f, "Singularity"),
    HEALER("\uD83D\uDC9A", 80, 0f, 220f, 0.2f, DamageType.MAGIC, 20f, "Mass Heal")
}
