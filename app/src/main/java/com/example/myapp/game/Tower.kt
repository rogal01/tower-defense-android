package com.example.myapp.game

data class Tower(
    val x: Float,
    val y: Float,
    var level: Int = 1,
    var range: Float = 200f,
    var damage: Float = 8f,
    var fireRate: Float = 1.0f,
    var fireTimer: Float = 0f,
    val size: Float = 35f,
    val type: TowerType = TowerType.ARROW
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

enum class TowerType(val emoji: String, val baseCost: Int) {
    ARROW("\uD83C\uDFF9", 30),
    MAGIC("\uD83E\uDDE8", 60),
    CANNON("\uD83D\uDCA3", 100),
    POISON("\u2620\uFE0F", 80),
    TESLA("\u26A1", 120)
}
