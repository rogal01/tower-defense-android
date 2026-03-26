package com.example.myapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityCampaignBinding
import com.example.myapp.game.CampaignData

class CampaignActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCampaignBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCampaignBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        buildLevelList()
    }

    private fun buildLevelList() {
        val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
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
                setBackgroundColor(
                    when {
                        isCompleted -> 0xFF0F3460.toInt()
                        isUnlocked -> 0xFF1B2838.toInt()
                        else -> 0xFF111122.toInt()
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
                        val intent = Intent(this@CampaignActivity, MainActivity::class.java)
                        intent.putExtra("difficulty", MainMenuActivity.DIFFICULTY_NORMAL)
                        intent.putExtra("campaign_level", level.id)
                        startActivity(intent)
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
                text = "Survive ${level.targetWave} waves  |  $towerList"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setTextColor(Color.parseColor("#888888"))
            }

            textCol.addView(title)
            textCol.addView(desc)
            textCol.addView(meta)

            val status = TextView(this).apply {
                text = when {
                    isCompleted -> "⭐"
                    isUnlocked -> "▶"
                    else -> ""
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                gravity = Gravity.CENTER
            }

            card.addView(emoji)
            card.addView(textCol)
            card.addView(status)
            container.addView(card)
        }

        binding.textProgress.text = "$completed / ${CampaignData.levels.size} Completed"
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
