package com.example.myapp.game

import com.example.myapp.SoundManager
import com.example.myapp.SfxType

internal class CombatSystem(
    private val engine: GameEngine
) {

    fun playerDash(targetX: Float, targetY: Float): Boolean {
        if (engine.dashCooldown > 0 || engine.player.hp <= 0) return false

        val dx = targetX - engine.player.x
        val dy = targetY - engine.player.y
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if (dist < 30f) return false

        val actualDist = dist.coerceAtMost(engine.dashRange)
        val nx = dx / dist
        val ny = dy / dist

        engine.dashTrailX = engine.player.x
        engine.dashTrailY = engine.player.y

        val dashEndX = engine.player.x + nx * actualDist
        val dashEndY = engine.player.y + ny * actualDist
        engine.enemies.filter { it.hp > 0 }.forEach { enemy ->
            val ex = enemy.x - engine.player.x
            val ey = enemy.y - engine.player.y
            val lx = dashEndX - engine.player.x
            val ly = dashEndY - engine.player.y
            val lenSq = lx * lx + ly * ly
            val t = if (lenSq < 0.01f) 0f else ((ex * lx + ey * ly) / lenSq).coerceIn(0f, 1f)
            val px = engine.player.x + t * lx
            val py = engine.player.y + t * ly
            val dsx = enemy.x - px
            val dsy = enemy.y - py
            if (dsx * dsx + dsy * dsy < 60f * 60f) {
                val shieldMult = if (enemy.shieldTimer > 0) 0.3f else 1f
                val actualDashDamage = engine.mapAdjustedDamage(enemy, engine.dashDamage * shieldMult)
                enemy.hp -= actualDashDamage
                enemy.hitFlash = 0.3f
                engine.floatingTexts.add(
                    FloatingText(
                        enemy.x,
                        enemy.y - enemy.size,
                        "DASH! -${actualDashDamage.toInt()}",
                        0xFF00E5FF.toInt(),
                        0.8f,
                        24f
                    )
                )
            }
        }

        val groundTop = engine.screenH * 0.12f + engine.player.size
        var clampedEndX = dashEndX.coerceIn(engine.player.size, engine.screenW - engine.player.size)
        var clampedEndY = dashEndY.coerceIn(groundTop, engine.screenH - engine.player.size)
        if (engine.isPointOnRiver(clampedEndX, clampedEndY)) {
            clampedEndX = engine.dashTrailX
            clampedEndY = engine.dashTrailY
        }

        engine.player.x = clampedEndX
        engine.player.y = clampedEndY
        engine.player.targetX = clampedEndX
        engine.player.targetY = clampedEndY

        repeat(12) { i ->
            val progress = i / 12f
            engine.particles.add(
                Particle(
                    engine.dashTrailX + (dashEndX - engine.dashTrailX) * progress,
                    engine.dashTrailY + (dashEndY - engine.dashTrailY) * progress,
                    (Math.random().toFloat() - 0.5f) * 60f,
                    (Math.random().toFloat() - 0.5f) * 60f,
                    0.6f,
                    0xFF00E5FF.toInt(),
                    6f
                )
            )
        }

        engine.shakeTimer = 0.1f
        engine.shakeIntensity = 5f
        engine.isDashing = true
        engine.dashCooldown = engine.dashCooldownMax
        SoundManager.play(SfxType.PLAYER_ATTACK)
        return true
    }
}
