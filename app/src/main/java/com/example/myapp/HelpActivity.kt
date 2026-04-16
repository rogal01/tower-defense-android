package com.example.myapp

import android.os.Bundle
import com.example.myapp.databinding.ActivityHelpBinding

class HelpActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.textHelpContent.text = buildHelpText()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun buildHelpText(): CharSequence = """
BASE
Your base is at the bottom of the map. If enemies reach it, they deal damage. When base HP reaches 0, the run ends.

PLAYER
Your hero auto-attacks nearby enemies. Tap the map to move. The hero helps control leaks but the base is still the main thing you protect.

DASH
Dash moves your hero quickly and damages enemies on the path. It is free, but it has a cooldown.

TOWERS
Tap a tower button, then tap the map to place it.

- Arrow: fast physical damage
- Magic: strong magic damage
- Cannon: splash damage
- Poison: damage over time
- Tesla: chain lightning
- Ice: slowing aura

TOWER UPGRADES
Tap a placed tower to select it, then use TOWER to upgrade it. Upgrades improve damage, range, and attack speed.

TARGETING
Selected towers can target Close, First, Last, or Strong enemies.

POWERS
- Fireball damages all enemies
- Freeze slows all enemies
- Heal restores base HP
- Lightning chains through multiple enemies

ENEMIES
Enemy types have different resistances, so mixed defenses are important.

BOSSES
Every 5th wave is a boss wave. The active boss ability is shown under the boss HP bar during the fight.

Common boss abilities:
- Charge: rushes the base
- Summon: spawns extra minions
- Heal: restores boss HP
- Flame Burst: disables nearby towers and can hit the base
- Shield: temporary damage reduction
- Screech: slows your hero and jams nearby towers
- Teleport: jumps ahead on the path
- Drain: steals your gold
- Quake: slows all towers
- Split: creates clones at low HP

ELITES
Elite enemies appear on later non-boss waves. They are stronger but give better rewards.

MAPS
Each map has a special rule:
- Classic: no extra rule
- Valley: high-ground towers deal more damage
- Crossroads: the center crossing is a kill zone
- Desert: sandstorms reduce range but slow enemies
- Snow: slows and freeze effects are stronger

MODES
Campaign is the best starting point for new players because it introduces mechanics step by step.
""".trimIndent()
}
