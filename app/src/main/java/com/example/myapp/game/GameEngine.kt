package com.example.myapp.game

import android.content.Context
import android.content.SharedPreferences

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

class GameEngine(context: Context) {

    val lock = Any()
    private val appContext = context.applicationContext

    // Difficulty: 0=easy, 1=normal, 2=hard
    var difficulty: Int = 1
    // Difficulty multipliers (set via setDifficulty)
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
    var gameOver: Boolean = false
    var waveInProgress: Boolean = false
    var enemiesRemaining: Int = 0
    var waveDelay: Float = 3f
    var waveTimer: Float = 2f

    // Wave banner
    var waveBannerTimer: Float = 0f
    var showWaveBanner: Boolean = false
    var waveBannerText: String = ""

    // Combo system
    var comboCount: Int = 0
    var comboTimer: Float = 0f
    var comboMultiplier: Float = 1f
    var bestCombo: Int = 0

    // Powers cooldowns
    val powerCooldowns = mutableMapOf<PowerType, Float>()
    var freezeTimer: Float = 0f   // global freeze duration remaining
    var goldBoostTimer: Float = 0f // gold multiplier duration remaining

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
        Achievement("kills_50", "Slayer", "Kill 50 enemies", "\uD83D\uDC80"),
        Achievement("kills_200", "Destroyer", "Kill 200 enemies", "\uD83D\uDD25"),
        Achievement("combo_10", "Combo King", "Get a 10x combo", "\uD83D\uDD17"),
        Achievement("boss_kill", "Boss Slayer", "Kill your first boss", "\u2620\uFE0F"),
        Achievement("5_towers", "Architect", "Place 5 towers", "\uD83C\uDFD7\uFE0F"),
        Achievement("rich", "Rich", "Have 500 gold at once", "\uD83D\uDCB0")
    )
    var newAchievement: Achievement? = null
    var achievementBannerTimer: Float = 0f

    // Upgrade levels
    var playerDamageLevel = 1
    var playerSpeedLevel = 1
    var playerHpLevel = 1
    var baseHpLevel = 1

    private val spawnPoints = mutableListOf<Pair<Float, Float>>()

    // Boss pool — shuffled at start, no repeats until all 10 used
    private val bossPool = mutableListOf<BossType>()
    var currentBoss: BossType? = null
        private set
    private var bossMinionsRemaining: Int = 0

    fun setDifficulty(level: Int) {
        difficulty = level
        when (level) {
            0 -> { // Easy
                enemyHpMult = 0.7f; enemyDmgMult = 0.6f; enemySpeedMult = 0.85f
                goldMult = 1.3f; spawnRateMult = 0.8f
                gold = 80
            }
            1 -> { // Normal
                enemyHpMult = 1f; enemyDmgMult = 1f; enemySpeedMult = 1f
                goldMult = 1f; spawnRateMult = 1f
                gold = 50
            }
            2 -> { // Hard
                enemyHpMult = 1.5f; enemyDmgMult = 1.4f; enemySpeedMult = 1.15f
                goldMult = 0.8f; spawnRateMult = 1.3f
                gold = 30
            }
        }
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

        spawnPoints.clear()
        spawnPoints.add(Pair(width * 0.1f, -40f))
        spawnPoints.add(Pair(width * 0.3f, -40f))
        spawnPoints.add(Pair(width * 0.5f, -40f))
        spawnPoints.add(Pair(width * 0.7f, -40f))
        spawnPoints.add(Pair(width * 0.9f, -40f))
        spawnPoints.add(Pair(-40f, height * 0.3f))
        spawnPoints.add(Pair(width + 40f, height * 0.3f))

        // Load high scores
        highScore = prefs.getInt("highScore", 0)
        highWave = prefs.getInt("highWave", 0)
        // Load achievement state
        achievements.forEach { a ->
            a.unlocked = prefs.getBoolean("ach_${a.id}", false)
        }
    }

    fun update(dt: Float) { synchronized(lock) {
        if (gameOver) return

        // Wave management
        if (!waveInProgress && enemies.isEmpty()) {
            waveTimer -= dt
            if (waveTimer <= 0) startNextWave()
        }

        // Wave banner countdown
        if (showWaveBanner) {
            waveBannerTimer -= dt
            if (waveBannerTimer <= 0f) showWaveBanner = false
        }

        // Achievement banner countdown
        if (achievementBannerTimer > 0) {
            achievementBannerTimer -= dt
            if (achievementBannerTimer <= 0f) newAchievement = null
        }

        // Check wave complete
        if (waveInProgress && enemies.isEmpty() && enemiesRemaining <= 0) {
            waveInProgress = false
            waveTimer = waveDelay
            val bonus = wave * 5
            gold += bonus
            floatingTexts.add(FloatingText(baseX, baseY - 80f, "+${bonus}g wave bonus!", 0xFFFFD700.toInt(), 1.5f, 32f))
        }

        // Spawn enemies
        if (waveInProgress && enemiesRemaining > 0) {
            if (enemies.size < 10 && Math.random() < dt * 2.5) {
                spawnEnemy()
                enemiesRemaining--
            }
        }

        // Update power cooldowns
        PowerType.values().forEach { p ->
            val cd = powerCooldowns.getOrDefault(p, 0f)
            if (cd > 0) powerCooldowns[p] = cd - dt
        }
        if (freezeTimer > 0) freezeTimer -= dt
        if (goldBoostTimer > 0) goldBoostTimer -= dt

        // Screen shake
        if (shakeTimer > 0) shakeTimer -= dt

        // Combo timer
        if (comboTimer > 0) {
            comboTimer -= dt
            if (comboTimer <= 0) {
                if (comboCount >= 5) {
                    val bonus = (comboCount * 2)
                    gold += bonus
                    floatingTexts.add(FloatingText(screenW / 2, screenH * 0.4f,
                        "${comboCount}x COMBO! +${bonus}g", 0xFFFF9800.toInt(), 2f, 40f))
                }
                comboCount = 0
                comboMultiplier = 1f
            }
        }

        // Update player
        player.update(dt)

        // Player auto-attack nearest enemy in range
        if (player.canAttack()) {
            val nearest = enemies.minByOrNull { it.distanceTo(player.x, player.y) }
            if (nearest != null && nearest.distanceTo(player.x, player.y) < player.attackRange) {
                nearest.hp -= player.attackDamage
                nearest.hitFlash = 0.15f
                player.attack()
                projectiles.add(Projectile(player.x, player.y, nearest.x, nearest.y,
                    damage = 0f, color = 0xFF42A5F5.toInt()))
                // Floating damage number
                floatingTexts.add(FloatingText(nearest.x, nearest.y - nearest.size,
                    "-${player.attackDamage.toInt()}", 0xFF42A5F5.toInt(), 0.8f, 22f))
            }
        }

        // Update enemies (freeze slows them)
        val speedMult = if (freezeTimer > 0) 0.2f else 1f
        enemies.forEach { enemy ->
            val dx = baseX - enemy.x
            val dy = baseY - enemy.y
            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (dist > enemy.size && dist > 0.1f) {
                enemy.x += (dx / dist) * enemy.speed * speedMult * dt
                enemy.y += (dy / dist) * enemy.speed * speedMult * dt
            }
            if (enemy.hitFlash > 0) enemy.hitFlash -= dt
            if (enemy.isAtBase(baseX, baseY)) {
                baseHp -= enemy.damage * dt
                enemy.hitFlash = 0.1f
            }
        }

        // Remove dead enemies, award gold, spawn particles
        val deadEnemies = enemies.filter { it.isDead() }
        deadEnemies.forEach { enemy ->
            val goldMult = if (goldBoostTimer > 0) 2 else 1
            val comboGold = (enemy.goldReward * comboMultiplier * goldMult).toInt()
            gold += comboGold
            score += comboGold
            totalKills++

            // Combo
            comboCount++
            comboTimer = 2f
            comboMultiplier = 1f + comboCount * 0.1f
            if (comboCount > bestCombo) bestCombo = comboCount

            // Float gold text
            floatingTexts.add(FloatingText(enemy.x, enemy.y, "+${comboGold}g", 0xFFFFD700.toInt()))

            // Particle explosion
            repeat(8) {
                val angle = Math.random() * Math.PI * 2
                particles.add(Particle(
                    enemy.x, enemy.y,
                    (Math.cos(angle) * 150).toFloat(), (Math.sin(angle) * 150).toFloat(),
                    0.5f, enemy.type.color, 5f
                ))
            }

            // Boss death = big shake + extra particles
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
                currentBoss = null
            }

            // Check achievements
            checkAchievement("first_kill")
            if (totalKills >= 50) checkAchievement("kills_50")
            if (totalKills >= 200) checkAchievement("kills_200")
            if (comboCount >= 10) checkAchievement("combo_10")
        }
        enemies.removeAll { it.isDead() }

        // Update towers
        towers.forEach { tower ->
            tower.update(dt)
            if (tower.canFire()) {
                val target = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range }
                    .minByOrNull { it.distanceTo(tower.x, tower.y) }
                if (target != null) {
                    tower.fire()
                    target.hp -= tower.damage
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
                        "-${tower.damage.toInt()}", 0xFFFFEB3B.toInt(), 0.6f, 18f))
                }
            }
        }

        // Update projectiles, floating texts, particles
        projectiles.forEach { it.update(dt) }
        projectiles.removeAll { !it.alive }
        floatingTexts.forEach { it.update(dt) }
        floatingTexts.removeAll { it.isDead() }
        particles.forEach { it.update(dt) }
        particles.removeAll { it.isDead() }

        // Check gold achievement
        if (gold >= 500) checkAchievement("rich")

        // Game over
        if (baseHp <= 0) {
            baseHp = 0f
            gameOver = true
            // Save high score
            if (score > highScore) { highScore = score; prefs.edit().putInt("highScore", highScore).apply() }
            if (wave > highWave) { highWave = wave; prefs.edit().putInt("highWave", highWave).apply() }
            // Unlock hard difficulty once wave 10 reached on normal
            if (difficulty == 1 && wave >= 10) {
                prefs.edit().putBoolean("normal_beaten", true).apply()
            }
        }
    } }

    private fun startNextWave() {
        wave++
        waveInProgress = true
        showWaveBanner = true
        waveBannerTimer = 1.5f
        // Boss every 5th wave
        if (wave % 5 == 0) {
            // Pick next unique boss
            if (bossPool.isEmpty()) {
                bossPool.addAll(BossType.values().toList().shuffled())
            }
            currentBoss = bossPool.removeFirst()
            bossMinionsRemaining = currentBoss!!.minionCount
            enemiesRemaining = 1 // The boss itself; minions spawn alongside
        } else {
            currentBoss = null
            bossMinionsRemaining = 0
            // More variety: mix of types
            enemiesRemaining = ((3 + wave * 2) * spawnRateMult).toInt().coerceAtMost(100)
        }
    }

    private fun spawnEnemy() {
        val spawn = spawnPoints.random()
        val waveScale = 1f + (wave - 1) * 0.15f

        // Boss wave: spawn boss first, then themed minions
        val boss = currentBoss
        if (boss != null && enemiesRemaining > 0) {
            // Spawn the boss itself
            val hp = (boss.baseHp + wave * 40f) * waveScale * enemyHpMult
            enemies.add(Enemy(
                x = spawn.first, y = spawn.second,
                speed = boss.baseSpeed * enemySpeedMult,
                hp = hp, maxHp = hp,
                goldReward = ((boss.baseGold + wave * 10f) * waveScale * goldMult).toInt(),
                damage = boss.baseDmg * waveScale * enemyDmgMult,
                type = EnemyType.BOSS, bossType = boss, size = 55f
            ))
            enemiesRemaining--
            // Also spawn minions alongside boss
            spawnBossMinions(boss, waveScale)
            return
        }

        // Regular wave enemies
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
                x = spawn.first,
                y = spawn.second,
                speed = (baseSpeed + (Math.random() * 20).toFloat()) * enemySpeedMult,
                hp = hp,
                maxHp = hp,
                goldReward = (baseGold * waveScale * goldMult).toInt(),
                damage = baseDmg * waveScale * enemyDmgMult,
                type = type,
                size = 30f
            )
        )
    }

    private fun spawnBossMinions(boss: BossType, waveScale: Float) {
        val count = (boss.minionCount * spawnRateMult).toInt().coerceAtLeast(2)
        repeat(count) {
            val sp = spawnPoints.random()
            val mHp = (15f + wave * 3f) * waveScale * enemyHpMult
            enemies.add(Enemy(
                x = sp.first + ((Math.random() - 0.5) * 80).toFloat(),
                y = sp.second + ((Math.random() - 0.5) * 40).toFloat(),
                speed = (70f + (Math.random() * 30).toFloat()) * enemySpeedMult,
                hp = mHp, maxHp = mHp,
                goldReward = ((2f + wave) * goldMult).toInt(),
                damage = (4f + wave) * waveScale * enemyDmgMult,
                type = boss.minionType,
                size = 22f
            ))
        }
    }

    fun placeTower(x: Float, y: Float, type: TowerType): Boolean { synchronized(lock) {
        if (gold < type.baseCost) return false
        // Don't place too close to another tower or the base
        if (towers.any { it.distanceTo(x, y) < 70f }) return false
        val distToBase = Math.sqrt(((x - baseX) * (x - baseX) + (y - baseY) * (y - baseY)).toDouble()).toFloat()
        if (distToBase < 60f) return false

        gold -= type.baseCost
        towers.add(Tower(x, y, type = type))
        return true
    } }

    fun upgradeTower(tower: Tower): Boolean { synchronized(lock) {
        val cost = tower.upgradeCost()
        if (gold < cost) return false
        gold -= cost
        tower.upgrade()
        return true
    } }

    fun upgradePlayerDamage(): Boolean { synchronized(lock) {
        val cost = playerDamageLevel * 25
        if (gold < cost) return false
        gold -= cost
        playerDamageLevel++
        player.attackDamage += 5f
        return true
    } }

    fun upgradePlayerSpeed(): Boolean { synchronized(lock) {
        val cost = playerSpeedLevel * 20
        if (gold < cost) return false
        gold -= cost
        playerSpeedLevel++
        player.speed += 30f
        return true
    } }

    fun upgradePlayerHp(): Boolean { synchronized(lock) {
        val cost = playerHpLevel * 30
        if (gold < cost) return false
        gold -= cost
        playerHpLevel++
        player.maxHp += 25f
        player.hp = player.maxHp
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
        return true
    } }

    fun usePower(type: PowerType): Boolean { synchronized(lock) {
        val cd = powerCooldowns.getOrDefault(type, 0f)
        if (cd > 0) return false

        when (type) {
            PowerType.FIREBALL -> {
                if (gold < type.cost) return false
                gold -= type.cost
                // AoE damage to all enemies
                enemies.forEach { enemy ->
                    enemy.hp -= 50f
                    enemy.hitFlash = 0.3f
                }
                shakeTimer = 0.3f; shakeIntensity = 10f
                // Big fireball particles
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
                // Ice particles on all enemies
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
                // Green heal particles
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
                // Chain lightning: hit up to 5 enemies for 80 damage
                val targets = enemies.sortedBy { it.distanceTo(player.x, player.y) }.take(5)
                var prevX = player.x; var prevY = player.y
                targets.forEach { enemy ->
                    enemy.hp -= 80f
                    enemy.hitFlash = 0.3f
                    // Lightning line particles
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

    fun restart() { synchronized(lock) {
        enemies.clear()
        towers.clear()
        projectiles.clear()
        floatingTexts.clear()
        particles.clear()
        setDifficulty(difficulty)
        wave = 0
        baseHp = 100f
        maxBaseHp = 100f
        score = 0
        totalKills = 0
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
        player.apply {
            attackDamage = 10f
            speed = 300f
            maxHp = 100f
            hp = 100f
            attackRange = 150f
            attackCooldown = 0.5f
        }
        init(screenW, screenH)
    } }
}
