package com.example.myapp.game

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
    BOSS_RALLY("Rally", "\uD83D\uDCEF", "All enemies move faster"),
    SHIELDED("Shielded", "\uD83D\uDD36", "Enemies start with temporary shields"),
    BERSERKER("Berserker Wave", "\uD83E\uDDB6", "All enemies speed up below 50% HP"),
    SPLIT("Splitting", "\uD83E\uDDA0", "Killed enemies split into 2 mini versions")
}

/** Different map layouts */
enum class MapType(val displayName: String, val emoji: String) {
    CLASSIC("Classic", "\uD83D\uDDFA\uFE0F"),
    VALLEY("Valley", "\uD83C\uDFD4\uFE0F"),
    CROSSROADS("Crossroads", "\u271A"),
    DESERT("Desert", "\uD83C\uDFDC\uFE0F"),
    SNOW("Snow", "\u2744\uFE0F"),
    LAVA("Lava", "\uD83C\uDF0B"),
    ENCHANTED("Enchanted", "\u2728"),
    VOLCANO("Volcano", "\uD83C\uDF0B")
}

/** Trap types placeable on enemy paths */
enum class TrapType(val displayName: String, val emoji: String, val cost: Int) {
    SPIKE("Spikes", "\uD83D\uDDE1\uFE0F", 30),
    TAR("Tar Pit", "\uD83D\uDFE4", 40),
    MINE("Landmine", "\uD83D\uDCA3", 60)
}

/** A trap placed on an enemy path */
data class Trap(
    val x: Float,
    val y: Float,
    val type: TrapType,
    var uses: Int,       // how many more times it can trigger
    val maxUses: Int,
    val size: Float = 25f,
    var cooldown: Float = 0f  // time until next trigger
) {
    fun distanceTo(ex: Float, ey: Float): Float {
        val dx = x - ex; val dy = y - ey
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
    fun isSpent(): Boolean = uses <= 0
}

/** Supply drop that spawns mid-wave and gives gold when tapped */
data class SupplyDrop(
    val x: Float,
    val y: Float,
    val goldAmount: Int,
    var lifetime: Float = 8f,
    val size: Float = 28f
) {
    fun distanceTo(tx: Float, ty: Float): Float {
        val dx = x - tx; val dy = y - ty
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
    fun isExpired(): Boolean = lifetime <= 0f
}

/** Bounty objective for endless/boss rush modes */
data class Bounty(
    val id: String,
    val description: String,
    val emoji: String,
    var target: Int,
    var progress: Int = 0,
    var completed: Boolean = false,
    val rewardDiamonds: Int = 0,
    val rewardGold: Int = 0
)

/** Endless mode milestone buffs — player picks one every 25 waves */
enum class EndlessBuff(val label: String, val emoji: String, val description: String) {
    TOWER_DMG_UP("\uD83D\uDDE1\uFE0F Tower Power", "\uD83D\uDDE1\uFE0F", "+20% tower damage"),
    GOLD_BONUS("\uD83D\uDCB0 Gold Rush", "\uD83D\uDCB0", "+30% gold from kills"),
    BASE_FORTIFY("\uD83C\uDFF0 Fortify", "\uD83C\uDFF0", "+50 max base HP"),
    POWER_CDR("\u26A1 Quick Cast", "\u26A1", "-25% power cooldowns"),
    CRIT_UP("\uD83C\uDFAF Precision", "\uD83C\uDFAF", "+8% crit chance"),
    HEAL_PULSE("\uD83D\uDC9A Regen Pulse", "\uD83D\uDC9A", "Base heals 2 HP/sec"),
    TOWER_RANGE("\uD83D\uDD2D Tower Range", "\uD83D\uDD2D", "+15% tower range"),
    ENEMY_SLOW("\u2744\uFE0F Chill", "\u2744\uFE0F", "Enemies 10% slower"),
    DOUBLE_INTEREST("\uD83C\uDFE6 Investor", "\uD83C\uDFE6", "Double gold interest"),
    COMBO_BOOST("\uD83D\uDD17 Combo Master", "\uD83D\uDD17", "Combo multiplier +50%")
}

/** A path with waypoints that enemies follow from spawn to base */
data class GamePath(val waypoints: List<GamePoint>) {
    val spawnPoint: GamePoint get() = waypoints.first()
}

/** Placeable blockade that enemies must destroy before passing */
data class Blockade(
    val x: Float,
    val y: Float,
    var hp: Float,
    val maxHp: Float,
    val size: Float = 30f
) {
    fun distanceTo(ex: Float, ey: Float): Float {
        val dx = x - ex
        val dy = y - ey
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }
    fun isDead(): Boolean = hp <= 0
}

class GameEngine(val prefs: GamePreferences, val audio: GameAudio = SilentAudio) {

    val lock = Any()

    // Difficulty: 0=easy, 1=normal, 2=hard
    var difficulty: Int = 1
    // Difficulty multipliers
    private var enemyHpMult: Float = 1f
    private var enemyDmgMult: Float = 1f
    private var enemySpeedMult: Float = 1f
    private var goldMult: Float = 1f
    private var spawnRateMult: Float = 1f

    val player = Player()
    val enemies = mutableListOf<Enemy>()
    val towers = mutableListOf<Tower>()
    val projectiles = mutableListOf<Projectile>()
    val floatingTexts = mutableListOf<FloatingText>()
    val particles = mutableListOf<Particle>()
    val blockades = mutableListOf<Blockade>()
    val traps = mutableListOf<Trap>()
    val supplyDrops = mutableListOf<SupplyDrop>()
    var supplyDropTimer: Float = 0f

    // Bounty board (endless / boss rush)
    val activeBounties = mutableListOf<Bounty>()
    var bountiesGenerated: Boolean = false

    // Volcano eruption
    var volcanoEruptionTimer: Float = 0f
    var volcanoErupting: Boolean = false
    var volcanoEruptDuration: Float = 0f
    var volcanoCenterX: Float = 0f
    var volcanoCenterY: Float = 0f
    var gold: Int = 50
    var wave: Int = 0
    var baseHp: Float = 100f
    var maxBaseHp: Float = 100f
    var baseX: Float = 0f
    var baseY: Float = 0f
    var screenW: Float = 1080f
    var screenH: Float = 1920f

    var score: Int = 0
    var scoreFromKills: Int = 0
    var scoreFromCombos: Int = 0
    var totalKills: Int = 0
    var totalGoldEarned: Int = 0
    var gameOver: Boolean = false
    var waveInProgress: Boolean = false
    var enemiesRemaining: Int = 0
    var waveDelay: Float = 5f
    var waveTimer: Float = 4f
    var autoWave: Boolean = false
    var playTimeSeconds: Float = 0f  // session play time

    // Wave progress tracking
    var totalEnemiesThisWave: Int = 0
    var enemiesSpawnedThisWave: Int = 0

    // Wave modifier
    var currentWaveModifier: WaveModifier = WaveModifier.NONE

    // Map type
    var mapType: MapType = MapType.CLASSIC

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
    // Boss spawn flash
    var bossFlashTimer: Float = 0f
    // Haptic feedback event: 0=none, 1=light, 2=heavy
    @Volatile var hapticPending: Int = 0

    // High score
    var highScore: Int = 0
    var highWave: Int = 0
    var highScoreEasy: Int = 0
    var highScoreNormal: Int = 0
    var highScoreHard: Int = 0

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
        Achievement("all_tower_types", "Arsenal", "Place all tower types", "\uD83C\uDFAF"),
        Achievement("use_power", "Sorcerer", "Use a power for the first time", "\u2728"),
        Achievement("max_tower", "Master Builder", "Upgrade a tower to level 5", "\u2B06\uFE0F"),
        Achievement("rich", "Rich", "Have 500 gold at once", "\uD83D\uDCB0"),
        Achievement("rich_1000", "Millionaire", "Have 1000 gold at once", "\uD83E\uDD11"),
        Achievement("score_1000", "Score Chaser", "Reach 1000 score", "\uD83D\uDCCA"),
        Achievement("diamond_10", "Diamond Hoarder", "Earn 10 diamonds in a run", "\uD83D\uDC8E"),
        Achievement("repaired_3", "Mechanic", "Repair the base 3 times in a run", "\uD83D\uDD27"),
        Achievement("upgrade_all", "Well Rounded", "Buy all 4 player upgrades", "\uD83C\uDF96\uFE0F"),
        Achievement("endless_10", "Endurance", "Reach wave 10 in endless mode", "\u267E\uFE0F"),
        Achievement("kills_1000", "Genocide", "Kill 1000 enemies in one run", "\uD83D\uDC7B"),
        Achievement("wave_100", "Centurion", "Reach wave 100", "\u2694\uFE0F"),
        Achievement("no_damage", "Untouchable", "Complete a wave without base taking damage", "\uD83D\uDEE1\uFE0F"),
        Achievement("speed_demon", "Speed Demon", "Beat wave 10 on 3x speed", "\uD83D\uDCA8"),
        Achievement("10_bosses", "Boss Legend", "Kill 10 bosses in one run", "\uD83D\uDC32"),
        Achievement("diamond_50", "Diamond Mine", "Earn 50 diamonds in one run", "\uD83D\uDC8E"),
        Achievement("gold_hoarder", "Gold Hoarder", "Have 2000 gold at once", "\uD83C\uDFE6"),
        Achievement("all_powers", "Elementalist", "Use all 4 powers in one run", "\uD83C\uDF0A"),
        Achievement("survivor_1hp", "Last Stand", "Win a wave with base at 1 HP", "\u2764\uFE0F"),
        Achievement("campaign_5", "Campaigner", "Complete 5 campaign levels", "\uD83D\uDDFA\uFE0F"),
        Achievement("campaign_10", "Strategist", "Complete 10 campaign levels", "\uD83C\uDFC5"),
        Achievement("campaign_all", "Conqueror", "Complete all campaign levels", "\uD83D\uDC51"),
        Achievement("campaign_no_damage", "Flawless", "Beat a campaign level without base damage", "\uD83D\uDEE1\uFE0F"),
        Achievement("campaign_3star", "Perfectionist", "Get 3 stars on 5 campaign levels", "\u2B50"),
        // --- New feature achievements ---
        Achievement("trap_first", "Trapper", "Place your first trap", "\uD83E\uDEE4"),
        Achievement("trap_10", "Minefield", "Place 10 traps in one run", "\uD83D\uDCA3"),
        Achievement("mine_triple", "Triple Threat", "Kill 3 enemies with one mine", "\uD83D\uDCA5"),
        Achievement("bounty_first", "Bounty Hunter", "Complete your first bounty", "\uD83C\uDFAF"),
        Achievement("bounty_all", "Bounty King", "Complete all 3 bounties in one run", "\uD83D\uDC51"),
        Achievement("volcano_win", "Volcanic Victory", "Reach wave 15 on Volcano map", "\uD83C\uDF0B"),
        Achievement("combo_30", "Unstoppable", "Get a 30x combo", "\uD83D\uDD25"),
        Achievement("combo_50", "Godlike", "Get a 50x combo", "\u26A1"),
        Achievement("streak_no_tower", "Lone Wolf", "Reach wave 5 with no towers placed", "\uD83D\uDC3A"),
        Achievement("all_maps", "Cartographer", "Play on all 8 maps", "\uD83C\uDF0D"),
        Achievement("boss_rush_5", "Gauntlet", "Defeat 5 bosses in Boss Rush", "\uD83D\uDDE1\uFE0F"),
        Achievement("randomizer_win", "Chaos Master", "Reach wave 15 in Randomizer", "\uD83C\uDFB2"),
        Achievement("ability_all", "Tactician", "Use abilities on 5 tower types in one run", "\u2728"),
        Achievement("prestige_first", "Reborn", "Prestige for the first time", "\uD83D\uDC51"),
        Achievement("score_5000", "High Roller", "Reach 5000 score", "\uD83C\uDFC5"),
        Achievement("score_10000", "Legendary Score", "Reach 10000 score", "\uD83E\uDD47")
    )
    // Track which powers were used this run for achievement
    val powersUsedThisRun = mutableSetOf<PowerType>()
    val abilityTowerTypesUsed = mutableSetOf<TowerType>()
    var trapsPlacedThisRun: Int = 0
    var baseHpBeforeWave: Float = 100f
    var newAchievement: Achievement? = null
    var achievementBannerTimer: Float = 0f

    // --- Tower mastery (persistent per-type kill tracking) ---
    val towerMasteryKills = mutableMapOf<TowerType, Int>()
    fun masteryLevel(type: TowerType): Int {
        val k = towerMasteryKills[type] ?: 0
        return when {
            k >= 1500 -> 5  // Master
            k >= 750 -> 4   // Diamond
            k >= 300 -> 3   // Gold
            k >= 100 -> 2   // Silver
            k >= 25 -> 1    // Bronze
            else -> 0
        }
    }
    fun masteryDamageBonus(type: TowerType): Float = masteryLevel(type) * 0.03f

    // --- Critical hits ---
    val critChance: Float get() = 0.12f + endlessBuffCritChance
    val critMultiplier: Float get() = 2.0f

    // --- Day/Night cycle ---
    var isNight: Boolean = false
    val nightHpMultiplier: Float get() = if (isNight) 1.20f else 1f
    val nightRangeMultiplier: Float get() = if (isNight) 0.90f else 1f
    // --- Weather ---
    var weatherType: Int = 0  // 0=clear, 1=rain, 2=snow
    var weatherIntensity: Float = 0f  // 0..1

    // --- Player dash ---
    var dashCooldown: Float = 0f
    val dashCooldownMax: Float = 8f
    val dashDamage: Float get() = player.attackDamage * 2.5f
    val dashRange: Float = 200f
    var isDashing: Boolean = false
    var dashTrailX: Float = 0f
    var dashTrailY: Float = 0f

    // --- Gold interest ---
    var interestRate: Float = 0.05f  // 5% between waves

    // --- Elite enemies ---
    var eliteSpawnedThisWave: Boolean = false

    // Upgrade levels
    var playerDamageLevel = 1
    var playerSpeedLevel = 1
    var playerHpLevel = 1
    var baseHpLevel = 1

    private val spawnPoints = mutableListOf<Pair<Float, Float>>()
    val paths = mutableListOf<GamePath>()
    val riverWaypoints = mutableListOf<GamePoint>()  // kept empty — river removed

    // Boss pool
    private val bossPool = mutableListOf<BossType>()
    var currentBoss: BossType? = null
        private set

    var isEndlessMode: Boolean = false
    var isBossRush: Boolean = false
    var bossRushWave: Int = 0
    var bossRushHighWave: Int = 0
    var isNewBossRushRecord: Boolean = false
    var bossIntervalOverride: Int = -1
    val bossInterval: Int get() = if (bossIntervalOverride > 0) bossIntervalOverride else if (isBossRush) 1 else if (campaignLevel?.id == 13 || campaignLevel?.id == 26) 3 else 5
    var nextWavePreview: WavePreview? = null
    var diamondsEarnedThisRun: Int = 0
    var bossesKilledThisRun: Int = 0
    var repairsThisRun: Int = 0
    var endlessHighWave: Int = 0
    var isNewHighScore: Boolean = false
    var isNewEndlessRecord: Boolean = false
    val skillTree = SkillTree(prefs)

    // Campaign
    var campaignLevel: CampaignLevel? = null
    var campaignVictory: Boolean = false
    var campaignStars: Int = 0

    val totalCampaignLevels: Int get() = CampaignData.levels.size
    val totalCampaignStars: Int get() = CampaignData.levels.sumOf { prefs.getInt("campaign_${it.id}_stars", 0) }

    // Daily challenge
    var isDailyChallenge: Boolean = false
    var dailyChallengeModifiers: List<WaveModifier> = emptyList()
    var dailyChallengeSeed: Long = 0L

    // Randomizer mode
    var isRandomizerMode: Boolean = false
    var isSurvivalMode: Boolean = false
    var isArenaMode: Boolean = false
    var isIronmanMode: Boolean = false
    var isLoadoutMode: Boolean = false
    var loadoutTowers: Set<TowerType> = emptySet()
    var randomizerSeed: Long = 0L
    /** Per-run randomized tower cost overrides (null = default) */
    var randomizerTowerCosts: Map<TowerType, Int> = emptyMap()
    /** Per-run randomized power cooldown overrides */
    var randomizerPowerCooldowns: Map<PowerType, Float> = emptyMap()
    /** Per-run randomized power damage multiplier */
    var randomizerPowerDamageMult: Float = 1f
    /** Per-run randomized starting gold */
    var randomizerStartGold: Int = 50
    /** Power identity swap — maps button press to different power effect */
    var randomizerPowerSwap: Map<PowerType, PowerType> = emptyMap()

    // --- Endless milestone buff system ---
    /** Buffs the player has picked at milestones */
    val endlessBuffs = mutableListOf<EndlessBuff>()
    /** When > 0, game is paused waiting for player to pick a buff */
    var endlessMilestoneChoices: List<EndlessBuff> = emptyList()
    var endlessMilestonePending: Boolean = false
    /** Extra tower damage multiplier from buffs */
    var endlessBuffTowerDmg: Float = 1f
    /** Extra gold multiplier from buffs */
    var endlessBuffGold: Float = 1f
    /** Extra base HP from buffs */
    var endlessBuffBaseHp: Float = 0f
    /** Power cooldown reduction from buffs (0..1) */
    var endlessBuffPowerCdr: Float = 0f
    /** Extra crit chance from buffs */
    var endlessBuffCritChance: Float = 0f
    /** Tower range multiplier from buffs */
    var endlessBuffTowerRange: Float = 1f

    fun applyDifficulty(level: Int) {
        difficulty = level
        isEndlessMode = level == 3
        isBossRush = level == 4
        isRandomizerMode = level == 5
        isSurvivalMode = level == 6
        isArenaMode = level == 7
        isIronmanMode = level == 8
        isLoadoutMode = level == 9
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
            6 -> { // Survival — scarce resources, starting gold 200, no interest
                enemyHpMult = 1.2f; enemyDmgMult = 1.1f; enemySpeedMult = 1f
                goldMult = 0.5f; spawnRateMult = 1f
                gold = 200; interestRate = 0f
            }
            7 -> { // Arena — fast waves, boss every 3, high speed
                enemyHpMult = 0.9f; enemyDmgMult = 1.2f; enemySpeedMult = 1.3f
                goldMult = 1.2f; spawnRateMult = 1.4f
                gold = 80; waveDelay = 2f; bossIntervalOverride = 3
            }
            8 -> { // Ironman — 1 HP base, hard scaling
                enemyHpMult = 1.3f; enemyDmgMult = 1.5f; enemySpeedMult = 1.1f
                goldMult = 1f; spawnRateMult = 1.1f
                gold = 60; maxBaseHp = 1f; baseHp = 1f
            }
            9 -> { // Loadout — normal stats, tower restriction via loadoutTowers
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
        mapType = level.mapType
        // Glass cannon: reduced base HP
        if (level.id == 28) { maxBaseHp = 50f; baseHp = 50f }
    }

    fun isTowerAllowed(type: TowerType): Boolean {
        if (isLoadoutMode && loadoutTowers.isNotEmpty()) return type in loadoutTowers
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

        // Load tower mastery from prefs
        for (t in TowerType.entries) {
            towerMasteryKills[t] = prefs.getInt("mastery_${t.name}", 0)
        }

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

        // Volcano eruption timer init
        if (mapType == MapType.VOLCANO) {
            volcanoEruptionTimer = 20f
        }

        // Generate bounties for applicable modes
        generateBounties()

        highScore = prefs.getInt("highScore", 0)
        highWave = prefs.getInt("highWave", 0)
        endlessHighWave = prefs.getInt("endlessHighWave", 0)
        highScoreEasy = prefs.getInt("highScore_0", 0)
        highScoreNormal = prefs.getInt("highScore_1", 0)
        highScoreHard = prefs.getInt("highScore_2", 0)
        achievements.forEach { a ->
            a.unlocked = prefs.getBoolean("ach_${a.id}", false)
        }
        
        generateNextWavePreview()
    }

    /** Build paths based on the selected map type — randomized each run */
    private fun generatePaths(w: Float, h: Float) {
        paths.clear()
        spawnPoints.clear()
        val bx = baseX
        val by = baseY
        val rng = java.util.Random()

        when (mapType) {
            MapType.CLASSIC -> generateRandomizedClassicPaths(w, h, bx, by, rng)
            MapType.VALLEY -> generateRandomizedValleyPaths(w, h, bx, by, rng)
            MapType.CROSSROADS -> generateRandomizedCrossroadsPaths(w, h, bx, by, rng)
            MapType.DESERT -> generateRandomizedDesertPaths(w, h, bx, by, rng)
            MapType.SNOW -> generateRandomizedSnowPaths(w, h, bx, by, rng)
            MapType.LAVA -> generateRandomizedLavaPaths(w, h, bx, by, rng)
            MapType.ENCHANTED -> generateRandomizedEnchantedPaths(w, h, bx, by, rng)
            MapType.VOLCANO -> generateRandomizedVolcanoPaths(w, h, bx, by, rng)
        }

        paths.forEach { path ->
            val sp = path.spawnPoint
            spawnPoints.add(Pair(sp.x, sp.y))
        }
    }

    /** Check if a point is on/near the river — river removed, always returns false */
    fun isPointOnRiver(x: Float, y: Float): Boolean = false

    /** Helper: jitter a base value by +/- range */
    private fun jitter(rng: java.util.Random, base: Float, range: Float): Float =
        base + (rng.nextFloat() * 2f - 1f) * range

    private fun generateRandomizedClassicPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.04f // jitter factor relative to screen
        // Left path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.08f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.28f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.26f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.16f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(jitter(rng, w * 0.36f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))
        // Center path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.46f, w * j), jitter(rng, h * 0.09f, h * j)),
            GamePoint(jitter(rng, w * 0.58f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.56f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.44f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
        // Right path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.92f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.72f, w * j), jitter(rng, h * 0.24f, h * j)),
            GamePoint(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.74f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.84f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(jitter(rng, w * 0.64f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateRandomizedValleyPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.05f
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.08f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.20f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.34f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.48f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.62f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.74f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.35f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.56f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateRandomizedCrossroadsPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.03f
        val cx = jitter(rng, w * 0.5f, w * 0.04f)
        val cy = jitter(rng, h * 0.45f, h * 0.03f)
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(cx, cy),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(-40f, jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.12f, w * j), jitter(rng, h * 0.38f, h * j)),
            GamePoint(cx, cy),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(w + 40f, jitter(rng, h * 0.40f, h * j)),
            GamePoint(jitter(rng, w * 0.88f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(cx, cy),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.90f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.15f, h * j)),
            GamePoint(cx, cy),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateRandomizedDesertPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.04f
        // Two wide sweeping paths (desert canyon feel)
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.20f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.25f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.58f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.80f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.45f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.62f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.75f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateRandomizedSnowPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.035f
        // Three narrow winding paths (icy mountain passes)
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.15f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.10f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.48f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.66f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.28f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.46f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.64f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.85f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.90f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.32f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.80f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateRandomizedLavaPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.04f
        // Two dangerous paths winding through lava fields
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.10f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.08f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.18f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.44f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.58f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.90f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.22f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.36f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.64f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        // Third narrow central path
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.14f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.34f, h * j)),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.54f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.72f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateRandomizedEnchantedPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.045f
        // Four spiraling fairy paths converging on base
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.05f, w * j), jitter(rng, h * 0.05f, h * j)),
            GamePoint(jitter(rng, w * 0.30f, w * j), jitter(rng, h * 0.15f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.35f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.95f, w * j), jitter(rng, h * 0.05f, h * j)),
            GamePoint(jitter(rng, w * 0.70f, w * j), jitter(rng, h * 0.15f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.35f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.68f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.40f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.60f, w * j), jitter(rng, h * 0.25f, h * j)),
            GamePoint(jitter(rng, w * 0.45f, w * j), jitter(rng, h * 0.42f, h * j)),
            GamePoint(jitter(rng, w * 0.55f, w * j), jitter(rng, h * 0.60f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.76f, h * j)),
            GamePoint(bx, by)
        )))
    }

    private fun generateRandomizedVolcanoPaths(w: Float, h: Float, bx: Float, by: Float, rng: java.util.Random) {
        val j = 0.04f
        volcanoCenterX = w * 0.50f
        volcanoCenterY = h * 0.40f
        // Three paths that curve around a central volcano
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.10f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.20f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.15f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.25f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.90f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.12f, h * j)),
            GamePoint(jitter(rng, w * 0.80f, w * j), jitter(rng, h * 0.30f, h * j)),
            GamePoint(jitter(rng, w * 0.85f, w * j), jitter(rng, h * 0.50f, h * j)),
            GamePoint(jitter(rng, w * 0.75f, w * j), jitter(rng, h * 0.65f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.78f, h * j)),
            GamePoint(bx, by)
        )))
        paths.add(GamePath(listOf(
            GamePoint(jitter(rng, w * 0.50f, w * j), -40f),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.10f, h * j)),
            GamePoint(jitter(rng, w * 0.35f, w * j), jitter(rng, h * 0.22f, h * j)),
            GamePoint(jitter(rng, w * 0.65f, w * j), jitter(rng, h * 0.55f, h * j)),
            GamePoint(jitter(rng, w * 0.50f, w * j), jitter(rng, h * 0.70f, h * j)),
            GamePoint(bx, by)
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
        val modCount = 3 + rng.nextInt(3) // 3-5 modifiers
        dailyChallengeModifiers = List(modCount) { allMods[rng.nextInt(allMods.size)] }
        // Daily challenge uses fixed difficulty
        applyDifficulty(1)
        // Deterministic daily map
        mapType = MapType.entries[rng.nextInt(MapType.entries.size)]
        // Slightly varied starting gold (40-80)
        gold = 40 + rng.nextInt(41)
        // Random enemy scaling twist
        val twistMult = 0.85f + rng.nextFloat() * 0.4f  // 0.85 - 1.25
        enemyHpMult *= twistMult
        enemySpeedMult *= (0.9f + rng.nextFloat() * 0.2f)
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

        // Scramble power identities — each button may trigger a different power's effect
        val shuffledPowers = PowerType.entries.shuffled(rng)
        randomizerPowerSwap = PowerType.entries.zip(shuffledPowers).toMap()

        // Pick a random map
        mapType = MapType.entries[rng.nextInt(MapType.entries.size)]
    }

    /** Get tower cost (respects randomizer overrides) */
    fun getTowerCost(type: TowerType): Int {
        return if (isRandomizerMode) randomizerTowerCosts[type] ?: type.baseCost else type.baseCost
    }

    /** Get power cooldown (respects randomizer overrides and endless CDR buff) */
    fun getRandomizedPowerCooldown(type: PowerType): Float {
        val base = if (isRandomizerMode) randomizerPowerCooldowns[type] ?: type.cooldown else type.cooldown
        return base * (1f - endlessBuffPowerCdr)
    }

    private fun generateNextWavePreview() {
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
                    nextWave >= 18 && Math.random() < 0.06 -> EnemyType.SHAPESHIFTER
                    nextWave >= 15 && Math.random() < 0.07 -> EnemyType.COMMANDER
                    nextWave >= 12 && Math.random() < 0.08 -> EnemyType.BERSERKER
                    nextWave >= 10 && Math.random() < 0.08 -> EnemyType.WISP
                    nextWave >= 9 && Math.random() < 0.08 -> EnemyType.SHADOW
                    nextWave >= 8 && Math.random() < 0.12 -> EnemyType.ARMORED_GOLEM
                    nextWave >= 7 && Math.random() < 0.15 -> EnemyType.DRAGON
                    nextWave >= 5 && Math.random() < 0.18 -> EnemyType.DEMON
                    nextWave >= 4 && Math.random() < 0.18 -> EnemyType.FAST_SKELETON
                    nextWave >= 3 && Math.random() < 0.25 -> EnemyType.ORC
                    nextWave >= 2 && Math.random() < 0.35 -> EnemyType.SKELETON
                    nextWave >= 1 && Math.random() < 0.20 -> EnemyType.BAT
                    nextWave >= 1 && Math.random() < 0.15 -> EnemyType.SLIME
                    nextWave >= 2 && Math.random() < 0.12 -> EnemyType.SPIDER
                    else -> EnemyType.GOBLIN
                }
                enemyCounts[type] = (enemyCounts[type] ?: 0) + 1
            }
            nextWavePreview = WavePreview(false, enemyCounts, modifier = previewMod)
        }
    }

    fun update(dt: Float) { synchronized(lock) {
        if (gameOver || campaignVictory || isPaused) return
        playTimeSeconds += dt

        if (!waveInProgress && enemies.isEmpty()) {
            waveTimer -= dt
            if (autoWave || waveTimer <= 0) startNextWave()
        }

        if (showWaveBanner) {
            waveBannerTimer -= dt
            if (waveBannerTimer <= 0f) showWaveBanner = false
        }

        if (achievementBannerTimer > 0) {
            achievementBannerTimer -= dt
            if (achievementBannerTimer <= 0f) newAchievement = null
        }

        // Supply drops — spawn periodically during waves
        if (waveInProgress) {
            supplyDropTimer -= dt
            if (supplyDropTimer <= 0f) {
                supplyDropTimer = 12f + (Math.random() * 8).toFloat() // every 12-20s
                if (Math.random() < 0.4) { // 40% chance each interval
                    val dropX = 60f + (Math.random() * (screenW - 120f)).toFloat()
                    val dropY = 80f + (Math.random() * (screenH * 0.6f)).toFloat()
                    val amount = (10 + wave * 2 + (Math.random() * wave * 3).toInt()).coerceAtMost(100)
                    supplyDrops.add(SupplyDrop(dropX, dropY, amount))
                }
            }
        }
        // Update supply drop lifetimes
        supplyDrops.forEach { it.lifetime -= dt }
        supplyDrops.removeAll { it.isExpired() }

        if (waveInProgress && enemies.isEmpty() && enemiesRemaining <= 0) {
            waveInProgress = false
            waveTimer = if (isBossRush) 3f else waveDelay
            audio.play(SfxType.WAVE_COMPLETE)
            // Clear volcano eruption between waves
            volcanoErupting = false
            volcanoEruptDuration = 0f
            if (baseHp >= baseHpBeforeWave) {
                checkAchievement("no_damage")
                trackBountyNoDamage()
            }
            trackBountyWave()
            if (baseHp == 1f) checkAchievement("survivor_1hp")
            if (gameSpeed == 3 && wave >= 10) checkAchievement("speed_demon")

            // Gold interest between waves
            val interestMult = if (endlessBuffs.count { it == EndlessBuff.DOUBLE_INTEREST } > 0) 2f else 1f
            val interest = (gold * interestRate * interestMult).toInt()
            if (interest > 0) {
                gold += interest
                totalGoldEarned += interest
                floatingTexts.add(FloatingText(baseX + 60f, baseY - 100f, "+${interest}g interest!", 0xFF81C784.toInt(), 1.5f, 26f))
            }

            val bonus = ((wave * 5 + skillTree.bonusWaveGold()) * goldMult * endlessBuffGold).toInt()
            gold += bonus
            totalGoldEarned += bonus
            floatingTexts.add(FloatingText(baseX, baseY - 80f, "+${bonus}g wave bonus!", 0xFFFFD700.toInt(), 1.5f, 32f))

            // Endless milestone buff trigger every 25 waves
            if (isEndlessMode && wave > 0 && wave % 25 == 0) {
                val available = EndlessBuff.entries.toMutableList()
                available.shuffle()
                endlessMilestoneChoices = available.take(3)
                endlessMilestonePending = true
                // Pause the game until player picks
                isPaused = true
            }

            // Campaign victory check
            val cl = campaignLevel
            if (cl != null && wave >= cl.targetWave) {
                campaignVictory = true
                saveRunHistory("Won")
                audio.play(SfxType.VICTORY)
                val editor = prefs.edit()
                editor.putBoolean("campaign_${cl.id}", true)

                // Star rating: 1⭐ = beat, 2⭐ = good score, 3⭐ = high score
                val stars = when {
                    score >= cl.star3Score -> 3
                    score >= cl.star2Score -> 2
                    else -> 1
                }
                campaignStars = stars
                val prevStars = prefs.getInt("campaign_${cl.id}_stars", 0)
                if (stars > prevStars) {
                    editor.putInt("campaign_${cl.id}_stars", stars)
                }

                // Diamond reward: base + bonus per star
                val starBonus = (stars - 1) * 2  // 0, 2, or 4 bonus diamonds
                val totalDiamonds = cl.diamondReward + starBonus
                skillTree.addDiamonds(totalDiamonds)
                diamondsEarnedThisRun += totalDiamonds

                // Campaign achievements
                val completed = CampaignData.levels.count { prefs.getBoolean("campaign_${it.id}", false) }
                if (completed >= 5) checkAchievement("campaign_5")
                if (completed >= 10) checkAchievement("campaign_10")
                if (completed >= CampaignData.levels.size) checkAchievement("campaign_all")
                if (baseHp >= maxBaseHp) {
                    checkAchievement("campaign_no_damage")
                    editor.putBoolean("campaign_${cl.id}_fullhp", true)
                }
                if (score >= cl.star3Score) {
                    val threeStarLevels = CampaignData.levels.count {
                        prefs.getInt("campaign_${it.id}_stars", 0) >= 3
                    } + if (stars >= 3 && prevStars < 3) 1 else 0
                    if (threeStarLevels >= 5) checkAchievement("campaign_3star")
                }
                editor.apply()
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
        if (bossFlashTimer > 0) bossFlashTimer -= dt

        if (comboTimer > 0) {
            comboTimer -= dt
            if (comboTimer <= 0) {
                if (comboCount >= 5) {
                    audio.play(SfxType.COMBO)
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

        // Endless buff: heal pulse (2 HP/s per stack)
        val healPulseStacks = endlessBuffs.count { it == EndlessBuff.HEAL_PULSE }
        if (healPulseStacks > 0 && baseHp < maxBaseHp && baseHp > 0) {
            baseHp = (baseHp + 2f * healPulseStacks * dt).coerceAtMost(maxBaseHp)
        }
        if (dashCooldown > 0) dashCooldown -= dt
        if (isDashing) isDashing = false

        if (player.hp > 0 && player.canAttack()) {
            val effectivePlayerRange = player.attackRange * (if (currentWaveModifier == WaveModifier.INVISIBLE) 0.7f else 1f)
            val nearest = enemies.filter { it.hp > 0 }.minByOrNull { it.distanceTo(player.x, player.y) }
            if (nearest != null && nearest.distanceTo(player.x, player.y) < effectivePlayerRange) {
                val shieldRed = if (nearest.shieldTimer > 0) 0.3f else 1f
                val actualPlayerDmg = player.attackDamage * shieldRed
                nearest.hp -= actualPlayerDmg
                nearest.hitFlash = 0.15f
                player.attack()
                audio.play(SfxType.PLAYER_ATTACK)
                projectiles.add(Projectile(player.x, player.y, nearest.x, nearest.y,
                    damage = 0f, color = 0xFF42A5F5.toInt()))
                floatingTexts.add(FloatingText(nearest.x, nearest.y - nearest.size,
                    "-${actualPlayerDmg.toInt()}", 0xFF42A5F5.toInt(), 0.8f, 22f))
            }
        }

        val speedMult = if (freezeTimer > 0) 0.2f else 1f
        // Reset ice slow on all enemies each frame, then reapply from ice towers / tar
        enemies.forEach {
            if (it.deepFreezeTimer > 0) { it.deepFreezeTimer -= dt; it.iceSlowFactor = 0.05f }
            else if (it.tarSlowTimer > 0) { it.tarSlowTimer -= dt; it.iceSlowFactor = 0.3f }
            else it.iceSlowFactor = 1f
            if (it.shieldTimer > 0) it.shieldTimer -= dt
        }
        // Ice tower slow aura
        towers.filter { it.type == TowerType.ICE }.forEach { tower ->
            val iceRange = tower.range * (if (currentWaveModifier == WaveModifier.INVISIBLE) 0.7f else 1f)
            enemies.forEach { enemy ->
                if (enemy.distanceTo(tower.x, tower.y) < iceRange) {
                    val slowPower = (0.5f - tower.level * 0.03f - skillTree.iceSlowBonus()).coerceAtLeast(0.05f)
                    enemy.iceSlowFactor = enemy.iceSlowFactor.coerceAtMost(slowPower)
                }
            }
        }
        enemies.forEach { enemy ->
            // Skip enemies in death animation
            if (enemy.deathProcessed) return@forEach
            // Regen from wave modifier
            if (enemy.regenRate > 0 && enemy.hp < enemy.maxHp) {
                enemy.hp = (enemy.hp + enemy.regenRate * dt).coerceAtMost(enemy.maxHp)
            }
            // Burn DoT from Flame tower
            if (enemy.burnTimer > 0 && !enemy.isDead()) {
                enemy.burnTimer -= dt
                enemy.hp -= enemy.burnDps * dt
                // Fire particles while burning
                if (Math.random() < 0.3) {
                    particles.add(Particle(enemy.x + (Math.random().toFloat() - 0.5f) * enemy.size,
                        enemy.y + (Math.random().toFloat() - 0.5f) * enemy.size,
                        (Math.random().toFloat() - 0.5f) * 30f, -40f - Math.random().toFloat() * 30f,
                        0.4f, 0xFFFF5722.toInt(), 3f))
                }
            }
            // Elite ability effects
            if (enemy.isElite && !enemy.isDead()) {
                enemy.eliteAuraTimer -= dt
                when (enemy.eliteAbility) {
                    EliteAbility.SPEED_AURA -> {
                        // Buff nearby non-elite enemies speed every 2s
                        if (enemy.eliteAuraTimer <= 0f) {
                            enemy.eliteAuraTimer = 2f
                            enemies.filter { it != enemy && it.distanceTo(enemy.x, enemy.y) < 150f && !it.isDead() }
                                .forEach { nearby ->
                                    nearby.roarSpeedBoost = 1.4f
                                    nearby.roarBoostTimer = 2.5f
                                }
                            particles.add(Particle(enemy.x, enemy.y, 0f, -20f, 0.8f, EliteAbility.SPEED_AURA.color, 6f))
                        }
                    }
                    EliteAbility.SHIELD -> {
                        // Refresh shield every 8 seconds
                        if (enemy.shieldTimer <= 0f && enemy.eliteAuraTimer <= 0f) {
                            enemy.shieldTimer = 4f
                            enemy.eliteAuraTimer = 8f
                            particles.add(Particle(enemy.x, enemy.y, 0f, -15f, 0.6f, EliteAbility.SHIELD.color, 8f))
                        }
                    }
                    else -> {}
                }
            }
            // Berserker: speed increases as HP drops (1x at full, up to 2.5x near death)
            val berserkerBoost = if (enemy.type == EnemyType.BERSERKER) enemy.berserkerRage
                else if (currentWaveModifier == WaveModifier.BERSERKER && enemy.hp < enemy.maxHp * 0.5f) 1.5f
                else 1f
            // Commander: buff nearby allies speed every 3s
            if (enemy.type == EnemyType.COMMANDER && !enemy.isDead()) {
                enemy.commanderAuraTimer -= dt
                if (enemy.commanderAuraTimer <= 0f) {
                    enemy.commanderAuraTimer = 3f
                    enemies.filter { it != enemy && it.distanceTo(enemy.x, enemy.y) < 180f && !it.isDead() }
                        .forEach { nearby ->
                            nearby.roarSpeedBoost = 1.2f
                            nearby.roarBoostTimer = 3.5f
                        }
                    particles.add(Particle(enemy.x, enemy.y, 0f, -15f, 0.6f, 0xFFFF6F00.toInt(), 8f))
                }
            }
            // Shapeshifter: cycle resistance phase every 5s
            if (enemy.type == EnemyType.SHAPESHIFTER && !enemy.isDead()) {
                enemy.shapeshiftTimer -= dt
                if (enemy.shapeshiftTimer <= 0f) {
                    enemy.shapeshiftTimer = 5f
                    enemy.shapeshiftPhase = (enemy.shapeshiftPhase + 1) % 4
                    particles.add(Particle(enemy.x, enemy.y, 0f, -20f, 0.5f, 0xFF7C4DFF.toInt(), 6f))
                }
            }
            val chargeBoost = if (enemy.isCharging) 3f else 1f
            val roarBoost = enemy.roarSpeedBoost
            val iceSlow = enemy.iceSlowFactor

            // Check for blockade collision — enemy stops and attacks it
            val nearestBlockade = blockades.filter { !it.isDead() }
                .minByOrNull { it.distanceTo(enemy.x, enemy.y) }
            val blockedByBlockade = nearestBlockade != null && nearestBlockade.distanceTo(enemy.x, enemy.y) < nearestBlockade.size + enemy.size
            if (blockedByBlockade) {
                // Attack the blockade instead of moving
                nearestBlockade!!.hp -= enemy.damage * 0.3f * dt  // Constant DPS
                if (nearestBlockade.isDead()) {
                    // Blockade destroyed effect
                    repeat(10) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 80f + (Math.random() * 60f).toFloat()
                        particles.add(Particle(nearestBlockade.x, nearestBlockade.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            0.6f, 0xFF8D6E63.toInt(), 5f))
                    }
                    floatingTexts.add(FloatingText(nearestBlockade.x, nearestBlockade.y - 20f, "DESTROYED!", 0xFFFF5252.toInt(), 1f, 22f))
                }
            } else {
            // Follow waypoints along assigned path
            val path = paths.getOrNull(enemy.pathIndex)
            val target = path?.waypoints?.getOrNull(enemy.waypointIndex)
            if (target != null) {
                val dx = target.x - enemy.x
                val dy = target.y - enemy.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist > enemy.size * 0.5f) {
                    enemy.x += (dx / dist) * enemy.speed * speedMult * chargeBoost * roarBoost * iceSlow * berserkerBoost * dt
                    enemy.y += (dy / dist) * enemy.speed * speedMult * chargeBoost * roarBoost * iceSlow * berserkerBoost * dt
                } else {
                    enemy.waypointIndex++
                }
            } else {
                // Past last waypoint or no path — head to base
                val dx = baseX - enemy.x
                val dy = baseY - enemy.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                if (dist > enemy.size) {
                    enemy.x += (dx / dist) * enemy.speed * speedMult * chargeBoost * roarBoost * iceSlow * berserkerBoost * dt
                    enemy.y += (dy / dist) * enemy.speed * speedMult * chargeBoost * roarBoost * iceSlow * berserkerBoost * dt
                }
            }
            } // end blockade else
            if (enemy.hitFlash > 0) enemy.hitFlash -= dt
            if (enemy.roarBoostTimer > 0) {
                enemy.roarBoostTimer -= dt
                if (enemy.roarBoostTimer <= 0f) enemy.roarSpeedBoost = 1f
            }
            if (enemy.isAtBase(baseX, baseY)) {
                baseHp -= enemy.damage
                audio.play(SfxType.BASE_HIT)
                hapticPending = 1  // light haptic on base damage
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

        val deadEnemies = enemies.filter { it.isDead() && !it.deathProcessed }
        deadEnemies.forEach { enemy ->
            enemy.deathProcessed = true
            enemy.deathAnimTimer = 0.4f  // 400ms shrink+fade animation
            // Enemies that reached the base are removed but give no rewards
            if (enemy.reachedBase) return@forEach

            audio.play(SfxType.ENEMY_DIE)
            val comboGold = (enemy.goldReward * comboMultiplier * skillTree.goldBonusMultiplier()).toInt()
            val baseGold = enemy.goldReward.toInt()
            gold += comboGold
            totalGoldEarned += comboGold
            score += comboGold
            scoreFromKills += baseGold
            scoreFromCombos += (comboGold - baseGold).coerceAtLeast(0)
            totalKills++

            // Bounty tracking
            trackBountyKill(enemy)
            comboCount++
            comboTimer = 2f
            val comboBoostMult = if (endlessBuffs.count { it == EndlessBuff.COMBO_BOOST } > 0) 1.5f else 1f
            comboMultiplier = (1f + comboCount * 0.1f * comboBoostMult).coerceAtMost(8f)
            if (comboCount > bestCombo) bestCombo = comboCount
            trackBountyCombo(comboCount)

            floatingTexts.add(FloatingText(enemy.x, enemy.y, "+${comboGold}g", 0xFFFFD700.toInt()))

            // Credit kill to the tower that last hit this enemy
            enemy.lastHitTower?.let { tower ->
                tower.totalKills++
                towerMasteryKills[tower.type] = (towerMasteryKills[tower.type] ?: 0) + 1
            }

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

            // Type-specific death effects
            when (enemy.type) {
                EnemyType.SKELETON, EnemyType.FAST_SKELETON -> {
                    // Bone fragments scatter
                    repeat(8) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 80f + (Math.random() * 80f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp - 40f).toFloat(),
                            0.9f, 0xFFF5F5DC.toInt(), 3f))
                    }
                }
                EnemyType.SLIME -> {
                    // Green splat expanding outward
                    repeat(14) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 30f + (Math.random() * 60f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            1.2f, 0xFF00C853.toInt(), 6f + Math.random().toFloat() * 4f))
                    }
                }
                EnemyType.BAT -> {
                    // Feathers floating down
                    repeat(6) {
                        val ox = (Math.random().toFloat() - 0.5f) * 40f
                        particles.add(Particle(enemy.x + ox, enemy.y,
                            (Math.random().toFloat() - 0.5f) * 30f, 20f + Math.random().toFloat() * 20f,
                            1.5f, 0xFF4A148C.toInt(), 3f))
                    }
                }
                EnemyType.DEMON -> {
                    // Fire eruption
                    repeat(10) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 60f + (Math.random() * 100f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp - 50f).toFloat(),
                            0.7f, 0xFFFF5722.toInt(), 5f))
                    }
                    repeat(5) {
                        particles.add(Particle(enemy.x + (Math.random().toFloat() - 0.5f) * 20f, enemy.y,
                            0f, -(80f + Math.random().toFloat() * 40f),
                            0.6f, 0xFFFFAB00.toInt(), 4f))
                    }
                }
                EnemyType.DRAGON -> {
                    // Fiery explosion with screen shake
                    shakeTimer = 0.2f; shakeIntensity = 8f
                    repeat(16) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 100f + (Math.random() * 120f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            0.8f, if (Math.random() < 0.5) 0xFFFF9800.toInt() else 0xFFFF5722.toInt(), 7f))
                    }
                }
                EnemyType.ARMORED_GOLEM -> {
                    // Stone chunks crumbling
                    repeat(10) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 50f + (Math.random() * 80f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp + 30f).toFloat(),
                            1.0f, 0xFF795548.toInt(), 5f + Math.random().toFloat() * 3f))
                    }
                    shakeTimer = 0.15f; shakeIntensity = 5f
                }
                EnemyType.SPIDER -> {
                    // Webs scatter
                    repeat(6) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 40f + (Math.random() * 50f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            1.0f, 0x99EEEEEE.toInt(), 2f))
                    }
                }
                EnemyType.ORC -> {
                    // Blood-green burst
                    repeat(8) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 70f + (Math.random() * 80f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            0.7f, 0xFF558B2F.toInt(), 4f))
                    }
                }
                EnemyType.WISP -> {
                    // Ethereal sparkle dissipation
                    repeat(12) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 30f + (Math.random() * 60f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp - 40f).toFloat(),
                            0.8f, 0xFF00BCD4.toInt(), 3f + Math.random().toFloat() * 2f))
                    }
                }
                EnemyType.SHADOW -> {
                    // Dark smoke dissipation
                    repeat(10) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 50f + (Math.random() * 60f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            1.0f, 0xFF37474F.toInt(), 5f + Math.random().toFloat() * 3f))
                    }
                }
                EnemyType.FAST_SKELETON -> {
                    // Quick bone scatter (lighter version of skeleton)
                    repeat(4) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 80f + (Math.random() * 60f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp + 20f).toFloat(),
                            0.6f, 0xFFE0E0E0.toInt(), 4f))
                    }
                }
                else -> {} // Goblin etc. uses the default death burst above
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
                trackBountyBossKill()
                if (bossesKilledThisRun >= 5) checkAchievement("5_bosses")
                if (bossesKilledThisRun >= 10) checkAchievement("10_bosses")
                if (isBossRush && bossesKilledThisRun >= 5) checkAchievement("boss_rush_5")
                currentBoss = null
                // Boss always drops diamonds
                val diamondDrop = (2 + wave / 5).coerceAtMost(10)
                skillTree.addDiamonds(diamondDrop)
                diamondsEarnedThisRun += diamondDrop
                audio.play(SfxType.DIAMOND_DROP)
                floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 20f,
                    "+${diamondDrop} \uD83D\uDC8E", 0xFF00E5FF.toInt(), 1.5f, 30f))
            } else {
                // Regular enemies have a small diamond drop chance
                val dropChance = 0.03f + skillTree.diamondDropBonus()
                if (Math.random() < dropChance) {
                    skillTree.addDiamonds(1)
                    diamondsEarnedThisRun += 1
                    audio.play(SfxType.DIAMOND_DROP)
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
            if (comboCount >= 30) checkAchievement("combo_30")
            if (comboCount >= 50) checkAchievement("combo_50")
            if (score >= 1000) checkAchievement("score_1000")
            if (score >= 5000) checkAchievement("score_5000")
            if (score >= 10000) checkAchievement("score_10000")
            if (diamondsEarnedThisRun >= 10) checkAchievement("diamond_10")
            if (diamondsEarnedThisRun >= 50) checkAchievement("diamond_50")
        }

        // SPLIT modifier: spawn 2 mini versions on death
        if (currentWaveModifier == WaveModifier.SPLIT) {
            for (dead in deadEnemies) {
                if (dead.reachedBase || dead.type == EnemyType.BOSS) continue
                repeat(2) {
                    val miniHp = dead.maxHp * 0.3f
                    val mini = Enemy(
                        x = dead.x + ((Math.random() - 0.5) * 30).toFloat(),
                        y = dead.y + ((Math.random() - 0.5) * 20).toFloat(),
                        speed = dead.speed * 1.2f,
                        hp = miniHp, maxHp = miniHp,
                        goldReward = (dead.goldReward * 0.3f).toInt().coerceAtLeast(1),
                        damage = dead.damage * 0.4f,
                        type = dead.type,
                        size = dead.size * 0.6f,
                        pathIndex = dead.pathIndex,
                        waypointIndex = dead.waypointIndex
                    )
                    enemies.add(mini)
                }
            }
        }

        // Tick death animation timers and remove fully dead enemies
        enemies.forEach { if (it.deathProcessed) it.deathAnimTimer -= dt }
        enemies.removeAll { it.isDead() && it.deathProcessed && it.deathAnimTimer <= 0f }
        blockades.removeAll { it.isDead() }

        // === TRAP TRIGGERS ===
        for (trap in traps) {
            if (trap.isSpent()) continue
            if (trap.cooldown > 0) { trap.cooldown -= dt; continue }
            for (enemy in enemies) {
                if (enemy.isDead() || enemy.deathProcessed) continue
                if (trap.distanceTo(enemy.x, enemy.y) > trap.size + enemy.size) continue
                when (trap.type) {
                    TrapType.SPIKE -> {
                        val dmg = 15f + wave * 2f
                        enemy.hp -= dmg
                        floatingTexts.add(FloatingText(enemy.x, enemy.y - 20f, "-${dmg.toInt()}", 0xFFFF5252.toInt(), 0.6f, 18f))
                        repeat(4) {
                            val a = Math.random() * Math.PI * 2
                            particles.add(Particle(trap.x, trap.y, (Math.cos(a) * 40f).toFloat(), (Math.sin(a) * 40f).toFloat(), 0.4f, 0xFFBDBDBD.toInt(), 3f))
                        }
                    }
                    TrapType.TAR -> {
                        enemy.tarSlowTimer = 2.5f // Slow for 2.5 seconds
                        floatingTexts.add(FloatingText(enemy.x, enemy.y - 20f, "Slowed!", 0xFF8D6E63.toInt(), 0.6f, 16f))
                    }
                    TrapType.MINE -> {
                        // AoE explosion
                        val dmg = 80f + wave * 5f
                        var mineKills = 0
                        for (e in enemies) {
                            if (e.isDead() || e.deathProcessed) continue
                            val d = trap.distanceTo(e.x, e.y)
                            if (d < 100f) {
                                val falloff = 1f - (d / 100f)
                                e.hp -= dmg * falloff
                                if (e.hp <= 0) mineKills++
                                floatingTexts.add(FloatingText(e.x, e.y - 20f, "-${(dmg * falloff).toInt()}", 0xFFFF5252.toInt(), 0.8f, 20f))
                            }
                        }
                        if (mineKills >= 3) checkAchievement("mine_triple")
                        shakeTimer = 0.15f; shakeIntensity = 8f
                        repeat(15) {
                            val a = Math.random() * Math.PI * 2
                            val sp = 60f + (Math.random() * 80f).toFloat()
                            particles.add(Particle(trap.x, trap.y, (Math.cos(a) * sp).toFloat(), (Math.sin(a) * sp).toFloat(), 0.7f, 0xFFFF6F00.toInt(), 5f))
                        }
                        audio.play(SfxType.POWER_FIREBALL)
                    }
                }
                trap.uses--
                trap.cooldown = if (trap.type == TrapType.MINE) 0f else 0.5f
                if (trap.isSpent()) break
            }
        }
        traps.removeAll { it.isSpent() }

        // === VOLCANO ERUPTION ===
        if (mapType == MapType.VOLCANO && waveInProgress) {
            volcanoEruptionTimer -= dt
            if (volcanoEruptionTimer <= 0f && !volcanoErupting) {
                volcanoErupting = true
                volcanoEruptDuration = 2f
                volcanoEruptionTimer = 25f + (Math.random().toFloat() * 10f) // 25-35s between eruptions
                shakeTimer = 0.5f; shakeIntensity = 12f
                floatingTexts.add(FloatingText(volcanoCenterX, volcanoCenterY - 60f, "\uD83C\uDF0B ERUPTION!", 0xFFFF3D00.toInt(), 2f, 36f))
                audio.play(SfxType.BOSS_QUAKE)
            }
            if (volcanoErupting) {
                volcanoEruptDuration -= dt
                // Spawn lava particles
                repeat(3) {
                    val a = Math.random() * Math.PI * 2
                    val sp = 30f + (Math.random() * 80f).toFloat()
                    particles.add(Particle(volcanoCenterX, volcanoCenterY,
                        (Math.cos(a) * sp).toFloat(), (Math.sin(a) * sp - 40f).toFloat(),
                        1.2f, if (Math.random() < 0.5) 0xFFFF6F00.toInt() else 0xFFFF3D00.toInt(), 6f))
                }
                // Damage enemies and towers near center
                val eruptRadius = 180f
                for (enemy in enemies) {
                    if (enemy.isDead() || enemy.deathProcessed) continue
                    val dx = enemy.x - volcanoCenterX; val dy = enemy.y - volcanoCenterY
                    if (dx * dx + dy * dy < eruptRadius * eruptRadius) {
                        enemy.hp -= (20f + wave * 1.5f) * dt
                    }
                }
                for (tower in towers) {
                    val dx = tower.x - volcanoCenterX; val dy = tower.y - volcanoCenterY
                    if (dx * dx + dy * dy < eruptRadius * eruptRadius) {
                        tower.thornJamTimer = 0.5f // Jam towers near volcano during eruption
                    }
                }
                if (volcanoEruptDuration <= 0f) {
                    volcanoErupting = false
                }
            }
        }

        val towerDmgMult = (1f + (playerDamageLevel - 1) * 0.1f) * skillTree.towerDamageMultiplier() * skillTree.prestigeDamageMultiplier() * endlessBuffTowerDmg

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
            // HEALER towers don't fire — they heal base and nearby blockades
            if (tower.type == TowerType.HEALER) {
                if (tower.canFire()) {
                    tower.fire()
                    // Heal base for 2 HP
                    if (baseHp < maxBaseHp) {
                        val healAmt = 2f.coerceAtMost(maxBaseHp - baseHp)
                        baseHp += healAmt
                        floatingTexts.add(FloatingText(baseX, baseY - 60f, "+${healAmt.toInt()} HP", 0xFF66BB6A.toInt(), 0.8f, 20f))
                        particles.add(Particle(tower.x, tower.y, 0f, -30f, 0.6f, 0xFF66BB6A.toInt(), 5f))
                    }
                    // Repair nearby blockades for 5 HP each
                    blockades.filter { !it.isDead() && tower.distanceTo(it.x, it.y) < tower.range }
                        .forEach { blockade ->
                            if (blockade.hp < blockade.maxHp) {
                                val repairAmt = 5f.coerceAtMost(blockade.maxHp - blockade.hp)
                                blockade.hp += repairAmt
                                floatingTexts.add(FloatingText(blockade.x, blockade.y - blockade.size, "+${repairAmt.toInt()}", 0xFF66BB6A.toInt(), 0.6f, 16f))
                            }
                        }
                    audio.play(SfxType.HEALER_FIRE)
                }
                return@forEach
            }
            val effectiveRange = tower.range * (if (currentWaveModifier == WaveModifier.INVISIBLE) 0.7f else 1f) * nightRangeMultiplier
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
                    var dmg = tower.damage * towerDmgMult * resistMult * synergyMult * critMult * shieldMult

                    // Necro execute: massive bonus damage to low-HP enemies
                    if (tower.type == TowerType.NECRO && target.hp < target.maxHp * 0.15f) {
                        dmg *= 3f
                        floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "EXECUTE!", 0xFF9C27B0.toInt(), 1f, 22f))
                    }

                    target.hp -= dmg
                    target.hitFlash = 0.15f
                    tower.totalDamageDealt += dmg
                    target.lastHitTower = tower
                    if (tower.type == TowerType.FLAME) {
                        target.burnTimer = 3f
                        target.burnDps = tower.damage * towerDmgMult * 0.3f  // 30% of damage as DPS for 3s
                    }

                    // Vortex tower: pull enemy slightly toward tower
                    if (tower.type == TowerType.VORTEX) {
                        val dx = tower.x - target.x
                        val dy = tower.y - target.y
                        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        if (dist > 10f) {
                            val pull = 15f  // pixels toward tower
                            target.x += (dx / dist) * pull
                            target.y += (dy / dist) * pull
                        }
                    }

                    // Thorns: jam the attacking tower temporarily
                    if (target.isElite && target.eliteAbility == EliteAbility.THORNS) {
                        tower.thornJamTimer = 1.5f
                        floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "THORNS!", EliteAbility.THORNS.color, 0.8f, 20f))
                        repeat(4) {
                            particles.add(Particle(tower.x, tower.y,
                                (Math.random().toFloat() - 0.5f) * 80f,
                                (Math.random().toFloat() - 0.5f) * 80f,
                                0.4f, EliteAbility.THORNS.color, 4f))
                        }
                    }
                    audio.play(when (tower.type) {
                        TowerType.ARROW -> SfxType.ARROW_FIRE
                        TowerType.MAGIC -> SfxType.MAGIC_FIRE
                        TowerType.CANNON -> SfxType.CANNON_FIRE
                        TowerType.POISON -> SfxType.POISON_FIRE
                        TowerType.TESLA -> SfxType.TESLA_FIRE
                        TowerType.ICE -> SfxType.ICE_FIRE
                        TowerType.FLAME -> SfxType.FLAME_FIRE
                        TowerType.NECRO -> SfxType.NECRO_FIRE
                        TowerType.BALLISTA -> SfxType.BALLISTA_FIRE
                        TowerType.VORTEX -> SfxType.VORTEX_FIRE
                        TowerType.HEALER -> SfxType.HEALER_FIRE
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
                            TowerType.FLAME -> 0xFFFF5722.toInt()
                            TowerType.NECRO -> 0xFF9C27B0.toInt()
                            TowerType.BALLISTA -> 0xFFFFD54F.toInt()
                            TowerType.VORTEX -> 0xFF7E57C2.toInt()
                            TowerType.HEALER -> 0xFF66BB6A.toInt()
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
            audio.play(SfxType.GAME_OVER)
            saveRunHistory("Lost")
            isNewHighScore = score > highScore
            isNewEndlessRecord = isEndlessMode && wave > endlessHighWave
            // Batch all SharedPreferences writes into a single editor
            val editor = prefs.edit()
            if (score > highScore) { highScore = score; editor.putInt("highScore", highScore) }
            if (wave > highWave) { highWave = wave; editor.putInt("highWave", highWave) }
            val diffScoreKey = "highScore_$difficulty"
            if (score > prefs.getInt(diffScoreKey, 0)) {
                when (difficulty) { 0 -> highScoreEasy = score; 1 -> highScoreNormal = score; 2 -> highScoreHard = score }
                editor.putInt(diffScoreKey, score)
            }
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
                .putInt("lifetime_playtime", prefs.getInt("lifetime_playtime", 0) + playTimeSeconds.toInt())
            val best = prefs.getInt("lifetime_best_combo", 0)
            if (bestCombo > best) editor.putInt("lifetime_best_combo", bestCombo)
            val towerCounts = towers.groupBy { it.type.name }.mapValues { it.value.size }
            for ((typeName, count) in towerCounts) {
                val key = "tower_count_$typeName"
                editor.putInt(key, prefs.getInt(key, 0) + count)
            }
            // Save tower mastery kills
            for ((type, kills) in towerMasteryKills) {
                editor.putInt("mastery_${type.name}", kills)
            }
            editor.apply()
        }
    } }

    private fun startNextWave() {
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
        // Weather: rain at night, snow chance every 10 waves, clear otherwise
        weatherType = when {
            isNight && wave % 10 >= 7 -> 2  // snow in late night
            isNight -> 1  // rain at night
            else -> 0  // clear
        }
        weatherIntensity = if (weatherType > 0) 0.5f + (wave % 5) * 0.1f else 0f
        audio.play(SfxType.WAVE_START)
        if (wave >= 5) checkAchievement("wave_5")
        if (wave >= 10) checkAchievement("wave_10")
        if (wave >= 20) checkAchievement("wave_20")
        if (wave >= 30) checkAchievement("wave_30")
        if (wave >= 50) checkAchievement("wave_50")
        if (wave >= 100) checkAchievement("wave_100")
        if (isEndlessMode && wave >= 10) checkAchievement("endless_10")
        if (mapType == MapType.VOLCANO && wave >= 15) checkAchievement("volcano_win")
        if (isRandomizerMode && wave >= 15) checkAchievement("randomizer_win")
        if (wave >= 5 && towers.isEmpty()) checkAchievement("streak_no_tower")
        // Track maps played for cartographer achievement
        val mapsKey = "maps_played"
        val mapsPlayed = prefs.getString(mapsKey, "").split(",").filter { it.isNotBlank() }.toMutableSet()
        mapsPlayed.add(mapType.name)
        prefs.edit().putString(mapsKey, mapsPlayed.joinToString(",")).apply()
        if (mapsPlayed.size >= MapType.entries.size) checkAchievement("all_maps")
        baseHpBeforeWave = baseHp
        if (isBossRush) bossRushWave++

        // Use pre-determined modifier from preview (so preview matches reality)
        val preview = nextWavePreview
        currentWaveModifier = preview?.modifier ?: WaveModifier.NONE

        if (isBossRush || wave % bossInterval == 0) {
            if (bossPool.isEmpty()) {
                bossPool.addAll(BossType.entries.shuffled())
            }
            currentBoss = bossPool.removeFirst()
            enemiesRemaining = 1
            val minionCount = if (isBossRush) {
                // Fewer minions in boss rush — focus is on the bosses
                (currentBoss!!.minionCount * spawnRateMult * 0.6f).toInt().coerceAtLeast(1)
            } else {
                (currentBoss!!.minionCount * spawnRateMult).toInt().coerceAtLeast(2)
            }
            totalEnemiesThisWave = 1 + minionCount
            audio.play(SfxType.BOSS_APPEAR)
            bossFlashTimer = 0.4f
            shakeTimer = 0.3f; shakeIntensity = 8f
            hapticPending = 2  // heavy haptic on boss spawn
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
                bossAbilityTimer = 5f,
                bossAbilityCooldown = if (boss.ability == BossAbility.SHIELD) 12f else 5f
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
            wave >= 18 && Math.random() < 0.06 -> EnemyType.SHAPESHIFTER
            wave >= 15 && Math.random() < 0.07 -> EnemyType.COMMANDER
            wave >= 12 && Math.random() < 0.08 -> EnemyType.BERSERKER
            wave >= 10 && Math.random() < 0.08 -> EnemyType.WISP
            wave >= 9 && Math.random() < 0.08 -> EnemyType.SHADOW
            wave >= 8 && Math.random() < 0.12 -> EnemyType.ARMORED_GOLEM
            wave >= 7 && Math.random() < 0.15 -> EnemyType.DRAGON
            wave >= 5 && Math.random() < 0.18 -> EnemyType.DEMON
            wave >= 4 && Math.random() < 0.18 -> EnemyType.FAST_SKELETON
            wave >= 3 && Math.random() < 0.25 -> EnemyType.ORC
            wave >= 2 && Math.random() < 0.35 -> EnemyType.SKELETON
            wave >= 1 && Math.random() < 0.20 -> EnemyType.BAT
            wave >= 1 && Math.random() < 0.15 -> EnemyType.SLIME
            wave >= 2 && Math.random() < 0.12 -> EnemyType.SPIDER
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
            EnemyType.BAT -> arrayOf(15f, 120f, 2f, 4f)
            EnemyType.SLIME -> arrayOf(30f, 50f, 4f, 6f)
            EnemyType.SPIDER -> arrayOf(25f, 110f, 4f, 7f)
            EnemyType.WISP -> arrayOf(18f, 130f, 8f, 3f)
            EnemyType.SHADOW -> arrayOf(40f, 100f, 10f, 10f)
            EnemyType.BERSERKER -> arrayOf(50f, 70f, 10f, 14f)
            EnemyType.COMMANDER -> arrayOf(70f, 55f, 15f, 10f)
            EnemyType.SHAPESHIFTER -> arrayOf(45f, 90f, 12f, 9f)
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
            WaveModifier.SHIELDED -> {} // applied after spawn
            WaveModifier.BERSERKER -> {} // handled in movement code
            WaveModifier.SPLIT -> { hpMod = 0.7f } // slightly less HP since they split
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
            y = spawn.y + ((Math.random() - 0.5) * 40).toFloat(),
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
        // Apply SHIELDED modifier
        if (currentWaveModifier == WaveModifier.SHIELDED) enemy.shieldTimer = 3f
        enemy.isElite = isElite
        if (isElite) {
            val abilities = EliteAbility.entries.filter { it != EliteAbility.NONE }
            enemy.eliteAbility = abilities.random()
            // Apply ability-specific setup
            when (enemy.eliteAbility) {
                EliteAbility.REGEN -> enemy.regenRate += 8f + wave * 1f
                EliteAbility.SHIELD -> enemy.shieldTimer = 5f
                else -> {}
            }
        }
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
            val sp = if (paths.isNotEmpty()) paths[pathIdx].spawnPoint else GamePoint(screenW / 2, -40f)
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
        val bt = boss.bossType ?: return
        when (bt.ability) {
            BossAbility.CHARGE -> {
                boss.isCharging = true
                boss.chargeTimer = 2f
                audio.play(SfxType.BOSS_CHARGE)
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
                audio.play(SfxType.BOSS_SUMMON)
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
                audio.play(SfxType.BOSS_HEAL)
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
                audio.play(SfxType.BOSS_AOE)
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
                audio.play(SfxType.BOSS_SHIELD)
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
                    minion.roarSpeedBoost = 2.0f  // 2x speed for 3 seconds
                    minion.roarBoostTimer = 3f
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDCA8 ROAR!", bt.color, 1.2f, 32f))
                audio.play(SfxType.BOSS_ROAR)
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
                    audio.play(SfxType.BOSS_TELEPORT)
                }
            }
            BossAbility.DRAIN -> {
                val stolen = (10 + wave).coerceAtMost(gold)
                if (stolen > 0) {
                    gold -= stolen
                    boss.hp = (boss.hp + stolen * 2f).coerceAtMost(boss.maxHp * 1.2f)
                    floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "-${stolen}g DRAIN!", 0xFFE040FB.toInt(), 1.2f, 28f))
                    audio.play(SfxType.BOSS_DRAIN)
                    floatingTexts.add(FloatingText(screenW / 2, screenH * 0.4f, "-${stolen} gold stolen!", 0xFFF44336.toInt(), 1.5f, 32f))
                }
            }
            BossAbility.QUAKE -> {
                shakeTimer = 1f; shakeIntensity = 20f
                // Slow all towers
                towers.forEach { it.fireTimer += 1.5f }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83C\uDF0B QUAKE!", bt.color, 1.5f, 32f))
                audio.play(SfxType.BOSS_QUAKE)
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
                    audio.play(SfxType.BOSS_SPLIT)
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

        gold -= cost
        val masteryBonus = 1f + masteryDamageBonus(type)
        towers.add(Tower(x, y, type = type, damage = type.baseDamage * masteryBonus, range = type.baseRange * endlessBuffTowerRange, fireRate = type.baseFireRate))
        audio.play(SfxType.TOWER_PLACE)
        if (towers.size >= 5) checkAchievement("5_towers")
        if (towers.size >= 10) checkAchievement("10_towers")
        val towerTypes = towers.map { it.type }.toSet()
        if (towerTypes.size >= TowerType.entries.size) checkAchievement("all_tower_types")
        return true
    } }

    /** Place a blockade on or near a path — enemies must destroy it to pass */
    val blockadeCost: Int get() = 50
    fun placeBlockade(x: Float, y: Float): Boolean { synchronized(lock) {
        if (gold < blockadeCost) return false
        // Must be near a path segment (opposite of tower rule)
        var nearPath = false
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
                if (dx * dx + dy * dy < 55f * 55f) { nearPath = true; break }
            }
            if (nearPath) break
        }
        if (!nearPath) return false
        // Don't overlap existing blockades or towers
        if (blockades.any { it.distanceTo(x, y) < 60f }) return false
        if (towers.any { it.distanceTo(x, y) < 60f }) return false
        val distToBase = Math.sqrt(((x - baseX) * (x - baseX) + (y - baseY) * (y - baseY)).toDouble()).toFloat()
        if (distToBase < 80f) return false

        gold -= blockadeCost
        val hp = 80f + wave * 10f // Scales with wave
        blockades.add(Blockade(x, y, hp, hp))
        audio.play(SfxType.TOWER_PLACE)
        floatingTexts.add(FloatingText(x, y - 30f, "Blockade!", 0xFF8D6E63.toInt(), 1f, 24f))
        return true
    } }

    /** Collect a supply drop at touch position, returns true if collected */
    fun collectSupplyDrop(touchX: Float, touchY: Float): Boolean { synchronized(lock) {
        val drop = supplyDrops.firstOrNull { it.distanceTo(touchX, touchY) < it.size * 2f }
        if (drop != null) {
            gold += drop.goldAmount
            totalGoldEarned += drop.goldAmount
            floatingTexts.add(FloatingText(drop.x, drop.y - 20f, "+${drop.goldAmount}g", 0xFFFFD700.toInt(), 1f, 24f))
            repeat(8) {
                val angle = Math.random() * Math.PI * 2
                particles.add(Particle(drop.x, drop.y,
                    (Math.cos(angle) * 60).toFloat(), (Math.sin(angle) * 60).toFloat(),
                    0.5f, 0xFFFFD700.toInt(), 4f))
            }
            audio.play(SfxType.DIAMOND_DROP)
            supplyDrops.remove(drop)
            return true
        }
        return false
    } }

    /** Place a trap on an enemy path */
    fun placeTrap(x: Float, y: Float, type: TrapType): Boolean { synchronized(lock) {
        if (gold < type.cost) return false
        // Must be near a path segment
        var nearPath = false
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
                if (dx * dx + dy * dy < 55f * 55f) { nearPath = true; break }
            }
            if (nearPath) break
        }
        if (!nearPath) return false
        if (traps.any { it.distanceTo(x, y) < 50f }) return false
        if (blockades.any { it.distanceTo(x, y) < 50f }) return false
        if (towers.any { it.distanceTo(x, y) < 50f }) return false
        val distToBase = Math.sqrt(((x - baseX) * (x - baseX) + (y - baseY) * (y - baseY)).toDouble()).toFloat()
        if (distToBase < 80f) return false

        gold -= type.cost
        val uses = when (type) {
            TrapType.SPIKE -> 5 + wave / 5
            TrapType.TAR -> 8 + wave / 4
            TrapType.MINE -> 1
        }
        traps.add(Trap(x, y, type, uses, uses))
        audio.play(SfxType.TOWER_PLACE)
        floatingTexts.add(FloatingText(x, y - 30f, "${type.emoji} ${type.displayName}!", 0xFFFF6F00.toInt(), 1f, 22f))
        trapsPlacedThisRun++
        checkAchievement("trap_first")
        if (trapsPlacedThisRun >= 10) checkAchievement("trap_10")
        return true
    } }

    fun upgradeTower(tower: Tower): Boolean { synchronized(lock) {
        val cost = tower.upgradeCost()
        if (gold < cost) return false
        gold -= cost
        tower.upgrade()
        audio.play(SfxType.TOWER_UPGRADE)
        if (tower.level >= 5) checkAchievement("max_tower")
        return true
    } }

    fun sellTower(tower: Tower): Boolean { synchronized(lock) {
        val refund = (tower.sellValue() * (1f + skillTree.sellValueBonus())).toInt()
        towers.remove(tower)
        gold += refund
        audio.play(SfxType.TOWER_SELL)
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
        audio.play(SfxType.TOWER_ABILITY)
        abilityTowerTypesUsed.add(tower.type)
        if (abilityTowerTypesUsed.size >= 5) checkAchievement("ability_all")
        val towerDmgMult = (1f + (playerDamageLevel - 1) * 0.1f) * skillTree.towerDamageMultiplier() * skillTree.prestigeDamageMultiplier() * endlessBuffTowerDmg
        when (tower.type) {
            TowerType.ARROW -> {
                // Volley: fire 5 rapid shots at different enemies
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range }
                    .shuffled().take(5)
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = tower.damage * towerDmgMult * 1.5f * resistMult
                    target.hp -= dmg
                    target.hitFlash = 0.2f
                    projectiles.add(Projectile(tower.x, tower.y, target.x, target.y, damage = 0f, color = 0xFFFFEB3B.toInt()))
                    floatingTexts.add(FloatingText(target.x, target.y - target.size, "-${dmg.toInt()}", 0xFFFFEB3B.toInt(), 0.5f, 16f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "VOLLEY!", 0xFFFFEB3B.toInt(), 1.2f, 28f))
            }
            TowerType.MAGIC -> {
                // Arcane Blast: AoE damage around tower
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range * 1.2f }
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = tower.damage * towerDmgMult * 2f * resistMult
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
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range }
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = tower.damage * towerDmgMult * 3f * resistMult
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
                    val dmg = tower.damage * towerDmgMult * 0.5f * resistMult
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
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range * 1.5f }
                var prevX = tower.x; var prevY = tower.y
                inRange.forEach { target ->
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = tower.damage * towerDmgMult * 1.8f * resistMult
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
                val iceRange = tower.range * 1.2f
                enemies.filter { it.distanceTo(tower.x, tower.y) < iceRange }.forEach { target ->
                    target.deepFreezeTimer = 3f // Nearly stopped for 3 seconds
                    target.hitFlash = 0.5f
                    repeat(4) {
                        particles.add(Particle(target.x, target.y,
                            ((Math.random() - 0.5) * 50).toFloat(), ((Math.random() - 0.5) * 50).toFloat(),
                            0.6f, 0xFF81D4FA.toInt(), 4f))
                    }
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "DEEP FREEZE!", 0xFF81D4FA.toInt(), 1.2f, 28f))
            }
            TowerType.FLAME -> {
                // Inferno: burn all enemies in range with heavy DoT
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range }
                inRange.forEach { target ->
                    target.burnTimer = 5f
                    target.burnDps = tower.damage * towerDmgMult * 0.5f
                    target.hitFlash = 0.3f
                }
                repeat(30) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * tower.range * 0.9
                    particles.add(Particle(
                        tower.x + (Math.cos(angle) * dist).toFloat(),
                        tower.y + (Math.sin(angle) * dist).toFloat(),
                        ((Math.random() - 0.5) * 40).toFloat(), -50f - (Math.random() * 30).toFloat(),
                        0.7f, 0xFFFF5722.toInt(), 6f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "INFERNO!", 0xFFFF5722.toInt(), 1.2f, 28f))
                shakeTimer = 0.2f; shakeIntensity = 5f
            }
            TowerType.NECRO -> {
                // Soul Harvest: instantly kill all enemies below 15% HP in range, bonus gold
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range && it.hp > 0 }
                var harvested = 0
                inRange.forEach { target ->
                    if (target.hp < target.maxHp * 0.15f) {
                        target.hp = 0f
                        harvested++
                        repeat(6) {
                            particles.add(Particle(target.x, target.y,
                                ((Math.random() - 0.5) * 60).toFloat(), -40f - (Math.random() * 40).toFloat(),
                                0.5f, 0xFF9C27B0.toInt(), 5f))
                        }
                    } else {
                        // Deal damage to healthy enemies
                        val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                        val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                        val dmg = tower.damage * towerDmgMult * 2f * resistMult
                        target.hp -= dmg
                        target.hitFlash = 0.3f
                        floatingTexts.add(FloatingText(target.x, target.y - target.size, "-${dmg.toInt()}", 0xFF9C27B0.toInt(), 0.5f, 16f))
                    }
                }
                if (harvested > 0) {
                    floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "HARVEST! ×$harvested", 0xFF9C27B0.toInt(), 1.2f, 28f))
                } else {
                    floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "SOUL HARVEST!", 0xFF9C27B0.toInt(), 1.2f, 28f))
                }
            }
            TowerType.BALLISTA -> {
                // Siege Shot: massive single-target damage to strongest enemy in extended range
                val target = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range * 1.3f && it.hp > 0 }
                    .maxByOrNull { it.hp }
                if (target != null) {
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = tower.damage * towerDmgMult * 5f * resistMult
                    target.hp -= dmg
                    target.hitFlash = 0.5f
                    projectiles.add(Projectile(tower.x, tower.y, target.x, target.y, damage = 0f, color = 0xFFFFD54F.toInt(), speed = 900f, size = 14f))
                    floatingTexts.add(FloatingText(target.x, target.y - target.size, "SIEGE! -${dmg.toInt()}", 0xFFFFD54F.toInt(), 1.2f, 28f))
                    shakeTimer = 0.25f; shakeIntensity = 7f
                    repeat(10) {
                        particles.add(Particle(target.x, target.y,
                            ((Math.random() - 0.5) * 150).toFloat(), ((Math.random() - 0.5) * 150).toFloat(),
                            0.5f, 0xFFFFD54F.toInt(), 5f))
                    }
                } else {
                    floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "No target!", 0xFF9E9E9E.toInt(), 0.8f, 20f))
                    tower.abilityTimer = 0f // Refund cooldown if no target
                }
            }
            TowerType.VORTEX -> {
                // Singularity: pull all enemies toward tower center and deal damage
                val inRange = enemies.filter { it.distanceTo(tower.x, tower.y) < tower.range * 1.2f && it.hp > 0 }
                inRange.forEach { target ->
                    val dx = tower.x - target.x
                    val dy = tower.y - target.y
                    val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                    if (dist > 10f) {
                        val pull = 60f  // Strong pull
                        target.x += (dx / dist) * pull
                        target.y += (dy / dist) * pull
                    }
                    val rawResist = EnemyResistances.getMultiplier(target.type, tower.type.damageType)
                    val resistMult = if (rawResist < 1f) rawResist + (1f - rawResist) * skillTree.resistancePierce() else rawResist
                    val dmg = tower.damage * towerDmgMult * 3f * resistMult
                    target.hp -= dmg
                    target.hitFlash = 0.2f
                }
                repeat(25) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = tower.range * 0.8 * Math.random()
                    val px = tower.x + (Math.cos(angle) * dist).toFloat()
                    val py = tower.y + (Math.sin(angle) * dist).toFloat()
                    particles.add(Particle(px, py,
                        (tower.x - px) * 2f, (tower.y - py) * 2f,
                        0.6f, 0xFF7E57C2.toInt(), 4f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "SINGULARITY!", 0xFF7E57C2.toInt(), 1.2f, 28f))
                shakeTimer = 0.2f; shakeIntensity = 6f
            }
            TowerType.HEALER -> {
                // Mass Heal: heal base for 10 HP and all blockades for 15 HP
                val healAmt = 10f.coerceAtMost(maxBaseHp - baseHp)
                if (healAmt > 0) {
                    baseHp += healAmt
                    floatingTexts.add(FloatingText(baseX, baseY - 60f, "+${healAmt.toInt()} HP!", 0xFF66BB6A.toInt(), 1.2f, 28f))
                }
                blockades.filter { !it.isDead() }.forEach { blockade ->
                    val repairAmt = 15f.coerceAtMost(blockade.maxHp - blockade.hp)
                    if (repairAmt > 0) {
                        blockade.hp += repairAmt
                        floatingTexts.add(FloatingText(blockade.x, blockade.y - blockade.size, "+${repairAmt.toInt()}", 0xFF66BB6A.toInt(), 0.8f, 20f))
                    }
                }
                repeat(15) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * tower.range * 0.6
                    particles.add(Particle(
                        tower.x + (Math.cos(angle) * dist).toFloat(),
                        tower.y + (Math.sin(angle) * dist).toFloat(),
                        0f, -30f - (Math.random() * 20).toFloat(),
                        0.6f, 0xFF66BB6A.toInt(), 5f))
                }
                floatingTexts.add(FloatingText(tower.x, tower.y - tower.size, "MASS HEAL!", 0xFF66BB6A.toInt(), 1.2f, 28f))
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
        audio.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun upgradePlayerSpeed(): Boolean { synchronized(lock) {
        val cost = playerSpeedLevel * 20
        if (gold < cost) return false
        gold -= cost
        playerSpeedLevel++
        player.speed += 30f
        audio.play(SfxType.PLAYER_UPGRADE)
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
        audio.play(SfxType.PLAYER_UPGRADE)
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
        audio.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun repairBase(): Boolean { synchronized(lock) {
        val cost = repairCost
        if (gold < cost) return false
        if (baseHp >= maxBaseHp) return false
        gold -= cost
        baseHp = (baseHp + 30f).coerceAtMost(maxBaseHp)
        audio.play(SfxType.POWER_HEAL)
        repairsThisRun++
        if (repairsThisRun >= 3) checkAchievement("repaired_3")
        return true
    } }

    /** Repair cost scales slightly with wave */
    val repairCost: Int get() = (20 + (wave / 5) * 5).coerceAtMost(50)

    /** Player picks an endless milestone buff */
    fun pickEndlessBuff(buff: EndlessBuff) { synchronized(lock) {
        endlessBuffs.add(buff)
        endlessMilestonePending = false
        endlessMilestoneChoices = emptyList()
        isPaused = false
        when (buff) {
            EndlessBuff.TOWER_DMG_UP -> endlessBuffTowerDmg += 0.20f
            EndlessBuff.GOLD_BONUS -> endlessBuffGold += 0.30f
            EndlessBuff.BASE_FORTIFY -> {
                endlessBuffBaseHp += 50f
                maxBaseHp += 50f
                baseHp += 50f
            }
            EndlessBuff.POWER_CDR -> endlessBuffPowerCdr = (endlessBuffPowerCdr + 0.25f).coerceAtMost(0.75f)
            EndlessBuff.CRIT_UP -> endlessBuffCritChance += 0.08f
            EndlessBuff.HEAL_PULSE -> {} // Applied in update loop
            EndlessBuff.TOWER_RANGE -> {
                endlessBuffTowerRange *= 1.15f
                towers.forEach { it.range *= 1.15f }
            }
            EndlessBuff.ENEMY_SLOW -> {
                enemySpeedMult *= 0.90f
            }
            EndlessBuff.DOUBLE_INTEREST -> {} // Applied in interest calc
            EndlessBuff.COMBO_BOOST -> {} // Applied in combo calc
        }
        floatingTexts.add(FloatingText(screenW / 2, screenH * 0.35f,
            "${buff.emoji} ${buff.label}!", 0xFFFFD700.toInt(), 2f, 36f))
    } }

    fun usePower(type: PowerType): Boolean { synchronized(lock) {
        if (!isPowerAllowed(type)) return false
        val cd = powerCooldowns.getOrDefault(type, 0f)
        if (cd > 0) return false

        // In randomizer mode, the button triggers a different power's effect
        val actualType = if (isRandomizerMode) randomizerPowerSwap[type] ?: type else type

        when (actualType) {
            PowerType.FIREBALL -> {
                if (gold < type.cost) return false
                gold -= type.cost
                enemies.forEach { enemy ->
                    val shieldMult = if (enemy.shieldTimer > 0) 0.3f else 1f
                    enemy.hp -= 50f * shieldMult * randomizerPowerDamageMult
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
                audio.play(SfxType.POWER_FIREBALL)
                powerCooldowns[type] = getRandomizedPowerCooldown(type)
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
                audio.play(SfxType.POWER_FREEZE)
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
                audio.play(SfxType.POWER_HEAL)
                powerCooldowns[type] = getRandomizedPowerCooldown(type)
            }
            PowerType.LIGHTNING -> {
                if (gold < type.cost) return false
                gold -= type.cost
                val targets = enemies.sortedBy { it.distanceTo(player.x, player.y) }.take(5)
                var prevX = player.x; var prevY = player.y
                targets.forEach { enemy ->
                    val shieldMult = if (enemy.shieldTimer > 0) 0.3f else 1f
                    enemy.hp -= 80f * shieldMult * randomizerPowerDamageMult
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
                audio.play(SfxType.POWER_LIGHTNING)
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
                val actualDashDmg = dashDamage * shieldMult
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
        audio.play(SfxType.PLAYER_ATTACK)
        return true
    } }

    // ─── BOUNTY SYSTEM ───

    fun generateBounties() {
        if (bountiesGenerated) return
        if (!isEndlessMode && !isBossRush) return
        bountiesGenerated = true
        activeBounties.clear()
        val rng = java.util.Random()
        val pool = mutableListOf(
            Bounty("kill_demons", "Kill 10 Demons", "\uD83D\uDC7F", 10, rewardDiamonds = 3),
            Bounty("kill_dragons", "Kill 5 Dragons", "\uD83D\uDC09", 5, rewardDiamonds = 5),
            Bounty("kill_50", "Kill 50 enemies", "\uD83D\uDC80", 50, rewardGold = 200),
            Bounty("kill_150", "Kill 150 enemies", "\uD83D\uDDE1\uFE0F", 150, rewardDiamonds = 5),
            Bounty("combo_15", "Reach 15x combo", "\uD83D\uDD17", 15, rewardDiamonds = 3),
            Bounty("combo_25", "Reach 25x combo", "\u26D3\uFE0F", 25, rewardDiamonds = 5),
            Bounty("bosses_3", "Kill 3 bosses", "\u2620\uFE0F", 3, rewardDiamonds = 5),
            Bounty("wave_15", "Survive 15 waves", "\u2694\uFE0F", 15, rewardGold = 250),
            Bounty("wave_30", "Survive 30 waves", "\uD83C\uDFC6", 30, rewardDiamonds = 5),
            Bounty("towers_8", "Build 8 towers", "\uD83C\uDFD7\uFE0F", 8, rewardGold = 150),
            Bounty("gold_500", "Earn 500 gold total", "\uD83D\uDCB0", 500, rewardDiamonds = 2),
            Bounty("no_damage_wave", "Complete a wave without base damage", "\uD83D\uDEE1\uFE0F", 1, rewardDiamonds = 4)
        )
        pool.shuffle(rng)
        activeBounties.addAll(pool.take(3))
    }

    private fun trackBountyKill(enemy: Enemy) {
        for (b in activeBounties) {
            if (b.completed) continue
            when (b.id) {
                "kill_demons" -> if (enemy.type == EnemyType.DEMON) b.progress++
                "kill_dragons" -> if (enemy.type == EnemyType.DRAGON) b.progress++
                "kill_50", "kill_150" -> b.progress++
            }
            if (b.progress >= b.target && !b.completed) completeBounty(b)
        }
    }

    fun trackBountyCombo(combo: Int) {
        for (b in activeBounties) {
            if (b.completed) continue
            when (b.id) {
                "combo_15", "combo_25" -> { b.progress = b.progress.coerceAtLeast(combo) }
            }
            if (b.progress >= b.target && !b.completed) completeBounty(b)
        }
    }

    fun trackBountyBossKill() {
        for (b in activeBounties) {
            if (b.completed) continue
            if (b.id == "bosses_3") b.progress++
            if (b.progress >= b.target && !b.completed) completeBounty(b)
        }
    }

    fun trackBountyWave() {
        for (b in activeBounties) {
            if (b.completed) continue
            when (b.id) {
                "wave_15", "wave_30" -> b.progress = wave
                "towers_8" -> b.progress = towers.size
                "gold_500" -> b.progress = totalGoldEarned
            }
            if (b.progress >= b.target && !b.completed) completeBounty(b)
        }
    }

    fun trackBountyNoDamage() {
        for (b in activeBounties) {
            if (b.completed) continue
            if (b.id == "no_damage_wave") {
                b.progress = 1
                completeBounty(b)
            }
        }
    }

    private fun completeBounty(b: Bounty) {
        b.completed = true
        if (b.rewardDiamonds > 0) {
            val d = prefs.getInt("diamonds", 0) + b.rewardDiamonds
            prefs.edit().putInt("diamonds", d).apply()
            diamondsEarnedThisRun += b.rewardDiamonds
            floatingTexts.add(FloatingText(screenW / 2, screenH * 0.25f,
                "\uD83C\uDFAF BOUNTY! +${b.rewardDiamonds}\uD83D\uDC8E", 0xFF00E5FF.toInt(), 2.5f, 32f))
        }
        if (b.rewardGold > 0) {
            gold += b.rewardGold
            totalGoldEarned += b.rewardGold
            floatingTexts.add(FloatingText(screenW / 2, screenH * 0.25f,
                "\uD83C\uDFAF BOUNTY! +${b.rewardGold}g", 0xFFFFD700.toInt(), 2.5f, 32f))
        }
        audio.play(SfxType.ACHIEVEMENT)
        checkAchievement("bounty_first")
        if (activeBounties.all { it.completed }) checkAchievement("bounty_all")
    }

    // ─── RUN HISTORY ───

    fun saveRunHistory(result: String = "Lost") {
        val mode = when {
            isBossRush -> "Boss Rush"
            isEndlessMode -> "Endless"
            isDailyChallenge -> "Daily"
            isRandomizerMode -> "Randomizer"
            campaignLevel != null -> "Campaign Lv${campaignLevel!!.id}"
            else -> "Classic"
        }
        val diffLabel = when (difficulty) { 0 -> "Easy"; 2 -> "Hard"; else -> "Normal" }
        // Load existing history
        val historyJson = prefs.getString("run_history", "") ?: ""
        val entries = historyJson.split("|||").filter { it.isNotBlank() }.toMutableList()
        // Build entry (11 fields: mode|diff|wave|score|kills|combo|towers|diamonds|map|timestamp|result)
        val entry = "$mode|$diffLabel|$wave|$score|$totalKills|$bestCombo|${towers.size}|$diamondsEarnedThisRun|${mapType.name}|${System.currentTimeMillis()}|$result"
        entries.add(0, entry) // newest first
        if (entries.size > 50) entries.subList(50, entries.size).clear() // cap at 50
        prefs.edit().putString("run_history", entries.joinToString("|||")).apply()
    }

    private fun checkAchievement(id: String) {
        val ach = achievements.find { it.id == id } ?: return
        if (ach.unlocked) return
        ach.unlocked = true
        prefs.edit().putBoolean("ach_${id}", true).apply()
        newAchievement = ach
        achievementBannerTimer = 3f
        audio.play(SfxType.ACHIEVEMENT)
    }

    private fun checkUpgradeAll() {
        if (playerDamageLevel >= 2 && playerSpeedLevel >= 2 && playerHpLevel >= 2 && baseHpLevel >= 2) {
            checkAchievement("upgrade_all")
        }
    }

    /** Save game state to preferences for continuing later */
    fun saveGame() {
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
        ed.putBoolean("has_save", true)
        ed.apply()
    }

    /** Load saved game state */
    fun loadGame(): Boolean {
        if (!prefs.getBoolean("has_save", false)) return false
        // Restore difficulty + mode flags + map before loading stats
        val savedDifficulty = prefs.getInt("save_difficulty", 1)
        applyDifficulty(savedDifficulty)
        val savedMap = prefs.getString("save_map", "CLASSIC") ?: "CLASSIC"
        try { mapType = MapType.valueOf(savedMap) } catch (_: Exception) {}
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
        waveInProgress = false
        waveTimer = 4f
        generateNextWavePreview()
        return true
    }

    /** Clear saved game */
    fun clearSave() {
        prefs.edit().putBoolean("has_save", false).apply()
    }

    fun hasSave(): Boolean = prefs.getBoolean("has_save", false)

    fun restart() { synchronized(lock) {
        enemies.clear()
        towers.clear()
        projectiles.clear()
        floatingTexts.clear()
        particles.clear()
        blockades.clear()
        traps.clear()
        supplyDrops.clear()
        supplyDropTimer = 15f
        activeBounties.clear()
        bountiesGenerated = false
        volcanoEruptionTimer = 0f
        volcanoErupting = false
        volcanoEruptDuration = 0f
        // Reset endless milestone buffs
        endlessBuffs.clear()
        endlessBuffTowerDmg = 1f
        endlessBuffGold = 1f
        endlessBuffBaseHp = 0f
        endlessBuffPowerCdr = 0f
        endlessBuffCritChance = 0f
        endlessBuffTowerRange = 1f
        endlessMilestoneChoices = emptyList()
        endlessMilestonePending = false
        campaignStars = 0
        applyDifficulty(difficulty)
        wave = 0
        if (!isIronmanMode) { baseHp = 100f; maxBaseHp = 100f }
        score = 0
        scoreFromKills = 0
        scoreFromCombos = 0
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
        abilityTowerTypesUsed.clear()
        trapsPlacedThisRun = 0
        baseHpBeforeWave = 100f
        dashCooldown = 0f
        isDashing = false
        isNight = false
        eliteSpawnedThisWave = false
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
