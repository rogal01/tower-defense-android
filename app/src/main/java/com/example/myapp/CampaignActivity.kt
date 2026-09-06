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
import com.example.myapp.game.SfxType

class CampaignActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityCampaignBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCampaignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        GameStrings.init(this)

        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        buildLevelList()
    }

    private fun buildLevelList() {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val container = binding.levelContainer
        container.removeAllViews()

        var completed = 0
        for (level in CampaignData.levels) {
            val isCompleted = prefs.getBoolean("campaign_${level.id}", false)
            val isHeroicCompleted = prefs.getBoolean("campaign_${level.id}_heroic", false)
            if (isCompleted) completed++
            val prevCompleted = level.id == 1 || prefs.getBoolean("campaign_${level.id - 1}", false)
            val isUnlocked = prevCompleted

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                background = getDrawable(
                    when {
                        isCompleted -> R.drawable.bg_card_menu_emerald
                        isUnlocked -> R.drawable.bg_card_menu_action
                        else -> R.drawable.bg_card_glass
                    }
                )
                setPadding(dp(16), dp(14), dp(16), dp(14))
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
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
                text = if (isUnlocked) level.emoji else "\uD83D\uDD12"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
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

            textCol.addView(title)
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
        val btnEasy = dialogView.findViewById<Button>(R.id.btn_diff_easy)
        val btnNormal = dialogView.findViewById<Button>(R.id.btn_diff_normal)
        val btnHard = dialogView.findViewById<Button>(R.id.btn_diff_hard)
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
        statusView.text = when {
            isHeroicCompleted && stars >= 3 -> "⭐⭐⭐ MASTERED • 💀 HEROIC"
            isCompleted && stars >= 3 -> "⭐⭐⭐ MASTERED"
            isCompleted && stars == 2 -> "⭐⭐ COMPLETED (2/3 Stars)"
            isCompleted -> "⭐ COMPLETED (1/3 Stars)"
            else -> "⚔️ READY FOR DEPLOYMENT"
        }
        statusView.setTextColor(if (isHeroicCompleted) Color.parseColor("#FF9800") else if (isCompleted) Color.parseColor("#00E5FF") else Color.parseColor("#FFD54F"))

        chipMap.text = "${level.mapType.emoji} ${level.mapType.displayName}"
        chipWaves.text = "⚔️ ${level.targetWave} Waves"
        chipGold.text = "💰 ${level.startingGold}g"
        mapPreview?.setMapType(level.mapType)

        descView.text = level.description
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

        var selectedDifficulty = MainMenuActivity.DIFFICULTY_NORMAL

        fun updateDifficultyUI() {
            btnEasy.setBackgroundResource(if (selectedDifficulty == MainMenuActivity.DIFFICULTY_EASY) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            btnEasy.setTextColor(if (selectedDifficulty == MainMenuActivity.DIFFICULTY_EASY) Color.WHITE else Color.parseColor("#B0BEC5"))

            btnNormal.setBackgroundResource(if (selectedDifficulty == MainMenuActivity.DIFFICULTY_NORMAL) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            btnNormal.setTextColor(if (selectedDifficulty == MainMenuActivity.DIFFICULTY_NORMAL) Color.WHITE else Color.parseColor("#B0BEC5"))

            btnHard.setBackgroundResource(if (selectedDifficulty == MainMenuActivity.DIFFICULTY_HARD) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            btnHard.setTextColor(if (selectedDifficulty == MainMenuActivity.DIFFICULTY_HARD) Color.WHITE else Color.parseColor("#B0BEC5"))
        }

        btnEasy.setOnClickListener {
            selectedDifficulty = MainMenuActivity.DIFFICULTY_EASY
            updateDifficultyUI()
            SoundManager.play(SfxType.UI_CLICK)
        }
        btnNormal.setOnClickListener {
            selectedDifficulty = MainMenuActivity.DIFFICULTY_NORMAL
            updateDifficultyUI()
            SoundManager.play(SfxType.UI_CLICK)
        }
        btnHard.setOnClickListener {
            selectedDifficulty = MainMenuActivity.DIFFICULTY_HARD
            updateDifficultyUI()
            SoundManager.play(SfxType.UI_CLICK)
        }

        closeBtn.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnDeploy.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
            val intent = Intent(this@CampaignActivity, MainActivity::class.java).apply {
                putExtra("difficulty", selectedDifficulty)
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
