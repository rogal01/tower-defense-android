package com.example.myapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityMainBinding
import android.graphics.Color
import android.content.Intent
import android.view.HapticFeedbackConstants
import com.example.myapp.game.CampaignData
import com.example.myapp.game.EndlessBuff
import com.example.myapp.game.MerchantCard
import com.example.myapp.game.PathTopology
import com.example.myapp.game.PowerType
import com.example.myapp.game.SfxType
import com.example.myapp.game.TowerType
import com.example.myapp.game.TrapType

class MainActivity : ImmersiveActivity() {
    private val S get() = GameStrings
    private var sellPendingTime = 0L

    // Cooldown UI refresh (~2/sec)
    private val cooldownHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val cooldownRunnable = object : Runnable {
        override fun run() {
            updatePowerButtons()
            val sel = runCatching { binding.gameView.getSelectedTower() }.getOrNull()
            if (sel != null && binding.layoutTowerInspector.visibility == View.VISIBLE) {
                updateTowerInspector(sel)
            }
            cooldownHandler.postDelayed(this, 500)
        }
    }

    private fun updatePowerButtons() {
        val engine = runCatching { binding.gameView.getEngine() }.getOrNull() ?: return
        fun applyOrb(btn: com.example.myapp.game.HeroPowerOrbView, type: com.example.myapp.game.PowerType, emoji: String, haloColor: Int) {
            val cd = engine.getPowerCooldown(type)
            btn.setPowerState(emoji, cd, type.cooldown, haloColor)
        }
        applyOrb(binding.btnPowerFireball,  com.example.myapp.game.PowerType.FIREBALL,  "🔥", 0xFFFF7043.toInt())
        applyOrb(binding.btnPowerFreeze,    com.example.myapp.game.PowerType.FREEZE,    "❄️", 0xFF00E5FF.toInt())
        applyOrb(binding.btnPowerHeal,      com.example.myapp.game.PowerType.HEAL,      "💚", 0xFF00E676.toInt())
        applyOrb(binding.btnPowerLightning, com.example.myapp.game.PowerType.LIGHTNING, "⚡", 0xFFFFD700.toInt())
    }

    override fun onStart() {
        super.onStart()
        findViewById<View>(R.id.btn_tutorial)?.setOnClickListener {
            TutorialDialog.show(this)
        }
        findViewById<View>(R.id.btn_codex)?.setOnClickListener {
            FusionCodexDialog.show(this, binding.gameView.getEngine().discoveredFusions)
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Request 120 FPS high refresh rate on supported displays
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.attributes = window.attributes.apply {
                preferredRefreshRate = 120f
            }
        }

        SoundManager.init(this)
        GameStrings.init(this)

        val gameView = binding.gameView
        val engine = gameView.getEngine()

        val difficulty = intent.getIntExtra("difficulty", 1)
        engine.applyDifficulty(difficulty)

        // Randomizer mode: scramble everything
        if (difficulty == 5) {
            engine.setupRandomizer()
        }

        // Endless sub-difficulty: apply difficulty scaling while keeping endless mode
        val endlessSubDiff = intent.getIntExtra("endless_sub_difficulty", -1)
        if (difficulty == 3 && endlessSubDiff >= 0) {
            engine.applyEndlessSubDifficulty(endlessSubDiff)
        }

        // Campaign mode setup
        val campaignLevelId = intent.getIntExtra("campaign_level", -1)
        val campaignLevel = if (campaignLevelId > 0) CampaignData.levels.find { it.id == campaignLevelId } else null
        if (campaignLevel != null) {
            val isHeroic = intent.getBooleanExtra("campaign_heroic", false)
            engine.applyCampaign(campaignLevel, isHeroic = isHeroic)
            if (isHeroic) {
                Toast.makeText(this, GameStrings.heroicChallenge, Toast.LENGTH_SHORT).show()
            }

            // Hide tower buttons not allowed
            binding.btnTowerArrow.visibility = if (TowerType.ARROW in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerMagic.visibility = if (TowerType.MAGIC in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerCannon.visibility = if (TowerType.CANNON in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerPoison.visibility = if (TowerType.POISON in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerTesla.visibility = if (TowerType.TESLA in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerIce.visibility = if (TowerType.ICE in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerFlame.visibility = if (TowerType.FLAME in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerNecro.visibility = if (TowerType.NECRO in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerBallista.visibility = if (TowerType.BALLISTA in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerVortex.visibility = if (TowerType.VORTEX in campaignLevel.allowedTowers) View.VISIBLE else View.GONE
            binding.btnTowerHealer.visibility = if (TowerType.HEALER in campaignLevel.allowedTowers) View.VISIBLE else View.GONE

            // Hide power buttons not allowed
            binding.btnPowerFireball.visibility = if (PowerType.FIREBALL in campaignLevel.allowedPowers) View.VISIBLE else View.GONE
            binding.btnPowerFreeze.visibility = if (PowerType.FREEZE in campaignLevel.allowedPowers) View.VISIBLE else View.GONE
            binding.btnPowerHeal.visibility = if (PowerType.HEAL in campaignLevel.allowedPowers) View.VISIBLE else View.GONE
            binding.btnPowerLightning.visibility = if (PowerType.LIGHTNING in campaignLevel.allowedPowers) View.VISIBLE else View.GONE

            // Hide powers bar entirely if no powers allowed
            if (campaignLevel.allowedPowers.isEmpty()) {
                binding.powersBar.visibility = View.GONE
            }

            // Hide upgrade buttons if upgrades disabled
            if (!campaignLevel.upgradesEnabled) {
                binding.btnUpDamage.visibility = View.GONE
                binding.btnUpSpeed.visibility = View.GONE
                binding.btnUpHp.visibility = View.GONE
                binding.btnUpBase.visibility = View.GONE
                binding.btnUpTower.visibility = View.GONE
            }

            // Show hint
            if (campaignLevel.hint.isNotEmpty()) {
                Toast.makeText(this, S.hintToast(campaignLevel.hint), Toast.LENGTH_LONG).show()
            }
        }

        // Loadout mode — restrict towers to player selection
        val loadoutTowerNames = intent.getStringArrayExtra("loadout_towers")
        if (difficulty == 9 && loadoutTowerNames != null) {
            val loadoutSet = loadoutTowerNames.mapNotNull { name ->
                try { TowerType.valueOf(name) } catch (_: Exception) { null }
            }.toSet()
            engine.loadoutTowers = loadoutSet
            binding.btnTowerArrow.visibility = if (TowerType.ARROW in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerMagic.visibility = if (TowerType.MAGIC in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerCannon.visibility = if (TowerType.CANNON in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerPoison.visibility = if (TowerType.POISON in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerTesla.visibility = if (TowerType.TESLA in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerIce.visibility = if (TowerType.ICE in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerFlame.visibility = if (TowerType.FLAME in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerNecro.visibility = if (TowerType.NECRO in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerBallista.visibility = if (TowerType.BALLISTA in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerVortex.visibility = if (TowerType.VORTEX in loadoutSet) View.VISIBLE else View.GONE
            binding.btnTowerHealer.visibility = if (TowerType.HEALER in loadoutSet) View.VISIBLE else View.GONE
        }

        // Campaign victory
        gameView.onCampaignVictory = {
            runOnUiThread {
                showBattleResultsDialog(isVictory = true, score = engine.score, wave = engine.wave)
            }
        }

        // HUD updates from game thread
        gameView.onGoldChanged = { gold ->
            binding.textGold.text = S.goldHud(gold)
            updateUpgradeCosts()
        }
        gameView.onWaveChanged = { wave ->
            binding.textWave.text = S.waveHud(wave)
            if (!engine.gameOver && !engine.campaignVictory && wave > 0) {
                engine.saveGame()
            }
        }
        gameView.onDiamondsChanged = { diamonds ->
            binding.textDiamonds.text = S.diamondsHud(diamonds)
        }
        gameView.onKillsChanged = { kills ->
            binding.textKills.text = S.killsHud(kills)
        }
        binding.textGold.text = S.goldHud(engine.gold)
        binding.textWave.text = S.waveHud(engine.wave)
        binding.textDiamonds.text = S.diamondsHud(engine.skillTree.diamonds)
        binding.textKills.text = S.killsHud(engine.totalKills)
        gameView.onWaveStateChanged = { inProgress, waveTimer ->
            updateCallWaveButton(inProgress, waveTimer)
        }
        gameView.onGameOver = { score, wave ->
            binding.textGold.text = S.gameOverHud(score, wave)
            binding.textWave.text = S.gameOverWaveHud(score, wave)
            showBattleResultsDialog(isVictory = false, score = score, wave = wave)
        }

        // Setup bottom categorized dock tabs
        setupCategoryTabs()

        // Setup placement cancellation & inspector close
        binding.btnCancelPlacement.setOnClickListener {
            exitPlacement()
        }
        binding.btnInspectorClose.setOnClickListener {
            gameView.clearSelectedTower()
        }

        // Placement mode change callback from GameView
        gameView.onPlacementModeChanged = { isPlacing ->
            if (!isPlacing) {
                binding.layoutPlacementBar.visibility = View.GONE
                if (gameView.getSelectedTower() != null) {
                    binding.layoutTowerInspector.visibility = View.VISIBLE
                } else {
                    binding.layoutBuildDock.visibility = View.VISIBLE
                }
            }
        }

        // Selected tower callback from GameView
        gameView.onTowerSelected = { tower ->
            updateTowerInspector(tower)
        }

        // Tower placement buttons
        fun bindTowerBtn(btn: android.widget.Button, type: TowerType, name: String, emoji: String) {
            btn.setOnClickListener {
                val cost = engine.getTowerCost(type)
                startPlacement(name, emoji, cost) {
                    gameView.placementMode = type
                }
            }
        }
        bindTowerBtn(binding.btnTowerArrow,    TowerType.ARROW,    "Arrow",    "🏹")
        bindTowerBtn(binding.btnTowerMagic,    TowerType.MAGIC,    "Magic",    "🧨")
        bindTowerBtn(binding.btnTowerCannon,   TowerType.CANNON,   "Cannon",   "💣")
        bindTowerBtn(binding.btnTowerPoison,   TowerType.POISON,   "Poison",   "☠️")
        bindTowerBtn(binding.btnTowerTesla,    TowerType.TESLA,    "Tesla",    "⚡")
        bindTowerBtn(binding.btnTowerIce,      TowerType.ICE,      "Ice",      "❄️")
        bindTowerBtn(binding.btnTowerFlame,    TowerType.FLAME,    "Flame",    "🔥")
        bindTowerBtn(binding.btnTowerNecro,    TowerType.NECRO,    "Necro",    "💀")
        bindTowerBtn(binding.btnTowerBallista, TowerType.BALLISTA, "Ballista", "🎯")
        bindTowerBtn(binding.btnTowerVortex,   TowerType.VORTEX,   "Vortex",   "🌀")
        bindTowerBtn(binding.btnTowerHealer,   TowerType.HEALER,   "Healer",   "💚")

        // Power buttons
        binding.btnPowerFireball.setOnClickListener {
            runCatching { binding.btnPowerFireball.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            val cd = engine.getPowerCooldown(PowerType.FIREBALL)
            if (cd > 0) { Toast.makeText(this, S.cooldownFmt(cd.toInt()), Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FIREBALL)) {
                Toast.makeText(this, S.fireballUsed, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.fireballNeedGold(PowerType.FIREBALL.cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerFreeze.setOnClickListener {
            runCatching { binding.btnPowerFreeze.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            val cd = engine.getPowerCooldown(PowerType.FREEZE)
            if (cd > 0) { Toast.makeText(this, S.cooldownFmt(cd.toInt()), Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FREEZE)) {
                Toast.makeText(this, S.freezeUsed, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.freezeNeedGold(PowerType.FREEZE.cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerHeal.setOnClickListener {
            runCatching { binding.btnPowerHeal.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            val cd = engine.getPowerCooldown(PowerType.HEAL)
            if (cd > 0) { Toast.makeText(this, S.cooldownFmt(cd.toInt()), Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.HEAL)) {
                Toast.makeText(this, S.healUsed, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.healNeedGold(PowerType.HEAL.cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerLightning.setOnClickListener {
            runCatching { binding.btnPowerLightning.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            val cd = engine.getPowerCooldown(PowerType.LIGHTNING)
            if (cd > 0) { Toast.makeText(this, S.cooldownFmt(cd.toInt()), Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.LIGHTNING)) {
                Toast.makeText(this, S.lightningUsed, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.lightningNeedGold(PowerType.LIGHTNING.cost), Toast.LENGTH_SHORT).show()
        }

        // Repair base
        binding.btnRepair.setOnClickListener {
            runCatching { binding.btnRepair.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            if (engine.repairBase()) {
                Toast.makeText(this, S.baseRepaired, Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else if (engine.baseHp >= engine.maxBaseHp) {
                Toast.makeText(this, S.baseFullHp, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }

        // Blockade placement
        binding.btnBlockade.setOnClickListener {
            val cost = engine.blockadeCost
            startPlacement("Blockade", "🧱", cost) {
                gameView.blockadePlacementMode = true
            }
        }

        // Trap placement buttons
        binding.btnTrapSpike.setOnClickListener {
            val cost = TrapType.SPIKE.cost
            startPlacement("Spike Trap", "🗡️", cost) {
                gameView.trapPlacementMode = TrapType.SPIKE
            }
        }
        binding.btnTrapTar.setOnClickListener {
            val cost = TrapType.TAR.cost
            startPlacement("Tar Trap", "🟣", cost) {
                gameView.trapPlacementMode = TrapType.TAR
            }
        }
        binding.btnTrapMine.setOnClickListener {
            val cost = TrapType.MINE.cost
            startPlacement("Landmine", "💣", cost) {
                gameView.trapPlacementMode = TrapType.MINE
            }
        }

        // Endless milestone buff callback
        gameView.onMilestoneBuff = {
            runOnUiThread { showMilestoneBuffDialog() }
        }

        // Wandering Merchant shop callback
        gameView.onMerchantShop = {
            runOnUiThread { showMerchantShopDialog() }
        }

        // Upgrade buttons
        binding.btnUpDamage.setOnClickListener {
            val cost = engine.getPlayerDamageCost()
            if (engine.upgradePlayerDamage()) {
                runCatching { binding.btnUpDamage.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                Toast.makeText(this, S.attackUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else if (engine.playerDamageLevel >= com.example.myapp.game.GameEngine.MAX_PLAYER_DAMAGE_LEVEL) {
                Toast.makeText(this, "Attack is already at MAX level!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnUpSpeed.setOnClickListener {
            val cost = engine.getPlayerSpeedCost()
            if (engine.upgradePlayerSpeed()) {
                runCatching { binding.btnUpSpeed.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                Toast.makeText(this, S.speedUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else if (engine.playerSpeedLevel >= com.example.myapp.game.GameEngine.MAX_PLAYER_SPEED_LEVEL) {
                Toast.makeText(this, "Speed is already at MAX level!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnUpHp.setOnClickListener {
            val cost = engine.getPlayerHpCost()
            if (engine.upgradePlayerHp()) {
                runCatching { binding.btnUpHp.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                Toast.makeText(this, S.hpUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else if (engine.playerHpLevel >= com.example.myapp.game.GameEngine.MAX_PLAYER_HP_LEVEL) {
                Toast.makeText(this, "Hero HP is already at MAX level!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnUpBase.setOnClickListener {
            val cost = engine.getBaseHpCost()
            if (engine.upgradeBaseHp()) {
                runCatching { binding.btnUpBase.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                Toast.makeText(this, S.baseUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else if (engine.baseHpLevel >= com.example.myapp.game.GameEngine.MAX_BASE_HP_LEVEL) {
                Toast.makeText(this, "Base HP is already at MAX level!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }

        // Selected Tower Inspector actions
        binding.btnUpTower.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                if (selected.level >= com.example.myapp.game.Tower.MAX_TOWER_LEVEL) {
                    Toast.makeText(this, "Tower is at MAX level (${com.example.myapp.game.Tower.MAX_TOWER_LEVEL})!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val cost = selected.upgradeCost()
                if (engine.upgradeTower(selected)) {
                    runCatching { binding.btnUpTower.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                    Toast.makeText(this, S.towerUpgraded(selected.level, cost), Toast.LENGTH_SHORT).show()
                    updateTowerInspector(selected)
                    updateUpgradeCosts()
                } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }
        binding.btnTarget.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                selected.targetingMode = selected.targetingMode.next()
                runCatching { binding.btnTarget.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                binding.btnTarget.text = "🎯 ${selected.targetingMode.label}"
                Toast.makeText(this, S.targetMode(selected.targetingMode.label), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }
        binding.btnSell.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                val value = (selected.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
                val now = System.currentTimeMillis()
                if (now - sellPendingTime < 2000L) {
                    engine.sellTower(selected)
                    gameView.clearSelectedTower()
                    runCatching { binding.btnSell.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                    Toast.makeText(this, S.soldTower(value), Toast.LENGTH_SHORT).show()
                    sellPendingTime = 0L
                    binding.btnSell.text = "💰 Sell\n+${value}g"
                    updateUpgradeCosts()
                } else {
                    sellPendingTime = now
                    binding.btnSell.text = "Confirm?\n+${value}g"
                    runCatching { binding.btnSell.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (System.currentTimeMillis() - sellPendingTime >= 2000L) {
                            val selNow = gameView.getSelectedTower()
                            if (selNow != null) {
                                val vNow = (selNow.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
                                binding.btnSell.text = "💰 Sell\n+${vNow}g"
                            }
                        }
                    }, 2100)
                }
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }
        binding.btnAbility.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                if (engine.activateTowerAbility(selected)) {
                    runCatching { binding.btnAbility.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                    Toast.makeText(this, S.abilityActivated(selected.type.abilityName), Toast.LENGTH_SHORT).show()
                } else {
                    val remaining = selected.abilityTimer.toInt()
                    Toast.makeText(this, S.cooldownFmt(remaining), Toast.LENGTH_SHORT).show()
                }
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }

        // Speed 1-tap cycle (1x -> 2x -> 3x -> 1x)
        val savedSpeed = engine.prefs.getInt("preferred_speed", 1).coerceIn(1, 3)
        engine.gameSpeed = savedSpeed
        updateSpeedButtons()

        binding.btnSpeed.setOnClickListener {
            engine.gameSpeed = when (engine.gameSpeed) {
                1 -> 2
                2 -> 3
                else -> 1
            }
            engine.prefs.edit().putInt("preferred_speed", engine.gameSpeed).apply()
            updateSpeedButtons()
            runCatching { binding.btnSpeed.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            Toast.makeText(this, S.speedToast(engine.gameSpeed), Toast.LENGTH_SHORT).show()
        }

        // Call wave early / rush
        binding.btnCallWave.setOnClickListener {
            val bounty = engine.callNextWaveEarly()
            if (bounty > 0) {
                runCatching { binding.btnCallWave.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
                Toast.makeText(this, "⚡ Wave Rushed! +${bounty}g Bonus", Toast.LENGTH_SHORT).show()
            }
        }

        // Pause
        binding.btnPause.setOnClickListener {
            engine.isPaused = !engine.isPaused
            runCatching { binding.btnPause.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            binding.btnPause.text = if (engine.isPaused) "▶" else "⏸"
            binding.btnPause.background = getDrawable(
                if (engine.isPaused) R.drawable.bg_btn_green else R.drawable.bg_btn_danger
            )
        }

        // Auto-wave toggle
        binding.btnAutoWave.setOnClickListener {
            engine.autoWave = !engine.autoWave
            updateAutoWaveButton()
            runCatching { binding.btnAutoWave.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
            Toast.makeText(this, if (engine.autoWave) "Auto-Wave ON" else "Auto-Wave OFF", Toast.LENGTH_SHORT).show()
        }
        updateAutoWaveButton()

        // Daily challenge setup
        val isDaily = intent.getBooleanExtra("daily_challenge", false)
        if (isDaily) {
            engine.setupDailyChallenge()
        }

        // Map type from intent
        val mapName = intent.getStringExtra("map_type")
        if (mapName != null) {
            try { engine.mapType = com.example.myapp.game.MapType.valueOf(mapName) } catch (_: Exception) {}
        }

        // Procedural path topology and map seed from intent
        val topologyName = intent.getStringExtra("topology")
        if (topologyName != null) {
            try { engine.selectedTopology = PathTopology.valueOf(topologyName) } catch (_: Exception) {}
        }
        val seed = intent.getLongExtra("map_seed", 0L)
        if (seed != 0L) {
            engine.mapSeed = seed
        }

        // Continue saved game if requested
        val isContinue = intent.getBooleanExtra("continue_game", false)
        if (isContinue) {
            engine.loadGame()
        }

        updateUpgradeCosts()
    }

    private fun setupCategoryTabs() {
        fun selectTab(selectedTab: android.widget.Button, activeContainer: View) {
            binding.containerTowers.visibility = if (activeContainer == binding.containerTowers) View.VISIBLE else View.GONE
            binding.containerTraps.visibility = if (activeContainer == binding.containerTraps) View.VISIBLE else View.GONE
            binding.containerHero.visibility = if (activeContainer == binding.containerHero) View.VISIBLE else View.GONE

            binding.tabTowers.background = getDrawable(if (selectedTab == binding.tabTowers) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.tabTowers.setTextColor(if (selectedTab == binding.tabTowers) Color.WHITE else Color.parseColor("#B0BEC5"))

            binding.tabTraps.background = getDrawable(if (selectedTab == binding.tabTraps) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.tabTraps.setTextColor(if (selectedTab == binding.tabTraps) Color.WHITE else Color.parseColor("#B0BEC5"))

            binding.tabHero.background = getDrawable(if (selectedTab == binding.tabHero) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.tabHero.setTextColor(if (selectedTab == binding.tabHero) Color.WHITE else Color.parseColor("#B0BEC5"))

            runCatching { selectedTab.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
        }

        binding.tabTowers.setOnClickListener { selectTab(binding.tabTowers, binding.containerTowers) }
        binding.tabTraps.setOnClickListener { selectTab(binding.tabTraps, binding.containerTraps) }
        binding.tabHero.setOnClickListener { selectTab(binding.tabHero, binding.containerHero) }
    }

    private fun startPlacement(name: String, emoji: String, cost: Int, onSetMode: () -> Unit) {
        val engine = binding.gameView.getEngine()
        if (engine.gold < cost) {
            Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
            return
        }
        binding.gameView.clearSelectedTower()
        onSetMode()
        binding.layoutBuildDock.visibility = View.GONE
        binding.layoutTowerInspector.visibility = View.GONE
        binding.layoutPlacementBar.visibility = View.VISIBLE
        binding.textPlacementInfo.text = "Placing: $emoji $name (${cost}g)"
        runCatching { binding.layoutPlacementBar.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    }

    private fun exitPlacement() {
        binding.gameView.clearPlacementMode()
        binding.layoutPlacementBar.visibility = View.GONE
        val selected = binding.gameView.getSelectedTower()
        if (selected != null) {
            binding.layoutTowerInspector.visibility = View.VISIBLE
        } else {
            binding.layoutBuildDock.visibility = View.VISIBLE
        }
        runCatching { binding.btnCancelPlacement.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
    }

    private fun updateTowerInspector(tower: com.example.myapp.game.Tower?) {
        if (tower == null) {
            binding.layoutTowerInspector.visibility = View.GONE
            if (binding.gameView.placementMode == null && !binding.gameView.blockadePlacementMode && binding.gameView.trapPlacementMode == null) {
                binding.layoutBuildDock.visibility = View.VISIBLE
            }
            return
        }

        binding.layoutPlacementBar.visibility = View.GONE
        binding.layoutBuildDock.visibility = View.GONE
        binding.layoutTowerInspector.visibility = View.VISIBLE

        val engine = binding.gameView.getEngine()
        val spec = tower.specialization
        val titleText = if (spec != com.example.myapp.game.TowerSpecialization.NONE) {
            "⭐ Lv.${tower.level} ${tower.type.displayName} · ${spec.emoji} ${spec.displayName}"
        } else {
            "⭐ Lv.${tower.level} ${tower.type.displayName}"
        }
        binding.inspectorTowerTitle.text = titleText
        val spd = (tower.fireRate * 10).toInt() / 10f
        binding.badgeStatDmg.text = "⚔️ ${tower.damage.toInt()}"
        binding.badgeStatRng.text = "🎯 ${tower.range.toInt()}"
        binding.badgeStatSpd.text = "⚡ ${spd}/s"
        binding.badgeStatKills.text = "💀 ${tower.totalKills}"

        if (tower.canSpecialize()) {
            binding.layoutTowerSpec.visibility = View.VISIBLE
            binding.btnSpecialize.setOnClickListener {
                showSpecializationDialog(tower)
            }
        } else {
            binding.layoutTowerSpec.visibility = View.GONE
        }

        if (tower.level >= com.example.myapp.game.Tower.MAX_TOWER_LEVEL) {
            binding.btnUpTower.text = "▲ Lv.MAX [MAX]"
            binding.btnUpTower.alpha = 0.45f
            binding.btnUpTower.isEnabled = false
        } else {
            val dmgDelta = (tower.type.baseDamage * 0.70f).toInt()
            val upCost = tower.upgradeCost()
            binding.btnUpTower.text = "▲ Lv.${tower.level + 1} [+$dmgDelta DMG] [💰 $upCost]"
            val canAfford = engine.gold >= upCost
            binding.btnUpTower.alpha = if (canAfford) 1.0f else 0.45f
            binding.btnUpTower.isEnabled = true
        }

        binding.btnTarget.text = "🎯 ${tower.targetingMode.label}"

        val sellVal = (tower.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
        binding.btnSell.text = "💸 Sell\n+${sellVal}g"

        val cd = tower.abilityTimer.toInt()
        if (cd > 0) {
            binding.btnAbility.text = "✨ ${tower.type.abilityName}\n(${cd}s)"
            binding.btnAbility.alpha = 0.45f
        } else {
            binding.btnAbility.text = "✨ ${tower.type.abilityName}"
            binding.btnAbility.alpha = 1.0f
        }
    }

    private fun showSpecializationDialog(tower: com.example.myapp.game.Tower) {
        val specs = tower.availableSpecializations()
        if (specs.isEmpty()) return
        val engine = binding.gameView.getEngine()

        val view = layoutInflater.inflate(R.layout.dialog_tower_specialization, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val towerBadge = view.findViewById<android.widget.TextView>(R.id.spec_tower_badge)
        towerBadge.text = "${tower.type.emoji} ${tower.type.displayName} · Level ${tower.level} Mastery"

        // Branch 1
        val s1 = specs[0]
        val spec1Emoji = view.findViewById<android.widget.TextView>(R.id.spec_1_emoji)
        val spec1Name = view.findViewById<android.widget.TextView>(R.id.spec_1_name)
        val spec1Desc = view.findViewById<android.widget.TextView>(R.id.spec_1_desc)
        val btnSpec1 = view.findViewById<android.widget.Button>(R.id.btn_choose_spec_1)
        val cardSpec1 = view.findViewById<android.view.View>(R.id.card_spec_1)

        spec1Emoji.text = s1.emoji
        spec1Name.text = s1.displayName
        spec1Desc.text = s1.description
        val pickS1 = {
            tower.applySpecialization(s1)
            engine.onTowerSpecialized(tower)
            SoundManager.play(com.example.myapp.game.SfxType.ACHIEVEMENT)
            Toast.makeText(this, "Specialized into ${s1.emoji} ${s1.displayName}!", Toast.LENGTH_SHORT).show()
            updateTowerInspector(tower)
            dialog.dismiss()
        }
        btnSpec1.setOnClickListener { pickS1() }
        cardSpec1.setOnClickListener { pickS1() }

        // Branch 2
        val cardSpec2 = view.findViewById<android.view.View>(R.id.card_spec_2)
        if (specs.size > 1) {
            val s2 = specs[1]
            val spec2Emoji = view.findViewById<android.widget.TextView>(R.id.spec_2_emoji)
            val spec2Name = view.findViewById<android.widget.TextView>(R.id.spec_2_name)
            val spec2Desc = view.findViewById<android.widget.TextView>(R.id.spec_2_desc)
            val btnSpec2 = view.findViewById<android.widget.Button>(R.id.btn_choose_spec_2)

            cardSpec2.visibility = View.VISIBLE
            spec2Emoji.text = s2.emoji
            spec2Name.text = s2.displayName
            spec2Desc.text = s2.description
            val pickS2 = {
                tower.applySpecialization(s2)
                engine.onTowerSpecialized(tower)
                SoundManager.play(com.example.myapp.game.SfxType.ACHIEVEMENT)
                Toast.makeText(this, "Specialized into ${s2.emoji} ${s2.displayName}!", Toast.LENGTH_SHORT).show()
                updateTowerInspector(tower)
                dialog.dismiss()
            }
            btnSpec2.setOnClickListener { pickS2() }
            cardSpec2.setOnClickListener { pickS2() }
        } else {
            cardSpec2.visibility = View.GONE
        }

        view.findViewById<android.widget.Button>(R.id.btn_spec_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun updateAutoWaveButton() {
        val engine = runCatching { binding.gameView.getEngine() }.getOrNull() ?: return
        if (engine.autoWave) {
            binding.btnAutoWave.text = "Auto ✓"
            binding.btnAutoWave.setTextColor(Color.parseColor("#4CAF50"))
            binding.btnAutoWave.background = getDrawable(R.drawable.bg_btn_green)
        } else {
            binding.btnAutoWave.text = "Auto"
            binding.btnAutoWave.setTextColor(Color.parseColor("#B0BEC5"))
            binding.btnAutoWave.background = getDrawable(R.drawable.bg_btn_action)
        }
    }

    private fun updateSpeedButtons() {
        val engine = runCatching { binding.gameView.getEngine() }.getOrNull() ?: return
        val s = engine.gameSpeed
        binding.btnSpeed.text = "${s}x"
        when (s) {
            1 -> {
                binding.btnSpeed.setTextColor(Color.parseColor("#B0BEC5"))
                binding.btnSpeed.background = getDrawable(R.drawable.bg_btn_action)
            }
            2 -> {
                binding.btnSpeed.setTextColor(Color.parseColor("#FFD54F"))
                binding.btnSpeed.background = getDrawable(R.drawable.bg_btn_primary)
            }
            3 -> {
                binding.btnSpeed.setTextColor(Color.parseColor("#FFFFFF"))
                binding.btnSpeed.background = getDrawable(R.drawable.bg_btn_danger)
            }
        }
    }

    private fun updateCallWaveButton(inProgress: Boolean, waveTimer: Float) {
        val engine = runCatching { binding.gameView.getEngine() }.getOrNull() ?: return
        if (engine.gameOver || engine.campaignVictory) {
            binding.btnCallWave.visibility = View.GONE
            return
        }
        binding.btnCallWave.visibility = View.VISIBLE
        if (!inProgress) {
            val bounty = engine.getEarlyWaveBounty()
            binding.btnCallWave.text = "▶▶ +${bounty}g"
            binding.btnCallWave.isEnabled = true
            binding.btnCallWave.alpha = 1.0f
            binding.btnCallWave.setTextColor(Color.parseColor("#FFD54F"))
            binding.btnCallWave.background = getDrawable(R.drawable.bg_btn_primary)
        } else {
            binding.btnCallWave.text = "▶▶ Rush"
            binding.btnCallWave.isEnabled = false
            binding.btnCallWave.alpha = 0.45f
            binding.btnCallWave.setTextColor(Color.parseColor("#B0BEC5"))
            binding.btnCallWave.background = getDrawable(R.drawable.bg_btn_action)
        }
    }

    private fun formatCost(title: String, cost: Int, canAfford: Boolean): CharSequence {
        val colorHex = if (canAfford) "#FFD700" else "#FF5252"
        return android.text.Html.fromHtml("$title<br/><font color='$colorHex'>${cost}g</font>", android.text.Html.FROM_HTML_MODE_LEGACY)
    }

    private fun updateUpgradeCosts() {
        val engine = runCatching { binding.gameView.getEngine() }.getOrNull() ?: return
        val gold = engine.gold

        // Hero Upgrades
        if (engine.playerDamageLevel >= com.example.myapp.game.GameEngine.MAX_PLAYER_DAMAGE_LEVEL) {
            binding.btnUpDamage.text = "⚔️ ATK\nMAX"
            binding.btnUpDamage.alpha = 0.45f
            binding.btnUpDamage.isEnabled = false
        } else {
            val dmgCost = engine.getPlayerDamageCost()
            val canAfford = gold >= dmgCost
            binding.btnUpDamage.text = formatCost("⚔️ ATK", dmgCost, canAfford)
            binding.btnUpDamage.alpha = if (canAfford) 1.0f else 0.45f
            binding.btnUpDamage.isEnabled = true
        }

        if (engine.playerSpeedLevel >= com.example.myapp.game.GameEngine.MAX_PLAYER_SPEED_LEVEL) {
            binding.btnUpSpeed.text = "👟 SPD\nMAX"
            binding.btnUpSpeed.alpha = 0.45f
            binding.btnUpSpeed.isEnabled = false
        } else {
            val spdCost = engine.getPlayerSpeedCost()
            val canAfford = gold >= spdCost
            binding.btnUpSpeed.text = formatCost("👟 SPD", spdCost, canAfford)
            binding.btnUpSpeed.alpha = if (canAfford) 1.0f else 0.45f
            binding.btnUpSpeed.isEnabled = true
        }

        if (engine.playerHpLevel >= com.example.myapp.game.GameEngine.MAX_PLAYER_HP_LEVEL) {
            binding.btnUpHp.text = "❤️ HP\nMAX"
            binding.btnUpHp.alpha = 0.45f
            binding.btnUpHp.isEnabled = false
        } else {
            val hpCost = engine.getPlayerHpCost()
            val canAfford = gold >= hpCost
            binding.btnUpHp.text = formatCost("❤️ HP", hpCost, canAfford)
            binding.btnUpHp.alpha = if (canAfford) 1.0f else 0.45f
            binding.btnUpHp.isEnabled = true
        }

        if (engine.baseHpLevel >= com.example.myapp.game.GameEngine.MAX_BASE_HP_LEVEL) {
            binding.btnUpBase.text = "🏰 BASE\nMAX"
            binding.btnUpBase.alpha = 0.45f
            binding.btnUpBase.isEnabled = false
        } else {
            val baseCost = engine.getBaseHpCost()
            val canAfford = gold >= baseCost
            binding.btnUpBase.text = formatCost("🏰 BASE", baseCost, canAfford)
            binding.btnUpBase.alpha = if (canAfford) 1.0f else 0.45f
            binding.btnUpBase.isEnabled = true
        }

        val repCost = engine.repairCost
        val canRep = gold >= repCost && engine.baseHp < engine.maxBaseHp
        binding.btnRepair.text = formatCost("🔧 Repair", repCost, canRep)
        binding.btnRepair.alpha = if (canRep) 1.0f else 0.45f

        // Defenses costs with dynamic gold/red color & alpha
        val bCost = engine.blockadeCost
        val canB = gold >= bCost
        binding.btnBlockade.text = formatCost("🪨 Blockade", bCost, canB)
        binding.btnBlockade.alpha = if (canB) 1.0f else 0.45f

        val sCost = TrapType.SPIKE.cost
        val canS = gold >= sCost
        binding.btnTrapSpike.text = formatCost("📌 Spikes", sCost, canS)
        binding.btnTrapSpike.alpha = if (canS) 1.0f else 0.45f

        val tCost = TrapType.TAR.cost
        val canT = gold >= tCost
        binding.btnTrapTar.text = formatCost("🍯 Tar Pit", tCost, canT)
        binding.btnTrapTar.alpha = if (canT) 1.0f else 0.45f

        val mCost = TrapType.MINE.cost
        val canM = gold >= mCost
        binding.btnTrapMine.text = formatCost("💣 Landmine", mCost, canM)
        binding.btnTrapMine.alpha = if (canM) 1.0f else 0.45f

        // Tower buttons with dynamic gold/red color & alpha
        fun updateTowerBtn(btn: android.widget.Button, type: TowerType, label: String) {
            val cost = engine.getTowerCost(type)
            val canAfford = gold >= cost
            btn.text = formatCost(label, cost, canAfford)
            btn.alpha = if (canAfford) 1.0f else 0.45f
        }
        updateTowerBtn(binding.btnTowerArrow,    TowerType.ARROW,    "🏹 Arrow")
        updateTowerBtn(binding.btnTowerMagic,    TowerType.MAGIC,    "🧨 Magic")
        updateTowerBtn(binding.btnTowerCannon,   TowerType.CANNON,   "💣 Cannon")
        updateTowerBtn(binding.btnTowerPoison,   TowerType.POISON,   "☠️ Poison")
        updateTowerBtn(binding.btnTowerTesla,    TowerType.TESLA,    "⚡ Tesla")
        updateTowerBtn(binding.btnTowerIce,      TowerType.ICE,      "❄️ Ice")
        updateTowerBtn(binding.btnTowerFlame,    TowerType.FLAME,    "🔥 Flame")
        updateTowerBtn(binding.btnTowerNecro,    TowerType.NECRO,    "💀 Necro")
        updateTowerBtn(binding.btnTowerBallista, TowerType.BALLISTA, "🎯 Ballista")
        updateTowerBtn(binding.btnTowerVortex,   TowerType.VORTEX,   "🌀 Vortex")
        updateTowerBtn(binding.btnTowerHealer,   TowerType.HEALER,   "💚 Healer")

        // Selected tower inspector update if visible
        val selected = binding.gameView.getSelectedTower()
        if (selected != null) {
            if (selected.level >= com.example.myapp.game.Tower.MAX_TOWER_LEVEL) {
                binding.btnUpTower.text = "▲ Lv.MAX [MAX]"
                binding.btnUpTower.alpha = 0.45f
                binding.btnUpTower.isEnabled = false
            } else {
                val dmgDelta = (selected.type.baseDamage * 0.70f).toInt()
                val upCost = selected.upgradeCost()
                binding.btnUpTower.text = "▲ Lv.${selected.level + 1} [+$dmgDelta DMG] [💰 $upCost]"
                val canAfford = gold >= upCost
                binding.btnUpTower.alpha = if (canAfford) 1.0f else 0.45f
                binding.btnUpTower.isEnabled = true
            }
        }
    }

    private fun showMilestoneBuffDialog() {
        val engine = binding.gameView.getEngine()
        val choices = engine.endlessMilestoneChoices
        if (choices.isEmpty()) return

        val view = layoutInflater.inflate(R.layout.dialog_milestone_buff, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        val titleView = view.findViewById<android.widget.TextView>(R.id.milestone_title)
        val subtitleView = view.findViewById<android.widget.TextView>(R.id.milestone_subtitle)
        titleView.text = S.milestoneTitle(engine.wave)
        subtitleView.text = "The realm acknowledges your valor! Choose one permanent boon:"

        fun setupBuffCard(cardId: Int, emojiId: Int, nameId: Int, descId: Int, btnId: Int, buff: EndlessBuff) {
            val card = view.findViewById<android.view.View>(cardId)
            val emoji = view.findViewById<android.widget.TextView>(emojiId)
            val name = view.findViewById<android.widget.TextView>(nameId)
            val desc = view.findViewById<android.widget.TextView>(descId)
            val btn = view.findViewById<android.widget.Button>(btnId)

            card.visibility = View.VISIBLE
            emoji.text = buff.emoji
            name.text = buff.label
            desc.text = buff.description

            val selectAction = {
                engine.pickEndlessBuff(buff)
                SoundManager.play(SfxType.ACHIEVEMENT)
                Toast.makeText(this, "Acquired: ${buff.emoji} ${buff.label}!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            card.setOnClickListener { selectAction() }
            btn.setOnClickListener { selectAction() }
        }

        if (choices.isNotEmpty()) {
            setupBuffCard(R.id.card_buff_1, R.id.buff_1_emoji, R.id.buff_1_title, R.id.buff_1_desc, R.id.btn_pick_buff_1, choices[0])
        }
        if (choices.size > 1) {
            setupBuffCard(R.id.card_buff_2, R.id.buff_2_emoji, R.id.buff_2_title, R.id.buff_2_desc, R.id.btn_pick_buff_2, choices[1])
        } else {
            view.findViewById<android.view.View>(R.id.card_buff_2).visibility = View.GONE
        }
        if (choices.size > 2) {
            setupBuffCard(R.id.card_buff_3, R.id.buff_3_emoji, R.id.buff_3_title, R.id.buff_3_desc, R.id.btn_pick_buff_3, choices[2])
        } else {
            view.findViewById<android.view.View>(R.id.card_buff_3).visibility = View.GONE
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showMerchantShopDialog() {
        val engine = binding.gameView.getEngine()
        val choices = engine.merchantShopChoices
        if (choices.isEmpty()) return

        val view = layoutInflater.inflate(R.layout.dialog_merchant_shop, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        val goldDisplay = view.findViewById<android.widget.TextView>(R.id.merchant_gold_display)
        goldDisplay.text = "💰 Your Gold: ${engine.gold}g"

        fun setupMerchantCard(
            cardId: Int, tierBadgeId: Int, formulaId: Int,
            emojiId: Int, titleId: Int, descId: Int,
            btnId: Int, card: MerchantCard
        ) {
            val cardView = view.findViewById<android.view.View>(cardId)
            val tierBadge = view.findViewById<android.widget.TextView>(tierBadgeId)
            val formula = view.findViewById<android.widget.TextView>(formulaId)
            val emoji = view.findViewById<android.widget.TextView>(emojiId)
            val title = view.findViewById<android.widget.TextView>(titleId)
            val desc = view.findViewById<android.widget.TextView>(descId)
            val btn = view.findViewById<android.widget.Button>(btnId)

            cardView.visibility = View.VISIBLE
            tierBadge.text = card.tier.displayName
            tierBadge.setTextColor(card.tier.badgeColor)
            formula.text = card.synergyFormula ?: ""
            emoji.text = card.emoji
            title.text = card.title
            desc.text = card.description

            val canAfford = engine.gold >= card.cost
            btn.text = if (card.cost > 0) "PURCHASE • ${card.cost}g" else "SACRIFICE & TAKE"
            btn.isEnabled = canAfford
            btn.alpha = if (canAfford) 1.0f else 0.45f

            val buyAction = {
                if (engine.draftMerchantCard(card)) {
                    SoundManager.play(SfxType.REWARD_CHEST)
                    Toast.makeText(this, "Acquired: ${card.emoji} ${card.title}!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Insufficient Gold!", Toast.LENGTH_SHORT).show()
                }
            }
            btn.setOnClickListener { buyAction() }
            if (canAfford) {
                cardView.setOnClickListener { buyAction() }
            }
        }

        if (choices.isNotEmpty()) {
            setupMerchantCard(R.id.card_merchant_1, R.id.item_tier_badge_1, R.id.item_formula_1, R.id.item_emoji_1, R.id.item_title_1, R.id.item_desc_1, R.id.btn_buy_1, choices[0])
        }
        if (choices.size > 1) {
            setupMerchantCard(R.id.card_merchant_2, R.id.item_tier_badge_2, R.id.item_formula_2, R.id.item_emoji_2, R.id.item_title_2, R.id.item_desc_2, R.id.btn_buy_2, choices[1])
        } else {
            view.findViewById<android.view.View>(R.id.card_merchant_2).visibility = View.GONE
        }
        if (choices.size > 2) {
            setupMerchantCard(R.id.card_merchant_3, R.id.item_tier_badge_3, R.id.item_formula_3, R.id.item_emoji_3, R.id.item_title_3, R.id.item_desc_3, R.id.btn_buy_3, choices[2])
        } else {
            view.findViewById<android.view.View>(R.id.card_merchant_3).visibility = View.GONE
        }

        val btnSkip = view.findViewById<android.widget.Button>(R.id.btn_merchant_skip)
        btnSkip.setOnClickListener {
            engine.skipMerchantShop()
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showBattleResultsDialog(isVictory: Boolean, score: Int, wave: Int) {
        val engine = binding.gameView.getEngine()
        val cl = engine.campaignLevel

        SoundManager.play(if (isVictory) SfxType.VICTORY else SfxType.GAME_OVER)
        binding.gameView.triggerHaptic(if (isVictory) com.example.myapp.game.GameView.HapticType.MEDIUM_PULSE else com.example.myapp.game.GameView.HapticType.HEAVY_RUMBLE)

        val view = layoutInflater.inflate(R.layout.dialog_battle_results, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        val textIcon = view.findViewById<android.widget.TextView>(R.id.result_icon)
        val textTitle = view.findViewById<android.widget.TextView>(R.id.result_title)
        val textSubtitle = view.findViewById<android.widget.TextView>(R.id.result_subtitle)
        val layoutStars = view.findViewById<android.view.View>(R.id.layout_stars)
        val textStars = view.findViewById<android.widget.TextView>(R.id.result_stars)
        val layoutObjectives = view.findViewById<android.view.View>(R.id.layout_objectives)
        val textObj1 = view.findViewById<android.widget.TextView>(R.id.result_obj1)
        val textObj2 = view.findViewById<android.widget.TextView>(R.id.result_obj2)
        val textObj3 = view.findViewById<android.widget.TextView>(R.id.result_obj3)

        val statWaves = view.findViewById<android.widget.TextView>(R.id.stat_waves)
        val statScore = view.findViewById<android.widget.TextView>(R.id.stat_score)
        val statKills = view.findViewById<android.widget.TextView>(R.id.stat_kills)
        val statCombo = view.findViewById<android.widget.TextView>(R.id.stat_combo)
        val statDiamonds = view.findViewById<android.widget.TextView>(R.id.stat_diamonds)
        val statBosses = view.findViewById<android.widget.TextView>(R.id.stat_bosses)
        val rewardBanner = view.findViewById<android.widget.TextView>(R.id.result_reward_banner)

        val btnNextMission = view.findViewById<android.widget.Button>(R.id.btn_next_mission)
        val btnPlayAgain = view.findViewById<android.widget.Button>(R.id.btn_play_again)
        val btnMenu = view.findViewById<android.widget.Button>(R.id.btn_menu)

        val kills = engine.totalKills
        val combo = engine.bestCombo
        val diamonds = engine.diamondsEarnedThisRun
        val bosses = engine.bossesKilledThisRun

        statWaves.text = if (cl != null) "🌊 Wave $wave/${cl.targetWave}" else "🌊 Wave $wave"
        statScore.text = "🎯 ${score} pts"
        statKills.text = "⚔️ $kills Slain"
        statCombo.text = "🔥 ${combo}x Combo"
        statDiamonds.text = "💎 +$diamonds"
        statBosses.text = "👑 $bosses Defeated"

        if (isVictory) {
            textIcon.text = if (engine.isHeroicMode) "💀" else "🏆"
            textTitle.text = if (engine.isHeroicMode) GameStrings.heroicVictoryTitle else if (cl != null) "REALM DEFENDED!" else "VICTORY!"
            textTitle.setTextColor(if (engine.isHeroicMode) Color.parseColor("#FF9800") else resources.getColor(R.color.gold, theme))
            textSubtitle.text = cl?.let { "${it.emoji} ${it.title}${if (engine.isHeroicMode) " [HEROIC]" else ""}" } ?: "Wave $wave Cleared"

            if (cl != null) {
                layoutStars.visibility = View.VISIBLE
                val stars = engine.campaignStars.coerceIn(1, 3)
                textStars.text = "⭐".repeat(stars) + "☆".repeat(3 - stars)
                layoutObjectives.visibility = View.VISIBLE

                val isPl = GameStrings.isPl
                val o1Met = engine.campaignObjective1Met
                val o2Met = engine.campaignObjective2Met
                val o3Met = engine.campaignObjective3Met

                textObj1.text = (if (o1Met) "✓ " else "✗ ") + cl.objective1.description(isPl)
                textObj1.setTextColor(if (o1Met) Color.WHITE else Color.parseColor("#EF5350"))

                textObj2.text = (if (o2Met) "✓ " else "✗ ") + cl.objective2.description(isPl)
                textObj2.setTextColor(if (o2Met) Color.WHITE else Color.parseColor("#EF5350"))

                textObj3.visibility = View.VISIBLE
                textObj3.text = (if (o3Met) "✓ " else "✗ ") + cl.objective3.description(isPl)
                textObj3.setTextColor(if (o3Met) Color.WHITE else Color.parseColor("#EF5350"))
            } else {
                layoutStars.visibility = View.GONE
                layoutObjectives.visibility = View.GONE
            }

            if (diamonds > 0) {
                rewardBanner.visibility = View.VISIBLE
                rewardBanner.text = "✨ +$diamonds Diamonds claimed for your vault!"
            } else {
                rewardBanner.visibility = View.GONE
            }

            val nextLevel = if (cl != null) CampaignData.levels.find { it.id == cl.id + 1 } else null
            if (nextLevel != null) {
                btnNextMission.visibility = View.VISIBLE
                btnNextMission.text = "▶ Next: ${nextLevel.emoji} ${nextLevel.title}"
                btnNextMission.setOnClickListener {
                    SoundManager.play(SfxType.UI_CLICK)
                    dialog.dismiss()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("difficulty", intent.getIntExtra("difficulty", 1))
                        putExtra("campaign_level", nextLevel.id)
                    }
                    startActivity(intent)
                    finish()
                }
            } else {
                btnNextMission.visibility = View.GONE
            }
        } else {
            textIcon.text = "💀"
            textTitle.text = "REALM OVERRUN"
            textTitle.setTextColor(Color.parseColor("#EF5350"))
            textSubtitle.text = "Fallen on Wave $wave · ${cl?.title ?: "Endless Defense"}"
            layoutStars.visibility = View.GONE
            btnNextMission.visibility = View.GONE

            if (cl != null) {
                layoutObjectives.visibility = View.VISIBLE
                val isPl = GameStrings.isPl
                val o1Met = engine.campaignObjective1Met
                val o2Met = engine.campaignObjective2Met
                val o3Met = engine.campaignObjective3Met

                textObj1.text = (if (o1Met) "✓ " else "✗ ") + cl.objective1.description(isPl)
                textObj1.setTextColor(if (o1Met) Color.WHITE else Color.parseColor("#EF5350"))

                textObj2.text = (if (o2Met) "✓ " else "✗ ") + cl.objective2.description(isPl)
                textObj2.setTextColor(if (o2Met) Color.WHITE else Color.parseColor("#EF5350"))

                textObj3.visibility = View.VISIBLE
                textObj3.text = (if (o3Met) "✓ " else "✗ ") + cl.objective3.description(isPl)
                textObj3.setTextColor(if (o3Met) Color.WHITE else Color.parseColor("#EF5350"))
            } else {
                layoutObjectives.visibility = View.GONE
            }

            if (diamonds > 0) {
                rewardBanner.visibility = View.VISIBLE
                rewardBanner.text = "💎 +$diamonds Diamonds salvaged from combat."
            } else {
                rewardBanner.visibility = View.GONE
            }
        }

        btnPlayAgain.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
            synchronized(engine.lock) { engine.restart() }
            binding.gameView.resetGameOverState()
        }

        btnMenu.text = if (cl != null) "📜 Campaign Map" else "🏠 Main Menu"
        btnMenu.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
            finish()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onPause() {
        super.onPause()
        cooldownHandler.removeCallbacks(cooldownRunnable)
        binding.gameView.pause()
        MusicManager.stop()
        // Auto-save if game is still running (not game over)
        val engine = binding.gameView.getEngine()
        if (!engine.gameOver && engine.wave > 0) {
            engine.saveGame()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.gameView.resume()
        cooldownHandler.post(cooldownRunnable)
    }

    @Deprecated("Use OnBackPressedDispatcher")
    override fun onBackPressed() {
        val engine = binding.gameView.getEngine()
        if (engine.gameOver || engine.campaignVictory || engine.wave == 0) {
            super.onBackPressed()
            return
        }
        engine.isPaused = true
        binding.btnPause.text = "\u25B6\uFE0F Play"

        val view = layoutInflater.inflate(R.layout.dialog_confirm_quit, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        val textStats = view.findViewById<android.widget.TextView>(R.id.quit_dialog_stats)
        val btnResume = view.findViewById<android.widget.Button>(R.id.btn_resume_run)
        val btnAbandon = view.findViewById<android.widget.Button>(R.id.btn_confirm_abandon)

        textStats.text = "🌊 Wave ${engine.wave}  ·  🎯 Score ${engine.score}"

        btnResume.setOnClickListener {
            engine.isPaused = false
            binding.btnPause.text = "\u23F8\uFE0F Pause"
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
        }

        btnAbandon.setOnClickListener {
            engine.saveRunHistory("Quit")
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
            finish()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
