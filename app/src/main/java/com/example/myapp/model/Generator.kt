package com.example.myapp.model

data class Generator(
    val id: Int,
    val name: String,
    val emoji: String,
    val baseGoldPerSec: Double,
    val baseCost: Double,
    val costMultiplier: Double = 1.15,
    var level: Int = 0
) {
    fun currentGoldPerSec(): Double {
        return baseGoldPerSec * level
    }

    fun nextCost(): Double {
        return baseCost * Math.pow(costMultiplier, level.toDouble())
    }
}
