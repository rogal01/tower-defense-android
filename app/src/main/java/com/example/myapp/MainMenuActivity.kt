package com.example.myapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivityMenuBinding
import com.example.myapp.game.CampaignData
import com.example.myapp.game.SkillTree

class MainMenuActivity : ImmersiveActivity() {

    private lateinit var binding: ActivityMenuBinding
    private var selectedDifficulty: Int = DIFFICULTY_NORMAL
    private var selectedMapType: String = "CLASSIC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        SoundManager.init(this)

        loadHighScore()
        updateHardLock()
        highlightDifficulty(selectedDifficulty)

        binding.btnEasy.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            selectedDifficulty = DIFFICULTY_EASY
            highlightDifficulty(selectedDifficulty)
        }
        binding.btnNormal.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            selectedDifficulty = DIFFICULTY_NORMAL
            highlightDifficulty(selectedDifficulty)
        }
        binding.btnHard.setOnClickListener {
            if (!isHardUnlocked()) {
                Toast.makeText(this, "\uD83D\uDD12 Reach wave 10 on Normal to unlock!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            SoundManager.play(SfxType.UI_CLICK)
            selectedDifficulty = DIFFICULTY_HARD
            highlightDifficulty(selectedDifficulty)
        }

        binding.btnPlay.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_DIFFICULTY, selectedDifficulty)
            intent.putExtra("map_type", selectedMapType)
            startActivity(intent)
        }

        binding.btnContinue.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
            val savedMap = prefs.getString("save_map", "CLASSIC") ?: "CLASSIC"
            val savedDiff = prefs.getInt("save_difficulty", DIFFICULTY_NORMAL)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_DIFFICULTY, savedDiff)
            intent.putExtra("map_type", savedMap)
            intent.putExtra("continue_game", true)
            startActivity(intent)
        }

        binding.btnEndless.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_DIFFICULTY, DIFFICULTY_ENDLESS)
            intent.putExtra("map_type", selectedMapType)
            startActivity(intent)
        }

        binding.btnSettings.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnSkillTree.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            startActivity(Intent(this, SkillTreeActivity::class.java))
        }

        binding.btnStats.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            startActivity(Intent(this, StatsActivity::class.java))
        }

        binding.btnAchievements.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        binding.btnCampaign.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            startActivity(Intent(this, CampaignActivity::class.java))
        }

        // Boss Rush
        binding.btnBossRush.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_DIFFICULTY, DIFFICULTY_BOSS_RUSH)
            intent.putExtra("map_type", selectedMapType)
            startActivity(intent)
        }

        // Daily challenge
        binding.btnDaily.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_DIFFICULTY, DIFFICULTY_NORMAL)
            intent.putExtra("daily_challenge", true)
            intent.putExtra("map_type", selectedMapType)
            startActivity(intent)
        }

        // Map selection
        binding.btnMapClassic.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            selectedMapType = "CLASSIC"
            highlightMap()
        }
        binding.btnMapValley.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            selectedMapType = "VALLEY"
            highlightMap()
        }
        binding.btnMapCrossroads.setOnClickListener {
            SoundManager.play(SfxType.UI_CLICK)
            selectedMapType = "CROSSROADS"
            highlightMap()
        }
    }

    override fun onResume() {
        super.onResume()
        loadHighScore()
        updateHardLock()
        loadDiamonds()
        loadCampaignProgress()
        updateContinueButton()
    }

    private fun updateContinueButton() {
        val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
        val hasSave = prefs.getBoolean("has_save", false)
        binding.btnContinue.visibility = if (hasSave) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun loadDiamonds() {
        val st = SkillTree(this)
        if (st.diamonds > 0) {
            binding.textDiamonds.text = "\uD83D\uDC8E ${st.diamonds} Diamonds"
        } else {
            binding.textDiamonds.text = ""
        }
    }

    private fun loadCampaignProgress() {
        val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
        val completed = CampaignData.levels.count { prefs.getBoolean("campaign_${it.id}", false) }
        if (completed > 0) {
            binding.btnCampaign.text = "\uD83D\uDDFA\uFE0F CAMPAIGN ($completed/${CampaignData.levels.size})"
        }
    }

    private fun isHardUnlocked(): Boolean {
        val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
        return prefs.getBoolean("normal_beaten", false)
    }

    private fun updateHardLock() {
        val unlocked = isHardUnlocked()
        binding.btnHard.alpha = if (unlocked) 1f else 0.3f
        binding.btnHard.text = if (unlocked) "\uD83D\uDD25 Hard" else "\uD83D\uDD12 Hard"
        if (!unlocked && selectedDifficulty == DIFFICULTY_HARD) {
            selectedDifficulty = DIFFICULTY_NORMAL
            highlightDifficulty(selectedDifficulty)
        }
    }

    private fun loadHighScore() {
        val prefs = getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
        val highScore = prefs.getInt("highScore", 0)
        val highWave = prefs.getInt("highWave", 0)
        val endlessHighWave = prefs.getInt("endlessHighWave", 0)
        if (highScore > 0) {
            binding.textHighScore.text = "⭐ Best: $highScore  |  Wave: $highWave"
        } else {
            binding.textHighScore.text = ""
        }
        if (endlessHighWave > 0) {
            binding.textEndlessRecord.text = "♾️ Endless Record: Wave $endlessHighWave"
        } else {
            binding.textEndlessRecord.text = ""
        }
        val bossRushHighWave = prefs.getInt("bossRushHighWave", 0)
        if (bossRushHighWave > 0) {
            binding.textBossRushRecord.text = "💀 Boss Rush Record: $bossRushHighWave bosses"
        } else {
            binding.textBossRushRecord.text = ""
        }
    }

    private fun highlightDifficulty(difficulty: Int) {
        val alpha = 0.4f
        binding.btnEasy.alpha = alpha
        binding.btnNormal.alpha = alpha
        // Keep hard dimmer if locked
        val hardUnlocked = isHardUnlocked()
        binding.btnHard.alpha = if (hardUnlocked) alpha else 0.3f
        when (difficulty) {
            DIFFICULTY_EASY -> binding.btnEasy.alpha = 1f
            DIFFICULTY_NORMAL -> binding.btnNormal.alpha = 1f
            DIFFICULTY_HARD -> if (hardUnlocked) binding.btnHard.alpha = 1f
        }
    }

    private fun highlightMap() {
        binding.btnMapClassic.alpha = if (selectedMapType == "CLASSIC") 1f else 0.4f
        binding.btnMapValley.alpha = if (selectedMapType == "VALLEY") 1f else 0.4f
        binding.btnMapCrossroads.alpha = if (selectedMapType == "CROSSROADS") 1f else 0.4f
    }

    companion object {
        const val EXTRA_DIFFICULTY = "difficulty"
        const val DIFFICULTY_EASY = 0
        const val DIFFICULTY_NORMAL = 1
        const val DIFFICULTY_HARD = 2
        const val DIFFICULTY_ENDLESS = 3
        const val DIFFICULTY_BOSS_RUSH = 4
    }
}
