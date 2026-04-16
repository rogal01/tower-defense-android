package com.example.myapp.game

import org.json.JSONArray
import org.json.JSONObject

data class PointSnapshot(
    val x: Float,
    val y: Float
) {
    fun toJson(): JSONObject = JSONObject()
        .put("x", x)
        .put("y", y)

    companion object {
        fun fromJson(json: JSONObject): PointSnapshot = PointSnapshot(
            x = json.getDouble("x").toFloat(),
            y = json.getDouble("y").toFloat()
        )
    }
}

data class PathSnapshot(
    val waypoints: List<PointSnapshot>
) {
    fun toJson(): JSONObject = JSONObject()
        .put("waypoints", JSONArray().apply { waypoints.forEach { put(it.toJson()) } })

    companion object {
        fun fromJson(json: JSONObject): PathSnapshot = PathSnapshot(
            waypoints = json.optJSONArray("waypoints").toObjectList { PointSnapshot.fromJson(it) }
        )
    }
}

data class PlayerSnapshot(
    val x: Float,
    val y: Float,
    val targetX: Float,
    val targetY: Float,
    val speed: Float,
    val attackRange: Float,
    val attackDamage: Float,
    val attackCooldown: Float,
    val attackTimer: Float,
    val size: Float,
    val hp: Float,
    val maxHp: Float,
    val slowTimer: Float
) {
    fun toJson(): JSONObject = JSONObject()
        .put("x", x)
        .put("y", y)
        .put("targetX", targetX)
        .put("targetY", targetY)
        .put("speed", speed)
        .put("attackRange", attackRange)
        .put("attackDamage", attackDamage)
        .put("attackCooldown", attackCooldown)
        .put("attackTimer", attackTimer)
        .put("size", size)
        .put("hp", hp)
        .put("maxHp", maxHp)
        .put("slowTimer", slowTimer)

    companion object {
        fun fromJson(json: JSONObject): PlayerSnapshot = PlayerSnapshot(
            x = json.getDouble("x").toFloat(),
            y = json.getDouble("y").toFloat(),
            targetX = json.getDouble("targetX").toFloat(),
            targetY = json.getDouble("targetY").toFloat(),
            speed = json.getDouble("speed").toFloat(),
            attackRange = json.getDouble("attackRange").toFloat(),
            attackDamage = json.getDouble("attackDamage").toFloat(),
            attackCooldown = json.getDouble("attackCooldown").toFloat(),
            attackTimer = json.getDouble("attackTimer").toFloat(),
            size = json.getDouble("size").toFloat(),
            hp = json.getDouble("hp").toFloat(),
            maxHp = json.getDouble("maxHp").toFloat(),
            slowTimer = json.optDouble("slowTimer", 0.0).toFloat()
        )
    }
}

data class TowerSnapshot(
    val x: Float,
    val y: Float,
    val level: Int,
    val range: Float,
    val damage: Float,
    val fireRate: Float,
    val fireTimer: Float,
    val size: Float,
    val type: String,
    val targetingMode: String,
    val abilityTimer: Float,
    val debuffTimer: Float
) {
    fun toJson(): JSONObject = JSONObject()
        .put("x", x)
        .put("y", y)
        .put("level", level)
        .put("range", range)
        .put("damage", damage)
        .put("fireRate", fireRate)
        .put("fireTimer", fireTimer)
        .put("size", size)
        .put("type", type)
        .put("targetingMode", targetingMode)
        .put("abilityTimer", abilityTimer)
        .put("debuffTimer", debuffTimer)

    companion object {
        fun fromJson(json: JSONObject): TowerSnapshot = TowerSnapshot(
            x = json.getDouble("x").toFloat(),
            y = json.getDouble("y").toFloat(),
            level = json.getInt("level"),
            range = json.getDouble("range").toFloat(),
            damage = json.getDouble("damage").toFloat(),
            fireRate = json.getDouble("fireRate").toFloat(),
            fireTimer = json.getDouble("fireTimer").toFloat(),
            size = json.optDouble("size", 35.0).toFloat(),
            type = json.getString("type"),
            targetingMode = json.getString("targetingMode"),
            abilityTimer = json.getDouble("abilityTimer").toFloat(),
            debuffTimer = json.optDouble("debuffTimer", 0.0).toFloat()
        )
    }
}

data class EnemySnapshot(
    val x: Float,
    val y: Float,
    val speed: Float,
    val hp: Float,
    val maxHp: Float,
    val goldReward: Int,
    val damage: Float,
    val size: Float,
    val type: String,
    val bossType: String?,
    val hitFlash: Float,
    val pathIndex: Int,
    val waypointIndex: Int,
    val bossAbilityTimer: Float,
    val bossAbilityCooldown: Float,
    val isCharging: Boolean,
    val chargeTimer: Float,
    val hasSplit: Boolean,
    val iceSlowFactor: Float,
    val deepFreezeTimer: Float,
    val regenRate: Float,
    val reachedBase: Boolean,
    val isElite: Boolean,
    val shieldTimer: Float
) {
    fun toJson(): JSONObject = JSONObject()
        .put("x", x)
        .put("y", y)
        .put("speed", speed)
        .put("hp", hp)
        .put("maxHp", maxHp)
        .put("goldReward", goldReward)
        .put("damage", damage)
        .put("size", size)
        .put("type", type)
        .put("bossType", bossType)
        .put("hitFlash", hitFlash)
        .put("pathIndex", pathIndex)
        .put("waypointIndex", waypointIndex)
        .put("bossAbilityTimer", bossAbilityTimer)
        .put("bossAbilityCooldown", bossAbilityCooldown)
        .put("isCharging", isCharging)
        .put("chargeTimer", chargeTimer)
        .put("hasSplit", hasSplit)
        .put("iceSlowFactor", iceSlowFactor)
        .put("deepFreezeTimer", deepFreezeTimer)
        .put("regenRate", regenRate)
        .put("reachedBase", reachedBase)
        .put("isElite", isElite)
        .put("shieldTimer", shieldTimer)

    companion object {
        fun fromJson(json: JSONObject): EnemySnapshot = EnemySnapshot(
            x = json.getDouble("x").toFloat(),
            y = json.getDouble("y").toFloat(),
            speed = json.getDouble("speed").toFloat(),
            hp = json.getDouble("hp").toFloat(),
            maxHp = json.getDouble("maxHp").toFloat(),
            goldReward = json.getInt("goldReward"),
            damage = json.getDouble("damage").toFloat(),
            size = json.optDouble("size", 30.0).toFloat(),
            type = json.getString("type"),
            bossType = json.optString("bossType", "").ifBlank { null },
            hitFlash = json.optDouble("hitFlash", 0.0).toFloat(),
            pathIndex = json.optInt("pathIndex", 0),
            waypointIndex = json.optInt("waypointIndex", 1),
            bossAbilityTimer = json.optDouble("bossAbilityTimer", 0.0).toFloat(),
            bossAbilityCooldown = json.optDouble("bossAbilityCooldown", 5.0).toFloat(),
            isCharging = json.optBoolean("isCharging", false),
            chargeTimer = json.optDouble("chargeTimer", 0.0).toFloat(),
            hasSplit = json.optBoolean("hasSplit", false),
            iceSlowFactor = json.optDouble("iceSlowFactor", 1.0).toFloat(),
            deepFreezeTimer = json.optDouble("deepFreezeTimer", 0.0).toFloat(),
            regenRate = json.optDouble("regenRate", 0.0).toFloat(),
            reachedBase = json.optBoolean("reachedBase", false),
            isElite = json.optBoolean("isElite", false),
            shieldTimer = json.optDouble("shieldTimer", 0.0).toFloat()
        )
    }
}

data class ProjectileSnapshot(
    val x: Float,
    val y: Float,
    val targetX: Float,
    val targetY: Float,
    val speed: Float,
    val damage: Float,
    val size: Float,
    val color: Int,
    val alive: Boolean
) {
    fun toJson(): JSONObject = JSONObject()
        .put("x", x)
        .put("y", y)
        .put("targetX", targetX)
        .put("targetY", targetY)
        .put("speed", speed)
        .put("damage", damage)
        .put("size", size)
        .put("color", color)
        .put("alive", alive)

    companion object {
        fun fromJson(json: JSONObject): ProjectileSnapshot = ProjectileSnapshot(
            x = json.getDouble("x").toFloat(),
            y = json.getDouble("y").toFloat(),
            targetX = json.getDouble("targetX").toFloat(),
            targetY = json.getDouble("targetY").toFloat(),
            speed = json.optDouble("speed", 600.0).toFloat(),
            damage = json.getDouble("damage").toFloat(),
            size = json.optDouble("size", 8.0).toFloat(),
            color = json.optInt("color", 0xFFFFD700.toInt()),
            alive = json.optBoolean("alive", true)
        )
    }
}

data class ModeSnapshot(
    val difficulty: Int,
    val mapType: String,
    val isEndlessMode: Boolean,
    val isBossRush: Boolean,
    val bossRushWave: Int,
    val currentBoss: String?,
    val bossPool: List<String>,
    val campaignLevelId: Int?,
    val campaignVictory: Boolean,
    val isDailyChallenge: Boolean,
    val dailyChallengeModifiers: List<String>,
    val dailyChallengeSeed: Long,
    val isRandomizerMode: Boolean,
    val randomizerSeed: Long,
    val randomizerTowerCosts: Map<String, Int>,
    val randomizerPowerCooldowns: Map<String, Float>,
    val randomizerPowerDamageMult: Float,
    val randomizerStartGold: Int,
    val enemyHpMult: Float,
    val enemyDmgMult: Float,
    val enemySpeedMult: Float,
    val goldMult: Float,
    val spawnRateMult: Float,
    val isNight: Boolean
) {
    fun toJson(): JSONObject = JSONObject()
        .put("difficulty", difficulty)
        .put("mapType", mapType)
        .put("isEndlessMode", isEndlessMode)
        .put("isBossRush", isBossRush)
        .put("bossRushWave", bossRushWave)
        .put("currentBoss", currentBoss)
        .put("bossPool", JSONArray(bossPool))
        .put("campaignLevelId", campaignLevelId)
        .put("campaignVictory", campaignVictory)
        .put("isDailyChallenge", isDailyChallenge)
        .put("dailyChallengeModifiers", JSONArray(dailyChallengeModifiers))
        .put("dailyChallengeSeed", dailyChallengeSeed)
        .put("isRandomizerMode", isRandomizerMode)
        .put("randomizerSeed", randomizerSeed)
        .put("randomizerTowerCosts", JSONObject(randomizerTowerCosts))
        .put("randomizerPowerCooldowns", JSONObject(randomizerPowerCooldowns.mapValues { it.value.toDouble() }))
        .put("randomizerPowerDamageMult", randomizerPowerDamageMult)
        .put("randomizerStartGold", randomizerStartGold)
        .put("enemyHpMult", enemyHpMult)
        .put("enemyDmgMult", enemyDmgMult)
        .put("enemySpeedMult", enemySpeedMult)
        .put("goldMult", goldMult)
        .put("spawnRateMult", spawnRateMult)
        .put("isNight", isNight)

    companion object {
        fun fromJson(json: JSONObject): ModeSnapshot = ModeSnapshot(
            difficulty = json.optInt("difficulty", 1),
            mapType = json.optString("mapType", MapType.CLASSIC.name),
            isEndlessMode = json.optBoolean("isEndlessMode", false),
            isBossRush = json.optBoolean("isBossRush", false),
            bossRushWave = json.optInt("bossRushWave", 0),
            currentBoss = json.optString("currentBoss", "").ifBlank { null },
            bossPool = json.optJSONArray("bossPool").toStringList(),
            campaignLevelId = if (json.isNull("campaignLevelId")) null else json.optInt("campaignLevelId"),
            campaignVictory = json.optBoolean("campaignVictory", false),
            isDailyChallenge = json.optBoolean("isDailyChallenge", false),
            dailyChallengeModifiers = json.optJSONArray("dailyChallengeModifiers").toStringList(),
            dailyChallengeSeed = json.optLong("dailyChallengeSeed", 0L),
            isRandomizerMode = json.optBoolean("isRandomizerMode", false),
            randomizerSeed = json.optLong("randomizerSeed", 0L),
            randomizerTowerCosts = json.optJSONObject("randomizerTowerCosts").toIntMap(),
            randomizerPowerCooldowns = json.optJSONObject("randomizerPowerCooldowns").toFloatMap(),
            randomizerPowerDamageMult = json.optDouble("randomizerPowerDamageMult", 1.0).toFloat(),
            randomizerStartGold = json.optInt("randomizerStartGold", 50),
            enemyHpMult = json.optDouble("enemyHpMult", 1.0).toFloat(),
            enemyDmgMult = json.optDouble("enemyDmgMult", 1.0).toFloat(),
            enemySpeedMult = json.optDouble("enemySpeedMult", 1.0).toFloat(),
            goldMult = json.optDouble("goldMult", 1.0).toFloat(),
            spawnRateMult = json.optDouble("spawnRateMult", 1.0).toFloat(),
            isNight = json.optBoolean("isNight", false)
        )
    }
}

data class RunSnapshot(
    val version: Int = 2,
    val wave: Int,
    val gold: Int,
    val score: Int,
    val totalKills: Int,
    val totalGoldEarned: Int,
    val baseHp: Float,
    val maxBaseHp: Float,
    val waveInProgress: Boolean,
    val enemiesRemaining: Int,
    val waveDelay: Float,
    val waveTimer: Float,
    val totalEnemiesThisWave: Int,
    val enemiesSpawnedThisWave: Int,
    val currentWaveModifier: String,
    val isPaused: Boolean,
    val gameSpeed: Int,
    val showWaveBanner: Boolean,
    val waveBannerTimer: Float,
    val comboCount: Int,
    val comboTimer: Float,
    val comboMultiplier: Float,
    val bestCombo: Int,
    val freezeTimer: Float,
    val shakeTimer: Float,
    val shakeIntensity: Float,
    val playerDamageLevel: Int,
    val playerSpeedLevel: Int,
    val playerHpLevel: Int,
    val baseHpLevel: Int,
    val diamondsEarnedThisRun: Int,
    val bossesKilledThisRun: Int,
    val repairsThisRun: Int,
    val baseHpBeforeWave: Float,
    val dashCooldown: Float,
    val isDashing: Boolean,
    val dashTrailX: Float,
    val dashTrailY: Float,
    val eliteSpawnedThisWave: Boolean,
    val desertStormTimer: Float,
    val desertStormDuration: Float,
    val powersUsedThisRun: List<String>,
    val powerCooldowns: Map<String, Float>,
    val player: PlayerSnapshot,
    val towers: List<TowerSnapshot>,
    val enemies: List<EnemySnapshot>,
    val projectiles: List<ProjectileSnapshot>,
    val paths: List<PathSnapshot>,
    val riverWaypoints: List<PointSnapshot>,
    val mode: ModeSnapshot
) {
    fun toJsonString(): String {
        val json = JSONObject()
            .put("version", version)
            .put("wave", wave)
            .put("gold", gold)
            .put("score", score)
            .put("totalKills", totalKills)
            .put("totalGoldEarned", totalGoldEarned)
            .put("baseHp", baseHp)
            .put("maxBaseHp", maxBaseHp)
            .put("waveInProgress", waveInProgress)
            .put("enemiesRemaining", enemiesRemaining)
            .put("waveDelay", waveDelay)
            .put("waveTimer", waveTimer)
            .put("totalEnemiesThisWave", totalEnemiesThisWave)
            .put("enemiesSpawnedThisWave", enemiesSpawnedThisWave)
            .put("currentWaveModifier", currentWaveModifier)
            .put("isPaused", isPaused)
            .put("gameSpeed", gameSpeed)
            .put("showWaveBanner", showWaveBanner)
            .put("waveBannerTimer", waveBannerTimer)
            .put("comboCount", comboCount)
            .put("comboTimer", comboTimer)
            .put("comboMultiplier", comboMultiplier)
            .put("bestCombo", bestCombo)
            .put("freezeTimer", freezeTimer)
            .put("shakeTimer", shakeTimer)
            .put("shakeIntensity", shakeIntensity)
            .put("playerDamageLevel", playerDamageLevel)
            .put("playerSpeedLevel", playerSpeedLevel)
            .put("playerHpLevel", playerHpLevel)
            .put("baseHpLevel", baseHpLevel)
            .put("diamondsEarnedThisRun", diamondsEarnedThisRun)
            .put("bossesKilledThisRun", bossesKilledThisRun)
            .put("repairsThisRun", repairsThisRun)
            .put("baseHpBeforeWave", baseHpBeforeWave)
            .put("dashCooldown", dashCooldown)
            .put("isDashing", isDashing)
            .put("dashTrailX", dashTrailX)
            .put("dashTrailY", dashTrailY)
            .put("eliteSpawnedThisWave", eliteSpawnedThisWave)
            .put("desertStormTimer", desertStormTimer)
            .put("desertStormDuration", desertStormDuration)
            .put("powersUsedThisRun", JSONArray(powersUsedThisRun))
            .put("powerCooldowns", JSONObject(powerCooldowns.mapValues { it.value.toDouble() }))
            .put("player", player.toJson())
            .put("towers", JSONArray().apply { towers.forEach { put(it.toJson()) } })
            .put("enemies", JSONArray().apply { enemies.forEach { put(it.toJson()) } })
            .put("projectiles", JSONArray().apply { projectiles.forEach { put(it.toJson()) } })
            .put("paths", JSONArray().apply { paths.forEach { put(it.toJson()) } })
            .put("riverWaypoints", JSONArray().apply { riverWaypoints.forEach { put(it.toJson()) } })
            .put("mode", mode.toJson())
        return json.toString()
    }

    companion object {
        fun fromJsonString(jsonString: String): RunSnapshot {
            val json = JSONObject(jsonString)
            return RunSnapshot(
                version = json.optInt("version", 2),
                wave = json.getInt("wave"),
                gold = json.getInt("gold"),
                score = json.getInt("score"),
                totalKills = json.optInt("totalKills", 0),
                totalGoldEarned = json.optInt("totalGoldEarned", 0),
                baseHp = json.getDouble("baseHp").toFloat(),
                maxBaseHp = json.getDouble("maxBaseHp").toFloat(),
                waveInProgress = json.optBoolean("waveInProgress", false),
                enemiesRemaining = json.optInt("enemiesRemaining", 0),
                waveDelay = json.optDouble("waveDelay", 5.0).toFloat(),
                waveTimer = json.optDouble("waveTimer", 4.0).toFloat(),
                totalEnemiesThisWave = json.optInt("totalEnemiesThisWave", 0),
                enemiesSpawnedThisWave = json.optInt("enemiesSpawnedThisWave", 0),
                currentWaveModifier = json.optString("currentWaveModifier", WaveModifier.NONE.name),
                isPaused = json.optBoolean("isPaused", false),
                gameSpeed = json.optInt("gameSpeed", 1),
                showWaveBanner = json.optBoolean("showWaveBanner", false),
                waveBannerTimer = json.optDouble("waveBannerTimer", 0.0).toFloat(),
                comboCount = json.optInt("comboCount", 0),
                comboTimer = json.optDouble("comboTimer", 0.0).toFloat(),
                comboMultiplier = json.optDouble("comboMultiplier", 1.0).toFloat(),
                bestCombo = json.optInt("bestCombo", 0),
                freezeTimer = json.optDouble("freezeTimer", 0.0).toFloat(),
                shakeTimer = json.optDouble("shakeTimer", 0.0).toFloat(),
                shakeIntensity = json.optDouble("shakeIntensity", 0.0).toFloat(),
                playerDamageLevel = json.optInt("playerDamageLevel", 1),
                playerSpeedLevel = json.optInt("playerSpeedLevel", 1),
                playerHpLevel = json.optInt("playerHpLevel", 1),
                baseHpLevel = json.optInt("baseHpLevel", 1),
                diamondsEarnedThisRun = json.optInt("diamondsEarnedThisRun", 0),
                bossesKilledThisRun = json.optInt("bossesKilledThisRun", 0),
                repairsThisRun = json.optInt("repairsThisRun", 0),
                baseHpBeforeWave = json.optDouble("baseHpBeforeWave", 100.0).toFloat(),
                dashCooldown = json.optDouble("dashCooldown", 0.0).toFloat(),
                isDashing = json.optBoolean("isDashing", false),
                dashTrailX = json.optDouble("dashTrailX", 0.0).toFloat(),
                dashTrailY = json.optDouble("dashTrailY", 0.0).toFloat(),
                eliteSpawnedThisWave = json.optBoolean("eliteSpawnedThisWave", false),
                desertStormTimer = json.optDouble("desertStormTimer", 12.0).toFloat(),
                desertStormDuration = json.optDouble("desertStormDuration", 0.0).toFloat(),
                powersUsedThisRun = json.optJSONArray("powersUsedThisRun").toStringList(),
                powerCooldowns = json.optJSONObject("powerCooldowns").toFloatMap(),
                player = PlayerSnapshot.fromJson(json.getJSONObject("player")),
                towers = json.optJSONArray("towers").toObjectList { TowerSnapshot.fromJson(it) },
                enemies = json.optJSONArray("enemies").toObjectList { EnemySnapshot.fromJson(it) },
                projectiles = json.optJSONArray("projectiles").toObjectList { ProjectileSnapshot.fromJson(it) },
                paths = json.optJSONArray("paths").toObjectList { PathSnapshot.fromJson(it) },
                riverWaypoints = json.optJSONArray("riverWaypoints").toObjectList { PointSnapshot.fromJson(it) },
                mode = ModeSnapshot.fromJson(json.getJSONObject("mode"))
            )
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (i in 0 until length()) add(optString(i))
    }
}

private inline fun <T> JSONArray?.toObjectList(mapper: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            add(mapper(item))
        }
    }
}

private fun JSONObject?.toFloatMap(): Map<String, Float> {
    if (this == null) return emptyMap()
    return keys().asSequence().associateWith { optDouble(it, 0.0).toFloat() }
}

private fun JSONObject?.toIntMap(): Map<String, Int> {
    if (this == null) return emptyMap()
    return keys().asSequence().associateWith { optInt(it, 0) }
}
