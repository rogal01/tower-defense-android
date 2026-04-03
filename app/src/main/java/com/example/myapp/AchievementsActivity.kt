package com.example.myapp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.GradientDrawable
import com.example.myapp.databinding.ActivityAchievementsBinding

class AchievementsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        GameStrings.init(this)

        loadAchievements()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadAchievements() {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
        val container = binding.achievementsContainer
        val achievementDefs = GameStrings.achievements()

        var unlocked = 0
        for (ach in achievementDefs) {
            val isUnlocked = prefs.getBoolean("ach_${ach.id}", false)
            if (isUnlocked) unlocked++

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val bg = GradientDrawable().apply {
                    cornerRadius = dp(8).toFloat()
                    if (isUnlocked) {
                        setColor(0xFF0F3460.toInt())
                        setStroke(dp(2), 0xFFFFD700.toInt())
                    } else {
                        setColor(0xFF1A1A2E.toInt())
                    }
                }
                background = bg
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

            // Badge indicator for unlocked
            val badge = if (isUnlocked) TextView(this).apply {
                text = "★"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(0xFFFFD700.toInt())
                gravity = Gravity.CENTER
                val lp = LinearLayout.LayoutParams(dp(20), LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = lp
            } else null

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
            if (badge != null) card.addView(badge)
            card.addView(textCol)
            card.addView(status)
            container.addView(card)
        }

        binding.textProgress.text = GameStrings.achievementProgress(unlocked, achievementDefs.size)
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
