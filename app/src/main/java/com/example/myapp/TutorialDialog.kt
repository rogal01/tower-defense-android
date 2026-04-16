package com.example.myapp

import android.app.AlertDialog
import android.content.Context

object TutorialDialog {

    private val pages = listOf(
        "BASICS" to """
Your goal is to defend your base from enemy waves.

- Tap or drag on the map to move your hero. The hero auto-attacks nearby enemies.
- Your base health is the main life bar. If base HP reaches 0, the run ends.
- Gold is earned from kills and wave rewards. Spend it on towers, upgrades, powers, and repairs.
- Diamonds are rarer and are spent in the Skill Tree between runs.
- Between waves you also gain interest and a wave-complete bonus.
""",
        "TOWERS" to """
Place towers by tapping a tower button, then tapping an open build spot.

- Arrow: cheap and fast physical damage
- Magic: high magic damage and good range
- Cannon: slow splash damage
- Poison: damage over time
- Tesla: chain lightning
- Ice: slows enemies in an aura

- Tap a placed tower to select it.
- Use TOWER to upgrade it.
- Use TARGET to change targeting: Close, First, Last, or Strong.
- Same-type towers placed near each other gain a synergy bonus.
""",
        "POWERS" to """
Powers cost gold and also have cooldowns.

- Fireball: screen-wide damage
- Freeze: slows all enemies for a few seconds
- Heal: restores base HP
- Lightning: chains through several enemies

Dash is free but has a cooldown. It moves your hero quickly and damages enemies on the path.
""",
        "UPGRADES" to """
Hero upgrades improve your direct combat stats:

- ATK increases hero damage
- SPD increases hero movement speed
- HP increases hero max HP
- BASE increases base max HP

Tower upgrades increase damage, range, and fire rate.
Repair restores base HP when you need emergency recovery.
""",
        "ENEMIES" to """
Different enemies resist different damage types, so mixing towers matters.

Examples:
- Skeletons resist physical but are weak to magic
- Orcs resist physical and prefer explosive or ice counters
- Demons resist poison and are weak to ice
- Dragons resist physical and prefer magic or ice

Every 5th wave is a boss wave.
Boss abilities are shown under the boss HP bar so players can react without guessing.

Dragon Queen uses Screech, which slows your hero and jams nearby towers.
Elite enemies appear later and are stronger but give better rewards.
""",
        "ADVANCED" to """
- Day and night cycle every 8 waves
- Night makes enemies tougher and shortens tower range
- Fast kill streaks build combos for extra gold
- Critical hits add burst damage
- Some waves get special modifiers like Fast, Armored, Regen, Swarm, Ghostly, Treasure, or Rally
- Map types have their own battlefield mechanics
""",
        "MODES" to """
Campaign is the best starting point because it introduces mechanics step by step.

- Campaign: guided progression and rewards
- Endless: survival with scaling difficulty
- Boss Rush: boss every wave
- Daily Challenge: rotating modifier run
- Randomizer: scrambled rules and power behavior

Help & Mechanics stays available from the main menu whenever the player needs it.
"""
    )

    fun show(context: Context) {
        showPage(context, 0)
    }

    fun showFirstRun(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Welcome Commander")
            .setMessage(
                """
This game has a lot of mechanics, so the best place to start is Campaign plus the tutorial.

Campaign teaches towers, upgrades, powers, bosses, and special rules step by step.
You can always reopen Help & Mechanics from the main menu later.
                """.trimIndent()
            )
            .setPositiveButton("Open Tutorial") { _, _ -> show(context) }
            .setNegativeButton("Skip", null)
            .show()
    }

    private fun showPage(context: Context, index: Int) {
        val (title, content) = pages[index]
        val builder = AlertDialog.Builder(context)
            .setTitle("$title (${index + 1}/${pages.size})")
            .setMessage(content.trimIndent())
        if (index > 0) {
            builder.setNeutralButton("Back") { _, _ -> showPage(context, index - 1) }
        }
        if (index < pages.size - 1) {
            builder.setPositiveButton("Next") { _, _ -> showPage(context, index + 1) }
        } else {
            builder.setPositiveButton("Got it") { d, _ -> d.dismiss() }
        }
        builder.show()
    }
}
