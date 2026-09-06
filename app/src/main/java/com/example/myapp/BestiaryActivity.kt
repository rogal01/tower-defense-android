package com.example.myapp

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapp.databinding.ActivityBestiaryBinding
import com.example.myapp.game.EnemyType
import com.example.myapp.game.SpriteManager

class BestiaryActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityBestiaryBinding

    data class BestiaryEntry(
        val type: EnemyType,
        val displayName: String,
        val emoji: String,
        val tier: String,
        val hp: Int,
        val speed: Int,
        val damage: Int,
        val lore: String,
        val weaknesses: List<String>,
        val resistances: List<String>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBestiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        SpriteManager.initialize()
        loadBestiary()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadBestiary() {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val container = binding.bestiaryContainer
        container.removeAllViews()

        val entries = listOf(
            BestiaryEntry(
                EnemyType.GOBLIN, "Goblin Scout", "👺", "Tier 1 · Rusher",
                40, 120, 8,
                "Quick-footed woodland scavengers attacking in swift raiding packs. Easy to pick off from range.",
                listOf("None (Standard)"),
                emptyList()
            ),
            BestiaryEntry(
                EnemyType.SKELETON, "Skeleton Warrior", "💀", "Tier 1 · Swarm",
                60, 90, 12,
                "Reanimated bones clattering along the battlefield. Susceptible to holy magic and explosive concussions.",
                listOf("💥 Explosive (+20%)", "🔮 Magic (+40%)"),
                listOf("🌑 Dark (-30%)")
            ),
            BestiaryEntry(
                EnemyType.ORC, "Orc Brute", "👹", "Tier 2 · Heavy",
                180, 65, 25,
                "Muscular green brutes with thick hides and brutal spiked clubs. Shrugs off light arrow fire.",
                listOf("🔥 Fire (+20%)", "🔮 Magic (+10%)"),
                listOf("🏹 Physical (-20%)")
            ),
            BestiaryEntry(
                EnemyType.DEMON, "Hellfire Fiend", "😈", "Tier 3 · Elite",
                260, 80, 35,
                "Infernal winged horror birthed from volcanic fissures. Extremely resistant to flame but chilled by frost.",
                listOf("❄️ Ice (+50%)"),
                listOf("🔥 Fire (-70%)", "🌑 Dark (-50%)")
            ),
            BestiaryEntry(
                EnemyType.DRAGON, "Shadow Wyrm", "🐉", "Tier 3 · Apex Flying",
                450, 75, 50,
                "Ancient serpentine winged terror that flies above ground hazards. Massive health pool.",
                listOf("❄️ Ice (+40%)", "🌑 Dark (+30%)"),
                listOf("🏹 Physical (-40%)", "🔥 Fire (-60%)")
            ),
            BestiaryEntry(
                EnemyType.SLIME, "Acidic Slime", "🟢", "Tier 1 · Divider",
                80, 55, 10,
                "Amorphous acidic jelly. Splits into two mini-slimes upon fatal damage.",
                listOf("❄️ Ice (+30%)", "🔥 Fire (+30%)"),
                listOf("🏹 Physical (-30%)")
            ),
            BestiaryEntry(
                EnemyType.ARMORED_GOLEM, "Runic Stone Golem", "🗿", "Tier 3 · Fortress",
                500, 40, 40,
                "Living granite boulder reinforced with runic armor. Pierced only by artillery and raw arcane power.",
                listOf("💥 Explosive (+50%)", "🔮 Magic (+40%)"),
                listOf("🏹 Physical (-70%)")
            ),
            BestiaryEntry(
                EnemyType.BAT, "Vampire Bat", "🦇", "Tier 1 · Aerial Rusher",
                30, 140, 6,
                "Lightning-fast aerial pests flying directly across defenses in erratic swarms.",
                listOf("⚡ Tesla (+40%)"),
                emptyList()
            ),
            BestiaryEntry(
                EnemyType.SPIDER, "Venom Spider", "🕷️", "Tier 2 · Trapper",
                90, 100, 14,
                "Lurking arachnid dripping with neurotoxins. Immune to poison traps.",
                listOf("🔥 Fire (+50%)"),
                listOf("☠️ Poison (-50%)")
            ),
            BestiaryEntry(
                EnemyType.SHADOW, "Shadow Wraith", "👻", "Tier 2 · Ethereal",
                120, 95, 20,
                "Phase-shifting ghost. Physical weapons pass through its misty form.",
                listOf("🔮 Magic (+50%)", "🔥 Fire (+40%)"),
                listOf("🏹 Physical (-70%)", "🌑 Dark (-80%)")
            ),
            BestiaryEntry(
                EnemyType.BERSERKER, "Blood Berserker", "🪓", "Tier 3 · Enrager",
                220, 70, 30,
                "Raging berserker who moves and attacks faster as his life ebbs away. Ice towers cool his fury.",
                listOf("❄️ Ice (+30%)"),
                listOf("🔥 Fire (-30%)")
            ),
            BestiaryEntry(
                EnemyType.COMMANDER, "Legion Commander", "🎖️", "Tier 3 · Vanguard",
                320, 60, 25,
                "Veteran tactician projecting a defense banner that shields surrounding allies.",
                listOf("🔮 Magic (+30%)"),
                listOf("🏹 Physical (-20%)")
            ),
            BestiaryEntry(
                EnemyType.NECROMANCER, "Dark Necromancer", "🧙‍♂️", "Tier 3 · Summoner",
                240, 60, 20,
                "Grim spellcaster clad in violet silks. Chants blasphemous rites to raise fallen skeletons mid-wave. Prioritize elimination with Sniper Perch towers!",
                listOf("💥 Explosive (+40%)", "🏹 Physical (+20%)"),
                listOf("🌑 Dark (-60%)", "🔮 Magic (-30%)")
            ),
            BestiaryEntry(
                EnemyType.GHOST, "Phantom Ghost", "👻", "Tier 2 · Infiltrator",
                110, 95, 15,
                "Ethereal spirit shifting between realms. Periodically enters intangible phase where physical arrows and cannonballs pass right through.",
                listOf("🔮 Magic (+60%)", "⚡ Tesla (+50%)"),
                listOf("🏹 Physical (-75%)", "💥 Explosive (-60%)", "🌑 Dark (-70%)")
            ),
            BestiaryEntry(
                EnemyType.MAGMA_CRAB, "Magma Crab", "🦀", "Tier 2 · Molten Tank",
                280, 45, 22,
                "Volcanic crustacean forged in caldera lava pits. Immune to burning flame and resistant to physical blows, but chilled frost towers easily shatter its shell.",
                listOf("❄️ Ice (+80%)"),
                listOf("🔥 Fire (Immune -90%)", "🏹 Physical (-40%)", "☠️ Poison (-70%)")
            ),
            BestiaryEntry(
                EnemyType.HARPY, "Screeching Harpy", "🦅", "Tier 2 · Aerial Disrupter",
                130, 125, 18,
                "Fierce winged raptor soaring above ground hazards. Emits disorienting sonic screeches that temporarily delay nearby towers' reload mechanisms.",
                listOf("⚡ Tesla (+40%)", "🏹 Physical (+30%)"),
                listOf("💥 Explosive (-60%)")
            ),
            BestiaryEntry(
                EnemyType.TREANT, "Ancient Treant", "🌲", "Tier 3 · Colossus",
                480, 35, 32,
                "Lumbering sentient oak giant with dense bark absorbing blunt impact. Heals rapidly over time unless set ablaze by Flame towers, which halts regeneration and deals double burn.",
                listOf("🔥 Fire (+80%)"),
                listOf("⚡ Tesla (-40%)", "💥 Explosive (-30%)", "🏹 Physical (-30%)", "☠️ Poison (-60%)")
            )
        )

        val totalKills = prefs.getInt("stats_total_kills", 0)
        binding.textMonsterSummary.text = "📜 ${entries.size} Creatures Catalogued · Total Enemies Slain: $totalKills"

        for (entry in entries) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val bg = GradientDrawable().apply {
                    cornerRadius = dp(12).toFloat()
                    setColor(0xEE161F30.toInt())
                    setStroke(dp(1), 0x4400E5FF.toInt())
                }
                background = bg
                setPadding(dp(14), dp(12), dp(14), dp(12))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = dp(12)
                layoutParams = lp
            }

            // Top Row: Sprite + Title + Tier
            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Sprite preview
            val spriteView = ImageView(this).apply {
                val sBmp = SpriteManager.getEnemyBitmap(entry.type, 0L)
                if (sBmp != null) {
                    setImageBitmap(sBmp)
                }
                val imgLp = LinearLayout.LayoutParams(dp(44), dp(44)).apply {
                    marginEnd = dp(12)
                }
                layoutParams = imgLp
            }
            topRow.addView(spriteView)

            // Title & Tier
            val titleCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val titleView = TextView(this).apply {
                text = "${entry.emoji} ${entry.displayName}"
                textSize = 16f
                setTextColor(Color.parseColor("#FFD54F"))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            val tierView = TextView(this).apply {
                text = entry.tier
                textSize = 12f
                setTextColor(Color.parseColor("#00E5FF"))
            }
            titleCol.addView(titleView)
            titleCol.addView(tierView)
            topRow.addView(titleCol)

            card.addView(topRow)

            // Lore description
            val loreView = TextView(this).apply {
                text = entry.lore
                textSize = 13f
                setTextColor(Color.parseColor("#CFD8DC"))
                setPadding(0, dp(6), 0, dp(6))
            }
            card.addView(loreView)

            // Stats row
            val statsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, dp(6))
            }
            statsRow.addView(createPill("❤️ ${entry.hp} HP", "#EF5350"))
            statsRow.addView(createPill("⚡ ${entry.speed} Spd", "#42A5F5"))
            statsRow.addView(createPill("⚔️ ${entry.damage} Dmg", "#FFA726"))
            card.addView(statsRow)

            // Weaknesses
            if (entry.weaknesses.isNotEmpty()) {
                val weakView = TextView(this).apply {
                    text = "💥 Weak to: ${entry.weaknesses.joinToString(", ")}"
                    textSize = 12f
                    setTextColor(Color.parseColor("#66BB6A"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                card.addView(weakView)
            }

            // Resistances
            if (entry.resistances.isNotEmpty()) {
                val resView = TextView(this).apply {
                    text = "🛡️ Resists: ${entry.resistances.joinToString(", ")}"
                    textSize = 12f
                    setTextColor(Color.parseColor("#FF7043"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                card.addView(resView)
            }

            container.addView(card)
        }
    }

    private fun createPill(text: String, colorHex: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
            val bg = GradientDrawable().apply {
                cornerRadius = dp(6).toFloat()
                setColor(Color.parseColor(colorHex))
            }
            background = bg
            setPadding(dp(8), dp(3), dp(8), dp(3))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = dp(6)
            }
            layoutParams = lp
        }
    }

    private fun dp(v: Int): Int {
        return (v * resources.displayMetrics.density).toInt()
    }
}
