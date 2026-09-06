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
                text = "${level.id}. ${level.title}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(
                    when {
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
                text = "${GameStrings.campaignSurvive(level.targetWave)}  |  $mapInfo  |  $towerList$starInfo\n⭐⭐ ${level.star2Score}  ⭐⭐⭐ ${level.star3Score}"
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
                    isCompleted && stars >= 3 -> "⭐⭐⭐"
                    isCompleted && stars >= 2 -> "⭐⭐"
                    isCompleted -> "⭐"
                    isUnlocked -> "▶"
                    else -> ""
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (stars >= 2) 18f else 24f)
                gravity = Gravity.CENTER
            }

            card.addView(emoji)
            card.addView(textCol)
            card.addView(status)
            container.addView(card)
        }

        val totalStars = CampaignData.levels.sumOf { prefs.getInt("campaign_${it.id}_stars", 0) }
        val maxStars = CampaignData.levels.size * 3
        binding.textProgress.text = GameStrings.campaignProgress(completed, CampaignData.levels.size, totalStars, maxStars)
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

        titleView.text = "${level.emoji} Mission ${level.id}: ${level.title}"
        statusView.text = when {
            isCompleted && stars >= 3 -> "⭐⭐⭐ MASTERED"
            isCompleted && stars == 2 -> "⭐⭐ COMPLETED (2/3 Stars)"
            isCompleted -> "⭐ COMPLETED (1/3 Stars)"
            else -> "⚔️ READY FOR DEPLOYMENT"
        }
        statusView.setTextColor(if (isCompleted) Color.parseColor("#00E5FF") else Color.parseColor("#FFD54F"))

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

        star1View.text = "⭐ 1 Star: Clear Wave ${level.targetWave}"
        star2View.text = "⭐⭐ 2 Stars: Achieve Score ≥ ${level.star2Score}"
        star3View.text = "⭐⭐⭐ 3 Stars: Achieve Score ≥ ${level.star3Score}"

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
            }
            startActivity(intent)
        }

        dialog.show()
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
