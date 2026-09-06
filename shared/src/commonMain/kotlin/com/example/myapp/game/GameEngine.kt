package com.example.myapp.game

import kotlin.math.min
import kotlin.math.max

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
    val emoji: String, var unlocked: Boolean = false,
    val diamondReward: Int = 10
)

data class WavePreview(
    val isBoss: Boolean,
    val enemies: Map<EnemyType, Int>,
    val bossType: BossType? = null,
    val modifier: WaveModifier = WaveModifier.NONE,
    val weather: WeatherEvent = WeatherEvent.CLEAR
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
    val activeFirePatches = mutableListOf<FirePatch>()
    val discoveredFusions: MutableSet<String> = prefs.getString("discovered_fusions", "").split(",").filter { it.isNotBlank() }.toMutableSet()
    private var isEchoingConduit: Boolean = false
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

    // Dynamic Weather Event
    var currentWeather: WeatherEvent = WeatherEvent.CLEAR
    var weatherBannerTimer: Float = 0f
    var showWeatherBanner: Boolean = false
    var weatherLightningTimer: Float = 0f
    var lightningFlashTimer: Float = 0f
    var lastLightningTarget: GamePoint? = null

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

    // Achievements with Diamond Bounties
    val achievements = mutableListOf(
        Achievement("first_kill", "First Blood", "Kill your first enemy", "\uD83D\uDDE1\uFE0F", false, 5),
        Achievement("wave_5", "Survivor", "Reach wave 5", "\uD83D\uDEE1\uFE0F", false, 10),
        Achievement("wave_10", "Veteran", "Reach wave 10", "\u2694\uFE0F", false, 15),
        Achievement("wave_20", "Legend", "Reach wave 20", "\uD83D\uDC51", false, 25),
        Achievement("wave_30", "Immortal", "Reach wave 30", "\uD83C\uDFC6", false, 35),
        Achievement("wave_50", "Mythic", "Reach wave 50", "\uD83C\uDF1F", false, 50),
        Achievement("kills_50", "Slayer", "Kill 50 enemies", "\uD83D\uDC80", false, 10),
        Achievement("kills_200", "Destroyer", "Kill 200 enemies", "\uD83D\uDD25", false, 20),
        Achievement("kills_500", "Annihilator", "Kill 500 enemies", "\uD83D\uDCA5", false, 30),
        Achievement("combo_10", "Combo King", "Get a 10x combo", "\uD83D\uDD17", false, 10),
        Achievement("combo_20", "Combo God", "Get a 20x combo", "\u26D3\uFE0F", false, 20),
        Achievement("boss_kill", "Boss Slayer", "Kill your first boss", "\u2620\uFE0F", false, 15),
        Achievement("5_bosses", "Boss Hunter", "Kill 5 bosses in one run", "\uD83D\uDC09", false, 30),
        Achievement("5_towers", "Architect", "Place 5 towers", "\uD83C\uDFD7\uFE0F", false, 10),
        Achievement("10_towers", "Fortress", "Place 10 towers", "\uD83C\uDFF0", false, 20),
        Achievement("all_tower_types", "Arsenal", "Place all tower types", "\uD83C\uDFAF", false, 25),
        Achievement("use_power", "Sorcerer", "Use a power for the first time", "\u2728", false, 5),
        Achievement("max_tower", "Master Builder", "Upgrade a tower to level 5", "\u2B06\uFE0F", false, 15),
        Achievement("rich", "Rich", "Have 500 gold at once", "\uD83D\uDCB0", false, 15),
        Achievement("rich_1000", "Millionaire", "Have 1000 gold at once", "\uD83E\uDD11", false, 25),
        Achievement("score_1000", "Score Chaser", "Reach 1000 score", "\uD83D\uDCCA", false, 15),
        Achievement("diamond_10", "Diamond Hoarder", "Earn 10 diamonds in a run", "\uD83D\uDC8E", false, 20),
        Achievement("repaired_3", "Mechanic", "Repair the base 3 times in a run", "\uD83D\uDD27", false, 10),
        Achievement("upgrade_all", "Well Rounded", "Buy all 4 player upgrades", "\uD83C\uDF96\uFE0F", false, 20),
        Achievement("endless_10", "Endurance", "Reach wave 10 in endless mode", "\u267E\uFE0F", false, 15),
        Achievement("kills_1000", "Genocide", "Kill 1000 enemies in one run", "\uD83D\uDC7B", false, 40),
        Achievement("wave_100", "Centurion", "Reach wave 100", "\u2694\uFE0F", false, 100),
        Achievement("no_damage", "Untouchable", "Complete a wave without base taking damage", "\uD83D\uDEE1\uFE0F", false, 20),
        Achievement("speed_demon", "Speed Demon", "Beat wave 10 on 3x speed", "\uD83D\uDCA8", false, 25),
        Achievement("10_bosses", "Boss Legend", "Kill 10 bosses in one run", "\uD83D\uDC32", false, 50),
        Achievement("diamond_50", "Diamond Mine", "Earn 50 diamonds in one run", "\uD83D\uDC8E", false, 40),
        Achievement("gold_hoarder", "Gold Hoarder", "Have 2000 gold at once", "\uD83C\uDFE6", false, 35),
        Achievement("all_powers", "Elementalist", "Use all 4 powers in one run", "\uD83C\uDF0A", false, 20),
        Achievement("survivor_1hp", "Last Stand", "Win a wave with base at 1 HP", "\u2764\uFE0F", false, 35),
        Achievement("campaign_5", "Campaigner", "Complete 5 campaign levels", "\uD83D\uDDFA\uFE0F", false, 25),
        Achievement("campaign_10", "Strategist", "Complete 10 campaign levels", "\uD83C\uDFC5", false, 35),
        Achievement("campaign_all", "Conqueror", "Complete all campaign levels", "\uD83D\uDC51", false, 75),
        Achievement("campaign_no_damage", "Flawless", "Beat a campaign level without base damage", "\uD83D\uDEE1\uFE0F", false, 25),
        Achievement("campaign_3star", "Perfectionist", "Get 3 stars on 5 campaign levels", "\u2B50", false, 30),
        Achievement("trap_first", "Trapper", "Place your first trap", "\uD83E\uDEE4", false, 10),
        Achievement("trap_10", "Minefield", "Place 10 traps in one run", "\uD83D\uDCA3", false, 20),
        Achievement("mine_triple", "Triple Threat", "Kill 3 enemies with one mine", "\uD83D\uDCA5", false, 25),
        Achievement("bounty_first", "Bounty Hunter", "Complete your first bounty", "\uD83C\uDFAF", false, 15),
        Achievement("bounty_all", "Bounty King", "Complete all 3 bounties in one run", "\uD83D\uDC51", false, 30),
        Achievement("volcano_win", "Volcanic Victory", "Reach wave 15 on Volcano map", "\uD83C\uDF0B", false, 25),
        Achievement("combo_30", "Unstoppable", "Get a 30x combo", "\uD83D\uDD25", false, 30),
        Achievement("combo_50", "Godlike", "Get a 50x combo", "\u26A1", false, 50),
        Achievement("streak_no_tower", "Lone Wolf", "Reach wave 5 with no towers placed", "\uD83D\uDC3A", false, 35),
        Achievement("all_maps", "Cartographer", "Play on all 8 maps", "\uD83C\uDF0D", false, 30),
        Achievement("boss_rush_5", "Gauntlet", "Defeat 5 bosses in Boss Rush", "\uD83D\uDDE1\uFE0F", false, 30),
        Achievement("randomizer_win", "Chaos Master", "Reach wave 15 in Randomizer", "\uD83C\uDFB2", false, 30),
        Achievement("ability_all", "Tactician", "Use abilities on 5 tower types in one run", "\u2728", false, 25),
        Achievement("prestige_first", "Reborn", "Prestige for the first time", "\uD83D\uDC51", false, 50),
        Achievement("score_5000", "High Roller", "Reach 5000 score", "\uD83C\uDFC5", false, 30),
        Achievement("score_10000", "Legendary Score", "Reach 10000 score", "\uD83E\uDD47", false, 60),

        // --- 15 New Strategic & Boss Achievements ---
        Achievement("boss_leviathan", "Storm Tamer", "Defeat the Storm Leviathan", "⚡", false, 25),
        Achievement("boss_phoenix", "Phoenix Reborn", "Defeat the Void Phoenix in both forms", "🪶", false, 25),
        Achievement("boss_spore", "Mycology Expert", "Defeat the Spore Overlord", "🍄", false, 25),
        Achievement("boss_chrono", "Time Warden", "Defeat the Chrono Lich", "⏳", false, 25),
        Achievement("boss_dreadnought", "Siege Breaker", "Destroy the Iron Dreadnought", "🤖", false, 25),
        Achievement("spec_first", "Master Artisan", "Specialize your first Level 5 tower", "⚡", false, 15),
        Achievement("spec_trio", "Grand Architect", "Specialize 3 towers in a single run", "🏛️", false, 30),
        Achievement("milestone_first", "Destiny's Boon", "Select your first Endless Milestone Buff", "🌟", false, 15),
        Achievement("milestone_trio", "Ascended Champion", "Pick 3 Milestone Buffs in one run", "✨", false, 30),
        Achievement("relic_first", "Ancient Reliquary", "Unlock your first Legendary Relic", "🏺", false, 20),
        Achievement("relic_3", "Treasury of Ancients", "Unlock 3 Legendary Relics", "👑", false, 50),
        Achievement("campaign_3star_10", "Grand Strategist", "Earn 3 stars on 10 campaign levels", "⭐", false, 50),
        Achievement("endless_25", "Abyssal Challenger", "Reach wave 25 in Endless mode", "🌊", false, 30),
        Achievement("endless_50", "Titan of the Endless", "Reach wave 50 in Endless mode", "🔥", false, 60),
        Achievement("boss_rush_10", "Colosseum God", "Defeat 10 bosses in Boss Rush", "⚔️", false, 50),

        // --- 16 Merchant, Synergy, Weather & Combat Mastery Achievements ---
        Achievement("merchant_first", "First Barter", "Draft your first item from the Wandering Merchant", "🛒", false, 15),
        Achievement("merchant_trio", "Bazaar Master", "Draft 3 items from the Wandering Merchant in one run", "⚖️", false, 30),
        Achievement("booster_alchemist", "Elixir Draught", "Draft any 3-wave booster potion", "🧪", false, 15),
        Achievement("synergy_proc", "Elemental Fusion", "Trigger an Elemental Synergy in combat", "💥", false, 25),
        Achievement("synergy_master", "Elemental Catalyst", "Trigger 25 Elemental Reactions in a single game", "🔮", false, 35),
        Achievement("fusion_scholar", "Fusion Scholar", "Discover all 14 Elemental & Arcane Fusions", "📜", false, 50),
        Achievement("pact_survivor", "Devil's Bargain", "Survive 5 waves while bound to a High-Stakes Pact", "📜", false, 30),
        Achievement("greed_curse_diamonds", "Avarice Reward", "Earn bonus diamonds through the Curse of Greed", "😈", false, 25),
        Achievement("weather_thunder", "Lightning Rod", "Clear a Thunderstorm wave without losing Base HP", "🌩️", false, 25),
        Achievement("weather_bloodmoon", "Blood Moon Vanguard", "Survive an enraged wave during a Blood Moon", "🌕", false, 25),
        Achievement("weather_eclipse", "Solar Aegis", "Clear a Solar Eclipse wave", "🌑", false, 25),
        Achievement("hero_slayer_50", "Frontline Champion", "Slay 50 enemies directly with your hero in one run", "🗡️", false, 20),
        Achievement("hero_crits", "Lethal Strikes", "Land 15 critical strikes with the hero in one run", "🎯", false, 20),
        Achievement("iron_wall_5", "Iron Bastion", "Clear 5 consecutive waves without taking base damage", "🏰", false, 30),
        Achievement("combo_75", "Combo Overlord", "Reach a 75x kill combo", "🔥", false, 40),
        Achievement("combo_100", "Transcendent Combo", "Reach a 100x kill combo", "⚡", false, 60),
        Achievement("wave_75", "Abyssal Conqueror", "Reach wave 75 in any mode", "🔱", false, 50),
        Achievement("kills_2500", "Harbinger of Ruin", "Eliminate 2500 enemies in a single run", "💀", false, 50),
        Achievement("campaign_heroic_first", "Heroic Champion", "Clear a campaign mission on Heroic difficulty", "💀", false, 35),
        Achievement("boss_slayer_hero", "Regicide", "Land the final blow on a Boss with the Hero", "⚔️", false, 25)
    )
    // Track which powers were used this run for achievement
    val powersUsedThisRun = mutableSetOf<PowerType>()
    val abilityTowerTypesUsed = mutableSetOf<TowerType>()
    var trapsPlacedThisRun: Int = 0
    var baseHpBeforeWave: Float = 100f
    var newAchievement: Achievement? = null
    var achievementBannerTimer: Float = 0f

    var merchantPurchasesThisRun: Int = 0
    var wavesSurvivedWithPact: Int = 0
    var playerKillsThisRun: Int = 0
    var playerCritsThisRun: Int = 0
    var consecutiveFlawlessWaves: Int = 0
    var totalElementalReactionsThisRun: Int = 0

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

    // --- Relic systems ---
    val isZephyrUnlocked: Boolean get() = skillTree.isRelicUnlocked(RelicId.ZEPHYR_GREAVES)
    var baseShield: Float = 0f
    var aegisHealTimer: Float = 0f

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
    val bossInterval: Int get() = if (bossIntervalOverride > 0) bossIntervalOverride else if (isBossRush || campaignLevel?.id == 69) 1 else if (campaignLevel?.id == 13 || campaignLevel?.id == 26) 3 else 5
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
    var isHeroicMode: Boolean = false
    var towersSoldThisRun: Int = 0
    var towersPlacedThisRun: Int = 0
    val towersPlacedTypes = mutableSetOf<TowerType>()
    var baseDamageTakenThisRun: Float = 0f
    var heroKilledBoss: Boolean = false
    var campaignObjective1Met: Boolean = false
    var campaignObjective2Met: Boolean = false
    var campaignObjective3Met: Boolean = false

    fun evaluateCampaignObjective(obj: CampaignObjective): Boolean = when (obj.type) {
        ObjectiveType.SURVIVE_WAVES -> {
            val cl = campaignLevel
            cl != null && wave >= cl.targetWave
        }
        ObjectiveType.PERFECT_BASE -> baseDamageTakenThisRun <= 0f && baseHp >= maxBaseHp
        ObjectiveType.BASE_HP_ABOVE -> {
            val pct = (baseHp / maxBaseHp) * 100f
            pct >= obj.targetValue
        }
        ObjectiveType.NO_TOWERS_SOLD -> towersSoldThisRun == 0
        ObjectiveType.MAX_TOWERS_PLACED -> towersPlacedThisRun <= obj.targetValue
        ObjectiveType.FORBIDDEN_TOWER -> {
            val forbidden = obj.forbiddenTower
            forbidden == null || forbidden !in towersPlacedTypes
        }
        ObjectiveType.NO_POWERS_USED -> powersUsedThisRun.isEmpty()
        ObjectiveType.MIN_COMBO -> bestCombo >= obj.targetValue
        ObjectiveType.HERO_SLAYS_BOSS -> heroKilledBoss
    }

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

    // --- Wandering Merchant System ---
    val activePermanentMerchantItems = mutableSetOf<MerchantItemId>()
    val activeTemporaryMerchantItems = mutableMapOf<MerchantItemId, Int>()
    var merchantShopChoices: List<MerchantCard> = emptyList()
    var merchantShopPending: Boolean = false
    var greedCurseKillCount: Int = 0

    fun hasMerchantItem(id: MerchantItemId): Boolean {
        return activePermanentMerchantItems.contains(id) || (activeTemporaryMerchantItems[id] ?: 0) > 0
    }

    fun draftMerchantCard(card: MerchantCard): Boolean {
        if (gold < card.cost) return false
        gold -= card.cost

        when (card.tier) {
            MerchantTier.PERMANENT_SYNERGY -> {
                activePermanentMerchantItems.add(card.id)
            }
            MerchantTier.HIGH_STAKES_PACT -> {
                activePermanentMerchantItems.add(card.id)
                if (card.id == MerchantItemId.GLASS_CANNON) {
                    maxBaseHp = (maxBaseHp * 0.75f).coerceAtLeast(20f)
                    baseHp = baseHp.coerceAtMost(maxBaseHp)
                } else if (card.id == MerchantItemId.BLOOD_OFFERING) {
                    baseHp = (baseHp - 25f).coerceAtLeast(1f)
                    gold += 180
                    totalGoldEarned += 180
                }
            }
            MerchantTier.TEMPORARY_BOOSTER -> {
                activeTemporaryMerchantItems[card.id] = card.durationWaves
                if (card.id == MerchantItemId.FORTRESS_AEGIS) {
                    baseHp = (baseHp + 40f).coerceAtMost(maxBaseHp)
                }
            }
        }
        checkAchievement("merchant_first")
        merchantPurchasesThisRun++
        if (merchantPurchasesThisRun >= 3) checkAchievement("merchant_trio")
        if (card.tier == MerchantTier.TEMPORARY_BOOSTER) checkAchievement("booster_alchemist")
        audio.play(SfxType.REWARD_CHEST)
        merchantShopPending = false
        isPaused = false
        return true
    }

    fun skipMerchantShop() {
        merchantShopPending = false
        isPaused = false
    }

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

    fun applyCampaign(level: CampaignLevel, isHeroic: Boolean = false) {
        campaignLevel = level
        campaignVictory = false
        isHeroicMode = isHeroic
        var hpM = level.enemyHpMult
        var dmgM = level.enemyDmgMult
        var spdM = level.enemySpeedMult
        if (isHeroic) {
            hpM *= 1.35f
            dmgM *= 1.25f
            spdM *= 1.20f
        }
        enemyHpMult = hpM
        enemyDmgMult = dmgM
        enemySpeedMult = spdM
        goldMult = level.goldMult
        spawnRateMult = level.spawnRateMult
        gold = level.startingGold
        mapType = level.mapType
        // Glass cannon: reduced base HP
        if (level.id == 25 || level.id == 28) { maxBaseHp = 50f; baseHp = 50f }
        if (level.id == 43) { maxBaseHp = 10f; baseHp = 10f }

        // Mission Archetypes
        when (level.missionType) {
            MissionType.SUDDEN_DEATH -> {
                maxBaseHp = if (level.id == 43) 10f else if (level.id == 25 || level.id == 28) 50f else 1f
                baseHp = maxBaseHp
            }
            MissionType.BLITZ -> {
                waveDelay = 1.5f
                spawnRateMult *= 1.30f
                goldMult *= 1.20f
            }
            MissionType.BOSS_BOUNTY -> {
                bossIntervalOverride = 1
            }
            MissionType.LONE_CHAMPION -> {
                player.attackDamage *= 2.5f
                player.speed *= 1.3f
            }
            MissionType.GOLD_RUSH -> {
                goldMult *= 2.5f
            }
            MissionType.STANDARD -> {}
        }

        // Level-specific weather events
        when (level.id) {
            53 -> currentWeather = WeatherEvent.THUNDERSTORM
            62 -> currentWeather = WeatherEvent.SOLAR_ECLIPSE
            64 -> currentWeather = WeatherEvent.BLOOD_MOON
            else -> currentWeather = WeatherEvent.CLEAR
        }

        // Level 63: Lone Champion hero buff
        if (level.id == 63) {
            player.attackDamage *= 2.5f
            player.speed *= 1.3f
        }
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

        // Apply persistent skill tree bonuses (scaled with campaign progression to preserve tutorial integrity)
        val skillTreeFactor = when {
            campaignLevel == null -> 1f
            campaignLevel!!.id <= 3 -> 0f    // Pure tutorial for levels 1-3
            campaignLevel!!.id <= 6 -> 0.35f  // Gentle introduction
            campaignLevel!!.id <= 10 -> 0.65f // Moderate scaling
            campaignLevel!!.id <= 15 -> 0.85f // Advanced scaling
            else -> 1f                        // Full skill tree influence
        }
        gold += (skillTree.bonusStartGold() * skillTreeFactor).toInt()
        val hpBonus = skillTree.bonusBaseHp() * skillTreeFactor
        if (campaignLevel?.id != 43 && campaignLevel?.id != 25 && campaignLevel?.id != 28) {
            maxBaseHp += hpBonus
            baseHp = maxBaseHp
        }
        player.attackDamage += skillTree.bonusPlayerDamage() * skillTreeFactor
        player.speed += skillTree.bonusPlayerSpeed() * skillTreeFactor
        player.maxHp += skillTree.bonusPlayerHp() * skillTreeFactor
        player.hp = player.maxHp
        player.attackRange += skillTree.bonusAttackRange() * skillTreeFactor

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

        if (skillTree.isRelicUnlocked(RelicId.AEGIS_OF_DAWN)) {
            baseShield = 100f
        }
        
        generateNextWavePreview()
    }

    /** Deals damage to the player base, absorbing through baseShield first */
    fun damageBase(rawDamage: Float) {
        baseDamageTakenThisRun += rawDamage
        val aegisMult = if (hasMerchantItem(MerchantItemId.FORTRESS_AEGIS)) 0.80f else 1f
        var remainingDmg = rawDamage * aegisMult
        if (baseShield > 0f) {
            if (baseShield >= remainingDmg) {
                baseShield -= remainingDmg
                remainingDmg = 0f
                floatingTexts.add(FloatingText(baseX, baseY - 40f, "\uD83D\uDEE1\uFE0F SHIELD ABSORB!", 0xFF00E5FF.toInt(), 1f, 22f))
            } else {
                remainingDmg -= baseShield
                baseShield = 0f
                floatingTexts.add(FloatingText(baseX, baseY - 40f, "\uD83D\uDEE1\uFE0F SHIELD BROKEN!", 0xFFFF5722.toInt(), 1f, 22f))
            }
        }
        if (remainingDmg > 0f) {
            baseHp = (baseHp - remainingDmg).coerceAtLeast(0f)
        }
    }

    /** Called when a tower reaches level 5 and selects a specialization */
    fun onTowerSpecialized(tower: Tower) {
        checkAchievement("spec_first")
        val specCount = towers.count { it.specialization != TowerSpecialization.NONE }
        if (specCount >= 3) checkAchievement("spec_trio")
    }

    /** Build paths based on the selected map type — randomized each run */
    private fun generatePaths(w: Float, h: Float) {
        paths.clear()
        spawnPoints.clear()
        val bx = baseX
        val by = baseY
        val rng = java.util.Random()

        if (mapType == MapType.VOLCANO) {
            val vc = MapPathGenerator.getVolcanoCenter(w, h)
            volcanoCenterX = vc.first
            volcanoCenterY = vc.second
        }

        val generated = MapPathGenerator.generate(mapType, w, h, bx, by, rng)
        paths.addAll(generated)

        paths.forEach { path ->
            val sp = path.spawnPoint
            spawnPoints.add(Pair(sp.x, sp.y))
        }
    }

    var riverChecker: ((Float, Float) -> Boolean)? = null

    /** Check if a point is on/near water hazards — by default false unless configured */
    fun isPointOnRiver(x: Float, y: Float): Boolean = riverChecker?.invoke(x, y) ?: false


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
        val previewMod = if (campaignLevel != null) {
            val cl = campaignLevel!!
            cl.waveModifiers[nextWave] ?: (if (cl.defaultModifier != WaveModifier.NONE) cl.defaultModifier else if (nextWave >= 3 && Math.random() < 0.35) WaveModifier.entries.filter { it != WaveModifier.NONE }.random() else WaveModifier.NONE)
        } else if (isDailyChallenge && dailyChallengeModifiers.isNotEmpty()) {
            dailyChallengeModifiers[nextWave % dailyChallengeModifiers.size]
        } else if (nextWave >= 3 && !isDailyChallenge) {
            val mods = WaveModifier.entries.filter { it != WaveModifier.NONE }
            if (Math.random() < 0.4) mods.random() else WaveModifier.NONE
        } else WaveModifier.NONE

        val previewWeather = if (nextWave >= 4 && !isDailyChallenge) {
            when {
                nextWave % 6 == 0 -> WeatherEvent.BLOOD_MOON
                nextWave % 4 == 0 -> WeatherEvent.THUNDERSTORM
                nextWave % 5 == 0 -> WeatherEvent.SOLAR_ECLIPSE
                Math.random() < 0.25 -> listOf(WeatherEvent.BLOOD_MOON, WeatherEvent.THUNDERSTORM, WeatherEvent.SOLAR_ECLIPSE).random()
                else -> WeatherEvent.CLEAR
            }
        } else WeatherEvent.CLEAR

        if (nextWave % bossInterval == 0) {
            // Refill pool the same way startNextWave() would, so preview matches actual
            if (bossPool.isEmpty()) bossPool.addAll(BossType.entries.shuffled())
            val type = bossPool.first()
            nextWavePreview = WavePreview(true, emptyMap(), type, previewMod, previewWeather)
        } else {
            val enemyCounts = mutableMapOf<EnemyType, Int>()
            var count = ((3 + nextWave * 2) * spawnRateMult).toInt().coerceAtMost(100)
            if (previewMod == WaveModifier.SWARM) count *= 2
            repeat(count) {
                val type = when {
                    nextWave >= 18 && Math.random() < 0.06 -> EnemyType.SHAPESHIFTER
                    nextWave >= 16 && Math.random() < 0.07 -> EnemyType.TREANT
                    nextWave >= 15 && Math.random() < 0.07 -> EnemyType.COMMANDER
                    nextWave >= 13 && Math.random() < 0.08 -> EnemyType.NECROMANCER
                    nextWave >= 12 && Math.random() < 0.08 -> EnemyType.BERSERKER
                    nextWave >= 10 && Math.random() < 0.08 -> EnemyType.WISP
                    nextWave >= 9 && Math.random() < 0.08 -> EnemyType.SHADOW
                    nextWave >= 8 && Math.random() < 0.12 -> EnemyType.ARMORED_GOLEM
                    nextWave >= 7 && Math.random() < 0.12 -> EnemyType.MAGMA_CRAB
                    nextWave >= 7 && Math.random() < 0.15 -> EnemyType.DRAGON
                    nextWave >= 6 && Math.random() < 0.14 -> EnemyType.HARPY
                    nextWave >= 5 && Math.random() < 0.18 -> EnemyType.DEMON
                    nextWave >= 4 && Math.random() < 0.14 -> EnemyType.GHOST
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
            nextWavePreview = WavePreview(false, enemyCounts, modifier = previewMod, weather = previewWeather)
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

        if (showWeatherBanner) {
            weatherBannerTimer -= dt
            if (weatherBannerTimer <= 0f) showWeatherBanner = false
        }
        if (lightningFlashTimer > 0f) {
            lightningFlashTimer -= dt
            if (lightningFlashTimer <= 0f) {
                lightningFlashTimer = 0f
                lastLightningTarget = null
            }
        }

        // Heavenly lightning during Thunderstorm weather
        if (currentWeather.hasHeavenlyLightning && waveInProgress && enemies.isNotEmpty()) {
            weatherLightningTimer -= dt
            if (weatherLightningTimer <= 0f) {
                weatherLightningTimer = 3.0f + (Math.random() * 1.5).toFloat()
                val target = enemies.filter { !it.isDead() && !it.deathProcessed }.randomOrNull()
                if (target != null) {
                    val dmg = 140f + wave * 30f
                    lastLightningTarget = GamePoint(target.x, target.y)
                    lightningFlashTimer = 0.22f
                    shakeTimer = 0.25f
                    shakeIntensity = 7f
                    hapticPending = 1
                    audio.play(SfxType.WEATHER_THUNDER)

                    val splashRadius = 130f
                    enemies.forEach { e ->
                        if (!e.isDead() && !e.deathProcessed) {
                            val dx = e.x - target.x
                            val dy = e.y - target.y
                            val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                            if (dist <= splashRadius) {
                                val falloff = 1f - (dist / splashRadius) * 0.35f
                                e.hp -= dmg * falloff
                                e.hitFlash = 0.2f
                                e.deepFreezeTimer = 1.0f
                            }
                        }
                    }
                    floatingTexts.add(FloatingText(target.x, target.y - 20f, "⚡ LIGHTNING!", 0xFFFFD700.toInt(), 1.2f, 30f))
                }
            }
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
                consecutiveFlawlessWaves++
                if (consecutiveFlawlessWaves >= 5) checkAchievement("iron_wall_5")
            } else {
                consecutiveFlawlessWaves = 0
            }
            when (currentWeather) {
                WeatherEvent.THUNDERSTORM -> {
                    if (baseHp >= baseHpBeforeWave) checkAchievement("weather_thunder")
                }
                WeatherEvent.BLOOD_MOON -> {
                    checkAchievement("weather_bloodmoon")
                }
                WeatherEvent.SOLAR_ECLIPSE -> {
                    checkAchievement("weather_eclipse")
                }
                else -> {}
            }
            if (hasMerchantItem(MerchantItemId.GLASS_CANNON) || hasMerchantItem(MerchantItemId.GREED_CURSE)) {
                wavesSurvivedWithPact++
                if (wavesSurvivedWithPact >= 5) checkAchievement("pact_survivor")
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

            val midasBonusMult = if (hasMerchantItem(MerchantItemId.MIDAS_TONIC)) 1.40f else 1f
            val bonus = ((wave * 5 + skillTree.bonusWaveGold()) * goldMult * endlessBuffGold * midasBonusMult).toInt()
            gold += bonus
            totalGoldEarned += bonus
            floatingTexts.add(FloatingText(baseX, baseY - 80f, "+${bonus}g wave bonus!", 0xFFFFD700.toInt(), 1.5f, 32f))

            if (currentWeather.bonusDiamondsOnClear > 0) {
                val bonusDia = currentWeather.bonusDiamondsOnClear
                val currentDiamonds = prefs.getInt("diamonds", 0) + bonusDia
                prefs.edit().putInt("diamonds", currentDiamonds).apply()
                diamondsEarnedThisRun += bonusDia
                floatingTexts.add(FloatingText(baseX - 40f, baseY - 120f, "+$bonusDia 💎 Blood Moon Bounty!", 0xFF00E5FF.toInt(), 2.0f, 32f))
            }

            // Endless milestone buff trigger every 25 waves
            if (isEndlessMode && wave > 0 && wave % 25 == 0) {
                val available = EndlessBuff.entries.toMutableList()
                available.shuffle()
                endlessMilestoneChoices = available.take(3)
                endlessMilestonePending = true
                audio.play(SfxType.REWARD_CHEST)
                // Pause the game until player picks
                isPaused = true
            }

            // Wandering Merchant arrival every 3 waves
            if (wave > 0 && wave % 3 == 0 && !campaignVictory && !gameOver) {
                merchantShopChoices = MerchantCatalog.generateShop(wave, activePermanentMerchantItems)
                merchantShopPending = true
                isPaused = true
            }

            // Campaign victory check
            val cl = campaignLevel
            if (cl != null && wave >= cl.targetWave) {
                campaignVictory = true
                clearSave()
                saveRunHistory("Won")
                audio.play(SfxType.VICTORY)
                val editor = prefs.edit()
                editor.putBoolean("campaign_${cl.id}", true)

                // Dynamic tactical 3-star objectives
                campaignObjective1Met = evaluateCampaignObjective(cl.objective1)
                campaignObjective2Met = evaluateCampaignObjective(cl.objective2)
                campaignObjective3Met = evaluateCampaignObjective(cl.objective3)

                var stars = 0
                if (campaignObjective1Met) stars++
                if (campaignObjective2Met) stars++
                if (campaignObjective3Met) stars++
                if (stars < 1) stars = 1
                campaignStars = stars

                val prevStars = prefs.getInt("campaign_${cl.id}_stars", 0)
                if (stars > prevStars) {
                    editor.putInt("campaign_${cl.id}_stars", stars)
                }

                // Diamond reward: base + bonus per star
                val starBonus = (stars - 1) * 2  // 0, 2, or 4 bonus diamonds
                var totalDiamonds = cl.diamondReward + starBonus

                if (isHeroicMode) {
                    val heroicKey = "campaign_${cl.id}_heroic"
                    val alreadyHeroic = prefs.getBoolean(heroicKey, false)
                    editor.putBoolean(heroicKey, true)
                    val heroicBounty = if (!alreadyHeroic) 5 else 2
                    totalDiamonds += heroicBounty
                    checkAchievement("campaign_heroic_first")
                }

                skillTree.addDiamonds(totalDiamonds)
                diamondsEarnedThisRun += totalDiamonds

                // Campaign achievements
                val completed = CampaignData.levels.count { prefs.getBoolean("campaign_${it.id}", false) }
                if (completed >= 5) checkAchievement("campaign_5")
                if (completed >= 10) checkAchievement("campaign_10")
                if (completed >= CampaignData.levels.size) checkAchievement("campaign_all")
                if (baseDamageTakenThisRun <= 0f && baseHp >= maxBaseHp) {
                    checkAchievement("campaign_no_damage")
                    editor.putBoolean("campaign_${cl.id}_fullhp", true)
                }
                val threeStarLevels = CampaignData.levels.count {
                    prefs.getInt("campaign_${it.id}_stars", 0) >= 3
                } + if (stars >= 3 && prevStars < 3) 1 else 0
                if (threeStarLevels >= 5) checkAchievement("campaign_3star")
                if (threeStarLevels >= 10) checkAchievement("campaign_3star_10")
                editor.apply()
            } else {
                generateNextWavePreview()
            }
        }

        if (waveInProgress && enemiesRemaining > 0) {
            val maxConcurrent = 10 + (gameSpeed - 1) * 6
            val spawnChance = dt * (2.5f + (gameSpeed - 1) * 1.5f)
            if (enemies.size < maxConcurrent && Math.random() < spawnChance) {
                spawnEnemy()
                enemiesRemaining--
            }
        }

        val weatherCdr = if (currentWeather == WeatherEvent.SOLAR_ECLIPSE) 1.40f else 1f
        val cdrRate = (if (skillTree.isRelicUnlocked(RelicId.CHRONO_HOURGLASS)) 1.35f else 1f) * weatherCdr
        PowerType.entries.forEach { p ->
            val cd = powerCooldowns.getOrDefault(p, 0f)
            if (cd > 0) powerCooldowns[p] = (cd - dt * cdrRate).coerceAtLeast(0f)
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
        // Relic: Aegis of Dawn (heals base 3 HP every 5 seconds)
        if (skillTree.isRelicUnlocked(RelicId.AEGIS_OF_DAWN) && baseHp < maxBaseHp && baseHp > 0) {
            aegisHealTimer += dt
            if (aegisHealTimer >= 5f) {
                aegisHealTimer = 0f
                baseHp = (baseHp + 3f).coerceAtMost(maxBaseHp)
                floatingTexts.add(FloatingText(baseX, baseY - 30f, "\uD83D\uDEE1\uFE0F +3 HP", 0xFF69F0AE.toInt(), 1.0f, 20f))
            }
        }

        // Passive movement speed scaling (e.g. Zephyr Greaves, Alchemist's Elixir)
        val elixirHeroSpeed = if (hasMerchantItem(MerchantItemId.ALCHEMIST_ELIXIR)) 1.20f else 1f
        player.speedMultiplier = (if (isZephyrUnlocked) 1.35f else 1f) * elixirHeroSpeed

        if (player.hp > 0 && player.canAttack()) {
            val effectivePlayerRange = player.attackRange * (if (currentWaveModifier == WaveModifier.INVISIBLE) 0.7f else 1f)
            val hasQuiver = skillTree.isRelicUnlocked(RelicId.ARTEMIS_QUIVER)
            val hasChrono = skillTree.isRelicUnlocked(RelicId.CHRONO_HOURGLASS)
            val inRangeEnemies = enemies.filter { it.hp > 0 && it.distanceTo(player.x, player.y) < effectivePlayerRange }
            val targets = if (hasQuiver) {
                inRangeEnemies.sortedBy { it.distanceTo(player.x, player.y) }.take(3)
            } else {
                val nearest = inRangeEnemies.minByOrNull { it.distanceTo(player.x, player.y) }
                if (nearest != null) listOf(nearest) else emptyList()
            }

            if (targets.isNotEmpty()) {
                val heroCdMult = if (currentWeather.heroAttackSpeedMultiplier > 1f) (1f / currentWeather.heroAttackSpeedMultiplier) else 1f
                player.attack(heroCdMult)
                audio.play(SfxType.PLAYER_ATTACK)
                val glassDmgMult = if (hasMerchantItem(MerchantItemId.GLASS_CANNON)) 1.50f else 1f
                for (target in targets) {
                    val shieldRed = if (target.shieldTimer > 0) 0.3f else 1f
                    val phaseRed = if (target.isPhased) 0.25f else 1f
                    val superconductMult = if (target.superconductTimer > 0f) 1.25f else 1f
                    var actualPlayerDmg = player.attackDamage * shieldRed * phaseRed * glassDmgMult * superconductMult
                    val isCrit = (Math.random() < 0.15) || (Math.random() < critChance)
                    if (isCrit) {
                        actualPlayerDmg *= critMultiplier
                        audio.play(SfxType.HERO_SPECIAL)
                        playerCritsThisRun++
                        if (playerCritsThisRun >= 15) checkAchievement("hero_crits")
                        if (hasChrono) {
                            freezeTimer = 2.0f
                            floatingTexts.add(FloatingText(target.x, target.y - target.size - 20f, "⏳ CHRONO FREEZE!", 0xFF00E5FF.toInt(), 1f, 22f))
                        }
                    }
                    val prevHp = target.hp
                    target.hp -= actualPlayerDmg
                    if (prevHp > 0 && target.hp <= 0) {
                        playerKillsThisRun++
                        if (target.isBoss) {
                            heroKilledBoss = true
                            checkAchievement("boss_slayer_hero")
                        }
                        if (playerKillsThisRun >= 50) checkAchievement("hero_slayer_50")
                    }
                    target.hitFlash = 0.15f
                    projectiles.add(Projectile(player.x, player.y, target.x, target.y,
                        damage = 0f, color = if (isCrit) 0xFFFFD700.toInt() else 0xFF42A5F5.toInt()))
                    val textDmg = if (isCrit) "CRIT! -${actualPlayerDmg.toInt()}" else if (target.isPhased) "-${actualPlayerDmg.toInt()} (Phased)" else "-${actualPlayerDmg.toInt()}"
                    floatingTexts.add(FloatingText(target.x, target.y - target.size,
                        textDmg, if (isCrit) 0xFFFFD700.toInt() else 0xFF42A5F5.toInt(), 0.8f, if (isCrit) 26f else 22f))
                }
            }
        }

        val greedCurseSpeedMult = if (hasMerchantItem(MerchantItemId.GREED_CURSE)) 1.20f else 1f
        val speedMult = (if (freezeTimer > 0) 0.2f else 1f) * currentWeather.enemySpeedMultiplier * greedCurseSpeedMult
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
                    if (enemy.burnTimer > 0f || enemy.shockTimer > 0f || enemy.arcaneMarkTimer > 0f || enemy.necroMarkTimer > 0f) {
                        triggerElementalReaction(enemy, DamageType.ICE, tower, 20f + tower.level * 8f)
                    }
                    val slowPower = (0.5f - tower.level * 0.03f - skillTree.iceSlowBonus()).coerceAtLeast(0.05f)
                    enemy.iceSlowFactor = enemy.iceSlowFactor.coerceAtMost(slowPower)
                }
            }
        }
        // Zephyr Greaves Gale Aura: 25% slow to enemies within 180px of player
        if (isZephyrUnlocked && player.hp > 0) {
            val auraRadius = 180f
            enemies.forEach { enemy ->
                if (enemy.hp > 0 && enemy.distanceTo(player.x, player.y) <= auraRadius) {
                    enemy.iceSlowFactor = enemy.iceSlowFactor.coerceAtMost(0.75f)
                }
            }
            if (player.moveMagnitude > 0.05f && Math.random() < 0.15) {
                particles.add(Particle(
                    player.x + (Math.random().toFloat() - 0.5f) * 30f,
                    player.y + (Math.random().toFloat() - 0.5f) * 30f,
                    (Math.random().toFloat() - 0.5f) * 20f,
                    (Math.random().toFloat() - 0.5f) * 20f,
                    0.4f, 0x88A7F3D0.toInt(), 4f
                ))
            }
        }
        enemies.forEach { enemy ->
            // Skip enemies in death animation
            if (enemy.deathProcessed) return@forEach

            // Decrement elemental & fusion status timers
            if (enemy.shockTimer > 0) enemy.shockTimer -= dt
            if (enemy.arcaneMarkTimer > 0) enemy.arcaneMarkTimer -= dt
            if (enemy.superconductTimer > 0) enemy.superconductTimer -= dt
            if (enemy.necroMarkTimer > 0) enemy.necroMarkTimer -= dt
            if (enemy.bombardTimer > 0) enemy.bombardTimer -= dt
            if (enemy.solarBurnTimer > 0) enemy.solarBurnTimer -= dt
            if (enemy.conduitTimer > 0) enemy.conduitTimer -= dt
            if (enemy.brittleTimer > 0) enemy.brittleTimer -= dt
            if (enemy.enfeebleTimer > 0) enemy.enfeebleTimer -= dt
            if (enemy.silenceTimer > 0) enemy.silenceTimer -= dt
            if (enemy.astralDecayTimer > 0 && !enemy.isDead()) {
                enemy.astralDecayTimer -= dt
                enemy.hp -= 35f * dt
                if (Math.random() < 0.2) {
                    particles.add(Particle(enemy.x, enemy.y,
                        ((Math.random() - 0.5) * 20).toFloat(), -15f - Math.random().toFloat() * 15f,
                        0.3f, 0xFFCE93D8.toInt(), 3f))
                }
            }
            if (enemy.soulburnTimer > 0 && !enemy.isDead()) {
                enemy.soulburnTimer -= dt
                enemy.hp -= 45f * dt
                if (Math.random() < 0.2) {
                    particles.add(Particle(enemy.x, enemy.y,
                        ((Math.random() - 0.5) * 25).toFloat(), -20f - Math.random().toFloat() * 20f,
                        0.35f, 0xFFFF1744.toInt(), 3.5f))
                }
            }
            if (enemy.stunTimer > 0) {
                enemy.stunTimer -= dt
                if (Math.random() < 0.2) {
                    particles.add(Particle(enemy.x, enemy.y,
                        ((Math.random() - 0.5) * 30).toFloat(), ((Math.random() - 0.5) * 30).toFloat(),
                        0.3f, 0xFFFFEE58.toInt(), 3f))
                }
                if (enemy.hitFlash > 0) enemy.hitFlash -= dt
                return@forEach
            }
            // Regen from wave modifier (suppressed by Solar Burn and Frost Tomb)
            if (enemy.regenRate > 0 && enemy.hp < enemy.maxHp && enemy.solarBurnTimer <= 0f && enemy.brittleTimer <= 0f) {
                enemy.hp = (enemy.hp + enemy.regenRate * dt).coerceAtMost(enemy.maxHp)
            }
            // Burn DoT from Flame tower (Treant suffers 2x burn DPS)
            if (enemy.burnTimer > 0 && !enemy.isDead()) {
                enemy.burnTimer -= dt
                val burnMult = if (enemy.type == EnemyType.TREANT) 2.0f else 1.0f
                enemy.hp -= enemy.burnDps * burnMult * dt
                // Fire particles while burning
                if (Math.random() < 0.3) {
                    particles.add(Particle(enemy.x + (Math.random().toFloat() - 0.5f) * enemy.size,
                        enemy.y + (Math.random().toFloat() - 0.5f) * enemy.size,
                        (Math.random().toFloat() - 0.5f) * 30f, -40f - Math.random().toFloat() * 30f,
                        0.4f, 0xFFFF5722.toInt(), 3f))
                }
            }
            // Poison DoT
            if (enemy.poisonTimer > 0 && !enemy.isDead()) {
                enemy.poisonTimer -= dt
                val poisonMult = if (enemy.type == EnemyType.MAGMA_CRAB || enemy.type == EnemyType.TREANT) 0.5f else 1.0f
                enemy.hp -= enemy.poisonDps * poisonMult * dt
                if (Math.random() < 0.25) {
                    particles.add(Particle(
                        enemy.x + (Math.random().toFloat() - 0.5f) * enemy.size,
                        enemy.y + (Math.random().toFloat() - 0.5f) * enemy.size,
                        (Math.random().toFloat() - 0.5f) * 20f, -20f - Math.random().toFloat() * 20f,
                        0.4f, 0xFF00E676.toInt(), 3f
                    ))
                }
            }
            // Elite ability effects (silenced by EMP Shockwave)
            if (enemy.isElite && !enemy.isDead() && enemy.silenceTimer <= 0f) {
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
            // Necromancer: chants and reanimates skeleton minions every 7s
            if (enemy.type == EnemyType.NECROMANCER && !enemy.isDead()) {
                enemy.abilityCooldownTimer -= dt
                if (enemy.abilityCooldownTimer <= 0f) {
                    enemy.abilityCooldownTimer = 7f
                    val waveScale = 1f + (wave - 1) * 0.15f
                    val mHp = (20f + wave * 2f) * waveScale * enemyHpMult
                    repeat(2) {
                        enemies.add(Enemy(
                            x = enemy.x + ((Math.random() - 0.5) * 40).toFloat(),
                            y = enemy.y + ((Math.random() - 0.5) * 40).toFloat(),
                            speed = 90f * enemySpeedMult,
                            hp = mHp, maxHp = mHp,
                            goldReward = (1 + wave / 5).coerceAtLeast(1),
                            damage = 6f * waveScale,
                            type = EnemyType.MINI_SKELETON,
                            size = 20f,
                            pathIndex = enemy.pathIndex,
                            waypointIndex = enemy.waypointIndex
                        ))
                    }
                    totalEnemiesThisWave += 2
                    floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size, "💀 ARISE!", 0xFFBA68C8.toInt(), 1f, 22f))
                    repeat(8) {
                        val angle = Math.random() * Math.PI * 2
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * 60).toFloat(), (Math.sin(angle) * 60).toFloat(),
                            0.5f, 0xFF7B1FA2.toInt(), 5f))
                    }
                }
            }
            // Ghost: periodically enters ethereal phase (intangible, leaves wisps)
            if (enemy.type == EnemyType.GHOST && !enemy.isDead()) {
                enemy.phaseTimer -= dt
                if (enemy.phaseTimer <= 0f) {
                    enemy.isPhased = !enemy.isPhased
                    enemy.phaseTimer = if (enemy.isPhased) 2.2f else 3.5f
                    if (enemy.isPhased) {
                        floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size, "👻 ETHEREAL!", 0xFF80DEEA.toInt(), 0.8f, 18f))
                    }
                }
                if (enemy.isPhased && Math.random() < 0.25) {
                    particles.add(Particle(enemy.x, enemy.y,
                        ((Math.random() - 0.5) * 20).toFloat(), -(Math.random() * 30 + 10).toFloat(),
                        0.4f, 0xFF80DEEA.toInt(), 4f))
                }
            }
            // Harpy: high-pitch screech disrupting nearby tower attack speeds every 6s
            if (enemy.type == EnemyType.HARPY && !enemy.isDead()) {
                enemy.abilityCooldownTimer -= dt
                if (enemy.abilityCooldownTimer <= 0f) {
                    enemy.abilityCooldownTimer = 6f
                    floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size, "🦅 SCREECH!", 0xFFB39DDB.toInt(), 0.9f, 20f))
                    towers.filter { it.distanceTo(enemy.x, enemy.y) < 180f }.forEach { t ->
                        t.fireTimer = (t.fireTimer + 0.8f).coerceAtMost(2.0f)
                    }
                    repeat(6) {
                        val angle = Math.random() * Math.PI * 2
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * 80).toFloat(), (Math.sin(angle) * 80).toFloat(),
                            0.4f, 0xFFD1C4E9.toInt(), 4f))
                    }
                }
            }
            // Treant: passive wood bark regeneration (8 HP/s), suppressed while burning, solar burned, or brittle
            if (enemy.type == EnemyType.TREANT && !enemy.isDead()) {
                if (enemy.burnTimer <= 0f && enemy.solarBurnTimer <= 0f && enemy.brittleTimer <= 0f && enemy.hp < enemy.maxHp) {
                    enemy.hp = (enemy.hp + 8f * dt).coerceAtMost(enemy.maxHp)
                    if (Math.random() < 0.1) {
                        particles.add(Particle(enemy.x, enemy.y, 0f, -15f, 0.4f, 0xFF81C784.toInt(), 4f))
                    }
                }
            }
            // Magma Crab: emits scorch embers
            if (enemy.type == EnemyType.MAGMA_CRAB && !enemy.isDead()) {
                if (Math.random() < 0.15) {
                    particles.add(Particle(enemy.x, enemy.y,
                        ((Math.random() - 0.5) * 20).toFloat(), ((Math.random() - 0.5) * 20).toFloat(),
                        0.4f, 0xFFFF5722.toInt(), 4f))
                }
            }
            val chargeBoost = if (enemy.isCharging) 3f else 1f
            val roarBoost = enemy.roarSpeedBoost
            val iceSlow = enemy.iceSlowFactor
            val enfeebleMult = if (enemy.enfeebleTimer > 0f) 0.5f else 1.0f

            // Check for blockade collision — enemy stops and attacks it
            val nearestBlockade = blockades.filter { !it.isDead() }
                .minByOrNull { it.distanceTo(enemy.x, enemy.y) }
            val blockedByBlockade = nearestBlockade != null && nearestBlockade.distanceTo(enemy.x, enemy.y) < nearestBlockade.size + enemy.size
            if (blockedByBlockade) {
                // Attack the blockade instead of moving
                nearestBlockade!!.hp -= enemy.damage * 0.3f * dt * enfeebleMult  // Constant DPS
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
                damageBase(enemy.damage * currentWeather.enemyDamageMultiplier * enfeebleMult)
                audio.play(SfxType.BASE_HIT)
                hapticPending = 1  // light haptic on base damage
                enemy.reachedBase = true
                enemy.hp = 0f  // Remove enemy after dealing damage once
            }
        }

        // Boss special abilities with telegraphs & health-phase transitions
        val bossAbilityEnemies = mutableListOf<Pair<Enemy, BossAbility>>()
        enemies.forEach { enemy ->
            if (enemy.type == EnemyType.BOSS && enemy.bossType != null && !enemy.isDead()) {
                val bt = enemy.bossType!!
                // 1. Tick telegraph if currently charging
                if (enemy.isTelegraphing) {
                    enemy.telegraphTimer -= dt
                    if (enemy.telegraphTimer <= 0f) {
                        enemy.isTelegraphing = false
                        val ability = enemy.pendingAbility ?: bt.ability
                        bossAbilityEnemies.add(enemy to ability)
                    }
                } else {
                    // 2. Health-based Phase Transitions (75%, 50%, 25%)
                    val hpRatio = enemy.hp / enemy.maxHp
                    if (!enemy.phase75Triggered && hpRatio <= 0.75f) {
                        enemy.phase75Triggered = true
                        enemy.currentPhase = 2
                        audio.play(SfxType.BOSS_SHIELD)
                        shakeTimer = 0.3f; shakeIntensity = 8f
                        floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 15f, "🛡️ PHASE 2: BARRIER!", 0xFF00E5FF.toInt(), 1.5f, 26f))
                        startBossTelegraph(enemy, BossAbility.SHIELD)
                    } else if (!enemy.phase50Triggered && hpRatio <= 0.50f) {
                        enemy.phase50Triggered = true
                        enemy.currentPhase = 3
                        audio.play(SfxType.BOSS_ROAR)
                        shakeTimer = 0.35f; shakeIntensity = 10f
                        floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 15f, "👾 PHASE 3: SWARM!", 0xFFFF9800.toInt(), 1.5f, 26f))
                        startBossTelegraph(enemy, BossAbility.SUMMON)
                    } else if (!enemy.phase25Triggered && hpRatio <= 0.25f) {
                        enemy.phase25Triggered = true
                        enemy.currentPhase = 4
                        audio.play(SfxType.BOSS_AOE)
                        shakeTimer = 0.45f; shakeIntensity = 14f
                        floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 15f, "💥 FINAL PHASE: TITAN STOMP!", 0xFFFF1744.toInt(), 1.8f, 28f))
                        startBossTelegraph(enemy, BossAbility.TITAN_STOMP)
                    } else {
                        // 3. Periodic Ability with Telegraph
                        enemy.bossAbilityTimer -= dt
                        if (enemy.isCharging) {
                            enemy.chargeTimer -= dt
                            if (enemy.chargeTimer <= 0f) enemy.isCharging = false
                        }
                        if (enemy.bossAbilityTimer <= 0f) {
                            enemy.bossAbilityTimer = enemy.bossAbilityCooldown
                            startBossTelegraph(enemy, bt.ability)
                        }
                    }
                }
            }
        }
        for ((boss, ability) in bossAbilityEnemies) {
            executeBossAbility(boss, dt, ability)
        }

        val deadEnemies = enemies.filter { it.isDead() && !it.deathProcessed }
        deadEnemies.forEach { enemy ->
            // Void Phoenix Rebirth mechanic
            if (enemy.bossType == BossType.VOID_PHOENIX && !enemy.hasReborn && !enemy.reachedBase) {
                enemy.hasReborn = true
                enemy.hp = enemy.maxHp * 0.45f
                floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size, "\uD83D\uDD25 REBIRTH!", 0xFFFF1744.toInt(), 1.5f, 32f))
                audio.play(SfxType.BOSS_SUMMON)
                shakeTimer = 0.4f; shakeIntensity = 12f
                repeat(25) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(enemy.x, enemy.y,
                        (Math.cos(angle) * 180).toFloat(), (Math.sin(angle) * 180).toFloat(),
                        0.7f, 0xFFFF1744.toInt(), 6f))
                }
                return@forEach
            }
            enemy.deathProcessed = true
            enemy.deathAnimTimer = 0.4f  // 400ms shrink+fade animation
            // Enemies that reached the base are removed but give no rewards
            if (enemy.reachedBase) return@forEach

            audio.play(SfxType.ENEMY_DIE)
            val relicGoldMult = if (skillTree.isRelicUnlocked(RelicId.MIDAS_CRUCIBLE)) 1.30f else 1.0f
            val weatherGoldMult = currentWeather.goldMultiplier
            val greedGoldMult = if (hasMerchantItem(MerchantItemId.GREED_CURSE)) 2.5f else 1.0f
            val comboGold = (enemy.goldReward * comboMultiplier * skillTree.goldBonusMultiplier() * relicGoldMult * weatherGoldMult * greedGoldMult).toInt()
            val baseGold = (enemy.goldReward * weatherGoldMult * greedGoldMult).toInt()
            gold += comboGold
            totalGoldEarned += comboGold
            score += comboGold
            scoreFromKills += baseGold
            scoreFromCombos += (comboGold - baseGold).coerceAtLeast(0)
            totalKills++

            if (hasMerchantItem(MerchantItemId.GREED_CURSE)) {
                greedCurseKillCount++
                if (greedCurseKillCount % 20 == 0) {
                    val currentDiamonds = prefs.getInt("diamonds", 0) + 1
                    prefs.edit().putInt("diamonds", currentDiamonds).apply()
                    diamondsEarnedThisRun += 1
                    checkAchievement("greed_curse_diamonds")
                    floatingTexts.add(FloatingText(enemy.x, enemy.y - 30f, "+1 💎 Greed Bonus!", 0xFF00E5FF.toInt(), 1.5f, 26f))
                }
            }

            // Soul Conduit proc: dying near Vortex tower
            if (hasMerchantItem(MerchantItemId.SOUL_CONDUIT)) {
                val nearVortex = towers.find { it.type == TowerType.VORTEX && it.distanceTo(enemy.x, enemy.y) <= 200f }
                if (nearVortex != null) {
                    checkAchievement("synergy_proc")
                    val soulPulseDmg = 120f
                    val pulseRadius = 160f
                    val caught = enemies.filter { it.hp > 0 && it != enemy && it.distanceTo(enemy.x, enemy.y) <= pulseRadius }
                    for (ce in caught) {
                        ce.hp -= soulPulseDmg
                        ce.hitFlash = 0.2f
                        val pdx = enemy.x - ce.x
                        val pdy = enemy.y - ce.y
                        val pdist = kotlin.math.hypot(pdx, pdy)
                        if (pdist > 5f) {
                            ce.x += (pdx / pdist) * 25f
                            ce.y += (pdy / pdist) * 25f
                        }
                    }
                    floatingTexts.add(FloatingText(enemy.x, enemy.y - 20f, "🌀 SOUL PULSE!", 0xFFB388FF.toInt(), 1.2f, 26f))
                    repeat(12) {
                        val a = Math.random() * Math.PI * 2
                        val spd = 50f + Math.random().toFloat() * 70f
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                            0.6f, 0xFFAB47BC.toInt(), 5f))
                    }
                }
            }

            // Hellfire: soul detonate into vengeful phantom wisp
            if (enemy.soulburnTimer > 0f) {
                val soulTarget = enemies.filter { it.hp > 0 && it != enemy && !it.isDead() && it.distanceTo(enemy.x, enemy.y) <= 200f }
                    .minByOrNull { it.distanceTo(enemy.x, enemy.y) }
                if (soulTarget != null) {
                    soulTarget.hp -= 150f
                    soulTarget.hitFlash = 0.3f
                    enemy.lastHitTower?.let {
                        soulTarget.lastHitTower = it
                        it.totalDamageDealt += 150f
                    }
                    floatingTexts.add(FloatingText(soulTarget.x, soulTarget.y - soulTarget.size, "💀 SOUL EXPLOSION! -150", 0xFFFF1744.toInt(), 1.2f, 22f))
                    repeat(12) {
                        val a = Math.random() * Math.PI * 2
                        val spd = 60f + Math.random().toFloat() * 60f
                        particles.add(Particle(soulTarget.x, soulTarget.y, (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(), 0.5f, 0xFFFF1744.toInt(), 4f))
                    }
                }
            }

            // Astral Decay: +50% bonus gold and void wisp
            if (enemy.astralDecayTimer > 0f) {
                val bonusGold = (comboGold * 0.5f).toInt().coerceAtLeast(1)
                gold += bonusGold
                totalGoldEarned += bonusGold
                floatingTexts.add(FloatingText(enemy.x, enemy.y - 45f, "+${bonusGold}g ASTRAL HARVEST! ✨", 0xFFBA68C8.toInt(), 1.2f, 22f))
                val voidTarget = enemies.filter { it.hp > 0 && it != enemy && !it.isDead() && it.distanceTo(enemy.x, enemy.y) <= 220f }
                    .minByOrNull { it.distanceTo(enemy.x, enemy.y) }
                if (voidTarget != null) {
                    voidTarget.hp -= 90f
                    voidTarget.hitFlash = 0.25f
                    enemy.lastHitTower?.let {
                        voidTarget.lastHitTower = it
                        it.totalDamageDealt += 90f
                    }
                    floatingTexts.add(FloatingText(voidTarget.x, voidTarget.y - voidTarget.size, "✨ VOID WISP! -90", 0xFFCE93D8.toInt(), 1.0f, 20f))
                    repeat(8) {
                        val a = Math.random() * Math.PI * 2
                        val spd = 50f + Math.random().toFloat() * 40f
                        particles.add(Particle(voidTarget.x, voidTarget.y, (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(), 0.4f, 0xFFE1BEE7.toInt(), 4f))
                    }
                }
            }

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
                EnemyType.NECROMANCER -> {
                    // Swirling dark violet soul energy
                    repeat(12) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 60f + (Math.random() * 80f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp - 30f).toFloat(),
                            0.9f, 0xFF9C27B0.toInt(), 5f))
                    }
                }
                EnemyType.GHOST -> {
                    // Dissipating cyan spirit vapor
                    repeat(14) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 40f + (Math.random() * 50f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp - 20f).toFloat(),
                            1.1f, 0xFF80DEEA.toInt(), 5f))
                    }
                }
                EnemyType.MAGMA_CRAB -> {
                    // Molten rock fragments and fiery smoke
                    repeat(10) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 70f + (Math.random() * 90f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            0.8f, 0xFFFF3D00.toInt(), 6f))
                    }
                }
                EnemyType.HARPY -> {
                    // Plumage feathers drifting downward
                    repeat(10) {
                        val ox = (Math.random().toFloat() - 0.5f) * 35f
                        particles.add(Particle(enemy.x + ox, enemy.y,
                            (Math.random().toFloat() - 0.5f) * 40f, 25f + Math.random().toFloat() * 30f,
                            1.2f, 0xFF9575CD.toInt(), 4f))
                    }
                }
                EnemyType.TREANT -> {
                    // Wood splinters and mossy leaves
                    repeat(14) {
                        val angle = Math.random() * Math.PI * 2
                        val sp = 60f + (Math.random() * 70f).toFloat()
                        particles.add(Particle(enemy.x, enemy.y,
                            (Math.cos(angle) * sp).toFloat(), (Math.sin(angle) * sp).toFloat(),
                            1.0f, if (Math.random() < 0.5) 0xFF4E342E.toInt() else 0xFF4CAF50.toInt(), 6f))
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
                when (enemy.bossType) {
                    BossType.STORM_LEVIATHAN -> checkAchievement("boss_leviathan")
                    BossType.VOID_PHOENIX -> checkAchievement("boss_phoenix")
                    BossType.SPORE_OVERLORD -> checkAchievement("boss_spore")
                    BossType.CHRONO_LICH -> checkAchievement("boss_chrono")
                    BossType.IRON_DREADNOUGHT -> checkAchievement("boss_dreadnought")
                    else -> {}
                }
                bossesKilledThisRun++
                trackBountyBossKill()
                if (bossesKilledThisRun >= 5) checkAchievement("5_bosses")
                if (bossesKilledThisRun >= 10) checkAchievement("10_bosses")
                if (isBossRush && bossesKilledThisRun >= 5) checkAchievement("boss_rush_5")
                if (isBossRush && bossesKilledThisRun >= 10) checkAchievement("boss_rush_10")
                currentBoss = null
                // Boss always drops diamonds
                val midasMult = if (skillTree.isRelicUnlocked(RelicId.MIDAS_CRUCIBLE)) 1.5f else 1.0f
                val diamondDrop = ((2 + wave / 5) * midasMult).toInt().coerceAtMost(15)
                skillTree.addDiamonds(diamondDrop)
                diamondsEarnedThisRun += diamondDrop
                audio.play(SfxType.DIAMOND_DROP)
                floatingTexts.add(FloatingText(enemy.x, enemy.y - enemy.size - 20f,
                    "+${diamondDrop} \uD83D\uDC8E", 0xFF00E5FF.toInt(), 1.5f, 30f))
            } else {
                // Regular enemies have a small diamond drop chance
                val midasChanceBonus = if (skillTree.isRelicUnlocked(RelicId.MIDAS_CRUCIBLE)) 0.03f else 0f
                val dropChance = 0.03f + skillTree.diamondDropBonus() + midasChanceBonus
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
            if (totalKills >= 2500) checkAchievement("kills_2500")
            if (comboCount >= 10) checkAchievement("combo_10")
            if (comboCount >= 20) checkAchievement("combo_20")
            if (comboCount >= 30) checkAchievement("combo_30")
            if (comboCount >= 50) checkAchievement("combo_50")
            if (comboCount >= 75) checkAchievement("combo_75")
            if (comboCount >= 100) checkAchievement("combo_100")
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

        // === FIRE PATCHES (Napalm Conflagration) ===
        val patchIter = activeFirePatches.iterator()
        while (patchIter.hasNext()) {
            val patch = patchIter.next()
            patch.duration -= dt
            if (patch.duration <= 0f) {
                patchIter.remove()
                continue
            }
            for (enemy in enemies) {
                if (enemy.isDead() || enemy.deathProcessed) continue
                if (enemy.distanceTo(patch.x, patch.y) <= patch.radius) {
                    val pDmg = patch.dps * dt
                    enemy.hp -= pDmg
                    if (patch.sourceTower != null) {
                        enemy.lastHitTower = patch.sourceTower
                        patch.sourceTower.totalDamageDealt += pDmg
                    }
                    if (Math.random() < 0.12) {
                        particles.add(Particle(
                            enemy.x + (Math.random().toFloat() - 0.5f) * enemy.size,
                            enemy.y + (Math.random().toFloat() - 0.5f) * enemy.size,
                            (Math.random().toFloat() - 0.5f) * 20f,
                            -25f - Math.random().toFloat() * 20f,
                            0.3f, 0xFFFF5722.toInt(), 3f
                        ))
                    }
                }
            }
            if (Math.random() < 0.25) {
                val ang = Math.random() * Math.PI * 2
                val rad = Math.random().toFloat() * patch.radius * 0.8f
                particles.add(Particle(
                    patch.x + (Math.cos(ang) * rad).toFloat(),
                    patch.y + (Math.sin(ang) * rad).toFloat(),
                    (Math.random().toFloat() - 0.5f) * 15f,
                    -20f - Math.random().toFloat() * 25f,
                    0.4f, 0xFFFF7043.toInt(), 3.5f
                ))
            }
        }

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
            val elixirSpeedMult = if (hasMerchantItem(MerchantItemId.ALCHEMIST_ELIXIR)) 1.35f else 1f
            tower.update(dt * elixirSpeedMult)
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
                    val glassMult = if (hasMerchantItem(MerchantItemId.GLASS_CANNON)) 1.50f else 1f
                    val superconductMult = if (target.superconductTimer > 0f) 1.25f else 1f
                    val brittleMult = if (target.brittleTimer > 0f && (tower.type.damageType == DamageType.PHYSICAL || tower.type.damageType == DamageType.EXPLOSIVE)) 1.4f else 1f
                    val astralMult = if (target.astralDecayTimer > 0f) 1.2f else 1f
                    var dmg = tower.damage * towerDmgMult * resistMult * synergyMult * critMult * shieldMult * glassMult * superconductMult * brittleMult * astralMult

                    // Necro execute: massive bonus damage to low-HP enemies
                    if (tower.type == TowerType.NECRO && target.hp < target.maxHp * 0.15f) {
                        dmg *= 3f
                        floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "EXECUTE!", 0xFF9C27B0.toInt(), 1f, 22f))
                    }

                    // --- TOWER SPECIALIZATIONS ---
                    when (tower.specialization) {
                        TowerSpecialization.SNIPER -> {
                            if (Math.random() < 0.25 || (target.type != EnemyType.BOSS && target.hp < target.maxHp * 0.20f)) {
                                dmg *= 2.5f
                                floatingTexts.add(FloatingText(target.x, target.y - target.size - 12f, "HEADSHOT! 🎯", 0xFFFFD700.toInt(), 1f, 22f))
                            }
                        }
                        TowerSpecialization.RANGER -> {
                            val otherTargets = inRange.filter { it != target }.take(2)
                            for (other in otherTargets) {
                                val volleyDmg = dmg * 0.70f
                                other.hp -= volleyDmg
                                other.hitFlash = 0.15f
                                tower.totalDamageDealt += volleyDmg
                                other.lastHitTower = tower
                                projectiles.add(Projectile(tower.x, tower.y, other.x, other.y, speed = 800f, damage = volleyDmg, size = 6f, color = 0xFFFFD700.toInt()))
                            }
                        }
                        TowerSpecialization.CLUSTER_MORTAR -> {
                            val nearbyEnemies = enemies.filter { it.hp > 0 && it != target && it.distanceTo(target.x, target.y) < 90f }
                            for (near in nearbyEnemies) {
                                val splash = dmg * 0.40f
                                near.hp -= splash
                                near.hitFlash = 0.15f
                                tower.totalDamageDealt += splash
                                near.lastHitTower = tower
                            }
                            repeat(6) {
                                particles.add(Particle(target.x + (Math.random().toFloat() - 0.5f) * 60f, target.y + (Math.random().toFloat() - 0.5f) * 60f, (Math.random().toFloat() - 0.5f) * 60f, (Math.random().toFloat() - 0.5f) * 60f, 0.4f, 0xFFFF7043.toInt(), 5f))
                            }
                        }
                        TowerSpecialization.RAILGUN -> {
                            val lineEnemies = enemies.filter { it.hp > 0 && it != target && it.distanceTo(tower.x, tower.y) < tower.range }
                            for (pe in lineEnemies) {
                                val lDx = target.x - tower.x
                                val lDy = target.y - tower.y
                                val lLenSq = lDx * lDx + lDy * lDy
                                if (lLenSq > 10f) {
                                    val t = (((pe.x - tower.x) * lDx + (pe.y - tower.y) * lDy) / lLenSq).coerceIn(0f, 1f)
                                    val px = tower.x + t * lDx
                                    val py = tower.y + t * lDy
                                    val distToBeam = Math.sqrt(((pe.x - px) * (pe.x - px) + (pe.y - py) * (pe.y - py)).toDouble()).toFloat()
                                    if (distToBeam < 35f) {
                                        val pierceDmg = dmg * 0.65f
                                        pe.hp -= pierceDmg
                                        pe.hitFlash = 0.15f
                                        tower.totalDamageDealt += pierceDmg
                                        pe.lastHitTower = tower
                                    }
                                }
                            }
                        }
                        TowerSpecialization.RIFT_WARP -> {
                            val pathIdx = target.pathIndex
                            if (pathIdx in paths.indices) {
                                val wps = paths[pathIdx].waypoints
                                if (target.waypointIndex > 1) {
                                    val prevWp = wps[target.waypointIndex - 1]
                                    target.x = (target.x + prevWp.x) / 2f
                                    target.y = (target.y + prevWp.y) / 2f
                                    floatingTexts.add(FloatingText(target.x, target.y - target.size - 10f, "WARP! 🌀", 0xFFAB47BC.toInt(), 0.8f, 20f))
                                }
                            }
                        }
                        else -> {}
                    }

                    target.hp -= dmg
                    target.hitFlash = 0.15f
                    tower.totalDamageDealt += dmg
                    target.lastHitTower = tower
                    if (target.conduitTimer > 0f) {
                        echoConduitDamage(target, dmg, tower)
                    }
                    if (tower.type == TowerType.FLAME) {
                        val reacted = triggerElementalReaction(target, DamageType.FIRE, tower, dmg)
                        if (!reacted) {
                            target.burnTimer = 3f
                            target.burnDps = tower.damage * towerDmgMult * 0.3f  // 30% of damage as DPS for 3s
                        }
                    }

                    if (tower.type == TowerType.POISON) {
                        val reacted = triggerElementalReaction(target, DamageType.POISON, tower, dmg)
                        if (!reacted) {
                            target.poisonTimer = 4f
                            target.poisonDps = tower.damage * towerDmgMult * 0.35f
                        }
                    }

                    if (tower.type == TowerType.TESLA) {
                        val reacted = triggerElementalReaction(target, DamageType.ELECTRIC, tower, dmg)
                        if (!reacted) {
                            target.shockTimer = 3.5f
                        }
                    }

                    if (tower.type == TowerType.MAGIC || tower.type == TowerType.VORTEX) {
                        val reacted = triggerElementalReaction(target, DamageType.MAGIC, tower, dmg)
                        if (!reacted) {
                            target.arcaneMarkTimer = 3.5f
                        }
                    }

                    if (tower.type == TowerType.NECRO) {
                        val reacted = triggerElementalReaction(target, DamageType.DARK, tower, dmg)
                        if (!reacted) {
                            target.necroMarkTimer = 3.5f
                        }
                    }

                    if (tower.type == TowerType.CANNON) {
                        val reacted = triggerElementalReaction(target, DamageType.EXPLOSIVE, tower, dmg)
                        if (!reacted) {
                            target.bombardTimer = 3.5f
                        }
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
            clearSave()
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

    fun triggerElementalReaction(
        target: Enemy,
        incomingElement: DamageType,
        sourceTower: Tower? = null,
        baseDamage: Float = 0f
    ): Boolean {
        if (target.isDead()) return false

        // 1. Steam Burst (FIRE + ICE)
        if ((incomingElement == DamageType.FIRE && (target.iceSlowFactor < 0.95f || target.deepFreezeTimer > 0f || freezeTimer > 0f)) ||
            (incomingElement == DamageType.ICE && target.burnTimer > 0f)
        ) {
            val hasBonus = hasMerchantItem(MerchantItemId.THERMAL_SHOCK)
            val dmgMult = if (hasBonus) 1.6f else 1.0f
            val aoeRadius = if (hasBonus) 160f else 130f
            val burstDmg = (120f + baseDamage * 0.75f) * dmgMult

            target.burnTimer = 0f
            target.deepFreezeTimer = 0f
            target.iceSlowFactor = 1f

            val aoeEnemies = enemies.filter { it.hp > 0 && it.distanceTo(target.x, target.y) <= aoeRadius }
            for (ae in aoeEnemies) {
                ae.hp -= burstDmg
                ae.hitFlash = 0.2f
                if (sourceTower != null) {
                    ae.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += burstDmg
                }
                if (ae != target) {
                    ae.iceSlowFactor = ae.iceSlowFactor.coerceAtMost(0.4f)
                }
            }
            val floatMsg = if (hasBonus) "💥 SHATTER STEAM!" else "💥 STEAM BURST!"
            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, floatMsg, 0xFF00E5FF.toInt(), 1.2f, 26f))
            audio.play(SfxType.TOWER_UPGRADE)
            repeat(14) {
                val a = Math.random() * Math.PI * 2
                val spd = 40f + Math.random().toFloat() * 70f
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.6f, 0xFFE0F7FA.toInt(), 5f))
            }
            onElementalReactionTriggered("steam_burst")
            return true
        }

        // 2. Volatile Detonation (FIRE + POISON)
        if ((incomingElement == DamageType.FIRE && target.poisonTimer > 0f) ||
            (incomingElement == DamageType.POISON && target.burnTimer > 0f)
        ) {
            val remainingPoison = target.poisonTimer * target.poisonDps
            val detDmg = 110f + remainingPoison * 1.5f + baseDamage * 0.5f
            val aoeRadius = 120f

            target.poisonTimer = 0f
            target.burnTimer = 0f

            val aoeEnemies = enemies.filter { it.hp > 0 && it.distanceTo(target.x, target.y) <= aoeRadius }
            for (ae in aoeEnemies) {
                ae.hp -= detDmg
                ae.hitFlash = 0.25f
                if (sourceTower != null) {
                    ae.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += detDmg
                }
            }
            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "☣️ DETONATION!", 0xFFFF7043.toInt(), 1.2f, 26f))
            audio.play(SfxType.TOWER_UPGRADE)
            repeat(16) {
                val a = Math.random() * Math.PI * 2
                val spd = 60f + Math.random().toFloat() * 80f
                val col = if (it % 2 == 0) 0xFFFF5722.toInt() else 0xFF76FF03.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.6f, col, 5f))
            }
            onElementalReactionTriggered("volatile_detonation")
            return true
        }

        // 3. Superconductor (ELECTRIC + ICE)
        if ((incomingElement == DamageType.ELECTRIC && (target.iceSlowFactor < 0.95f || target.deepFreezeTimer > 0f || freezeTimer > 0f)) ||
            (incomingElement == DamageType.ICE && target.shockTimer > 0f)
        ) {
            val chainDmg = 65f + baseDamage * 0.4f
            target.shockTimer = 0f
            target.deepFreezeTimer = 0f
            target.iceSlowFactor = 1f
            target.superconductTimer = 4.0f

            val chainTargets = enemies.filter { it.hp > 0 && it != target && it.distanceTo(target.x, target.y) <= 150f }.take(3)
            for (ce in chainTargets) {
                ce.superconductTimer = 4.0f
                ce.hp -= chainDmg
                ce.hitFlash = 0.2f
                if (sourceTower != null) {
                    ce.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += chainDmg
                }
                repeat(5) {
                    particles.add(Particle(ce.x, ce.y,
                        ((Math.random() - 0.5) * 60).toFloat(), ((Math.random() - 0.5) * 60).toFloat(),
                        0.4f, 0xFF80D8FF.toInt(), 4f))
                }
            }
            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "⚡ SUPERCONDUCT!", 0xFF40C4FF.toInt(), 1.2f, 26f))
            audio.play(SfxType.TOWER_UPGRADE)
            repeat(12) {
                val a = Math.random() * Math.PI * 2
                val spd = 70f + Math.random().toFloat() * 70f
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.5f, 0xFF00E5FF.toInt(), 4f))
            }
            onElementalReactionTriggered("superconductor")
            return true
        }

        // 4. Corrosive Shock (ELECTRIC + POISON)
        if ((incomingElement == DamageType.ELECTRIC && target.poisonTimer > 0f) ||
            (incomingElement == DamageType.POISON && target.shockTimer > 0f)
        ) {
            val hasBonus = hasMerchantItem(MerchantItemId.NEUROTOXIN_CHAIN)
            val dmgMult = if (hasBonus) 1.6f else 1.0f
            val burstRadius = if (hasBonus) 180f else 140f
            val shockDmg = (75f + baseDamage * 0.45f) * dmgMult

            target.shockTimer = 0f
            target.stunTimer = target.stunTimer.coerceAtLeast(0.6f)

            val nearby = enemies.filter { it.hp > 0 && it != target && it.distanceTo(target.x, target.y) <= burstRadius }
            for (ne in nearby) {
                ne.poisonTimer = 4f
                ne.poisonDps = ne.poisonDps.coerceAtLeast(target.poisonDps.coerceAtLeast(15f))
                ne.hp -= shockDmg
                ne.hitFlash = 0.2f
                ne.stunTimer = ne.stunTimer.coerceAtLeast(0.4f)
                if (sourceTower != null) {
                    ne.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += shockDmg
                }
            }
            val floatMsg = if (hasBonus) "⚡ TOXIN BURST CORROSION!" else "🧪 CORROSIVE SHOCK!"
            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, floatMsg, 0xFF00E676.toInt(), 1.2f, 26f))
            audio.play(SfxType.TOWER_UPGRADE)
            repeat(12) {
                val a = Math.random() * Math.PI * 2
                val spd = 50f + Math.random().toFloat() * 60f
                val col = if (it % 2 == 0) 0xFF00E676.toInt() else 0xFFFFEE58.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.5f, col, 5f))
            }
            onElementalReactionTriggered("corrosive_shock")
            return true
        }

        // 5. Solar Flare (MAGIC + FIRE)
        if ((incomingElement == DamageType.MAGIC && target.burnTimer > 0f) ||
            (incomingElement == DamageType.FIRE && target.arcaneMarkTimer > 0f)
        ) {
            val baseBurst = 140f + baseDamage * 0.6f
            val aoeRadius = 140f

            target.burnTimer = 0f
            target.arcaneMarkTimer = 0f
            target.solarBurnTimer = 3.5f

            val aoeEnemies = enemies.filter { it.hp > 0 && it.distanceTo(target.x, target.y) <= aoeRadius }
            for (ae in aoeEnemies) {
                val isShielded = ae.shieldTimer > 0f
                if (isShielded) ae.shieldTimer = 0f
                val effectiveDmg = if (isShielded) baseBurst * 1.5f else baseBurst
                ae.hp -= effectiveDmg
                ae.hitFlash = 0.3f
                ae.solarBurnTimer = 3.5f
                if (sourceTower != null) {
                    ae.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += effectiveDmg
                }
            }
            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "☀️ SOLAR FLARE!", 0xFFFFD54F.toInt(), 1.3f, 28f))
            audio.play(SfxType.POWER_FIREBALL)
            repeat(18) {
                val a = Math.random() * Math.PI * 2
                val spd = 60f + Math.random().toFloat() * 80f
                val col = if (it % 2 == 0) 0xFFFFD54F.toInt() else 0xFFFF5722.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.6f, col, 5f))
            }
            onElementalReactionTriggered("solar_flare")
            return true
        }

        // 6. Glacial Singularity (MAGIC + ICE)
        if ((incomingElement == DamageType.MAGIC && (target.iceSlowFactor < 0.95f || target.deepFreezeTimer > 0f || freezeTimer > 0f)) ||
            (incomingElement == DamageType.ICE && target.arcaneMarkTimer > 0f)
        ) {
            val singDmg = 125f + baseDamage * 0.5f
            val pullRadius = 150f

            target.arcaneMarkTimer = 0f
            target.deepFreezeTimer = 0f
            target.iceSlowFactor = 0.1f
            target.stunTimer = target.stunTimer.coerceAtLeast(1.2f)

            val nearby = enemies.filter { it.hp > 0 && it.distanceTo(target.x, target.y) <= pullRadius }
            for (ne in nearby) {
                ne.hp -= singDmg
                ne.hitFlash = 0.25f
                ne.stunTimer = ne.stunTimer.coerceAtLeast(1.2f)
                ne.iceSlowFactor = ne.iceSlowFactor.coerceAtMost(0.1f)
                if (sourceTower != null) {
                    ne.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += singDmg
                }
                if (ne != target) {
                    val dx = target.x - ne.x
                    val dy = target.y - ne.y
                    val d = kotlin.math.hypot(dx, dy)
                    if (d > 10f) {
                        val pullDist = 45f
                        ne.x += (dx / d) * pullDist
                        ne.y += (dy / d) * pullDist
                    }
                }
            }
            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "🌌 GLACIAL SINGULARITY!", 0xFF80D8FF.toInt(), 1.3f, 28f))
            audio.play(SfxType.TOWER_UPGRADE)
            repeat(20) {
                val a = Math.random() * Math.PI * 2
                val spd = 70f + Math.random().toFloat() * 70f
                val col = if (it % 2 == 0) 0xFF00E5FF.toInt() else 0xFFBA68C8.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.6f, col, 5f))
            }
            onElementalReactionTriggered("glacial_singularity")
            return true
        }

        // 7. Overload Flux (MAGIC + ELECTRIC)
        if ((incomingElement == DamageType.MAGIC && target.shockTimer > 0f) ||
            (incomingElement == DamageType.ELECTRIC && target.arcaneMarkTimer > 0f)
        ) {
            val fluxDmg = 100f + baseDamage * 0.5f
            val linkRadius = 160f

            target.arcaneMarkTimer = 0f
            target.shockTimer = 0f
            target.conduitTimer = 4.0f
            target.hp -= fluxDmg
            target.hitFlash = 0.25f
            if (sourceTower != null) {
                target.lastHitTower = sourceTower
                sourceTower.totalDamageDealt += fluxDmg
            }

            val linkTargets = enemies.filter { it.hp > 0 && it != target && it.distanceTo(target.x, target.y) <= linkRadius }.take(4)
            for (lt in linkTargets) {
                lt.conduitTimer = 4.0f
                lt.hitFlash = 0.2f
                repeat(4) {
                    particles.add(Particle(lt.x, lt.y,
                        ((Math.random() - 0.5) * 50).toFloat(), ((Math.random() - 0.5) * 50).toFloat(),
                        0.4f, 0xFFE040FB.toInt(), 4f))
                }
            }
            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "🔮 OVERLOAD FLUX!", 0xFFE040FB.toInt(), 1.3f, 28f))
            audio.play(SfxType.TESLA_FIRE)
            repeat(16) {
                val a = Math.random() * Math.PI * 2
                val spd = 60f + Math.random().toFloat() * 70f
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.5f, 0xFFE040FB.toInt(), 5f))
            }
            onElementalReactionTriggered("overload_flux")
            return true
        }

        // 8. Astral Decay (MAGIC + POISON)
        if ((incomingElement == DamageType.MAGIC && target.poisonTimer > 0f) ||
            (incomingElement == DamageType.POISON && target.arcaneMarkTimer > 0f)
        ) {
            val burstDmg = 90f + baseDamage * 0.4f
            target.poisonTimer = 0f
            target.arcaneMarkTimer = 0f
            target.astralDecayTimer = 4.0f
            target.hp -= burstDmg
            target.hitFlash = 0.25f
            if (sourceTower != null) {
                target.lastHitTower = sourceTower
                sourceTower.totalDamageDealt += burstDmg
            }

            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "✨ ASTRAL DECAY!", 0xFFCE93D8.toInt(), 1.3f, 28f))
            audio.play(SfxType.MAGIC_FIRE)
            repeat(14) {
                val a = Math.random() * Math.PI * 2
                val spd = 50f + Math.random().toFloat() * 60f
                val col = if (it % 2 == 0) 0xFFCE93D8.toInt() else 0xFF66BB6A.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.5f, col, 4.5f))
            }
            onElementalReactionTriggered("astral_decay")
            return true
        }

        // 9. Hellfire (DARK + FIRE)
        if ((incomingElement == DamageType.DARK && target.burnTimer > 0f) ||
            (incomingElement == DamageType.FIRE && target.necroMarkTimer > 0f)
        ) {
            val hellDmg = 130f + baseDamage * 0.6f
            target.burnTimer = 0f
            target.necroMarkTimer = 0f
            target.soulburnTimer = 3.5f
            target.hp -= hellDmg
            target.hitFlash = 0.3f
            if (sourceTower != null) {
                target.lastHitTower = sourceTower
                sourceTower.totalDamageDealt += hellDmg
            }

            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "💀 HELLFIRE!", 0xFFFF1744.toInt(), 1.3f, 28f))
            audio.play(SfxType.NECRO_FIRE)
            repeat(16) {
                val a = Math.random() * Math.PI * 2
                val spd = 60f + Math.random().toFloat() * 80f
                val col = if (it % 2 == 0) 0xFFFF1744.toInt() else 0xFF9C27B0.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.6f, col, 5f))
            }
            onElementalReactionTriggered("hellfire")
            return true
        }

        // 10. Frost Tomb (DARK + ICE)
        if ((incomingElement == DamageType.DARK && (target.iceSlowFactor < 0.95f || target.deepFreezeTimer > 0f || freezeTimer > 0f)) ||
            (incomingElement == DamageType.ICE && target.necroMarkTimer > 0f)
        ) {
            val tombDmg = 110f + baseDamage * 0.5f
            target.necroMarkTimer = 0f
            target.deepFreezeTimer = 0f
            target.iceSlowFactor = 1f
            target.stunTimer = target.stunTimer.coerceAtLeast(2.0f)
            target.brittleTimer = 4.0f
            target.hp -= tombDmg
            target.hitFlash = 0.25f
            if (sourceTower != null) {
                target.lastHitTower = sourceTower
                sourceTower.totalDamageDealt += tombDmg
            }

            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "❄️ FROST TOMB!", 0xFF80DEEA.toInt(), 1.3f, 28f))
            audio.play(SfxType.ICE_FIRE)
            repeat(14) {
                val a = Math.random() * Math.PI * 2
                val spd = 50f + Math.random().toFloat() * 60f
                val col = if (it % 2 == 0) 0xFF80DEEA.toInt() else 0xFF7B1FA2.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.5f, col, 4.5f))
            }
            onElementalReactionTriggered("frost_tomb")
            return true
        }

        // 11. Shadow Surge (DARK + ELECTRIC)
        if ((incomingElement == DamageType.DARK && target.shockTimer > 0f) ||
            (incomingElement == DamageType.ELECTRIC && target.necroMarkTimer > 0f)
        ) {
            val surgeDmg = 95f + baseDamage * 0.5f
            target.necroMarkTimer = 0f
            target.shockTimer = 0f
            target.stunTimer = target.stunTimer.coerceAtLeast(0.5f)
            target.enfeebleTimer = 5.0f
            target.hp -= surgeDmg
            target.hitFlash = 0.25f
            if (sourceTower != null) {
                target.lastHitTower = sourceTower
                sourceTower.totalDamageDealt += surgeDmg
            }

            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "👻 SHADOW SURGE!", 0xFF7C4DFF.toInt(), 1.3f, 28f))
            audio.play(SfxType.NECRO_FIRE)
            repeat(14) {
                val a = Math.random() * Math.PI * 2
                val spd = 60f + Math.random().toFloat() * 60f
                val col = if (it % 2 == 0) 0xFF7C4DFF.toInt() else 0xFF00E5FF.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.5f, col, 4.5f))
            }
            onElementalReactionTriggered("shadow_surge")
            return true
        }

        // 12. Corpse Miasma (DARK + POISON)
        if ((incomingElement == DamageType.DARK && target.poisonTimer > 0f) ||
            (incomingElement == DamageType.POISON && target.necroMarkTimer > 0f)
        ) {
            val miasmaRadius = 140f
            val hpBonusDmg = target.maxHp * 0.035f
            val baseMiasmaDmg = 85f + baseDamage * 0.4f + hpBonusDmg

            target.poisonTimer = 0f
            target.necroMarkTimer = 0f

            val caught = enemies.filter { it.hp > 0 && it.distanceTo(target.x, target.y) <= miasmaRadius }
            for (ce in caught) {
                ce.hp -= baseMiasmaDmg
                ce.hitFlash = 0.2f
                ce.iceSlowFactor = ce.iceSlowFactor.coerceAtMost(0.65f)
                if (sourceTower != null) {
                    ce.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += baseMiasmaDmg
                }
            }

            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "☠️ CORPSE MIASMA!", 0xFF69F0AE.toInt(), 1.3f, 28f))
            audio.play(SfxType.POISON_FIRE)
            repeat(16) {
                val a = Math.random() * Math.PI * 2
                val spd = 40f + Math.random().toFloat() * 70f
                val col = if (it % 2 == 0) 0xFF69F0AE.toInt() else 0xFF4A148C.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.6f, col, 5f))
            }
            onElementalReactionTriggered("corpse_miasma")
            return true
        }

        // 13. Napalm Conflagration (EXPLOSIVE + FIRE)
        if ((incomingElement == DamageType.EXPLOSIVE && target.burnTimer > 0f) ||
            (incomingElement == DamageType.FIRE && target.bombardTimer > 0f)
        ) {
            val blastDmg = 150f + baseDamage * 0.6f
            val blastRadius = 150f

            target.burnTimer = 0f
            target.bombardTimer = 0f

            val hitEnemies = enemies.filter { it.hp > 0 && it.distanceTo(target.x, target.y) <= blastRadius }
            for (he in hitEnemies) {
                he.hp -= blastDmg
                he.hitFlash = 0.3f
                if (sourceTower != null) {
                    he.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += blastDmg
                }
            }

            // Spawn ground FirePatch for 3.0s dealing 40 DPS
            activeFirePatches.add(FirePatch(target.x, target.y, radius = 90f, duration = 3.0f, dps = 40f, sourceTower = sourceTower))
            shakeTimer = 0.2f
            shakeIntensity = 6f

            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "🔥 CONFLAGRATION!", 0xFFFF3D00.toInt(), 1.3f, 28f))
            audio.play(SfxType.POWER_FIREBALL)
            repeat(20) {
                val a = Math.random() * Math.PI * 2
                val spd = 70f + Math.random().toFloat() * 90f
                val col = if (it % 2 == 0) 0xFFFF3D00.toInt() else 0xFFFFAB00.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.6f, col, 5f))
            }
            onElementalReactionTriggered("napalm_conflagration")
            return true
        }

        // 14. EMP Shockwave (EXPLOSIVE + ELECTRIC)
        if ((incomingElement == DamageType.EXPLOSIVE && target.shockTimer > 0f) ||
            (incomingElement == DamageType.ELECTRIC && target.bombardTimer > 0f)
        ) {
            val empDmg = 95f + baseDamage * 0.5f
            val empRadius = 140f

            target.shockTimer = 0f
            target.bombardTimer = 0f

            val hitEnemies = enemies.filter { it.hp > 0 && it.distanceTo(target.x, target.y) <= empRadius }
            for (he in hitEnemies) {
                he.hp -= empDmg
                he.hitFlash = 0.25f
                he.shieldTimer = 0f
                he.silenceTimer = 2.5f
                he.isTelegraphing = false
                he.telegraphTimer = 0f
                if (sourceTower != null) {
                    he.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += empDmg
                }
            }

            floatingTexts.add(FloatingText(target.x, target.y - target.size - 15f, "⚡ EMP SHOCKWAVE!", 0xFF00E5FF.toInt(), 1.3f, 28f))
            audio.play(SfxType.TESLA_FIRE)
            repeat(18) {
                val a = Math.random() * Math.PI * 2
                val spd = 60f + Math.random().toFloat() * 80f
                val col = if (it % 2 == 0) 0xFF00E5FF.toInt() else 0xFFFFFFFF.toInt()
                particles.add(Particle(target.x, target.y,
                    (Math.cos(a) * spd).toFloat(), (Math.sin(a) * spd).toFloat(),
                    0.5f, col, 4.5f))
            }
            onElementalReactionTriggered("emp_shockwave")
            return true
        }

        return false
    }

    fun echoConduitDamage(sourceEnemy: Enemy, rawDmg: Float, sourceTower: Tower? = null) {
        if (isEchoingConduit || rawDmg <= 0f) return
        val echoDmg = rawDmg * 0.35f
        if (echoDmg < 1f) return
        isEchoingConduit = true
        try {
            val linked = enemies.filter { it.hp > 0 && it != sourceEnemy && it.conduitTimer > 0f }
            for (le in linked) {
                le.hp -= echoDmg
                le.hitFlash = 0.15f
                if (sourceTower != null) {
                    le.lastHitTower = sourceTower
                    sourceTower.totalDamageDealt += echoDmg
                }
                repeat(2) {
                    particles.add(Particle(le.x, le.y,
                        ((Math.random() - 0.5) * 40).toFloat(), ((Math.random() - 0.5) * 40).toFloat(),
                        0.3f, 0xFFE040FB.toInt(), 3f))
                }
            }
        } finally {
            isEchoingConduit = false
        }
    }

    private fun onElementalReactionTriggered(fusionId: String = "") {
        totalElementalReactionsThisRun++
        if (fusionId.isNotBlank()) {
            recordFusionDiscovery(fusionId)
        }
        checkAchievement("synergy_proc")
        if (totalElementalReactionsThisRun >= 25) {
            checkAchievement("synergy_master")
        }
    }


    fun recordFusionDiscovery(fusionId: String) = synchronized(lock) {
        if (discoveredFusions.add(fusionId)) {
            prefs.edit().putString("discovered_fusions", discoveredFusions.joinToString(",")).apply()
            floatingTexts.add(FloatingText(baseX, baseY - 120f, "📖 NEW FUSION DISCOVERED!", 0xFFFFD700.toInt(), 1.8f, 28f))
        }
        if (discoveredFusions.size >= FusionCatalog.allFusions.size) {
            checkAchievement("fusion_scholar")
        }
    }

    val currentGoldMult: Float get() = goldMult

    fun getEarlyWaveBounty(): Int { synchronized(lock) {
        if (gameOver || campaignVictory || waveInProgress || enemies.isNotEmpty()) return 0
        return ((waveTimer.coerceAtLeast(1f) * 6f + wave * 2f) * goldMult).toInt().coerceAtLeast(15)
    } }

    fun callNextWaveEarly(): Int { synchronized(lock) {
        if (gameOver || campaignVictory) return 0
        if (!waveInProgress && enemies.isEmpty()) {
            val bounty = getEarlyWaveBounty()
            gold += bounty
            totalGoldEarned += bounty
            floatingTexts.add(FloatingText(baseX, baseY - 90f, "+${bounty}g RUSH BONUS! ⚡", 0xFFFFD700.toInt(), 1.6f, 34f))
            audio.play(SfxType.REWARD_CHEST)
            waveTimer = 0f
            startNextWave()
            return bounty
        }
        return 0
    } }

    private fun startNextWave() {
        wave++
        waveInProgress = true
        showWaveBanner = true
        waveBannerTimer = 1.5f
        eliteSpawnedThisWave = false

        // Decrement temporary merchant boosters
        val expiredMerchantItems = mutableListOf<MerchantItemId>()
        activeTemporaryMerchantItems.forEach { (id, remaining) ->
            if (remaining <= 1) {
                expiredMerchantItems.add(id)
            } else {
                activeTemporaryMerchantItems[id] = remaining - 1
            }
        }
        expiredMerchantItems.forEach { activeTemporaryMerchantItems.remove(it) }

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
        if (wave >= 75) checkAchievement("wave_75")
        if (wave >= 100) checkAchievement("wave_100")
        if (isEndlessMode && wave >= 10) checkAchievement("endless_10")
        if (isEndlessMode && wave >= 25) checkAchievement("endless_25")
        if (isEndlessMode && wave >= 50) checkAchievement("endless_50")
        if (skillTree.isRelicUnlocked(RelicId.AEGIS_OF_DAWN)) {
            baseShield = 100f
        }
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

        // Use pre-determined modifier and weather from preview (so preview matches reality)
        val preview = nextWavePreview
        currentWaveModifier = preview?.modifier ?: (campaignLevel?.waveModifiers?.get(wave) ?: campaignLevel?.defaultModifier ?: WaveModifier.NONE)
        if (campaignLevel?.id !in setOf(53, 62, 64)) {
            currentWeather = preview?.weather ?: WeatherEvent.CLEAR
        }
        weatherLightningTimer = 2.5f
        if (currentWeather != WeatherEvent.CLEAR) {
            showWeatherBanner = true
            weatherBannerTimer = 3.5f
            when (currentWeather) {
                WeatherEvent.BLOOD_MOON -> audio.play(SfxType.WEATHER_BLOOD_MOON)
                WeatherEvent.THUNDERSTORM -> audio.play(SfxType.WEATHER_THUNDER)
                WeatherEvent.SOLAR_ECLIPSE -> audio.play(SfxType.WEATHER_ECLIPSE)
                else -> {}
            }
        }

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
            val greedHpMult = if (hasMerchantItem(MerchantItemId.GREED_CURSE)) 1.15f else 1f
            val hp = (boss.baseHp + wave * 40f) * waveScale * enemyHpMult * bossRushScale * greedHpMult
            enemies.add(Enemy(
                x = spawn.x, y = spawn.y,
                speed = boss.baseSpeed * enemySpeedMult,
                hp = hp, maxHp = hp,
                goldReward = ((boss.baseGold + wave * 10f) * waveScale * goldMult).toInt().coerceAtLeast(1),
                damage = boss.baseDmg * waveScale * enemyDmgMult * bossRushScale,
                type = EnemyType.BOSS, bossType = boss, size = 55f,
                pathIndex = pathIdx,
                bossAbilityTimer = 5f,
                bossAbilityCooldown = when (boss.ability) {
                    BossAbility.SHIELD -> 12f
                    BossAbility.EMP_BLAST -> 9f
                    BossAbility.TIME_WARP -> 11f
                    BossAbility.SPORE_CLOUD -> 8f
                    BossAbility.BARRAGE -> 7f
                    else -> 6f
                }
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
            wave >= 16 && Math.random() < 0.07 -> EnemyType.TREANT
            wave >= 15 && Math.random() < 0.07 -> EnemyType.COMMANDER
            wave >= 13 && Math.random() < 0.08 -> EnemyType.NECROMANCER
            wave >= 12 && Math.random() < 0.08 -> EnemyType.BERSERKER
            wave >= 10 && Math.random() < 0.08 -> EnemyType.WISP
            wave >= 9 && Math.random() < 0.08 -> EnemyType.SHADOW
            wave >= 8 && Math.random() < 0.12 -> EnemyType.ARMORED_GOLEM
            wave >= 7 && Math.random() < 0.12 -> EnemyType.MAGMA_CRAB
            wave >= 7 && Math.random() < 0.15 -> EnemyType.DRAGON
            wave >= 6 && Math.random() < 0.14 -> EnemyType.HARPY
            wave >= 5 && Math.random() < 0.18 -> EnemyType.DEMON
            wave >= 4 && Math.random() < 0.14 -> EnemyType.GHOST
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
            EnemyType.NECROMANCER -> arrayOf(75f, 60f, 18f, 14f)
            EnemyType.GHOST -> arrayOf(35f, 95f, 9f, 8f)
            EnemyType.MAGMA_CRAB -> arrayOf(90f, 45f, 12f, 12f)
            EnemyType.HARPY -> arrayOf(38f, 125f, 11f, 10f)
            EnemyType.TREANT -> arrayOf(160f, 35f, 22f, 22f)
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

        val greedHpMult = if (hasMerchantItem(MerchantItemId.GREED_CURSE)) 1.15f else 1f
        val hp = baseHpVal * waveScale * enemyHpMult * hpMod * nightHpMultiplier * greedHpMult
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

    fun startBossTelegraph(boss: Enemy, ability: BossAbility) {
        val duration = if (isHeroicMode) 1.2f else 1.5f
        boss.isTelegraphing = true
        boss.telegraphTimer = duration
        boss.telegraphDuration = duration
        boss.pendingAbility = ability
        boss.telegraphRadius = when (ability) {
            BossAbility.TITAN_STOMP, BossAbility.QUAKE, BossAbility.AOE_DAMAGE -> 220f
            BossAbility.SPORE_CLOUD -> 240f
            BossAbility.EMP_BLAST -> 200f
            else -> 180f
        }
        audio.play(SfxType.BOSS_CHARGE)
        floatingTexts.add(FloatingText(boss.x, boss.y - boss.size - 12f, "⚠️ CHARGING!", 0xFFFFD700.toInt(), 1.0f, 22f))
    }

    fun executeBossAbility(boss: Enemy, dt: Float, ability: BossAbility = boss.pendingAbility ?: boss.bossType?.ability ?: BossAbility.AOE_DAMAGE) {
        val bt = boss.bossType ?: return
        when (ability) {
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
                    damageBase(15f)
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
                // Stun and slow all towers
                towers.forEach {
                    it.stunTimer = (it.stunTimer + 1.5f).coerceAtLeast(1.5f)
                    it.fireTimer += 1.5f
                }
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
            BossAbility.EMP_BLAST -> {
                val inRange = towers.sortedBy { it.distanceTo(boss.x, boss.y) }.take(4)
                inRange.forEach { it.fireTimer += 3.0f }
                enemies.filter { it.type == EnemyType.HARPY || it.type == EnemyType.BAT }.forEach {
                    it.roarSpeedBoost = 1.6f
                    it.roarBoostTimer = 4f
                }
                audio.play(SfxType.TESLA_FIRE)
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\u26A1 EMP BLAST!", bt.color, 1.2f, 30f))
                shakeTimer = 0.2f; shakeIntensity = 8f
                repeat(18) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(boss.x, boss.y,
                        (Math.cos(angle) * 140).toFloat(), (Math.sin(angle) * 140).toFloat(),
                        0.6f, bt.color, 5f))
                }
            }
            BossAbility.REBIRTH -> {
                // Active Solar Flare while alive
                towers.filter { it.distanceTo(boss.x, boss.y) < 200f }.forEach { it.fireTimer += 1.5f }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83D\uDD25 SOLAR FLARE!", bt.color, 1.2f, 30f))
                audio.play(SfxType.POWER_FIREBALL)
                repeat(16) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(boss.x, boss.y,
                        (Math.cos(angle) * 120).toFloat(), (Math.sin(angle) * 120).toFloat(),
                        0.6f, bt.color, 6f))
                }
            }
            BossAbility.SPORE_CLOUD -> {
                towers.filter { it.distanceTo(boss.x, boss.y) < 240f }.forEach { it.thornJamTimer = 3.5f }
                if (player.distanceTo(boss.x, boss.y) < 220f) {
                    player.hp = (player.hp - 12f).coerceAtLeast(0f)
                    floatingTexts.add(FloatingText(player.x, player.y - 30f, "-12 POISON", 0xFF00E676.toInt(), 1f, 22f))
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83C\uDF44 SPORE CLOUD!", bt.color, 1.2f, 30f))
                audio.play(SfxType.BOSS_ROAR)
                repeat(20) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * 220f
                    particles.add(Particle(
                        boss.x + (Math.cos(angle) * dist).toFloat(),
                        boss.y + (Math.sin(angle) * dist).toFloat(),
                        0f, -20f, 0.8f, 0xFF00E676.toInt(), 5f))
                }
            }
            BossAbility.TIME_WARP -> {
                enemies.filter { it != boss && !it.isDead() }.forEach { minion ->
                    val path = paths.getOrNull(minion.pathIndex)
                    if (path != null && minion.waypointIndex > 1) {
                        minion.waypointIndex = (minion.waypointIndex - 2).coerceAtLeast(1)
                        val wp = path.waypoints[minion.waypointIndex]
                        minion.x = wp.x; minion.y = wp.y
                    }
                    minion.hp = (minion.hp + minion.maxHp * 0.15f).coerceAtMost(minion.maxHp)
                    minion.hitFlash = 0.3f
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\u23F3 TIME WARP!", bt.color, 1.3f, 30f))
                audio.play(SfxType.BOSS_TELEPORT)
                repeat(18) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(boss.x, boss.y,
                        (Math.cos(angle) * 110).toFloat(), (Math.sin(angle) * 110).toFloat(),
                        0.7f, bt.color, 5f))
                }
            }
            BossAbility.BARRAGE -> {
                val targetBlockade = blockades.minByOrNull { it.distanceTo(boss.x, boss.y) }
                if (targetBlockade != null) {
                    targetBlockade.hp -= 40f
                    floatingTexts.add(FloatingText(targetBlockade.x, targetBlockade.y - 20f, "-40 HP", 0xFFFF9100.toInt(), 1f, 24f))
                }
                if (player.distanceTo(boss.x, boss.y) < 300f) {
                    player.hp = (player.hp - 15f).coerceAtLeast(0f)
                    floatingTexts.add(FloatingText(player.x, player.y - 30f, "-15 BARRAGE", 0xFFFF9100.toInt(), 1f, 24f))
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "\uD83E\uDD16 BARRAGE!", bt.color, 1.2f, 30f))
                audio.play(SfxType.CANNON_FIRE)
                shakeTimer = 0.3f; shakeIntensity = 10f
                repeat(16) {
                    val angle = Math.random() * Math.PI * 2
                    particles.add(Particle(boss.x, boss.y,
                        (Math.cos(angle) * 160).toFloat(), (Math.sin(angle) * 160).toFloat(),
                        0.6f, 0xFFFF9100.toInt(), 6f))
                }
            }
            BossAbility.TITAN_STOMP -> {
                val stompRange = 220f
                towers.filter { it.distanceTo(boss.x, boss.y) <= stompRange }.forEach { tower ->
                    tower.stunTimer = 3.0f
                    floatingTexts.add(FloatingText(tower.x, tower.y - 25f, "💫 STUNNED!", 0xFFFFD700.toInt(), 1.2f, 22f))
                }
                if (player.distanceTo(boss.x, boss.y) <= stompRange) {
                    player.hp = (player.hp - 20f).coerceAtLeast(0f)
                    floatingTexts.add(FloatingText(player.x, player.y - 30f, "-20 STOMP", 0xFFF44336.toInt(), 1.2f, 26f))
                }
                val dbx = baseX - boss.x; val dby = baseY - boss.y
                if (dbx * dbx + dby * dby <= stompRange * stompRange) {
                    damageBase(20f)
                }
                floatingTexts.add(FloatingText(boss.x, boss.y - boss.size, "💥 TITAN STOMP!", 0xFFFF1744.toInt(), 1.5f, 32f))
                audio.play(SfxType.BOSS_AOE)
                shakeTimer = 0.5f; shakeIntensity = 16f
                repeat(25) {
                    val angle = Math.random() * Math.PI * 2
                    val dist = Math.random() * stompRange
                    particles.add(Particle(
                        boss.x + (Math.cos(angle) * dist).toFloat(),
                        boss.y + (Math.sin(angle) * dist).toFloat(),
                        (Math.cos(angle) * 120).toFloat(), (Math.sin(angle) * 120).toFloat(),
                        0.6f, 0xFFFF5722.toInt(), 6f
                    ))
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

    fun canPlaceTower(x: Float, y: Float, type: TowerType): Boolean { synchronized(lock) {
        if (!isTowerAllowed(type)) return false
        val cost = getTowerCost(type)
        if (gold < cost) return false
        if (towers.any { it.distanceTo(x, y) < 70f }) return false
        val distToBase = Math.sqrt(((x - baseX) * (x - baseX) + (y - baseY) * (y - baseY)).toDouble()).toFloat()
        if (distToBase < 60f) return false

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
        return true
    } }

    fun placeTower(x: Float, y: Float, type: TowerType): Boolean { synchronized(lock) {
        if (!isTowerAllowed(type)) return false
        if (campaignLevel?.missionType == MissionType.LONE_CHAMPION && towers.size >= 4) {
            floatingTexts.add(FloatingText(x, y, "MAX 4 TOWERS!", 0xFFFF4444.toInt(), 1.2f, 24f))
            return false
        }
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
        towersPlacedThisRun++
        towersPlacedTypes.add(type)
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
        val extraUses = if (skillTree.isRelicUnlocked(RelicId.DEMOLITION_SATCHEL)) 2 else 0
        val uses = when (type) {
            TrapType.SPIKE -> 5 + wave / 5 + extraUses
            TrapType.TAR -> 8 + wave / 4 + extraUses
            TrapType.MINE -> 1 + if (extraUses > 0) 1 else 0
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
        if (!tower.canUpgrade()) return false
        val cost = tower.upgradeCost()
        if (gold < cost) return false
        gold -= cost
        tower.upgrade()
        audio.play(SfxType.TOWER_UPGRADE)
        if (tower.level >= 5) checkAchievement("max_tower")
        return true
    } }

    fun sellTower(tower: Tower): Boolean { synchronized(lock) {
        val refund = if (campaignLevel?.id == 78) 0 else (tower.sellValue() * (1f + skillTree.sellValueBonus())).toInt()
        towers.remove(tower)
        towersSoldThisRun++
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
                    val reacted = triggerElementalReaction(target, DamageType.POISON, tower, dmg)
                    if (!reacted) {
                        target.poisonTimer = 4f
                        target.poisonDps = (tower.damage * towerDmgMult * 0.35f).coerceAtLeast(target.poisonDps)
                    }
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
                    val reacted = triggerElementalReaction(target, DamageType.ELECTRIC, tower, dmg)
                    if (!reacted) {
                        target.shockTimer = 3.5f
                    }
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
                    val reacted = triggerElementalReaction(target, DamageType.ICE, tower, 50f)
                    if (!reacted) {
                        target.deepFreezeTimer = 3f // Nearly stopped for 3 seconds
                    }
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
                    val reacted = triggerElementalReaction(target, DamageType.FIRE, tower, tower.damage * towerDmgMult)
                    if (!reacted) {
                        target.burnTimer = 5f
                        target.burnDps = tower.damage * towerDmgMult * 0.5f
                    }
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

    companion object {
        const val MAX_PLAYER_DAMAGE_LEVEL = 20
        const val MAX_PLAYER_SPEED_LEVEL = 10
        const val MAX_PLAYER_HP_LEVEL = 20
        const val MAX_BASE_HP_LEVEL = 20
    }

    fun getPlayerDamageCost(): Int =
        if (playerDamageLevel >= MAX_PLAYER_DAMAGE_LEVEL) Int.MAX_VALUE
        else 25 + (playerDamageLevel - 1) * 20 + (playerDamageLevel - 1) * (playerDamageLevel - 1) * 8

    fun getPlayerSpeedCost(): Int =
        if (playerSpeedLevel >= MAX_PLAYER_SPEED_LEVEL) Int.MAX_VALUE
        else 20 + (playerSpeedLevel - 1) * 25 + (playerSpeedLevel - 1) * (playerSpeedLevel - 1) * 12

    fun getPlayerHpCost(): Int =
        if (playerHpLevel >= MAX_PLAYER_HP_LEVEL) Int.MAX_VALUE
        else 30 + (playerHpLevel - 1) * 25 + (playerHpLevel - 1) * (playerHpLevel - 1) * 10

    fun getBaseHpCost(): Int =
        if (baseHpLevel >= MAX_BASE_HP_LEVEL) Int.MAX_VALUE
        else 40 + (baseHpLevel - 1) * 35 + (baseHpLevel - 1) * (baseHpLevel - 1) * 12

    fun upgradePlayerDamage(): Boolean { synchronized(lock) {
        if (playerDamageLevel >= MAX_PLAYER_DAMAGE_LEVEL) return false
        val cost = getPlayerDamageCost()
        if (gold < cost) return false
        gold -= cost
        playerDamageLevel++
        player.attackDamage += 3f
        audio.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun upgradePlayerSpeed(): Boolean { synchronized(lock) {
        if (playerSpeedLevel >= MAX_PLAYER_SPEED_LEVEL) return false
        val cost = getPlayerSpeedCost()
        if (gold < cost) return false
        gold -= cost
        playerSpeedLevel++
        player.speed = min(525f, player.speed + 25f)
        audio.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun upgradePlayerHp(): Boolean { synchronized(lock) {
        if (playerHpLevel >= MAX_PLAYER_HP_LEVEL) return false
        val cost = getPlayerHpCost()
        if (gold < cost) return false
        gold -= cost
        playerHpLevel++
        player.maxHp += 20f
        player.hp = player.maxHp
        audio.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun upgradeBaseHp(): Boolean { synchronized(lock) {
        if (baseHpLevel >= MAX_BASE_HP_LEVEL) return false
        val cost = getBaseHpCost()
        if (gold < cost) return false
        gold -= cost
        baseHpLevel++
        maxBaseHp += 25f
        baseHp = (baseHp + 25f).coerceAtMost(maxBaseHp)
        audio.play(SfxType.PLAYER_UPGRADE)
        checkUpgradeAll()
        return true
    } }

    fun repairBase(): Boolean { synchronized(lock) {
        if (campaignLevel?.id == 78) return false
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
        checkAchievement("milestone_first")
        if (endlessBuffs.size >= 3) checkAchievement("milestone_trio")
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
                    val scMult = if (enemy.superconductTimer > 0f) 1.25f else 1f
                    val dmg = 50f * shieldMult * randomizerPowerDamageMult * scMult
                    enemy.hp -= dmg
                    enemy.hitFlash = 0.3f
                    val reacted = triggerElementalReaction(enemy, DamageType.FIRE, null, dmg)
                    if (!reacted) {
                        enemy.burnTimer = 3f
                        enemy.burnDps = 20f
                    }
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
                    triggerElementalReaction(enemy, DamageType.ICE, null, 30f)
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
                    val scMult = if (enemy.superconductTimer > 0f) 1.25f else 1f
                    val dmg = 80f * shieldMult * randomizerPowerDamageMult * scMult
                    enemy.hp -= dmg
                    enemy.hitFlash = 0.3f
                    val reacted = triggerElementalReaction(enemy, DamageType.ELECTRIC, null, dmg)
                    if (!reacted) {
                        enemy.shockTimer = 3.5f
                    }
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
    fun saveGame() { synchronized(lock) {
        if (gameOver || campaignVictory || wave <= 0) {
            clearSave()
            return
        }
        val ed = prefs.edit()
        ed.putInt("save_wave", wave)
        ed.putInt("save_gold", gold)
        ed.putInt("save_score", score)
        ed.putInt("save_totalKills", totalKills)
        ed.putInt("save_totalGoldEarned", totalGoldEarned)
        ed.putInt("save_bestCombo", bestCombo)
        ed.putFloat("save_baseHp", baseHp)
        ed.putFloat("save_maxBaseHp", maxBaseHp)
        ed.putFloat("save_playerHp", player.hp)
        ed.putFloat("save_playerMaxHp", player.maxHp)
        ed.putFloat("save_playerDmg", player.attackDamage)
        ed.putFloat("save_playerSpd", player.speed)
        ed.putFloat("save_playerX", player.x)
        ed.putFloat("save_playerY", player.y)
        ed.putInt("save_difficulty", difficulty)
        ed.putInt("save_dmgLvl", playerDamageLevel)
        ed.putInt("save_spdLvl", playerSpeedLevel)
        ed.putInt("save_hpLvl", playerHpLevel)
        ed.putInt("save_baseLvl", baseHpLevel)
        ed.putInt("save_diamonds", diamondsEarnedThisRun)
        ed.putString("save_map", mapType.name)

        // Mode flags
        ed.putBoolean("save_isEndless", isEndlessMode)
        ed.putBoolean("save_isBossRush", isBossRush)
        ed.putBoolean("save_isDaily", isDailyChallenge)
        ed.putBoolean("save_isRandomizer", isRandomizerMode)
        ed.putBoolean("save_isIronman", isIronmanMode)
        ed.putBoolean("save_isArena", isArenaMode)
        ed.putBoolean("save_isSurvival", isSurvivalMode)
        ed.putInt("save_campaignLevelId", campaignLevel?.id ?: -1)

        // Endless buffs
        val buffsStr = endlessBuffs.joinToString(";") { it.name }
        ed.putString("save_endlessBuffs", buffsStr)

        // Towers (positions, levels, types, targeting, stats, specialization)
        val towerStr = towers.joinToString(";") { t ->
            "${t.x},${t.y},${t.level},${t.type.name},${t.targetingMode.name},${t.range},${t.damage},${t.fireRate},${t.totalKills},${t.totalDamageDealt.toInt()},${t.specialization.name}"
        }
        ed.putString("save_towers", towerStr)

        // Traps
        val trapStr = traps.joinToString(";") { tr ->
            "${tr.x},${tr.y},${tr.type.name},${tr.uses},${tr.maxUses}"
        }
        ed.putString("save_traps", trapStr)

        // Blockades
        val blockadeStr = blockades.joinToString(";") { b ->
            "${b.x},${b.y},${b.hp},${b.maxHp}"
        }
        ed.putString("save_blockades", blockadeStr)

        val permMerchantStr = activePermanentMerchantItems.joinToString(";") { it.name }
        ed.putString("save_permMerchant", permMerchantStr)
        val tempMerchantStr = activeTemporaryMerchantItems.entries.joinToString(";") { "${it.key.name},${it.value}" }
        ed.putString("save_tempMerchant", tempMerchantStr)
        ed.putInt("save_greedKills", greedCurseKillCount)
        ed.putInt("save_merchantPurchases", merchantPurchasesThisRun)
        ed.putInt("save_wavesPact", wavesSurvivedWithPact)
        ed.putInt("save_playerKills", playerKillsThisRun)
        ed.putInt("save_playerCrits", playerCritsThisRun)
        ed.putInt("save_consecutiveFlawless", consecutiveFlawlessWaves)
        ed.putBoolean("save_isHeroicMode", isHeroicMode)
        ed.putInt("save_towersSold", towersSoldThisRun)
        ed.putInt("save_towersPlaced", towersPlacedThisRun)
        ed.putFloat("save_baseDamageTaken", baseDamageTakenThisRun)
        ed.putBoolean("save_heroKilledBoss", heroKilledBoss)

        ed.putBoolean("has_save", true)
        ed.apply()
    } }

    /** Load saved game state */
    fun loadGame(): Boolean { synchronized(lock) {
        if (!prefs.getBoolean("has_save", false)) return false
        val savedDifficulty = prefs.getInt("save_difficulty", 1)
        applyDifficulty(savedDifficulty)
        val savedMap = prefs.getString("save_map", "CLASSIC")
        try { mapType = MapType.valueOf(savedMap) } catch (_: Exception) {}

        isEndlessMode = prefs.getBoolean("save_isEndless", false)
        isBossRush = prefs.getBoolean("save_isBossRush", false)
        isDailyChallenge = prefs.getBoolean("save_isDaily", false)
        isRandomizerMode = prefs.getBoolean("save_isRandomizer", false)
        isIronmanMode = prefs.getBoolean("save_isIronman", false)
        isArenaMode = prefs.getBoolean("save_isArena", false)
        isSurvivalMode = prefs.getBoolean("save_isSurvival", false)

        val campId = prefs.getInt("save_campaignLevelId", -1)
        if (campId > 0) {
            val cl = CampaignData.levels.find { it.id == campId }
            if (cl != null) applyCampaign(cl)
        }

        wave = prefs.getInt("save_wave", 0)
        gold = prefs.getInt("save_gold", 50)
        score = prefs.getInt("save_score", 0)
        totalKills = prefs.getInt("save_totalKills", 0)
        totalGoldEarned = prefs.getInt("save_totalGoldEarned", 0)
        bestCombo = prefs.getInt("save_bestCombo", 0)
        baseHp = prefs.getFloat("save_baseHp", 100f)
        maxBaseHp = prefs.getFloat("save_maxBaseHp", 100f)
        player.x = prefs.getFloat("save_playerX", 400f)
        player.y = prefs.getFloat("save_playerY", 600f)
        player.hp = prefs.getFloat("save_playerHp", 100f)
        player.maxHp = prefs.getFloat("save_playerMaxHp", 100f)
        player.attackDamage = prefs.getFloat("save_playerDmg", 10f)
        player.speed = prefs.getFloat("save_playerSpd", 300f)
        playerDamageLevel = prefs.getInt("save_dmgLvl", 1)
        playerSpeedLevel = prefs.getInt("save_spdLvl", 1)
        playerHpLevel = prefs.getInt("save_hpLvl", 1)
        baseHpLevel = prefs.getInt("save_baseLvl", 1)
        diamondsEarnedThisRun = prefs.getInt("save_diamonds", 0)

        activePermanentMerchantItems.clear()
        val permStr = prefs.getString("save_permMerchant", "")
        if (permStr.isNotBlank()) {
            for (item in permStr.split(";")) {
                if (item.isBlank()) continue
                runCatching { MerchantItemId.valueOf(item) }.getOrNull()?.let { activePermanentMerchantItems.add(it) }
            }
        }
        activeTemporaryMerchantItems.clear()
        val tempStr = prefs.getString("save_tempMerchant", "")
        if (tempStr.isNotBlank()) {
            for (item in tempStr.split(";")) {
                if (item.isBlank()) continue
                val parts = item.split(",")
                if (parts.size == 2) {
                    val id = runCatching { MerchantItemId.valueOf(parts[0]) }.getOrNull()
                    val dur = parts[1].toIntOrNull()
                    if (id != null && dur != null) {
                        activeTemporaryMerchantItems[id] = dur
                    }
                }
            }
        }
        greedCurseKillCount = prefs.getInt("save_greedKills", 0)
        merchantPurchasesThisRun = prefs.getInt("save_merchantPurchases", 0)
        wavesSurvivedWithPact = prefs.getInt("save_wavesPact", 0)
        playerKillsThisRun = prefs.getInt("save_playerKills", 0)
        playerCritsThisRun = prefs.getInt("save_playerCrits", 0)
        consecutiveFlawlessWaves = prefs.getInt("save_consecutiveFlawless", 0)
        isHeroicMode = prefs.getBoolean("save_isHeroicMode", false)
        towersSoldThisRun = prefs.getInt("save_towersSold", 0)
        towersPlacedThisRun = prefs.getInt("save_towersPlaced", 0)
        baseDamageTakenThisRun = prefs.getFloat("save_baseDamageTaken", 0f)
        heroKilledBoss = prefs.getBoolean("save_heroKilledBoss", false)

        // Restore endless buffs
        endlessBuffs.clear()
        endlessBuffTowerDmg = 1f
        endlessBuffGold = 1f
        endlessBuffBaseHp = 0f
        endlessBuffPowerCdr = 0f
        endlessBuffCritChance = 0f
        endlessBuffTowerRange = 1f
        val buffsRaw = prefs.getString("save_endlessBuffs", "")
        if (buffsRaw.isNotBlank()) {
            for (bName in buffsRaw.split(";")) {
                if (bName.isBlank()) continue
                val b = runCatching { EndlessBuff.valueOf(bName) }.getOrNull()
                if (b != null) pickEndlessBuff(b)
            }
        }

        // Restore Towers
        towers.clear()
        val towersRaw = prefs.getString("save_towers", "")
        if (towersRaw.isNotBlank()) {
            for (tStr in towersRaw.split(";")) {
                if (tStr.isBlank()) continue
                val p = tStr.split(",")
                if (p.size >= 5) {
                    val tx = p[0].toFloatOrNull() ?: continue
                    val ty = p[1].toFloatOrNull() ?: continue
                    val tLvl = p[2].toIntOrNull() ?: 1
                    val tType = runCatching { TowerType.valueOf(p[3]) }.getOrDefault(TowerType.ARROW)
                    val tTarget = runCatching { TargetingMode.valueOf(p[4]) }.getOrDefault(TargetingMode.CLOSE)
                    val tRange = if (p.size >= 6) p[5].toFloatOrNull() ?: (tType.baseRange + (tLvl - 1) * 10f) else (tType.baseRange + (tLvl - 1) * 10f)
                    val tDamage = if (p.size >= 7) p[6].toFloatOrNull() ?: (tType.baseDamage + (tLvl - 1) * (tType.baseDamage * 0.70f)) else (tType.baseDamage + (tLvl - 1) * (tType.baseDamage * 0.70f))
                    val tFireRate = if (p.size >= 8) p[7].toFloatOrNull() ?: tType.baseFireRate else tType.baseFireRate
                    val tKills = if (p.size >= 9) p[8].toIntOrNull() ?: 0 else 0
                    val tDmgDealt = if (p.size >= 10) p[9].toFloatOrNull() ?: 0f else 0f
                    val tSpec = if (p.size >= 11) runCatching { TowerSpecialization.valueOf(p[10]) }.getOrDefault(TowerSpecialization.NONE) else TowerSpecialization.NONE
                    towers.add(Tower(
                        x = tx, y = ty, level = tLvl, range = tRange, damage = tDamage, fireRate = tFireRate,
                        type = tType, targetingMode = tTarget, specialization = tSpec, totalKills = tKills, totalDamageDealt = tDmgDealt
                    ))
                }
            }
        }

        // Restore Traps
        traps.clear()
        activeFirePatches.clear()
        val trapsRaw = prefs.getString("save_traps", "")
        if (trapsRaw.isNotBlank()) {
            for (trStr in trapsRaw.split(";")) {
                if (trStr.isBlank()) continue
                val p = trStr.split(",")
                if (p.size >= 5) {
                    val trx = p[0].toFloatOrNull() ?: continue
                    val tryY = p[1].toFloatOrNull() ?: continue
                    val trType = runCatching { TrapType.valueOf(p[2]) }.getOrDefault(TrapType.SPIKE)
                    val trUses = p[3].toIntOrNull() ?: 1
                    val trMax = p[4].toIntOrNull() ?: trUses
                    traps.add(Trap(trx, tryY, trType, uses = trUses, maxUses = trMax))
                } else if (p.size >= 4) {
                    val trx = p[0].toFloatOrNull() ?: continue
                    val tryY = p[1].toFloatOrNull() ?: continue
                    val trType = runCatching { TrapType.valueOf(p[2]) }.getOrDefault(TrapType.SPIKE)
                    val trUses = p[3].toIntOrNull() ?: 1
                    traps.add(Trap(trx, tryY, trType, uses = trUses, maxUses = trUses))
                }
            }
        }

        // Restore Blockades
        blockades.clear()
        val blockadesRaw = prefs.getString("save_blockades", "")
        if (blockadesRaw.isNotBlank()) {
            for (bStr in blockadesRaw.split(";")) {
                if (bStr.isBlank()) continue
                val p = bStr.split(",")
                if (p.size >= 4) {
                    val bx = p[0].toFloatOrNull() ?: continue
                    val by = p[1].toFloatOrNull() ?: continue
                    val bhp = p[2].toFloatOrNull() ?: 150f
                    val bmax = p[3].toFloatOrNull() ?: 150f
                    blockades.add(Blockade(bx, by, hp = bhp, maxHp = bmax))
                }
            }
        }

        waveInProgress = false
        waveTimer = 4f
        generateNextWavePreview()
        return true
    } }

    /** Clear saved game */
    fun clearSave() {
        prefs.edit().putBoolean("has_save", false).apply()
    }

    fun hasSave(): Boolean = prefs.getBoolean("has_save", false)

    fun restart() { synchronized(lock) {
        clearSave()
        enemies.clear()
        towers.clear()
        projectiles.clear()
        floatingTexts.clear()
        particles.clear()
        blockades.clear()
        traps.clear()
        activeFirePatches.clear()
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
        activePermanentMerchantItems.clear()
        activeTemporaryMerchantItems.clear()
        merchantShopChoices = emptyList()
        merchantShopPending = false
        greedCurseKillCount = 0
        merchantPurchasesThisRun = 0
        wavesSurvivedWithPact = 0
        playerKillsThisRun = 0
        playerCritsThisRun = 0
        consecutiveFlawlessWaves = 0
        totalElementalReactionsThisRun = 0
        campaignStars = 0
        towersSoldThisRun = 0
        towersPlacedThisRun = 0
        towersPlacedTypes.clear()
        powersUsedThisRun.clear()
        baseDamageTakenThisRun = 0f
        heroKilledBoss = false
        campaignObjective1Met = false
        campaignObjective2Met = false
        campaignObjective3Met = false
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
        baseShield = if (skillTree.isRelicUnlocked(RelicId.AEGIS_OF_DAWN)) 100f else 0f
        aegisHealTimer = 0f
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
        currentWeather = WeatherEvent.CLEAR
        showWeatherBanner = false
        weatherBannerTimer = 0f
        lightningFlashTimer = 0f
        lastLightningTarget = null
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
