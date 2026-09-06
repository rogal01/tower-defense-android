package com.example.myapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.myapp.databinding.ActivityAchievementsBinding
import com.example.myapp.game.AndroidGamePreferences
import com.example.myapp.game.CampaignData
import com.example.myapp.game.RelicId
import com.example.myapp.game.SfxType
import com.example.myapp.game.SkillTree

class AchievementsActivity : ImmersiveActivity() {

    enum class JumpTarget(val label: String) {
        BATTLE("\u2694\uFE0F Battle"),
        CAMPAIGN("\uD83D\uDDFA\uFE0F Campaign"),
        SKILL_TREE("\uD83C\uDF33 Skill Tree")
    }

    data class AchProgress(
        val current: Int,
        val target: Int,
        val jumpTarget: JumpTarget
    )

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

    private fun computeProgress(
        achId: String,
        isUnlocked: Boolean,
        lifetimeKills: Int,
        lifetimeBosses: Int,
        lifetimeCombo: Int,
        lifetimeTowers: Int,
        lifetimeGold: Int,
        highWave: Int,
        endlessHighWave: Int,
        bossRushHighWave: Int,
        highScore: Int,
        diamonds: Int,
        completedCampaign: Int,
        threeStarCampaign: Int,
        relicsCount: Int
    ): AchProgress {
        if (isUnlocked) {
            val (target, jTarget) = getTargetAndJump(achId)
            return AchProgress(target, target, jTarget)
        }

        return when (achId) {
            "kills_50" -> AchProgress(lifetimeKills.coerceAtMost(50), 50, JumpTarget.BATTLE)
            "kills_200" -> AchProgress(lifetimeKills.coerceAtMost(200), 200, JumpTarget.BATTLE)
            "kills_500" -> AchProgress(lifetimeKills.coerceAtMost(500), 500, JumpTarget.BATTLE)
            "kills_1000" -> AchProgress(lifetimeKills.coerceAtMost(1000), 1000, JumpTarget.BATTLE)
            "wave_50" -> AchProgress(highWave.coerceAtMost(50), 50, JumpTarget.BATTLE)
            "wave_100" -> AchProgress(highWave.coerceAtMost(100), 100, JumpTarget.BATTLE)
            "endless_10" -> AchProgress(endlessHighWave.coerceAtMost(10), 10, JumpTarget.BATTLE)
            "endless_25" -> AchProgress(endlessHighWave.coerceAtMost(25), 25, JumpTarget.BATTLE)
            "endless_50" -> AchProgress(endlessHighWave.coerceAtMost(50), 50, JumpTarget.BATTLE)
            "boss_kill" -> AchProgress(lifetimeBosses.coerceAtMost(1), 1, JumpTarget.BATTLE)
            "5_bosses" -> AchProgress(lifetimeBosses.coerceAtMost(5), 5, JumpTarget.BATTLE)
            "10_bosses" -> AchProgress(lifetimeBosses.coerceAtMost(10), 10, JumpTarget.BATTLE)
            "boss_rush_5" -> AchProgress(bossRushHighWave.coerceAtMost(5), 5, JumpTarget.BATTLE)
            "boss_rush_10" -> AchProgress(bossRushHighWave.coerceAtMost(10), 10, JumpTarget.BATTLE)
            "combo_10" -> AchProgress(lifetimeCombo.coerceAtMost(10), 10, JumpTarget.BATTLE)
            "combo_20" -> AchProgress(lifetimeCombo.coerceAtMost(20), 20, JumpTarget.BATTLE)
            "combo_30" -> AchProgress(lifetimeCombo.coerceAtMost(30), 30, JumpTarget.BATTLE)
            "combo_50" -> AchProgress(lifetimeCombo.coerceAtMost(50), 50, JumpTarget.BATTLE)
            "5_towers" -> AchProgress(lifetimeTowers.coerceAtMost(5), 5, JumpTarget.BATTLE)
            "10_towers" -> AchProgress(lifetimeTowers.coerceAtMost(10), 10, JumpTarget.BATTLE)
            "all_tower_types" -> AchProgress(0, 10, JumpTarget.BATTLE)
            "rich" -> AchProgress(lifetimeGold.coerceAtMost(500), 500, JumpTarget.BATTLE)
            "rich_1000" -> AchProgress(lifetimeGold.coerceAtMost(1000), 1000, JumpTarget.BATTLE)
            "gold_hoarder" -> AchProgress(lifetimeGold.coerceAtMost(2000), 2000, JumpTarget.BATTLE)
            "score_1000" -> AchProgress(highScore.coerceAtMost(1000), 1000, JumpTarget.BATTLE)
            "score_5000" -> AchProgress(highScore.coerceAtMost(5000), 5000, JumpTarget.BATTLE)
            "score_10000" -> AchProgress(highScore.coerceAtMost(10000), 10000, JumpTarget.BATTLE)
            "diamond_10" -> AchProgress(diamonds.coerceAtMost(10), 10, JumpTarget.SKILL_TREE)
            "diamond_50" -> AchProgress(diamonds.coerceAtMost(50), 50, JumpTarget.SKILL_TREE)
            "relic_first" -> AchProgress(relicsCount.coerceAtMost(1), 1, JumpTarget.SKILL_TREE)
            "relic_3" -> AchProgress(relicsCount.coerceAtMost(3), 3, JumpTarget.SKILL_TREE)
            "campaign_5" -> AchProgress(completedCampaign.coerceAtMost(5), 5, JumpTarget.CAMPAIGN)
            "campaign_10" -> AchProgress(completedCampaign.coerceAtMost(10), 10, JumpTarget.CAMPAIGN)
            "campaign_all" -> AchProgress(completedCampaign.coerceAtMost(40), 40, JumpTarget.CAMPAIGN)
            "campaign_3star" -> AchProgress(threeStarCampaign.coerceAtMost(5), 5, JumpTarget.CAMPAIGN)
            "campaign_3star_10" -> AchProgress(threeStarCampaign.coerceAtMost(10), 10, JumpTarget.CAMPAIGN)
            "campaign_no_damage" -> AchProgress(0, 1, JumpTarget.CAMPAIGN)
            "upgrade_all", "prestige_first" -> AchProgress(0, 1, JumpTarget.SKILL_TREE)
            else -> AchProgress(0, 1, JumpTarget.BATTLE)
        }
    }

    private fun getTargetAndJump(achId: String): Pair<Int, JumpTarget> = when (achId) {
        "kills_50" -> 50 to JumpTarget.BATTLE
        "kills_200" -> 200 to JumpTarget.BATTLE
        "kills_500" -> 500 to JumpTarget.BATTLE
        "kills_1000" -> 1000 to JumpTarget.BATTLE
        "wave_50" -> 50 to JumpTarget.BATTLE
        "wave_100" -> 100 to JumpTarget.BATTLE
        "endless_10" -> 10 to JumpTarget.BATTLE
        "endless_25" -> 25 to JumpTarget.BATTLE
        "endless_50" -> 50 to JumpTarget.BATTLE
        "boss_kill" -> 1 to JumpTarget.BATTLE
        "5_bosses" -> 5 to JumpTarget.BATTLE
        "10_bosses" -> 10 to JumpTarget.BATTLE
        "boss_rush_5" -> 5 to JumpTarget.BATTLE
        "boss_rush_10" -> 10 to JumpTarget.BATTLE
        "combo_10" -> 10 to JumpTarget.BATTLE
        "combo_20" -> 20 to JumpTarget.BATTLE
        "combo_30" -> 30 to JumpTarget.BATTLE
        "combo_50" -> 50 to JumpTarget.BATTLE
        "5_towers" -> 5 to JumpTarget.BATTLE
        "10_towers" -> 10 to JumpTarget.BATTLE
        "all_tower_types" -> 10 to JumpTarget.BATTLE
        "rich" -> 500 to JumpTarget.BATTLE
        "rich_1000" -> 1000 to JumpTarget.BATTLE
        "gold_hoarder" -> 2000 to JumpTarget.BATTLE
        "score_1000" -> 1000 to JumpTarget.BATTLE
        "score_5000" -> 5000 to JumpTarget.BATTLE
        "score_10000" -> 10000 to JumpTarget.BATTLE
        "diamond_10" -> 10 to JumpTarget.SKILL_TREE
        "diamond_50" -> 50 to JumpTarget.SKILL_TREE
        "relic_first" -> 1 to JumpTarget.SKILL_TREE
        "relic_3" -> 3 to JumpTarget.SKILL_TREE
        "campaign_5" -> 5 to JumpTarget.CAMPAIGN
        "campaign_10" -> 10 to JumpTarget.CAMPAIGN
        "campaign_all" -> 40 to JumpTarget.CAMPAIGN
        "campaign_3star" -> 5 to JumpTarget.CAMPAIGN
        "campaign_3star_10" -> 10 to JumpTarget.CAMPAIGN
        "campaign_no_damage" -> 1 to JumpTarget.CAMPAIGN
        "upgrade_all", "prestige_first" -> 1 to JumpTarget.SKILL_TREE
        else -> 1 to JumpTarget.BATTLE
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

        // Pre-query lifetime values
        val lifetimeKills = prefs.getInt("lifetime_kills", 0)
        val lifetimeBosses = prefs.getInt("lifetime_bosses", 0)
        val lifetimeCombo = prefs.getInt("lifetime_best_combo", 0)
        val lifetimeTowers = prefs.getInt("lifetime_towers", 0)
        val lifetimeGold = prefs.getInt("lifetime_gold", 0)
        val highWave = prefs.getInt("highWave", 0)
        val endlessHighWave = prefs.getInt("endlessHighWave", 0)
        val bossRushHighWave = prefs.getInt("bossRushHighWave", 0)
        val highScore = prefs.getInt("highScore", 0)
        val diamonds = skillTree.diamonds
        val completedCampaign = CampaignData.levels.count { prefs.getInt("campaign_${it.id}_stars", 0) > 0 }
        val threeStarCampaign = CampaignData.levels.count { prefs.getInt("campaign_${it.id}_stars", 0) >= 3 }
        val relicsCount = RelicId.entries.count { prefs.getBoolean("relic_unlocked_${it.name}", false) }

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

            val progress = computeProgress(
                ach.id, isUnlocked,
                lifetimeKills, lifetimeBosses, lifetimeCombo, lifetimeTowers, lifetimeGold,
                highWave, endlessHighWave, bossRushHighWave, highScore, diamonds,
                completedCampaign, threeStarCampaign, relicsCount
            )

            val pct = if (progress.target > 0) ((progress.current.toFloat() / progress.target) * 100f).toInt().coerceIn(0, 100) else 0

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val bg = GradientDrawable().apply {
                    cornerRadius = dp(10).toFloat()
                    if (isUnlocked && !isClaimed) {
                        setColor(0xFF162A45.toInt())
                        setStroke(dp(2), 0xFF00E5FF.toInt())
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
                if (!isUnlocked && progress.current == 0) alpha = 0.65f
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
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColor(if (isUnlocked) Color.parseColor("#FFD700") else Color.parseColor("#E0E0E0"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val desc = TextView(this).apply {
                text = ach.description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(if (isUnlocked) Color.parseColor("#B0BEC5") else Color.parseColor("#78909C"))
            }

            // Numerical Progress Text
            val progressText = TextView(this).apply {
                text = if (isUnlocked) "\u2713 Completed (100%)" else "Progress: ${progress.current} / ${progress.target} ($pct%)"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(if (isUnlocked) Color.parseColor("#4CAF50") else Color.parseColor("#00E5FF"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(3), 0, dp(2))
            }

            // Mini Visual Progress Bar Track + Fill
            val progressBar = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(4)).apply {
                    topMargin = dp(2)
                    bottomMargin = dp(4)
                }
                layoutParams = lp
                weightSum = 100f
                background = GradientDrawable().apply {
                    cornerRadius = dp(2).toFloat()
                    setColor(0xFF1C2233.toInt())
                }

                val fill = View(this@AchievementsActivity).apply {
                    val fillLp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, pct.toFloat().coerceAtLeast(1f))
                    layoutParams = fillLp
                    background = GradientDrawable().apply {
                        cornerRadius = dp(2).toFloat()
                        setColor(if (isUnlocked) 0xFF4CAF50.toInt() else 0xFF00E5FF.toInt())
                    }
                }
                addView(fill)
            }

            val rewardBounty = TextView(this).apply {
                text = "+${ach.diamondReward} \uD83D\uDC8E"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(if (isUnlocked && !isClaimed) Color.parseColor("#00E5FF") else Color.parseColor("#78909C"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            textCol.addView(title)
            textCol.addView(desc)
            textCol.addView(progressText)
            textCol.addView(progressBar)
            textCol.addView(rewardBounty)

            // Right action / navigation button
            val rightView: View = if (isUnlocked && !isClaimed) {
                Button(this).apply {
                    text = GameStrings.claimBtn(ach.diamondReward)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    setTextColor(Color.WHITE)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    background = getDrawable(R.drawable.bg_btn_green)
                    minHeight = dp(44)
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(44))
                    layoutParams = lp
                    setPadding(dp(14), 0, dp(14), 0)
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
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    layoutParams = lp
                }
            } else {
                // "Go To" contextual navigation button
                Button(this).apply {
                    text = progress.jumpTarget.label
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTextColor(Color.parseColor("#80D8FF"))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    background = getDrawable(R.drawable.bg_hud_chip)
                    minHeight = dp(38)
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(38))
                    layoutParams = lp
                    setPadding(dp(10), 0, dp(10), 0)
                    setOnClickListener {
                        when (progress.jumpTarget) {
                            JumpTarget.CAMPAIGN -> startActivity(Intent(this@AchievementsActivity, CampaignActivity::class.java))
                            JumpTarget.SKILL_TREE -> startActivity(Intent(this@AchievementsActivity, SkillTreeActivity::class.java))
                            JumpTarget.BATTLE -> startActivity(Intent(this@AchievementsActivity, MainActivity::class.java).apply {
                                putExtra("difficulty", 1)
                            })
                        }
                    }
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
