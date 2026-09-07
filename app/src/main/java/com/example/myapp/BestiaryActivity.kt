package com.example.myapp

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.myapp.databinding.ActivityBestiaryBinding
import com.example.myapp.game.BossAbility
import com.example.myapp.game.BossType
import com.example.myapp.game.EnemyType
import com.example.myapp.game.SpriteManager

class BestiaryActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityBestiaryBinding

    enum class BestiaryCategory(val labelPl: String, val labelEn: String, val icon: String) {
        ALL("Wszystkie", "All", "🌐"),
        SWARM("Rój", "Swarm", "⚡"),
        TANKS("Opancerzeni", "Tanks", "🛡️"),
        MAGIC("Magia", "Magic", "🔮"),
        BOSSES("Bossowie", "Bosses", "👑")
    }

    data class BestiaryEntry(
        val type: EnemyType,
        val bossType: BossType? = null,
        val nameEn: String,
        val namePl: String,
        val emoji: String,
        val category: BestiaryCategory,
        val roleEn: String,
        val rolePl: String,
        val hp: Int,
        val speed: Int,
        val damage: Int,
        val loreEn: String,
        val lorePl: String,
        val weaknessesEn: List<String>,
        val weaknessesPl: List<String>,
        val resistancesEn: List<String>,
        val resistancesPl: List<String>,
        val tacticalCounterEn: String,
        val tacticalCounterPl: String
    )

    private var selectedCategory: BestiaryCategory = BestiaryCategory.ALL
    private val allEntries = mutableListOf<BestiaryEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBestiaryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        SpriteManager.initialize()
        buildCatalog()
        setupCategoryTabs()
        renderCatalog()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupCategoryTabs() {
        val container = binding.categoryTabContainer
        container.removeAllViews()
        val isPl = GameStrings.isPl

        for (cat in BestiaryCategory.entries) {
            val btn = Button(this).apply {
                text = "${cat.icon} ${if (isPl) cat.labelPl else cat.labelEn}"
                textSize = 12f
                isAllCaps = false
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(38)
                ).apply {
                    marginEnd = dp(8)
                }
                layoutParams = lp
                setPadding(dp(14), 0, dp(14), 0)

                updateTabStyle(this, cat == selectedCategory)

                setOnClickListener {
                    if (selectedCategory != cat) {
                        selectedCategory = cat
                        refreshTabs()
                        renderCatalog()
                    }
                }
            }
            container.addView(btn)
        }
    }

    private fun refreshTabs() {
        val container = binding.categoryTabContainer
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i) as? Button ?: continue
            val cat = BestiaryCategory.entries.getOrNull(i) ?: continue
            updateTabStyle(child, cat == selectedCategory)
        }
    }

    private fun updateTabStyle(btn: Button, isActive: Boolean) {
        if (isActive) {
            btn.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_active)
            btn.setTextColor(Color.WHITE)
            btn.setTypeface(null, Typeface.BOLD)
        } else {
            btn.background = ContextCompat.getDrawable(this, R.drawable.bg_tab_inactive)
            btn.setTextColor(Color.parseColor("#B0BEC5"))
            btn.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun renderCatalog() {
        val isPl = GameStrings.isPl
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val totalKills = prefs.getInt("stats_total_kills", 0)
        val container = binding.bestiaryContainer
        container.removeAllViews()

        val filtered = if (selectedCategory == BestiaryCategory.ALL) {
            allEntries
        } else {
            allEntries.filter { it.category == selectedCategory }
        }

        binding.textMonsterSummary.text = if (isPl) {
            "📜 Wyświetlono: ${filtered.size} / ${allEntries.size} · Pokonani wrogowie: $totalKills"
        } else {
            "📜 Showing: ${filtered.size} / ${allEntries.size} · Enemies Slain: $totalKills"
        }

        for (entry in filtered) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val bg = GradientDrawable().apply {
                    cornerRadius = dp(12).toFloat()
                    if (entry.bossType != null) {
                        setColor(0xEE2A1526.toInt())
                        setStroke(dp(1), 0x88FF5252.toInt())
                    } else {
                        setColor(0xEE161F30.toInt())
                        setStroke(dp(1), 0x4400E5FF.toInt())
                    }
                }
                background = bg
                setPadding(dp(14), dp(12), dp(14), dp(12))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(12)
                }
                layoutParams = lp
            }

            // Top Row: Sprite + Title + Tier/Role
            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val spriteView = ImageView(this).apply {
                val sBmp = SpriteManager.getEnemyBitmap(entry.type, 0L)
                    ?: entry.bossType?.let { SpriteManager.getEnemyBitmap(it.minionType, 0L) }
                if (sBmp != null) {
                    setImageBitmap(sBmp)
                }
                val imgLp = LinearLayout.LayoutParams(dp(44), dp(44)).apply {
                    marginEnd = dp(12)
                }
                layoutParams = imgLp
            }
            topRow.addView(spriteView)

            val titleCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val titleView = TextView(this).apply {
                text = "${entry.emoji} ${if (isPl) entry.namePl else entry.nameEn}"
                textSize = 16f
                setTextColor(if (entry.bossType != null) Color.parseColor("#FF5252") else Color.parseColor("#FFD54F"))
                setTypeface(null, Typeface.BOLD)
            }
            val roleView = TextView(this).apply {
                text = if (isPl) entry.rolePl else entry.roleEn
                textSize = 12f
                setTextColor(Color.parseColor("#00E5FF"))
            }
            titleCol.addView(titleView)
            titleCol.addView(roleView)
            topRow.addView(titleCol)

            card.addView(topRow)

            // Lore description
            val loreView = TextView(this).apply {
                text = if (isPl) entry.lorePl else entry.loreEn
                textSize = 13f
                setTextColor(Color.parseColor("#CFD8DC"))
                setPadding(0, dp(6), 0, dp(6))
            }
            card.addView(loreView)

            // Stats row
            val statsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(2), 0, dp(6))
            }
            statsRow.addView(createPill("❤️ ${entry.hp} HP", "#EF5350"))
            statsRow.addView(createPill("⚡ ${entry.speed} Spd", "#42A5F5"))
            statsRow.addView(createPill("⚔️ ${entry.damage} Dmg", "#FFA726"))
            if (entry.bossType != null) {
                statsRow.addView(createPill("👑 BOSS", "#AB47BC"))
            }
            card.addView(statsRow)

            // Boss Special Ability Banner
            if (entry.bossType != null) {
                val bossBox = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    val boxBg = GradientDrawable().apply {
                        cornerRadius = dp(6).toFloat()
                        setColor(0x33AB47BC.toInt())
                        setStroke(dp(1), 0x55AB47BC.toInt())
                    }
                    background = boxBg
                    setPadding(dp(8), dp(6), dp(8), dp(6))
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(6)
                    }
                    layoutParams = lp
                }
                val abilityText = TextView(this).apply {
                    val abName = entry.bossType.ability.name
                    text = if (isPl) "⚡ Zdolność Bossa: $abName | Miniony: ${entry.bossType.minionCount}x"
                           else "⚡ Boss Ability: $abName | Minions: ${entry.bossType.minionCount}x"
                    textSize = 12f
                    setTextColor(Color.parseColor("#E1BEE7"))
                    setTypeface(null, Typeface.BOLD)
                }
                bossBox.addView(abilityText)
                card.addView(bossBox)
            }

            // Weaknesses
            val weaknesses = if (isPl) entry.weaknessesPl else entry.weaknessesEn
            if (weaknesses.isNotEmpty()) {
                val weakView = TextView(this).apply {
                    text = (if (isPl) "💥 Podatności: " else "💥 Weak to: ") + weaknesses.joinToString(", ")
                    textSize = 12f
                    setTextColor(Color.parseColor("#66BB6A"))
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, dp(2), 0, dp(2))
                }
                card.addView(weakView)
            }

            // Resistances
            val resistances = if (isPl) entry.resistancesPl else entry.resistancesEn
            if (resistances.isNotEmpty()) {
                val resView = TextView(this).apply {
                    text = (if (isPl) "🛡️ Odporności: " else "🛡️ Resists: ") + resistances.joinToString(", ")
                    textSize = 12f
                    setTextColor(Color.parseColor("#FF7043"))
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, dp(2), 0, dp(2))
                }
                card.addView(resView)
            }

            // Tactical Counter Advice Box
            val counterBox = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val counterBg = GradientDrawable().apply {
                    cornerRadius = dp(6).toFloat()
                    setColor(0x2200E5FF.toInt())
                    setStroke(dp(1), 0x4400E5FF.toInt())
                }
                background = counterBg
                setPadding(dp(8), dp(6), dp(8), dp(6))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(6)
                }
                layoutParams = lp
            }
            val counterText = TextView(this).apply {
                text = (if (isPl) "🎯 Taktyka: " else "🎯 Tactical Counter: ") +
                        (if (isPl) entry.tacticalCounterPl else entry.tacticalCounterEn)
                textSize = 12f
                setTextColor(Color.parseColor("#80DEEA"))
            }
            counterBox.addView(counterText)
            card.addView(counterBox)

            container.addView(card)
        }
    }

    private fun createPill(text: String, colorHex: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
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

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun buildCatalog() {
        allEntries.clear()

        // --- REGULAR ENEMIES (18) ---
        allEntries.add(
            BestiaryEntry(
                type = EnemyType.GOBLIN,
                nameEn = "Goblin Scout",
                namePl = "Goblin Zwiadowca",
                emoji = "👺",
                category = BestiaryCategory.SWARM,
                roleEn = "Tier 1 · Rusher",
                rolePl = "Poziom 1 · Zwiadowca",
                hp = 40, speed = 120, damage = 8,
                loreEn = "Quick-footed woodland scavengers attacking in swift raiding packs. Easy to pick off from range.",
                lorePl = "Szybki leśny grabieżca atakujący w licznych hordach. Łatwy do zlikwidowania z dystansu.",
                weaknessesEn = listOf("Standard vulnerability"),
                weaknessesPl = listOf("Standardowa podatność"),
                resistancesEn = emptyList(),
                resistancesPl = emptyList(),
                tacticalCounterEn = "Archer Towers thin their ranks quickly. Place spike traps at chokepoints.",
                tacticalCounterPl = "Wieża Łucznicza szybko dziesiątkuje zwiadowców. Ustaw pułapki kolcowe na zakrętach."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.SKELETON,
                nameEn = "Skeleton Warrior",
                namePl = "Szkielet Wojownik",
                emoji = "💀",
                category = BestiaryCategory.SWARM,
                roleEn = "Tier 1 · Swarm",
                rolePl = "Poziom 1 · Rój Kości",
                hp = 60, speed = 90, damage = 12,
                loreEn = "Reanimated bones clattering along the battlefield. Highly susceptible to arcane magic and explosive concussions.",
                lorePl = "Ożywione kości stukoczące w marszu na bazę. Kruche na potężne wybuchy i magię.",
                weaknessesEn = listOf("💥 Explosive (+20%)", "🔮 Magic (+40%)"),
                weaknessesPl = listOf("💥 Wybuch (+20%)", "🔮 Magia (+40%)"),
                resistancesEn = listOf("🌑 Dark (-30%)"),
                resistancesPl = listOf("🌑 Mrok (-30%)"),
                tacticalCounterEn = "Mage Towers and explosive Cannon artillery grind bone to dust.",
                tacticalCounterPl = "Wieże Magiczne oraz Działa przeciwpiechotne obracają kości w proch."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.FAST_SKELETON,
                nameEn = "Skeleton Sprinter",
                namePl = "Szkielet Sprinter",
                emoji = "💀",
                category = BestiaryCategory.SWARM,
                roleEn = "Tier 1 · Rapid Skirmisher",
                rolePl = "Poziom 1 · Szybki Szturmowiec",
                hp = 45, speed = 135, damage = 10,
                loreEn = "Light unarmored bone structure providing extreme agility. Dashes past slow firing artillery.",
                lorePl = "Lekkie kości pozbawione zbroi dające niesamowitą zwinność. Błyskawicznie przemyka obok wolnych wież.",
                weaknessesEn = listOf("🔮 Magic (+40%)", "🔥 Fire (+30%)", "💥 Explosive (+20%)"),
                weaknessesPl = listOf("🔮 Magia (+40%)", "🔥 Ogień (+30%)", "💥 Wybuch (+20%)"),
                resistancesEn = listOf("🏹 Physical (-40%)", "🌑 Dark (-30%)"),
                resistancesPl = listOf("🏹 Fizyczne (-40%)", "💀 Mrok (-30%)"),
                tacticalCounterEn = "Ice Towers immediately neutralize its dash speed, leaving it vulnerable.",
                tacticalCounterPl = "Wieża Lodowa natychmiast niweluje jego przewagę prędkości."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BAT,
                nameEn = "Vampire Bat",
                namePl = "Wampirzy Nietoperz",
                emoji = "🦇",
                category = BestiaryCategory.SWARM,
                roleEn = "Tier 1 · Aerial Rusher",
                rolePl = "Poziom 1 · Latający Rój",
                hp = 30, speed = 140, damage = 6,
                loreEn = "Lightning-fast aerial pests flying directly across defenses in erratic swarms.",
                lorePl = "Błyskawiczne skrzydlate szkodniki omijające naziemne przeszkody i pułapki.",
                weaknessesEn = listOf("⚡ Tesla (+40%)", "🏹 Physical (+20%)"),
                weaknessesPl = listOf("⚡ Tesla (+40%)", "🏹 Fizyczne (+20%)"),
                resistancesEn = listOf("Ground hazards (Immune)"),
                resistancesPl = listOf("Pułapki naziemne (Niewrażliwy)"),
                tacticalCounterEn = "Tesla Towers and high fire-rate archers swat them down effortlessly.",
                tacticalCounterPl = "Wieże Tesla oraz Łucznicy zdejmują je z nieba w ułamku sekundy."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.SPIDER,
                nameEn = "Venom Spider",
                namePl = "Pająk Jadowity",
                emoji = "🕷️",
                category = BestiaryCategory.SWARM,
                roleEn = "Tier 2 · Trapper",
                rolePl = "Poziom 2 · Truciciel",
                hp = 90, speed = 100, damage = 14,
                loreEn = "Lurking arachnid dripping with neurotoxins. Immune to poison traps.",
                lorePl = "Drapieżny stawonóg nasączony neurotoksyną. Całkowicie odporny na trujące opary.",
                weaknessesEn = listOf("🔥 Fire (+50%)"),
                weaknessesPl = listOf("🔥 Ogień (+50%)"),
                resistancesEn = listOf("☠️ Poison (-50%)"),
                resistancesPl = listOf("☠️ Trucizna (-50%)"),
                tacticalCounterEn = "Flame Towers cleanse arachnids with wide fire arcs and burning damage.",
                tacticalCounterPl = "Wieża Ognia natychmiast przypala pajęcze pancerze."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.SLIME,
                nameEn = "Acidic Slime",
                namePl = "Żrący Szlam",
                emoji = "🟢",
                category = BestiaryCategory.SWARM,
                roleEn = "Tier 1 · Divider",
                rolePl = "Poziom 1 · Rozdzielacz",
                hp = 80, speed = 55, damage = 10,
                loreEn = "Amorphous acidic jelly. Splits into two mini-slimes upon fatal damage.",
                lorePl = "Galaretowata biomasa kwasowa. Po śmierci dzieli się na dwa mniejsze szlamy!",
                weaknessesEn = listOf("❄️ Ice (+30%)", "🔥 Fire (+30%)"),
                weaknessesPl = listOf("❄️ Lód (+30%)", "🔥 Ogień (+30%)"),
                resistancesEn = listOf("🏹 Physical (-30%)"),
                resistancesPl = listOf("🏹 Fizyczne (-30%)"),
                tacticalCounterEn = "Area-of-effect towers (Cannon, Fire) wipe out both split spawns simultaneously.",
                tacticalCounterPl = "Wieże obszarowe (Działo, Ogień) niszczą rozszczepione szlamy za jednym razem."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.HARPY,
                nameEn = "Screeching Harpy",
                namePl = "Skrzecząca Harpia",
                emoji = "🦅",
                category = BestiaryCategory.SWARM,
                roleEn = "Tier 2 · Aerial Disrupter",
                rolePl = "Poziom 2 · Latający Zakłócacz",
                hp = 130, speed = 125, damage = 18,
                loreEn = "Fierce winged raptor soaring above ground hazards. Emits disorienting screeches delaying reload mechanisms.",
                lorePl = "Zwinna bestia powietrzna wydająca ogłuszający pisk opóźniający ładowanie wież.",
                weaknessesEn = listOf("⚡ Tesla (+40%)", "🏹 Physical (+30%)"),
                weaknessesPl = listOf("⚡ Tesla (+40%)", "🏹 Fizyczne (+30%)"),
                resistancesEn = listOf("💥 Explosive (-60%)"),
                resistancesPl = listOf("💥 Wybuch (-60%)"),
                tacticalCounterEn = "Tesla Tower chain-lightning grounds harpies before they can screech.",
                tacticalCounterPl = "Błyskawice wieży Tesla uziemiają harpie jednym przeskokiem prądu."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.ORC,
                nameEn = "Orc Brute",
                namePl = "Ork Rębacz",
                emoji = "👹",
                category = BestiaryCategory.TANKS,
                roleEn = "Tier 2 · Heavy",
                rolePl = "Poziom 2 · Ciężka Piechota",
                hp = 180, speed = 65, damage = 25,
                loreEn = "Muscular green brutes with thick hides and brutal spiked clubs. Shrugs off light arrow fire.",
                lorePl = "Masywny ork o grubej skórze i brutalnej sile. Zwykłe strzały odbijają się od jego torsu.",
                weaknessesEn = listOf("🔥 Fire (+20%)", "🔮 Magic (+10%)"),
                weaknessesPl = listOf("🔥 Ogień (+20%)", "🔮 Magia (+10%)"),
                resistancesEn = listOf("🏹 Physical (-20%)"),
                resistancesPl = listOf("🏹 Fizyczne (-20%)"),
                tacticalCounterEn = "Magic Towers bypass thick skin; Flame burn DoT drains their beefy HP pool.",
                tacticalCounterPl = "Wieże Magiczne ignorują pancerz, a Ogień wypala ich potężną pulę życia."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.ARMORED_GOLEM,
                nameEn = "Runic Stone Golem",
                namePl = "Runiczny Kamienny Golem",
                emoji = "🗿",
                category = BestiaryCategory.TANKS,
                roleEn = "Tier 3 · Fortress",
                rolePl = "Poziom 3 · Żywa Forteca",
                hp = 500, speed = 40, damage = 40,
                loreEn = "Living granite boulder reinforced with runic armor plates. Heavily absorbs kinetic impact.",
                lorePl = "Ożywiony granit z runiczną płytą pancerną. Redukuje większość ataków kinetycznych.",
                weaknessesEn = listOf("💥 Explosive (+50%)", "🔮 Magic (+40%)", "🌑 Dark (+30%)"),
                weaknessesPl = listOf("💥 Wybuch (+50%)", "🔮 Magia (+40%)", "💀 Mrok (+30%)"),
                resistancesEn = listOf("🏹 Physical (-70%)"),
                resistancesPl = listOf("🏹 Fizyczne (-70%)"),
                tacticalCounterEn = "Artillery Cannons and Arcane Magic Towers shatter its tectonic plates.",
                tacticalCounterPl = "Artyleria Działowa oraz Wieże Magiczne skruszą pancerz z łatwością."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BERSERKER,
                nameEn = "Blood Berserker",
                namePl = "Krwawy Berserker",
                emoji = "🪓",
                category = BestiaryCategory.TANKS,
                roleEn = "Tier 3 · Enrager",
                rolePl = "Poziom 3 · Wściekły Niszczyciel",
                hp = 220, speed = 70, damage = 30,
                loreEn = "Raging berserker who moves and attacks faster as his life ebbs away. Ice towers cool his fury.",
                lorePl = "Fanatyk wojenny, którego szał i prędkość rosną w miarę odnoszonych obrażeń!",
                weaknessesEn = listOf("❄️ Ice (+30%)", "🌑 Dark (+20%)"),
                weaknessesPl = listOf("❄️ Lód (+30%)", "💀 Mrok (+20%)"),
                resistancesEn = listOf("🔥 Fire (-30%)", "🏹 Physical (-20%)"),
                resistancesPl = listOf("🔥 Ogień (-30%)", "🏹 Fizyczne (-20%)"),
                tacticalCounterEn = "Ice Towers freeze his blood frenzy and prevent high-speed base breaches.",
                tacticalCounterPl = "Wieża Lodowa studzi jego furię i nie pozwala mu zbliżyć się do bazy."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.COMMANDER,
                nameEn = "Legion Commander",
                namePl = "Dowódca Legionu",
                emoji = "🎖️",
                category = BestiaryCategory.TANKS,
                roleEn = "Tier 3 · Vanguard",
                rolePl = "Poziom 3 · Opancerzony Lider",
                hp = 320, speed = 60, damage = 25,
                loreEn = "Veteran tactician projecting a defense banner that shields surrounding allies.",
                lorePl = "Wojenny generał w pełnej zbroi płytowej roztaczający aurę ochronną na sojuszników.",
                weaknessesEn = listOf("⚡ Tesla (+40%)", "🔮 Magic (+30%)"),
                weaknessesPl = listOf("⚡ Tesla (+40%)", "🔮 Magia (+30%)"),
                resistancesEn = listOf("🏹 Physical (-40%)"),
                resistancesPl = listOf("🏹 Fizyczne (-40%)"),
                tacticalCounterEn = "Tesla lightning conducts through metal armor, shocking him and nearby escort.",
                tacticalCounterPl = "Wyładowania wieży Tesla przewodzą przez jego zbroję i rażą eskortę."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.MAGMA_CRAB,
                nameEn = "Magma Crab",
                namePl = "Krab Magmowy",
                emoji = "🦀",
                category = BestiaryCategory.TANKS,
                roleEn = "Tier 2 · Molten Tank",
                rolePl = "Poziom 2 · Wulkaniczny Czołg",
                hp = 280, speed = 45, damage = 22,
                loreEn = "Volcanic crustacean forged in caldera lava pits. Immune to fire, but thermal shock shatters its shell.",
                lorePl = "Skorupiak zrodzony w lawie. Ogień go nie rusza, lecz szok termiczny kruszy jego skorupę.",
                weaknessesEn = listOf("❄️ Ice (+80%)"),
                weaknessesPl = listOf("❄️ Lód (+80%)"),
                resistancesEn = listOf("🔥 Fire (Immune -90%)", "🏹 Physical (-40%)", "☠️ Poison (-70%)"),
                resistancesPl = listOf("🔥 Ogień (Niewrażliwy -90%)", "🏹 Fizyczne (-40%)", "☠️ Trucizna (-70%)"),
                tacticalCounterEn = "Ice Towers are vital: thermal shock deals a massive +80% bonus damage.",
                tacticalCounterPl = "Wieża Lodowa to klucz: szok termiczny zadaje aż +80% krytycznych obrażeń!"
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.TREANT,
                nameEn = "Ancient Treant",
                namePl = "Starożytny Drzewiec",
                emoji = "🌲",
                category = BestiaryCategory.TANKS,
                roleEn = "Tier 3 · Colossus",
                rolePl = "Poziom 3 · Kolos Regenerujący",
                hp = 480, speed = 35, damage = 32,
                loreEn = "Lumbering sentient oak giant. Heals rapidly over time unless set ablaze by Flame towers.",
                lorePl = "Ożywiony dąb pochłaniający uderzenia i regenerujący rany, dopóki nie stanie w płomieniach.",
                weaknessesEn = listOf("🔥 Fire (+80%)"),
                weaknessesPl = listOf("🔥 Ogień (+80%)"),
                resistancesEn = listOf("⚡ Tesla (-40%)", "💥 Explosive (-30%)", "🏹 Physical (-30%)", "☠️ Poison (-60%)"),
                resistancesPl = listOf("⚡ Tesla (-40%)", "💥 Wybuch (-30%)", "🏹 Fizyczne (-30%)", "☠️ Trucizna (-60%)"),
                tacticalCounterEn = "Ignite with Flame Towers to shut down HP regeneration and deal lethal burn.",
                tacticalCounterPl = "Podpal go Wieżą Ognia, aby natychmiast zatrzymać regenerację!"
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.DEMON,
                nameEn = "Hellfire Fiend",
                namePl = "Piekielny Bies",
                emoji = "😈",
                category = BestiaryCategory.MAGIC,
                roleEn = "Tier 3 · Elite",
                rolePl = "Poziom 3 · Skrzydlaty Niszczyciel",
                hp = 260, speed = 80, damage = 35,
                loreEn = "Infernal winged horror birthed from volcanic fissures. Extremely resistant to flame but chilled by frost.",
                lorePl = "Zrodzony z siarki demon ziejący ogniem. Piekielne żary spływają po nim bez śladu.",
                weaknessesEn = listOf("❄️ Ice (+50%)"),
                weaknessesPl = listOf("❄️ Lód (+50%)"),
                resistancesEn = listOf("🔥 Fire (-70%)", "🌑 Dark (-50%)"),
                resistancesPl = listOf("🔥 Ogień (-70%)", "💀 Mrok (-50%)"),
                tacticalCounterEn = "Ice Towers freeze his wings and deal severe thermal collapse damage.",
                tacticalCounterPl = "Wieża Lodowa drastycznie go osłabia i zamraża skrzydła w locie."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.DRAGON,
                nameEn = "Shadow Wyrm",
                namePl = "Mroczny Wiwern",
                emoji = "🐉",
                category = BestiaryCategory.MAGIC,
                roleEn = "Tier 3 · Apex Flying",
                rolePl = "Poziom 3 · Władca Niebios",
                hp = 450, speed = 75, damage = 50,
                loreEn = "Ancient serpentine winged terror that flies above ground hazards with massive health.",
                lorePl = "Olbrzymi skrzydlaty gad szybujący nad labiryntem obrony o olbrzymiej żywotności.",
                weaknessesEn = listOf("❄️ Ice (+40%)", "🌑 Dark (+30%)", "🔮 Magic (+20%)"),
                weaknessesPl = listOf("❄️ Lód (+40%)", "💀 Mrok (+30%)", "🔮 Magia (+20%)"),
                resistancesEn = listOf("🏹 Physical (-40%)", "🔥 Fire (-60%)"),
                resistancesPl = listOf("🏹 Fizyczne (-40%)", "🔥 Ogień (-60%)"),
                tacticalCounterEn = "Long-range Snipers equipped with frost ammunition and Arcane towers.",
                tacticalCounterPl = "Wieże Snajperskie z pociskami lodowymi i Wieże Magiczne."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.SHADOW,
                nameEn = "Shadow Wraith",
                namePl = "Mroczny Cień",
                emoji = "👻",
                category = BestiaryCategory.MAGIC,
                roleEn = "Tier 2 · Ethereal",
                rolePl = "Poziom 2 · Eteryczny Skrytobójca",
                hp = 120, speed = 95, damage = 20,
                loreEn = "Phase-shifting ghost. Physical weapons pass harmlessly through its misty form.",
                lorePl = "Widmo przenikające przez wymiary. Strzały i miecze przeszywają jedynie pustkę.",
                weaknessesEn = listOf("🔮 Magic (+50%)", "🔥 Fire (+40%)", "⚡ Tesla (+30%)"),
                weaknessesPl = listOf("🔮 Magia (+50%)", "🔥 Ogień (+40%)", "⚡ Tesla (+30%)"),
                resistancesEn = listOf("🏹 Physical (-70%)", "🌑 Dark (-80%)"),
                resistancesPl = listOf("🏹 Fizyczne (-70%)", "💀 Mrok (-80%)"),
                tacticalCounterEn = "Arcane Magic Towers and pure fire purge its spectral existence.",
                tacticalCounterPl = "Wieże Magiczne i Płomienie wypalają jego eteryczną strukturę."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.GHOST,
                nameEn = "Phantom Ghost",
                namePl = "Eteryczna Zjawa",
                emoji = "👻",
                category = BestiaryCategory.MAGIC,
                roleEn = "Tier 2 · Infiltrator",
                rolePl = "Poziom 2 · Niewidzialny Infiltrator",
                hp = 110, speed = 95, damage = 15,
                loreEn = "Ethereal spirit shifting between realms. Physical attacks phase straight through.",
                lorePl = "Dusza uwięziona między światami. Zwykła broń kinetyczna nie wyrządza jej prawie nic.",
                weaknessesEn = listOf("🔮 Magic (+60%)", "⚡ Tesla (+50%)"),
                weaknessesPl = listOf("🔮 Magia (+60%)", "⚡ Tesla (+50%)"),
                resistancesEn = listOf("🏹 Physical (-75%)", "💥 Explosive (-60%)", "🌑 Dark (-70%)"),
                resistancesPl = listOf("🏹 Fizyczne (-75%)", "💥 Wybuch (-60%)", "💀 Mrok (-70%)"),
                tacticalCounterEn = "Pure arcane magic bursts and Tesla electrical arcs bypass its intangibility.",
                tacticalCounterPl = "Czysta energia magiczna i łuki elektryczne Tesli są jedyną skuteczną bronią."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.NECROMANCER,
                nameEn = "Dark Necromancer",
                namePl = "Mroczny Nekromanta",
                emoji = "🧙‍♂️",
                category = BestiaryCategory.MAGIC,
                roleEn = "Tier 3 · Summoner",
                rolePl = "Poziom 3 · Przywoływacz Umarłych",
                hp = 240, speed = 60, damage = 20,
                loreEn = "Grim spellcaster chanting blasphemous rites to raise fallen skeletons mid-wave.",
                lorePl = "Czarownik władający nekromancją. W trakcie marszu wskrzesza poległych jako szkielety!",
                weaknessesEn = listOf("💥 Explosive (+40%)", "🏹 Physical (+20%)"),
                weaknessesPl = listOf("💥 Wybuch (+40%)", "🏹 Fizyczne (+20%)"),
                resistancesEn = listOf("🌑 Dark (-60%)", "🔮 Magic (-30%)"),
                resistancesPl = listOf("💀 Mrok (-60%)", "🔮 Magia (-30%)"),
                tacticalCounterEn = "Target him first with Long-range Snipers before he floods lanes with summons.",
                tacticalCounterPl = "Eliminuj go priorytetowo Wieżą Snajperską, zanim zapełni ścieżkę szkieletami."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.SHAPESHIFTER,
                nameEn = "Chaos Shapeshifter",
                namePl = "Zmienny Kształt",
                emoji = "🌀",
                category = BestiaryCategory.MAGIC,
                roleEn = "Tier 3 · Adaptive Shifter",
                rolePl = "Poziom 3 · Zmiennokształtny",
                hp = 200, speed = 85, damage = 22,
                loreEn = "Chaos entity that periodically morphs its elemental defense affinities on the fly.",
                lorePl = "Tajemnicza istota z Chaosu cyklicznie zmieniająca odporności na żywioły w trakcie marszu!",
                weaknessesEn = listOf("Dynamic Weakness (Rotates 4 elements)"),
                weaknessesPl = listOf("Zmienna podatność (Rotacja 4 żywiołów)"),
                resistancesEn = listOf("Dynamic Resistance (Rotates 4 elements)"),
                resistancesPl = listOf("Zmienna odporność (Rotacja 4 żywiołów)"),
                tacticalCounterEn = "Maintain a diversified defense mix of Physical, Arcane, Fire, and Ice.",
                tacticalCounterPl = "Zrównoważona obrona: kombinacja Łuczników, Magii, Ognia i Lodu."
            )
        )

        // --- 15 ACT CLIMAX BOSSES ---
        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.ORC_KING,
                nameEn = "Orc King",
                namePl = "Król Orków",
                emoji = "👹",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 1 Boss · Warlord",
                rolePl = "Boss Aktu 1 · Watażka",
                hp = 700, speed = 35, damage = 45,
                loreEn = "Supreme monarch of the orcish horde. Enrages at mid-health to execute a sudden sprint toward your gates.",
                lorePl = "Potężny monarcha hord orków. Gdy odniesie rany, szarżuje z taranem prosto na bramę bazy.",
                weaknessesEn = listOf("🔥 Fire (+25%)", "🔮 Magic (+20%)"),
                weaknessesPl = listOf("🔥 Ogień (+25%)", "🔮 Magia (+20%)"),
                resistancesEn = listOf("🏹 Physical (-30%)"),
                resistancesPl = listOf("🏹 Fizyczne (-30%)"),
                tacticalCounterEn = "Lay down tar traps and cold frost towers to blunt his deadly charge.",
                tacticalCounterPl = "Skup ogień spowalniający Wież Lodowych oraz pułapki smołowe, aby zatrzymać szarżę."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.LICH_LORD,
                nameEn = "Lich Lord",
                namePl = "Władca Liczów",
                emoji = "💀",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 2 Boss · Arch-Necromancer",
                rolePl = "Boss Aktu 2 · Arcymag Umarłych",
                hp = 600, speed = 45, damage = 35,
                loreEn = "Undead archmage raising endless bony thralls to overwhelm player choke points.",
                lorePl = "Nieumarły czarnoksiężnik wskrzeszający poległe legiony szkieletów.",
                weaknessesEn = listOf("💥 Explosive (+40%)", "🔮 Magic (+30%)"),
                weaknessesPl = listOf("💥 Wybuch (+40%)", "🔮 Magia (+30%)"),
                resistancesEn = listOf("🌑 Dark (-70%)", "🏹 Physical (-25%)"),
                resistancesPl = listOf("💀 Mrok (-70%)", "🏹 Fizyczne (-25%)"),
                tacticalCounterEn = "Cannon artillery cleanses minions in sweeping explosions so Snipers can focus the Lich.",
                tacticalCounterPl = "Działa artyleryjskie oczyszczają ścieżkę z minionów, odsłaniając Licza dla Snajperów."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.DEMON_PRINCE,
                nameEn = "Demon Prince",
                namePl = "Książę Piekieł",
                emoji = "😈",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 3 Boss · Hellfire Avatar",
                rolePl = "Boss Aktu 3 · Awatar Piekieł",
                hp = 900, speed = 40, damage = 55,
                loreEn = "Lord of the brimstone abyss radiating molten seismic waves that damage nearby fortifications.",
                lorePl = "Władca piekielnych otchłani. Emituje fale płynnej lawy uszkadzające sąsiednie wieże.",
                weaknessesEn = listOf("❄️ Ice (+60%)", "⚡ Tesla (+20%)"),
                weaknessesPl = listOf("❄️ Lód (+60%)", "⚡ Tesla (+20%)"),
                resistancesEn = listOf("🔥 Fire (Immune -80%)", "🌑 Dark (-60%)"),
                resistancesPl = listOf("🔥 Ogień (Niewrażliwy -80%)", "💀 Mrok (-60%)"),
                tacticalCounterEn = "Only the absolute zero frost of Ice Towers can extinguish his molten mantle.",
                tacticalCounterPl = "Mrożąca potęga Wież Lodowych ugasi jego lawową powłokę z potężnym bonusem obrażeń."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.DRAGON_QUEEN,
                nameEn = "Dragon Queen",
                namePl = "Królowa Smoków",
                emoji = "🐲",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 4 Boss · Apex Dragon",
                rolePl = "Boss Aktu 4 · Smocza Królowa",
                hp = 1100, speed = 30, damage = 60,
                loreEn = "Matriarch of the ancient skies hovering over barricades. Her roar spurs broods into overdrive.",
                lorePl = "Matka pradawnych gadów szybująca ponad fortyfikacjami. Jej ryk drastycznie przyspiesza sługi.",
                weaknessesEn = listOf("❄️ Ice (+50%)", "🌑 Dark (+35%)", "🔮 Magic (+25%)"),
                weaknessesPl = listOf("❄️ Lód (+50%)", "💀 Mrok (+35%)", "🔮 Magia (+25%)"),
                resistancesEn = listOf("🔥 Fire (-70%)", "🏹 Physical (-40%)"),
                resistancesPl = listOf("🔥 Ogień (-70%)", "🏹 Fizyczne (-40%)"),
                tacticalCounterEn = "Armor-piercing Snipers combined with cryo-slow fields to freeze her massive wings.",
                tacticalCounterPl = "Wieże Snajperskie z amunicją przeciwpancerną i mrożące wyładowania obronne."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.SHADOW_WRAITH,
                nameEn = "Shadow Wraith",
                namePl = "Arcywidmo Cienia",
                emoji = "👻",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 5 Boss · Void Walker",
                rolePl = "Boss Aktu 5 · Kroczący w Pustce",
                hp = 550, speed = 60, damage = 30,
                loreEn = "Creature composed of antimatter that suddenly blinks forward along the route.",
                lorePl = "Istota z antymaterii potrafiąca nagle przeskoczyć w czasie i przestrzeni wzdłuż ścieżki.",
                weaknessesEn = listOf("🔮 Magic (+60%)", "🔥 Fire (+45%)", "⚡ Tesla (+35%)"),
                weaknessesPl = listOf("🔮 Magia (+60%)", "🔥 Ogień (+45%)", "⚡ Tesla (+35%)"),
                resistancesEn = listOf("🏹 Physical (-80%)", "🌑 Dark (-90%)"),
                resistancesPl = listOf("🏹 Fizyczne (-80%)", "💀 Mrok (-90%)"),
                tacticalCounterEn = "Continuous-beam Arcane Towers and deep layered defense traps throughout the track.",
                tacticalCounterPl = "Ciągły promień Wież Magicznych oraz gęste pułapki na całej długości trasy."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.SLIME_KING,
                nameEn = "Slime King",
                namePl = "Król Szlamów",
                emoji = "👑",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 6 Boss · Amorphous Behemoth",
                rolePl = "Boss Aktu 6 · Monstrum Kwasowe",
                hp = 800, speed = 25, damage = 25,
                loreEn = "Colossal gelatinous mass. Upon fatal defeat, violently explodes into 12 acidic mini slimes!",
                lorePl = "Gigantyczna żelatynowa masa. Gdy zadasz jej ostateczny cios, eksploduje w 12 mniejszych szlamów!",
                weaknessesEn = listOf("❄️ Ice (+40%)", "🔥 Fire (+40%)"),
                weaknessesPl = listOf("❄️ Lód (+40%)", "🔥 Ogień (+40%)"),
                resistancesEn = listOf("🏹 Physical (-40%)"),
                resistancesPl = listOf("🏹 Fizyczne (-40%)"),
                tacticalCounterEn = "Station Flame and Cannon towers near his expected defeat zone to annihilate the cluster.",
                tacticalCounterPl = "Przygotuj Wieże Ognia i Działa przy punkcie rozpadu, aby zniszczyć odłamki w sekundę."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.VAMPIRE_LORD,
                nameEn = "Vampire Lord",
                namePl = "Władca Wampirów",
                emoji = "🧛",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 7 Boss · Blood Sovereign",
                rolePl = "Boss Aktu 7 · Suweren Krwi",
                hp = 750, speed = 50, damage = 40,
                loreEn = "Aristocrat of the crypt siphoning your gold treasury every second he stays alive.",
                lorePl = "Arystokrata nocy kradnący twoje złoto. Im dłużej żyje, tym mniej surowców na obronę.",
                weaknessesEn = listOf("⚡ Tesla (+45%)", "🔮 Magic (+35%)"),
                weaknessesPl = listOf("⚡ Tesla (+45%)", "🔮 Magia (+35%)"),
                resistancesEn = listOf("🏹 Physical (-30%)", "🌑 Dark (-50%)"),
                resistancesPl = listOf("🏹 Fizyczne (-30%)", "💀 Mrok (-50%)"),
                tacticalCounterEn = "Focus Hero abilities and Tesla chain lightning to eliminate him before bankruptcy.",
                tacticalCounterPl = "Skoncentruj ogień bohatera i wież Tesla, aby zneutralizować go błyskawicznie."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.SPIDER_QUEEN,
                nameEn = "Spider Queen",
                namePl = "Królowa Pająków",
                emoji = "🕷️",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 8 Boss · Broodmother",
                rolePl = "Boss Aktu 8 · Matka Arachnidów",
                hp = 650, speed = 45, damage = 35,
                loreEn = "Broodmother weaving webs across the path and deploying waves of toxic hatchlings.",
                lorePl = "Matka arachnidów pokrywająca ziemię pajęczyną i przyzywająca zastępy młodych.",
                weaknessesEn = listOf("🔥 Fire (+60%)"),
                weaknessesPl = listOf("🔥 Ogień (+60%)"),
                resistancesEn = listOf("☠️ Poison (Immune -90%)", "🏹 Physical (-20%)"),
                resistancesPl = listOf("☠️ Trucizna (Niewrażliwa -90%)", "🏹 Fizyczne (-20%)"),
                tacticalCounterEn = "Flame Towers cleanse webs and turn newly hatched spiders to ashes.",
                tacticalCounterPl = "Wieża Ognia spala pajęczyny i natychmiast obraca młode pająki w popiół."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.FROST_TITAN,
                nameEn = "Frost Titan",
                namePl = "Tytan Mrozu",
                emoji = "❄️",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 9 Boss · Glacial Behemoth",
                rolePl = "Boss Aktu 9 · Lodowy Olbrzym",
                hp = 1200, speed = 20, damage = 50,
                loreEn = "Glacial behemoth whose earthquakes jam nearby tower reload mechanisms.",
                lorePl = "Chodzący lodowiec emanujący zmarzliną. Jego tąpnięcia spowalniają mechanizmy wież.",
                weaknessesEn = listOf("🔥 Fire (+85%)"),
                weaknessesPl = listOf("🔥 Ogień (+85%)"),
                resistancesEn = listOf("❄️ Ice (Immune -90%)", "🏹 Physical (-30%)"),
                resistancesPl = listOf("❄️ Lód (Niewrażliwy -90%)", "🏹 Fizyczne (-30%)"),
                tacticalCounterEn = "Flame Towers are essential — melt his glacial armor with immense +85% fire bonus.",
                tacticalCounterPl = "Wieża Ognia to konieczność — roztapia lodowiec z premią aż +85% obrażeń!"
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.STONE_GOLEM,
                nameEn = "Stone Golem",
                namePl = "Megalityczny Golem",
                emoji = "🗿",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 10 Boss · Monolith",
                rolePl = "Boss Aktu 10 · Monolit",
                hp = 1400, speed = 15, damage = 65,
                loreEn = "Monolithic titan boasting extreme armor ratings. Shattered fragments advance on their own.",
                lorePl = "Tytan z pradawnego granitu. Odłamki po rozbiciu kontynuują marsz jako miniony.",
                weaknessesEn = listOf("💥 Explosive (+50%)", "🔮 Magic (+45%)"),
                weaknessesPl = listOf("💥 Wybuch (+50%)", "🔮 Magia (+45%)"),
                resistancesEn = listOf("🏹 Physical (-75%)", "⚡ Tesla (-20%)"),
                resistancesPl = listOf("🏹 Fizyczne (-75%)", "⚡ Tesla (-20%)"),
                tacticalCounterEn = "Concentrate Artillery Cannons and Arcane Magic to shatter the bedrock plating.",
                tacticalCounterPl = "Skup baterię Dział Artyleryjskich i Wież Magicznych, aby rozkruszyć monolit."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.STORM_LEVIATHAN,
                nameEn = "Storm Leviathan",
                namePl = "Lewiatan Burzy",
                emoji = "⚡",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 11 Boss · Tempest Bringer",
                rolePl = "Boss Aktu 11 · Władca Burzy",
                hp = 1300, speed = 28, damage = 65,
                loreEn = "Atmospheric titan crackling with megavolts. Radiates EMP shocks disabling towers for 3 seconds.",
                lorePl = "Potwór burzowy naładowany megawoltami. Emituje impulsy EMP paraliżujące wieże na 3 sekundy!",
                weaknessesEn = listOf("🏹 Physical (+30%)", "💥 Explosive (+25%)"),
                weaknessesPl = listOf("🏹 Fizyczne (+30%)", "💥 Wybuch (+25%)"),
                resistancesEn = listOf("⚡ Tesla (Immune -90%)", "🔮 Magic (-40%)"),
                resistancesPl = listOf("⚡ Tesla (Niewrażliwy -90%)", "🔮 Magia (-40%)"),
                tacticalCounterEn = "Kinetic snipers and heavy mechanical artillery unaffected by electric disruption.",
                tacticalCounterPl = "Tradycyjna artyleria fizyczna i snajperzy odporni na zakłócenia elektromagnetyczne."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.VOID_PHOENIX,
                nameEn = "Void Phoenix",
                namePl = "Feniks Pustki",
                emoji = "🪶",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 12 Boss · Immortal Void Bird",
                rolePl = "Boss Aktu 12 · Nieśmiertelny Ptak",
                hp = 850, speed = 45, damage = 50,
                loreEn = "Astral void bird. Upon destruction, reincarnates once in an incandescent fiery nova!",
                lorePl = "Kosmiczny ptak pustki. Po zniszczeniu odradza się w potężnym wybuchu z połową zdrowia!",
                weaknessesEn = listOf("❄️ Ice (+70%)", "🏹 Physical (+20%)"),
                weaknessesPl = listOf("❄️ Lód (+70%)", "🏹 Fizyczne (+20%)"),
                resistancesEn = listOf("🔥 Fire (Immune -90%)", "🌑 Dark (-60%)"),
                resistancesPl = listOf("🔥 Ogień (Niewrażliwy -90%)", "💀 Mrok (-60%)"),
                tacticalCounterEn = "Sustained sub-zero ice towers counter both life stages and quell the supernova.",
                tacticalCounterPl = "Ciągły ogień wież mroźnych niszczy oba stadia ptaka bez szans na ucieczkę."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.SPORE_OVERLORD,
                nameEn = "Spore Overlord",
                namePl = "Zarodnikowy Władca",
                emoji = "🍄",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 13 Boss · Toxic Hivemind",
                rolePl = "Boss Aktu 13 · Zarodnikowy Umysł",
                hp = 1350, speed = 18, damage = 60,
                loreEn = "Fungal behemoth releasing toxic dust clouds that heavily blind and reduce tower ranges.",
                lorePl = "Monstrualny grzyb wypuszczający chmury toksycznego pyłu drastycznie ograniczające zasięg wież.",
                weaknessesEn = listOf("🔥 Fire (+85%)"),
                weaknessesPl = listOf("🔥 Ogień (+85%)"),
                resistancesEn = listOf("☠️ Poison (Immune -95%)", "⚡ Tesla (-40%)", "🏹 Physical (-30%)"),
                resistancesPl = listOf("☠️ Trucizna (Niewrażliwy -95%)", "⚡ Tesla (-40%)", "🏹 Fizyczne (-30%)"),
                tacticalCounterEn = "Ignite the fungal haze using high-tier Flame Towers for catastrophic chain burns.",
                tacticalCounterPl = "Podpal chmurę zarodników Wieżami Płomieni, wywołując kaskadowe wybuchy ogniowe."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.CHRONO_LICH,
                nameEn = "Chrono Lich",
                namePl = "Licz Czasu",
                emoji = "⏳",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 14 Boss · Time Weaver",
                rolePl = "Boss Aktu 14 · Tkacz Czasu",
                hp = 950, speed = 35, damage = 45,
                loreEn = "Temporal anomaly that can warp time, instantly restoring health and rewinding movement.",
                lorePl = "Manipulator czasu. Odwraca bieg wydarzeń, cofając uszkodzenia i pozycje wrogów.",
                weaknessesEn = listOf("🔮 Magic (+50%)", "⚡ Tesla (+40%)"),
                weaknessesPl = listOf("🔮 Magia (+50%)", "⚡ Tesla (+40%)"),
                resistancesEn = listOf("🏹 Physical (-60%)", "🌑 Dark (-70%)"),
                resistancesPl = listOf("🏹 Fizyczne (-60%)", "💀 Mrok (-70%)"),
                tacticalCounterEn = "Tesla lightning and pure arcane bursts to disrupt temporal flux before rewind triggers.",
                tacticalCounterPl = "Wieża Tesla i potężne uderzenia czystej magii, aby przerwać manipulację czasem."
            )
        )

        allEntries.add(
            BestiaryEntry(
                type = EnemyType.BOSS,
                bossType = BossType.IRON_DREADNOUGHT,
                nameEn = "Iron Dreadnought",
                namePl = "Żelazny Drednot",
                emoji = "🤖",
                category = BestiaryCategory.BOSSES,
                roleEn = "Act 15 Final Boss · Siege Engine",
                rolePl = "Boss Aktu 15 · Machina Oblężnicza",
                hp = 1650, speed = 16, damage = 70,
                loreEn = "Mechanical behemoth armed with cruise missiles and heavy plating. The ultimate siege machine.",
                lorePl = "Mechaniczny moloch zbrojny w pociski rakietowe i pancerz płytowy. Najgroźniejsza machina zniszczenia.",
                weaknessesEn = listOf("💥 Explosive (+40%)", "⚡ Tesla (+40%)", "🔮 Magic (+30%)"),
                weaknessesPl = listOf("💥 Wybuch (+40%)", "⚡ Tesla (+40%)", "🔮 Magia (+30%)"),
                resistancesEn = listOf("🏹 Physical (-80%)", "🔥 Fire (-30%)"),
                resistancesPl = listOf("🏹 Fizyczne (-80%)", "🔥 Ogień (-30%)"),
                tacticalCounterEn = "Heavy explosive artillery, Tesla circuit overloads, and reinforced road barricades.",
                tacticalCounterPl = "Artyleria przeciwpancerna, przeciążenie obwodów wieżami Tesla i blokady drogowe."
            )
        )
    }
}
