package com.example.myapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityAchievementsBinding

class AchievementsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    // Mirror the achievement definitions from GameEngine
    private data class AchievementDef(
        val id: String, val title: String, val description: String, val emoji: String
    )

    private val achievementDefs = listOf(
        AchievementDef("first_kill", "First Blood", "Kill your first enemy", "\uD83D\uDDE1\uFE0F"),
        AchievementDef("wave_5", "Survivor", "Reach wave 5", "\uD83D\uDEE1\uFE0F"),
        AchievementDef("wave_10", "Veteran", "Reach wave 10", "\u2694\uFE0F"),
        AchievementDef("wave_20", "Legend", "Reach wave 20", "\uD83D\uDC51"),
        AchievementDef("wave_30", "Immortal", "Reach wave 30", "\uD83C\uDFC6"),
        AchievementDef("wave_50", "Mythic", "Reach wave 50", "\uD83C\uDF1F"),
        AchievementDef("kills_50", "Slayer", "Kill 50 enemies", "\uD83D\uDC80"),
        AchievementDef("kills_200", "Destroyer", "Kill 200 enemies", "\uD83D\uDD25"),
        AchievementDef("kills_500", "Annihilator", "Kill 500 enemies", "\uD83D\uDCA5"),
        AchievementDef("combo_10", "Combo King", "Get a 10x combo", "\uD83D\uDD17"),
        AchievementDef("combo_20", "Combo God", "Get a 20x combo", "\u26D3\uFE0F"),
        AchievementDef("boss_kill", "Boss Slayer", "Kill your first boss", "\u2620\uFE0F"),
        AchievementDef("5_bosses", "Boss Hunter", "Kill 5 bosses in one run", "\uD83D\uDC09"),
        AchievementDef("5_towers", "Architect", "Place 5 towers", "\uD83C\uDFD7\uFE0F"),
        AchievementDef("10_towers", "Fortress", "Place 10 towers", "\uD83C\uDFF0"),
        AchievementDef("all_tower_types", "Arsenal", "Place all tower types", "\uD83C\uDFAF"),
        AchievementDef("use_power", "Sorcerer", "Use a power for the first time", "\u2728"),
        AchievementDef("max_tower", "Master Builder", "Upgrade a tower to level 5", "\u2B06\uFE0F"),
        AchievementDef("rich", "Rich", "Have 500 gold at once", "\uD83D\uDCB0"),
        AchievementDef("rich_1000", "Millionaire", "Have 1000 gold at once", "\uD83E\uDD11"),
        AchievementDef("score_1000", "Score Chaser", "Reach 1000 score", "\uD83D\uDCCA"),
        AchievementDef("diamond_10", "Diamond Hoarder", "Earn 10 diamonds in a run", "\uD83D\uDC8E"),
        AchievementDef("repaired_3", "Mechanic", "Repair the base 3 times in a run", "\uD83D\uDD27"),
        AchievementDef("upgrade_all", "Well Rounded", "Buy all 4 player upgrades", "\uD83C\uDF96\uFE0F"),
        AchievementDef("endless_10", "Endurance", "Reach wave 10 in endless mode", "\u267E\uFE0F"),
        AchievementDef("kills_1000", "Genocide", "Kill 1000 enemies in one run", "\uD83D\uDC7B"),
        AchievementDef("wave_100", "Centurion", "Reach wave 100", "\u2694\uFE0F"),
        AchievementDef("no_damage", "Untouchable", "Complete a wave without base taking damage", "\uD83D\uDEE1\uFE0F"),
        AchievementDef("speed_demon", "Speed Demon", "Beat wave 10 on 3x speed", "\uD83D\uDCA8"),
        AchievementDef("10_bosses", "Boss Legend", "Kill 10 bosses in one run", "\uD83D\uDC32"),
        AchievementDef("diamond_50", "Diamond Mine", "Earn 50 diamonds in one run", "\uD83D\uDC8E"),
        AchievementDef("gold_hoarder", "Gold Hoarder", "Have 2000 gold at once", "\uD83C\uDFE6"),
        AchievementDef("all_powers", "Elementalist", "Use all 4 powers in one run", "\uD83C\uDF0A"),
        AchievementDef("survivor_1hp", "Last Stand", "Win a wave with base at 1 HP", "\u2764\uFE0F")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        loadAchievements()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadAchievements() {
        val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
        val container = binding.achievementsContainer

        var unlocked = 0
        for (ach in achievementDefs) {
            val isUnlocked = prefs.getBoolean("ach_${ach.id}", false)
            if (isUnlocked) unlocked++

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(if (isUnlocked) 0xFF0F3460.toInt() else 0xFF1A1A2E.toInt())
                setPadding(dp(16), dp(12), dp(16), dp(12))
                gravity = Gravity.CENTER_VERTICAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.bottomMargin = dp(8)
                layoutParams = lp
                if (!isUnlocked) alpha = 0.5f
            }

            val emoji = TextView(this).apply {
                text = if (isUnlocked) ach.emoji else "\uD83D\uDD12"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                val lp = LinearLayout.LayoutParams(dp(48), LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = lp
            }

            val textCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                lp.marginStart = dp(12)
                layoutParams = lp
            }

            val title = TextView(this).apply {
                text = ach.title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(if (isUnlocked) Color.parseColor("#FFD700") else Color.parseColor("#888888"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val desc = TextView(this).apply {
                text = ach.description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(if (isUnlocked) Color.parseColor("#CCCCCC") else Color.parseColor("#666666"))
            }

            textCol.addView(title)
            textCol.addView(desc)

            val status = TextView(this).apply {
                text = if (isUnlocked) "✅" else "❌"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                gravity = Gravity.CENTER
            }

            card.addView(emoji)
            card.addView(textCol)
            card.addView(status)
            container.addView(card)
        }

        binding.textProgress.text = "$unlocked / ${achievementDefs.size} Unlocked"
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
