package com.example.myapp

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapp.game.DailyChallengeHelper

object DailyChallengeDialog {

    fun show(context: Context, onDeploy: (Intent) -> Unit) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_daily_challenge, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val challenge = DailyChallengeHelper.getDailyChallenge()

        // Title & Subtitle
        view.findViewById<TextView>(R.id.daily_trial_title)?.text = "📅 " + challenge.title
        view.findViewById<TextView>(R.id.daily_trial_subtitle)?.text =
            "${challenge.dateString} • 15 Waves to Victory"

        // Chips
        view.findViewById<TextView>(R.id.chip_daily_map)?.text =
            "${challenge.mapType.emoji} ${challenge.mapType.displayName}"
        view.findViewById<TextView>(R.id.chip_daily_gold)?.text =
            "💰 ${challenge.startingGold} Gold"
        view.findViewById<TextView>(R.id.chip_daily_reward)?.text =
            "💎 +${challenge.diamondReward} Diamonds"

        // Modifiers
        val modContainer = view.findViewById<LinearLayout>(R.id.container_daily_modifiers)
        modContainer?.removeAllViews()

        for (mod in challenge.modifiers) {
            val modRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                val bg = GradientDrawable().apply {
                    cornerRadius = dp(context, 8).toFloat()
                    setColor(0x33263238)
                    setStroke(dp(context, 1), 0x44455A64)
                }
                background = bg
                setPadding(dp(context, 10), dp(context, 8), dp(context, 10), dp(context, 8))
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(context, 6) }
                layoutParams = lp
            }

            val iconView = TextView(context).apply {
                text = if (mod.emoji.isNotBlank()) mod.emoji else "⚡"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                setPadding(0, 0, dp(context, 10), 0)
            }

            val textCol = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams = lp
            }

            val nameView = TextView(context).apply {
                text = mod.displayName
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.WHITE)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }

            val descView = TextView(context).apply {
                text = mod.description
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setTextColor(Color.parseColor("#90A4AE"))
            }

            textCol.addView(nameView)
            textCol.addView(descView)

            modRow.addView(iconView)
            modRow.addView(textCol)
            modContainer?.addView(modRow)
        }

        // Twist details
        val hpDelta = ((challenge.hpMultiplier - 1f) * 100).toInt()
        val spdDelta = ((challenge.speedMultiplier - 1f) * 100).toInt()
        val hpSign = if (hpDelta >= 0) "+$hpDelta%" else "$hpDelta%"
        val spdSign = if (spdDelta >= 0) "+$spdDelta%" else "$spdDelta%"
        view.findViewById<TextView>(R.id.daily_twist_desc)?.text =
            "Daily parameters active: Enemy HP: $hpSign · Enemy Movement: $spdSign. Defeat all 15 waves to claim +${challenge.diamondReward} 💎."

        // Close & Cancel
        view.findViewById<TextView>(R.id.btn_daily_close)?.setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btn_daily_cancel)?.setOnClickListener { dialog.dismiss() }

        // Deploy
        view.findViewById<Button>(R.id.btn_daily_deploy)?.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("difficulty", 1)
                putExtra("daily_challenge", true)
            }
            onDeploy(intent)
        }

        dialog.show()
    }

    private fun dp(context: Context, value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()
}
