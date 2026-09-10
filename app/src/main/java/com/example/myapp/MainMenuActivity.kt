package com.example.myapp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Button
import android.content.SharedPreferences
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.ScrollView
import android.widget.Toast
import com.example.myapp.game.MapType
import com.example.myapp.game.TowerType

class MainMenuActivity : ImmersiveActivity() {

    companion object {
        const val DIFFICULTY_EASY = 0
        const val DIFFICULTY_NORMAL = 1
        const val DIFFICULTY_HARD = 2
        const val DIFFICULTY_ENDLESS = 3
        const val DIFFICULTY_BOSS_RUSH = 4
        const val DIFFICULTY_RANDOMIZER = 5
        const val DIFFICULTY_SURVIVAL = 6
        const val DIFFICULTY_ARENA = 7
        const val DIFFICULTY_IRONMAN = 8
        const val DIFFICULTY_LOADOUT = 9
    }

    private var selectedMap: MapType = MapType.CLASSIC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        GameStrings.init(this)
        SoundManager.init(this)

        // Show tutorial on very first launch (mandatory)
        val prefs0 = getSharedPreferences("tower_defense_prefs", MODE_PRIVATE)
        if (!prefs0.getBoolean("tutorial_seen", false)) {
            prefs0.edit().putBoolean("tutorial_seen", true).apply()
            TutorialDialog.show(this)
        }

        // Language flag toggle
        val btnEn = findViewById<Button>(R.id.btn_lang_en)
        val btnPl = findViewById<Button>(R.id.btn_lang_pl)
        val curLang = LocaleHelper.getLanguage(this)
        btnEn.alpha = if (curLang == "en") 1f else 0.4f
        btnPl.alpha = if (curLang == "pl") 1f else 0.4f
        btnEn.setOnClickListener {
            if (LocaleHelper.getLanguage(this) != "en") {
                LocaleHelper.setLanguage(this, "en")
                recreate()
            }
        }
        btnPl.setOnClickListener {
            if (LocaleHelper.getLanguage(this) != "pl") {
                LocaleHelper.setLanguage(this, "pl")
                recreate()
            }
        }

        val prefs = getSharedPreferences("tower_defense_prefs", MODE_PRIVATE)

        // High score display
        val highScore = prefs.getInt("highScore", 0)
        val highWave = prefs.getInt("highWave", 0)
        val textHighScore = findViewById<TextView>(R.id.text_high_score)
        if (highScore > 0) {
            textHighScore.text = GameStrings.highScoreFmt(highScore, highWave)
        } else {
            textHighScore.text = GameStrings.welcomeCommander
        }

        // Endless record
        val endlessHigh = prefs.getInt("endlessHighWave", 0)
        val textEndlessRecord = findViewById<TextView>(R.id.text_endless_record)
        if (endlessHigh > 0) {
            textEndlessRecord.text = GameStrings.endlessRecordFmt(endlessHigh)
        }

        // Boss Rush record
        val bossRushHigh = prefs.getInt("bossRushHighWave", 0)
        val textBossRushRecord = findViewById<TextView>(R.id.text_boss_rush_record)
        if (bossRushHigh > 0) {
            textBossRushRecord.text = GameStrings.bossRushRecordFmt(bossRushHigh)
        }

        // Diamond counter
        val diamonds = prefs.getInt("diamonds", 0)
        val textDiamonds = findViewById<TextView>(R.id.text_diamonds)
        textDiamonds.text = GameStrings.diamondsFmt(diamonds)

        // Daily login reward
        checkDailyReward(prefs)

        // Rate prompt — after 5+ games played
        checkRatePrompt(prefs)

        // Continue button — show only if there's a saved game
        val btnContinue = findViewById<Button>(R.id.btn_continue)
        val hasSave = prefs.getBoolean("has_save", false)
        btnContinue.visibility = if (hasSave) View.VISIBLE else View.GONE
        btnContinue.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("difficulty", 1)
            intent.putExtra("continue_game", true)
            startActivity(intent)
        }

        // Campaign
        findViewById<Button>(R.id.btn_campaign).setOnClickListener {
            startActivity(Intent(this, CampaignActivity::class.java))
        }

        // Endless Mode — popup with difficulty + map selection
        findViewById<Button>(R.id.btn_endless).setOnClickListener {
            showEndlessPopup()
        }

        // More Modes expandable toggle
        val modesContainer = findViewById<LinearLayout>(R.id.modes_container)
        val btnMoreModes = findViewById<Button>(R.id.btn_more_modes)
        btnMoreModes.setOnClickListener {
            if (modesContainer.visibility == View.GONE) {
                modesContainer.visibility = View.VISIBLE
                btnMoreModes.text = "\u25BC More Modes"
            } else {
                modesContainer.visibility = View.GONE
                btnMoreModes.text = "\u25B6 More Modes"
            }
        }

        // Boss Rush
        findViewById<Button>(R.id.btn_boss_rush).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("difficulty", 4)
            intent.putExtra("map_type", selectedMap.name)
            startActivity(intent)
        }

        // Randomizer
        findViewById<Button>(R.id.btn_randomizer).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("difficulty", 5)
            startActivity(intent)
        }

        // Survival
        findViewById<Button>(R.id.btn_survival).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("difficulty", 6)
            intent.putExtra("map_type", selectedMap.name)
            startActivity(intent)
        }

        // Arena
        findViewById<Button>(R.id.btn_arena).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("difficulty", 7)
            intent.putExtra("map_type", selectedMap.name)
            startActivity(intent)
        }

        // Ironman
        findViewById<Button>(R.id.btn_ironman).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("difficulty", 8)
            intent.putExtra("map_type", selectedMap.name)
            startActivity(intent)
        }

        // Loadout
        findViewById<Button>(R.id.btn_loadout).setOnClickListener {
            showLoadoutPopup()
        }

        // Settings
        findViewById<Button>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Skill Tree
        findViewById<Button>(R.id.btn_skill_tree).setOnClickListener {
            startActivity(Intent(this, SkillTreeActivity::class.java))
        }

        // Statistics
        findViewById<Button>(R.id.btn_stats).setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }

        // Achievements
        findViewById<Button>(R.id.btn_achievements).setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        // Daily Challenge
        findViewById<Button>(R.id.btn_daily).setOnClickListener {
            DailyChallengeDialog.show(this) { intent ->
                startActivity(intent)
            }
        }

        // Help
        findViewById<Button>(R.id.btn_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // Bestiary & Lore Codex
        findViewById<Button>(R.id.btn_bestiary)?.setOnClickListener {
            startActivity(Intent(this, BestiaryActivity::class.java))
        }

        // Elemental Fusions Codex
        findViewById<Button>(R.id.btn_fusions)?.setOnClickListener {
            val discovered = prefs.getString("discovered_fusions", "")
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet() ?: emptySet()
            FusionCodexDialog.show(this, discovered)
        }

        // Run History
        findViewById<Button>(R.id.btn_history).setOnClickListener {
            startActivity(Intent(this, RunHistoryActivity::class.java))
        }

        // Daily Reward calendar button
        findViewById<Button>(R.id.btn_daily_reward).setOnClickListener {
            showDailyRewardDialog(prefs)
        }

        // Beta banner — only visible in debug builds
        if (BuildConfig.DEBUG) {
            findViewById<View>(R.id.beta_banner)?.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh displayed values
        val prefs = getSharedPreferences("tower_defense_prefs", MODE_PRIVATE)
        val diamonds = prefs.getInt("diamonds", 0)
        findViewById<TextView>(R.id.text_diamonds).text = GameStrings.diamondsFmt(diamonds)

        val hasSave = prefs.getBoolean("has_save", false)
        findViewById<Button>(R.id.btn_continue).visibility = if (hasSave) View.VISIBLE else View.GONE

        val highScore = prefs.getInt("highScore", 0)
        val highWave = prefs.getInt("highWave", 0)
        val textHighScore = findViewById<TextView>(R.id.text_high_score)
        if (highScore > 0) {
            textHighScore.text = GameStrings.highScoreFmt(highScore, highWave)
        }

        if (SoundManager.effectiveMusic > 0.01f) {
            MusicManager.playTrack(MusicManager.Track.MENU)
        }
    }

    override fun onPause() {
        super.onPause()
        MusicManager.stop()
    }

    private fun showEndlessPopup() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_map_selection, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tv_dialog_title)
        val btnClose = dialogView.findViewById<TextView>(R.id.btn_close_dialog)
        val btnEasy = dialogView.findViewById<Button>(R.id.btn_diff_easy)
        val btnNormal = dialogView.findViewById<Button>(R.id.btn_diff_normal)
        val btnHard = dialogView.findViewById<Button>(R.id.btn_diff_hard)
        val containerTabs = dialogView.findViewById<LinearLayout>(R.id.container_biome_tabs)
        val containerTopologyTabs = dialogView.findViewById<LinearLayout>(R.id.container_topology_tabs)
        val tvTopologyLabel = dialogView.findViewById<TextView>(R.id.tv_topology_label)
        val tvTopologyDesc = dialogView.findViewById<TextView>(R.id.tv_topology_desc)
        val btnRerollSeed = dialogView.findViewById<Button>(R.id.btn_reroll_seed)
        val mapPreview = dialogView.findViewById<com.example.myapp.game.MapPreviewView>(R.id.map_preview_view)
        val tvMapName = dialogView.findViewById<TextView>(R.id.tv_map_name)
        val tvLaneBadge = dialogView.findViewById<TextView>(R.id.tv_lane_badge)
        val tvTacticalBadge = dialogView.findViewById<TextView>(R.id.tv_tactical_badge)
        val tvMapDesc = dialogView.findViewById<TextView>(R.id.tv_map_desc)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel_dialog)
        val btnStart = dialogView.findViewById<Button>(R.id.btn_start_run)

        tvTitle.text = GameStrings.endlessModeTitle
        btnStart.text = GameStrings.deployToEndless
        tvTopologyLabel.text = GameStrings.pathTopologyLabel
        btnRerollSeed.text = GameStrings.rerollSeedBtn

        var chosenSubDiff = 1 // 0=easy, 1=normal, 2=hard
        var chosenMap = selectedMap
        var chosenTopology = com.example.myapp.game.PathTopology.DEFAULT
        var chosenSeed = 42L

        fun updateDifficultyUI() {
            btnEasy.setBackgroundResource(if (chosenSubDiff == 0) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            btnEasy.setTextColor(if (chosenSubDiff == 0) Color.WHITE else Color.parseColor("#B0BEC5"))

            btnNormal.setBackgroundResource(if (chosenSubDiff == 1) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            btnNormal.setTextColor(if (chosenSubDiff == 1) Color.WHITE else Color.parseColor("#B0BEC5"))

            btnHard.setBackgroundResource(if (chosenSubDiff == 2) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            btnHard.setTextColor(if (chosenSubDiff == 2) Color.WHITE else Color.parseColor("#B0BEC5"))
        }

        btnEasy.setOnClickListener { chosenSubDiff = 0; updateDifficultyUI() }
        btnNormal.setOnClickListener { chosenSubDiff = 1; updateDifficultyUI() }
        btnHard.setOnClickListener { chosenSubDiff = 2; updateDifficultyUI() }
        updateDifficultyUI()

        val tabButtons = mutableListOf<Button>()
        val topologyButtons = mutableListOf<Button>()

        fun updateMapCard(mapType: MapType) {
            chosenMap = mapType
            selectedMap = mapType
            mapPreview.setMapType(mapType, chosenTopology, chosenSeed)
            tvMapName.text = "${mapType.emoji} ${GameStrings.mapName(mapType.displayName)}"
            val lanes = com.example.myapp.game.MapPathGenerator.getLaneCount(mapType, chosenTopology)
            tvLaneBadge.text = "⚔️ " + GameStrings.mapLanesFmt(lanes)
            tvTacticalBadge.text = GameStrings.mapTacticalTag(mapType)
            tvMapDesc.text = GameStrings.mapDescription(mapType)
            tvTopologyDesc.text = GameStrings.topologyDesc(chosenTopology)

            tabButtons.forEachIndexed { idx, btn ->
                val isSel = MapType.entries[idx] == mapType
                btn.setBackgroundResource(if (isSel) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
                btn.setTextColor(if (isSel) Color.WHITE else Color.parseColor("#B0BEC5"))
                btn.setTypeface(null, if (isSel) Typeface.BOLD else Typeface.NORMAL)
            }
        }

        fun updateTopologyUI() {
            mapPreview.setTopology(chosenTopology, chosenSeed)
            tvTopologyDesc.text = GameStrings.topologyDesc(chosenTopology)
            val lanes = com.example.myapp.game.MapPathGenerator.getLaneCount(chosenMap, chosenTopology)
            tvLaneBadge.text = "⚔️ " + GameStrings.mapLanesFmt(lanes)

            topologyButtons.forEachIndexed { idx, btn ->
                val topo = com.example.myapp.game.PathTopology.entries[idx]
                val isSel = topo == chosenTopology
                btn.setBackgroundResource(if (isSel) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
                btn.setTextColor(if (isSel) Color.WHITE else Color.parseColor("#B0BEC5"))
                btn.setTypeface(null, if (isSel) Typeface.BOLD else Typeface.NORMAL)
            }
        }

        containerTabs.removeAllViews()
        MapType.entries.forEach { mt ->
            val btn = Button(this).apply {
                text = "${mt.emoji} ${GameStrings.mapName(mt.displayName)}"
                textSize = 12f
                isAllCaps = false
                val padH = (12 * resources.displayMetrics.density).toInt()
                val padV = (6 * resources.displayMetrics.density).toInt()
                setPadding(padH, padV, padH, padV)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (36 * resources.displayMetrics.density).toInt()
                ).apply {
                    marginEnd = (6 * resources.displayMetrics.density).toInt()
                }
                layoutParams = lp
                setOnClickListener { updateMapCard(mt) }
            }
            tabButtons.add(btn)
            containerTabs.addView(btn)
        }

        containerTopologyTabs.removeAllViews()
        com.example.myapp.game.PathTopology.entries.forEach { topo ->
            val btn = Button(this).apply {
                text = "${topo.emoji} ${GameStrings.topologyName(topo)}"
                textSize = 12f
                isAllCaps = false
                val padH = (12 * resources.displayMetrics.density).toInt()
                val padV = (6 * resources.displayMetrics.density).toInt()
                setPadding(padH, padV, padH, padV)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (36 * resources.displayMetrics.density).toInt()
                ).apply {
                    marginEnd = (6 * resources.displayMetrics.density).toInt()
                }
                layoutParams = lp
                setOnClickListener {
                    chosenTopology = topo
                    updateTopologyUI()
                }
            }
            topologyButtons.add(btn)
            containerTopologyTabs.addView(btn)
        }

        btnRerollSeed.setOnClickListener {
            chosenSeed = (System.currentTimeMillis() % 100000L) + 1L
            updateTopologyUI()
        }

        updateMapCard(chosenMap)
        updateTopologyUI()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnStart.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("difficulty", 3)
            intent.putExtra("endless_sub_difficulty", chosenSubDiff)
            intent.putExtra("map_type", chosenMap.name)
            intent.putExtra("topology", chosenTopology.name)
            intent.putExtra("map_seed", chosenSeed)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun showLoadoutPopup() {
        val maxTowers = 6
        val selected = mutableSetOf<TowerType>()

        val root = ScrollView(this).apply {
            setBackgroundColor(0xFF1B2838.toInt())
        }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }
        root.addView(layout)

        // Title
        layout.addView(TextView(this).apply {
            text = getString(R.string.loadout_title)
            textSize = 22f
            setTextColor(0xFFFFD700.toInt())
            gravity = android.view.Gravity.CENTER
        })

        // Subtitle
        val subtitleView = TextView(this).apply {
            text = getString(R.string.loadout_max) + " (0/$maxTowers)"
            textSize = 14f
            setTextColor(0xFFB0BEC5.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 16)
        }
        layout.addView(subtitleView)

        // Tower checkboxes
        val checkboxes = mutableListOf<CheckBox>()
        val grid = GridLayout(this).apply {
            columnCount = 2
            setPadding(0, 8, 0, 8)
        }

        fun updateSubtitle() {
            subtitleView.text = getString(R.string.loadout_max) + " (${selected.size}/$maxTowers)"
        }

        for (tt in TowerType.entries) {
            val cb = CheckBox(this).apply {
                text = "${tt.emoji}  ${tt.name}  (${tt.baseCost}g)"
                setTextColor(0xFFE0E0E0.toInt())
                textSize = 14f
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                layoutParams = params
                setPadding(8, 8, 8, 8)
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        if (selected.size >= maxTowers) {
                            this.isChecked = false
                            return@setOnCheckedChangeListener
                        }
                        selected.add(tt)
                    } else {
                        selected.remove(tt)
                    }
                    updateSubtitle()
                }
            }
            checkboxes.add(cb)
            grid.addView(cb)
        }
        layout.addView(grid)

        // Map selection
        layout.addView(TextView(this).apply {
            text = GameStrings.mapSection
            textSize = 16f
            setTextColor(0xFFE0E0E0.toInt())
            setPadding(0, 16, 0, 4)
        })
        val mapGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        MapType.entries.forEachIndexed { idx, mt ->
            mapGroup.addView(RadioButton(this).apply {
                text = "${mt.emoji} ${GameStrings.mapName(mt.displayName)}"
                setTextColor(0xFFE0E0E0.toInt())
                id = idx + 200
                if (mt == selectedMap) isChecked = true
            })
        }
        layout.addView(mapGroup)

        val dialog = AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
            .setView(root)
            .setPositiveButton(GameStrings.btnPlay) { _, _ ->
                if (selected.isEmpty()) return@setPositiveButton
                val mapIdx = (mapGroup.checkedRadioButtonId - 200).coerceIn(0, MapType.entries.size - 1)
                val chosenMap = MapType.entries[mapIdx]

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("difficulty", 9)
                intent.putExtra("map_type", chosenMap.name)
                intent.putExtra("loadout_towers", selected.map { it.name }.toTypedArray())
                startActivity(intent)
            }
            .setNegativeButton(GameStrings.btnCancel, null)
            .create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFFFFD700.toInt())
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF9E9E9E.toInt())
    }

    // ─── Daily Reward System ───

    private fun checkDailyReward(prefs: SharedPreferences) {
        val today = java.time.LocalDate.now().toEpochDay()
        val lastClaim = prefs.getLong("daily_last_claim", 0L)
        if (today > lastClaim) {
            // New day — show notification dot on calendar button
            findViewById<Button>(R.id.btn_daily_reward)?.let { btn ->
                btn.text = "📅❗"
                btn.textSize = 18f
            }
        }
    }

    private fun showDailyRewardDialog(prefs: SharedPreferences) {
        val today = java.time.LocalDate.now().toEpochDay()
        val lastClaim = prefs.getLong("daily_last_claim", 0L)
        val streak = prefs.getInt("daily_streak", 0)
        val canClaim = today > lastClaim

        // Reward schedule: day 1-7, cycling
        val rewards = arrayOf(
            "💰 20 Gold" to 20,
            "💎 1 Diamond" to 1,
            "💰 35 Gold" to 35,
            "💰 50 Gold" to 50,
            "💎 2 Diamonds" to 2,
            "💰 75 Gold" to 75,
            "💎 5 Diamonds" to 5
        )

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 24)
            setBackgroundColor(0xFF1B2838.toInt())
        }

        // Title
        layout.addView(TextView(this).apply {
            text = "📅 Daily Rewards"
            textSize = 24f
            setTextColor(0xFFFFD700.toInt())
            gravity = android.view.Gravity.CENTER
        })

        // Streak
        layout.addView(TextView(this).apply {
            text = "🔥 Streak: ${streak} day${if (streak != 1) "s" else ""}"
            textSize = 16f
            setTextColor(0xFFFF9800.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })

        // Calendar grid — 7 days
        val grid = GridLayout(this).apply {
            columnCount = 7
            setPadding(0, 8, 0, 16)
        }

        val currentDay = streak % 7  // which reward slot we're on
        for (i in 0 until 7) {
            val dayLabel = "Day ${i + 1}"
            val (rewardText, _) = rewards[i]
            val isPast = i < currentDay
            val isToday = i == currentDay && canClaim
            val isFuture = i > currentDay || (i == currentDay && !canClaim)

            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                layoutParams = params
                setPadding(4, 8, 4, 8)
                setBackgroundColor(when {
                    isToday -> 0xFF2E7D32.toInt()
                    isPast -> 0xFF37474F.toInt()
                    else -> 0xFF263238.toInt()
                })
            }
            cell.addView(TextView(this).apply {
                text = dayLabel
                textSize = 10f
                setTextColor(if (isToday) 0xFFFFFFFF.toInt() else 0xFFB0BEC5.toInt())
                gravity = android.view.Gravity.CENTER
            })
            cell.addView(TextView(this).apply {
                text = if (isPast) "✅" else rewardText.split(" ")[0]
                textSize = 16f
                setTextColor(0xFFFFFFFF.toInt())
                gravity = android.view.Gravity.CENTER
            })
            cell.addView(TextView(this).apply {
                text = if (isPast) "Claimed" else rewards[i].first.split(" ").drop(1).joinToString(" ")
                textSize = 9f
                setTextColor(if (isToday) 0xFFFFD700.toInt() else 0xFF78909C.toInt())
                gravity = android.view.Gravity.CENTER
            })
            grid.addView(cell)
        }
        layout.addView(grid)

        // Status text
        layout.addView(TextView(this).apply {
            text = if (canClaim) "🎁 Tap CLAIM to collect today's reward!" else "✅ Already claimed today. Come back tomorrow!"
            textSize = 14f
            setTextColor(if (canClaim) 0xFF4CAF50.toInt() else 0xFF78909C.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 0)
        })

        val builder = AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
            .setView(layout)
            .setNegativeButton("Close", null)

        if (canClaim) {
            builder.setPositiveButton("🎁 CLAIM") { _, _ ->
                val (rewardLabel, rewardAmount) = rewards[currentDay]
                val newStreak = streak + 1
                val ed = prefs.edit()
                ed.putLong("daily_last_claim", today)
                ed.putInt("daily_streak", newStreak)

                if (rewardLabel.contains("Diamond")) {
                    val cur = prefs.getInt("diamonds", 0)
                    ed.putInt("diamonds", cur + rewardAmount)
                    Toast.makeText(this, "🎁 +$rewardAmount 💎 Diamonds!", Toast.LENGTH_SHORT).show()
                } else {
                    // Gold bonus — stored and given at next game start
                    val curBonus = prefs.getInt("daily_gold_bonus", 0)
                    ed.putInt("daily_gold_bonus", curBonus + rewardAmount)
                    Toast.makeText(this, "🎁 +$rewardAmount 💰 Gold (next game)!", Toast.LENGTH_SHORT).show()
                }
                ed.apply()

                // Update diamond display
                findViewById<TextView>(R.id.text_diamonds).text =
                    GameStrings.diamondsFmt(prefs.getInt("diamonds", 0))
                // Remove notification dot
                findViewById<Button>(R.id.btn_daily_reward)?.let { btn ->
                    btn.text = "📅"
                    btn.textSize = 24f
                }
            }
        }

        val dialog = builder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFFFFD700.toInt())
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF9E9E9E.toInt())
    }

    // ─── Rate Prompt ───

    private fun checkRatePrompt(prefs: SharedPreferences) {
        val gamesPlayed = prefs.getInt("total_games", 0)
        val alreadyRated = prefs.getBoolean("has_rated", false)
        val lastPrompt = prefs.getLong("rate_last_prompt", 0L)
        val today = java.time.LocalDate.now().toEpochDay()

        // Show prompt after 5+ games, not already rated, not prompted in last 7 days
        if (gamesPlayed >= 5 && !alreadyRated && today - lastPrompt >= 7) {
            prefs.edit().putLong("rate_last_prompt", today).apply()

            val dialog = AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert)
                .setTitle("⭐ Enjoying the game?")
                .setMessage("If you're having fun, please rate us on Google Play! It helps a lot.")
                .setPositiveButton("⭐ Rate Now") { _, _ ->
                    prefs.edit().putBoolean("has_rated", true).apply()
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("market://details?id=$packageName")))
                    } catch (_: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                    }
                }
                .setNeutralButton("Later", null)
                .setNegativeButton("No Thanks") { _, _ ->
                    prefs.edit().putBoolean("has_rated", true).apply()
                }
                .create()
            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(0xFFFFD700.toInt())
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(0xFF9E9E9E.toInt())
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(0xFF9E9E9E.toInt())
        }
    }
}
