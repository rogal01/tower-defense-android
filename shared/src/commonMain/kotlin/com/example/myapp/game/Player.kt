package com.example.myapp.game

import kotlin.math.sqrt

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
        val engine = GameEngineHolder.engine
        // Clamp to playable area: stay within screen bounds and below sky line
        var clampedX = tx
        var clampedY = ty
        if (engine != null) {
            val groundTop = engine.screenH * 0.12f + size  // below sky
            clampedX = clampedX.coerceIn(size, engine.screenW - size)
            clampedY = clampedY.coerceIn(groundTop, engine.screenH - size)
        }
        // Prevent moving into river: clamp to nearest non-river point
        if (engine != null && engine.isPointOnRiver(clampedX, clampedY)) {
            // Find closest point outside river by stepping away along the vector
            var safeX = clampedX
            var safeY = clampedY
            val cx = x
            val cy = y
            val maxStep = 10
            var found = false
            for (i in 1..maxStep) {
                val t = i / maxStep.toFloat()
                val testX = cx + (clampedX - cx) * (1 - t)
                val testY = cy + (clampedY - cy) * (1 - t)
                if (!engine.isPointOnRiver(testX, testY)) {
                    safeX = testX
                    safeY = testY
                    found = true
                    break
                }
            }
            targetX = safeX
            targetY = safeY
        } else {
            targetX = clampedX
            targetY = clampedY
        }
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
            val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
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
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
}
