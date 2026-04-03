package com.example.myapp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.view.Gravity

class RunHistoryActivity : ImmersiveActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val S = GameStrings
        val prefs = getSharedPreferences("tower_defense_prefs", MODE_PRIVATE)
        val raw = prefs.getString("run_history", "") ?: ""

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0D0D1A.toInt())
        }

        // Top bar: back button + title
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 48, 16, 8)
        }
        topBar.addView(Button(this).apply {
            text = "← Back"
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF2A2A3E.toInt())
            setOnClickListener { finish() }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 16 }
            layoutParams = lp
        })
        topBar.addView(TextView(this).apply {
            text = S.runHistoryTitle
            textSize = 22f
            setTextColor(0xFFFFD700.toInt())
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        root.addView(topBar)

        val scroll = ScrollView(this).apply {
            setPadding(16, 8, 16, 32)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        if (raw.isBlank()) {
            layout.addView(TextView(this).apply {
                text = S.runHistoryEmpty
                textSize = 18f
                setTextColor(0xFFBDBDBD.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 64, 0, 0)
            })
        } else {
            val entries = raw.split("|||").filter { it.isNotBlank() }
            for ((idx, entry) in entries.withIndex()) {
                val parts = entry.split("|")
                if (parts.size < 10) continue

                val mode = parts[0]
                val diff = parts[1]
                val wave = parts[2]
                val score = parts[3]
                val kills = parts[4]
                val combo = parts[5]
                val towers = parts[6]
                val diamonds = parts[7]
                val map = parts[8]
                val timestamp = parts[9].toLongOrNull() ?: 0L
                val result = if (parts.size >= 11) parts[10] else "Lost" // backwards compat

                val date = if (timestamp > 0L) {
                    java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(timestamp))
                } else "—"

                // Card appearance based on result
                val (resultIcon, borderColor, cardBg) = when (result) {
                    "Won"  -> Triple("✅ Won",  0xFF2E7D32.toInt(), 0xFF1A2E1A.toInt())
                    "Quit" -> Triple("🏃 Quit", 0xFF555577.toInt(), 0xFF1A1A2A.toInt())
                    else   -> Triple("❌ Lost", 0xFF7D2020.toInt(), 0xFF2E1A1A.toInt())
                }

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    val border = GradientDrawable().apply {
                        setColor(cardBg)
                        setStroke(3, borderColor)
                        cornerRadius = 12f
                    }
                    background = border
                    setPadding(24, 14, 24, 14)
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 10 }
                    layoutParams = lp
                }

                // Row 1: number + mode + result badge
                val row1 = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }
                row1.addView(TextView(this).apply {
                    text = "#${idx + 1}  $mode ($diff)"
                    textSize = 15f
                    setTextColor(0xFFFFD54F.toInt())
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
                row1.addView(TextView(this).apply {
                    text = resultIcon
                    textSize = 13f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.END
                })
                card.addView(row1)

                // Row 2: map + date
                card.addView(TextView(this).apply {
                    text = "🗺️ $map  ·  📅 $date"
                    textSize = 12f
                    setTextColor(0xFF9E9E9E.toInt())
                    setPadding(0, 2, 0, 6)
                })

                // Row 3: wave / score / kills
                card.addView(TextView(this).apply {
                    text = "⚔️ Wave $wave  |  🏆 $score pts  |  💀 $kills kills"
                    textSize = 14f
                    setTextColor(Color.WHITE)
                })

                // Row 4: combo / towers / diamonds
                card.addView(TextView(this).apply {
                    text = "🔥 Combo ×$combo  |  🏰 $towers towers  |  💎 $diamonds"
                    textSize = 12f
                    setTextColor(0xFFBDBDBD.toInt())
                    setPadding(0, 2, 0, 0)
                })

                layout.addView(card)
            }
        }

        scroll.addView(layout)
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        setContentView(root)
    }
}
