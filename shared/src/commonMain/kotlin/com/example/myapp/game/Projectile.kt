package com.example.myapp.game

import kotlin.math.sqrt

data class Projectile(
    var x: Float,
    var y: Float,
    val targetX: Float,
    val targetY: Float,
    val speed: Float = 600f,
    val damage: Float,
    val size: Float = 8f,
    val color: Int = 0xFFFFD700.toInt(),
    var alive: Boolean = true
) {
    fun update(dt: Float) {
        val dx = targetX - x
        val dy = targetY - y
        val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if (dist < 1f) {
            x = targetX
            y = targetY
            alive = false
        } else if (dist < speed * dt) {
            x = targetX
            y = targetY
            alive = false
        } else {
            x += (dx / dist) * speed * dt
            y += (dy / dist) * speed * dt
        }
    }
}
