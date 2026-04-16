package com.example.myapp.game

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import com.example.myapp.SoundManager
import com.example.myapp.SfxType

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
    val bossType: BossType? = null,
    val modifier: WaveModifier = WaveModifier.NONE
)

/** Wave modifiers that apply random effects to a wave */
enum class WaveModifier(val displayName: String, val emoji: String, val description: String) {
    NONE("Normal", "", ""),
    FAST("Double Speed", "\uD83D\uDCA8", "Enemies move 2\u00D7 faster"),
    ARMORED("Armored", "\uD83D\uDEE1\uFE0F", "Enemies have 50% more HP"),
    REGEN("Regenerating", "\uD83D\uDC9A", "Enemies slowly regenerate HP"),
    SWARM("Swarm", "\uD83D\uDC1C", "Double enemies, half HP"),
    INVISIBLE("Ghostly", "\uD83D\uDC7B", "Tower range reduced 30%"),
    RICH("Treasure", "\uD83D\uDCB0", "Enemies drop 2\u00D7 gold"),
    BOSS_RALLY("Rally", "\uD83D\uDCEF", "All enemies move faster")
}

/** Different map layouts */
enum class MapType(val displayName: String, val emoji: String) {
    CLASSIC("Classic", "\uD83D\uDDFA\uFE0F"),
    VALLEY("Valley", "\uD83C\uDFD4\uFE0F"),
    CROSSROADS("Crossroads", "\u271A"),
    DESERT("Desert", "\uD83C\uDFDC\uFE0F"),
    SNOW("Snow", "\u2744\uFE0F")
}

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
    private val runPersistence = RunPersistence(prefs)
    private val metaProgression = MetaProgression(this, prefs)
    private val combatSystem = CombatSystem(this)

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
    var waveDelay: Float = 5f
    var waveTimer: Float = 4f

    // Wave progress tracking
    var totalEnemiesThisWave: Int = 0
    var enemiesSpawnedThisWave: Int = 0

    // Wave modifier
    var currentWaveModifier: WaveModifier = WaveModifier.NONE

    // Map type
    var mapType: MapType = MapType.CLASSIC
    private var crossroadsCenterX: Float = 0f
    private var crossroadsCenterY: Float = 0f
    private var desertStormTimer: Float = 12f
    var desertStormDuration: Float = 0f
        private set

    // Pause state
    var isPaused: Boolean = false

    // Game speed multiplier (1x, 2x, 3x)
    var gameSpeed: Int = 1

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

    // Screen shake
    var shakeTimer: Float = 0f
    var shakeIntensity: Float = 0f

    // High score
    var highScore: Int = 0
    var highWave: Int = 0

    // Achievements
    val achievements = ContentCatalog.createAchievements()
    // Track which powers were used this run for achievement
    val powersUsedThisRun = mutableSetOf<PowerType>()
    var baseHpBeforeWave: Float = 100f
    var newAchievement: Achievement? = null
    var achievementBannerTimer: Float = 0f

    // --- Critical hits ---
    val critChance: Float get() = 0.12f  // 12% base crit chance
    val critMultiplier: Float get() = 2.0f

    // --- Day/Night cycle ---
    var isNight: Boolean = false
    val nightHpMultiplier: Float get() = if (isNight) 1.20f else 1f
    val nightRangeMultiplier: Float get() = if (isNight) 0.90f else 1f

    // --- Player dash ---
    var dashCooldown: Float = 0f
    val dashCooldownMax: Float = 8f
    val dashDamage: Float get() = player.attackDamage * 2.5f
    val dashRange: Float = 200f
    var isDashing: Boolean = false
    var dashTrailX: Float = 0f
    var dashTrailY: Float = 0f

    // --- Gold interest ---
    val interestRate: Float get() = 0.05f  // 5% between waves

    // --- Elite enemies ---
    var eliteSpawnedThisWave: Boolean = false

    // Upgrade levels
    var playerDamageLevel = 1
    var playerSpeedLevel = 1
    var playerHpLevel = 1
    var baseHpLevel = 1

    private val spawnPoints = mutableListOf<Pair<Float, Float>>()
    val paths = mutableListOf<GamePath>()
    val riverWaypoints = mutableListOf<PointF>()

    // Boss pool
    internal val bossPool = mutableListOf<BossType>()
    var currentBoss: BossType? = null
        private set
    private val waveDirector = WaveDirector(this, bossPool, ::checkAchievement)
    private val bossSystem = BossSystem(this)

    var isEndlessMode: Boolean = false
    var isBossRush: Boolean = false
    var bossRushWave: Int = 0
    var bossRushHighWave: Int = 0
    var isNewBossRushRecord: Boolean = false
    val bossInterval: Int get() = if (isBossRush) 1 else if (campaignLevel?.id == 13) 3 else 5
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

    // Daily challenge
    var isDailyChallenge: Boolean = false
    var dailyChallengeModifiers: List<WaveModifier> = emptyList()
    var dailyChallengeSeed: Long = 0L

    // Randomizer mode
    var isRandomizerMode: Boolean = false
    var randomizerSeed: Long = 0L
    /** Per-run randomized tower cost overrides (null = default) */
    var randomizerTowerCosts: Map<TowerType, Int> = emptyMap()
    /** Per-run randomized power cooldown overrides */
    var randomizerPowerCooldowns: Map<PowerType, Float> = emptyMap()
    /** Per-run randomized power damage multiplier */
    var randomizerPowerDamageMult: Float = 1f
    /** Per-run randomized starting gold */
    var randomizerStartGold: Int = 50
    internal val enemyHpMultiplier: Float get() = enemyHpMult
    internal val enemyDamageMultiplier: Float get() = enemyDmgMult
    internal val enemySpeedMultiplier: Float get() = enemySpeedMult
    internal val goldMultiplier: Float get() = goldMult
    internal val spawnRateMultiplier: Float get() = spawnRateMult

    fun applyDifficulty(level: Int) {
        difficulty = level
        isEndlessMode = level == 3
        isBossRush = level == 4
        isRandomizerMode = level == 5
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
            3 -> { // Endless — starts normal but scales faster
                enemyHpMult = 1f; enemyDmgMult = 1f; enemySpeedMult = 1f
                goldMult = 1f; spawnRateMult = 1f
                gold = 50
                // Endless scaling is applied per-wave in startNextWave
            }
            4 -> {
                enemyHpMult = 1f; enemyDmgMult = 1f; enemySpeedMult = 1f
                goldMult = 1.5f; spawnRateMult = 0.8f
                gold = 100
                bossRushHighWave = prefs.getInt("bossRushHighWave", 0)
            }
            5 -> { // Randomizer — everything gets scrambled
                enemyHpMult = 1f; enemyDmgMult = 1f; enemySpeedMult = 1f
                goldMult = 1f; spawnRateMult = 1f
                gold = 50
            }
        }
    }

    fun applyEndlessSubDifficulty(subDiff: Int) {
        // Apply difficulty scaling on top of endless mode
        when (subDiff) {
            0 -> { // Easy
                enemyHpMult = 0.7f; enemyDmgMult = 0.6f; enemySpeedMult = 0.85f
                goldMult = 1.3f; spawnRateMult = 0.8f; gold = 80
            }
            2 -> { // Hard
                enemyHpMult = 1.5f; enemyDmgMult = 1.4f; enemySpeedMult = 1.15f
                goldMult = 0.8f; spawnRateMult = 1.3f; gold = 30
            }
            // Normal (1) keeps defaults
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

    fun applyMapType(type: MapType) {
        mapType = type
        if (screenW > 0f && screenH > 0f) {
            generatePaths(screenW, screenH)
        }
    }

    internal fun setCurrentBoss(boss: BossType?) {
        currentBoss = boss
    }

    internal fun setDifficultyMultipliers(
        enemyHp: Float = enemyHpMult,
        enemyDamage: Float = enemyDmgMult,
        enemySpeed: Float = enemySpeedMult,
        gold: Float = goldMult,
        spawnRate: Float = spawnRateMult
    ) {
        enemyHpMult = enemyHp
        enemyDmgMult = enemyDamage
        enemySpeedMult = enemySpeed
        goldMult = gold
        spawnRateMult = spawnRate
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

    val crossroadsKillZoneRadius: Float get() = minOf(screenW, screenH) * 0.12f
    val crossroadsZoneX: Float get() = crossroadsCenterX
    val crossroadsZoneY: Float get() = crossroadsCenterY
    private val valleyHighGroundThreshold: Float get() = screenH * 0.55f

    fun getMapMechanicLabel(): String = when (mapType) {
        MapType.CLASSIC -> "\uD83D\uDDFA\uFE0F Classic terrain: balanced battlefield"
        MapType.VALLEY -> "\uD83C\uDFD4\uFE0F High Ground: upper towers deal +15% damage"
        MapType.CROSSROADS -> "\u271A Kill Zone: center-crossing enemies take +20% damage"
        MapType.DESERT -> "\uD83C\uDFDC\uFE0F Sandstorm: storms slow enemies but cut range"
        MapType.SNOW -> "\u2744\uFE0F Deep Cold: enemies slow down and freezes last longer"
    }

    fun getMapMechanicStatus(): String = when (mapType) {
        MapType.CLASSIC -> getMapMechanicLabel()
        MapType.VALLEY -> "\uD83C\uDFD4\uFE0F High Ground active above the river line"
        MapType.CROSSROADS -> "\u271A Kill Zone centered on the middle crossing"
        MapType.DESERT -> if (desertStormDuration > 0f) {
            "\uD83C\uDF2A\uFE0F Sandstorm active: -20% enemy speed, -10% range"
        } else {
            "\uD83C\uDFDC\uFE0F Sandstorm in ${kotlin.math.ceil(desertStormTimer.toDouble()).toInt()}s"
        }
        MapType.SNOW -> "\u2744\uFE0F Deep Cold: enemies -12%, freeze effects extended"
    }

    fun getCombatDebuffStatus(): String? {
        val heroSlowed = player.slowTimer > 0f
        val towersDebuffed = towers.any { it.debuffTimer > 0f }
        return when {
            heroSlowed && towersDebuffed -> "\uD83D\uDC09 Dragon screech: hero slowed, towers disrupted"
            heroSlowed -> "\uD83D\uDC09 Dragon screech: hero slowed"
            towersDebuffed -> "\uD83D\uDC09 Dragon screech: towers disrupted"
            else -> null
        }
    }

    fun getDisplayedTowerRange(tower: Tower): Float =
        tower.range * mapTowerRangeMultiplier() * (if (currentWaveModifier == WaveModifier.INVISIBLE) 0.7f else 1f) * nightRangeMultiplier

    private fun mapTowerRangeMultiplier(): Float =
        if (mapType == MapType.DESERT && desertStormDuration > 0f) 0.9f else 1f

    private fun mapPlayerRangeMultiplier(): Float =
        if (mapType == MapType.DESERT && desertStormDuration > 0f) 0.9f else 1f

    private fun mapTowerDamageMultiplier(tower: Tower): Float =
        if (mapType == MapType.VALLEY && tower.y < valleyHighGroundThreshold) 1.15f else 1f

    private fun mapEnemySpeedMultiplier(): Float {
        var mult = 1f
        if (mapType == MapType.SNOW) mult *= 0.88f
        if (mapType == MapType.DESERT && desertStormDuration > 0f) mult *= 0.8f
        return mult
    }

    private fun mapDamageTakenMultiplier(enemy: Enemy): Float =
        if (mapType == MapType.CROSSROADS &&
            enemy.distanceTo(crossroadsCenterX, crossroadsCenterY) < crossroadsKillZoneRadius
        ) 1.2f else 1f

    internal fun mapAdjustedDamage(enemy: Enemy, damage: Float): Float =
        damage * mapDamageTakenMultiplier(enemy)

    private fun mapIceSlowBonus(): Float =
        if (mapType == MapType.SNOW) 0.08f else 0f

    private fun mapFreezeDurationMultiplier(): Float =
        if (mapType == MapType.SNOW) 1.25f else 1f

    /** Build paths based on the selected map type — randomized each run */
    private fun generatePaths(w: Float, h: Float) {
        paths.clear()
        spawnPoints.clear()
        val bx = baseX
        val by = baseY
        val rng = java.util.Random()
        crossroadsCenterX = w * 0.5f
        crossroadsCenterY = h * 0.45f

        when (mapType) {
            MapType.CLASSIC -> generateRandomizedClassicPaths(w, h, bx, by, rng)
            MapType.VALLEY -> generateRandomizedValleyPaths(w, h, bx, by, rng)
            MapType.CROSSROADS -> generateRandomizedCrossroadsPaths(w, h, bx, by, rng)
            MapType.DESERT -> generateRandomizedDesertPaths(w, h, bx, by, rng)
            MapType.SNOW -> generateRandomizedSnowPaths(w, h, bx, by, rng)
        }

        paths.forEach { path ->
            val sp = path.spawnPoint
            spawnPoints.add(Pair(sp.x, sp.y))
        }

        // River flowing left-to-right with randomized wobble
        riverWaypoints.clear()
        val ry = h * (0.30f + rng.nextFloat() * 0.06f)
        riverWaypoints.add(PointF(-20f, ry + rng.nextFloat() * 20f - 5f))
        riverWaypoints.add(PointF(w * 0.15f, ry + rng.nextFloat() * 30f - 15f))
        riverWaypoints.add(PointF(w * 0.30f, ry + rng.nextFloat() * 30f - 10f))
        riverWaypoints.add(PointF(w * 0.50f, ry + rng.nextFloat() * 20f - 10f))
        riverWaypoints.add(PointF(w * 0.70f, ry + rng.nextFloat() * 30f - 10f))
        riverWaypoints.add(PointF(w * 0.85f, ry + rng.nextFloat() * 20f - 10f))
        riverWaypoints.add(PointF(w + 20f, ry + rng.nextFloat() * 20f - 5f))
    }

    /** Check if a point is on/near the river */
    fun isPointOnRiver(x: Float, y: Float): Boolean {
        for (i in 0 until riverWaypoints.size - 1) {
            val ax = riverWaypoints[i].x; val ay = riverWaypoints[i].y
            val bx2 = riverWaypoints[i + 1].x; val by2 = riverWaypoints[i + 1].y
            val segLenSq = (bx2 - ax) * (bx2 - ax) + (by2 - ay) * (by2 - ay)
            val t = if (segLenSq < 0.01f) 0f else
                (((x - ax) * (bx2 - ax) + (y - ay) * (by2 - ay)) / segLenSq).coerceIn(0f, 1f)
            val px = ax + t * (bx2 - ax)
            val py = ay + t * (by2 - ay)
            val dx = x - px; val dy = y - py
            if (dx * dx + dy * dy < 30f * 30f) return true
        }
        return false
    }

    /** Helper: jitter a base value by +/- range */
    private fun jitter(rng: java.util.Random, base: Float, range: Float): Float =
        base + (rng.nextFloat() * 2f - 1f) * range

    private fun generateRandomizedClassicPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.04f // jitter factor relative to screen
        // Left path
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.08f, w * j), -40f),
            PointF(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.10f, h * j)),
            PointF(jitter(rng, w * 0.28f, w * j), jitter(rng, h * 0.24f, h * j)),
            PointF(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.40f, h * j)),
            PointF(jitter(rng, w * 0.26f, w * j), jitter(rng, h * 0.56f, h * j)),
            PointF(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.70f, h * j)),
            PointF(jitter(rng, w * 0.36f, w * j), jitter(rng, h * 0.80f, h * j)),
            PointF(bx, by)
        )))
        // Center path
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.50f, w * j), -40f),
            PointF(jitter(rng, w * 0.46f, w * j), jitter(rng, h * 0.09f, h * j)),
            PointF(jitter(rng, w * 0.58f, w * j), jitter(rng, h * 0.24f, h * j)),
            PointF(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.40f, h * j)),
            PointF(jitter(rng, w * 0.56f, w * j), jitter(rng, h * 0.56f, h * j)),
            PointF(jitter(rng, w * 0.44f, w * j), jitter(rng, h * 0.70f, h * j)),
            PointF(bx, by)
        )))
        // Right path
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.92f, w * j), -40f),
            PointF(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.10f, h * j)),
            PointF(jitter(rng, w * 0.72f, w * j), jitter(rng, h * 0.24f, h * j)),
            PointF(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.40f, h * j)),
            PointF(jitter(rng, w * 0.74f, w * j), jitter(rng, h * 0.56f, h * j)),
            PointF(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.70f, h * j)),
            PointF(jitter(rng, w * 0.64f, w * j), jitter(rng, h * 0.80f, h * j)),
            PointF(bx, by)
        )))
    }

    private fun generateRandomizedValleyPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.05f
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.50f, w * j), -40f),
            PointF(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.08f, h * j)),
            PointF(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.20f, h * j)),
            PointF(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.34f, h * j)),
            PointF(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.48f, h * j)),
            PointF(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.62f, h * j)),
            PointF(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.74f, h * j)),
            PointF(bx, by)
        )))
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.35f, w * j), -40f),
            PointF(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.12f, h * j)),
            PointF(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.28f, h * j)),
            PointF(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.42f, h * j)),
            PointF(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.56f, h * j)),
            PointF(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.70f, h * j)),
            PointF(bx, by)
        )))
    }

    private fun generateRandomizedCrossroadsPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.03f
        val cx = jitter(rng, w * 0.5f, w * 0.04f)
        val cy = jitter(rng, h * 0.45f, h * 0.03f)
        crossroadsCenterX = cx
        crossroadsCenterY = cy
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.50f, w * j), -40f),
            PointF(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.10f, h * j)),
            PointF(cx, cy),
            PointF(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.65f, h * j)),
            PointF(bx, by)
        )))
        paths.add(GamePath(listOf(
            PointF(-40f, jitter(rng, h * 0.40f, h * j)),
            PointF(jitter(rng, w * 0.12f, w * j), jitter(rng, h * 0.38f, h * j)),
            PointF(cx, cy),
            PointF(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.65f, h * j)),
            PointF(bx, by)
        )))
        paths.add(GamePath(listOf(
            PointF(w + 40f, jitter(rng, h * 0.40f, h * j)),
            PointF(jitter(rng, w * 0.88f, w * j), jitter(rng, h * 0.42f, h * j)),
            PointF(cx, cy),
            PointF(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.65f, h * j)),
            PointF(bx, by)
        )))
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.90f, w * j), -40f),
            PointF(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.15f, h * j)),
            PointF(cx, cy),
            PointF(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.68f, h * j)),
            PointF(bx, by)
        )))
    }

    private fun generateRandomizedDesertPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.04f
        // Two wide sweeping paths (desert canyon feel)
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.20f, w * j), -40f),
            PointF(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.12f, h * j)),
            PointF(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.25f, h * j)),
            PointF(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.42f, h * j)),
            PointF(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.58f, h * j)),
            PointF(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.72f, h * j)),
            PointF(bx, by)
        )))
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.80f, w * j), -40f),
            PointF(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.10f, h * j)),
            PointF(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.28f, h * j)),
            PointF(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.45f, h * j)),
            PointF(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.62f, h * j)),
            PointF(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.75f, h * j)),
            PointF(bx, by)
        )))
    }

    private fun generateRandomizedSnowPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.035f
        // Three narrow winding paths (icy mountain passes)
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.15f, w * j), -40f),
            PointF(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.12f, h * j)),
            PointF(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.30f, h * j)),
            PointF(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.48f, h * j)),
            PointF(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.66f, h * j)),
            PointF(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.78f, h * j)),
            PointF(bx, by)
        )))
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.50f, w * j), -40f),
            PointF(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.10f, h * j)),
            PointF(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.28f, h * j)),
            PointF(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.46f, h * j)),
            PointF(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.64f, h * j)),
            PointF(bx, by)
        )))
        paths.add(GamePath(listOf(
            PointF(jitter(rng, w * 0.85f, w * j), -40f),
            PointF(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.14f, h * j)),
            PointF(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.32f, h * j)),
            PointF(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.50f, h * j)),
            PointF(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.68f, h * j)),
            PointF(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.80f, h * j)),
            PointF(bx, by)
        )))
    }

    fun setupDailyChallenge() {
        isDailyChallenge = true
        // Seed based on current day
        val cal = java.util.Calendar.getInstance()
        dailyChallengeSeed = (cal.get(java.util.Calendar.YEAR) * 10000L +
                (cal.get(java.util.Calendar.MONTH) + 1) * 100L + cal.get(java.util.Calendar.DAY_OF_MONTH))
        val rng = java.util.Random(dailyChallengeSeed)
        val allMods = WaveModifier.entries.filter { it != WaveModifier.NONE }
        dailyChallengeModifiers = List(3) { allMods[rng.nextInt(allMods.size)] }
        // Daily challenge uses fixed difficulty
        applyDifficulty(1)
    }

    /** Set up randomizer mode — scramble tower costs, power cooldowns, multipliers, etc. */
    fun setupRandomizer() {
        isRandomizerMode = true
        randomizerSeed = System.currentTimeMillis()
        val rng = java.util.Random(randomizerSeed)

        // Randomize starting gold: 20–150
        randomizerStartGold = 20 + rng.nextInt(131)
        gold = randomizerStartGold

        // Randomize tower costs: 50%–200% of base
        randomizerTowerCosts = TowerType.entries.associateWith {
            (it.baseCost * (0.5f + rng.nextFloat() * 1.5f)).toInt().coerceAtLeast(5)
        }

        // Randomize power cooldowns: 40%–180% of base
        randomizerPowerCooldowns = PowerType.entries.associateWith {
            it.cooldown * (0.4f + rng.nextFloat() * 1.4f)
        }

        // Randomize power damage multiplier: 0.5–2.5x
        randomizerPowerDamageMult = 0.5f + rng.nextFloat() * 2f

        // Scramble enemy multipliers
        enemyHpMult = 0.5f + rng.nextFloat() * 1.5f
        enemyDmgMult = 0.5f + rng.nextFloat() * 1.5f
        enemySpeedMult = 0.7f + rng.nextFloat() * 0.8f
        goldMult = 0.5f + rng.nextFloat() * 1.5f
        spawnRateMult = 0.6f + rng.nextFloat() * 1.0f

        // Randomize player stats: 70%–150% of base
        player.attackDamage = 10f * (0.7f + rng.nextFloat() * 0.8f)
        player.speed = 300f * (0.7f + rng.nextFloat() * 0.8f)
        player.attackRange = 150f * (0.7f + rng.nextFloat() * 0.8f)

        // Pick a random map
        mapType = MapType.entries[rng.nextInt(MapType.entries.size)]
    }

    /** Get tower cost (respects randomizer overrides) */
    fun getTowerCost(type: TowerType): Int {
        return if (isRandomizerMode) randomizerTowerCosts[type] ?: type.baseCost else type.baseCost
    }

    /** Get power cooldown (respects randomizer overrides) */
    fun getRandomizedPowerCooldown(type: PowerType): Float {
        return if (isRandomizerMode) randomizerPowerCooldowns[type] ?: type.cooldown else type.cooldown
    }

    private fun encodeRandomizerTowerCosts(): String =
        randomizerTowerCosts.entries.joinToString(";") { "${it.key.name}:${it.value}" }

    private fun decodeRandomizerTowerCosts(encoded: String): Map<TowerType, Int> {
        if (encoded.isBlank()) return emptyMap()
        return encoded.split(";").mapNotNull { part ->
            val tokens = part.split(":", limit = 2)
            if (tokens.size != 2) return@mapNotNull null
            val type = runCatching { TowerType.valueOf(tokens[0]) }.getOrNull() ?: return@mapNotNull null
            val value = tokens[1].toIntOrNull() ?: return@mapNotNull null
            type to value
        }.toMap()
    }

    private fun encodeRandomizerPowerCooldowns(): String =
        randomizerPowerCooldowns.entries.joinToString(";") { "${it.key.name}:${it.value}" }

    private fun decodeRandomizerPowerCooldowns(encoded: String): Map<PowerType, Float> {
        if (encoded.isBlank()) return emptyMap()
        return encoded.split(";").mapNotNull { part ->
            val tokens = part.split(":", limit = 2)
            if (tokens.size != 2) return@mapNotNull null
            val type = runCatching { PowerType.valueOf(tokens[0]) }.getOrNull() ?: return@mapNotNull null
            val value = tokens[1].toFloatOrNull() ?: return@mapNotNull null
            type to value
        }.toMap()
    }

    private fun encodeSavedTowers(): String =
        towers.joinToString("|") { tower ->
            listOf(
                tower.type.name,
                tower.x,
                tower.y,
                tower.level,
                tower.range,
                tower.damage,
                tower.fireRate,
                tower.fireTimer,
                tower.targetingMode.name,
                tower.abilityTimer,
                tower.debuffTimer
            ).joinToString(",")
        }

    private fun decodeSavedTowers(encoded: String): List<Tower> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split("|").mapNotNull { part ->
            val tokens = part.split(",")
            if (tokens.size != 11) return@mapNotNull null
            val type = runCatching { TowerType.valueOf(tokens[0]) }.getOrNull() ?: return@mapNotNull null
            val x = tokens[1].toFloatOrNull() ?: return@mapNotNull null
            val y = tokens[2].toFloatOrNull() ?: return@mapNotNull null
            val level = tokens[3].toIntOrNull() ?: return@mapNotNull null
            val range = tokens[4].toFloatOrNull() ?: return@mapNotNull null
            val damage = tokens[5].toFloatOrNull() ?: return@mapNotNull null
            val fireRate = tokens[6].toFloatOrNull() ?: return@mapNotNull null
            val fireTimer = tokens[7].toFloatOrNull() ?: return@mapNotNull null
            val targetingMode = runCatching { TargetingMode.valueOf(tokens[8]) }.getOrDefault(TargetingMode.CLOSE)
            val abilityTimer = tokens[9].toFloatOrNull() ?: return@mapNotNull null
            val debuffTimer = tokens[10].toFloatOrNull() ?: return@mapNotNull null
            Tower(
                x = x,
                y = y,
                level = level,
                range = range,
                damage = damage,
                fireRate = fireRate,
                fireTimer = fireTimer,
                type = type,
                targetingMode = targetingMode,
                abilityTimer = abilityTimer,
                debuffTimer = debuffTimer
            )
        }
    }

    private fun rebuildSpawnPoints() {
        spawnPoints.clear()
        paths.forEach { path ->
            val spawn = path.spawnPoint
            spawnPoints.add(spawn.x to spawn.y)
        }
    }

    private fun buildRunSnapshot(): RunSnapshot = RunSnapshot(
        wave = wave,
        gold = gold,
        score = score,
        totalKills = totalKills,
        totalGoldEarned = totalGoldEarned,
        baseHp = baseHp,
        maxBaseHp = maxBaseHp,
        waveInProgress = waveInProgress,
        enemiesRemaining = enemiesRemaining,
        waveDelay = waveDelay,
        waveTimer = waveTimer,
        totalEnemiesThisWave = totalEnemiesThisWave,
        enemiesSpawnedThisWave = enemiesSpawnedThisWave,
        currentWaveModifier = currentWaveModifier.name,
        isPaused = isPaused,
        gameSpeed = gameSpeed,
        showWaveBanner = showWaveBanner,
        waveBannerTimer = waveBannerTimer,
        comboCount = comboCount,
        comboTimer = comboTimer,
        comboMultiplier = comboMultiplier,
        bestCombo = bestCombo,
        freezeTimer = freezeTimer,
        shakeTimer = shakeTimer,
        shakeIntensity = shakeIntensity,
        playerDamageLevel = playerDamageLevel,
        playerSpeedLevel = playerSpeedLevel,
        playerHpLevel = playerHpLevel,
        baseHpLevel = baseHpLevel,
        diamondsEarnedThisRun = diamondsEarnedThisRun,
        bossesKilledThisRun = bossesKilledThisRun,
        repairsThisRun = repairsThisRun,
        baseHpBeforeWave = baseHpBeforeWave,
        dashCooldown = dashCooldown,
        isDashing = isDashing,
        dashTrailX = dashTrailX,
        dashTrailY = dashTrailY,
        eliteSpawnedThisWave = eliteSpawnedThisWave,
        desertStormTimer = desertStormTimer,
        desertStormDuration = desertStormDuration,
        powersUsedThisRun = powersUsedThisRun.map { it.name },
        powerCooldowns = powerCooldowns.mapKeys { it.key.name },
        player = PlayerSnapshot(
            x = player.x,
            y = player.y,
            targetX = player.targetX,
            targetY = player.targetY,
            speed = player.speed,
            attackRange = player.attackRange,
            attackDamage = player.attackDamage,
            attackCooldown = player.attackCooldown,
            attackTimer = player.attackTimer,
            size = player.size,
            hp = player.hp,
            maxHp = player.maxHp,
            slowTimer = player.slowTimer
        ),
        towers = towers.map { tower ->
            TowerSnapshot(
                x = tower.x,
                y = tower.y,
                level = tower.level,
                range = tower.range,
                damage = tower.damage,
                fireRate = tower.fireRate,
                fireTimer = tower.fireTimer,
                size = tower.size,
                type = tower.type.name,
                targetingMode = tower.targetingMode.name,
                abilityTimer = tower.abilityTimer,
                debuffTimer = tower.debuffTimer
            )
        },
        enemies = enemies.map { enemy ->
            EnemySnapshot(
                x = enemy.x,
                y = enemy.y,
                speed = enemy.speed,
                hp = enemy.hp,
                maxHp = enemy.maxHp,
                goldReward = enemy.goldReward,
                damage = enemy.damage,
                size = enemy.size,
                type = enemy.type.name,
                bossType = enemy.bossType?.name,
                hitFlash = enemy.hitFlash,
                pathIndex = enemy.pathIndex,
                waypointIndex = enemy.waypointIndex,
                bossAbilityTimer = enemy.bossAbilityTimer,
                bossAbilityCooldown = enemy.bossAbilityCooldown,
                isCharging = enemy.isCharging,
                chargeTimer = enemy.chargeTimer,
                hasSplit = enemy.hasSplit,
                iceSlowFactor = enemy.iceSlowFactor,
                deepFreezeTimer = enemy.deepFreezeTimer,
                regenRate = enemy.regenRate,
                reachedBase = enemy.reachedBase,
                isElite = enemy.isElite,
                shieldTimer = enemy.shieldTimer
            )
        },
        projectiles = projectiles.map { projectile ->
            ProjectileSnapshot(
                x = projectile.x,
                y = projectile.y,
                targetX = projectile.targetX,
                targetY = projectile.targetY,
                speed = projectile.speed,
                damage = projectile.damage,
                size = projectile.size,
                color = projectile.color,
                alive = projectile.alive
            )
        },
        paths = paths.map { path ->
            PathSnapshot(path.waypoints.map { waypoint -> PointSnapshot(waypoint.x, waypoint.y) })
        },
        riverWaypoints = riverWaypoints.map { waypoint -> PointSnapshot(waypoint.x, waypoint.y) },
        mode = ModeSnapshot(
            difficulty = difficulty,
            mapType = mapType.name,
            isEndlessMode = isEndlessMode,
            isBossRush = isBossRush,
            bossRushWave = bossRushWave,
            currentBoss = currentBoss?.name,
            bossPool = bossPool.map { it.name },
            campaignLevelId = campaignLevel?.id,
            campaignVictory = campaignVictory,
            isDailyChallenge = isDailyChallenge,
            dailyChallengeModifiers = dailyChallengeModifiers.map { it.name },
            dailyChallengeSeed = dailyChallengeSeed,
            isRandomizerMode = isRandomizerMode,
            randomizerSeed = randomizerSeed,
            randomizerTowerCosts = randomizerTowerCosts.mapKeys { it.key.name },
            randomizerPowerCooldowns = randomizerPowerCooldowns.mapKeys { it.key.name },
            randomizerPowerDamageMult = randomizerPowerDamageMult,
            randomizerStartGold = randomizerStartGold,
            enemyHpMult = enemyHpMult,
            enemyDmgMult = enemyDmgMult,
            enemySpeedMult = enemySpeedMult,
            goldMult = goldMult,
            spawnRateMult = spawnRateMult,
            isNight = isNight
        )
    )

    private fun restoreRunSnapshot(snapshot: RunSnapshot) {
        val mode = snapshot.mode
        difficulty = mode.difficulty
        isEndlessMode = mode.isEndlessMode
        isBossRush = mode.isBossRush
        bossRushWave = mode.bossRushWave
        campaignVictory = mode.campaignVictory
        isDailyChallenge = mode.isDailyChallenge
        dailyChallengeModifiers = mode.dailyChallengeModifiers.mapNotNull { modifierName ->
            runCatching { WaveModifier.valueOf(modifierName) }.getOrNull()
        }
        dailyChallengeSeed = mode.dailyChallengeSeed
        isRandomizerMode = mode.isRandomizerMode
        randomizerSeed = mode.randomizerSeed
        randomizerTowerCosts = mode.randomizerTowerCosts.mapNotNull { entry ->
            runCatching { TowerType.valueOf(entry.key) }.getOrNull()?.let { towerType ->
                towerType to entry.value
            }
        }.toMap()
        randomizerPowerCooldowns = mode.randomizerPowerCooldowns.mapNotNull { entry ->
            runCatching { PowerType.valueOf(entry.key) }.getOrNull()?.let { powerType ->
                powerType to entry.value
            }
        }.toMap()
        randomizerPowerDamageMult = mode.randomizerPowerDamageMult
        randomizerStartGold = mode.randomizerStartGold
        enemyHpMult = mode.enemyHpMult
        enemyDmgMult = mode.enemyDmgMult
        enemySpeedMult = mode.enemySpeedMult
        goldMult = mode.goldMult
        spawnRateMult = mode.spawnRateMult
        isNight = mode.isNight

        val restoredMapType = runCatching { MapType.valueOf(mode.mapType) }.getOrDefault(MapType.CLASSIC)
        applyMapType(restoredMapType)
        if (snapshot.paths.isNotEmpty()) {
            paths.clear()
            paths.addAll(snapshot.paths.map { pathSnapshot ->
                GamePath(pathSnapshot.waypoints.map { waypoint -> PointF(waypoint.x, waypoint.y) })
            })
            riverWaypoints.clear()
            riverWaypoints.addAll(snapshot.riverWaypoints.map { waypoint -> PointF(waypoint.x, waypoint.y) })
            rebuildSpawnPoints()
        }

        bossPool.clear()
        bossPool.addAll(mode.bossPool.mapNotNull { bossName ->
            runCatching { BossType.valueOf(bossName) }.getOrNull()
        })
        currentBoss = mode.currentBoss?.let { bossName ->
            runCatching { BossType.valueOf(bossName) }.getOrNull()
        }
        campaignLevel = mode.campaignLevelId?.let { id ->
            CampaignData.levels.find { it.id == id }
        }

        wave = snapshot.wave
        gold = snapshot.gold
        score = snapshot.score
        totalKills = snapshot.totalKills
        totalGoldEarned = snapshot.totalGoldEarned
        baseHp = snapshot.baseHp
        maxBaseHp = snapshot.maxBaseHp
        waveInProgress = snapshot.waveInProgress
        enemiesRemaining = snapshot.enemiesRemaining
        waveDelay = snapshot.waveDelay
        waveTimer = snapshot.waveTimer
        totalEnemiesThisWave = snapshot.totalEnemiesThisWave
        enemiesSpawnedThisWave = snapshot.enemiesSpawnedThisWave
        currentWaveModifier = runCatching { WaveModifier.valueOf(snapshot.currentWaveModifier) }
            .getOrDefault(WaveModifier.NONE)
        isPaused = snapshot.isPaused
        gameSpeed = snapshot.gameSpeed
        showWaveBanner = snapshot.showWaveBanner
        waveBannerTimer = snapshot.waveBannerTimer
        comboCount = snapshot.comboCount
        comboTimer = snapshot.comboTimer
        comboMultiplier = snapshot.comboMultiplier
        bestCombo = snapshot.bestCombo
        freezeTimer = snapshot.freezeTimer
        shakeTimer = snapshot.shakeTimer
        shakeIntensity = snapshot.shakeIntensity
        playerDamageLevel = snapshot.playerDamageLevel
        playerSpeedLevel = snapshot.playerSpeedLevel
        playerHpLevel = snapshot.playerHpLevel
        baseHpLevel = snapshot.baseHpLevel
        diamondsEarnedThisRun = snapshot.diamondsEarnedThisRun
        bossesKilledThisRun = snapshot.bossesKilledThisRun
        repairsThisRun = snapshot.repairsThisRun
        baseHpBeforeWave = snapshot.baseHpBeforeWave
        dashCooldown = snapshot.dashCooldown
        isDashing = snapshot.isDashing
        dashTrailX = snapshot.dashTrailX
        dashTrailY = snapshot.dashTrailY
        eliteSpawnedThisWave = snapshot.eliteSpawnedThisWave
        desertStormTimer = snapshot.desertStormTimer
        desertStormDuration = snapshot.desertStormDuration

        powersUsedThisRun.clear()
        powersUsedThisRun.addAll(snapshot.powersUsedThisRun.mapNotNull { powerName ->
            runCatching { PowerType.valueOf(powerName) }.getOrNull()
        })
        powerCooldowns.clear()
        powerCooldowns.putAll(snapshot.powerCooldowns.mapNotNull { entry ->
            runCatching { PowerType.valueOf(entry.key) }.getOrNull()?.let { powerType ->
                powerType to entry.value
            }
        }.toMap())

        player.apply {
            x = snapshot.player.x
            y = snapshot.player.y
            targetX = snapshot.player.targetX
            targetY = snapshot.player.targetY
            speed = snapshot.player.speed
            attackRange = snapshot.player.attackRange
            attackDamage = snapshot.player.attackDamage
            attackCooldown = snapshot.player.attackCooldown
            attackTimer = snapshot.player.attackTimer
            hp = snapshot.player.hp
            maxHp = snapshot.player.maxHp
            slowTimer = snapshot.player.slowTimer
        }

        towers.clear()
        towers.addAll(snapshot.towers.map { towerSnapshot ->
            Tower(
                x = towerSnapshot.x,
                y = towerSnapshot.y,
                level = towerSnapshot.level,
                range = towerSnapshot.range,
                damage = towerSnapshot.damage,
                fireRate = towerSnapshot.fireRate,
                fireTimer = towerSnapshot.fireTimer,
                size = towerSnapshot.size,
                type = runCatching { TowerType.valueOf(towerSnapshot.type) }.getOrDefault(TowerType.ARROW),
                targetingMode = runCatching { TargetingMode.valueOf(towerSnapshot.targetingMode) }
                    .getOrDefault(TargetingMode.CLOSE),
                abilityTimer = towerSnapshot.abilityTimer,
                debuffTimer = towerSnapshot.debuffTimer
            )
        })

        enemies.clear()
        enemies.addAll(snapshot.enemies.map { enemySnapshot ->
            Enemy(
                x = enemySnapshot.x,
                y = enemySnapshot.y,
                speed = enemySnapshot.speed,
                hp = enemySnapshot.hp,
                maxHp = enemySnapshot.maxHp,
                goldReward = enemySnapshot.goldReward,
                damage = enemySnapshot.damage,
                size = enemySnapshot.size,
                type = runCatching { EnemyType.valueOf(enemySnapshot.type) }.getOrDefault(EnemyType.GOBLIN),
                bossType = enemySnapshot.bossType?.let { bossName ->
                    runCatching { BossType.valueOf(bossName) }.getOrNull()
                },
                hitFlash = enemySnapshot.hitFlash,
                pathIndex = enemySnapshot.pathIndex,
                waypointIndex = enemySnapshot.waypointIndex,
                bossAbilityTimer = enemySnapshot.bossAbilityTimer,
                bossAbilityCooldown = enemySnapshot.bossAbilityCooldown,
                isCharging = enemySnapshot.isCharging,
                chargeTimer = enemySnapshot.chargeTimer,
                hasSplit = enemySnapshot.hasSplit
            ).apply {
                iceSlowFactor = enemySnapshot.iceSlowFactor
                deepFreezeTimer = enemySnapshot.deepFreezeTimer
                regenRate = enemySnapshot.regenRate
                reachedBase = enemySnapshot.reachedBase
                isElite = enemySnapshot.isElite
                shieldTimer = enemySnapshot.shieldTimer
            }
        })

        projectiles.clear()
        projectiles.addAll(snapshot.projectiles.map { projectileSnapshot ->
            Projectile(
                x = projectileSnapshot.x,
                y = projectileSnapshot.y,
                targetX = projectileSnapshot.targetX,
                targetY = projectileSnapshot.targetY,
                speed = projectileSnapshot.speed,
                damage = projectileSnapshot.damage,
                size = projectileSnapshot.size,
                color = projectileSnapshot.color,
                alive = projectileSnapshot.alive
            )
        })

        floatingTexts.clear()
        particles.clear()
        gameOver = false
        newAchievement = null
        achievementBannerTimer = 0f
        nextWavePreview = null
        if (!waveInProgress && !campaignVictory) {
            generateNextWavePreview()
        }
    }

    private fun generateNextWavePreview() {
        waveDirector.generateNextWavePreview()
        return
        val nextWave = wave + 1
        // Preview modifier — pre-determine so startNextWave() can reuse it
        val previewMod = if (isDailyChallenge && dailyChallengeModifiers.isNotEmpty()) {
            dailyChallengeModifiers[nextWave % dailyChallengeModifiers.size]
        } else if (nextWave >= 3 && !isDailyChallenge) {
            val mods = WaveModifier.entries.filter { it != WaveModifier.NONE }
            if (Math.random() < 0.4) mods.random() else WaveModifier.NONE
        } else WaveModifier.NONE

        if (nextWave % bossInterval == 0) {
            // Refill pool the same way startNextWave() would, so preview matches actual
            if (bossPool.isEmpty()) bossPool.addAll(BossType.entries.shuffled())
            val type = bossPool.first()
            nextWavePreview = WavePreview(true, emptyMap(), type, previewMod)
        } else {
            val enemyCounts = mutableMapOf<EnemyType, Int>()
            var count = ((3 + nextWave * 2) * spawnRateMult).toInt().coerceAtMost(100)
            if (previewMod == WaveModifier.SWARM) count *= 2
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
            nextWavePreview = WavePreview(false, enemyCounts, modifier = previewMod)
        }
    }

    fun update(dt: Float) { synchronized(lock) {
        if (gameOver || campaignVictory || isPaused) return

        if (mapType == MapType.DESERT) {
            if (desertStormDuration > 0f) {
                desertStormDuration -= dt
                if (desertStormDuration <= 0f) {
                    desertStormDuration = 0f
                    floatingTexts.add(FloatingText(screenW / 2f, screenH * 0.18f,
                        "Sandstorm clears", 0xFFFFE082.toInt(), 1.2f, 24f))
                }
            } else {
                desertStormTimer -= dt
                if (desertStormTimer <= 0f) {
                    desertStormDuration = 6f
                    desertStormTimer = 18f
                    floatingTexts.add(FloatingText(screenW / 2f, screenH * 0.18f,
                        "SANDSTORM!", 0xFFFFB74D.toInt(), 1.4f, 30f))
                }
            }
        } else {
            desertStormDuration = 0f
            desertStormTimer = 12f
        }

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
            waveTimer = if (isBossRush) 3f else waveDelay
            SoundManager.play(SfxType.WAVE_COMPLETE)
            if (baseHp >= baseHpBeforeWave) checkAchievement("no_damage")
            if (baseHp == 1f) checkAchievement("survivor_1hp")
            if (gameSpeed == 3 && wave >= 10) checkAchievement("speed_demon")

            // Gold interest between waves
            val interest = (gold * interestRate).toInt()
            if (interest > 0) {
                gold += interest
                totalGoldEarned += interest
                floatingTexts.add(FloatingText(baseX + 60f, baseY - 100f, "+${interest}g interest!", 0xFF81C784.toInt(), 1.5f, 26f))
            }

            val bonus = ((wave * 5 + skillTree.bonusWaveGold()) * goldMult).toInt()
            gold += bonus
            totalGoldEarned += bonus
            floatingTexts.add(FloatingText(baseX, baseY - 80f, "+${bonus}g wave bonus!", 0xFFFFD700.toInt(), 1.5f, 32f))

            // Campaign victory check
            val cl = campaignLevel
            if (cl != null && wave >= cl.targetWave) {
                campaignVictory = true
                SoundManager.play(SfxType.VICTORY)
                prefs.edit().putBoolean("campaign_${cl.id}", true).apply()
                skillTree.addDiamonds(cl.diamondReward)
                diamondsEarnedThisRun += cl.diamondReward

                // Campaign achievements
                val completed = CampaignData.levels.count { prefs.getBoolean("campaign_${it.id}", false) }
                if (completed >= 5) checkAchievement("campaign_5")
                if (completed >= 10) checkAchievement("campaign_10")
                if (completed >= CampaignData.levels.size) checkAchievement("campaign_all")
                if (baseHp >= maxBaseHp) {
                    checkAchievement("campaign_no_damage")
                    val fullHpLevels = CampaignData.levels.count {
                        prefs.getBoolean("campaign_${it.id}_fullhp", false)
                    } + 1 // +1 for this level
                    prefs.edit().putBoolean("campaign_${cl.id}_fullhp", true).apply()
                    if (fullHpLevels >= 5) checkAchievement("campaign_3star")
                }
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

        PowerType.entries.forEach { p ->
            val cd = powerCooldowns.getOrDefault(p, 0f)
            if (cd > 0) powerCooldowns[p] = cd - dt
        }
        if (freezeTimer > 0) freezeTimer -= dt

        if (shakeTimer > 0) shakeTimer -= dt

        if (comboTimer > 0) {
            comboTimer -= dt
            if (comboTimer <= 0) {
                if (comboCount >= 5) {
                    SoundManager.play(SfxType.COMBO)
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

        if (player.hp > 0) player.update(dt)
        if (dashCooldown > 0) dashCooldown -= dt
        if (isDashing) isDashing = false

        if (player.hp > 0 && player.canAttack()) {
            val effectivePlayerRange =
                player.attackRange *
                    (if (currentWaveModifier == WaveModifier.INVISIBLE) 0.7f else 1f) *
                    mapPlayerRangeMultiplier()
            val nearest = enemies.filter { it.hp > 0 }.minByOrNull { it.distanceTo(player.x, player.y) }
            if (nearest != null && nearest.distanceTo(player.x, player.y) < effectivePlayerRange) {
                val shieldRed = if (nearest.shieldTimer > 0) 0.3f else 1f
                val actualPlayerDmg = mapAdjustedDamage(nearest, player.attackDamage * shieldRed)
                nearest.hp -= actualPlayerDmg
                nearest.hitFlash = 0.15f
                player.attack()
                SoundManager.play(SfxType.PLAYER_ATTACK)
                projectiles.add(Projectile(player.x, player.y, nearest.x, nearest.y,
                    damage = 0f, color = 0xFF42A5F5.toInt()))
                floatingTexts.add(FloatingText(nearest.x, nearest.y - nearest.size,
                    "-${actualPlayerDmg.toInt()}", 0xFF42A5F5.toInt(), 0.8f, 22f))
            }
        }

        val speedMult = if (freezeTimer > 0) 0.2f else 1f
        // Reset ice slow on all enemies each frame, then reapply from ice towers
        enemies.forEach {
            if (it.deepFreezeTimer > 0) { it.deepFreezeTimer -= dt; it.iceSlowFactor = 0.05f }
            else it.iceSlowFactor = 1f
            if (it.shieldTimer > 0) it.shieldTimer -= dt
        }
        // Ice tower slow aura
        towers.filter { it.type == TowerType.ICE }.forEach { tower ->
            val iceRange = getDisplayedTowerRange(tower)
            enemies.forEach { enemy ->
                if (enemy.distanceTo(tower.x, tower.y) < iceRange) {
                    val slowPower = (0.5f - tower.level * 0.03f - skillTree.iceSlowBonus() - mapIceSlowBonus()).coerceAtLeast(0.05f)
                    enemy.iceSlowFactor = enemy.iceSlowFactor.coerceAtMost(slowPower)
                }
            }
        }
        enemies.forEach { enemy ->
            // Regen from wave modifier
            if (enemy.regenRate > 0 && enemy.hp < enemy.maxHp) {
                enemy.hp = (enemy.hp + enemy.regenRate * dt).coerceAtMost(enemy.maxHp)
            }
            val chargeBoost = if (enemy.isCharging) 3f else 1f
            val iceSlow = enemy.iceSlowFactor
            // Follow waypoints along assigned path
            val path = paths.getOrNull(enemy.pathIndex)
            val target = path?.waypoints?.getOrNull(enemy.waypointIndex)
            if (target != null) {
                val dx = target.x - enemy.x
                val dy = target.y - enemy.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist > enemy.size * 0.5f) {
                    enemy.x += (dx / dist) * enemy.speed * speedMult * chargeBoost * iceSlow * mapEnemySpeedMultiplier() * dt
                    enemy.y += (dy / dist) * enemy.speed * speedMult * chargeBoost * iceSlow * mapEnemySpeedMultiplier() * dt
                } else {
                    enemy.waypointIndex++
                }
            } else {
                // Past last waypoint or no path — head to base
                val dx = baseX - enemy.x
                val dy = baseY - enemy.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist > enemy.size) {
                    enemy.x += (dx / dist) * enemy.speed * speedMult * chargeBoost * iceSlow * mapEnemySpeedMultiplier() * dt
                    enemy.y += (dy / dist) * enemy.speed * speedMult * chargeBoost * iceSlow * mapEnemySpeedMultiplier() * dt
                }
            }
            if (enemy.hitFlash > 0) enemy.hitFlash -= dt
            if (enemy.isAtBase(baseX, baseY)) {
                baseHp -= enemy.damage
                SoundManager.play(SfxType.BASE_HIT)
                enemy.reachedBase = true
                enemy.hp = 0f  // Remove enemy after dealing damage once
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
            // Enemies that reached the base are removed but give no rewards
            if (enemy.reachedBase) return@forEach

            SoundManager.play(SfxType.ENEMY_DIE)
            val comboGold = (enemy.goldReward * comboMultiplier * skillTree.goldBonusMultiplier()).toInt()
            gold += comboGold
            totalGoldEarned += comboGold
            score += comboGold
            totalKills++

            comboCount++
            comboTimer = 2f
            comboMultiplier = (1f + comboCount * 0.1f).coerceAtMost(5f)
            if (comboCount > bestCombo) bestCombo = comboCount

            floatingTexts.add(FloatingText(enemy.x, enemy.y, "+${comboGold}g", 0xFFFFD700.toInt()))

            // Death burst — colored sparks outward
            repeat(12) {
                val angle = Math.random() * Math.PI * 2
                val speed = 120f + (Math.random() * 100f).toFloat()
                particles.add(Particle(
                    enemy.x, enemy.y,
                    (Math.cos(angle) * speed).toFloat(), (Math.sin(angle) * speed).toFloat(),
                    0.6f, enemy.type.color, 5f
                ))
            }
            // Debris (smaller, grayish)
            repeat(6) {
                val debrisAngle = Math.random() * Math.PI * 2
                particles.add(Particle(
                    enemy.x, enemy.y,
                    (Math.cos(debrisAngle) * 70).toFloat(), (Math.sin(debrisAngle) * 70 - 30).toFloat(),
                    0.8f, 0xFFBDBDBD.toInt(), 3f
                ))
            }
            // Expanding shockwave ring
            particles.add(Particle(
                enemy.x, enemy.y,
                0f, 0f,
                0.4f, 0x55FFFFFF, 25f
            ))
            // Inner flash (bright center burst)
            particles.add(Particle(
                enemy.x, enemy.y,
                0f, 0f,
                0.2f, 0xCCFFFFFF.toInt(), 18f
            ))
            // Rising smoke wisps
            repeat(4) {
                val ox = (Math.random().toFloat() - 0.5f) * enemy.size
                particles.add(Particle(
                    enemy.x + ox, enemy.y,
                    (Math.random().toFloat() - 0.5f) * 20f, -(40f + Math.random().toFloat() * 60f),
                    1.0f, 0x44666666, 4f + Math.random().toFloat() * 3f
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
                if (bossesKilledThisRun >= 10) checkAchievement("10_bosses")
                currentBoss = null
                // Boss always drops diamonds
                val diamondDrop = (2 + wave / 5).coerceAtMost(10)
                skillTree.addDiamonds(diamondDrop)
                diamondsEarnedThisRun += diamondDrop
                SoundManager.play(SfxType.DIAMOND_DROP)
                floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 20f,
                    "+${diamondDrop} \uD83D\uDC8E", 0xFF00E5FF.toInt(), 1.5f, 30f))
            } else {
                // Regular enemies have a small diamond drop chance
                val dropChance = 0.03f + skillTree.diamondDropBonus()
                if (Math.random() < dropChance) {
                    skillTree.addDiamonds(1)
                    diamondsEarnedThisRun += 1
                    SoundManager.play(SfxType.DIAMOND_DROP)
                    floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 20f,
                        "+1 \uD83D\uDC8E", 0xFF00E5FF.toInt(), 1.2f, 24f))
                }
            }

            checkAchievement("first_kill")
            if (totalKills >= 50) checkAchievement("kills_50")
            if (totalKills >= 200) checkAchievement("kills_200")
            if (totalKills >= 500) checkAchievement("kills_500")
            if (totalKills >= 1000) checkAchievement("kills_1000")
            if (comboCount >= 10) checkAchievement("combo_10")
            if (comboCount >= 20) checkAchievement("combo_20")
            if (score >= 1000) checkAchievement("score_1000")
            if (diamondsEarnedThisRun >= 10) checkAchievement("diamond_10")
            if (diamondsEarnedThisRun >= 50) checkAchievement("diamond_50")
        }
        enemies.removeAll { it.isDead() }

        val towerDmgMult = (1f + (playerDamageLevel - 1) * 0.1f) * skillTree.towerDamageMultiplier() * skillTree.prestigeDamageMultiplier()

        // Pre-compute tower synergy: same-type towers nearby boost each other by 10%
        val synergyMap = mutableMapOf<Tower, Float>()
        for (tower in towers) {
            var synCount = 0
            for (other in towers) {
                if (other === tower) continue
                if (other.type == tower.type && tower.distanceTo(other.x, other.y) < tower.range * 1.2f) {
                    synCount++
                }
            }
            synergyMap[tower] = 1f + synCount.coerceAtMost(3) * 0.10f
        }

        towers.forEach { tower ->
            tower.update(dt)
            // ICE towers don't fire projectiles — they use aura (handled above)
            if (tower.type == TowerType.ICE) return@forEach
            val effectiveRange = getDisplayedTowerRange(tower)
            if (tower.canFire()) {
                val inRange = enemies.filter { it.hp > 0 && it.distanceTo(tower.x, tower.y) < effectiveRange }
                val target = when (tower.targetingMode) {
                    TargetingMode.CLOSE -> inRange.minByOrNull { it.distanceTo(tower.x, tower.y) }
                    TargetingMode.FIRST -> inRange.maxByOrNull { enemyPathProgress(it) }
                    TargetingMode.LAST -> inRange.minByOrNull { enemyPathProgress(it) }
                    TargetingMode.STRONG -> inRange.maxByOrNull { it.hp }
                }
                if (target != null) {
                    tower.fire()
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val synergyMult = synergyMap[tower] ?: 1f
                    val isCrit = Math.random() < critChance
                    val critMult = if (isCrit) critMultiplier else 1f
                    val shieldMult = if (target.shieldTimer > 0) 0.3f else 1f
                    val dmg = mapAdjustedDamage(
                        target,
                        tower.damage * towerDmgMult * resistMult * synergyMult * critMult * shieldMult * mapTowerDamageMultiplier(tower)
                    )
                    target.hp -= dmg
                    target.hitFlash = 0.15f
                    SoundManager.play(when (tower.type) {
                        TowerType.ARROW -> SfxType.ARROW_FIRE
                        TowerType.MAGIC -> SfxType.MAGIC_FIRE
                        TowerType.CANNON -> SfxType.CANNON_FIRE
                        TowerType.POISON -> SfxType.POISON_FIRE
                        TowerType.TESLA -> SfxType.TESLA_FIRE
                        TowerType.ICE -> SfxType.ICE_FIRE
                    })
                    projectiles.add(Projectile(tower.x, tower.y, target.x, target.y,
                        damage = 0f,
                        color = when (tower.type) {
                            TowerType.ARROW -> 0xFFFFEB3B.toInt()
                            TowerType.MAGIC -> 0xFFAB47BC.toInt()
                            TowerType.CANNON -> 0xFFFF7043.toInt()
                            TowerType.POISON -> 0xFF66BB6A.toInt()
                            TowerType.TESLA -> 0xFF29B6F6.toInt()
                            TowerType.ICE -> 0xFF81D4FA.toInt()
                        }
                    ))
                    val dmgColor = if (isCrit) 0xFFFF1744.toInt() else if (resistMult > 1f) 0xFFFF5722.toInt() else if (resistMult < 1f) 0xFF9E9E9E.toInt() else 0xFFFFEB3B.toInt()
                    floatingTexts.add(FloatingText(target.x, target.y - target.size,
                        if (isCrit) "CRIT! -${dmg.toInt()}" else "-${dmg.toInt()}", dmgColor, if (isCrit) 1f else 0.6f, if (isCrit) 26f else 18f))
                    if (isCrit) {
                        // Red crit particles
                        repeat(6) {
                            particles.add(Particle(target.x, target.y,
                                (Math.random().toFloat() - 0.5f) * 200f,
                                (Math.random().toFloat() - 0.5f) * 200f,
                                0.5f, 0xFFFF1744.toInt(), 5f))
                        }
                    }
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
        if (gold >= 2000) checkAchievement("gold_hoarder")

        if (baseHp <= 0) {
            baseHp = 0f
            gameOver = true
            SoundManager.play(SfxType.GAME_OVER)
            isNewHighScore = score > highScore
            isNewEndlessRecord = isEndlessMode && wave > endlessHighWave
            // Batch all SharedPreferences writes into a single editor
            val editor = prefs.edit()
            if (score > highScore) { highScore = score; editor.putInt("highScore", highScore) }
            if (wave > highWave) { highWave = wave; editor.putInt("highWave", highWave) }
            if (difficulty == 1 && wave >= 10) {
                editor.putBoolean("normal_beaten", true)
            }
            if (isEndlessMode && wave > endlessHighWave) {
                endlessHighWave = wave
                editor.putInt("endlessHighWave", endlessHighWave)
            }
            if (isBossRush) {
                isNewBossRushRecord = bossRushWave > bossRushHighWave
                if (isNewBossRushRecord) {
                    bossRushHighWave = bossRushWave
                    editor.putInt("bossRushHighWave", bossRushHighWave)
                }
            }
            editor
                .putInt("lifetime_kills", prefs.getInt("lifetime_kills", 0) + totalKills)
                .putInt("lifetime_games", prefs.getInt("lifetime_games", 0) + 1)
                .putInt("lifetime_waves", prefs.getInt("lifetime_waves", 0) + wave)
                .putInt("lifetime_score", prefs.getInt("lifetime_score", 0) + score)
                .putInt("lifetime_gold", prefs.getInt("lifetime_gold", 0) + totalGoldEarned)
                .putInt("lifetime_towers", prefs.getInt("lifetime_towers", 0) + towers.size)
                .putInt("lifetime_bosses", prefs.getInt("lifetime_bosses", 0) + bossesKilledThisRun)
            val best = prefs.getInt("lifetime_best_combo", 0)
            if (bestCombo > best) editor.putInt("lifetime_best_combo", bestCombo)
            val towerCounts = towers.groupBy { it.type.name }.mapValues { it.value.size }
            val fav = towerCounts.maxByOrNull { it.value }
            if (fav != null) {
                val key = "tower_count_${fav.key}"
                editor.putInt(key, prefs.getInt(key, 0) + fav.value)
            }
            editor.apply()
        }
    } }

    private fun startNextWave() {
        waveDirector.startNextWave()
        return
        wave++
        waveInProgress = true
        showWaveBanner = true
        waveBannerTimer = 1.5f
        eliteSpawnedThisWave = false

        // Endless mode: progressive scaling every 10 waves
        if (isEndlessMode && wave > 10) {
            val tier = ((wave - 10) / 10f).coerceAtMost(5f)
            enemyHpMult = 1f + tier * 0.15f
            enemyDmgMult = 1f + tier * 0.10f
            enemySpeedMult = 1f + tier * 0.05f
            spawnRateMult = 1f + tier * 0.08f
        }

        // Day/Night cycle: toggle every 8 waves
        isNight = (wave / 8) % 2 == 1
        SoundManager.play(SfxType.WAVE_START)
        if (wave >= 5) checkAchievement("wave_5")
        if (wave >= 10) checkAchievement("wave_10")
        if (wave >= 20) checkAchievement("wave_20")
        if (wave >= 30) checkAchievement("wave_30")
        if (wave >= 50) checkAchievement("wave_50")
        if (wave >= 100) checkAchievement("wave_100")
        if (isEndlessMode && wave >= 10) checkAchievement("endless_10")
        baseHpBeforeWave = baseHp
        if (isBossRush) bossRushWave++

        // Use pre-determined modifier from preview (so preview matches reality)
        val preview = nextWavePreview
        currentWaveModifier = preview?.modifier ?: WaveModifier.NONE

        if (isBossRush || wave % bossInterval == 0) {
            if (bossPool.isEmpty()) {
                bossPool.addAll(BossType.entries.shuffled())
            }
            currentBoss = bossPool.removeAt(0)
            enemiesRemaining = 1
            val minionCount = if (isBossRush) {
                // Fewer minions in boss rush — focus is on the bosses
                (currentBoss!!.minionCount * spawnRateMult * 0.6f).toInt().coerceAtLeast(1)
            } else {
                (currentBoss!!.minionCount * spawnRateMult).toInt().coerceAtLeast(2)
            }
            totalEnemiesThisWave = 1 + minionCount
            SoundManager.play(SfxType.BOSS_APPEAR)
        } else {
            currentBoss = null
            var count = ((3 + wave * 2) * spawnRateMult).toInt().coerceAtMost(100)
            if (currentWaveModifier == WaveModifier.SWARM) count *= 2
            enemiesRemaining = count
            totalEnemiesThisWave = count
        }
        enemiesSpawnedThisWave = 0
    }

    private fun spawnEnemy() {
        waveDirector.spawnEnemy()
        return
        if (paths.isEmpty()) return
        val pathIdx = paths.indices.random()
        val spawn = paths[pathIdx].spawnPoint
        val waveScale = 1f + (wave - 1) * 0.15f

        val boss = currentBoss
        if (boss != null) {
            val bossRushScale = if (isBossRush) 1f + bossRushWave * 0.12f else 1f
            val hp = (boss.baseHp + wave * 40f) * waveScale * enemyHpMult * bossRushScale
            enemies.add(Enemy(
                x = spawn.x, y = spawn.y,
                speed = boss.baseSpeed * enemySpeedMult,
                hp = hp, maxHp = hp,
                goldReward = ((boss.baseGold + wave * 10f) * waveScale * goldMult).toInt().coerceAtLeast(1),
                damage = boss.baseDmg * waveScale * enemyDmgMult * bossRushScale,
                type = EnemyType.BOSS, bossType = boss, size = 55f,
                pathIndex = pathIdx,
                bossAbilityTimer = 5f
            ))
            val minionCount = if (isBossRush) {
                (boss.minionCount * spawnRateMult * 0.6f).toInt().coerceAtLeast(1)
            } else {
                (boss.minionCount * spawnRateMult).toInt().coerceAtLeast(2)
            }
            spawnBossMinions(boss, waveScale, minionCount)
            enemiesSpawnedThisWave += 1 + minionCount
            return
        }

        val type = when {
            wave >= 12 && Math.random() < 0.08 -> EnemyType.ARMORED_GOLEM
            wave >= 10 && Math.random() < 0.1 -> EnemyType.DRAGON
            wave >= 8 && Math.random() < 0.15 -> EnemyType.DEMON
            wave >= 7 && Math.random() < 0.12 -> EnemyType.FAST_SKELETON
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
            EnemyType.FAST_SKELETON -> arrayOf(25f, 150f, 6f, 6f)
            EnemyType.ARMORED_GOLEM -> arrayOf(120f, 40f, 15f, 18f)
            else -> arrayOf(20f, 80f, 3f, 5f)
        }

        // Apply wave modifier effects
        var hpMod = 1f
        var speedMod = 1f
        var goldMod = 1f
        var regen = 0f
        when (currentWaveModifier) {
            WaveModifier.FAST -> speedMod = 2f
            WaveModifier.ARMORED -> hpMod = 1.5f
            WaveModifier.REGEN -> regen = 3f + wave * 0.5f
            WaveModifier.SWARM -> { hpMod = 0.5f }
            WaveModifier.RICH -> goldMod = 2f
            WaveModifier.BOSS_RALLY -> speedMod = 1.4f
            else -> {}
        }

        val hp = baseHpVal * waveScale * enemyHpMult * hpMod * nightHpMultiplier
        // Elite enemies on every 5th non-boss wave (first enemy of the wave)
        val isElite = wave % 5 == 0 && wave % bossInterval != 0 && !eliteSpawnedThisWave && wave >= 5
        val eliteHpMult = if (isElite) 3f else 1f
        val eliteGoldMult = if (isElite) 2f else 1f
        val eliteSize = if (isElite) 42f else 30f
        if (isElite) eliteSpawnedThisWave = true
        val enemy = Enemy(
            x = spawn.x,
            y = spawn.y,
            speed = (baseSpeed + (Math.random() * 20).toFloat()) * enemySpeedMult * speedMod,
            hp = hp * eliteHpMult,
            maxHp = hp * eliteHpMult,
            goldReward = (baseGold * waveScale * goldMult * goldMod * eliteGoldMult).toInt().coerceAtLeast(1),
            damage = baseDmg * waveScale * enemyDmgMult * (if (isElite) 1.5f else 1f),
            type = type,
            size = eliteSize,
            pathIndex = pathIdx
        )
        enemy.regenRate = regen
        enemy.isElite = isElite
        enemies.add(enemy)
        enemiesSpawnedThisWave++
    }

    private fun spawnBossMinions(boss: BossType, waveScale: Float, count: Int) {
        // Apply wave modifier effects to minions
        var hpMod = 1f; var speedMod = 1f; var goldMod = 1f; var regen = 0f
        when (currentWaveModifier) {
            WaveModifier.FAST -> speedMod = 2f
            WaveModifier.ARMORED -> hpMod = 1.5f
            WaveModifier.REGEN -> regen = 3f + wave * 0.5f
            WaveModifier.SWARM -> hpMod = 0.5f
            WaveModifier.RICH -> goldMod = 2f
            WaveModifier.BOSS_RALLY -> speedMod = 1.4f
            else -> {}
        }
        repeat(count) {
            val pathIdx = if (paths.isNotEmpty()) paths.indices.random() else 0
            val sp = if (paths.isNotEmpty()) paths[pathIdx].spawnPoint else PointF(screenW / 2, -40f)
            val mHp = (15f + wave * 3f) * waveScale * enemyHpMult * hpMod
            val enemy = Enemy(
                x = sp.x + ((Math.random() - 0.5) * 80).toFloat(),
                y = sp.y + ((Math.random() - 0.5) * 40).toFloat(),
                speed = (70f + (Math.random() * 30).toFloat()) * enemySpeedMult * speedMod,
                hp = mHp, maxHp = mHp,
                goldReward = ((2f + wave) * goldMult * goldMod).toInt().coerceAtLeast(1),
                damage = (4f + wave) * waveScale * enemyDmgMult,
                type = boss.minionType,
                size = 22f,
                pathIndex = pathIdx
            )
            enemy.regenRate = regen
            enemies.add(enemy)
        }
    }

    private fun executeBossAbility(boss: Enemy, dt: Float) {
        bossSystem.executeBossAbility(boss)
        return
        val bt = boss.bossType ?: return
        when (bt.ability) {
            BossAbility.CHARGE -> {
                boss.isCharging = true
                boss.chargeTimer = 2f
                SoundManager.play(SfxType.BOSS_CHARGE)
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\u26A1 CHARGE!", bt.color, 1.2f, 28f))
                shakeTimer = 0.2f; shakeIntensity = 6f
            }
            BossAbility.SUMMON -> {
                val waveScale = 1f + (wave - 1) * 0.15f
                repeat(3) {
                    val pathIdx = boss.pathIndex
                    val mHp = (15f + wave * 3f) * waveScale * enemyHpMult
                    enemies.add(Enemy(
                        x = boss.x + ((Math.random() - 0.5) * 60).toFloat(),
                        y = boss.y + ((Math.random() - 0.5) * 40).toFloat(),
                        speed = (70f + (Math.random() * 30).toFloat()) * enemySpeedMult,
                        hp = mHp, maxHp = mHp,
                        goldReward = ((2f + wave) * goldMult).toInt().coerceAtLeast(1),
                        damage = (4f + wave) * waveScale * enemyDmgMult,
                        type = bt.minionType, size = 22f, pathIndex = pathIdx,
                        waypointIndex = boss.waypointIndex.coerceAtMost(
                            (paths.getOrNull(pathIdx)?.waypoints?.size ?: 1) - 1
                        )
                    ))
                }
                totalEnemiesThisWave += 3
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDC7E SUMMON!", bt.color, 1.2f, 28f))
                SoundManager.play(SfxType.BOSS_SUMMON)
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
                SoundManager.play(SfxType.BOSS_HEAL)
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
                SoundManager.play(SfxType.BOSS_AOE)
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
                // Temporary shield: 70% damage reduction for 5 seconds (beatable!)
                boss.shieldTimer = 5f
                SoundManager.play(SfxType.BOSS_SHIELD)
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDEE1\uFE0F SHIELD!", 0xFF29B6F6.toInt(), 1.5f, 28f))
                repeat(12) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(
                        boss.x + (Math.cos(angle) * 40).toFloat(),
                        boss.y + (Math.sin(angle) * 40).toFloat(),
                        0f, 0f, 0.8f, 0xFF29B6F6.toInt(), 4f))
                }
            }
            BossAbility.SCREECH -> {
                player.slowTimer = maxOf(player.slowTimer, 4f)
                var affectedTowers = 0
                towers.forEach { tower ->
                    if (tower.distanceTo(boss.x, boss.y) < 260f) {
                        tower.debuffTimer = maxOf(tower.debuffTimer, 4f)
                        tower.fireTimer += 0.75f
                        affectedTowers++
                    }
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDC09 SCREECH!", bt.color, 1.2f, 30f))
                floatingTexts.add(FloatingText(player.x, player.y - 36f, "SLOWED!", 0xFFFFB300.toInt(), 1f, 22f))
                if (affectedTowers > 0) {
                    floatingTexts.add(FloatingText(boss.x, boss.y + boss.size, "TOWERS JAMMED!", 0xFFFF7043.toInt(), 1f, 20f))
                }
                SoundManager.play(SfxType.BOSS_SCREECH)
                shakeTimer = 0.25f; shakeIntensity = 9f
                repeat(14) {
                    val angle = Math.random() * Math.PI * 2
                    val speed = 70 + Math.random() * 110
                    particles.add(Particle(
                        boss.x, boss.y,
                        (Math.cos(angle) * speed).toFloat(),
                        (Math.sin(angle) * speed).toFloat(),
                        0.45f, bt.color, 5f
                    ))
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
                    SoundManager.play(SfxType.BOSS_TELEPORT)
                }
            }
            BossAbility.DRAIN -> {
                val stolen = (10 + wave).coerceAtMost(gold)
                if (stolen > 0) {
                    gold -= stolen
                    boss.hp = (boss.hp + stolen * 2f).coerceAtMost(boss.maxHp * 1.2f)
                    floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "-${stolen}g DRAIN!", 0xFFE040FB.toInt(), 1.2f, 28f))
                    SoundManager.play(SfxType.BOSS_DRAIN)
                    floatingTexts.add(FloatingText(screenW / 2, screenH * 0.4f, "-${stolen} gold stolen!", 0xFFF44336.toInt(), 1.5f, 32f))
                }
            }
            BossAbility.QUAKE -> {
                shakeTimer = 1f; shakeIntensity = 20f
                // Slow all towers
                towers.forEach { it.fireTimer += 1.5f }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83C\uDF0B QUAKE!", bt.color, 1.5f, 32f))
                SoundManager.play(SfxType.BOSS_QUAKE)
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
                    val cloneHp = boss.hp * 0.3f
                    boss.hp = boss.hp * 0.4f  // Boss loses 60% of remaining HP when splitting
                    repeat(2) {
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
                    totalEnemiesThisWave += 2
                    floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83E\uDDA0 SPLIT!", bt.color, 1.2f, 28f))
                    SoundManager.play(SfxType.BOSS_SPLIT)
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

    /** Compute how far an enemy has progressed along its path (higher = closer to base) */
    private fun enemyPathProgress(enemy: Enemy): Float {
        val path = paths.getOrNull(enemy.pathIndex) ?: return enemy.waypointIndex.toFloat()
        val wps = path.waypoints
        val wpIdx = enemy.waypointIndex.coerceIn(0, wps.size - 1)
        val wp = wps[wpIdx]
        val dx = wp.x - enemy.x
        val dy = wp.y - enemy.y
        val distToWp = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        // Segment length for normalization (avoid div-by-zero)
        val segLen = if (wpIdx > 0) {
            val prev = wps[wpIdx - 1]
            val sx = wp.x - prev.x; val sy = wp.y - prev.y
            Math.sqrt((sx * sx + sy * sy).toDouble()).toFloat().coerceAtLeast(1f)
        } else 100f
        return wpIdx + (1f - (distToWp / segLen).coerceIn(0f, 1f))
    }

    fun placeTower(x: Float, y: Float, type: TowerType): Boolean { synchronized(lock) {
        if (!isTowerAllowed(type)) return false
        val cost = getTowerCost(type)
        if (gold < cost) return false
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

        gold -= cost
        towers.add(Tower(x, y, type = type, damage = type.baseDamage, range = type.baseRange, fireRate = type.baseFireRate))
        SoundManager.play(SfxType.TOWER_PLACE)
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
        SoundManager.play(SfxType.TOWER_UPGRADE)
        if (tower.level >= 5) checkAchievement("max_tower")
        return true
    } }

    fun sellTower(tower: Tower): Boolean { synchronized(lock) {
        val refund = (tower.sellValue() * (1f + skillTree.sellValueBonus())).toInt()
        towers.remove(tower)
        gold += refund
        SoundManager.play(SfxType.TOWER_SELL)
        floatingTexts.add(FloatingText(tower.x, tower.y, "+${refund}g", 0xFFFFD700.toInt(), 1f, 26f))
        repeat(8) {
            val angle = Math.random() * Math.PI * 2
            particles.add(Particle(tower.x, tower.y,
                (Math.cos(angle) * 80).toFloat(), (Math.sin(angle) * 80).toFloat(),
                0.4f, 0xFFBDBDBD.toInt(), 4f))
        }
        return true
    } }

    fun activateTowerAbility(tower: Tower): Boolean { synchronized(lock) {
        if (!tower.canUseAbility()) return false
        tower.useAbility()
        // Apply prestige cooldown reduction
        tower.abilityTimer *= (1f - skillTree.abilityCooldownReduction())
        SoundManager.play(SfxType.TOWER_ABILITY)
        val towerDmgMult = (1f + (playerDamageLevel - 1) * 0.1f) * skillTree.towerDamageMultiplier() * skillTree.prestigeDamageMultiplier()
        val towerMapDamageMult = mapTowerDamageMultiplier(tower)
        val baseRange = getDisplayedTowerRange(tower)
        when (tower.type) {
            TowerType.ARROW -> {
                // Volley: fire 5 rapid shots at different enemies
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < baseRange }
                    .shuffled().take(5)
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = mapAdjustedDamage(target, tower.damage * towerDmgMult * 1.5f * resistMult * towerMapDamageMult)
                    target.hp -= dmg
                    target.hitFlash = 0.2f
                    projectiles.add(Projectile(tower.x, tower.y, target.x, target.y, damage = 0f, color = 0xFFFFEB3B.toInt()))
                    floatingTexts.add(FloatingText(target.x, target.y - target.size, "-${dmg.toInt()}", 0xFFFFEB3B.toInt(), 0.5f, 16f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "VOLLEY!", 0xFFFFEB3B.toInt(), 1.2f, 28f))
            }
            TowerType.MAGIC -> {
                // Arcane Blast: AoE damage around tower
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < baseRange * 1.2f }
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = mapAdjustedDamage(target, tower.damage * towerDmgMult * 2f * resistMult * towerMapDamageMult)
                    target.hp -= dmg
                    target.hitFlash = 0.3f
                    floatingTexts.add(FloatingText(target.x, target.y - target.size, "-${dmg.toInt()}", 0xFFAB47BC.toInt(), 0.5f, 16f))
                }
                repeat(20) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * tower.range
                    particles.add(Particle(
                        tower.x + (Math.cos(angle) * dist).toFloat(),
                        tower.y + (Math.sin(angle) * dist).toFloat(),
                        0f, -30f, 0.6f, 0xFFAB47BC.toInt(), 6f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "ARCANE BLAST!", 0xFFAB47BC.toInt(), 1.2f, 28f))
                shakeTimer = 0.2f; shakeIntensity = 6f
            }
            TowerType.CANNON -> {
                // Napalm: fire zone that deals damage over time to enemies in range
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < baseRange }
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = mapAdjustedDamage(target, tower.damage * towerDmgMult * 3f * resistMult * towerMapDamageMult)
                    target.hp -= dmg
                    target.hitFlash = 0.3f
                    floatingTexts.add(FloatingText(target.x, target.y - target.size, "-${dmg.toInt()}", 0xFFFF7043.toInt(), 0.5f, 16f))
                }
                repeat(25) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * tower.range * 0.8
                    particles.add(Particle(
                        tower.x + (Math.cos(angle) * dist).toFloat(),
                        tower.y + (Math.sin(angle) * dist).toFloat(),
                        ((Math.random() - 0.5) * 30).toFloat(), -20f, 0.8f, 0xFFFF5722.toInt(), 7f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "NAPALM!", 0xFFFF7043.toInt(), 1.2f, 28f))
                shakeTimer = 0.3f; shakeIntensity = 8f
            }
            TowerType.POISON -> {
                // Plague: poison all enemies on screen
                enemies.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = mapAdjustedDamage(target, tower.damage * towerDmgMult * 0.5f * resistMult * towerMapDamageMult)
                    target.hp -= dmg
                    target.hitFlash = 0.2f
                    repeat(3) {
                        particles.add(Particle(target.x, target.y,
                            ((Math.random() - 0.5) * 40).toFloat(), -30f,
                            0.5f, 0xFF66BB6A.toInt(), 4f))
                    }
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "PLAGUE!", 0xFF66BB6A.toInt(), 1.2f, 28f))
            }
            TowerType.TESLA -> {
                // Overcharge: chain lightning to all enemies in extended range
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < baseRange * 1.5f }
                var prevX = tower.x; var prevY = tower.y
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = mapAdjustedDamage(target, tower.damage * towerDmgMult * 1.8f * resistMult * towerMapDamageMult)
                    target.hp -= dmg
                    target.hitFlash = 0.3f
                    repeat(4) { i ->
                        val t = i / 4f
                        particles.add(Particle(
                            prevX + (target.x - prevX) * t, prevY + (target.y - prevY) * t,
                            ((Math.random() - 0.5) * 30).toFloat(), ((Math.random() - 0.5) * 30).toFloat(),
                            0.3f, 0xFF29B6F6.toInt(), 3f))
                    }
                    prevX = target.x; prevY = target.y
                    floatingTexts.add(FloatingText(target.x, target.y - target.size, "-${dmg.toInt()}", 0xFF29B6F6.toInt(), 0.5f, 16f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "OVERCHARGE!", 0xFF29B6F6.toInt(), 1.2f, 28f))
                shakeTimer = 0.15f; shakeIntensity = 5f
            }
            TowerType.ICE -> {
                // Deep Freeze: stun all enemies in range for 3 seconds
                val iceRange = baseRange * 1.2f
                enemies.filter { it.distanceTo(tower.x, tower.y) < iceRange }.forEach { target ->
                    target.deepFreezeTimer = 3f * mapFreezeDurationMultiplier()
                    target.hitFlash = 0.5f
                    repeat(4) {
                        particles.add(Particle(target.x, target.y,
                            ((Math.random() - 0.5) * 50).toFloat(), ((Math.random() - 0.5) * 50).toFloat(),
                            0.6f, 0xFF81D4FA.toInt(), 4f))
                    }
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "DEEP FREEZE!", 0xFF81D4FA.toInt(), 1.2f, 28f))
            }
        }
        return true
    } }

    fun upgradePlayerDamage(): Boolean { synchronized(lock) {
        val cost = playerDamageLevel * 25
        if (gold < cost) return false
        gold -= cost
        playerDamageLevel++
        player.attackDamage += 5f
        SoundManager.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun upgradePlayerSpeed(): Boolean { synchronized(lock) {
        val cost = playerSpeedLevel * 20
        if (gold < cost) return false
        gold -= cost
        playerSpeedLevel++
        player.speed += 30f
        SoundManager.play(SfxType.PLAYER_UPGRADE)
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
        SoundManager.play(SfxType.PLAYER_UPGRADE)
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
        SoundManager.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun repairBase(): Boolean { synchronized(lock) {
        val cost = 20
        if (gold < cost) return false
        if (baseHp >= maxBaseHp) return false
        gold -= cost
        baseHp = (baseHp + 30f).coerceAtMost(maxBaseHp)
        SoundManager.play(SfxType.POWER_HEAL)
        repairsThisRun++
        if (repairsThisRun >= 3) checkAchievement("repaired_3")
        return true
    } }

    fun usePower(type: PowerType): Boolean { synchronized(lock) {
        if (!isPowerAllowed(type)) return false
        val cd = powerCooldowns.getOrDefault(type, 0f)
        if (cd > 0) return false

        when (type) {
            PowerType.FIREBALL -> {
                if (gold < type.cost) return false
                gold -= type.cost
                enemies.forEach { enemy ->
                    val shieldMult = if (enemy.shieldTimer > 0) 0.3f else 1f
                    enemy.hp -= mapAdjustedDamage(enemy, 50f * shieldMult * randomizerPowerDamageMult)
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
                SoundManager.play(SfxType.POWER_FIREBALL)
                powerCooldowns[type] = getRandomizedPowerCooldown(type)
            }
            PowerType.FREEZE -> {
                if (gold < type.cost) return false
                gold -= type.cost
                freezeTimer = 4f * mapFreezeDurationMultiplier()
                enemies.forEach { enemy ->
                    repeat(4) {
                        particles.add(Particle(enemy.x, enemy.y,
                            ((Math.random() - 0.5) * 80).toFloat(), ((Math.random() - 0.5) * 80).toFloat(),
                            0.5f, 0xFF81D4FA.toInt(), 4f))
                    }
                }
                floatingTexts.add(FloatingText(screenW / 2, screenH * 0.35f,
                    "FREEZE!", 0xFF29B6F6.toInt(), 1.5f, 44f))
                SoundManager.play(SfxType.POWER_FREEZE)
                powerCooldowns[type] = getRandomizedPowerCooldown(type)
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
                SoundManager.play(SfxType.POWER_HEAL)
                powerCooldowns[type] = getRandomizedPowerCooldown(type)
            }
            PowerType.LIGHTNING -> {
                if (gold < type.cost) return false
                gold -= type.cost
                val targets = enemies.sortedBy { it.distanceTo(player.x, player.y) }.take(5)
                var prevX = player.x; var prevY = player.y
                targets.forEach { enemy ->
                    val shieldMult = if (enemy.shieldTimer > 0) 0.3f else 1f
                    enemy.hp -= mapAdjustedDamage(enemy, 80f * shieldMult * randomizerPowerDamageMult)
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
                SoundManager.play(SfxType.POWER_LIGHTNING)
                powerCooldowns[type] = getRandomizedPowerCooldown(type)
            }
        }
        checkAchievement("use_power")
        powersUsedThisRun.add(type)
        if (powersUsedThisRun.size >= PowerType.entries.size) checkAchievement("all_powers")
        return true
    } }

    fun getPowerCooldown(type: PowerType): Float = powerCooldowns.getOrDefault(type, 0f)

    /** Player dash — teleport to target and deal AoE damage along the path */
    fun playerDash(targetX: Float, targetY: Float): Boolean { synchronized(lock) {
        return combatSystem.playerDash(targetX, targetY)
        if (dashCooldown > 0 || player.hp <= 0) return false
        val dx = targetX - player.x
        val dy = targetY - player.y
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if (dist < 30f) return false

        val actualDist = dist.coerceAtMost(dashRange)
        val nx = dx / dist
        val ny = dy / dist

        // Save trail start
        dashTrailX = player.x
        dashTrailY = player.y

        // Damage enemies along the dash path
        val dashEndX = player.x + nx * actualDist
        val dashEndY = player.y + ny * actualDist
        enemies.filter { it.hp > 0 }.forEach { enemy ->
            // Point-to-segment distance
            val ex = enemy.x - player.x; val ey = enemy.y - player.y
            val lx = dashEndX - player.x; val ly = dashEndY - player.y
            val lenSq = lx * lx + ly * ly
            val t = if (lenSq < 0.01f) 0f else ((ex * lx + ey * ly) / lenSq).coerceIn(0f, 1f)
            val px = player.x + t * lx; val py = player.y + t * ly
            val dsx = enemy.x - px; val dsy = enemy.y - py
            if (dsx * dsx + dsy * dsy < 60f * 60f) {
                val shieldMult = if (enemy.shieldTimer > 0) 0.3f else 1f
                val actualDashDmg = mapAdjustedDamage(enemy, dashDamage * shieldMult)
                enemy.hp -= actualDashDmg
                enemy.hitFlash = 0.3f
                floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size,
                    "DASH! -${actualDashDmg.toInt()}", 0xFF00E5FF.toInt(), 0.8f, 24f))
            }
        }

        // Move player to end position, clamped to playable area
        val groundTop = screenH * 0.12f + player.size
        var clampedEndX = dashEndX.coerceIn(player.size, screenW - player.size)
        var clampedEndY = dashEndY.coerceIn(groundTop, screenH - player.size)
        // If dash lands in river, snap back to start
        if (isPointOnRiver(clampedEndX, clampedEndY)) {
            clampedEndX = dashTrailX
            clampedEndY = dashTrailY
        }
        player.x = clampedEndX
        player.y = clampedEndY
        player.targetX = clampedEndX
        player.targetY = clampedEndY

        // Trail particles
        repeat(12) { i ->
            val t = i / 12f
            particles.add(Particle(
                dashTrailX + (dashEndX - dashTrailX) * t,
                dashTrailY + (dashEndY - dashTrailY) * t,
                (Math.random().toFloat() - 0.5f) * 60f,
                (Math.random().toFloat() - 0.5f) * 60f,
                0.6f, 0xFF00E5FF.toInt(), 6f))
        }
        shakeTimer = 0.1f; shakeIntensity = 5f
        isDashing = true
        dashCooldown = dashCooldownMax
        SoundManager.play(SfxType.PLAYER_ATTACK)
        return true
    } }

    private fun checkAchievement(id: String) {
        metaProgression.checkAchievement(id)
        return
        val ach = achievements.find { it.id == id } ?: return
        if (ach.unlocked) return
        ach.unlocked = true
        prefs.edit().putBoolean("ach_${id}", true).apply()
        newAchievement = ach
        achievementBannerTimer = 3f
        SoundManager.play(SfxType.ACHIEVEMENT)
    }

    private fun checkUpgradeAll() {
        metaProgression.checkUpgradeAll()
        return
        if (playerDamageLevel >= 2 && playerSpeedLevel >= 2 && playerHpLevel >= 2 && baseHpLevel >= 2) {
            checkAchievement("upgrade_all")
        }
    }

    /** Save game state to preferences for continuing later */
    fun saveGame() {
        synchronized(lock) {
            runPersistence.saveSnapshot(buildRunSnapshot())
            val ed = prefs.edit()
            ed.putInt("save_wave", wave)
            ed.putInt("save_gold", gold)
            ed.putInt("save_score", score)
            ed.putInt("save_totalKills", totalKills)
            ed.putFloat("save_baseHp", baseHp)
            ed.putFloat("save_maxBaseHp", maxBaseHp)
            ed.putFloat("save_playerHp", player.hp)
            ed.putFloat("save_playerMaxHp", player.maxHp)
            ed.putFloat("save_playerDmg", player.attackDamage)
            ed.putFloat("save_playerSpd", player.speed)
            ed.putInt("save_difficulty", difficulty)
            ed.putInt("save_dmgLvl", playerDamageLevel)
            ed.putInt("save_spdLvl", playerSpeedLevel)
            ed.putInt("save_hpLvl", playerHpLevel)
            ed.putInt("save_baseLvl", baseHpLevel)
            ed.putInt("save_diamonds", diamondsEarnedThisRun)
            ed.putString("save_map", mapType.name)
            ed.putBoolean("save_is_randomizer", isRandomizerMode)
            ed.putLong("save_randomizer_seed", randomizerSeed)
            ed.putInt("save_randomizer_start_gold", randomizerStartGold)
            ed.putFloat("save_randomizer_power_damage_mult", randomizerPowerDamageMult)
            ed.putString("save_randomizer_tower_costs", encodeRandomizerTowerCosts())
            ed.putString("save_randomizer_power_cooldowns", encodeRandomizerPowerCooldowns())
            ed.putFloat("save_enemy_hp_mult", enemyHpMult)
            ed.putFloat("save_enemy_dmg_mult", enemyDmgMult)
            ed.putFloat("save_enemy_speed_mult", enemySpeedMult)
            ed.putFloat("save_gold_mult", goldMult)
            ed.putFloat("save_spawn_rate_mult", spawnRateMult)
            ed.putString("save_towers", encodeSavedTowers())
            ed.putBoolean("has_save", true)
            ed.apply()
        }
    }

    /** Load saved game state */
    fun loadGame(): Boolean {
        return synchronized(lock) {
            if (!prefs.getBoolean("has_save", false)) return@synchronized false

            val snapshot = runPersistence.loadSnapshot()
            if (snapshot != null) {
                val restored = runCatching { restoreRunSnapshot(snapshot) }.isSuccess
                if (restored) {
                    return@synchronized true
                }
            }

            applyMapType(
                runCatching {
                    MapType.valueOf(prefs.getString("save_map", MapType.CLASSIC.name) ?: MapType.CLASSIC.name)
                }.getOrDefault(MapType.CLASSIC)
            )
            isRandomizerMode = prefs.getBoolean("save_is_randomizer", difficulty == 5)
            randomizerSeed = prefs.getLong("save_randomizer_seed", 0L)
            randomizerStartGold = prefs.getInt("save_randomizer_start_gold", 50)
            randomizerPowerDamageMult = prefs.getFloat("save_randomizer_power_damage_mult", 1f)
            randomizerTowerCosts = decodeRandomizerTowerCosts(prefs.getString("save_randomizer_tower_costs", "") ?: "")
            randomizerPowerCooldowns = decodeRandomizerPowerCooldowns(prefs.getString("save_randomizer_power_cooldowns", "") ?: "")
            enemyHpMult = prefs.getFloat("save_enemy_hp_mult", enemyHpMult)
            enemyDmgMult = prefs.getFloat("save_enemy_dmg_mult", enemyDmgMult)
            enemySpeedMult = prefs.getFloat("save_enemy_speed_mult", enemySpeedMult)
            goldMult = prefs.getFloat("save_gold_mult", goldMult)
            spawnRateMult = prefs.getFloat("save_spawn_rate_mult", spawnRateMult)
            wave = prefs.getInt("save_wave", 0)
            gold = prefs.getInt("save_gold", 50)
            score = prefs.getInt("save_score", 0)
            totalKills = prefs.getInt("save_totalKills", 0)
            baseHp = prefs.getFloat("save_baseHp", 100f)
            maxBaseHp = prefs.getFloat("save_maxBaseHp", 100f)
            player.hp = prefs.getFloat("save_playerHp", 100f)
            player.maxHp = prefs.getFloat("save_playerMaxHp", 100f)
            player.attackDamage = prefs.getFloat("save_playerDmg", 10f)
            player.speed = prefs.getFloat("save_playerSpd", 300f)
            playerDamageLevel = prefs.getInt("save_dmgLvl", 1)
            playerSpeedLevel = prefs.getInt("save_spdLvl", 1)
            playerHpLevel = prefs.getInt("save_hpLvl", 1)
            baseHpLevel = prefs.getInt("save_baseLvl", 1)
            diamondsEarnedThisRun = prefs.getInt("save_diamonds", 0)
            towers.clear()
            towers.addAll(decodeSavedTowers(prefs.getString("save_towers", "") ?: ""))
            enemies.clear()
            projectiles.clear()
            floatingTexts.clear()
            particles.clear()
            if (!isRandomizerMode) {
                randomizerSeed = 0L
                randomizerStartGold = 50
                randomizerPowerDamageMult = 1f
                randomizerTowerCosts = emptyMap()
                randomizerPowerCooldowns = emptyMap()
            }
            waveInProgress = false
            waveTimer = 4f
            nextWavePreview = null
            generateNextWavePreview()
            true
        }
    }

    /** Clear saved game */
    fun clearSave() {
        prefs.edit()
            .putBoolean("has_save", false)
            .apply()
        runPersistence.clearSnapshot()
    }

    fun hasSave(): Boolean = prefs.getBoolean("has_save", false)

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
        waveTimer = 4f
        playerDamageLevel = 1
        playerSpeedLevel = 1
        playerHpLevel = 1
        baseHpLevel = 1
        comboCount = 0
        comboTimer = 0f
        comboMultiplier = 1f
        freezeTimer = 0f
        shakeTimer = 0f
        powerCooldowns.clear()
        bossPool.clear()
        currentBoss = null
        bossRushWave = 0
        isNewBossRushRecord = false
        newAchievement = null
        achievementBannerTimer = 0f
        showWaveBanner = false
        diamondsEarnedThisRun = 0
        bossesKilledThisRun = 0
        repairsThisRun = 0
        powersUsedThisRun.clear()
        baseHpBeforeWave = 100f
        dashCooldown = 0f
        isDashing = false
        isNight = false
        eliteSpawnedThisWave = false
        desertStormTimer = 12f
        desertStormDuration = 0f
        bestCombo = 0
        isNewHighScore = false
        isNewEndlessRecord = false
        campaignVictory = false
        // Reset new fields
        totalEnemiesThisWave = 0
        enemiesSpawnedThisWave = 0
        currentWaveModifier = WaveModifier.NONE
        isPaused = false
        gameSpeed = 1
        val savedBossRush = isBossRush
        val savedDailyChallenge = isDailyChallenge
        val savedDailyMods = dailyChallengeModifiers
        val savedDailySeed = dailyChallengeSeed
        val savedRandomizer = isRandomizerMode
        isDailyChallenge = false
        dailyChallengeModifiers = emptyList()
        dailyChallengeSeed = 0L
        isBossRush = false
        isRandomizerMode = false
        randomizerSeed = 0L
        randomizerTowerCosts = emptyMap()
        randomizerPowerCooldowns = emptyMap()
        randomizerPowerDamageMult = 1f
        randomizerStartGold = 50
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
        if (savedDailyChallenge) {
            isDailyChallenge = true
            dailyChallengeModifiers = savedDailyMods
            dailyChallengeSeed = savedDailySeed
        }
        if (savedBossRush) {
            isBossRush = true
        }
        if (savedRandomizer) {
            setupRandomizer()
        }
        init(screenW, screenH)
    } }
}
