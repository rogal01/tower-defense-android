package com.example.myapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityCampaignBinding
import com.example.myapp.game.CampaignData
import com.example.myapp.game.CampaignLevel
import com.example.myapp.game.MissionType
import com.example.myapp.game.SfxType

class CampaignActivity : ImmersiveActivity() {

    data class CampaignAct(
        val actNumber: Int,
        val startLevel: Int,
        val endLevel: Int,
        val titleEn: String,
        val titlePl: String,
        val emoji: String
    ) {
        fun title(isPl: Boolean): String = if (isPl) titlePl else titleEn
    }

    private val campaignActs = listOf(
        CampaignAct(1, 1, 10, "Act I: The Outskirts", "Akt I: Obrzeża Królestwa", "🌲"),
        CampaignAct(2, 11, 20, "Act II: Molten Canyons", "Akt II: Stopione Wąwozy", "🌋"),
        CampaignAct(3, 21, 30, "Act III: Frozen Necropolis", "Akt III: Zmarznięta Nekropolia", "❄️"),
        CampaignAct(4, 31, 40, "Act IV: Corrupted Frontier", "Akt IV: Spaczone Pogranicze", "☣️"),
        CampaignAct(5, 41, 50, "Act V: Infernal Bastion", "Akt V: Piekielny Bastion", "🔥"),
        CampaignAct(6, 51, 65, "Act VI: The Void Rifts", "Akt VI: Szczeliny Pustki", "🌌"),
        CampaignAct(7, 66, 80, "Act VII: Apex Citadel", "Akt VII: Cytadela Szczytu", "👑")
    )

    private lateinit var binding: ActivityCampaignBinding
    private var selectedAct: Int = 0 // 0 = All, 1..7 for Acts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCampaignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        GameStrings.init(this)

        binding.textCampaignTitle.text = if (GameStrings.isPl) "📜 KAMPANIA" else "📜 CAMPAIGN"
        binding.btnBack.setOnClickListener { finish() }
        buildActTabs()
    }

    override fun onResume() {
        super.onResume()
        buildActTabs()
        buildLevelList()
    }

    private fun buildActTabs() {
        val container = binding.actTabContainer
        container.removeAllViews()
        val isPl = GameStrings.isPl

        val tabsData = mutableListOf(
            Pair(0, if (isPl) "🌐 Wszystkie" else "🌐 All")
        )
        for (act in campaignActs) {
            tabsData.add(Pair(act.actNumber, "${act.emoji} ${if (isPl) "Akt" else "Act"} ${act.actNumber}"))
        }

        for ((actNum, label) in tabsData) {
            val tabBtn = Button(this).apply {
                text = label
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTypeface(typeface, Typeface.BOLD)
                isAllCaps = false
                setPadding(dp(12), dp(6), dp(12), dp(6))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(38)
                ).apply {
                    if (actNum > 0) marginStart = dp(6)
                }
                val isSelected = selectedAct == actNum
                setBackgroundResource(if (isSelected) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
                setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#B0BEC5"))

                setOnClickListener {
                    if (selectedAct != actNum) {
                        selectedAct = actNum
                        SoundManager.play(SfxType.UI_CLICK)
                        buildActTabs()
                        buildLevelList()
                    }
                }
            }
            container.addView(tabBtn)
        }
    }

    private fun buildLevelList() {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val container = binding.levelContainer
        container.removeAllViews()

        var completed = 0
        for (lvl in CampaignData.levels) {
            if (prefs.getBoolean("campaign_${lvl.id}", false)) completed++
        }

        val isPl = GameStrings.isPl
        val levelsToDisplay = if (selectedAct == 0) {
            CampaignData.levels
        } else {
            val targetAct = campaignActs.find { it.actNumber == selectedAct }
            if (targetAct != null) {
                CampaignData.levels.filter { it.id in targetAct.startLevel..targetAct.endLevel }
            } else {
                CampaignData.levels
            }
        }

        for (level in levelsToDisplay) {
            val isCompleted = prefs.getBoolean("campaign_${level.id}", false)
            val isHeroicCompleted = prefs.getBoolean("campaign_${level.id}_heroic", false)
            val prevCompleted = level.id == 1 || prefs.getBoolean("campaign_${level.id - 1}", false)
            val isUnlocked = prevCompleted

            // Act Section Header
            val act = campaignActs.find { it.startLevel == level.id }
            if (act != null) {
                val actLevels = CampaignData.levels.filter { it.id in act.startLevel..act.endLevel }
                val actStars = actLevels.sumOf { prefs.getInt("campaign_${it.id}_stars", 0) }
                val actMaxStars = actLevels.size * 3

                val actHeader = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    background = getDrawable(R.drawable.bg_parchment_banner)
                    setPadding(dp(16), dp(14), dp(16), dp(12))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = if (act.actNumber > 1 && selectedAct == 0) dp(20) else dp(6)
                        bottomMargin = dp(10)
                    }
                }

                val headerRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val actTitle = TextView(this).apply {
                    text = "${act.emoji} ${act.title(isPl)}"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    setTextColor(Color.parseColor("#FFD54F"))
                    setTypeface(typeface, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val actStarProgress = TextView(this).apply {
                    text = "⭐ $actStars / $actMaxStars"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(Color.parseColor("#FFE082"))
                    setTypeface(typeface, Typeface.BOLD)
                    background = getDrawable(R.drawable.bg_hud_chip)
                    setPadding(dp(8), dp(3), dp(8), dp(3))
                }

                headerRow.addView(actTitle)
                headerRow.addView(actStarProgress)
                actHeader.addView(headerRow)

                val loreText = TextView(this).apply {
                    text = GameStrings.actLore(act.actNumber)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f)
                    setTextColor(Color.parseColor("#D7CCC8"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(6)
                        bottomMargin = dp(6)
                    }
                }
                actHeader.addView(loreText)

                val perkBadge = TextView(this).apply {
                    text = "⚡ " + GameStrings.actRegionalPerk(act.actNumber)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(Color.parseColor("#00E5FF"))
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(dp(10), dp(4), dp(10), dp(4))
                    background = getDrawable(R.drawable.bg_hud_chip)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                actHeader.addView(perkBadge)

                container.addView(actHeader)
            }

            val isClimaxBoss = level.id in listOf(10, 20, 30, 40, 50, 65, 80)
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                background = getDrawable(
                    when {
                        !isUnlocked -> R.drawable.bg_card_glass
                        isClimaxBoss && isCompleted -> R.drawable.bg_card_climax_boss_completed
                        isClimaxBoss -> R.drawable.bg_card_climax_boss
                        isCompleted -> R.drawable.bg_card_menu_emerald
                        else -> R.drawable.bg_card_menu_action
                    }
                )
                setPadding(dp(16), if (isClimaxBoss) dp(16) else dp(14), dp(16), if (isClimaxBoss) dp(16) else dp(14))
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = if (isClimaxBoss) dp(12) else dp(8) }
                if (!isUnlocked) alpha = 0.4f
                isClickable = isUnlocked
                isFocusable = isUnlocked
                if (isUnlocked) {
                    setOnClickListener {
                        SoundManager.play(SfxType.UI_CLICK)
                        showLevelBriefingDialog(level, isCompleted)
                    }
                }
            }

            val emoji = TextView(this).apply {
                text = if (isUnlocked) {
                    if (isClimaxBoss) "👑\n${level.emoji}" else level.emoji
                } else "\uD83D\uDD12"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isClimaxBoss) 24f else 32f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(48), LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val textCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(12)
                }
            }

            val title = TextView(this).apply {
                val heroicBadge = if (isHeroicCompleted) " 💀" else ""
                text = "${level.id}. ${level.title}$heroicBadge"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(
                    when {
                        isHeroicCompleted -> Color.parseColor("#FF9800")
                        isCompleted -> Color.parseColor("#FFD700")
                        isUnlocked -> Color.WHITE
                        else -> Color.parseColor("#666666")
                    }
                )
                setTypeface(typeface, Typeface.BOLD)
            }

            textCol.addView(title)

            if (isClimaxBoss) {
                val bossName = when (level.id) {
                    10 -> "Grommash Ironhide"
                    20 -> "Spore Overlord Malgor"
                    30 -> "Ignis the Fire Drake"
                    40 -> "Ymir Frost Titan"
                    50 -> "Chrono-Lich Zalfir"
                    65 -> "Void Phoenix Xul"
                    80 -> "Iron Colossus Goliath"
                    else -> "Boss"
                }
                val bossBadge = TextView(this).apply {
                    text = if (isPl) "👑 BOSS ROZDZIAŁU: $bossName" else "👑 ACT CLIMAX BOSS: $bossName"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
                    setTextColor(if (isCompleted) Color.parseColor("#FFD700") else Color.parseColor("#FF5252"))
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(dp(8), dp(3), dp(8), dp(3))
                    background = getDrawable(if (isCompleted) R.drawable.bg_hud_chip_diamond else R.drawable.bg_hud_chip)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(2)
                        bottomMargin = dp(3)
                    }
                }
                textCol.addView(bossBadge)
            }

            if (level.missionType != MissionType.STANDARD) {
                val badge = TextView(this).apply {
                    text = "${level.missionType.badgeEmoji} ${level.missionType.displayName(isPl).uppercase()}"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTextColor(Color.parseColor(level.missionType.badgeColorHex))
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(dp(6), dp(2), dp(6), dp(2))
                    background = getDrawable(R.drawable.bg_hud_chip)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(2)
                        bottomMargin = dp(3)
                    }
                }
                textCol.addView(badge)
            }

            val desc = TextView(this).apply {
                text = level.description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(if (isUnlocked) Color.parseColor("#BBBBBB") else Color.parseColor("#555555"))
            }

            val meta = TextView(this).apply {
                val towerList = level.allowedTowers.joinToString(" ") { it.emoji }
                val mapInfo = "${level.mapType.emoji} ${level.mapType.displayName}"
                val starInfo = if (isCompleted) {
                    val s = prefs.getInt("campaign_${level.id}_stars", 0)
                    "  |  💎 ${level.diamondReward + (s - 1).coerceAtLeast(0) * 2}"
                } else "  |  💎 ${level.diamondReward}-${level.diamondReward + 4}"
                val heroicStatus = if (isHeroicCompleted) "  |  💀 HEROIC ✓" else ""
                val obj2Text = level.objective2.description(GameStrings.isPl)
                val obj3Text = level.objective3.description(GameStrings.isPl)
                text = "${GameStrings.campaignSurvive(level.targetWave)}  |  $mapInfo  |  $towerList$starInfo$heroicStatus\n⭐⭐ $obj2Text  •  ⭐⭐⭐ $obj3Text"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(Color.parseColor("#888888"))
                gravity = Gravity.START
            }

            textCol.addView(desc)
            textCol.addView(meta)

            val status = TextView(this).apply {
                val stars = prefs.getInt("campaign_${level.id}_stars", 0)
                text = when {
                    isHeroicCompleted && stars >= 3 -> "⭐⭐⭐\n💀"
                    isCompleted && stars >= 3 -> "⭐⭐⭐"
                    isCompleted && stars >= 2 -> "⭐⭐"
                    isCompleted -> "⭐"
                    isUnlocked -> "▶"
                    else -> ""
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (stars >= 2) 16f else 22f)
                gravity = Gravity.CENTER
            }

            card.addView(emoji)
            card.addView(textCol)
            card.addView(status)
            container.addView(card)
        }

        val totalStars = CampaignData.levels.sumOf { prefs.getInt("campaign_${it.id}_stars", 0) }
        val maxStars = CampaignData.levels.size * 3
        val heroicCount = CampaignData.levels.count { prefs.getBoolean("campaign_${it.id}_heroic", false) }
        val heroicInfo = if (heroicCount > 0) " • " + GameStrings.heroicProgressFmt(heroicCount, CampaignData.levels.size) else ""
        binding.textProgress.text = "${GameStrings.campaignProgress(completed, CampaignData.levels.size, totalStars, maxStars)}$heroicInfo"
    }

    private fun showLevelBriefingDialog(level: CampaignLevel, isCompleted: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_campaign_briefing, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val titleView = dialogView.findViewById<TextView>(R.id.briefing_title)
        val statusView = dialogView.findViewById<TextView>(R.id.briefing_status)
        val closeBtn = dialogView.findViewById<TextView>(R.id.btn_close)
        val chipMap = dialogView.findViewById<TextView>(R.id.chip_map)
        val chipWaves = dialogView.findViewById<TextView>(R.id.chip_waves)
        val chipGold = dialogView.findViewById<TextView>(R.id.chip_gold)
        val mapPreview = dialogView.findViewById<com.example.myapp.game.MapPreviewView>(R.id.briefing_map_preview)
        val descView = dialogView.findViewById<TextView>(R.id.briefing_desc)
        val hintView = dialogView.findViewById<TextView>(R.id.briefing_hint)
        val towersView = dialogView.findViewById<TextView>(R.id.briefing_allowed_towers)
        val powersView = dialogView.findViewById<TextView>(R.id.briefing_powers_and_upgrades)
        val star1View = dialogView.findViewById<TextView>(R.id.goal_star_1)
        val star2View = dialogView.findViewById<TextView>(R.id.goal_star_2)
        val star3View = dialogView.findViewById<TextView>(R.id.goal_star_3)
        val rewardView = dialogView.findViewById<TextView>(R.id.briefing_reward_text)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnDeploy = dialogView.findViewById<Button>(R.id.btn_deploy)

        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val stars = prefs.getInt("campaign_${level.id}_stars", 0)
        val isHeroicCompleted = prefs.getBoolean("campaign_${level.id}_heroic", false)
        val isPl = GameStrings.isPl

        val switchHeroic = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switch_heroic)
        val heroicTitle = dialogView.findViewById<TextView>(R.id.heroic_title)
        val heroicDesc = dialogView.findViewById<TextView>(R.id.heroic_desc)

        if (heroicTitle != null && switchHeroic != null && heroicDesc != null) {
            heroicTitle.text = if (isHeroicCompleted) "💀 " + (if (isPl) "WYZWANIE HEROICZNE (UKOŃCZONE ✓)" else "HEROIC CHALLENGE (CLEARED ✓)") else GameStrings.heroicChallenge
            heroicTitle.setTextColor(if (isHeroicCompleted) Color.parseColor("#FFD700") else Color.parseColor("#FF9800"))

            if (isCompleted) {
                switchHeroic.isEnabled = true
                switchHeroic.isChecked = false
                heroicDesc.text = GameStrings.heroicModeDesc
                switchHeroic.setOnCheckedChangeListener { _, _ ->
                    SoundManager.play(SfxType.UI_CLICK)
                }
            } else {
                switchHeroic.isEnabled = false
                switchHeroic.isChecked = false
                heroicDesc.text = if (isPl) "🔒 Ukończ tę misję, aby odblokować Wyzwanie Heroiczne!" else "🔒 Complete this mission to unlock Heroic Challenge!"
            }
        }

        titleView.text = "${level.emoji} Mission ${level.id}: ${level.title}"
        val baseStatus = when {
            isHeroicCompleted && stars >= 3 -> "⭐⭐⭐ MASTERED • 💀 HEROIC"
            isCompleted && stars >= 3 -> "⭐⭐⭐ MASTERED"
            isCompleted && stars == 2 -> "⭐⭐ COMPLETED (2/3 Stars)"
            isCompleted -> "⭐ COMPLETED (1/3 Stars)"
            else -> if (isPl) "⚔️ GOTOWY DO WALKI" else "⚔️ READY FOR DEPLOYMENT"
        }
        val missionPrefix = if (level.missionType != MissionType.STANDARD) {
            "${level.missionType.badgeEmoji} ${level.missionType.displayName(isPl).uppercase()} • "
        } else ""
        statusView.text = "$missionPrefix$baseStatus"
        statusView.setTextColor(if (isHeroicCompleted) Color.parseColor("#FF9800") else if (isCompleted) Color.parseColor("#00E5FF") else Color.parseColor("#FFD54F"))

        chipMap.text = "${level.mapType.emoji} ${level.mapType.displayName}"
        chipWaves.text = "⚔️ ${level.targetWave} Waves"
        chipGold.text = "💰 ${level.startingGold}g"
        mapPreview?.setMapType(level.mapType)

        var fullDesc = level.description
        when (level.missionType) {
            MissionType.LONE_CHAMPION -> {
                fullDesc += if (isPl) "\n\n⚠️ Zasada Czempiona: Maksymalnie 4 wieże! Czempion ma +150% obrażeń i +30% prędkości."
                    else "\n\n⚠️ Champion Rule: Max 4 towers allowed! Hero has +150% damage and +30% speed."
            }
            MissionType.SUDDEN_DEATH -> {
                fullDesc += if (isPl) "\n\n⚠️ Nagła Śmierć: Baza ma tylko 1 HP! Żaden wróg nie może przejść!"
                    else "\n\n⚠️ Sudden Death: Base has only 1 HP! No enemy may pass!"
            }
            MissionType.BLITZ -> {
                fullDesc += if (isPl) "\n\n⚡ Szybki Szturm: Błyskawiczny czas między falami, +30% tempo wrogów, +20% złota."
                    else "\n\n⚡ Blitz Rush: Rapid wave delays, +30% enemy spawn rate, +20% gold reward."
            }
            MissionType.GOLD_RUSH -> {
                fullDesc += if (isPl) "\n\n💰 Gorączka Złota: Zdobywaj 2.5x więcej złota za każdego pokonanego potwora!"
                    else "\n\n💰 Gold Rush: Earn 2.5x gold for every defeated enemy!"
            }
            MissionType.BOSS_BOUNTY -> {
                fullDesc += if (isPl) "\n\n🎯 Polowanie na Bossa: Boss pojawia się w każdej fali! Pokonaj ich wszystkich!"
                    else "\n\n🎯 Boss Bounty: Boss arrives on every single wave! Slay them all!"
            }
            MissionType.STANDARD -> {}
        }
        descView.text = fullDesc
        if (level.hint.isNotEmpty()) {
            hintView.visibility = View.VISIBLE
            hintView.text = "💡 Tactical Intel: ${level.hint}"
        } else {
            hintView.visibility = View.GONE
        }

        val towerNames = level.allowedTowers.joinToString(", ") { "${it.emoji} ${it.displayName}" }
        towersView.text = "Allowed Towers: $towerNames"

        val powerText = if (level.allowedPowers.isNotEmpty()) {
            "⚡ Spells: " + level.allowedPowers.joinToString(" ") { it.emoji }
        } else {
            "🔒 Spells Locked"
        }
        val upgradeText = if (level.upgradesEnabled) "⬆ Upgrades Enabled" else "🔒 Upgrades Disabled"
        powersView.text = "$powerText  •  $upgradeText"

        star1View.text = "⭐ " + level.objective1.description(isPl)
        star2View.text = "⭐⭐ " + level.objective2.description(isPl)
        star3View.text = "⭐⭐⭐ " + level.objective3.description(isPl)
        if (stars >= 1) star1View.setTextColor(Color.parseColor("#FFD700"))
        if (stars >= 2) star2View.setTextColor(Color.parseColor("#FFD700"))
        if (stars >= 3) star3View.setTextColor(Color.parseColor("#FFD700"))

        val earnedText = if (isCompleted) "Claimed (Re-run for highscore)" else "💎 ${level.diamondReward}-${level.diamondReward + 4} Diamonds"
        rewardView.text = earnedText

        closeBtn.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDeploy.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
            val intent = Intent(this@CampaignActivity, MainActivity::class.java).apply {
                putExtra("difficulty", MainMenuActivity.DIFFICULTY_NORMAL)
                putExtra("campaign_level", level.id)
                putExtra("campaign_heroic", switchHeroic?.isChecked == true)
            }
            startActivity(intent)
        }

        dialog.show()
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
