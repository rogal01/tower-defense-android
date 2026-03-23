package com.example.myapp.model

data class GameState(
    var gold: Double = 0.0,
    var totalGoldEarned: Double = 0.0,
    var tapPower: Double = 1.0,
    var dungeonLevel: Int = 1,
    var prestigeMultiplier: Double = 1.0,
    var prestigeCount: Int = 0,
    var lastSaveTime: Long = System.currentTimeMillis()
)
