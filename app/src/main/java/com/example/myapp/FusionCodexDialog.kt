package com.example.myapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.myapp.game.DamageType
import com.example.myapp.game.FusionCategory
import com.example.myapp.game.FusionCatalog
import com.example.myapp.game.FusionDefinition
import com.example.myapp.game.SfxType

object FusionCodexDialog {

    fun show(context: Context, discoveredFusions: Set<String>) {
        GameStrings.init(context)
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.dialog_fusion_codex, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val titleView = dialogView.findViewById<TextView>(R.id.text_codex_title)
        val progressView = dialogView.findViewById<TextView>(R.id.text_codex_progress)
        val closeBtn = dialogView.findViewById<TextView>(R.id.btn_codex_close)
        val dismissBtn = dialogView.findViewById<Button>(R.id.btn_codex_dismiss)
        val container = dialogView.findViewById<LinearLayout>(R.id.codex_content_container)

        titleView.text = GameStrings.codexTitle
        progressView.text = GameStrings.codexDiscoveredCount(discoveredFusions.size, FusionCatalog.allFusions.size)
        dismissBtn.text = GameStrings.codexClose

        val tabAll = dialogView.findViewById<Button>(R.id.tab_all)
        val tabElemental = dialogView.findViewById<Button>(R.id.tab_elemental)
        val tabArcane = dialogView.findViewById<Button>(R.id.tab_arcane)
        val tabDark = dialogView.findViewById<Button>(R.id.tab_dark)
        val tabSiege = dialogView.findViewById<Button>(R.id.tab_siege)

        tabAll.text = " ()"
        tabElemental.text = "🔥 "
        tabArcane.text = "🔮 "
        tabDark.text = "💀 "
        tabSiege.text = "💣 "

        val tabs = listOf(
            Pair(tabAll, null as FusionCategory?),
            Pair(tabElemental, FusionCategory.ELEMENTAL),
            Pair(tabArcane, FusionCategory.ARCANE),
            Pair(tabDark, FusionCategory.DARK_ARTS),
            Pair(tabSiege, FusionCategory.SIEGE)
        )

        fun dp(v: Int): Int = (v * context.resources.displayMetrics.density).toInt()

        fun getElementBadge(type: DamageType): Pair<String, String> = when (type) {
            DamageType.FIRE -> Pair("🔥 FIRE", "#FF5722")
            DamageType.ICE -> Pair("❄️ ICE", "#80DEEA")
            DamageType.ELECTRIC -> Pair("⚡ ELEC", "#00E5FF")
            DamageType.POISON -> Pair("☠️ POISON", "#76FF03")
            DamageType.MAGIC -> Pair("🔮 MAGIC", "#AB47BC")
            DamageType.DARK -> Pair("💀 DARK", "#B388FF")
            DamageType.EXPLOSIVE -> Pair("💣 BOMB", "#FF9800")
            DamageType.PHYSICAL -> Pair("🏹 PHYS", "#B0BEC5")
        }

        fun renderCategory(cat: FusionCategory?) {
            // Update Tab backgrounds
            tabs.forEach { (btn, tabCat) ->
                if (tabCat == cat) {
                    btn.setBackgroundResource(R.drawable.bg_tab_active)
                    btn.setTextColor(Color.WHITE)
                } else {
                    btn.setBackgroundResource(R.drawable.bg_tab_inactive)
                    btn.setTextColor(Color.parseColor("#B0BEC5"))
                }
            }

            val list = if (cat == null) {
                FusionCatalog.allFusions
            } else {
                FusionCatalog.allFusions.filter { it.category == cat }
            }

            container.removeAllViews()

            for (fusion in list) {
                val isDiscovered = discoveredFusions.contains(fusion.id)
                val cardLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundResource(R.drawable.bg_hud_chip)
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                    layoutParams = lp
                }

                // Row 1: Header (Icon + Name + Status Badge)
                val headerRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                }

                val iconView = TextView(context).apply {
                    text = if (isDiscovered) fusion.icon else "🔒"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    setPadding(0, 0, dp(8), 0)
                }
                headerRow.addView(iconView)

                val nameView = TextView(context).apply {
                    text = if (isDiscovered) GameStrings.getLocalizedFusionName(fusion.id) else "??? []"
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(if (isDiscovered) Color.parseColor("#FFD54F") else Color.parseColor("#90A4AE"))
                    setTypeface(typeface, Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                headerRow.addView(nameView)

                val statusBadge = TextView(context).apply {
                    text = if (isDiscovered) GameStrings.codexStatusDiscovered else GameStrings.codexStatusUndiscovered
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(if (isDiscovered) Color.parseColor("#69F0AE") else Color.parseColor("#78909C"))
                    setPadding(dp(6), dp(2), dp(6), dp(2))
                }
                headerRow.addView(statusBadge)
                cardLayout.addView(headerRow)

                // Row 2: Formula / Recipe Badges
                val recipeRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    setPadding(0, dp(4), 0, dp(4))
                }

                val (badgeAText, badgeAColor) = getElementBadge(fusion.elementA)
                val (badgeBText, badgeBColor) = getElementBadge(fusion.elementB)

                val badgeA = TextView(context).apply {
                    text = badgeAText
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(Color.parseColor(badgeAColor))
                    setBackgroundResource(R.drawable.bg_card_glass)
                    setPadding(dp(6), dp(2), dp(6), dp(2))
                }
                val plusView = TextView(context).apply {
                    text = " + "
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(Color.WHITE)
                    setPadding(dp(4), 0, dp(4), 0)
                }
                val badgeB = TextView(context).apply {
                    text = badgeBText
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(Color.parseColor(badgeBColor))
                    setBackgroundResource(R.drawable.bg_card_glass)
                    setPadding(dp(6), dp(2), dp(6), dp(2))
                }

                recipeRow.addView(badgeA)
                recipeRow.addView(plusView)
                recipeRow.addView(badgeB)
                cardLayout.addView(recipeRow)

                // Row 3: Description
                val descView = TextView(context).apply {
                    text = if (isDiscovered) GameStrings.getLocalizedFusionDesc(fusion.id) else GameStrings.codexHintUndiscovered
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    setTextColor(if (isDiscovered) Color.parseColor("#ECEFF1") else Color.parseColor("#78909C"))
                    setLineSpacing(dp(2).toFloat(), 1f)
                    setPadding(0, dp(2), 0, dp(4))
                }
                cardLayout.addView(descView)

                // Row 4: Tactical Role
                val roleView = TextView(context).apply {
                    val role = if (isDiscovered) GameStrings.getLocalizedTacticalRole(fusion.id) else "???"
                    text = "🎯 "
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                    setTypeface(typeface, Typeface.BOLD)
                    setTextColor(if (isDiscovered) Color.parseColor("#00E5FF") else Color.parseColor("#546E7A"))
                }
                cardLayout.addView(roleView)

                container.addView(cardLayout)
            }
        }

        tabs.forEach { (btn, cat) ->
            btn.setOnClickListener {
                SoundManager.play(SfxType.UI_CLICK)
                renderCategory(cat)
            }
        }

        val closeAction = View.OnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            dialog.dismiss()
        }
        closeBtn.setOnClickListener(closeAction)
        dismissBtn.setOnClickListener(closeAction)

        renderCategory(null)
        dialog.show()
    }
}
