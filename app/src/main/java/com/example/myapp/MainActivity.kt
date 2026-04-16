package com.example.myapp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.game.CampaignData
import com.example.myapp.game.CampaignLevel
import com.example.myapp.game.GameEngine
import com.example.myapp.game.MapType
import com.example.myapp.game.PowerType
import com.example.myapp.game.TowerType
import java.util.Locale

class MainActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityMainBinding
    private var shouldShowReturnMenu = false
    private var suppressAutoSave = false

    override fun onStart() {
        super.onStart()
        findViewById<View>(R.id.btn_tutorial)?.setOnClickListener {
            TutorialDialog.show(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        SoundManager.init(this)

        val gameView = binding.gameView
        val engine = gameView.getEngine()
        val isContinue = intent.getBooleanExtra("continue_game", false)
        val difficulty = intent.getIntExtra("difficulty", 1)
        val requestedMapType = intent.getStringExtra("map_type")?.let { mapName ->
            runCatching { MapType.valueOf(mapName) }.getOrNull()
        }
        val campaignLevel = intent.getIntExtra("campaign_level", -1)
            .takeIf { it > 0 }
            ?.let { levelId -> CampaignData.levels.find { it.id == levelId } }

        engine.applyDifficulty(difficulty)

        if (difficulty == 5 && !isContinue) {
            engine.setupRandomizer()
        }

        val endlessSubDifficulty = intent.getIntExtra("endless_sub_difficulty", -1)
        if (difficulty == 3 && endlessSubDifficulty >= 0 && !isContinue) {
            engine.applyEndlessSubDifficulty(endlessSubDifficulty)
        }

        if (campaignLevel != null && !isContinue) {
            engine.applyCampaign(campaignLevel)
        }

        val isDaily = intent.getBooleanExtra("daily_challenge", false)
        if (isDaily && !isContinue) {
            engine.setupDailyChallenge()
        }

        bindHudCallbacks(gameView)
        bindTowerButtons(gameView, engine)
        bindPowerButtons(engine)
        bindUpgradeButtons(gameView, engine)
        bindUtilityButtons(engine)

        gameView.onCampaignVictory = { finish() }

        syncModeUi(engine, showBriefing = campaignLevel != null && !isContinue)
        refreshHud(engine)

        gameView.whenReady {
            if (!isContinue && requestedMapType != null) {
                engine.applyMapType(requestedMapType)
                gameView.refreshMapPresentation()
            }

            if (isContinue && engine.loadGame()) {
                gameView.refreshMapPresentation()
                syncModeUi(engine, showBriefing = false)
            }

            refreshHud(engine)
        }
    }

    private fun bindHudCallbacks(gameView: com.example.myapp.game.GameView) {
        gameView.onGoldChanged = { gold ->
            binding.textGold.text = "Gold $gold"
            updateUpgradeCosts()
        }
        gameView.onWaveChanged = { wave ->
            binding.textWave.text = "Wave $wave"
        }
        gameView.onDiamondsChanged = { diamonds ->
            binding.textKills.text = "Diamonds $diamonds"
        }
        gameView.onGameOver = { score, wave ->
            binding.textGold.text = "Game Over"
            binding.textWave.text = "Wave $wave  Score $score"
        }

        gameView.onTowerSelected = { tower ->
            binding.btnTarget.text = "Target: ${tower?.targetingMode?.label ?: "Close"}"
            binding.btnAbility.text = if (tower != null) {
                "Ability: ${tower.type.abilityName}"
            } else {
                "Ability"
            }
        }
    }

    private fun bindTowerButtons(gameView: com.example.myapp.game.GameView, engine: GameEngine) {
        binding.btnTowerArrow.setOnClickListener {
            selectTowerPlacement(gameView, engine, TowerType.ARROW, "Arrow")
        }
        binding.btnTowerMagic.setOnClickListener {
            selectTowerPlacement(gameView, engine, TowerType.MAGIC, "Magic")
        }
        binding.btnTowerCannon.setOnClickListener {
            selectTowerPlacement(gameView, engine, TowerType.CANNON, "Cannon")
        }
        binding.btnTowerPoison.setOnClickListener {
            selectTowerPlacement(gameView, engine, TowerType.POISON, "Poison")
        }
        binding.btnTowerTesla.setOnClickListener {
            selectTowerPlacement(gameView, engine, TowerType.TESLA, "Tesla")
        }
        binding.btnTowerIce.setOnClickListener {
            selectTowerPlacement(gameView, engine, TowerType.ICE, "Ice")
        }
    }

    private fun bindPowerButtons(engine: GameEngine) {
        binding.btnPowerFireball.setOnClickListener {
            usePower(engine, PowerType.FIREBALL, "Fireball unleashed.")
        }
        binding.btnPowerFreeze.setOnClickListener {
            usePower(engine, PowerType.FREEZE, "Freeze cast. Enemies are slowed.")
        }
        binding.btnPowerHeal.setOnClickListener {
            usePower(engine, PowerType.HEAL, "Base restored.")
        }
        binding.btnPowerLightning.setOnClickListener {
            usePower(engine, PowerType.LIGHTNING, "Lightning chained through the wave.")
        }
    }

    private fun bindUpgradeButtons(
        gameView: com.example.myapp.game.GameView,
        engine: GameEngine
    ) {
        binding.btnUpDamage.setOnClickListener {
            val cost = engine.playerDamageLevel * 25
            if (engine.upgradePlayerDamage()) {
                toast("Hero attack upgraded for ${cost}g.")
                updateUpgradeCosts()
            } else {
                toast("Need ${cost}g.")
            }
        }

        binding.btnUpSpeed.setOnClickListener {
            val cost = engine.playerSpeedLevel * 20
            if (engine.upgradePlayerSpeed()) {
                toast("Hero speed upgraded for ${cost}g.")
                updateUpgradeCosts()
            } else {
                toast("Need ${cost}g.")
            }
        }

        binding.btnUpHp.setOnClickListener {
            val cost = engine.playerHpLevel * 30
            if (engine.upgradePlayerHp()) {
                toast("Hero HP upgraded for ${cost}g.")
                updateUpgradeCosts()
            } else {
                toast("Need ${cost}g.")
            }
        }

        binding.btnUpBase.setOnClickListener {
            val cost = engine.baseHpLevel * 40
            if (engine.upgradeBaseHp()) {
                toast("Base HP upgraded for ${cost}g.")
                updateUpgradeCosts()
            } else {
                toast("Need ${cost}g.")
            }
        }

        binding.btnUpTower.setOnClickListener {
            val selectedTower = gameView.getSelectedTower()
            if (selectedTower == null) {
                toast("Tap a tower first.")
                return@setOnClickListener
            }

            val cost = selectedTower.upgradeCost()
            if (engine.upgradeTower(selectedTower)) {
                toast("Tower upgraded to level ${selectedTower.level} for ${cost}g.")
            } else {
                toast("Need ${cost}g.")
            }
        }

        binding.btnTarget.setOnClickListener {
            val selectedTower = gameView.getSelectedTower()
            if (selectedTower == null) {
                toast("Tap a tower first.")
                return@setOnClickListener
            }

            selectedTower.targetingMode = selectedTower.targetingMode.next()
            binding.btnTarget.text = "Target: ${selectedTower.targetingMode.label}"
            toast("Targeting changed to ${selectedTower.targetingMode.label}.")
        }

        binding.btnSell.setOnClickListener {
            val selectedTower = gameView.getSelectedTower()
            if (selectedTower == null) {
                toast("Tap a tower first.")
                return@setOnClickListener
            }

            val value = (selectedTower.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
            engine.sellTower(selectedTower)
            gameView.clearSelectedTower()
            toast("Tower sold for ${value}g.")
        }

        binding.btnAbility.setOnClickListener {
            val selectedTower = gameView.getSelectedTower()
            if (selectedTower == null) {
                toast("Tap a tower first.")
                return@setOnClickListener
            }

            if (engine.activateTowerAbility(selectedTower)) {
                toast("${selectedTower.type.abilityName} activated.")
            } else {
                toast("Cooldown: ${selectedTower.abilityTimer.toInt()}s")
            }
        }
    }

    private fun bindUtilityButtons(engine: GameEngine) {
        binding.btnDash.setOnClickListener {
            if (engine.dashCooldown > 0f) {
                toast("Dash cooldown: ${engine.dashCooldown.toInt()}s")
                return@setOnClickListener
            }

            if (engine.playerDash(engine.player.targetX, engine.player.targetY)) {
                toast("Dash executed.")
            } else {
                toast("Dash is not available right now.")
            }
        }

        binding.btnRepair.setOnClickListener {
            when {
                engine.repairBase() -> toast("Base repaired.")
                engine.baseHp >= engine.maxBaseHp -> toast("Base is already at full HP.")
                else -> toast("Not enough gold.")
            }
        }

        binding.btnSpeed.setOnClickListener {
            engine.gameSpeed = if (engine.gameSpeed == 2) 1 else 2
            updateSpeedButtons()
            toast("Speed set to ${engine.gameSpeed}x.")
        }

        binding.btnSpeed3.setOnClickListener {
            engine.gameSpeed = if (engine.gameSpeed == 3) 1 else 3
            updateSpeedButtons()
            toast("Speed set to ${engine.gameSpeed}x.")
        }

        binding.btnPause.setOnClickListener {
            engine.isPaused = !engine.isPaused
            updatePauseButton()
        }
    }

    private fun selectTowerPlacement(
        gameView: com.example.myapp.game.GameView,
        engine: GameEngine,
        type: TowerType,
        label: String
    ) {
        val cost = engine.getTowerCost(type)
        if (engine.gold < cost) {
            toast("Not enough gold.")
            return
        }

        gameView.placementMode = type
        toast("Tap the field to place $label for ${cost}g.")
    }

    private fun usePower(engine: GameEngine, type: PowerType, successMessage: String) {
        val cooldown = engine.getPowerCooldown(type)
        if (cooldown > 0f) {
            toast("Cooldown: ${cooldown.toInt()}s")
            return
        }

        if (engine.usePower(type)) {
            toast(successMessage)
        } else {
            toast("Need ${type.cost}g.")
        }
    }

    private fun syncModeUi(engine: GameEngine, showBriefing: Boolean) {
        val level = engine.campaignLevel
        val allowedTowers = level?.allowedTowers ?: TowerType.entries.toSet()
        val allowedPowers = level?.allowedPowers ?: PowerType.entries.toSet()
        val upgradesEnabled = level?.upgradesEnabled != false

        binding.btnTowerArrow.visibility = if (TowerType.ARROW in allowedTowers) View.VISIBLE else View.GONE
        binding.btnTowerMagic.visibility = if (TowerType.MAGIC in allowedTowers) View.VISIBLE else View.GONE
        binding.btnTowerCannon.visibility = if (TowerType.CANNON in allowedTowers) View.VISIBLE else View.GONE
        binding.btnTowerPoison.visibility = if (TowerType.POISON in allowedTowers) View.VISIBLE else View.GONE
        binding.btnTowerTesla.visibility = if (TowerType.TESLA in allowedTowers) View.VISIBLE else View.GONE
        binding.btnTowerIce.visibility = if (TowerType.ICE in allowedTowers) View.VISIBLE else View.GONE

        binding.btnPowerFireball.visibility = if (PowerType.FIREBALL in allowedPowers) View.VISIBLE else View.GONE
        binding.btnPowerFreeze.visibility = if (PowerType.FREEZE in allowedPowers) View.VISIBLE else View.GONE
        binding.btnPowerHeal.visibility = if (PowerType.HEAL in allowedPowers) View.VISIBLE else View.GONE
        binding.btnPowerLightning.visibility = if (PowerType.LIGHTNING in allowedPowers) View.VISIBLE else View.GONE
        binding.powersBar.visibility = if (allowedPowers.isEmpty()) View.GONE else View.VISIBLE

        val upgradeVisibility = if (upgradesEnabled) View.VISIBLE else View.GONE
        binding.btnUpDamage.visibility = upgradeVisibility
        binding.btnUpSpeed.visibility = upgradeVisibility
        binding.btnUpHp.visibility = upgradeVisibility
        binding.btnUpBase.visibility = upgradeVisibility
        binding.btnUpTower.visibility = upgradeVisibility

        if (showBriefing && level != null) {
            binding.root.post { showCampaignBriefing(engine, level) }
        }
    }

    private fun refreshHud(engine: GameEngine) {
        binding.textGold.text = if (engine.gameOver) "Game Over" else "Gold ${engine.gold}"
        binding.textWave.text = "Wave ${engine.wave}"
        binding.textKills.text = "Diamonds ${engine.diamondsEarnedThisRun}"
        binding.btnTarget.text = "Target: Close"
        binding.btnAbility.text = "Ability"
        updateUpgradeCosts()
        updateSpeedButtons()
        updatePauseButton()
    }

    private fun updateUpgradeCosts() {
        val engine = binding.gameView.getEngine()
        binding.btnUpDamage.text = "ATK\n${engine.playerDamageLevel * 25}g"
        binding.btnUpSpeed.text = "SPD\n${engine.playerSpeedLevel * 20}g"
        binding.btnUpHp.text = "HP\n${engine.playerHpLevel * 30}g"
        binding.btnUpBase.text = "BASE\n${engine.baseHpLevel * 40}g"
    }

    private fun updateSpeedButtons() {
        val speed = binding.gameView.getEngine().gameSpeed
        binding.btnSpeed.alpha = if (speed == 2) 1f else 0.5f
        binding.btnSpeed3.alpha = if (speed == 3) 1f else 0.5f
    }

    private fun updatePauseButton() {
        val engine = binding.gameView.getEngine()
        binding.btnPause.text = if (engine.isPaused) "Resume" else "Pause"
        binding.btnPause.backgroundTintList = ColorStateList.valueOf(
            if (engine.isPaused) 0xFF4CAF50.toInt() else 0xFFD32F2F.toInt()
        )
    }

    private fun showReturnMenu() {
        val engine = binding.gameView.getEngine()
        if (!hasActiveRun(engine) || isFinishing) return

        shouldShowReturnMenu = false
        engine.isPaused = true
        updatePauseButton()

        val options = arrayOf("Resume run", "Restart run", "Help", "Leave run")
        AlertDialog.Builder(this)
            .setTitle("Welcome back")
            .setMessage("Your run was restored. What would you like to do?")
            .setCancelable(false)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        engine.isPaused = false
                        updatePauseButton()
                    }
                    1 -> {
                        engine.restart()
                        engine.saveGame()
                        binding.gameView.refreshMapPresentation()
                        syncModeUi(engine, showBriefing = engine.campaignLevel != null)
                        refreshHud(engine)
                    }
                    2 -> {
                        startActivity(Intent(this, HelpActivity::class.java))
                    }
                    3 -> {
                        shouldShowReturnMenu = false
                        suppressAutoSave = true
                        engine.clearSave()
                        finish()
                    }
                }
            }
            .show()
    }

    private fun showCampaignBriefing(engine: GameEngine, level: CampaignLevel) {
        engine.isPaused = true
        updatePauseButton()

        val previousLevel = CampaignData.levels.getOrNull(level.id - 2)
        AlertDialog.Builder(this)
            .setTitle("${level.emoji} Level ${level.id}: ${level.title}")
            .setMessage(buildCampaignBriefing(level, previousLevel))
            .setCancelable(false)
            .setPositiveButton("Start") { dialog, _ ->
                engine.isPaused = false
                updatePauseButton()
                dialog.dismiss()
            }
            .show()
    }

    private fun buildCampaignBriefing(level: CampaignLevel, previousLevel: CampaignLevel?): String {
        val lines = mutableListOf<String>()
        lines += level.description
        lines += ""
        lines += "Objective"
        lines += "- Survive ${level.targetWave} waves"
        lines += "- Start with ${level.startingGold}g"
        lines += "- Reward: ${level.diamondReward} diamonds"

        val newThings = mutableListOf<String>()
        if (previousLevel == null) {
            newThings += "Learn tower placement, path control, and base defense."
        }

        (level.allowedTowers - (previousLevel?.allowedTowers ?: emptySet()))
            .sortedBy { it.ordinal }
            .forEach { tower ->
                newThings += "Unlocked ${towerLabel(tower)} tower."
            }

        (level.allowedPowers - (previousLevel?.allowedPowers ?: emptySet()))
            .sortedBy { it.ordinal }
            .forEach { power ->
                newThings += "Unlocked ${power.displayName}."
            }

        if (level.upgradesEnabled && previousLevel?.upgradesEnabled == false) {
            newThings += "Hero and tower upgrades are now available."
        }

        if (newThings.isNotEmpty()) {
            lines += ""
            lines += "New Here"
            newThings.forEach { lines += "- $it" }
        }

        val rules = mutableListOf<String>()
        if (level.allowedTowers.size != TowerType.entries.size) {
            rules += "Available towers: ${level.allowedTowers.sortedBy { it.ordinal }.joinToString(", ") { towerLabel(it) }}"
        }
        if (level.allowedPowers.isEmpty()) {
            rules += "Powers are disabled in this level."
        } else if (level.allowedPowers.size != PowerType.entries.size) {
            rules += "Available powers: ${level.allowedPowers.sortedBy { it.ordinal }.joinToString(", ") { it.displayName }}"
        }
        if (!level.upgradesEnabled) {
            rules += "Hero and tower upgrades are disabled."
        }
        if (level.enemyHpMult > 1f) {
            rules += "Enemies have ${(level.enemyHpMult * 100 - 100).toInt()}% more HP."
        } else if (level.enemyHpMult < 1f) {
            rules += "Enemies are weaker than normal."
        }
        if (level.enemySpeedMult > 1f) {
            rules += "Enemies move ${(level.enemySpeedMult * 100 - 100).toInt()}% faster."
        } else if (level.enemySpeedMult < 1f) {
            rules += "Enemies move slower than normal."
        }
        if (level.enemyDmgMult > 1f) {
            rules += "Enemies deal extra base damage."
        } else if (level.enemyDmgMult < 1f) {
            rules += "Base takes reduced damage from leaks."
        }
        if (level.goldMult < 1f) {
            rules += "Gold income is reduced to ${(level.goldMult * 100).toInt()}%."
        } else if (level.goldMult > 1f) {
            rules += "Gold income is boosted to ${(level.goldMult * 100).toInt()}%."
        }
        if (level.spawnRateMult > 1f) {
            rules += "Enemy waves spawn ${(level.spawnRateMult * 100).toInt()}% as fast."
        }

        when (level.id) {
            5 -> rules += "First boss arrives on wave 5."
            13 -> rules += "Bosses appear every 3 waves in this level."
            15 -> rules += "Watch for the dangerous night cycle."
            16 -> rules += "Expect swarm pressure from many weak enemies."
            20 -> rules += "Final campaign challenge: everything is turned up."
        }

        if (rules.isNotEmpty()) {
            lines += ""
            lines += "Special Rules"
            rules.forEach { lines += "- $it" }
        }

        if (level.hint.isNotEmpty()) {
            lines += ""
            lines += "Tip"
            lines += "- ${level.hint}"
        }

        lines += ""
        lines += "Tap Start when you are ready."
        return lines.joinToString("\n")
    }

    private fun towerLabel(tower: TowerType): String {
        val name = tower.name.lowercase(Locale.getDefault()).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        return name
    }

    private fun hasActiveRun(engine: GameEngine): Boolean {
        return !engine.gameOver && !engine.campaignVictory && engine.wave > 0
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        binding.gameView.pause()

        val engine = binding.gameView.getEngine()
        val activeRun = hasActiveRun(engine)
        if (activeRun && !suppressAutoSave) {
            engine.saveGame()
        }
        shouldShowReturnMenu = activeRun && !isFinishing
    }

    override fun onResume() {
        super.onResume()
        binding.gameView.resume()
        suppressAutoSave = false

        if (shouldShowReturnMenu) {
            binding.root.post { showReturnMenu() }
        }
    }
}
