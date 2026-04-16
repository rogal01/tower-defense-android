package com.example.myapp.game

import android.graphics.PointF
import com.example.myapp.SoundManager
import com.example.myapp.SfxType

internal class WaveDirector(
    private val engine: GameEngine,
    private val bossPool: MutableList<BossType>,
    private val awardAchievement: (String) -> Unit
) {

    fun generateNextWavePreview() {
        val nextWave = engine.wave + 1
        val previewModifier = if (engine.isDailyChallenge && engine.dailyChallengeModifiers.isNotEmpty()) {
            engine.dailyChallengeModifiers[nextWave % engine.dailyChallengeModifiers.size]
        } else if (nextWave >= 3 && !engine.isDailyChallenge) {
            val modifiers = WaveModifier.entries.filter { it != WaveModifier.NONE }
            if (Math.random() < 0.4) modifiers.random() else WaveModifier.NONE
        } else {
            WaveModifier.NONE
        }

        if (nextWave % engine.bossInterval == 0) {
            if (bossPool.isEmpty()) {
                bossPool.addAll(BossType.entries.shuffled())
            }
            engine.nextWavePreview = WavePreview(true, emptyMap(), bossPool.first(), previewModifier)
        } else {
            val enemyCounts = mutableMapOf<EnemyType, Int>()
            var count = ((3 + nextWave * 2) * engine.spawnRateMultiplier).toInt().coerceAtMost(100)
            if (previewModifier == WaveModifier.SWARM) count *= 2
            repeat(count) {
                val type = when {
                    nextWave >= 12 && Math.random() < 0.08 -> EnemyType.ARMORED_GOLEM
                    nextWave >= 10 && Math.random() < 0.1 -> EnemyType.DRAGON
                    nextWave >= 8 && Math.random() < 0.15 -> EnemyType.DEMON
                    nextWave >= 7 && Math.random() < 0.12 -> EnemyType.FAST_SKELETON
                    nextWave >= 6 && Math.random() < 0.2 -> EnemyType.ORC
                    nextWave >= 4 && Math.random() < 0.25 -> EnemyType.SKELETON
                    else -> EnemyType.GOBLIN
                }
                enemyCounts[type] = (enemyCounts[type] ?: 0) + 1
            }
            engine.nextWavePreview = WavePreview(false, enemyCounts, modifier = previewModifier)
        }
    }

    fun startNextWave() {
        engine.wave++
        engine.waveInProgress = true
        engine.showWaveBanner = true
        engine.waveBannerTimer = 1.5f
        engine.eliteSpawnedThisWave = false

        if (engine.isEndlessMode && engine.wave > 10) {
            val tier = ((engine.wave - 10) / 10f).coerceAtMost(5f)
            engine.setDifficultyMultipliers(
                enemyHp = 1f + tier * 0.15f,
                enemyDamage = 1f + tier * 0.10f,
                enemySpeed = 1f + tier * 0.05f,
                gold = engine.goldMultiplier,
                spawnRate = 1f + tier * 0.08f
            )
        }

        engine.isNight = (engine.wave / 8) % 2 == 1
        SoundManager.play(SfxType.WAVE_START)
        if (engine.wave >= 5) awardAchievement("wave_5")
        if (engine.wave >= 10) awardAchievement("wave_10")
        if (engine.wave >= 20) awardAchievement("wave_20")
        if (engine.wave >= 30) awardAchievement("wave_30")
        if (engine.wave >= 50) awardAchievement("wave_50")
        if (engine.wave >= 100) awardAchievement("wave_100")
        if (engine.isEndlessMode && engine.wave >= 10) awardAchievement("endless_10")
        engine.baseHpBeforeWave = engine.baseHp
        if (engine.isBossRush) engine.bossRushWave++

        val preview = engine.nextWavePreview
        engine.currentWaveModifier = preview?.modifier ?: WaveModifier.NONE

        if (engine.isBossRush || engine.wave % engine.bossInterval == 0) {
            if (bossPool.isEmpty()) {
                bossPool.addAll(BossType.entries.shuffled())
            }
            engine.setCurrentBoss(bossPool.removeAt(0))
            engine.enemiesRemaining = 1
            val minionCount = if (engine.isBossRush) {
                (engine.currentBoss!!.minionCount * engine.spawnRateMultiplier * 0.6f).toInt().coerceAtLeast(1)
            } else {
                (engine.currentBoss!!.minionCount * engine.spawnRateMultiplier).toInt().coerceAtLeast(2)
            }
            engine.totalEnemiesThisWave = 1 + minionCount
            SoundManager.play(SfxType.BOSS_APPEAR)
        } else {
            engine.setCurrentBoss(null)
            var count = ((3 + engine.wave * 2) * engine.spawnRateMultiplier).toInt().coerceAtMost(100)
            if (engine.currentWaveModifier == WaveModifier.SWARM) count *= 2
            engine.enemiesRemaining = count
            engine.totalEnemiesThisWave = count
        }
        engine.enemiesSpawnedThisWave = 0
    }

    fun spawnEnemy() {
        if (engine.paths.isEmpty()) return

        val pathIndex = engine.paths.indices.random()
        val spawn = engine.paths[pathIndex].spawnPoint
        val waveScale = 1f + (engine.wave - 1) * 0.15f

        val boss = engine.currentBoss
        if (boss != null) {
            val bossRushScale = if (engine.isBossRush) 1f + engine.bossRushWave * 0.12f else 1f
            val hp = (boss.baseHp + engine.wave * 40f) * waveScale * engine.enemyHpMultiplier * bossRushScale
            engine.enemies.add(
                Enemy(
                    x = spawn.x,
                    y = spawn.y,
                    speed = boss.baseSpeed * engine.enemySpeedMultiplier,
                    hp = hp,
                    maxHp = hp,
                    goldReward = ((boss.baseGold + engine.wave * 10f) * waveScale * engine.goldMultiplier).toInt().coerceAtLeast(1),
                    damage = boss.baseDmg * waveScale * engine.enemyDamageMultiplier * bossRushScale,
                    type = EnemyType.BOSS,
                    bossType = boss,
                    size = 55f,
                    pathIndex = pathIndex,
                    bossAbilityTimer = 5f
                )
            )
            val minionCount = if (engine.isBossRush) {
                (boss.minionCount * engine.spawnRateMultiplier * 0.6f).toInt().coerceAtLeast(1)
            } else {
                (boss.minionCount * engine.spawnRateMultiplier).toInt().coerceAtLeast(2)
            }
            spawnBossMinions(boss, waveScale, minionCount)
            engine.enemiesSpawnedThisWave += 1 + minionCount
            return
        }

        val type = when {
            engine.wave >= 12 && Math.random() < 0.08 -> EnemyType.ARMORED_GOLEM
            engine.wave >= 10 && Math.random() < 0.1 -> EnemyType.DRAGON
            engine.wave >= 8 && Math.random() < 0.15 -> EnemyType.DEMON
            engine.wave >= 7 && Math.random() < 0.12 -> EnemyType.FAST_SKELETON
            engine.wave >= 6 && Math.random() < 0.2 -> EnemyType.ORC
            engine.wave >= 4 && Math.random() < 0.25 -> EnemyType.SKELETON
            else -> EnemyType.GOBLIN
        }

        val (baseHpVal, baseSpeed, baseGold, baseDamage) = when (type) {
            EnemyType.GOBLIN -> arrayOf(20f, 80f, 3f, 5f)
            EnemyType.SKELETON -> arrayOf(35f, 100f, 5f, 8f)
            EnemyType.ORC -> arrayOf(60f, 60f, 8f, 12f)
            EnemyType.DEMON -> arrayOf(80f, 90f, 12f, 15f)
            EnemyType.DRAGON -> arrayOf(150f, 70f, 20f, 20f)
            EnemyType.FAST_SKELETON -> arrayOf(25f, 150f, 6f, 6f)
            EnemyType.ARMORED_GOLEM -> arrayOf(120f, 40f, 15f, 18f)
            else -> arrayOf(20f, 80f, 3f, 5f)
        }

        var hpMod = 1f
        var speedMod = 1f
        var goldMod = 1f
        var regen = 0f
        when (engine.currentWaveModifier) {
            WaveModifier.FAST -> speedMod = 2f
            WaveModifier.ARMORED -> hpMod = 1.5f
            WaveModifier.REGEN -> regen = 3f + engine.wave * 0.5f
            WaveModifier.SWARM -> hpMod = 0.5f
            WaveModifier.RICH -> goldMod = 2f
            WaveModifier.BOSS_RALLY -> speedMod = 1.4f
            else -> {}
        }

        val hp = baseHpVal * waveScale * engine.enemyHpMultiplier * hpMod * engine.nightHpMultiplier
        val isElite = engine.wave % 5 == 0 && engine.wave % engine.bossInterval != 0 && !engine.eliteSpawnedThisWave && engine.wave >= 5
        val eliteHpMult = if (isElite) 3f else 1f
        val eliteGoldMult = if (isElite) 2f else 1f
        val eliteSize = if (isElite) 42f else 30f
        if (isElite) engine.eliteSpawnedThisWave = true

        val enemy = Enemy(
            x = spawn.x,
            y = spawn.y,
            speed = (baseSpeed + (Math.random() * 20).toFloat()) * engine.enemySpeedMultiplier * speedMod,
            hp = hp * eliteHpMult,
            maxHp = hp * eliteHpMult,
            goldReward = (baseGold * waveScale * engine.goldMultiplier * goldMod * eliteGoldMult).toInt().coerceAtLeast(1),
            damage = baseDamage * waveScale * engine.enemyDamageMultiplier * (if (isElite) 1.5f else 1f),
            type = type,
            size = eliteSize,
            pathIndex = pathIndex
        )
        enemy.regenRate = regen
        enemy.isElite = isElite
        engine.enemies.add(enemy)
        engine.enemiesSpawnedThisWave++
    }

    private fun spawnBossMinions(boss: BossType, waveScale: Float, count: Int) {
        var hpMod = 1f
        var speedMod = 1f
        var goldMod = 1f
        var regen = 0f
        when (engine.currentWaveModifier) {
            WaveModifier.FAST -> speedMod = 2f
            WaveModifier.ARMORED -> hpMod = 1.5f
            WaveModifier.REGEN -> regen = 3f + engine.wave * 0.5f
            WaveModifier.SWARM -> hpMod = 0.5f
            WaveModifier.RICH -> goldMod = 2f
            WaveModifier.BOSS_RALLY -> speedMod = 1.4f
            else -> {}
        }

        repeat(count) {
            val pathIndex = if (engine.paths.isNotEmpty()) engine.paths.indices.random() else 0
            val spawn = if (engine.paths.isNotEmpty()) engine.paths[pathIndex].spawnPoint else PointF(engine.screenW / 2, -40f)
            val minionHp = (15f + engine.wave * 3f) * waveScale * engine.enemyHpMultiplier * hpMod
            val enemy = Enemy(
                x = spawn.x + ((Math.random() - 0.5) * 80).toFloat(),
                y = spawn.y + ((Math.random() - 0.5) * 40).toFloat(),
                speed = (70f + (Math.random() * 30).toFloat()) * engine.enemySpeedMultiplier * speedMod,
                hp = minionHp,
                maxHp = minionHp,
                goldReward = ((2f + engine.wave) * engine.goldMultiplier * goldMod).toInt().coerceAtLeast(1),
                damage = (4f + engine.wave) * waveScale * engine.enemyDamageMultiplier,
                type = boss.minionType,
                size = 22f,
                pathIndex = pathIndex
            )
            enemy.regenRate = regen
            engine.enemies.add(enemy)
        }
    }
}
