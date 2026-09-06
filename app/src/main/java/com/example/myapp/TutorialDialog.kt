package com.example.myapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapp.game.SfxType

object TutorialDialog {

    private data class TutorialCard(
        val icon: String,
        val title: String,
        val desc: String,
        val accentColor: String = "#FFD54F"
    )

    private data class TutorialChapter(
        val title: String,
        val subtitle: String,
        val cards: List<TutorialCard>
    )

    fun show(context: Context) {
        GameStrings.init(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_tutorial_codex, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val subtitleView = dialogView.findViewById<TextView>(R.id.text_chapter_subtitle)
        val closeBtn = dialogView.findViewById<TextView>(R.id.btn_tutorial_close)
        val container = dialogView.findViewById<LinearLayout>(R.id.tutorial_content_container)
        val indicatorView = dialogView.findViewById<TextView>(R.id.text_step_indicator)
        val prevBtn = dialogView.findViewById<Button>(R.id.btn_tutorial_prev)
        val nextBtn = dialogView.findViewById<Button>(R.id.btn_tutorial_next)

        val tabBasics = dialogView.findViewById<Button>(R.id.tab_basics)
        val tabTowers = dialogView.findViewById<Button>(R.id.tab_towers)
        val tabPowers = dialogView.findViewById<Button>(R.id.tab_powers)
        val tabMonsters = dialogView.findViewById<Button>(R.id.tab_monsters)
        val tabProgression = dialogView.findViewById<Button>(R.id.tab_progression)
        val tabs = listOf(tabBasics, tabTowers, tabPowers, tabMonsters, tabProgression)

        val chapters = listOf(
            TutorialChapter(
                "Chapter 1: Hero & Base Defense",
                "Master the core battlefield mechanics and resources",
                listOf(
                    TutorialCard("🎮", "Hero Movement & Combat", "Tap or drag anywhere on the battlefield to move your Hero. Your hero automatically strikes nearby monsters with sword attacks and intercepts rushers.", "#00E5FF"),
                    TutorialCard("🏰", "Sanctuary Base (100 HP)", "Defend the lower sanctum! If enemies slip through your defenses and breach the base, its health drops. Use the 'Repair Base' button between waves to restore HP.", "#81C784"),
                    TutorialCard("💰", "Gold & 5% Wave Interest", "Earn gold by slaying monsters. Keep extra gold banked — you earn a compounding +5% bank interest bonus at the end of each wave!", "#FFD700"),
                    TutorialCard("💎", "Diamonds & Boss Drops", "Rare gemstones earned by felling mighty Bosses and conquering Campaign missions. Diamonds are used to unlock permanent skill tree perks.", "#BA68C8")
                )
            ),
            TutorialChapter(
                "Chapter 2: Arsenal & Towers",
                "Deploy defenses and unlock Level 5 branching specializations",
                listOf(
                    TutorialCard("🏹", "Arrow Tower (30g)", "Fast, cheap physical ballistics. At Level 5, specialize into 🎯 Sniper (+60 range, 2.5x crit) or 🏹 Ranger (triple arrow volley).", "#FFB74D"),
                    TutorialCard("🔮", "Magic Tower (60g)", "High arcane damage ignoring physical armor. Specializes into 🔮 Arcane Beam (ramping boss melt) or 🌀 Rift Altar (teleports enemies back).", "#B388FF"),
                    TutorialCard("💣", "Cannon Tower (100g)", "Heavy area-of-effect explosive blasts. Specializes into 💥 Cluster Mortar (fragmenting bomblets) or ⚡ Railgun (linear piercing beam).", "#FF8A65"),
                    TutorialCard("⚡", "Tesla (120g) & ❄️ Frost (70g)", "Tesla chains high-voltage electricity across up to 4 targets. Frost towers emit freezing auras slowing entire enemy swarms by up to 60%.", "#80DEEA"),
                    TutorialCard("🎯", "Targeting Priorities", "Tap any placed tower on the field to cycle targeting modes: CLOSE (nearest threat), FIRST (lead runner), LAST (tail), or STRONG (focus high-HP elites/bosses).", "#4DD0E1")
                )
            ),
            TutorialChapter(
                "Chapter 3: Spells & Fortifications",
                "Turn the tide with active divine powers and placed obstacles",
                listOf(
                    TutorialCard("🔥", "Fireball (40g)", "Calls down a blazing meteor from the skies, dealing 120 splash damage to all enemies clustered in the impact zone.", "#FF7043"),
                    TutorialCard("❄️", "Blizzard Freeze (30g)", "Flash-freezes all monsters across the entire map, paralyzing them in place for 4 critical seconds.", "#4FC3F7"),
                    TutorialCard("💚", "Divine Heal (25g)", "Surges restorative holy light, restoring 35 HP to both the Hero and Sanctuary Base in emergency moments.", "#81C784"),
                    TutorialCard("⚡", "Lightning Strike (50g)", "Calls a pinpoint lightning bolt striking the strongest monster on screen for massive single-target boss damage.", "#FFF176"),
                    TutorialCard("🛡️", "Blockades & Traps", "Erect wooden barricades to halt enemy advance at choke points, and lay Spike and Tar traps on paths for damage and slow.", "#A1887F")
                )
            ),
            TutorialChapter(
                "Chapter 4: Tactical Weaknesses",
                "Exploit enemy vulnerabilities with the right damage types",
                listOf(
                    TutorialCard("🧙‍♂️", "Necromancers (Weak to Physical +40%)", "Dark summoners that chant every 7s to raise skeleton minions. Focus fire with Sniper and Ballista towers before summon waves grow!", "#CE93D8"),
                    TutorialCard("👻", "Phantom Ghosts (Weak to Magic +60%)", "Ethereal phasing allows ghosts to slip past physical attacks (-75% damage). Disintegrate them with Magic and Tesla electricity!", "#80DEEA"),
                    TutorialCard("🦀", "Magma Crabs (Weak to Frost +80%)", "Molten volcanic shell is nearly immune to fire (-90%). Shatter their shells with Frost towers and ice magic for thermal shock damage!", "#FF8A65"),
                    TutorialCard("🦅", "Screeching Harpies (Weak to Ballistics)", "High-speed aerial flyers emitting sonic shrieks that jam tower reloads. Shoot them down from afar with Ranger and Sniper towers!", "#B39DDB"),
                    TutorialCard("🌲", "Ancient Treants (Weak to Flame +80%)", "Colossi with dense bark regenerating 8 HP/sec. Ignite them with Flame towers to completely halt their healing and deal double burn damage!", "#A5D6A7")
                )
            ),
            TutorialChapter(
                "Chapter 5: Permanent Progression",
                "Grow stronger across every campaign run and endless wave",
                listOf(
                    TutorialCard("💎", "The Skill Tree", "Spend your hard-earned Diamonds in the Skill Tree (Main Menu) to unlock permanent starting gold, base armor, tower damage, and hero speed.", "#BA68C8"),
                    TutorialCard("📜", "Campaign Star Mastery", "Earn up to 3 stars per mission by beating wave targets with high score combos and healthy base HP to claim big diamond bounties.", "#FFD54F"),
                    TutorialCard("👑", "Endless Milestone Perks", "Survive to wave 25, 50, 75, and 100 in Endless Mode to draft game-changing buffs (+20% Tower Damage, Double Interest, Quick Cast).", "#00E5FF")
                )
            )
        )

        var currentChapter = 0

        fun dp(value: Int): Int =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()

        fun renderChapter(index: Int) {
            currentChapter = index
            val ch = chapters[index]
            subtitleView.text = ch.title

            // Update Tab styles
            tabs.forEachIndexed { i, btn ->
                btn.setBackgroundResource(if (i == index) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
                btn.setTextColor(if (i == index) Color.WHITE else Color.parseColor("#B0BEC5"))
            }

            // Update Step Dots
            val dots = StringBuilder()
            for (i in chapters.indices) {
                dots.append(if (i == index) "● " else "○ ")
            }
            indicatorView.text = dots.toString().trim()

            // Update Prev/Next Buttons
            prevBtn.visibility = if (index > 0) View.VISIBLE else View.GONE
            if (index == chapters.size - 1) {
                nextBtn.text = "⚔️ JUMP INTO BATTLE"
                nextBtn.setBackgroundResource(R.drawable.bg_btn_green)
            } else {
                nextBtn.text = "Next Chapter ➔"
                nextBtn.setBackgroundResource(R.drawable.bg_btn_green)
            }

            // Render Chapter Cards
            container.removeAllViews()
            for (card in ch.cards) {
                val cardLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.bg_hud_chip)
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                    layoutParams = lp
                }

                val headerRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                }

                val iconView = TextView(context).apply {
                    text = card.icon
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    setPadding(0, 0, dp(8), 0)
                }

                val cardTitle = TextView(context).apply {
                    text = card.title
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(Color.parseColor(card.accentColor))
                    setTypeface(typeface, Typeface.BOLD)
                }

                headerRow.addView(iconView)
                headerRow.addView(cardTitle)
                cardLayout.addView(headerRow)

                val cardDesc = TextView(context).apply {
                    text = card.desc
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(Color.parseColor("#ECEFF1"))
                    setLineSpacing(dp(2).toFloat(), 1f)
                    setPadding(dp(24), dp(4), 0, 0)
                }
                cardLayout.addView(cardDesc)

                container.addView(cardLayout)
            }
        }

        // Tab click listeners
        tabs.forEachIndexed { i, btn ->
            btn.setOnClickListener {
                SoundManager.play(SfxType.UI_CLICK)
                renderChapter(i)
            }
        }

        prevBtn.setOnClickListener {
            if (currentChapter > 0) {
                SoundManager.play(SfxType.UI_CLICK)
                renderChapter(currentChapter - 1)
            }
        }

        nextBtn.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            if (currentChapter < chapters.size - 1) {
                renderChapter(currentChapter + 1)
            } else {
                dialog.dismiss()
            }
        }

        closeBtn.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
        }

        renderChapter(0)
        dialog.show()
    }
}

