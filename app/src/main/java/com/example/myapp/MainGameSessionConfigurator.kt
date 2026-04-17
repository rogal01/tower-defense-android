package com.example.myapp

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.game.CampaignData
import com.example.myapp.game.GameEngine
import com.example.myapp.game.MapType
import com.example.myapp.game.PowerType
import com.example.myapp.game.TowerType

internal class MainGameSessionConfigurator(
    private val binding: ActivityMainBinding,
    private val engine: GameEngine,
) {
    private data class TowerButtonSpec(val button: Button, val type: TowerType)
    private data class PowerButtonSpec(val button: Button, val type: PowerType)

    private val towerButtons = listOf(
        TowerButtonSpec(binding.btnTowerArrow, TowerType.ARROW),
        TowerButtonSpec(binding.btnTowerMagic, TowerType.MAGIC),
        TowerButtonSpec(binding.btnTowerCannon, TowerType.CANNON),
        TowerButtonSpec(binding.btnTowerPoison, TowerType.POISON),
        TowerButtonSpec(binding.btnTowerTesla, TowerType.TESLA),
        TowerButtonSpec(binding.btnTowerIce, TowerType.ICE),
        TowerButtonSpec(binding.btnTowerFlame, TowerType.FLAME),
        TowerButtonSpec(binding.btnTowerNecro, TowerType.NECRO),
        TowerButtonSpec(binding.btnTowerBallista, TowerType.BALLISTA),
        TowerButtonSpec(binding.btnTowerVortex, TowerType.VORTEX),
        TowerButtonSpec(binding.btnTowerHealer, TowerType.HEALER),
    )

    private val powerButtons = listOf(
        PowerButtonSpec(binding.btnPowerFireball, PowerType.FIREBALL),
        PowerButtonSpec(binding.btnPowerFreeze, PowerType.FREEZE),
        PowerButtonSpec(binding.btnPowerLightning, PowerType.LIGHTNING),
    )

    fun apply(intent: Intent, activity: MainActivity) {
        val difficulty = intent.getIntExtra("difficulty", 1)
        engine.applyDifficulty(difficulty)

        if (difficulty == 5) {
            engine.setupRandomizer()
        }

        val endlessSubDiff = intent.getIntExtra("endless_sub_difficulty", -1)
        if (difficulty == 3 && endlessSubDiff >= 0) {
            engine.applyEndlessSubDifficulty(endlessSubDiff)
        }

        configureCampaign(intent, activity)
        configureLoadout(intent, difficulty)
        configureDailyChallenge(intent)
        configureMap(intent)
        configureContinue(intent)
    }

    private fun configureCampaign(intent: Intent, activity: MainActivity) {
        val campaignLevelId = intent.getIntExtra("campaign_level", -1)
        val campaignLevel = CampaignData.levels.find { it.id == campaignLevelId }
            ?: return

        engine.applyCampaign(campaignLevel)
        applyAllowedTowers(campaignLevel.allowedTowers)
        applyAllowedPowers(campaignLevel.allowedPowers)

        if (!campaignLevel.upgradesEnabled) {
            binding.btnUpDamage.visibility = View.GONE
            binding.btnUpSpeed.visibility = View.GONE
            binding.btnUpHp.visibility = View.GONE
            binding.btnUpBase.visibility = View.GONE
            binding.btnUpTower.visibility = View.GONE
        }

        if (campaignLevel.hint.isNotEmpty()) {
            Toast.makeText(
                activity,
                GameStrings.hintToast(campaignLevel.hint),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    private fun configureLoadout(intent: Intent, difficulty: Int) {
        val loadoutTowerNames = intent.getStringArrayExtra("loadout_towers") ?: return
        if (difficulty != 9) {
            return
        }

        val loadoutSet = loadoutTowerNames.mapNotNull { name ->
            runCatching { TowerType.valueOf(name) }.getOrNull()
        }.toSet()

        engine.loadoutTowers = loadoutSet
        applyAllowedTowers(loadoutSet)
    }

    private fun configureDailyChallenge(intent: Intent) {
        if (intent.getBooleanExtra("daily_challenge", false)) {
            engine.setupDailyChallenge()
        }
    }

    private fun configureMap(intent: Intent) {
        val mapName = intent.getStringExtra("map_type") ?: return
        engine.mapType = runCatching { MapType.valueOf(mapName) }.getOrDefault(engine.mapType)
    }

    private fun configureContinue(intent: Intent) {
        if (intent.getBooleanExtra("continue_game", false)) {
            engine.loadGame()
        }
    }

    private fun applyAllowedTowers(allowed: Set<TowerType>) {
        towerButtons.forEach { spec ->
            spec.button.visibility = if (spec.type in allowed) View.VISIBLE else View.GONE
        }
    }

    private fun applyAllowedPowers(allowed: Set<PowerType>) {
        powerButtons.forEach { spec ->
            spec.button.visibility = if (spec.type in allowed) View.VISIBLE else View.GONE
        }
        binding.powersBar.visibility = if (allowed.isEmpty()) View.GONE else View.VISIBLE
    }
}
