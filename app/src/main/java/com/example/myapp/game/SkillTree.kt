package com.example.myapp.game

import android.content.Context
import android.content.SharedPreferences

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
class SkillTree(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)

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

    /** Current level for each skill id (0 = not purchased) */
    val levels = mutableMapOf<String, Int>()

    init { load() }

    // ---- Persistence ----

    fun load() {
        diamonds = prefs.getInt("diamonds", 0)
        skills.forEach { skill ->
            levels[skill.id] = prefs.getInt("skill_${skill.id}", 0)
        }
    }

    fun save() {
        val editor = prefs.edit()
        editor.putInt("diamonds", diamonds)
        levels.forEach { (id, lvl) -> editor.putInt("skill_$id", lvl) }
        editor.apply()
    }

    // ---- API ----

    fun getLevel(id: String): Int = levels[id] ?: 0

    fun canUpgrade(id: String): Boolean {
        val skill = skills.find { it.id == id } ?: return false
        val lvl = getLevel(id)
        return lvl < skill.maxLevel && diamonds >= skill.cost(lvl)
    }

    fun upgrade(id: String): Boolean {
        val skill = skills.find { it.id == id } ?: return false
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

    // ---- Gameplay effects (queried by GameEngine) ----

    fun bonusStartGold(): Int       = getLevel("start_gold") * 15
    fun bonusBaseHp(): Float        = getLevel("base_hp") * 20f
    fun bonusPlayerDamage(): Float  = getLevel("player_damage") * 3f
    fun bonusPlayerSpeed(): Float   = getLevel("player_speed") * 20f
    fun bonusPlayerHp(): Float      = getLevel("player_hp") * 15f
    fun towerDamageMultiplier(): Float = 1f + getLevel("tower_damage") * 0.08f
    fun goldBonusMultiplier(): Float   = 1f + getLevel("gold_bonus") * 0.10f
    fun diamondDropBonus(): Float      = getLevel("diamond_luck") * 0.05f
    fun bonusWaveGold(): Int        = getLevel("wave_bonus") * 3
    fun bonusAttackRange(): Float   = getLevel("attack_range") * 15f
}
