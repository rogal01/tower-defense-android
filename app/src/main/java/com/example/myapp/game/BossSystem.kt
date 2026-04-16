package com.example.myapp.game

import com.example.myapp.SoundManager
import com.example.myapp.SfxType

internal class BossSystem(
    private val engine: GameEngine
) {

    fun executeBossAbility(boss: Enemy) {
        val bossType = boss.bossType ?: return
        when (bossType.ability) {
            BossAbility.CHARGE -> {
                boss.isCharging = true
                boss.chargeTimer = 2f
                SoundManager.play(SfxType.BOSS_CHARGE)
                engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\u26A1 CHARGE!", bossType.color, 1.2f, 28f))
                engine.shakeTimer = 0.2f
                engine.shakeIntensity = 6f
            }

            BossAbility.SUMMON -> {
                val waveScale = 1f + (engine.wave - 1) * 0.15f
                repeat(3) {
                    val pathIdx = boss.pathIndex
                    val minionHp = (15f + engine.wave * 3f) * waveScale * engine.enemyHpMultiplier
                    engine.enemies.add(
                        Enemy(
                            x = boss.x + ((Math.random() - 0.5) * 60).toFloat(),
                            y = boss.y + ((Math.random() - 0.5) * 40).toFloat(),
                            speed = (70f + (Math.random() * 30).toFloat()) * engine.enemySpeedMultiplier,
                            hp = minionHp,
                            maxHp = minionHp,
                            goldReward = ((2f + engine.wave) * engine.goldMultiplier).toInt().coerceAtLeast(1),
                            damage = (4f + engine.wave) * waveScale * engine.enemyDamageMultiplier,
                            type = bossType.minionType,
                            size = 22f,
                            pathIndex = pathIdx,
                            waypointIndex = boss.waypointIndex.coerceAtMost(
                                (engine.paths.getOrNull(pathIdx)?.waypoints?.size ?: 1) - 1
                            )
                        )
                    )
                }
                engine.totalEnemiesThisWave += 3
                engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDC7E SUMMON!", bossType.color, 1.2f, 28f))
                SoundManager.play(SfxType.BOSS_SUMMON)
                repeat(10) {
                    val angle = Math.random() * Math.PI * 2
                    engine.particles.add(
                        Particle(
                            boss.x,
                            boss.y,
                            (Math.cos(angle) * 100).toFloat(),
                            (Math.sin(angle) * 100).toFloat(),
                            0.5f,
                            bossType.color,
                            5f
                        )
                    )
                }
            }

            BossAbility.HEAL -> {
                val healAmount = boss.maxHp * 0.15f
                boss.hp = (boss.hp + healAmount).coerceAtMost(boss.maxHp)
                SoundManager.play(SfxType.BOSS_HEAL)
                engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "+${healAmount.toInt()} HP", 0xFF66BB6A.toInt(), 1.2f, 28f))
                repeat(8) {
                    val angle = Math.random() * Math.PI * 2
                    engine.particles.add(
                        Particle(
                            boss.x,
                            boss.y,
                            (Math.cos(angle) * 60).toFloat(),
                            (Math.sin(angle) * 60 - 40).toFloat(),
                            0.6f,
                            0xFF66BB6A.toInt(),
                            5f
                        )
                    )
                }
            }

            BossAbility.AOE_DAMAGE -> {
                val range = 200f
                engine.towers.forEach { tower ->
                    val dx = tower.x - boss.x
                    val dy = tower.y - boss.y
                    if (dx * dx + dy * dy < range * range) {
                        tower.fireTimer += 2f
                    }
                }
                val dbx = engine.baseX - boss.x
                val dby = engine.baseY - boss.y
                if (dbx * dbx + dby * dby < range * range) {
                    engine.baseHp = (engine.baseHp - 15f).coerceAtLeast(0f)
                }
                engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDD25 AOE!", 0xFFFF5722.toInt(), 1.2f, 32f))
                SoundManager.play(SfxType.BOSS_AOE)
                engine.shakeTimer = 0.3f
                engine.shakeIntensity = 10f
                repeat(15) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * range
                    engine.particles.add(
                        Particle(
                            boss.x + (Math.cos(angle) * dist).toFloat(),
                            boss.y + (Math.sin(angle) * dist).toFloat(),
                            0f,
                            -40f,
                            0.6f,
                            0xFFFF5722.toInt(),
                            6f
                        )
                    )
                }
            }

            BossAbility.SHIELD -> {
                boss.shieldTimer = 5f
                SoundManager.play(SfxType.BOSS_SHIELD)
                engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDEE1\uFE0F SHIELD!", 0xFF29B6F6.toInt(), 1.5f, 28f))
                repeat(12) {
                    val angle = Math.random() * Math.PI * 2
                    engine.particles.add(
                        Particle(
                            boss.x + (Math.cos(angle) * 40).toFloat(),
                            boss.y + (Math.sin(angle) * 40).toFloat(),
                            0f,
                            0f,
                            0.8f,
                            0xFF29B6F6.toInt(),
                            4f
                        )
                    )
                }
            }

            BossAbility.SCREECH -> {
                engine.player.slowTimer = maxOf(engine.player.slowTimer, 4f)
                var affectedTowers = 0
                engine.towers.forEach { tower ->
                    if (tower.distanceTo(boss.x, boss.y) < 260f) {
                        tower.debuffTimer = maxOf(tower.debuffTimer, 4f)
                        tower.fireTimer += 0.75f
                        affectedTowers++
                    }
                }
                engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDC09 SCREECH!", bossType.color, 1.2f, 30f))
                engine.floatingTexts.add(FloatingText(engine.player.x, engine.player.y - 36f, "SLOWED!", 0xFFFFB300.toInt(), 1f, 22f))
                if (affectedTowers > 0) {
                    engine.floatingTexts.add(FloatingText(boss.x, boss.y + boss.size, "TOWERS JAMMED!", 0xFFFF7043.toInt(), 1f, 20f))
                }
                SoundManager.play(SfxType.BOSS_SCREECH)
                engine.shakeTimer = 0.25f
                engine.shakeIntensity = 9f
                repeat(14) {
                    val angle = Math.random() * Math.PI * 2
                    val speed = 70 + Math.random() * 110
                    engine.particles.add(
                        Particle(
                            boss.x,
                            boss.y,
                            (Math.cos(angle) * speed).toFloat(),
                            (Math.sin(angle) * speed).toFloat(),
                            0.45f,
                            bossType.color,
                            5f
                        )
                    )
                }
            }

            BossAbility.TELEPORT -> {
                val path = engine.paths.getOrNull(boss.pathIndex)
                if (path != null && boss.waypointIndex < path.waypoints.size - 2) {
                    boss.waypointIndex += 2
                    val waypoint = path.waypoints[boss.waypointIndex]
                    repeat(8) {
                        val angle = Math.random() * Math.PI * 2
                        engine.particles.add(
                            Particle(
                                boss.x,
                                boss.y,
                                (Math.cos(angle) * 80).toFloat(),
                                (Math.sin(angle) * 80).toFloat(),
                                0.4f,
                                0xFF263238.toInt(),
                                5f
                            )
                        )
                    }
                    boss.x = waypoint.x
                    boss.y = waypoint.y
                    repeat(8) {
                        val angle = Math.random() * Math.PI * 2
                        engine.particles.add(
                            Particle(
                                boss.x,
                                boss.y,
                                (Math.cos(angle) * 80).toFloat(),
                                (Math.sin(angle) * 80).toFloat(),
                                0.4f,
                                0xFF263238.toInt(),
                                5f
                            )
                        )
                    }
                    engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDCA8 TELEPORT!", bossType.color, 1f, 26f))
                    SoundManager.play(SfxType.BOSS_TELEPORT)
                }
            }

            BossAbility.DRAIN -> {
                val stolen = (10 + engine.wave).coerceAtMost(engine.gold)
                if (stolen > 0) {
                    engine.gold -= stolen
                    boss.hp = (boss.hp + stolen * 2f).coerceAtMost(boss.maxHp * 1.2f)
                    engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "-${stolen}g DRAIN!", 0xFFE040FB.toInt(), 1.2f, 28f))
                    SoundManager.play(SfxType.BOSS_DRAIN)
                    engine.floatingTexts.add(FloatingText(engine.screenW / 2, engine.screenH * 0.4f, "-${stolen} gold stolen!", 0xFFF44336.toInt(), 1.5f, 32f))
                }
            }

            BossAbility.QUAKE -> {
                engine.shakeTimer = 1f
                engine.shakeIntensity = 20f
                engine.towers.forEach { it.fireTimer += 1.5f }
                engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83C\uDF0B QUAKE!", bossType.color, 1.5f, 32f))
                SoundManager.play(SfxType.BOSS_QUAKE)
                repeat(20) {
                    engine.particles.add(
                        Particle(
                            (Math.random() * engine.screenW).toFloat(),
                            engine.screenH * 0.8f,
                            ((Math.random() - 0.5) * 40).toFloat(),
                            -(Math.random() * 200 + 50).toFloat(),
                            0.6f,
                            0xFF795548.toInt(),
                            4f
                        )
                    )
                }
            }

            BossAbility.SPLIT -> {
                if (!boss.hasSplit && boss.hp < boss.maxHp * 0.5f) {
                    boss.hasSplit = true
                    val cloneHp = boss.hp * 0.3f
                    boss.hp *= 0.4f
                    repeat(2) {
                        engine.enemies.add(
                            Enemy(
                                x = boss.x + ((Math.random() - 0.5) * 50).toFloat(),
                                y = boss.y + ((Math.random() - 0.5) * 30).toFloat(),
                                speed = boss.speed * 1.3f,
                                hp = cloneHp,
                                maxHp = cloneHp,
                                goldReward = boss.goldReward / 4,
                                damage = boss.damage * 0.5f,
                                type = bossType.minionType,
                                size = 30f,
                                pathIndex = boss.pathIndex,
                                waypointIndex = boss.waypointIndex
                            )
                        )
                    }
                    engine.totalEnemiesThisWave += 2
                    engine.floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83E\uDDA0 SPLIT!", bossType.color, 1.2f, 28f))
                    SoundManager.play(SfxType.BOSS_SPLIT)
                    repeat(12) {
                        val angle = Math.random() * Math.PI * 2
                        engine.particles.add(
                            Particle(
                                boss.x,
                                boss.y,
                                (Math.cos(angle) * 120).toFloat(),
                                (Math.sin(angle) * 120).toFloat(),
                                0.5f,
                                bossType.color,
                                6f
                            )
                        )
                    }
                }
            }
        }
    }
}
