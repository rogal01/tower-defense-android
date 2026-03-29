package com.example.myapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityStatsBinding
import com.example.myapp.game.SkillTree
import com.example.myapp.game.TowerType

class StatsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityStatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        loadStats()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadStats() {
        val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)

        // Combat
        val kills = prefs.getInt("lifetime_kills", 0)
        val bosses = prefs.getInt("lifetime_bosses", 0)
        val bestCombo = prefs.getInt("lifetime_best_combo", 0)
        val towers = prefs.getInt("lifetime_towers", 0)

        binding.statKills.text = "\uD83D\uDDE1\uFE0F Total Kills: $kills"
        binding.statBosses.text = "\u2620\uFE0F Bosses Defeated: $bosses"
        binding.statBestCombo.text = "\uD83D\uDD17 Best Combo: ${bestCombo}x"
        binding.statTowers.text = "\uD83C\uDFF0 Towers Placed: $towers"

        // Favorite tower
        var favName = "None yet"
        var favCount = 0
        for (tt in TowerType.entries) {
            val count = prefs.getInt("tower_count_${tt.name}", 0)
            if (count > favCount) { favCount = count; favName = "${tt.emoji} ${tt.name}" }
        }
        binding.statFavoriteTower.text = "\u2764\uFE0F Favorite Tower: $favName"

        // Progress
        val games = prefs.getInt("lifetime_games", 0)
        val waves = prefs.getInt("lifetime_waves", 0)
        val score = prefs.getInt("lifetime_score", 0)
        val gold = prefs.getInt("lifetime_gold", 0)

        binding.statGames.text = "\uD83C\uDFAE Games Played: $games"
        binding.statWaves.text = "\uD83C\uDF0A Total Waves: $waves"
        binding.statScore.text = "\u2B50 Lifetime Score: $score"
        binding.statGold.text = "\uD83D\uDCB0 Lifetime Gold: $gold"

        // Records
        val highScore = prefs.getInt("highScore", 0)
        val highWave = prefs.getInt("highWave", 0)
        val endlessHigh = prefs.getInt("endlessHighWave", 0)
        val bossRushHigh = prefs.getInt("bossRushHighWave", 0)
        val diamonds = SkillTree(this).diamonds

        binding.statHighScore.text = "\u2B50 High Score: $highScore"
        binding.statHighWave.text = "\u2694\uFE0F Best Wave: $highWave"
        binding.statEndlessRecord.text = "\u267E\uFE0F Endless Record: Wave $endlessHigh"
        binding.statBossRushRecord.text = "\uD83D\uDC80 Boss Rush Record: $bossRushHigh bosses"
        binding.statDiamonds.text = "\uD83D\uDC8E Diamonds: $diamonds"
    }
}
