package com.example.myapp

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.game.EndlessBuff
import com.example.myapp.game.PowerType
import com.example.myapp.game.TowerType
import com.example.myapp.game.TrapType

internal class MainGameUiController(
    private val activity: AppCompatActivity,
    private val binding: ActivityMainBinding,
) {
    private data class TowerActionSpec(
        val button: Button,
        val type: TowerType,
        val emoji: String,
        val label: String,
    )

    private data class PowerActionSpec(
        val button: Button,
        val type: PowerType,
        val onSuccess: String,
        val onFailure: (Int) -> String,
    )

    private val engine get() = binding.gameView.getEngine()
    private val gameView get() = binding.gameView
    private var sellPendingTime = 0L

    private val cooldownHandler = Handler(Looper.getMainLooper())
    private val cooldownRunnable = object : Runnable {
        override fun run() {
            updatePowerButtons()
            cooldownHandler.postDelayed(this, 500)
        }
    }

    private val towerActions = listOf(
        TowerActionSpec(binding.btnTowerArrow, TowerType.ARROW, "\uD83C\uDFF9", "Arrow"),
        TowerActionSpec(binding.btnTowerMagic, TowerType.MAGIC, "\uD83E\uDDE8", "Magic"),
        TowerActionSpec(binding.btnTowerCannon, TowerType.CANNON, "\uD83D\uDCA3", "Cannon"),
        TowerActionSpec(binding.btnTowerPoison, TowerType.POISON, "\u2620\uFE0F", "Poison"),
        TowerActionSpec(binding.btnTowerTesla, TowerType.TESLA, "\u26A1", "Tesla"),
        TowerActionSpec(binding.btnTowerIce, TowerType.ICE, "\u2744\uFE0F", "Ice"),
        TowerActionSpec(binding.btnTowerFlame, TowerType.FLAME, "\uD83D\uDD25", "Flame"),
        TowerActionSpec(binding.btnTowerNecro, TowerType.NECRO, "\uD83D\uDC80", "Necro"),
        TowerActionSpec(binding.btnTowerBallista, TowerType.BALLISTA, "\uD83C\uDFAF", "Ballista"),
        TowerActionSpec(binding.btnTowerVortex, TowerType.VORTEX, "\uD83C\uDF00", "Vortex"),
        TowerActionSpec(binding.btnTowerHealer, TowerType.HEALER, "\uD83D\uDC9A", "Healer"),
    )

    private val powerActions = listOf(
        PowerActionSpec(
            binding.btnPowerFireball,
            PowerType.FIREBALL,
            GameStrings.fireballUsed,
            { GameStrings.fireballNeedGold(it) },
        ),
        PowerActionSpec(
            binding.btnPowerFreeze,
            PowerType.FREEZE,
            GameStrings.freezeUsed,
            { GameStrings.freezeNeedGold(it) },
        ),
        PowerActionSpec(
            binding.btnPowerLightning,
            PowerType.LIGHTNING,
            GameStrings.lightningUsed,
            { GameStrings.lightningNeedGold(it) },
        ),
    )

    fun bind(onExitToMenu: () -> Unit) {
        bindTutorial()
        bindHudCallbacks(onExitToMenu)
        bindTowerButtons()
        bindPowerButtons()
        bindDashButton()
        bindBaseActions()
        bindTrapButtons()
        bindUpgradeButtons()
        bindSelectionActions()
        bindRunControls()
        refreshUi()
    }

    fun refreshUi() {
        updateUpgradeCosts()
        updatePowerButtons()
        updateSpeedButtons()
        updatePauseButton()
        updateAutoWaveButton()
    }

    fun onResume() {
        gameView.resume()
        cooldownHandler.post(cooldownRunnable)
        MusicManager.start()
    }

    fun onPause() {
        cooldownHandler.removeCallbacks(cooldownRunnable)
        gameView.pause()
        MusicManager.stop()

        val currentEngine = engine
        if (!currentEngine.gameOver && currentEngine.wave > 0) {
            currentEngine.saveGame()
        }
    }

    fun handleBackPressed(): Boolean {
        val currentEngine = engine
        if (currentEngine.gameOver || currentEngine.campaignVictory || currentEngine.wave == 0) {
            return false
        }

        currentEngine.isPaused = true
        updatePauseButton()
        showQuitDialog()
        return true
    }

    private fun bindTutorial() {
        binding.btnTutorial.setOnClickListener {
            TutorialDialog.show(activity)
        }
    }

    private fun bindHudCallbacks(onExitToMenu: () -> Unit) {
        gameView.onCampaignVictory = onExitToMenu
        gameView.onGoldChanged = { gold ->
            binding.textGold.text = GameStrings.goldHud(gold)
            updateUpgradeCosts()
        }
        gameView.onWaveChanged = { wave ->
            binding.textWave.text = GameStrings.waveHud(wave)
        }
        gameView.onDiamondsChanged = { diamonds ->
            binding.textKills.text = GameStrings.diamondsHud(diamonds)
        }
        gameView.onGameOver = { score, wave ->
            binding.textGold.text = GameStrings.gameOverHud(score, wave)
            binding.textWave.text = GameStrings.gameOverWaveHud(score, wave)
            showGameOverDialog(score, wave)
        }
        gameView.onMilestoneBuff = {
            activity.runOnUiThread { showMilestoneBuffDialog() }
        }
        gameView.onTowerSelected = { tower ->
            resetSellConfirmation()
            binding.btnTarget.text = "\uD83C\uDFAF ${tower?.targetingMode?.label ?: "Close"}"
            binding.btnAbility.text = if (tower != null) {
                "\u2728 ${tower.type.abilityName}"
            } else {
                "\u2728 Ability"
            }
        }
    }

    private fun bindTowerButtons() {
        towerActions.forEach { spec ->
            spec.button.setOnClickListener {
                val cost = engine.getTowerCost(spec.type)
                if (engine.gold >= cost) {
                    gameView.placementMode = spec.type
                    gameView.trapPlacementMode = null
                    gameView.blockadePlacementMode = false
                    toast(GameStrings.towerPlacementToast(spec.emoji, spec.label, cost))
                } else {
                    toast(GameStrings.notEnoughGold)
                }
            }
        }
    }

    private fun bindPowerButtons() {
        powerActions.forEach { spec ->
            spec.button.setOnClickListener {
                val cooldown = engine.getPowerCooldown(spec.type)
                if (cooldown > 0) {
                    toast(GameStrings.cooldownFmt(cooldown.toInt()))
                    return@setOnClickListener
                }

                if (engine.usePower(spec.type)) {
                    toast(spec.onSuccess)
                } else {
                    toast(spec.onFailure(spec.type.cost))
                }
            }
        }
    }

    private fun bindDashButton() {
        binding.btnDash.setOnClickListener {
            if (engine.dashCooldown > 0) {
                toast(GameStrings.dashCooldownFmt(engine.dashCooldown.toInt()))
                return@setOnClickListener
            }

            if (engine.playerDash(engine.player.targetX, engine.player.targetY)) {
                toast(GameStrings.dashUsed)
            } else {
                toast(GameStrings.cantDash)
            }
        }
    }

    private fun bindBaseActions() {
        binding.btnRepair.setOnClickListener {
            when {
                engine.repairBase() -> toast(GameStrings.baseRepaired)
                engine.baseHp >= engine.maxBaseHp -> toast(GameStrings.baseFullHp)
                else -> toast(GameStrings.notEnoughGold)
            }
        }

        binding.btnBlockade.setOnClickListener {
            val cost = engine.blockadeCost
            if (engine.gold >= cost) {
                gameView.blockadePlacementMode = true
                gameView.placementMode = null
                gameView.trapPlacementMode = null
                toast(GameStrings.blockadePlacement(cost))
            } else {
                toast(GameStrings.notEnoughGold)
            }
        }
    }

    private fun bindTrapButtons() {
        binding.btnTrapSpike.setOnClickListener { armTrapPlacement(TrapType.SPIKE, GameStrings.placeTrapSpike(TrapType.SPIKE.cost)) }
        binding.btnTrapTar.setOnClickListener { armTrapPlacement(TrapType.TAR, GameStrings.placeTrapTar(TrapType.TAR.cost)) }
        binding.btnTrapMine.setOnClickListener { armTrapPlacement(TrapType.MINE, GameStrings.placeTrapMine(TrapType.MINE.cost)) }
    }

    private fun bindUpgradeButtons() {
        binding.btnUpDamage.setOnClickListener {
            val cost = engine.playerDamageLevel * 25
            if (engine.upgradePlayerDamage()) {
                toast(GameStrings.attackUp(cost))
                updateUpgradeCosts()
            } else {
                toast(GameStrings.needGold(cost))
            }
        }
        binding.btnUpSpeed.setOnClickListener {
            val cost = engine.playerSpeedLevel * 20
            if (engine.upgradePlayerSpeed()) {
                toast(GameStrings.speedUp(cost))
                updateUpgradeCosts()
            } else {
                toast(GameStrings.needGold(cost))
            }
        }
        binding.btnUpHp.setOnClickListener {
            val cost = engine.playerHpLevel * 30
            if (engine.upgradePlayerHp()) {
                toast(GameStrings.hpUp(cost))
                updateUpgradeCosts()
            } else {
                toast(GameStrings.needGold(cost))
            }
        }
        binding.btnUpBase.setOnClickListener {
            val cost = engine.baseHpLevel * 40
            if (engine.upgradeBaseHp()) {
                toast(GameStrings.baseUp(cost))
                updateUpgradeCosts()
            } else {
                toast(GameStrings.needGold(cost))
            }
        }
    }

    private fun bindSelectionActions() {
        binding.btnUpTower.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected == null) {
                toast(GameStrings.tapTowerFirst)
                return@setOnClickListener
            }

            val cost = selected.upgradeCost()
            if (engine.upgradeTower(selected)) {
                toast(GameStrings.towerUpgraded(selected.level, cost))
            } else {
                toast(GameStrings.needGold(cost))
            }
        }

        binding.btnTarget.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected == null) {
                toast(GameStrings.tapTowerFirst)
                return@setOnClickListener
            }

            selected.targetingMode = selected.targetingMode.next()
            binding.btnTarget.text = "\uD83C\uDFAF ${selected.targetingMode.label}"
            toast(GameStrings.targetMode(selected.targetingMode.label))
        }

        binding.btnSell.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected == null) {
                toast(GameStrings.tapTowerFirst)
                return@setOnClickListener
            }

            val value = (selected.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
            val now = System.currentTimeMillis()
            if (now - sellPendingTime < 2_000L) {
                engine.sellTower(selected)
                gameView.clearSelectedTower()
                toast(GameStrings.soldTower(value))
                resetSellConfirmation()
                return@setOnClickListener
            }

            sellPendingTime = now
            binding.btnSell.text = "+${value}g - tap again!"
            binding.btnSell.backgroundTintList = ColorStateList.valueOf(0xFFE65100.toInt())
            Handler(Looper.getMainLooper()).postDelayed({
                if (System.currentTimeMillis() - sellPendingTime >= 2_000L) {
                    resetSellConfirmation()
                }
            }, 2_100L)
        }

        binding.btnAbility.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected == null) {
                toast(GameStrings.tapTowerFirst)
                return@setOnClickListener
            }

            if (engine.activateTowerAbility(selected)) {
                toast(GameStrings.abilityActivated(selected.type.abilityName))
            } else {
                toast(GameStrings.cooldownFmt(selected.abilityTimer.toInt()))
            }
        }
    }

    private fun bindRunControls() {
        binding.btnSpeed.setOnClickListener {
            engine.gameSpeed = if (engine.gameSpeed == 2) 1 else 2
            updateSpeedButtons()
            toast(GameStrings.speedToast(engine.gameSpeed))
        }

        binding.btnSpeed3.setOnClickListener {
            engine.gameSpeed = if (engine.gameSpeed == 3) 1 else 3
            updateSpeedButtons()
            toast(GameStrings.speedToast(engine.gameSpeed))
        }

        binding.btnPause.setOnClickListener {
            engine.isPaused = !engine.isPaused
            updatePauseButton()
        }

        binding.btnAutoWave.setOnClickListener {
            engine.autoWave = !engine.autoWave
            updateAutoWaveButton()
            toast(if (engine.autoWave) "Auto-Wave ON" else "Auto-Wave OFF")
        }
    }

    private fun armTrapPlacement(type: TrapType, message: String) {
        if (engine.gold >= type.cost) {
            gameView.trapPlacementMode = type
            gameView.placementMode = null
            gameView.blockadePlacementMode = false
            toast(message)
        } else {
            toast(GameStrings.notEnoughGold)
        }
    }

    private fun updateUpgradeCosts() {
        val currentEngine = engine
        binding.btnUpDamage.text = "\u2694\uFE0F ATK\n${currentEngine.playerDamageLevel * 25}g"
        binding.btnUpSpeed.text = "\uD83D\uDC5F SPD\n${currentEngine.playerSpeedLevel * 20}g"
        binding.btnUpHp.text = "\u2764\uFE0F HP\n${currentEngine.playerHpLevel * 30}g"
        binding.btnUpBase.text = "\uD83C\uDFF0 BASE\n${currentEngine.baseHpLevel * 40}g"
        binding.btnRepair.text = "\uD83D\uDD27 Repair\n${currentEngine.repairCost}g"
    }

    private fun updatePowerButtons() {
        val currentEngine = runCatching { engine }.getOrNull() ?: return
        applyPowerButton(binding.btnPowerFireball, PowerType.FIREBALL, "\uD83D\uDD25", 0xFFFF7043.toInt(), currentEngine)
        applyPowerButton(binding.btnPowerFreeze, PowerType.FREEZE, "\u2744\uFE0F", 0xFF42A5F5.toInt(), currentEngine)
        applyPowerButton(binding.btnPowerLightning, PowerType.LIGHTNING, "\u26A1", 0xFFFFD700.toInt(), currentEngine)

        val dashCooldown = currentEngine.dashCooldown
        if (dashCooldown > 0f) {
            binding.btnDash.text = "${dashCooldown.toInt() + 1}"
            binding.btnDash.alpha = 0.45f
        } else {
            binding.btnDash.text = "\uD83D\uDCA8"
            binding.btnDash.alpha = 1f
        }
    }

    private fun applyPowerButton(
        button: Button,
        type: PowerType,
        emoji: String,
        tint: Int,
        currentEngine: com.example.myapp.game.GameEngine,
    ) {
        val cooldown = currentEngine.getPowerCooldown(type)
        if (cooldown > 0f) {
            button.text = "${cooldown.toInt() + 1}"
            button.alpha = 0.45f
        } else {
            button.text = emoji
            button.alpha = 1f
        }
        button.backgroundTintList = ColorStateList.valueOf(tint)
    }

    private fun updateSpeedButtons() {
        when (engine.gameSpeed) {
            2 -> {
                binding.btnSpeed.alpha = 1f
                binding.btnSpeed3.alpha = 0.5f
            }
            3 -> {
                binding.btnSpeed.alpha = 0.5f
                binding.btnSpeed3.alpha = 1f
            }
            else -> {
                binding.btnSpeed.alpha = 0.5f
                binding.btnSpeed3.alpha = 0.5f
            }
        }
    }

    private fun updatePauseButton() {
        binding.btnPause.text = if (engine.isPaused) "\u25B6\uFE0F Play" else "\u23F8\uFE0F Pause"
        binding.btnPause.backgroundTintList = ColorStateList.valueOf(
            if (engine.isPaused) 0xFF4CAF50.toInt() else 0xFFD32F2F.toInt(),
        )
    }

    private fun updateAutoWaveButton() {
        binding.btnAutoWave.backgroundTintList = ColorStateList.valueOf(
            if (engine.autoWave) 0xFF4CAF50.toInt() else 0xFF455A64.toInt(),
        )
    }

    private fun showGameOverDialog(score: Int, wave: Int) {
        val dialog = AlertDialog.Builder(
            activity,
            com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert,
        )
            .setTitle("Game Over")
            .setMessage("Wave $wave  |  Score $score")
            .setPositiveButton("\u25B6 Play Again") { _, _ ->
                synchronized(engine.lock) { engine.restart() }
                gameView.resetGameOverState()
                refreshUi()
            }
            .setNegativeButton("\u2302 Menu") { _, _ ->
                activity.finish()
            }
            .setCancelable(true)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFF4CAF50.toInt())
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF9E9E9E.toInt())
    }

    private fun showMilestoneBuffDialog() {
        val choices: List<EndlessBuff> = engine.endlessMilestoneChoices
        if (choices.isEmpty()) {
            return
        }

        val items = choices.map { "${it.emoji} ${it.label}\n${it.description}" }.toTypedArray()
        AlertDialog.Builder(
            activity,
            com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert,
        )
            .setTitle(GameStrings.milestoneTitle(engine.wave))
            .setItems(items) { _, which ->
                engine.pickEndlessBuff(choices[which])
                SoundManager.play(com.example.myapp.game.SfxType.UI_CLICK)
            }
            .setCancelable(false)
            .show()
    }

    private fun showQuitDialog() {
        val dialog = AlertDialog.Builder(
            activity,
            com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert,
        )
            .setTitle("Quit run?")
            .setMessage("Your progress will be saved to Run History as a quit.")
            .setPositiveButton("Quit") { _, _ ->
                engine.saveRunHistory("Quit")
                activity.finish()
            }
            .setNegativeButton("Resume") { _, _ ->
                engine.isPaused = false
                updatePauseButton()
            }
            .setCancelable(false)
            .create()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFFEF5350.toInt())
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF4CAF50.toInt())
    }

    private fun resetSellConfirmation() {
        sellPendingTime = 0L
        binding.btnSell.text = "\uD83D\uDCB8 Sell"
        binding.btnSell.backgroundTintList = ColorStateList.valueOf(0xFFEF5350.toInt())
    }

    private fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}
