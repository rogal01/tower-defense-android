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

class SkillTreeActivity : ImmersiveActivity() {

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

        // Prestige info
        if (skillTree.prestigeLevel > 0) {
            val prestigeInfo = TextView(this).apply {
                text = "\uD83D\uDC51 Prestige Level ${skillTree.prestigeLevel} — +${(skillTree.prestigeLevel * 5)}% damage bonus"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextColor(0xFFFFD700.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 16)
            }
            binding.skillContainer.addView(prestigeInfo)
        }

        // Page 1 header
        addSectionHeader("Page 1 — Core Skills")

        for (skill in skillTree.skills) {
            addSkillRow(skill)
        }

        // Prestige button
        val prestigeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(16, 24, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val prestigeBtn = Button(this).apply {
            val cost = skillTree.prestigeCost()
            val canDo = skillTree.canPrestige()
            text = if (skillTree.allPage1Maxed()) "\uD83D\uDC51 PRESTIGE (\uD83D\uDC8E $cost)" else "\uD83D\uDD12 Max all skills to prestige"
            isEnabled = canDo
            alpha = if (canDo) 1f else 0.4f
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setOnClickListener {
                if (skillTree.prestige()) {
                    SoundManager.play(SfxType.ACHIEVEMENT)
                    Toast.makeText(this@SkillTreeActivity,
                        "\uD83D\uDC51 Prestige ${skillTree.prestigeLevel}! Page 1 reset, bonus unlocked!", Toast.LENGTH_LONG).show()
                    buildSkillList()
                }
            }
        }
        prestigeRow.addView(prestigeBtn)
        binding.skillContainer.addView(prestigeRow)

        // Page 2: Prestige skills
        if (skillTree.prestigeLevel >= 1) {
            addSectionHeader("Page 2 — Prestige Skills")
            for (skill in skillTree.prestigeSkills) {
                addSkillRow(skill)
            }
        } else {
            val lockText = TextView(this).apply {
                text = "\uD83D\uDD12 Prestige to unlock Page 2 skills"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(0xFF666666.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 16, 0, 8)
            }
            binding.skillContainer.addView(lockText)
        }
    }

    private fun addSectionHeader(title: String) {
        val header = TextView(this).apply {
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(0xFF81D4FA.toInt())
            gravity = Gravity.START
            setPadding(16, 20, 0, 8)
        }
        binding.skillContainer.addView(header)
    }

    private fun addSkillRow(skill: com.example.myapp.game.Skill) {
        val lvl = skillTree.getLevel(skill.id)
        val maxed = lvl >= skill.maxLevel
        val cost = if (maxed) 0 else skill.cost(lvl)
        val canBuy = skillTree.canUpgrade(skill.id)

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
                    SoundManager.play(SfxType.PLAYER_UPGRADE)
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
