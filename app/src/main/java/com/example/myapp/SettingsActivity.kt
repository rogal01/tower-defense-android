package com.example.myapp

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivitySettingsBinding

class SettingsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val prefs = getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)

        // Load saved values
        binding.seekMaster.progress = prefs.getInt("master_volume", 80)
        binding.seekMusic.progress = prefs.getInt("music_volume", 70)
        binding.seekSfx.progress = prefs.getInt("sfx_volume", 80)
        binding.switchFps.isChecked = prefs.getBoolean("show_fps", false)
        binding.switchShake.isChecked = prefs.getBoolean("screen_shake", true)

        val seekListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val key = when (seekBar.id) {
                    R.id.seek_master -> "master_volume"
                    R.id.seek_music -> "music_volume"
                    R.id.seek_sfx -> "sfx_volume"
                    else -> return
                }
                prefs.edit().putInt(key, progress).apply()
                SoundManager.loadSettings(this@SettingsActivity)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

        binding.seekMaster.setOnSeekBarChangeListener(seekListener)
        binding.seekMusic.setOnSeekBarChangeListener(seekListener)
        binding.seekSfx.setOnSeekBarChangeListener(seekListener)

        binding.switchFps.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("show_fps", checked).apply()
        }

        binding.switchShake.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("screen_shake", checked).apply()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Progress")
                .setMessage("This will erase all high scores and achievements. Are you sure?")
                .setPositiveButton("Reset") { _, _ ->
                    getSharedPreferences("tower_defense_save", Context.MODE_PRIVATE)
                        .edit().clear().apply()
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
