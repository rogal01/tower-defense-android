package com.example.myapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityMainBinding
import com.example.myapp.game.PowerType
import com.example.myapp.game.TowerType

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val gameView = binding.gameView
        val engine = gameView.getEngine()

        // HUD updates from game thread
        gameView.onGoldChanged = { gold ->
            binding.textGold.text = "\uD83D\uDCB0 $gold"
        }
        gameView.onWaveChanged = { wave ->
            binding.textWave.text = "\u2694\uFE0F Wave $wave"
        }
        gameView.onStatsChanged = {
            binding.textKills.text = "\uD83D\uDC80 ${engine.totalKills}"
        }
        gameView.onGameOver = { score, wave -> }

        // Tower placement buttons
        binding.btnTowerArrow.setOnClickListener {
            if (engine.gold >= TowerType.ARROW.baseCost) {
                gameView.placementMode = TowerType.ARROW
                Toast.makeText(this, "Tap to place \uD83C\uDFF9 Arrow (${TowerType.ARROW.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerMagic.setOnClickListener {
            if (engine.gold >= TowerType.MAGIC.baseCost) {
                gameView.placementMode = TowerType.MAGIC
                Toast.makeText(this, "Tap to place \uD83E\uDDE8 Magic (${TowerType.MAGIC.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerCannon.setOnClickListener {
            if (engine.gold >= TowerType.CANNON.baseCost) {
                gameView.placementMode = TowerType.CANNON
                Toast.makeText(this, "Tap to place \uD83D\uDCA3 Cannon (${TowerType.CANNON.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerPoison.setOnClickListener {
            if (engine.gold >= TowerType.POISON.baseCost) {
                gameView.placementMode = TowerType.POISON
                Toast.makeText(this, "Tap to place \u2620\uFE0F Poison (${TowerType.POISON.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }
        binding.btnTowerTesla.setOnClickListener {
            if (engine.gold >= TowerType.TESLA.baseCost) {
                gameView.placementMode = TowerType.TESLA
                Toast.makeText(this, "Tap to place \u26A1 Tesla (${TowerType.TESLA.baseCost}g)", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }

        // Power buttons
        binding.btnPowerFireball.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.FIREBALL)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FIREBALL)) {
                Toast.makeText(this, "\uD83D\uDD25 Fireball! AoE damage!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need 15g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerFreeze.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.FREEZE)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.FREEZE)) {
                Toast.makeText(this, "\u2744\uFE0F Freeze! Enemies slowed!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need 10g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerHeal.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.HEAL)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.HEAL)) {
                Toast.makeText(this, "\uD83D\uDC9A Heal! Base +50 HP!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need 20g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnPowerLightning.setOnClickListener {
            val cd = engine.getPowerCooldown(PowerType.LIGHTNING)
            if (cd > 0) { Toast.makeText(this, "Cooldown: ${cd.toInt()}s", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (engine.usePower(PowerType.LIGHTNING)) {
                Toast.makeText(this, "\u26A1 Lightning! Chain damage!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Need 25g!", Toast.LENGTH_SHORT).show()
        }

        // Repair base
        binding.btnRepair.setOnClickListener {
            if (engine.repairBase()) {
                Toast.makeText(this, "\uD83D\uDD27 Base repaired!", Toast.LENGTH_SHORT).show()
            } else if (engine.baseHp >= engine.maxBaseHp) {
                Toast.makeText(this, "Base is full HP!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Not enough gold!", Toast.LENGTH_SHORT).show()
        }

        // Upgrade buttons
        binding.btnUpDamage.setOnClickListener {
            val cost = engine.playerDamageLevel * 25
            if (engine.upgradePlayerDamage()) Toast.makeText(this, "\u2694\uFE0F Attack up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpSpeed.setOnClickListener {
            val cost = engine.playerSpeedLevel * 20
            if (engine.upgradePlayerSpeed()) Toast.makeText(this, "\uD83D\uDC5F Speed up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpHp.setOnClickListener {
            val cost = engine.playerHpLevel * 30
            if (engine.upgradePlayerHp()) Toast.makeText(this, "\u2764\uFE0F HP up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpBase.setOnClickListener {
            val cost = engine.baseHpLevel * 40
            if (engine.upgradeBaseHp()) Toast.makeText(this, "\uD83C\uDFF0 Base up! (${cost}g)", Toast.LENGTH_SHORT).show()
            else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
        }
        binding.btnUpTower.setOnClickListener {
            val selected = gameView.getSelectedTower()
            if (selected != null) {
                val cost = selected.upgradeCost()
                if (engine.upgradeTower(selected)) Toast.makeText(this, "\u2B06\uFE0F Tower Lv${selected.level}! (${cost}g)", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Need ${cost}g!", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Tap a tower first!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.gameView.pause()
    }

    override fun onResume() {
        super.onResume()
        binding.gameView.resume()
    }
}
