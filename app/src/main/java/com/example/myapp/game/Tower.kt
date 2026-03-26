package com.example.myapp.game

enum class TargetingMode(val label: String) {
    CLOSE("Close"),
    FIRST("First"),
    LAST("Last"),
    STRONG("Strong");

    fun next(): TargetingMode = entries[(ordinal + 1) % entries.size]
}

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
    var targetingMode: TargetingMode = TargetingMode.CLOSE
) {
    fun update(dt: Float) {
        if (fireTimer > 0) fireTimer -= dt
    }

    fun canFire(): Boolean = fireTimer <= 0f

    fun fire() {
        fireTimer = 1f / fireRate
    }

    fun distanceTo(ex: Float, ey: Float): Float {
        val dx = x - ex
        val dy = y - ey
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun upgradeCost(): Int = level * 50

    fun upgrade() {
        level++
        damage *= 1.4f
        range += 15f
        fireRate *= 1.15f
    }
}

enum class TowerType(val emoji: String, val baseCost: Int, val baseDamage: Float, val baseRange: Float, val baseFireRate: Float) {
    ARROW("\uD83C\uDFF9", 30, 8f, 200f, 1.2f),
    MAGIC("\uD83E\uDDE8", 60, 14f, 220f, 0.8f),
    CANNON("\uD83D\uDCA3", 100, 30f, 180f, 0.5f),
    POISON("\u2620\uFE0F", 80, 6f, 210f, 1.0f),
    TESLA("\u26A1", 120, 20f, 250f, 0.7f)
}
