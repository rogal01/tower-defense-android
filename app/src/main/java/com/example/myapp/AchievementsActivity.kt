package com.example.myapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapp.databinding.ActivityAchievementsBinding
import com.example.myapp.game.ContentCatalog

class AchievementsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityAchievementsBinding

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
        val achievementDefs = ContentCatalog.achievementDefs

        var unlocked = 0
        for (achievement in achievementDefs) {
            val isUnlocked = prefs.getBoolean("ach_${achievement.id}", false)
            if (isUnlocked) unlocked++

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(if (isUnlocked) 0xFF0F3460.toInt() else 0xFF1A1A2E.toInt())
                setPadding(dp(16), dp(12), dp(16), dp(12))
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(8)
                }
                if (!isUnlocked) alpha = 0.5f
            }

            val emoji = TextView(this).apply {
                text = if (isUnlocked) achievement.emoji else "\uD83D\uDD12"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                layoutParams = LinearLayout.LayoutParams(dp(48), LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val textCol = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = dp(12)
                }
            }

            val title = TextView(this).apply {
                text = achievement.title
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(if (isUnlocked) Color.parseColor("#FFD700") else Color.parseColor("#888888"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val desc = TextView(this).apply {
                text = achievement.description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(if (isUnlocked) Color.parseColor("#CCCCCC") else Color.parseColor("#666666"))
            }

            val status = TextView(this).apply {
                text = if (isUnlocked) "DONE" else "LOCKED"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(if (isUnlocked) Color.parseColor("#81C784") else Color.parseColor("#B0BEC5"))
                gravity = Gravity.CENTER
            }

            textCol.addView(title)
            textCol.addView(desc)

            card.addView(emoji)
            card.addView(textCol)
            card.addView(status)
            container.addView(card)
        }

        binding.textProgress.text = "$unlocked / ${achievementDefs.size} unlocked"
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
