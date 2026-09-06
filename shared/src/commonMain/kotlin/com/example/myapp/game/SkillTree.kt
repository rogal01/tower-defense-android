package com.example.myapp.game

/** A single node in the persistent skill tree — purchased with diamonds across runs */
data class Skill(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val maxLevel: Int,
    val baseCost: Int,          // diamond cost for level 1
    val costPerLevel: Int       // additional cost per level
) {
    fun cost(currentLevel: Int): Int = baseCost + currentLevel * costPerLevel
}

/**
 * Persistent meta-progression skill tree.
 * Diamonds are earned in-game (enemy/boss drops) and spent here between runs.
 */
class SkillTree(private val prefs: GamePreferences) {

    // ---- Currency ----
    var diamonds: Int = 0
        private set

    // ---- Skill definitions ----
    val skills: List<Skill> = listOf(
        Skill("start_gold",     "Golden Start",   "+15 starting gold per level",        "\uD83D\uDCB0", 5, 3, 2),
        Skill("base_hp",        "Fortified Base",  "+20 base HP per level",             "\uD83C\uDFF0", 5, 4, 3),
        Skill("player_damage",  "Sharp Blade",     "+3 starting attack damage",         "\u2694\uFE0F",  5, 5, 3),
        Skill("player_speed",   "Swift Feet",      "+20 starting move speed",           "\uD83D\uDC5F",  5, 3, 2),
        Skill("player_hp",      "Tough Skin",      "+15 starting player HP",            "\u2764\uFE0F",  5, 4, 3),
        Skill("tower_damage",   "Tower Mastery",   "+8% tower damage per level",        "\uD83C\uDFF9",  5, 6, 4),
        Skill("gold_bonus",     "Treasure Hunter", "+10% gold from kills per level",    "\uD83D\uDC8E",  5, 5, 3),
        Skill("diamond_luck",   "Diamond Magnet",  "+5% diamond drop chance per level", "\uD83C\uDF1F",  3, 8, 6),
        Skill("wave_bonus",     "War Veteran",     "+3 gold per wave bonus",            "\u2B50",        5, 4, 3),
        Skill("attack_range",   "Eagle Eye",       "+15 starting attack range",         "\uD83D\uDC41\uFE0F", 4, 5, 4)
    )

    /** Page 2 skills — unlocked after prestige 1 */
    val prestigeSkills: List<Skill> = listOf(
        Skill("ice_power",      "Frost Mastery",   "+10% ice tower slow per level",     "\u2744\uFE0F",  5, 6, 4),
        Skill("ability_cd",     "Quick Cast",      "-5% ability cooldown per level",    "\u2728",        5, 5, 3),
        Skill("sell_bonus",     "Haggler",         "+10% tower sell value per level",   "\uD83D\uDCB8",  5, 4, 2),
        Skill("resist_pierce",  "Armor Break",     "+5% resistance pierce per level",   "\uD83D\uDDE1\uFE0F", 5, 7, 5),
        Skill("wave_modifier",  "Lucky Waves",     "Better wave modifier chances",      "\uD83C\uDF40",  3, 8, 6),
        Skill("prestige_gold",  "Midas Touch",     "+5% gold per prestige level",       "\uD83D\uDC51",  5, 5, 4)
    )

    /** Prestige level — resets skill tree page 1 for permanent bonuses */
    var prestigeLevel: Int = 0
        private set

    /** Current level for each skill id (0 = not purchased) */
    val levels = mutableMapOf<String, Int>()

    init { load() }

    // ---- Persistence ----

    fun load() {
        diamonds = prefs.getInt("diamonds", 0)
        prestigeLevel = prefs.getInt("prestige_level", 0)
        skills.forEach { skill ->
            levels[skill.id] = prefs.getInt("skill_${skill.id}", 0)
        }
        prestigeSkills.forEach { skill ->
            levels[skill.id] = prefs.getInt("skill_${skill.id}", 0)
        }
    }

    fun save() {
        val editor = prefs.edit()
        editor.putInt("diamonds", diamonds)
        editor.putInt("prestige_level", prestigeLevel)
        levels.forEach { (id, lvl) -> editor.putInt("skill_$id", lvl) }
        editor.apply()
    }

    // ---- API ----

    fun getLevel(id: String): Int = levels[id] ?: 0

    private fun findSkill(id: String): Skill? = skills.find { it.id == id } ?: prestigeSkills.find { it.id == id }

    fun canUpgrade(id: String): Boolean {
        val skill = findSkill(id) ?: return false
        val lvl = getLevel(id)
        return lvl < skill.maxLevel && diamonds >= skill.cost(lvl)
    }

    fun upgrade(id: String): Boolean {
        val skill = findSkill(id) ?: return false
        val lvl = getLevel(id)
        if (lvl >= skill.maxLevel) return false
        val cost = skill.cost(lvl)
        if (diamonds < cost) return false
        diamonds -= cost
        levels[id] = lvl + 1
        save()
        return true
    }

    fun addDiamonds(amount: Int) {
        diamonds += amount
        save()
    }

    /** Check if all page 1 skills are maxed */
    fun allPage1Maxed(): Boolean = skills.all { getLevel(it.id) >= it.maxLevel }

    /** Prestige cost in diamonds */
    fun prestigeCost(): Int = 50 + prestigeLevel * 30

    /** Can prestige? Requires all page 1 maxed + enough diamonds */
    fun canPrestige(): Boolean = allPage1Maxed() && diamonds >= prestigeCost()

    /** Prestige: resets page 1 skills, increments prestige level */
    fun prestige(): Boolean {
        if (!canPrestige()) return false
        diamonds -= prestigeCost()
        prestigeLevel++
        // Reset page 1 skills
        skills.forEach { levels[it.id] = 0 }
        save()
        prestigeAchieved = true
        return true
    }
    var prestigeAchieved: Boolean = false

    // ---- Gameplay effects (queried by GameEngine) ----

    fun bonusStartGold(): Int       = getLevel("start_gold") * 15 + if (isRelicUnlocked(RelicId.MIDAS_CRUCIBLE)) 100 else 0
    fun bonusBaseHp(): Float        = getLevel("base_hp") * 20f
    fun bonusPlayerDamage(): Float  = getLevel("player_damage") * 3f
    fun bonusPlayerSpeed(): Float   = getLevel("player_speed") * 20f
    fun bonusPlayerHp(): Float      = getLevel("player_hp") * 15f
    fun towerDamageMultiplier(): Float = 1f + getLevel("tower_damage") * 0.08f
    fun goldBonusMultiplier(): Float   = (1f + getLevel("gold_bonus") * 0.10f) * (1f + getLevel("prestige_gold") * 0.05f)
    fun diamondDropBonus(): Float      = getLevel("diamond_luck") * 0.05f
    fun bonusWaveGold(): Int        = getLevel("wave_bonus") * 3
    fun bonusAttackRange(): Float   = getLevel("attack_range") * 15f

    // Page 2 effects
    fun iceSlowBonus(): Float       = getLevel("ice_power") * 0.10f
    fun abilityCooldownReduction(): Float = getLevel("ability_cd") * 0.05f
    fun sellValueBonus(): Float     = getLevel("sell_bonus") * 0.10f
    fun resistancePierce(): Float   = getLevel("resist_pierce") * 0.05f

    /** Prestige gives a permanent multiplier to all damage */
    fun prestigeDamageMultiplier(): Float = 1f + prestigeLevel * 0.05f

    // ---- Relic Vault ----

    fun isRelicUnlocked(relic: RelicId): Boolean = prefs.getBoolean(relic.prefKey, false)

    fun canUnlockRelic(relic: RelicId): Boolean = !isRelicUnlocked(relic) && diamonds >= relic.diamondCost

    fun unlockRelic(relic: RelicId): Boolean {
        if (!canUnlockRelic(relic)) return false
        diamonds -= relic.diamondCost
        val ed = prefs.edit().putBoolean(relic.prefKey, true).putInt("diamonds", diamonds)
        val count = RelicId.entries.count { it == relic || isRelicUnlocked(it) }
        if (count >= 1) ed.putBoolean("ach_relic_first", true)
        if (count >= 3) ed.putBoolean("ach_relic_3", true)
        ed.apply()
        return true
    }

    fun unlockedRelicsCount(): Int = RelicId.entries.count { isRelicUnlocked(it) }
}
