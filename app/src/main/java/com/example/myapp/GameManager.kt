package com.example.myapp

import android.content.Context
import android.content.SharedPreferences
import com.example.myapp.model.GameState
import com.example.myapp.model.Generator

class GameManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("idle_dungeon_save", Context.MODE_PRIVATE)

    val state = GameState()

    val generators: List<Generator> = listOf(
        Generator(0, "Apprentice", "\u2694\uFE0F", 1.0, 10.0),
        Generator(1, "Warrior", "\uD83D\uDEE1\uFE0F", 5.0, 75.0),
        Generator(2, "Mage", "\uD83E\uDDD9", 25.0, 500.0),
        Generator(3, "Rogue", "\uD83D\uDDE1\uFE0F", 100.0, 3_000.0),
        Generator(4, "Paladin", "\u2728", 500.0, 15_000.0),
        Generator(5, "Ranger", "\uD83C\uDFF9", 2_500.0, 80_000.0),
        Generator(6, "Dragon Slayer", "\uD83D\uDC09", 10_000.0, 400_000.0),
        Generator(7, "Archmage", "\uD83C\uDF1F", 50_000.0, 2_000_000.0),
        Generator(8, "Legendary Hero", "\uD83D\uDC51", 250_000.0, 10_000_000.0),
        Generator(9, "Demigod", "\u26A1", 1_000_000.0, 50_000_000.0)
    )

    fun totalGoldPerSec(): Double {
        return generators.sumOf { it.currentGoldPerSec() } * state.prestigeMultiplier
    }

    fun tap(): Double {
        val earned = state.tapPower * state.prestigeMultiplier
        state.gold += earned
        state.totalGoldEarned += earned
        updateDungeonLevel()
        return earned
    }

    fun tick(deltaSeconds: Double) {
        val earned = totalGoldPerSec() * deltaSeconds
        state.gold += earned
        state.totalGoldEarned += earned
        updateDungeonLevel()
    }

    fun canAfford(generator: Generator): Boolean {
        return state.gold >= generator.nextCost()
    }

    fun buyGenerator(generator: Generator): Boolean {
        val cost = generator.nextCost()
        if (state.gold < cost) return false
        state.gold -= cost
        generator.level++

        // Every 10 levels of any generator, increase tap power
        if (generator.level % 10 == 0) {
            state.tapPower += generator.baseGoldPerSec * 0.5
        }
        return true
    }

    fun canPrestige(): Boolean {
        return state.totalGoldEarned >= 1_000_000
    }

    fun prestige() {
        if (!canPrestige()) return
        val bonus = Math.log10(state.totalGoldEarned / 1_000_000.0) + 1.0
        state.prestigeMultiplier += bonus
        state.prestigeCount++
        state.gold = 0.0
        state.totalGoldEarned = 0.0
        state.tapPower = 1.0
        state.dungeonLevel = 1
        generators.forEach { it.level = 0 }
    }

    private fun updateDungeonLevel() {
        state.dungeonLevel = (Math.log10(state.totalGoldEarned + 1.0) + 1).toInt().coerceAtLeast(1)
    }

    fun save() {
        state.lastSaveTime = System.currentTimeMillis()
        prefs.edit().apply {
            putLong("gold", state.gold.toLong())
            putLong("totalGoldEarned", state.totalGoldEarned.toLong())
            putFloat("tapPower", state.tapPower.toFloat())
            putInt("dungeonLevel", state.dungeonLevel)
            putFloat("prestigeMultiplier", state.prestigeMultiplier.toFloat())
            putInt("prestigeCount", state.prestigeCount)
            putLong("lastSaveTime", state.lastSaveTime)
            generators.forEach { gen ->
                putInt("gen_${gen.id}_level", gen.level)
            }
            apply()
        }
    }

    fun load() {
        if (!prefs.contains("gold")) return
        state.gold = prefs.getLong("gold", 0).toDouble()
        state.totalGoldEarned = prefs.getLong("totalGoldEarned", 0).toDouble()
        state.tapPower = prefs.getFloat("tapPower", 1f).toDouble()
        state.dungeonLevel = prefs.getInt("dungeonLevel", 1)
        state.prestigeMultiplier = prefs.getFloat("prestigeMultiplier", 1f).toDouble()
        state.prestigeCount = prefs.getInt("prestigeCount", 0)
        state.lastSaveTime = prefs.getLong("lastSaveTime", System.currentTimeMillis())
        generators.forEach { gen ->
            gen.level = prefs.getInt("gen_${gen.id}_level", 0)
        }

        // Calculate offline earnings
        val now = System.currentTimeMillis()
        val elapsedSec = (now - state.lastSaveTime) / 1000.0
        if (elapsedSec > 1) {
            // Offline earns at 50% rate
            val offlineEarnings = totalGoldPerSec() * elapsedSec * 0.5
            state.gold += offlineEarnings
            state.totalGoldEarned += offlineEarnings
        }
    }

    companion object {
        fun formatNumber(value: Double): String {
            return when {
                value >= 1_000_000_000_000.0 -> String.format("%.2fT", value / 1_000_000_000_000.0)
                value >= 1_000_000_000.0 -> String.format("%.2fB", value / 1_000_000_000.0)
                value >= 1_000_000.0 -> String.format("%.2fM", value / 1_000_000.0)
                value >= 1_000.0 -> String.format("%.2fK", value / 1_000.0)
                value >= 1.0 -> String.format("%.0f", value)
                else -> String.format("%.1f", value)
            }
        }
    }
}
