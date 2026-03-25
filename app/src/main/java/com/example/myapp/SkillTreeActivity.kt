package com.example.myapp

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivitySkillTreeBinding
import com.example.myapp.game.SkillTree

class SkillTreeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySkillTreeBinding
    private lateinit var skillTree: SkillTree

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySkillTreeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        skillTree = SkillTree(this)
        binding.btnBack.setOnClickListener { finish() }

        buildSkillList()
    }

    private fun buildSkillList() {
        binding.textDiamonds.text = "\uD83D\uDC8E ${skillTree.diamonds}"
        binding.skillContainer.removeAllViews()

        for (skill in skillTree.skills) {
            val lvl = skillTree.getLevel(skill.id)
            val maxed = lvl >= skill.maxLevel
            val cost = if (maxed) 0 else skill.cost(lvl)
            val canBuy = skillTree.canUpgrade(skill.id)

            // Row container
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 16, 16, 16)
                val bgColor = if (maxed) 0xFF1A3A1A.toInt() else 0xFF1B2838.toInt()
                setBackgroundColor(bgColor)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8 }
            }

            // Left: emoji + info
            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val titleText = TextView(this).apply {
                text = "${skill.emoji} ${skill.name}"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(if (maxed) 0xFF66BB6A.toInt() else Color.WHITE)
            }

            val descText = TextView(this).apply {
                text = skill.description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setTextColor(0xFFAAAAAA.toInt())
            }

            // Level pips
            val levelText = TextView(this).apply {
                val pips = buildString {
                    repeat(lvl) { append("\u2B50") }
                    repeat(skill.maxLevel - lvl) { append("\u2B1C") }
                }
                text = "Lv $lvl/${skill.maxLevel}  $pips"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(0xFFFFD700.toInt())
            }

            infoLayout.addView(titleText)
            infoLayout.addView(descText)
            infoLayout.addView(levelText)

            // Right: upgrade button
            val btn = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                if (maxed) {
                    text = "MAX"
                    isEnabled = false
                    alpha = 0.5f
                } else {
                    text = "\uD83D\uDC8E $cost"
                    isEnabled = canBuy
                    alpha = if (canBuy) 1f else 0.4f
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setOnClickListener {
                    if (skillTree.upgrade(skill.id)) {
                        Toast.makeText(this@SkillTreeActivity,
                            "${skill.emoji} ${skill.name} upgraded!", Toast.LENGTH_SHORT).show()
                        buildSkillList()
                    } else {
                        Toast.makeText(this@SkillTreeActivity,
                            "Not enough diamonds!", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            row.addView(infoLayout)
            row.addView(btn)
            binding.skillContainer.addView(row)
        }
    }
}
