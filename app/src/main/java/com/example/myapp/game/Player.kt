package com.example.myapp.game

import android.graphics.PointF

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    var targetX: Float = 0f,
    var targetY: Float = 0f,
    var speed: Float = 300f,
    var attackRange: Float = 150f,
    var attackDamage: Float = 10f,
    var attackCooldown: Float = 0.5f,
    var attackTimer: Float = 0f,
    var size: Float = 40f,
    var hp: Float = 100f,
    var maxHp: Float = 100f
) {
    fun moveTo(tx: Float, ty: Float) {
        targetX = tx
        targetY = ty
    }

    fun update(dt: Float) {
        // Sub-step to prevent glitching at high game speeds
        val maxStep = 0.02f
        var remaining = dt
        while (remaining > 0f) {
            val step = remaining.coerceAtMost(maxStep)
            remaining -= step
            val dx = targetX - x
            val dy = targetY - y
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (dist > 5f) {
                val move = speed * step
                if (move >= dist) {
                    x = targetX
                    y = targetY
                } else {
                    x += (dx / dist) * move
                    y += (dy / dist) * move
                }
            }
        }
        if (attackTimer > 0) attackTimer -= dt
    }

    fun canAttack(): Boolean = attackTimer <= 0f

    fun attack() {
        attackTimer = attackCooldown
    }

    fun distanceTo(tx: Float, ty: Float): Float {
        val dx = x - tx; val dy = y - ty
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
