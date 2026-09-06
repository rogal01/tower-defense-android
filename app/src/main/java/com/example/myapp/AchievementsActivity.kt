package com.example.myapp

import android.content.Context
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
import com.example.myapp.databinding.ActivityAchievementsBinding
import com.example.myapp.game.AndroidGamePreferences
import com.example.myapp.game.SfxType
import com.example.myapp.game.SkillTree

class AchievementsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityAchievementsBinding
    private lateinit var skillTree: SkillTree

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        GameStrings.init(this)

        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        skillTree = SkillTree(AndroidGamePreferences(prefs))

        binding.btnBack.setOnClickListener { finish() }
        binding.btnClaimAll.setOnClickListener { claimAllRewards() }

        loadAchievements()
    }

    private fun loadAchievements() {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val container = binding.achievementsContainer
        container.removeAllViews()

        binding.textDiamonds.text = GameStrings.skillDiamonds(skillTree.diamonds)

        val achievementDefs = GameStrings.achievements()
        var unlockedCount = 0
        var unclaimedCount = 0
        var unclaimedDiamonds = 0

        for (ach in achievementDefs) {
            val isUnlocked = prefs.getBoolean("ach_${ach.id}", false)
            val isClaimed = prefs.getBoolean("ach_claimed_${ach.id}", false)

            if (isUnlocked) {
                unlockedCount++
                if (!isClaimed) {
                    unclaimedCount++
                    unclaimedDiamonds += ach.diamondReward
                }
            }

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val bg = GradientDrawable().apply {
                    cornerRadius = dp(10).toFloat()
                    if (isUnlocked && !isClaimed) {
                        setColor(0xFF162A45.toInt())
                        setStroke(dp(2), 0xFF00E5FF.toInt()) // glowing cyan border for claimable
                    } else if (isUnlocked) {
                        setColor(0xFF0F2035.toInt())
                        setStroke(dp(1), 0xFF2E4057.toInt())
                    } else {
                        setColor(0xFF141824.toInt())
                        setStroke(dp(1), 0xFF1C2233.toInt())
                    }
                }
                background = bg
                setPadding(dp(14), dp(12), dp(14), dp(12))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(10) }
                layoutParams = lp
                if (!isUnlocked) alpha = 0.55f
            }

            // Left icon
            val emoji = TextView(this).apply {
                text = if (isUnlocked) ach.emoji else "\uD83D\uDD12"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
                gravity = Gravity.CENTER
                val lp = LinearLayout.LayoutParams(dp(44), LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = lp
            }

            // Center details
            val textCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = dp(10)
                    marginEnd = dp(10)
                }
                layoutParams = lp
            }

            val title = TextView(this).apply {
                text = ach.title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(if (isUnlocked) Color.parseColor("#FFD700") else Color.parseColor("#888888"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val desc = TextView(this).apply {
                text = ach.description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(if (isUnlocked) Color.parseColor("#B0BEC5") else Color.parseColor("#607D8B"))
            }

            val rewardBounty = TextView(this).apply {
                text = "+${ach.diamondReward} \uD83D\uDC8E"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(if (isUnlocked && !isClaimed) Color.parseColor("#00E5FF") else Color.parseColor("#78909C"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(2), 0, 0)
            }

            textCol.addView(title)
            textCol.addView(desc)
            textCol.addView(rewardBounty)

            // Right action / status
            val rightView: View = if (isUnlocked && !isClaimed) {
                Button(this).apply {
                    text = GameStrings.claimBtn(ach.diamondReward)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    background = getDrawable(R.drawable.bg_btn_green)
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(38)
                    )
                    layoutParams = lp
                    setPadding(dp(12), 0, dp(12), 0)
                    setOnClickListener {
                        claimSingleAchievement(ach)
                    }
                }
            } else if (isUnlocked) {
                TextView(this).apply {
                    text = GameStrings.claimedBtn
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(Color.parseColor("#4CAF50"))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    gravity = Gravity.CENTER
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams = lp
                }
            } else {
                TextView(this).apply {
                    text = "\uD83D\uDD12 +${ach.diamondReward}\uD83D\uDC8E"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(Color.parseColor("#546E7A"))
                    gravity = Gravity.CENTER
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams = lp
                }
            }

            card.addView(emoji)
            card.addView(textCol)
            card.addView(rightView)
            container.addView(card)
        }

        // Update progress text
        binding.textProgress.text = GameStrings.achievementProgress(unlockedCount, achievementDefs.size)

        // Update Claim All button
        if (unclaimedCount > 0) {
            binding.btnClaimAll.visibility = View.VISIBLE
            binding.btnClaimAll.text = "${GameStrings.claimAllBtn} (+$unclaimedDiamonds \uD83D\uDC8E)"
        } else {
            binding.btnClaimAll.visibility = View.GONE
        }
    }

    private fun claimSingleAchievement(ach: GameStrings.AchDef) {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("ach_claimed_${ach.id}", true).apply()
        skillTree.addDiamonds(ach.diamondReward)
        SoundManager.play(SfxType.DIAMOND_DROP)
        Toast.makeText(this, "+${ach.diamondReward} \uD83D\uDC8E!", Toast.LENGTH_SHORT).show()
        loadAchievements()
    }

    private fun claimAllRewards() {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        var totalDiamonds = 0
        var claimedCount = 0

        for (ach in GameStrings.achievements()) {
            val isUnlocked = prefs.getBoolean("ach_${ach.id}", false)
            val isClaimed = prefs.getBoolean("ach_claimed_${ach.id}", false)
            if (isUnlocked && !isClaimed) {
                editor.putBoolean("ach_claimed_${ach.id}", true)
                totalDiamonds += ach.diamondReward
                claimedCount++
            }
        }
        editor.apply()

        if (claimedCount > 0) {
            skillTree.addDiamonds(totalDiamonds)
            SoundManager.play(SfxType.DIAMOND_DROP)
            Toast.makeText(this, GameStrings.claimedAllToast(claimedCount, totalDiamonds), Toast.LENGTH_SHORT).show()
            loadAchievements()
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
