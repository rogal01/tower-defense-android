package com.example.myapp.game

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF

// Floating text for damage numbers, gold, combos
data class FloatingText(
    var x: Float, var y: Float,
    val text: String, val color: Int,
    var life: Float = 1f, val size: Float = 28f
) {
    val maxLife: Float = life
    fun update(dt: Float) { y -= 60f * dt; life -= dt }
    fun isDead(): Boolean = life <= 0
}

// Particle effect
data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var life: Float, val color: Int,
    val size: Float = 6f
) {
    val maxLife: Float = life
    fun update(dt: Float) { x += vx * dt; y += vy * dt; life -= dt }
    fun isDead(): Boolean = life <= 0
}

// Powers
enum class PowerType(val emoji: String, val displayName: String, val cost: Int, val cooldown: Float) {
    FIREBALL("\uD83D\uDD25", "Fireball", 40, 8f),
    FREEZE("\u2744\uFE0F", "Freeze", 30, 12f),
    HEAL("\uD83D\uDC9A", "Heal", 25, 15f),
    LIGHTNING("\u26A1", "Lightning", 50, 10f)
}

// Achievement
data class Achievement(
    val id: String, val title: String, val description: String,
    val emoji: String, var unlocked: Boolean = false
)

data class WavePreview(
    val isBoss: Boolean,
    val enemies: Map<EnemyType, Int>,
    val bossType: BossType? = null
)

/** A path with waypoints that enemies follow from spawn to base */
data class GamePath(val waypoints: List<PointF>) {
    val spawnPoint: PointF get() = waypoints.first()
}

class GameEngine(context: Context) {

    val lock = Any()
    private val appContext = context.applicationContext

    // Difficulty: 0=easy, 1=normal, 2=hard
    var difficulty: Int = 1
    // Difficulty multipliers
    private var enemyHpMult: Float = 1f
    private var enemyDmgMult: Float = 1f
    private var enemySpeedMult: Float = 1f
    private var goldMult: Float = 1f
    private var spawnRateMult: Float = 1f

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)

    val player = Player()
    val enemies = mutableListOf<Enemy>()
    val towers = mutableListOf<Tower>()
    val projectiles = mutableListOf<Projectile>()
    val floatingTexts = mutableListOf<FloatingText>()
    val particles = mutableListOf<Particle>()

    var gold: Int = 50
    var wave: Int = 0
    var baseHp: Float = 100f
    var maxBaseHp: Float = 100f
    var baseX: Float = 0f
    var baseY: Float = 0f
    var screenW: Float = 1080f
    var screenH: Float = 1920f

    var score: Int = 0
    var totalKills: Int = 0
    var totalGoldEarned: Int = 0
    var gameOver: Boolean = false
    var waveInProgress: Boolean = false
    var enemiesRemaining: Int = 0
    var waveDelay: Float = 3f
    var waveTimer: Float = 2f

    // Wave banner
    var waveBannerTimer: Float = 0f
    var showWaveBanner: Boolean = false

    // Combo system
    var comboCount: Int = 0
    var comboTimer: Float = 0f
    var comboMultiplier: Float = 1f
    var bestCombo: Int = 0

    // Powers cooldowns
    val powerCooldowns = mutableMapOf<PowerType, Float>()
    var freezeTimer: Float = 0f
    var goldBoostTimer: Float = 0f

    // Screen shake
    var shakeTimer: Float = 0f
    var shakeIntensity: Float = 0f

    // High score
    var highScore: Int = 0
    var highWave: Int = 0

    // Achievements
    val achievements = mutableListOf(
        Achievement("first_kill", "First Blood", "Kill your first enemy", "\uD83D\uDDE1\uFE0F"),
        Achievement("wave_5", "Survivor", "Reach wave 5", "\uD83D\uDEE1\uFE0F"),
        Achievement("wave_10", "Veteran", "Reach wave 10", "\u2694\uFE0F"),
        Achievement("wave_20", "Legend", "Reach wave 20", "\uD83D\uDC51"),
        Achievement("wave_30", "Immortal", "Reach wave 30", "\uD83C\uDFC6"),
        Achievement("wave_50", "Mythic", "Reach wave 50", "\uD83C\uDF1F"),
        Achievement("kills_50", "Slayer", "Kill 50 enemies", "\uD83D\uDC80"),
        Achievement("kills_200", "Destroyer", "Kill 200 enemies", "\uD83D\uDD25"),
        Achievement("kills_500", "Annihilator", "Kill 500 enemies", "\uD83D\uDCA5"),
        Achievement("combo_10", "Combo King", "Get a 10x combo", "\uD83D\uDD17"),
        Achievement("combo_20", "Combo God", "Get a 20x combo", "\u26D3\uFE0F"),
        Achievement("boss_kill", "Boss Slayer", "Kill your first boss", "\u2620\uFE0F"),
        Achievement("5_bosses", "Boss Hunter", "Kill 5 bosses in one run", "\uD83D\uDC09"),
        Achievement("5_towers", "Architect", "Place 5 towers", "\uD83C\uDFD7\uFE0F"),
        Achievement("10_towers", "Fortress", "Place 10 towers", "\uD83C\uDFF0"),
        Achievement("all_tower_types", "Arsenal", "Place all 5 tower types", "\uD83C\uDFAF"),
        Achievement("use_power", "Sorcerer", "Use a power for the first time", "\u2728"),
        Achievement("max_tower", "Master Builder", "Upgrade a tower to level 5", "\u2B06\uFE0F"),
        Achievement("rich", "Rich", "Have 500 gold at once", "\uD83D\uDCB0"),
        Achievement("rich_1000", "Millionaire", "Have 1000 gold at once", "\uD83E\uDD11"),
        Achievement("score_1000", "Score Chaser", "Reach 1000 score", "\uD83D\uDCCA"),
        Achievement("diamond_10", "Diamond Hoarder", "Earn 10 diamonds in a run", "\uD83D\uDC8E"),
        Achievement("repaired_3", "Mechanic", "Repair the base 3 times in a run", "\uD83D\uDD27"),
        Achievement("upgrade_all", "Well Rounded", "Buy all 4 player upgrades", "\uD83C\uDF96\uFE0F"),
        Achievement("endless_10", "Endurance", "Reach wave 10 in endless mode", "\u267E\uFE0F")
    )
    var newAchievement: Achievement? = null
    var achievementBannerTimer: Float = 0f

    // Upgrade levels
    var playerDamageLevel = 1
    var playerSpeedLevel = 1
    var playerHpLevel = 1
    var baseHpLevel = 1

    private val spawnPoints = mutableListOf<Pair<Float, Float>>()
    val paths = mutableListOf<GamePath>()
    val riverWaypoints = mutableListOf<PointF>()

    // Boss pool
    private val bossPool = mutableListOf<BossType>()
    var currentBoss: BossType? = null
        private set
    private var bossMinionsRemaining: Int = 0

    var isEndlessMode: Boolean = false
    val bossInterval: Int get() = if (campaignLevel?.id == 13) 3 else 5
    var nextWavePreview: WavePreview? = null
    var diamondsEarnedThisRun: Int = 0
    var bossesKilledThisRun: Int = 0
    var repairsThisRun: Int = 0
    var endlessHighWave: Int = 0
    var isNewHighScore: Boolean = false
    var isNewEndlessRecord: Boolean = false
    val skillTree = SkillTree(context)

    // Campaign
    var campaignLevel: CampaignLevel? = null
    var campaignVictory: Boolean = false

    fun applyDifficulty(level: Int) {
        difficulty = level
        isEndlessMode = level == 3
        when (level) {
            0 -> {
                enemyHpMult = 0.7f; enemyDmgMult = 0.6f; enemySpeedMult = 0.85f
                goldMult = 1.3f; spawnRateMult = 0.8f
                gold = 80
            }
            1 -> {
                enemyHpMult = 1f; enemyDmgMult = 1f; enemySpeedMult = 1f
                goldMult = 1f; spawnRateMult = 1f
                gold = 50
            }
            2 -> {
                enemyHpMult = 1.5f; enemyDmgMult = 1.4f; enemySpeedMult = 1.15f
                goldMult = 0.8f; spawnRateMult = 1.3f
                gold = 30
            }
            3 -> {
                enemyHpMult = 1f; enemyDmgMult = 1f; enemySpeedMult = 1f
                goldMult = 1f; spawnRateMult = 1f
                gold = 50
            }
        }
    }

    fun applyCampaign(level: CampaignLevel) {
        campaignLevel = level
        campaignVictory = false
        enemyHpMult = level.enemyHpMult
        enemyDmgMult = level.enemyDmgMult
        enemySpeedMult = level.enemySpeedMult
        goldMult = level.goldMult
        spawnRateMult = level.spawnRateMult
        gold = level.startingGold
    }

    fun isTowerAllowed(type: TowerType): Boolean {
        val cl = campaignLevel ?: return true
        return type in cl.allowedTowers
    }

    fun isPowerAllowed(type: PowerType): Boolean {
        val cl = campaignLevel ?: return true
        return type in cl.allowedPowers
    }

    fun init(width: Float, height: Float) {
        screenW = width
        screenH = height
        baseX = width / 2f
        baseY = height * 0.85f
        player.x = baseX
        player.y = baseY - 80f
        player.targetX = player.x
        player.targetY = player.y

        // Apply persistent skill tree bonuses
        gold += skillTree.bonusStartGold()
        maxBaseHp += skillTree.bonusBaseHp()
        baseHp = maxBaseHp
        player.attackDamage += skillTree.bonusPlayerDamage()
        player.speed += skillTree.bonusPlayerSpeed()
        player.maxHp += skillTree.bonusPlayerHp()
        player.hp = player.maxHp
        player.attackRange += skillTree.bonusAttackRange()

        generatePaths(width, height)

        highScore = prefs.getInt("highScore", 0)
        highWave = prefs.getInt("highWave", 0)
        endlessHighWave = prefs.getInt("endlessHighWave", 0)
        achievements.forEach { a ->
            a.unlocked = prefs.getBoolean("ach_${a.id}", false)
        }
        
        generateNextWavePreview()
    }

    /** Build 3 winding paths from spawn edges down to the base */
    private fun generatePaths(w: Float, h: Float) {
        paths.clear()
        spawnPoints.clear()
        val bx = baseX
        val by = baseY

        // Left path — winding S-curve from top-left
        paths.add(GamePath(listOf(
            PointF(w * 0.08f, -40f),
            PointF(w * 0.15f, h * 0.10f),
            PointF(w * 0.28f, h * 0.24f),
            PointF(w * 0.10f, h * 0.40f),
            PointF(w * 0.26f, h * 0.56f),
            PointF(w * 0.16f, h * 0.70f),
            PointF(w * 0.36f, h * 0.80f),
            PointF(bx, by)
        )))

        // Center path — gentle S-curve
        paths.add(GamePath(listOf(
            PointF(w * 0.50f, -40f),
            PointF(w * 0.46f, h * 0.09f),
            PointF(w * 0.58f, h * 0.24f),
            PointF(w * 0.40f, h * 0.40f),
            PointF(w * 0.56f, h * 0.56f),
            PointF(w * 0.44f, h * 0.70f),
            PointF(bx, by)
        )))

        // Right path — winding S-curve from top-right
        paths.add(GamePath(listOf(
            PointF(w * 0.92f, -40f),
            PointF(w * 0.85f, h * 0.10f),
            PointF(w * 0.72f, h * 0.24f),
            PointF(w * 0.90f, h * 0.40f),
            PointF(w * 0.74f, h * 0.56f),
            PointF(w * 0.84f, h * 0.70f),
            PointF(w * 0.64f, h * 0.80f),
            PointF(bx, by)
        )))

        paths.forEach { path ->
            val sp = path.spawnPoint
            spawnPoints.add(Pair(sp.x, sp.y))
        }

        // River flowing left-to-right across the map at ~33% height
        riverWaypoints.clear()
        val ry = h * 0.33f
        riverWaypoints.add(PointF(-20f, ry + 15f))
        riverWaypoints.add(PointF(w * 0.15f, ry - 10f))
        riverWaypoints.add(PointF(w * 0.30f, ry + 20f))
        riverWaypoints.add(PointF(w * 0.50f, ry - 5f))
        riverWaypoints.add(PointF(w * 0.70f, ry + 18f))
        riverWaypoints.add(PointF(w * 0.85f, ry - 8f))
        riverWaypoints.add(PointF(w + 20f, ry + 10f))
    }

    private fun generateNextWavePreview() {
        val nextWave = wave + 1
        if (nextWave % bossInterval == 0) {
            val type = if (bossPool.isEmpty()) BossType.values().random() else bossPool.first()
            nextWavePreview = WavePreview(true, emptyMap(), type)
        } else {
            val enemyCounts = mutableMapOf<EnemyType, Int>()
            val count = ((3 + nextWave * 2) * spawnRateMult).toInt().coerceAtMost(100)
            repeat(count) {
                val type = when {
                    nextWave >= 10 && Math.random() < 0.1 -> EnemyType.DRAGON
                    nextWave >= 8 && Math.random() < 0.15 -> EnemyType.DEMON
                    nextWave >= 6 && Math.random() < 0.2 -> EnemyType.ORC
                    nextWave >= 4 && Math.random() < 0.25 -> EnemyType.SKELETON
                    else -> EnemyType.GOBLIN
                }
                enemyCounts[type] = (enemyCounts[type] ?: 0) + 1
            }
            nextWavePreview = WavePreview(false, enemyCounts)
        }
    }

    fun update(dt: Float) { synchronized(lock) {
        if (gameOver || campaignVictory) return

        if (!waveInProgress && enemies.isEmpty()) {
            waveTimer -= dt
            if (waveTimer <= 0) startNextWave()
        }

        if (showWaveBanner) {
            waveBannerTimer -= dt
            if (waveBannerTimer <= 0f) showWaveBanner = false
        }

        if (achievementBannerTimer > 0) {
            achievementBannerTimer -= dt
            if (achievementBannerTimer <= 0f) newAchievement = null
        }

        if (waveInProgress && enemies.isEmpty() && enemiesRemaining <= 0) {
            waveInProgress = false
            waveTimer = waveDelay
            val bonus = wave * 5 + skillTree.bonusWaveGold()
            gold += bonus
            totalGoldEarned += bonus
            floatingTexts.add(FloatingText(baseX, baseY - 80f, "+${bonus}g wave bonus!", 0xFFFFD700.toInt(), 1.5f, 32f))

            // Campaign victory check
            val cl = campaignLevel
            if (cl != null && wave >= cl.targetWave) {
                campaignVictory = true
                prefs.edit().putBoolean("campaign_${cl.id}", true).apply()
                skillTree.addDiamonds(cl.diamondReward)
                diamondsEarnedThisRun += cl.diamondReward
            } else {
                generateNextWavePreview()
            }
        }

        if (waveInProgress && enemiesRemaining > 0) {
            if (enemies.size < 10 && Math.random() < dt * 2.5) {
                spawnEnemy()
                enemiesRemaining--
            }
        }

        PowerType.values().forEach { p ->
            val cd = powerCooldowns.getOrDefault(p, 0f)
            if (cd > 0) powerCooldowns[p] = cd - dt
        }
        if (freezeTimer > 0) freezeTimer -= dt
        if (goldBoostTimer > 0) goldBoostTimer -= dt

        if (shakeTimer > 0) shakeTimer -= dt

        if (comboTimer > 0) {
            comboTimer -= dt
            if (comboTimer <= 0) {
                if (comboCount >= 5) {
                    val bonus = (comboCount * 2)
                    gold += bonus
                    totalGoldEarned += bonus
                    floatingTexts.add(FloatingText(screenW / 2, screenH * 0.4f,
                        "${comboCount}x COMBO! +${bonus}g", 0xFFFF9800.toInt(), 2f, 40f))
                }
                comboCount = 0
                comboMultiplier = 1f
            }
        }

        player.update(dt)

        if (player.hp > 0 && player.canAttack()) {
            val nearest = enemies.minByOrNull { it.distanceTo(player.x, player.y) }
            if (nearest != null && nearest.distanceTo(player.x, player.y) < player.attackRange) {
                nearest.hp -= player.attackDamage
                nearest.hitFlash = 0.15f
                player.attack()
                projectiles.add(Projectile(player.x, player.y, nearest.x, nearest.y,
                    damage = 0f, color = 0xFF42A5F5.toInt()))
                floatingTexts.add(FloatingText(nearest.x, nearest.y - nearest.size,
                    "-${player.attackDamage.toInt()}", 0xFF42A5F5.toInt(), 0.8f, 22f))
            }
        }

        val speedMult = if (freezeTimer > 0) 0.2f else 1f
        enemies.forEach { enemy ->
            val chargeBoost = if (enemy.isCharging) 3f else 1f
            // Follow waypoints along assigned path
            val path = paths.getOrNull(enemy.pathIndex)
            val target = path?.waypoints?.getOrNull(enemy.waypointIndex)
            if (target != null) {
                val dx = target.x - enemy.x
                val dy = target.y - enemy.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist > enemy.size * 0.5f) {
                    enemy.x += (dx / dist) * enemy.speed * speedMult * chargeBoost * dt
                    enemy.y += (dy / dist) * enemy.speed * speedMult * chargeBoost * dt
                } else {
                    enemy.waypointIndex++
                }
            } else {
                // Past last waypoint or no path — head to base
                val dx = baseX - enemy.x
                val dy = baseY - enemy.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist > enemy.size) {
                    enemy.x += (dx / dist) * enemy.speed * speedMult * chargeBoost * dt
                    enemy.y += (dy / dist) * enemy.speed * speedMult * chargeBoost * dt
                }
            }
            if (enemy.hitFlash > 0) enemy.hitFlash -= dt
            if (enemy.isAtBase(baseX, baseY)) {
                baseHp -= enemy.damage * dt
                enemy.hitFlash = 0.1f
            }
        }

        // Boss special abilities
        val bossAbilityEnemies = mutableListOf<Enemy>()
        enemies.forEach { enemy ->
            if (enemy.type == EnemyType.BOSS && enemy.bossType != null && !enemy.isDead()) {
                enemy.bossAbilityTimer -= dt
                if (enemy.isCharging) {
                    enemy.chargeTimer -= dt
                    if (enemy.chargeTimer <= 0f) enemy.isCharging = false
                }
                if (enemy.bossAbilityTimer <= 0f) {
                    enemy.bossAbilityTimer = enemy.bossAbilityCooldown
                    bossAbilityEnemies.add(enemy)
                }
            }
        }
        for (boss in bossAbilityEnemies) {
            executeBossAbility(boss, dt)
        }

        val deadEnemies = enemies.filter { it.isDead() }
        deadEnemies.forEach { enemy ->
            val goldMultVal = if (goldBoostTimer > 0) 2 else 1
            val comboGold = (enemy.goldReward * comboMultiplier * goldMultVal * skillTree.goldBonusMultiplier()).toInt()
            gold += comboGold
            totalGoldEarned += comboGold
            score += comboGold
            totalKills++

            comboCount++
            comboTimer = 2f
            comboMultiplier = 1f + comboCount * 0.1f
            if (comboCount > bestCombo) bestCombo = comboCount

            floatingTexts.add(FloatingText(enemy.x, enemy.y, "+${comboGold}g", 0xFFFFD700.toInt()))

            repeat(8) {
                val angle = Math.random() * Math.PI * 2
                particles.add(Particle(
                    enemy.x, enemy.y,
                    (Math.cos(angle) * 150).toFloat(), (Math.sin(angle) * 150).toFloat(),
                    0.5f, enemy.type.color, 5f
                ))
            }

            if (enemy.type == EnemyType.BOSS) {
                shakeTimer = 0.4f; shakeIntensity = 15f
                val bossColor = enemy.bossType?.color ?: 0xFFFFD700.toInt()
                repeat(20) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(enemy.x, enemy.y,
                        (Math.cos(angle) * 250).toFloat(), (Math.sin(angle) * 250).toFloat(),
                        0.8f, bossColor, 8f))
                }
                checkAchievement("boss_kill")
                bossesKilledThisRun++
                if (bossesKilledThisRun >= 5) checkAchievement("5_bosses")
                currentBoss = null
                // Boss always drops diamonds
                val diamondDrop = (2 + wave / 5).coerceAtMost(10)
                skillTree.addDiamonds(diamondDrop)
                diamondsEarnedThisRun += diamondDrop
                floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 20f,
                    "+${diamondDrop} \uD83D\uDC8E", 0xFF00E5FF.toInt(), 1.5f, 30f))
            } else {
                // Regular enemies have a small diamond drop chance
                val dropChance = 0.03f + skillTree.diamondDropBonus()
                if (Math.random() < dropChance) {
                    skillTree.addDiamonds(1)
                    diamondsEarnedThisRun += 1
                    floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 20f,
                        "+1 \uD83D\uDC8E", 0xFF00E5FF.toInt(), 1.2f, 24f))
                }
            }

            checkAchievement("first_kill")
            if (totalKills >= 50) checkAchievement("kills_50")
            if (totalKills >= 200) checkAchievement("kills_200")
            if (totalKills >= 500) checkAchievement("kills_500")
            if (comboCount >= 10) checkAchievement("combo_10")
            if (comboCount >= 20) checkAchievement("combo_20")
            if (score >= 1000) checkAchievement("score_1000")
            if (diamondsEarnedThisRun >= 10) checkAchievement("diamond_10")
        }
        enemies.removeAll { it.isDead() }

        val towerDmgMult = (1f + (playerDamageLevel - 1) * 0.1f) * skillTree.towerDamageMultiplier()
        towers.forEach { tower ->
            tower.update(dt)
            if (tower.canFire()) {
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range }
                val target = when (tower.targetingMode) {
                    TargetingMode.CLOSE -> inRange.minByOrNull { it.distanceTo(tower.x, tower.y) }
                    TargetingMode.FIRST -> inRange.maxByOrNull { it.waypointIndex }
                    TargetingMode.LAST -> inRange.minByOrNull { it.waypointIndex }
                    TargetingMode.STRONG -> inRange.maxByOrNull { it.hp }
                }
                if (target != null) {
                    tower.fire()
                    val dmg = tower.damage * towerDmgMult
                    target.hp -= dmg
                    target.hitFlash = 0.15f
                    projectiles.add(Projectile(tower.x, tower.y, target.x, target.y,
                        damage = 0f,
                        color = when (tower.type) {
                            TowerType.ARROW -> 0xFFFFEB3B.toInt()
                            TowerType.MAGIC -> 0xFFAB47BC.toInt()
                            TowerType.CANNON -> 0xFFFF7043.toInt()
                            TowerType.POISON -> 0xFF66BB6A.toInt()
                            TowerType.TESLA -> 0xFF29B6F6.toInt()
                        }
                    ))
                    floatingTexts.add(FloatingText(target.x, target.y - target.size,
                        "-${dmg.toInt()}", 0xFFFFEB3B.toInt(), 0.6f, 18f))
                }
            }
        }

        projectiles.forEach { it.update(dt) }
        projectiles.removeAll { !it.alive }
        floatingTexts.forEach { it.update(dt) }
        floatingTexts.removeAll { it.isDead() }
        particles.forEach { it.update(dt) }
        particles.removeAll { it.isDead() }

        if (gold >= 500) checkAchievement("rich")
        if (gold >= 1000) checkAchievement("rich_1000")

        if (baseHp <= 0) {
            baseHp = 0f
            gameOver = true
            isNewHighScore = score > highScore
            isNewEndlessRecord = isEndlessMode && wave > endlessHighWave
            if (score > highScore) { highScore = score; prefs.edit().putInt("highScore", highScore).apply() }
            if (wave > highWave) { highWave = wave; prefs.edit().putInt("highWave", highWave).apply() }
            if (difficulty == 1 && wave >= 10) {
                prefs.edit().putBoolean("normal_beaten", true).apply()
            }
            if (isEndlessMode && wave > endlessHighWave) {
                endlessHighWave = wave
                prefs.edit().putInt("endlessHighWave", endlessHighWave).apply()
            }
            // Save lifetime stats
            prefs.edit()
                .putInt("lifetime_kills", prefs.getInt("lifetime_kills", 0) + totalKills)
                .putInt("lifetime_games", prefs.getInt("lifetime_games", 0) + 1)
                .putInt("lifetime_waves", prefs.getInt("lifetime_waves", 0) + wave)
                .putInt("lifetime_score", prefs.getInt("lifetime_score", 0) + score)
                .putInt("lifetime_gold", prefs.getInt("lifetime_gold", 0) + totalGoldEarned)
                .putInt("lifetime_towers", prefs.getInt("lifetime_towers", 0) + towers.size)
                .putInt("lifetime_bosses", prefs.getInt("lifetime_bosses", 0) + (if (wave / 5 > 0) wave / 5 else 0))
                .apply()
            val best = prefs.getInt("lifetime_best_combo", 0)
            if (bestCombo > best) prefs.edit().putInt("lifetime_best_combo", bestCombo).apply()
            // Track favorite tower
            val towerCounts = towers.groupBy { it.type.name }.mapValues { it.value.size }
            val fav = towerCounts.maxByOrNull { it.value }
            if (fav != null) {
                val key = "tower_count_${fav.key}"
                prefs.edit().putInt(key, prefs.getInt(key, 0) + fav.value).apply()
            }
        }
    } }

    private fun startNextWave() {
        wave++
        waveInProgress = true
        showWaveBanner = true
        waveBannerTimer = 1.5f
        if (wave >= 5) checkAchievement("wave_5")
        if (wave >= 10) checkAchievement("wave_10")
        if (wave >= 20) checkAchievement("wave_20")
        if (wave >= 30) checkAchievement("wave_30")
        if (wave >= 50) checkAchievement("wave_50")
        if (isEndlessMode && wave >= 10) checkAchievement("endless_10")
        if (wave % bossInterval == 0) {
            if (bossPool.isEmpty()) {
                bossPool.addAll(BossType.values().toList().shuffled())
            }
            currentBoss = bossPool.removeFirst()
            bossMinionsRemaining = currentBoss!!.minionCount
            enemiesRemaining = 1
        } else {
            currentBoss = null
            bossMinionsRemaining = 0
            enemiesRemaining = ((3 + wave * 2) * spawnRateMult).toInt().coerceAtMost(100)
        }
    }

    private fun spawnEnemy() {
        if (paths.isEmpty()) return
        val pathIdx = paths.indices.random()
        val spawn = paths[pathIdx].spawnPoint
        val waveScale = 1f + (wave - 1) * 0.15f

        val boss = currentBoss
        if (boss != null) {
            val hp = (boss.baseHp + wave * 40f) * waveScale * enemyHpMult
            enemies.add(Enemy(
                x = spawn.x, y = spawn.y,
                speed = boss.baseSpeed * enemySpeedMult,
                hp = hp, maxHp = hp,
                goldReward = ((boss.baseGold + wave * 10f) * waveScale * goldMult).toInt(),
                damage = boss.baseDmg * waveScale * enemyDmgMult,
                type = EnemyType.BOSS, bossType = boss, size = 55f,
                pathIndex = pathIdx
            ))
            spawnBossMinions(boss, waveScale)
            return
        }

        val type = when {
            wave >= 10 && Math.random() < 0.1 -> EnemyType.DRAGON
            wave >= 8 && Math.random() < 0.15 -> EnemyType.DEMON
            wave >= 6 && Math.random() < 0.2 -> EnemyType.ORC
            wave >= 4 && Math.random() < 0.25 -> EnemyType.SKELETON
            else -> EnemyType.GOBLIN
        }

        val (baseHpVal, baseSpeed, baseGold, baseDmg) = when (type) {
            EnemyType.GOBLIN -> arrayOf(20f, 80f, 3f, 5f)
            EnemyType.SKELETON -> arrayOf(35f, 100f, 5f, 8f)
            EnemyType.ORC -> arrayOf(60f, 60f, 8f, 12f)
            EnemyType.DEMON -> arrayOf(80f, 90f, 12f, 15f)
            EnemyType.DRAGON -> arrayOf(150f, 70f, 20f, 20f)
            else -> arrayOf(20f, 80f, 3f, 5f)
        }

        val hp = baseHpVal * waveScale * enemyHpMult
        enemies.add(
            Enemy(
                x = spawn.x,
                y = spawn.y,
                speed = (baseSpeed + (Math.random() * 20).toFloat()) * enemySpeedMult,
                hp = hp,
                maxHp = hp,
                goldReward = (baseGold * waveScale * goldMult).toInt(),
                damage = baseDmg * waveScale * enemyDmgMult,
                type = type,
                size = 30f,
                pathIndex = pathIdx
            )
        )
    }

    private fun spawnBossMinions(boss: BossType, waveScale: Float) {
        val count = (boss.minionCount * spawnRateMult).toInt().coerceAtLeast(2)
        repeat(count) {
            val pathIdx = if (paths.isNotEmpty()) paths.indices.random() else 0
            val sp = if (paths.isNotEmpty()) paths[pathIdx].spawnPoint else PointF(screenW / 2, -40f)
            val mHp = (15f + wave * 3f) * waveScale * enemyHpMult
            enemies.add(Enemy(
                x = sp.x + ((Math.random() - 0.5) * 80).toFloat(),
                y = sp.y + ((Math.random() - 0.5) * 40).toFloat(),
                speed = (70f + (Math.random() * 30).toFloat()) * enemySpeedMult,
                hp = mHp, maxHp = mHp,
                goldReward = ((2f + wave) * goldMult).toInt(),
                damage = (4f + wave) * waveScale * enemyDmgMult,
                type = boss.minionType,
                size = 22f,
                pathIndex = pathIdx
            ))
        }
    }

    private fun executeBossAbility(boss: Enemy, dt: Float) {
        val bt = boss.bossType ?: return
        when (bt.ability) {
            BossAbility.CHARGE -> {
                boss.isCharging = true
                boss.chargeTimer = 2f
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\u26A1 CHARGE!", bt.color, 1.2f, 28f))
                shakeTimer = 0.2f; shakeIntensity = 6f
            }
            BossAbility.SUMMON -> {
                val waveScale = 1f + (wave - 1) * 0.15f
                repeat(3) {
                    val pathIdx = if (paths.isNotEmpty()) paths.indices.random() else 0
                    val mHp = (15f + wave * 3f) * waveScale * enemyHpMult
                    enemies.add(Enemy(
                        x = boss.x + ((Math.random() - 0.5) * 60).toFloat(),
                        y = boss.y + ((Math.random() - 0.5) * 40).toFloat(),
                        speed = (70f + (Math.random() * 30).toFloat()) * enemySpeedMult,
                        hp = mHp, maxHp = mHp,
                        goldReward = ((2f + wave) * goldMult).toInt(),
                        damage = (4f + wave) * waveScale * enemyDmgMult,
                        type = bt.minionType, size = 22f, pathIndex = pathIdx,
                        waypointIndex = boss.waypointIndex.coerceAtMost(
                            (paths.getOrNull(pathIdx)?.waypoints?.size ?: 1) - 1
                        )
                    ))
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDC7E SUMMON!", bt.color, 1.2f, 28f))
                repeat(10) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(boss.x, boss.y,
                        (Math.cos(angle) * 100).toFloat(), (Math.sin(angle) * 100).toFloat(),
                        0.5f, bt.color, 5f))
                }
            }
            BossAbility.HEAL -> {
                val healAmt = boss.maxHp * 0.15f
                boss.hp = (boss.hp + healAmt).coerceAtMost(boss.maxHp)
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "+${healAmt.toInt()} HP", 0xFF66BB6A.toInt(), 1.2f, 28f))
                repeat(8) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(boss.x, boss.y,
                        (Math.cos(angle) * 60).toFloat(), (Math.sin(angle) * 60 - 40).toFloat(),
                        0.6f, 0xFF66BB6A.toInt(), 5f))
                }
            }
            BossAbility.AOE_DAMAGE -> {
                val range = 200f
                towers.forEach { tower ->
                    val dx = tower.x - boss.x; val dy = tower.y - boss.y
                    if (dx * dx + dy * dy < range * range) {
                        tower.fireTimer += 2f // Disable towers temporarily
                    }
                }
                // Also damage base if in range
                val dbx = baseX - boss.x; val dby = baseY - boss.y
                if (dbx * dbx + dby * dby < range * range) {
                    baseHp = (baseHp - 15f).coerceAtLeast(0f)
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDD25 AOE!", 0xFFFF5722.toInt(), 1.2f, 32f))
                shakeTimer = 0.3f; shakeIntensity = 10f
                repeat(15) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * range
                    particles.add(Particle(
                        boss.x + (Math.cos(angle) * dist).toFloat(),
                        boss.y + (Math.sin(angle) * dist).toFloat(),
                        0f, -40f, 0.6f, 0xFFFF5722.toInt(), 6f))
                }
            }
            BossAbility.SHIELD -> {
                boss.hp += boss.maxHp * 0.1f // Overshield
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDEE1\uFE0F SHIELD!", 0xFF29B6F6.toInt(), 1.5f, 28f))
                repeat(12) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(
                        boss.x + (Math.cos(angle) * 40).toFloat(),
                        boss.y + (Math.sin(angle) * 40).toFloat(),
                        0f, 0f, 0.8f, 0xFF29B6F6.toInt(), 4f))
                }
            }
            BossAbility.ROAR -> {
                enemies.filter { it != boss && it.distanceTo(boss.x, boss.y) < 250f }.forEach { minion ->
                    minion.hitFlash = 0.3f
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDCA8 ROAR!", bt.color, 1.2f, 32f))
                shakeTimer = 0.2f; shakeIntensity = 8f
                // Damage player if nearby
                if (player.distanceTo(boss.x, boss.y) < 200f) {
                    player.hp = (player.hp - 15f).coerceAtLeast(0f)
                    floatingTexts.add(FloatingText(player.x, player.y - 30f, "-15", 0xFFF44336.toInt(), 0.8f, 22f))
                }
            }
            BossAbility.TELEPORT -> {
                val path = paths.getOrNull(boss.pathIndex)
                if (path != null && boss.waypointIndex < path.waypoints.size - 2) {
                    boss.waypointIndex += 2
                    val wp = path.waypoints[boss.waypointIndex]
                    repeat(8) {
                        val angle = Math.random() * Math.PI * 2
                        particles.add(Particle(boss.x, boss.y,
                            (Math.cos(angle) * 80).toFloat(), (Math.sin(angle) * 80).toFloat(),
                            0.4f, 0xFF263238.toInt(), 5f))
                    }
                    boss.x = wp.x; boss.y = wp.y
                    repeat(8) {
                        val angle = Math.random() * Math.PI * 2
                        particles.add(Particle(boss.x, boss.y,
                            (Math.cos(angle) * 80).toFloat(), (Math.sin(angle) * 80).toFloat(),
                            0.4f, 0xFF263238.toInt(), 5f))
                    }
                    floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDCA8 TELEPORT!", bt.color, 1f, 26f))
                }
            }
            BossAbility.DRAIN -> {
                val stolen = (10 + wave).coerceAtMost(gold)
                if (stolen > 0) {
                    gold -= stolen
                    boss.hp = (boss.hp + stolen * 2f).coerceAtMost(boss.maxHp * 1.2f)
                    floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "-${stolen}g DRAIN!", 0xFFE040FB.toInt(), 1.2f, 28f))
                    floatingTexts.add(FloatingText(screenW / 2, screenH * 0.4f, "-${stolen} gold stolen!", 0xFFF44336.toInt(), 1.5f, 32f))
                }
            }
            BossAbility.QUAKE -> {
                shakeTimer = 1f; shakeIntensity = 20f
                // Slow all towers
                towers.forEach { it.fireTimer += 1.5f }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83C\uDF0B QUAKE!", bt.color, 1.5f, 32f))
                repeat(20) {
                    particles.add(Particle(
                        (Math.random() * screenW).toFloat(), screenH * 0.8f,
                        ((Math.random() - 0.5) * 40).toFloat(), -(Math.random() * 200 + 50).toFloat(),
                        0.6f, 0xFF795548.toInt(), 4f))
                }
            }
            BossAbility.SPLIT -> {
                if (!boss.hasSplit && boss.hp < boss.maxHp * 0.5f) {
                    boss.hasSplit = true
                    val waveScale = 1f + (wave - 1) * 0.15f
                    repeat(2) {
                        val cloneHp = boss.hp * 0.3f
                        enemies.add(Enemy(
                            x = boss.x + ((Math.random() - 0.5) * 50).toFloat(),
                            y = boss.y + ((Math.random() - 0.5) * 30).toFloat(),
                            speed = boss.speed * 1.3f,
                            hp = cloneHp, maxHp = cloneHp,
                            goldReward = (boss.goldReward / 4),
                            damage = boss.damage * 0.5f,
                            type = bt.minionType, size = 30f,
                            pathIndex = boss.pathIndex, waypointIndex = boss.waypointIndex
                        ))
                    }
                    floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83E\uDDA0 SPLIT!", bt.color, 1.2f, 28f))
                    repeat(12) {
                        val angle = Math.random() * Math.PI * 2
                        particles.add(Particle(boss.x, boss.y,
                            (Math.cos(angle) * 120).toFloat(), (Math.sin(angle) * 120).toFloat(),
                            0.5f, bt.color, 6f))
                    }
                }
            }
        }
    }

    fun placeTower(x: Float, y: Float, type: TowerType): Boolean { synchronized(lock) {
        if (gold < type.baseCost) return false
        if (towers.any { it.distanceTo(x, y) < 70f }) return false
        val distToBase = Math.sqrt(((x - baseX) * (x - baseX) + (y - baseY) * (y - baseY)).toDouble()).toFloat()
        if (distToBase < 60f) return false

        // Block placement on/near enemy paths
        for (gamePath in paths) {
            val wps = gamePath.waypoints
            for (i in 0 until wps.size - 1) {
                val ax = wps[i].x; val ay = wps[i].y
                val bx2 = wps[i + 1].x; val by2 = wps[i + 1].y
                val segLenSq = (bx2 - ax) * (bx2 - ax) + (by2 - ay) * (by2 - ay)
                val t = if (segLenSq < 0.01f) 0f else
                    (((x - ax) * (bx2 - ax) + (y - ay) * (by2 - ay)) / segLenSq).coerceIn(0f, 1f)
                val px = ax + t * (bx2 - ax)
                val py = ay + t * (by2 - ay)
                val dx = x - px; val dy = y - py
                if (dx * dx + dy * dy < 50f * 50f) return false
            }
        }

        // Block placement on/near river
        for (i in 0 until riverWaypoints.size - 1) {
            val ax = riverWaypoints[i].x; val ay = riverWaypoints[i].y
            val bx2 = riverWaypoints[i + 1].x; val by2 = riverWaypoints[i + 1].y
            val segLenSq = (bx2 - ax) * (bx2 - ax) + (by2 - ay) * (by2 - ay)
            val t = if (segLenSq < 0.01f) 0f else
                (((x - ax) * (bx2 - ax) + (y - ay) * (by2 - ay)) / segLenSq).coerceIn(0f, 1f)
            val px = ax + t * (bx2 - ax)
            val py = ay + t * (by2 - ay)
            val dx = x - px; val dy = y - py
            if (dx * dx + dy * dy < 40f * 40f) return false
        }

        gold -= type.baseCost
        towers.add(Tower(x, y, type = type, damage = type.baseDamage, range = type.baseRange, fireRate = type.baseFireRate))
        if (towers.size >= 5) checkAchievement("5_towers")
        if (towers.size >= 10) checkAchievement("10_towers")
        val towerTypes = towers.map { it.type }.toSet()
        if (towerTypes.size >= TowerType.entries.size) checkAchievement("all_tower_types")
        return true
    } }

    fun upgradeTower(tower: Tower): Boolean { synchronized(lock) {
        val cost = tower.upgradeCost()
        if (gold < cost) return false
        gold -= cost
        tower.upgrade()
        if (tower.level >= 5) checkAchievement("max_tower")
        return true
    } }

    fun upgradePlayerDamage(): Boolean { synchronized(lock) {
        val cost = playerDamageLevel * 25
        if (gold < cost) return false
        gold -= cost
        playerDamageLevel++
        player.attackDamage += 5f
        checkUpgradeAll()
        return true
    } }

    fun upgradePlayerSpeed(): Boolean { synchronized(lock) {
        val cost = playerSpeedLevel * 20
        if (gold < cost) return false
        gold -= cost
        playerSpeedLevel++
        player.speed += 30f
        checkUpgradeAll()
        return true
    } }

    fun upgradePlayerHp(): Boolean { synchronized(lock) {
        val cost = playerHpLevel * 30
        if (gold < cost) return false
        gold -= cost
        playerHpLevel++
        player.maxHp += 25f
        player.hp = player.maxHp
        checkUpgradeAll()
        return true
    } }

    fun upgradeBaseHp(): Boolean { synchronized(lock) {
        val cost = baseHpLevel * 40
        if (gold < cost) return false
        gold -= cost
        baseHpLevel++
        maxBaseHp += 30f
        baseHp = (baseHp + 30f).coerceAtMost(maxBaseHp)
        return true
    } }

    fun repairBase(): Boolean { synchronized(lock) {
        val cost = 20
        if (gold < cost) return false
        if (baseHp >= maxBaseHp) return false
        gold -= cost
        baseHp = (baseHp + 30f).coerceAtMost(maxBaseHp)
        repairsThisRun++
        if (repairsThisRun >= 3) checkAchievement("repaired_3")
        return true
    } }

    fun usePower(type: PowerType): Boolean { synchronized(lock) {
        val cd = powerCooldowns.getOrDefault(type, 0f)
        if (cd > 0) return false

        when (type) {
            PowerType.FIREBALL -> {
                if (gold < type.cost) return false
                gold -= type.cost
                enemies.forEach { enemy ->
                    enemy.hp -= 50f
                    enemy.hitFlash = 0.3f
                }
                shakeTimer = 0.3f; shakeIntensity = 10f
                repeat(30) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * screenW * 0.4
                    particles.add(Particle(
                        screenW / 2 + (Math.cos(angle) * dist).toFloat(),
                        screenH * 0.4f + (Math.sin(angle) * dist).toFloat(),
                        (Math.cos(angle) * 100).toFloat(), (Math.sin(angle) * 100).toFloat(),
                        0.6f, 0xFFFF5722.toInt(), 8f
                    ))
                }
                floatingTexts.add(FloatingText(screenW / 2, screenH * 0.35f,
                    "FIREBALL!", 0xFFFF5722.toInt(), 1.5f, 44f))
                powerCooldowns[type] = type.cooldown
            }
            PowerType.FREEZE -> {
                if (gold < type.cost) return false
                gold -= type.cost
                freezeTimer = 4f
                enemies.forEach { enemy ->
                    repeat(4) {
                        particles.add(Particle(enemy.x, enemy.y,
                            ((Math.random() - 0.5) * 80).toFloat(), ((Math.random() - 0.5) * 80).toFloat(),
                            0.5f, 0xFF81D4FA.toInt(), 4f))
                    }
                }
                floatingTexts.add(FloatingText(screenW / 2, screenH * 0.35f,
                    "FREEZE!", 0xFF29B6F6.toInt(), 1.5f, 44f))
                powerCooldowns[type] = type.cooldown
            }
            PowerType.HEAL -> {
                if (gold < type.cost) return false
                gold -= type.cost
                baseHp = (baseHp + 50f).coerceAtMost(maxBaseHp)
                repeat(15) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(baseX, baseY,
                        (Math.cos(angle) * 60).toFloat(), (Math.sin(angle) * 60 - 50).toFloat(),
                        0.8f, 0xFF66BB6A.toInt(), 6f))
                }
                floatingTexts.add(FloatingText(baseX, baseY - 60f,
                    "+50 HP!", 0xFF66BB6A.toInt(), 1.2f, 36f))
                powerCooldowns[type] = type.cooldown
            }
            PowerType.LIGHTNING -> {
                if (gold < type.cost) return false
                gold -= type.cost
                val targets = enemies.sortedBy { it.distanceTo(player.x, player.y) }.take(5)
                var prevX = player.x; var prevY = player.y
                targets.forEach { enemy ->
                    enemy.hp -= 80f
                    enemy.hitFlash = 0.3f
                    repeat(6) {
                        val t = it / 6f
                        particles.add(Particle(
                            prevX + (enemy.x - prevX) * t, prevY + (enemy.y - prevY) * t,
                            ((Math.random() - 0.5) * 40).toFloat(), ((Math.random() - 0.5) * 40).toFloat(),
                            0.4f, 0xFFFFEB3B.toInt(), 3f))
                    }
                    prevX = enemy.x; prevY = enemy.y
                }
                shakeTimer = 0.2f; shakeIntensity = 8f
                floatingTexts.add(FloatingText(screenW / 2, screenH * 0.35f,
                    "LIGHTNING!", 0xFFFFEB3B.toInt(), 1.5f, 44f))
                powerCooldowns[type] = type.cooldown
            }
        }
        checkAchievement("use_power")
        return true
    } }

    fun getPowerCooldown(type: PowerType): Float = powerCooldowns.getOrDefault(type, 0f)

    private fun checkAchievement(id: String) {
        val ach = achievements.find { it.id == id } ?: return
        if (ach.unlocked) return
        ach.unlocked = true
        prefs.edit().putBoolean("ach_${id}", true).apply()
        newAchievement = ach
        achievementBannerTimer = 3f
    }

    private fun checkUpgradeAll() {
        if (playerDamageLevel >= 2 && playerSpeedLevel >= 2 && playerHpLevel >= 2 && baseHpLevel >= 2) {
            checkAchievement("upgrade_all")
        }
    }

    fun restart() { synchronized(lock) {
        enemies.clear()
        towers.clear()
        projectiles.clear()
        floatingTexts.clear()
        particles.clear()
        applyDifficulty(difficulty)
        wave = 0
        baseHp = 100f
        maxBaseHp = 100f
        score = 0
        totalKills = 0
        totalGoldEarned = 0
        gameOver = false
        waveInProgress = false
        enemiesRemaining = 0
        waveTimer = 2f
        playerDamageLevel = 1
        playerSpeedLevel = 1
        playerHpLevel = 1
        baseHpLevel = 1
        comboCount = 0
        comboTimer = 0f
        comboMultiplier = 1f
        freezeTimer = 0f
        goldBoostTimer = 0f
        shakeTimer = 0f
        powerCooldowns.clear()
        bossPool.clear()
        currentBoss = null
        bossMinionsRemaining = 0
        newAchievement = null
        achievementBannerTimer = 0f
        showWaveBanner = false
        diamondsEarnedThisRun = 0
        bossesKilledThisRun = 0
        repairsThisRun = 0
        bestCombo = 0
        isNewHighScore = false
        isNewEndlessRecord = false
        campaignVictory = false
        val savedCampaign = campaignLevel
        player.apply {
            attackDamage = 10f
            speed = 300f
            maxHp = 100f
            hp = 100f
            attackRange = 150f
            attackCooldown = 0.5f
        }
        if (savedCampaign != null) applyCampaign(savedCampaign)
        init(screenW, screenH)
    } }
}
