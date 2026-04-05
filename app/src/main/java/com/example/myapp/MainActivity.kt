package com.example.myapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.game.CampaignData
import com.example.myapp.game.EndlessBuff
import com.example.myapp.game.PowerType
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
            cooldownHandler.postDelayed(this, 500)
        }
    }

    private fun updatePowerButtons() {
        val engine = runCatching { binding.gameView.getEngine() }.getOrNull() ?: return
        fun applyBtn(btn: android.widget.Button, type: com.example.myapp.game.PowerType, emoji: String, tint: Int) {
            val cd = engine.getPowerCooldown(type)
            if (cd > 0f) {
                btn.text = "${cd.toInt() + 1}"
                btn.alpha = 0.45f
            } else {
                btn.text = emoji
                btn.alpha = 1f
            }
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(tint)
        }
        applyBtn(binding.btnPowerFireball,  com.example.myapp.game.PowerType.FIREBALL,  "\uD83D\uDD25",   0xFFFF7043.toInt())
        applyBtn(binding.btnPowerFreeze,    com.example.myapp.game.PowerType.FREEZE,    "\u2744\uFE0F", 0xFF42A5F5.toInt())
        applyBtn(binding.btnPowerLightning, com.example.myapp.game.PowerType.LIGHTNING, "\u26A1",         0xFFFFD700.toInt())
        val dashCd = engine.dashCooldown
        if (dashCd > 0f) {
            binding.btnDash.text = "${dashCd.toInt() + 1}"
            binding.btnDash.alpha = 0.45f
        } else {
            binding.btnDash.text = "\uD83D\uDCA8"
            binding.btnDash.alpha = 1f
        }
    }

    override fun onStart() {
        super.onStart()
        findViewById<View>(R.id.btn_tutorial)?.setOnClickListener {
            TutorialDialog.show(this)
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

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
            engine.applyCampaign(campaignLevel)

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

        // Campaign victory — return to level select
        gameView.onCampaignVictory = {
            finish()
        }

        // HUD updates from game thread
        gameView.onGoldChanged = { gold ->
            binding.textGold.text = S.goldHud(gold)
            updateUpgradeCosts()
        }
        gameView.onWaveChanged = { wave ->
            binding.textWave.text = S.waveHud(wave)
        }
        gameView.onDiamondsChanged = { diamonds ->
            binding.textKills.text = S.diamondsHud(diamonds)
        }
        gameView.onGameOver = { score, wave ->
            binding.textGold.text = S.gameOverHud(score, wave)
            binding.textWave.text = S.gameOverWaveHud(score, wave)
            val goDialog = AlertDialog.Builder(this@MainActivity, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setTitle("Game Over")
                .setMessage("Wave $wave  ·  Score $score")
                .setPositiveButton("▶ Play Again") { _, _ ->
                    synchronized(engine.lock) { engine.restart() }
                    gameView.resetGameOverState()
                }
                .setNegativeButton("🏠 Menu") { _, _ ->
                    finish()
                }
                .setCancelable(true)
                .create()
            goDialog.show()
            goDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            goDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFF4CAF50.toInt())
            goDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF9E9E9E.toInt())
        }

        // Tower placement buttons
        binding.btnTowerArrow.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.ARROW)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.ARROW
                Toast.makeText(this, S.towerPlacementToast("🏹", "Arrow", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerMagic.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.MAGIC)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.MAGIC
                Toast.makeText(this, S.towerPlacementToast("🧨", "Magic", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerCannon.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.CANNON)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.CANNON
                Toast.makeText(this, S.towerPlacementToast("💣", "Cannon", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerPoison.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.POISON)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.POISON
                Toast.makeText(this, S.towerPlacementToast("☠️", "Poison", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerTesla.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.TESLA)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.TESLA
                Toast.makeText(this, S.towerPlacementToast("⚡", "Tesla", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerIce.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.ICE)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.ICE
                Toast.makeText(this, S.towerPlacementToast("❄️", "Ice", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerFlame.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.FLAME)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.FLAME
                Toast.makeText(this, S.towerPlacementToast("🔥", "Flame", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerNecro.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.NECRO)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.NECRO
                Toast.makeText(this, S.towerPlacementToast("💀", "Necro", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerBallista.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.BALLISTA)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.BALLISTA
                Toast.makeText(this, S.towerPlacementToast("🎯", "Ballista", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerVortex.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.VORTEX)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.VORTEX
                Toast.makeText(this, S.towerPlacementToast("🌀", "Vortex", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerHealer.setOnClickListener {
            val cost = engine.getTowerCost(TowerType.HEALER)
            if (engine.gold >= cost) {
                gameView.placementMode = TowerType.HEALER
                Toast.makeText(this, S.towerPlacementToast("💚", "Healer", cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }

        // Power buttons
        binding.btnPowerFireball.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.FIREBALL)
            if (cd > 0) { Toast.makeText(this, S.cooldownFmt(cd.toInt()), Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FIREBALL)) {
                Toast.makeText(this, S.fireballUsed, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.fireballNeedGold(PowerType.FIREBALL.cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerFreeze.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.FREEZE)
            if (cd > 0) { Toast.makeText(this, S.cooldownFmt(cd.toInt()), Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FREEZE)) {
                Toast.makeText(this, S.freezeUsed, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.freezeNeedGold(PowerType.FREEZE.cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerLightning.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.LIGHTNING)
            if (cd > 0) { Toast.makeText(this, S.cooldownFmt(cd.toInt()), Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.LIGHTNING)) {
                Toast.makeText(this, S.lightningUsed, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.lightningNeedGold(PowerType.LIGHTNING.cost), Toast.LENGTH_SHORT).show()
        }

        // Dash button
        binding.btnDash.setOnClickListener {
            if (engine.dashCooldown > 0) {
                Toast.makeText(this, S.dashCooldownFmt(engine.dashCooldown.toInt()), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (engine.playerDash(engine.player.targetX, engine.player.targetY)) {
                Toast.makeText(this, S.dashUsed, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, S.cantDash, Toast.LENGTH_SHORT).show()
            }
        }

        // Repair base
        binding.btnRepair.setOnClickListener {
            if (engine.repairBase()) {
                Toast.makeText(this, S.baseRepaired, Toast.LENGTH_SHORT).show()
            } else if (engine.baseHp >= engine.maxBaseHp) {
                Toast.makeText(this, S.baseFullHp, Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }

        // Blockade placement
        binding.btnBlockade.setOnClickListener {
            val cost = engine.blockadeCost
            if (engine.gold >= cost) {
                gameView.blockadePlacementMode = true
                gameView.placementMode = null
                gameView.trapPlacementMode = null
                Toast.makeText(this, S.blockadePlacement(cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }

        // Trap placement buttons
        binding.btnTrapSpike.setOnClickListener {
            val cost = TrapType.SPIKE.cost
            if (engine.gold >= cost) {
                gameView.trapPlacementMode = TrapType.SPIKE
                gameView.placementMode = null
                gameView.blockadePlacementMode = false
                Toast.makeText(this, S.placeTrapSpike(cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTrapTar.setOnClickListener {
            val cost = TrapType.TAR.cost
            if (engine.gold >= cost) {
                gameView.trapPlacementMode = TrapType.TAR
                gameView.placementMode = null
                gameView.blockadePlacementMode = false
                Toast.makeText(this, S.placeTrapTar(cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }
        binding.btnTrapMine.setOnClickListener {
            val cost = TrapType.MINE.cost
            if (engine.gold >= cost) {
                gameView.trapPlacementMode = TrapType.MINE
                gameView.placementMode = null
                gameView.blockadePlacementMode = false
                Toast.makeText(this, S.placeTrapMine(cost), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.notEnoughGold, Toast.LENGTH_SHORT).show()
        }

        // Endless milestone buff callback
        gameView.onMilestoneBuff = {
            runOnUiThread { showMilestoneBuffDialog() }
        }

        // Upgrade buttons
        binding.btnUpDamage.setOnClickListener {
            val cost = engine.playerDamageLevel * 25
            if (engine.upgradePlayerDamage()) {
                Toast.makeText(this, S.attackUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnUpSpeed.setOnClickListener {
            val cost = engine.playerSpeedLevel * 20
            if (engine.upgradePlayerSpeed()) {
                Toast.makeText(this, S.speedUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnUpHp.setOnClickListener {
            val cost = engine.playerHpLevel * 30
            if (engine.upgradePlayerHp()) {
                Toast.makeText(this, S.hpUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnUpBase.setOnClickListener {
            val cost = engine.baseHpLevel * 40
            if (engine.upgradeBaseHp()) {
                Toast.makeText(this, S.baseUp(cost), Toast.LENGTH_SHORT).show()
                updateUpgradeCosts()
            } else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
        }
        binding.btnUpTower.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                if (selected.isMaxLevel()) {
                    Toast.makeText(this, S.towerMaxLevel, Toast.LENGTH_SHORT).show()
                } else {
                    val cost = selected.upgradeCost()
                    if (engine.upgradeTower(selected)) Toast.makeText(this, S.towerUpgraded(selected.level, cost), Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this, S.needGold(cost), Toast.LENGTH_SHORT).show()
                }
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }
        binding.btnTarget.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                selected.targetingMode = selected.targetingMode.next()
                binding.btnTarget.text = "\uD83C\uDFAF ${selected.targetingMode.label}"
                Toast.makeText(this, S.targetMode(selected.targetingMode.label), Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }

        gameView.onTowerSelected = { tower ->
            binding.btnTarget.text = "\uD83C\uDFAF ${tower?.targetingMode?.label ?: "Close"}"
            // Update ability button with tower's ability name
            if (tower != null) {
                binding.btnAbility.text = "\u2728 ${tower.type.abilityName}"
                binding.btnUpTower.text = if (tower.isMaxLevel()) "\u2B06\uFE0F MAX" else "\u2B06\uFE0F ${tower.upgradeCost()}g"
            } else {
                binding.btnAbility.text = "\u2728 Ability"
                binding.btnUpTower.text = "\u2B06\uFE0F Up"
            }
        }

        // Sell tower
        binding.btnSell.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                val value = (selected.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
                val now = System.currentTimeMillis()
                if (now - sellPendingTime < 2000L) {
                    engine.sellTower(selected)
                    gameView.clearSelectedTower()
                    Toast.makeText(this, S.soldTower(value), Toast.LENGTH_SHORT).show()
                    sellPendingTime = 0L
                    binding.btnSell.text = "\uD83D\uDCB8 Sell"
                    binding.btnSell.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFEF5350.toInt())
                } else {
                    sellPendingTime = now
                    binding.btnSell.text = "+${value}g — tap again!"
                    binding.btnSell.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE65100.toInt())
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (System.currentTimeMillis() - sellPendingTime >= 2000L) {
                            binding.btnSell.text = "\uD83D\uDCB8 Sell"
                            binding.btnSell.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFEF5350.toInt())
                        }
                    }, 2100)
                }
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }

        // Tower ability
        binding.btnAbility.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                if (engine.activateTowerAbility(selected)) {
                    Toast.makeText(this, S.abilityActivated(selected.type.abilityName), Toast.LENGTH_SHORT).show()
                } else {
                    val remaining = selected.abilityTimer.toInt()
                    Toast.makeText(this, S.cooldownFmt(remaining), Toast.LENGTH_SHORT).show()
                }
            } else Toast.makeText(this, S.tapTowerFirst, Toast.LENGTH_SHORT).show()
        }

        // Speed 2x toggle
        binding.btnSpeed.setOnClickListener {
            engine.gameSpeed = if (engine.gameSpeed == 2) 1 else 2
            updateSpeedButtons()
            Toast.makeText(this, S.speedToast(engine.gameSpeed), Toast.LENGTH_SHORT).show()
        }

        // Speed 3x toggle
        binding.btnSpeed3.setOnClickListener {
            engine.gameSpeed = if (engine.gameSpeed == 3) 1 else 3
            updateSpeedButtons()
            Toast.makeText(this, S.speedToast(engine.gameSpeed), Toast.LENGTH_SHORT).show()
        }

        // Pause
        binding.btnPause.setOnClickListener {
            engine.isPaused = !engine.isPaused
            binding.btnPause.text = if (engine.isPaused) "\u25B6\uFE0F Play" else "\u23F8\uFE0F Pause"
            binding.btnPause.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (engine.isPaused) 0xFF4CAF50.toInt() else 0xFFD32F2F.toInt()
            )
        }

        // Auto-wave toggle
        binding.btnAutoWave.setOnClickListener {
            engine.autoWave = !engine.autoWave
            binding.btnAutoWave.backgroundTintList = android.content.res.ColorStateList.valueOf(
                if (engine.autoWave) 0xFF4CAF50.toInt() else 0xFF455A64.toInt()
            )
            Toast.makeText(this, if (engine.autoWave) "Auto-Wave ON" else "Auto-Wave OFF", Toast.LENGTH_SHORT).show()
        }

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

        // Continue saved game if requested
        val isContinue = intent.getBooleanExtra("continue_game", false)
        if (isContinue) {
            engine.loadGame()
        }

        updateUpgradeCosts()
    }

    private fun updateUpgradeCosts() {
        val engine = binding.gameView.getEngine()
        binding.btnUpDamage.text = "\u2694\uFE0F ATK\n${engine.playerDamageLevel * 25}g"
        binding.btnUpSpeed.text = "\uD83D\uDC5F SPD\n${engine.playerSpeedLevel * 20}g"
        binding.btnUpHp.text = "\u2764\uFE0F HP\n${engine.playerHpLevel * 30}g"
        binding.btnUpBase.text = "\uD83C\uDFF0 BASE\n${engine.baseHpLevel * 40}g"
        binding.btnRepair.text = "\uD83D\uDD27 Repair\n${engine.repairCost}g"
    }

    private fun updateSpeedButtons() {
        val engine = binding.gameView.getEngine()
        val s = engine.gameSpeed
        binding.btnSpeed.alpha = if (s == 2) 1f else 0.5f
        binding.btnSpeed3.alpha = if (s == 3) 1f else 0.5f
    }

    private fun showMilestoneBuffDialog() {
        val engine = binding.gameView.getEngine()
        val choices = engine.endlessMilestoneChoices
        if (choices.isEmpty()) return
        val items = choices.map { "${it.emoji} ${it.label}\n${it.description}" }.toTypedArray()
        AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
            .setTitle(S.milestoneTitle(engine.wave))
            .setItems(items) { _, which ->
                engine.pickEndlessBuff(choices[which])
                SoundManager.play(com.example.myapp.game.SfxType.UI_CLICK)
            }
            .setCancelable(false)
            .show()
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
        MusicManager.start()
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
        val quitDialog = AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
            .setTitle("Quit run?")
            .setMessage("Your progress will be saved to Run History as a quit.")
            .setPositiveButton("Quit") { _, _ ->
                engine.saveRunHistory("Quit")
                finish()
            }
            .setNegativeButton("Resume") { _, _ ->
                engine.isPaused = false
                binding.btnPause.text = "\u23F8\uFE0F Pause"
            }
            .setCancelable(false)
            .create()
        quitDialog.show()
        quitDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        quitDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFFEF5350.toInt())
        quitDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF4CAF50.toInt())
    }
}
