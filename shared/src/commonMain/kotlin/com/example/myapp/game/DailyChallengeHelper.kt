package com.example.myapp.game

import java.util.Calendar
import java.util.Random

/**
 * Encapsulates deterministic daily trial metadata for briefing and gameplay execution.
 */
data class DailyChallengeData(
    val seed: Long,
    val dateString: String,
    val title: String,
    val mapType: MapType,
    val startingGold: Int,
    val targetWaves: Int = 15,
    val modifiers: List<WaveModifier>,
    val hpMultiplier: Float,
    val speedMultiplier: Float,
    val diamondReward: Int = 10
)

object DailyChallengeHelper {

    private val TRIAL_TITLES = listOf(
        "Trial of the Crimson Eclipse",
        "Gorge of the Dreadnought",
        "Crossroads Reckoning",
        "Sands of the Pharaoh",
        "Glacial Crevasse Assault",
        "Brimstone Bastion Siege",
        "Whispering Feywood Incursion",
        "Caldera of the Sunken Titan",
        "Citadel Under Siege",
        "Echoes of the Void Gate"
    )

    fun getTodaySeed(): Long {
        val cal = Calendar.getInstance()
        return (cal.get(Calendar.YEAR) * 10000L +
                (cal.get(Calendar.MONTH) + 1) * 100L +
                cal.get(Calendar.DAY_OF_MONTH))
    }

    fun getDailyChallenge(seed: Long = getTodaySeed()): DailyChallengeData {
        val rng = Random(seed)
        val allMods = WaveModifier.entries.filter { it != WaveModifier.NONE }
        val modCount = 3 + rng.nextInt(3) // 3-5 distinct modifiers
        val selectedMods = mutableListOf<WaveModifier>()
        val pool = allMods.toMutableList()
        repeat(modCount.coerceAtMost(pool.size)) {
            val idx = rng.nextInt(pool.size)
            selectedMods.add(pool.removeAt(idx))
        }

        val map = MapType.entries[rng.nextInt(MapType.entries.size)]
        val startGold = 40 + rng.nextInt(41) // 40 - 80 starting gold
        val hpMult = 0.85f + rng.nextFloat() * 0.4f // 0.85 - 1.25x enemy HP
        val spdMult = 0.90f + rng.nextFloat() * 0.20f // 0.90 - 1.10x enemy speed
        val titleIdx = ((seed xor (seed shr 4)) % TRIAL_TITLES.size).toInt().let { if (it < 0) -it else it }
        val title = TRIAL_TITLES[titleIdx % TRIAL_TITLES.size]

        val cal = Calendar.getInstance()
        val year = (seed / 10000L).toInt()
        val month = ((seed % 10000L) / 100L).toInt()
        val day = (seed % 100L).toInt()
        val dateStr = String.format("%04d-%02d-%02d", year, month, day)

        return DailyChallengeData(
            seed = seed,
            dateString = dateStr,
            title = title,
            mapType = map,
            startingGold = startGold,
            targetWaves = 15,
            modifiers = selectedMods,
            hpMultiplier = hpMult,
            speedMultiplier = spdMult,
            diamondReward = 10
        )
    }
}
