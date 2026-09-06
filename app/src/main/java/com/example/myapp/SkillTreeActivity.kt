package com.example.myapp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.myapp.databinding.ActivitySkillTreeBinding
import com.example.myapp.game.AndroidGamePreferences
import com.example.myapp.game.DiamondBlessing
import com.example.myapp.game.RelicId
import com.example.myapp.game.SfxType
import com.example.myapp.game.Skill
import com.example.myapp.game.SkillTree

class SkillTreeActivity : ImmersiveActivity() {

    private lateinit var binding: ActivitySkillTreeBinding
    private lateinit var skillTree: SkillTree
    private var selectedTab: Int = 0 // 0 = Citadel, 1 = Alchemy & Runes, 2 = Relics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillTreeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val prefs = getSharedPreferences("tower_defense_prefs", MODE_PRIVATE)
        skillTree = SkillTree(AndroidGamePreferences(prefs))
        binding.btnBack.setOnClickListener { finish() }
        GameStrings.init(this)

        binding.btnTabSkills.text = GameStrings.tabCitadelPassives
        binding.btnTabAlchemy.text = GameStrings.tabElementalAlchemy
        binding.btnTabRelics.text = GameStrings.tabRelicVault

        binding.btnTabSkills.setOnClickListener {
            if (selectedTab != 0) {
                selectedTab = 0
                updateTabButtons()
                buildContent()
            }
        }

        binding.btnTabAlchemy.setOnClickListener {
            if (selectedTab != 1) {
                selectedTab = 1
                updateTabButtons()
                buildContent()
            }
        }

        binding.btnTabRelics.setOnClickListener {
            if (selectedTab != 2) {
                selectedTab = 2
                updateTabButtons()
                buildContent()
            }
        }

        updateTabButtons()
        buildContent()
    }

    private fun updateTabButtons() {
        val activeBg = getDrawable(R.drawable.bg_tab_active)
        val inactiveBg = getDrawable(R.drawable.bg_tab_inactive)
        val activeColor = Color.WHITE
        val inactiveColor = Color.parseColor("#90A4AE")

        binding.btnTabSkills.background = if (selectedTab == 0) activeBg else inactiveBg
        binding.btnTabSkills.setTextColor(if (selectedTab == 0) activeColor else inactiveColor)

        binding.btnTabAlchemy.background = if (selectedTab == 1) activeBg else inactiveBg
        binding.btnTabAlchemy.setTextColor(if (selectedTab == 1) activeColor else inactiveColor)

        binding.btnTabRelics.background = if (selectedTab == 2) activeBg else inactiveBg
        binding.btnTabRelics.setTextColor(if (selectedTab == 2) activeColor else inactiveColor)

        binding.textSubtitle.text = when (selectedTab) {
            0 -> GameStrings.citadelSubtitle
            1 -> GameStrings.alchemySubtitle
            else -> GameStrings.relicVaultSubtitle
        }
    }

    private fun buildContent() {
        binding.textDiamonds.text = GameStrings.skillDiamonds(skillTree.diamonds)
        binding.skillContainer.removeAllViews()

        when (selectedTab) {
            0 -> buildCitadelSkills()
            1 -> buildAlchemyAndBlessings()
            else -> buildRelicVault()
        }
    }

    private fun buildCitadelSkills() {
        // Prestige info
        if (skillTree.prestigeLevel > 0) {
            val prestigeInfo = TextView(this).apply {
                text = GameStrings.skillPrestigeLevel(skillTree.prestigeLevel, skillTree.prestigeLevel * 5)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(0xFFFFD700.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 4, 0, 14)
            }
            binding.skillContainer.addView(prestigeInfo)
        }

        // Page 1 header
        addSectionHeader(GameStrings.skillPage1)

        for (skill in skillTree.skills) {
            addSkillRow(skill)
        }

        // Prestige button
        val prestigeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 20, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val prestigeBtn = Button(this).apply {
            val cost = skillTree.prestigeCost()
            val canDo = skillTree.canPrestige()
            text = if (skillTree.allPage1Maxed()) GameStrings.skillPrestigeBtn(cost) else GameStrings.skillPrestigeLocked
            isEnabled = canDo
            alpha = if (canDo) 1f else 0.4f
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(Color.WHITE)
            background = getDrawable(if (canDo) R.drawable.bg_btn_primary else R.drawable.bg_tab_inactive)
            setOnClickListener {
                if (skillTree.prestige()) {
                    SoundManager.play(SfxType.ACHIEVEMENT)
                    val prefs = getSharedPreferences("tower_defense_prefs", MODE_PRIVATE)
                    prefs.edit().putBoolean("ach_prestige_first", true).apply()
                    Toast.makeText(this@SkillTreeActivity,
                        GameStrings.skillPrestigeDone(skillTree.prestigeLevel), Toast.LENGTH_LONG).show()
                    buildContent()
                }
            }
        }
        prestigeRow.addView(prestigeBtn)
        binding.skillContainer.addView(prestigeRow)

        // Page 2: Prestige skills
        if (skillTree.prestigeLevel >= 1) {
            addSectionHeader(GameStrings.skillPage2)
            for (skill in skillTree.prestigeSkills) {
                addSkillRow(skill)
            }
        } else {
            val lockText = TextView(this).apply {
                text = GameStrings.skillPage2Locked
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(0xFF78909C.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 14, 0, 8)
            }
            binding.skillContainer.addView(lockText)
        }

        // Citadel Fortifications
        addSectionHeader(GameStrings.combatSkillsHeader)
        for (skill in skillTree.combatSkills) {
            addSkillRow(skill)
        }
    }

    private fun buildAlchemyAndBlessings() {
        // Pre-Run Diamond Blessings section
        addSectionHeader(GameStrings.blessingSectionTitle)

        val desc = TextView(this).apply {
            text = GameStrings.blessingSectionDesc
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(Color.parseColor("#90A4AE"))
            setPadding(dp(12), 0, dp(12), dp(10))
        }
        binding.skillContainer.addView(desc)

        val currentBlessing = skillTree.getActiveBlessing()
        for (blessing in DiamondBlessing.entries) {
            addBlessingCard(blessing, currentBlessing == blessing)
        }

        // Elemental Alchemy section
        addSectionHeader(GameStrings.alchemySkillsHeader)
        for (skill in skillTree.alchemySkills) {
            addSkillRow(skill)
        }
    }

    private fun addBlessingCard(blessing: DiamondBlessing, isActive: Boolean) {
        val canAfford = skillTree.canAffordBlessing(blessing)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            val bg = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                if (isActive) {
                    setColor(0xFF0C2718.toInt())
                    setStroke(dp(2), 0xFF00E5FF.toInt())
                } else {
                    setColor(0xFF161F30.toInt())
                    setStroke(dp(1), 0xFF2A3B50.toInt())
                }
            }
            background = bg
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
            layoutParams = lp
        }

        val icon = TextView(this).apply {
            text = if (blessing.emoji.isNotBlank()) blessing.emoji else "🛡️"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dp(44), LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(10)
                marginEnd = dp(10)
            }
        }

        val titleText = TextView(this).apply {
            text = GameStrings.getLocalizedBlessingTitle(blessing)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(if (isActive) Color.parseColor("#00E5FF") else Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val descText = TextView(this).apply {
            text = GameStrings.getLocalizedBlessingDesc(blessing)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(Color.parseColor("#CFD8DC"))
        }

        infoLayout.addView(titleText)
        infoLayout.addView(descText)

        val rightView: View = if (isActive) {
            TextView(this).apply {
                text = GameStrings.blessingActiveBadge
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(Color.parseColor("#00E5FF"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(4), dp(8), dp(4))
            }
        } else {
            Button(this).apply {
                val costStr = if (blessing.cost > 0) "💎 ${blessing.cost}" else "FREE"
                text = "$costStr\n${GameStrings.blessingSelectBtn}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.WHITE)
                background = getDrawable(if (canAfford) R.drawable.bg_btn_primary else R.drawable.bg_tab_inactive)
                isEnabled = canAfford
                alpha = if (canAfford) 1f else 0.45f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(44)
                )
                setPadding(dp(12), 0, dp(12), 0)
                setOnClickListener {
                    if (skillTree.purchaseBlessing(blessing)) {
                        SoundManager.play(SfxType.REWARD_CHEST)
                        Toast.makeText(this@SkillTreeActivity,
                            "${blessing.title} ${if (GameStrings.isPl) "aktywowano na następny bieg!" else "activated for next run!"}", Toast.LENGTH_SHORT).show()
                        buildContent()
                    }
                }
            }
        }

        card.addView(icon)
        card.addView(infoLayout)
        card.addView(rightView)
        binding.skillContainer.addView(card)
    }

    private fun buildRelicVault() {
        val vaultHeader = TextView(this).apply {
            val unlockedCount = skillTree.unlockedRelicsCount()
            text = "🏺 ${GameStrings.tabRelicVault} ($unlockedCount / ${RelicId.entries.size})"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(0xFFFFD700.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(12, 12, 12, 14)
        }
        binding.skillContainer.addView(vaultHeader)

        for (relic in RelicId.entries) {
            addRelicRow(relic)
        }
    }

    private fun addRelicRow(relic: RelicId) {
        val isUnlocked = skillTree.isRelicUnlocked(relic)
        val canUnlock = skillTree.canUnlockRelic(relic)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            val bg = GradientDrawable().apply {
                cornerRadius = dp(10).toFloat()
                if (isUnlocked) {
                    setColor(0xFF0C2718.toInt()) // Deep emerald background for unlocked relics
                    setStroke(dp(2), 0xFF4CAF50.toInt())
                } else {
                    setColor(0xFF161F30.toInt())
                    setStroke(dp(1), 0xFF2A3B50.toInt())
                }
            }
            background = bg
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }
            layoutParams = lp
        }

        // Left Icon
        val icon = TextView(this).apply {
            text = relic.emoji
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            gravity = Gravity.CENTER
            val lp = LinearLayout.LayoutParams(dp(44), LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams = lp
        }

        // Center Details
        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(10)
                marginEnd = dp(10)
            }
            layoutParams = lp
        }

        val titleText = TextView(this).apply {
            text = GameStrings.getLocalizedRelicTitle(relic)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(if (isUnlocked) Color.parseColor("#4CAF50") else Color.parseColor("#FFD700"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val descText = TextView(this).apply {
            text = GameStrings.getLocalizedRelicDesc(relic)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(if (isUnlocked) Color.parseColor("#CFD8DC") else Color.parseColor("#90A4AE"))
        }

        infoLayout.addView(titleText)
        infoLayout.addView(descText)

        // Right Action / Status
        val rightView: View = if (isUnlocked) {
            TextView(this).apply {
                text = GameStrings.relicActive
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(Color.parseColor("#4CAF50"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(4), dp(8), dp(4))
            }
        } else {
            Button(this).apply {
                text = GameStrings.relicUnlockBtn(relic.diamondCost)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(Color.WHITE)
                background = getDrawable(if (canUnlock) R.drawable.bg_btn_primary else R.drawable.bg_tab_inactive)
                isEnabled = canUnlock
                alpha = if (canUnlock) 1f else 0.45f
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(38)
                )
                layoutParams = lp
                setPadding(dp(12), 0, dp(12), 0)
                setOnClickListener {
                    if (skillTree.unlockRelic(relic)) {
                        SoundManager.play(SfxType.ACHIEVEMENT)
                        Toast.makeText(this@SkillTreeActivity,
                            "🏺 ${GameStrings.getLocalizedRelicTitle(relic)} ${if (GameStrings.isPl) "odblokowany!" else "unlocked!"}", Toast.LENGTH_SHORT).show()
                        buildContent()
                    }
                }
            }
        }

        row.addView(icon)
        row.addView(infoLayout)
        row.addView(rightView)
        binding.skillContainer.addView(row)
    }

    private fun addSectionHeader(title: String) {
        val header = TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(0xFF81D4FA.toInt())
            gravity = Gravity.START
            setPadding(12, 16, 0, 8)
        }
        binding.skillContainer.addView(header)
    }

    private fun addSkillRow(skill: com.example.myapp.game.Skill) {
        val lvl = skillTree.getLevel(skill.id)
        val maxed = lvl >= skill.maxLevel
        val cost = if (maxed) 0 else skill.cost(lvl)
        val canBuy = skillTree.canUpgrade(skill.id)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            val bg = GradientDrawable().apply {
                cornerRadius = dp(8).toFloat()
                if (maxed) {
                    setColor(0xFF0F2618.toInt())
                    setStroke(dp(1), 0xFF2E7D32.toInt())
                } else {
                    setColor(0xFF141F30.toInt())
                    setStroke(dp(1), 0xFF223247.toInt())
                }
            }
            background = bg
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
            layoutParams = lp
        }

        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(10)
            }
            layoutParams = lp
        }

        val titleText = TextView(this).apply {
            val localizedName = GameStrings.getLocalizedSkillName(skill.id)
            text = "${skill.emoji} $localizedName"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(if (maxed) 0xFF66BB6A.toInt() else Color.WHITE)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val descText = TextView(this).apply {
            text = GameStrings.getLocalizedSkillDesc(skill.id)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(0xFF90A4AE.toInt())
        }

        val levelText = TextView(this).apply {
            val pips = buildString {
                repeat(lvl) { append("\u2B50") }
                repeat(skill.maxLevel - lvl) { append("\u2B1C") }
            }
            text = "Lv $lvl/${skill.maxLevel}  $pips"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(0xFFFFD700.toInt())
            setPadding(0, dp(2), 0, 0)
        }

        infoLayout.addView(titleText)
        infoLayout.addView(descText)
        infoLayout.addView(levelText)

        val btn = Button(this).apply {
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(38)
            )
            layoutParams = lp
            if (maxed) {
                text = GameStrings.skillMax
                isEnabled = false
                alpha = 0.5f
                setTextColor(Color.parseColor("#81C784"))
                background = getDrawable(R.drawable.bg_tab_inactive)
            } else {
                text = "\uD83D\uDC8E $cost"
                isEnabled = canBuy
                alpha = if (canBuy) 1f else 0.45f
                setTextColor(Color.WHITE)
                background = getDrawable(if (canBuy) R.drawable.bg_btn_primary else R.drawable.bg_tab_inactive)
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(dp(12), 0, dp(12), 0)
            setOnClickListener {
                if (skillTree.upgrade(skill.id)) {
                    SoundManager.play(SfxType.PLAYER_UPGRADE)
                    val locName = GameStrings.getLocalizedSkillName(skill.id)
                    Toast.makeText(this@SkillTreeActivity,
                        "${skill.emoji} $locName ${if (GameStrings.isPl) "ulepszono!" else "upgraded!"}", Toast.LENGTH_SHORT).show()
                    buildContent()
                } else {
                    Toast.makeText(this@SkillTreeActivity,
                        if (GameStrings.isPl) "Za mało diamentów!" else "Not enough diamonds!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        row.addView(infoLayout)
        row.addView(btn)
        binding.skillContainer.addView(row)
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
