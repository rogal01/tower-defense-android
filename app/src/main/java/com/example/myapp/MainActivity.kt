package com.example.myapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.game.CampaignData
import com.example.myapp.game.PowerType
import com.example.myapp.game.TowerType

class MainActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        SoundManager.init(this)

        val gameView = binding.gameView
        val engine = gameView.getEngine()

        val difficulty = intent.getIntExtra("difficulty", 1)
        engine.applyDifficulty(difficulty)

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
                Toast.makeText(this, "\uD83D\uDCA1 ${campaignLevel.hint}", Toast.LENGTH_LONG).show()
            }
        }

        // Campaign victory — return to level select
        gameView.onCampaignVictory = {
            finish()
        }

        // HUD updates from game thread
        gameView.onGoldChanged = { gold ->
            binding.textGold.text = "💰 $gold"
        }
        gameView.onWaveChanged = { wave ->
            binding.textWave.text = "⚔️ Wave $wave"
        }
        gameView.onDiamondsChanged = { diamonds ->
            binding.textKills.text = "💎 $diamonds"
        }
        gameView.onGameOver = { score, wave ->
            binding.textGold.text = "💰 GAME OVER"
            binding.textWave.text = "⚔️ Wave $wave | Score $score"
        }

        // Tower placement buttons
        binding.btnTowerArrow.setOnClickListener {
            if (engine.gold >= TowerType.ARROW.baseCost) {
                gameView.placementMode = TowerType.ARROW
                Toast.makeText(this, "Tap to place 🏹 Arrow (${TowerType.ARROW.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerMagic.setOnClickListener {
            if (engine.gold >= TowerType.MAGIC.baseCost) {
                gameView.placementMode = TowerType.MAGIC
                Toast.makeText(this, "Tap to place 🧨 Magic (${TowerType.MAGIC.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerCannon.setOnClickListener {
            if (engine.gold >= TowerType.CANNON.baseCost) {
                gameView.placementMode = TowerType.CANNON
                Toast.makeText(this, "Tap to place 💣 Cannon (${TowerType.CANNON.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerPoison.setOnClickListener {
            if (engine.gold >= TowerType.POISON.baseCost) {
                gameView.placementMode = TowerType.POISON
                Toast.makeText(this, "Tap to place ☠️ Poison (${TowerType.POISON.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerTesla.setOnClickListener {
            if (engine.gold >= TowerType.TESLA.baseCost) {
                gameView.placementMode = TowerType.TESLA
                Toast.makeText(this, "Tap to place ⚡ Tesla (${TowerType.TESLA.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerIce.setOnClickListener {
            if (engine.gold >= TowerType.ICE.baseCost) {
                gameView.placementMode = TowerType.ICE
                Toast.makeText(this, "Tap to place ❄️ Ice (${TowerType.ICE.baseCost}g) — Slows enemies!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }

        // Power buttons
        binding.btnPowerFireball.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.FIREBALL)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FIREBALL)) {
                Toast.makeText(this, "🔥 Fireball! AoE damage!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need ${PowerType.FIREBALL.cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerFreeze.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.FREEZE)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FREEZE)) {
                Toast.makeText(this, "❄️ Freeze! Enemies slowed!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need ${PowerType.FREEZE.cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerHeal.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.HEAL)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.HEAL)) {
                Toast.makeText(this, "💚 Heal! Base +50 HP!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need ${PowerType.HEAL.cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerLightning.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.LIGHTNING)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.LIGHTNING)) {
                Toast.makeText(this, "⚡ Lightning! Chain damage!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need ${PowerType.LIGHTNING.cost}g!", Toast.LENGTH_SHORT).show()
        }

        // Dash button
        binding.btnDash.setOnClickListener {
            if (engine.dashCooldown > 0) {
                Toast.makeText(this, "Dash cooldown: ${engine.dashCooldown.toInt()}s", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (engine.playerDash(engine.player.targetX, engine.player.targetY)) {
                Toast.makeText(this, "\uD83D\uDCA8 Dash! AoE damage!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Can't dash right now!", Toast.LENGTH_SHORT).show()
            }
        }

        // Repair base
        binding.btnRepair.setOnClickListener {
            if (engine.repairBase()) {
                Toast.makeText(this, "🔧 Base repaired!", Toast.LENGTH_SHORT).show()
            } else if (engine.baseHp >= engine.maxBaseHp) {
                Toast.makeText(this, "Base is full HP!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }

        // Upgrade buttons
        binding.btnUpDamage.setOnClickListener {
            val cost = engine.playerDamageLevel * 25
            if (engine.upgradePlayerDamage()) Toast.makeText(this, "⚔️ Attack up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpSpeed.setOnClickListener {
            val cost = engine.playerSpeedLevel * 20
            if (engine.upgradePlayerSpeed()) Toast.makeText(this, "👟 Speed up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpHp.setOnClickListener {
            val cost = engine.playerHpLevel * 30
            if (engine.upgradePlayerHp()) Toast.makeText(this, "❤️ HP up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpBase.setOnClickListener {
            val cost = engine.baseHpLevel * 40
            if (engine.upgradeBaseHp()) Toast.makeText(this, "🏰 Base up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpTower.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                val cost = selected.upgradeCost()
                if (engine.upgradeTower(selected)) Toast.makeText(this, "⬆️ Tower Lv${selected.level}! (${cost}g)", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Tap a tower first!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTarget.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                selected.targetingMode = selected.targetingMode.next()
                binding.btnTarget.text = "\uD83C\uDFAF ${selected.targetingMode.label}"
                Toast.makeText(this, "Target: ${selected.targetingMode.label}", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Tap a tower first!", Toast.LENGTH_SHORT).show()
        }

        gameView.onTowerSelected = { tower ->
            binding.btnTarget.text = "\uD83C\uDFAF ${tower?.targetingMode?.label ?: "Close"}"
            // Update ability button with tower's ability name
            if (tower != null) {
                binding.btnAbility.text = "\u2728 ${tower.type.abilityName}"
            } else {
                binding.btnAbility.text = "\u2728 Ability"
            }
        }

        // Sell tower
        binding.btnSell.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                val value = (selected.sellValue() * (1f + engine.skillTree.sellValueBonus())).toInt()
                engine.sellTower(selected)
                gameView.clearSelectedTower()
                Toast.makeText(this, "\uD83D\uDCB8 Sold for ${value}g!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Tap a tower first!", Toast.LENGTH_SHORT).show()
        }

        // Tower ability
        binding.btnAbility.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                if (engine.activateTowerAbility(selected)) {
                    Toast.makeText(this, "\u2728 ${selected.type.abilityName}!", Toast.LENGTH_SHORT).show()
                } else {
                    val remaining = selected.abilityTimer.toInt()
                    Toast.makeText(this, "Cooldown: ${remaining}s", Toast.LENGTH_SHORT).show()
                }
            } else Toast.makeText(this, "Tap a tower first!", Toast.LENGTH_SHORT).show()
        }

        // Speed toggle
        binding.btnSpeed.setOnClickListener {
            engine.gameSpeed = when (engine.gameSpeed) {
                1 -> 2
                2 -> 3
                else -> 1
            }
            binding.btnSpeed.text = "\u25B6\uFE0F ${engine.gameSpeed}x"
            Toast.makeText(this, "Speed: ${engine.gameSpeed}x", Toast.LENGTH_SHORT).show()
        }

        // Pause
        binding.btnPause.setOnClickListener {
            engine.isPaused = !engine.isPaused
            binding.btnPause.text = if (engine.isPaused) "\u25B6\uFE0F" else "\u23F8\uFE0F"
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
    }

    override fun onPause() {
        super.onPause()
        binding.gameView.pause()
        // Auto-save if game is still running (not game over)
        val engine = binding.gameView.getEngine()
        if (!engine.gameOver && engine.wave > 0) {
            engine.saveGame()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.gameView.resume()
    }
}
