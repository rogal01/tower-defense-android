package com.example.myapp.game

data class Enemy(
    var x: Float,
    var y: Float,
    val speed: Float,
    var hp: Float,
    val maxHp: Float,
    val goldReward: Int,
    val damage: Float,
    val size: Float = 30f,
    val type: EnemyType = EnemyType.GOBLIN,
    var hitFlash: Float = 0f
) {
    fun update(dt: Float, baseX: Float, baseY: Float) {
        val dx = baseX - x
        val dy = baseY - y
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if (dist > size) {
            x += (dx / dist) * speed * dt
            y += (dy / dist) * speed * dt
        }
        if (hitFlash > 0) hitFlash -= dt
    }

    fun distanceTo(tx: Float, ty: Float): Float {
        val dx = x - tx
        val dy = y - ty
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun isAtBase(baseX: Float, baseY: Float): Boolean {
        return distanceTo(baseX, baseY) < size + 30f
    }

    fun isDead(): Boolean = hp <= 0
}

enum class EnemyType(val emoji: String, val color: Int) {
    GOBLIN("\uD83D\uDC7A", 0xFF4CAF50.toInt()),
    SKELETON("\uD83D\uDC80", 0xFFBDBDBD.toInt()),
    ORC("\uD83D\uDC79", 0xFF8BC34A.toInt()),
    DEMON("\uD83D\uDD25", 0xFFFF5722.toInt()),
    DRAGON("\uD83D\uDC09", 0xFFFF9800.toInt()),
    BOSS("\u2620\uFE0F", 0xFFE91E63.toInt())
}
