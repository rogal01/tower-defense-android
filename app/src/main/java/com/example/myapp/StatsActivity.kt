package com.example.myapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityStatsBinding
import com.example.myapp.game.AndroidGamePreferences
import com.example.myapp.game.SkillTree
import com.example.myapp.game.TowerType

class StatsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        GameStrings.init(this)

        loadStats()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadStats() {
        val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)

        // Combat
        val kills = prefs.getInt("lifetime_kills", 0)
        val bosses = prefs.getInt("lifetime_bosses", 0)
        val bestCombo = prefs.getInt("lifetime_best_combo", 0)
        val towers = prefs.getInt("lifetime_towers", 0)

        binding.statKills.text = GameStrings.statTotalKills(kills)
        binding.statBosses.text = GameStrings.statBossesDefeated(bosses)
        binding.statBestCombo.text = GameStrings.statBestCombo(bestCombo)
        binding.statTowers.text = GameStrings.statTowersPlaced(towers)

        // Favorite tower + per-tower breakdown
        var favName = GameStrings.statNoFavorite
        var favCount = 0
        for (tt in TowerType.entries) {
            val count = prefs.getInt("tower_count_${tt.name}", 0)
            if (count > favCount) { favCount = count; favName = "${tt.emoji} ${tt.name}" }
        }
        val towerLine = TowerType.entries.joinToString("  ") { tt ->
            "${tt.emoji}×${prefs.getInt("tower_count_${tt.name}", 0)}"
        }
        binding.statFavoriteTower.text = "${GameStrings.statFavoriteTower(favName)}\n$towerLine"

        // Progress
        val games = prefs.getInt("lifetime_games", 0)
        val waves = prefs.getInt("lifetime_waves", 0)
        val score = prefs.getInt("lifetime_score", 0)
        val gold = prefs.getInt("lifetime_gold", 0)

        binding.statGames.text = GameStrings.statGamesPlayed(games)
        binding.statWaves.text = GameStrings.statTotalWaves(waves)
        binding.statScore.text = GameStrings.statLifetimeScore(score)
        binding.statGold.text = GameStrings.statLifetimeGold(gold)

        // Records
        val highScore = prefs.getInt("highScore", 0)
        val highWave = prefs.getInt("highWave", 0)
        val endlessHigh = prefs.getInt("endlessHighWave", 0)
        val bossRushHigh = prefs.getInt("bossRushHighWave", 0)
        val diamonds = SkillTree(AndroidGamePreferences(getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE))).diamonds
        val hsEasy = prefs.getInt("highScore_0", 0)
        val hsNormal = prefs.getInt("highScore_1", 0)
        val hsHard = prefs.getInt("highScore_2", 0)

        binding.statHighScore.text = if (hsEasy + hsNormal + hsHard > 0)
            "⭐ Best: Easy $hsEasy | Normal $hsNormal | Hard $hsHard"
        else GameStrings.statHighScore(highScore)
        binding.statHighWave.text = GameStrings.statBestWave(highWave)
        binding.statEndlessRecord.text = GameStrings.statEndlessRecord(endlessHigh)
        binding.statBossRushRecord.text = GameStrings.statBossRushRecord(bossRushHigh)
        binding.statDiamonds.text = GameStrings.statDiamonds(diamonds)

        // Play time
        val totalSeconds = prefs.getInt("lifetime_playtime", 0)
        val hours = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        binding.statPlaytime.text = "⏱ Play Time: ${hours}h ${mins}m"

        // Computed averages
        if (games > 0) {
            val avgWave = waves.toFloat() / games
            val avgKills = kills.toFloat() / games
            binding.statAverages.text = "📊 Avg: %.1f waves/game · %.0f kills/game".format(avgWave, avgKills)
        } else {
            binding.statAverages.text = ""
        }

        // Tower mastery
        val masteryLines = TowerType.entries.joinToString("  ") { tt ->
            val mk = prefs.getInt("mastery_${tt.name}", 0)
            val lvl = when {
                mk >= 1500 -> "★★★★★"
                mk >= 750 -> "★★★★"
                mk >= 300 -> "★★★"
                mk >= 100 -> "★★"
                mk >= 25 -> "★"
                else -> "·"
            }
            "${tt.emoji}$lvl"
        }
        binding.statMastery.text = "🏆 Mastery: $masteryLines"
    }
}
