package com.example.myapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import android.graphics.Color
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.databinding.ActivitySettingsBinding
import com.example.myapp.game.SfxType
import org.json.JSONObject

class SettingsActivity : ImmersiveActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri ?: return@registerForActivityResult
        try {
            val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
            val json = JSONObject()
            for ((k, v) in prefs.all) {
                when (v) {
                    is Int -> json.put(k, v)
                    is Boolean -> json.put(k, v)
                    is Long -> json.put(k, v)
                    is Float -> json.put(k, v.toDouble())
                    is String -> json.put(k, v)
                }
            }
            contentResolver.openOutputStream(uri)?.use { it.write(json.toString(2).toByteArray()) }
            Toast.makeText(this, "✅ Save exported!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@registerForActivityResult
        try {
            val text = contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return@registerForActivityResult
            val json = JSONObject(text)
            val prefs = getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            for (key in json.keys()) {
                when (val v = json.get(key)) {
                    is Int -> editor.putInt(key, v)
                    is Boolean -> editor.putBoolean(key, v)
                    is Long -> editor.putLong(key, v)
                    is Double -> editor.putFloat(key, v.toFloat())
                    is String -> editor.putString(key, v)
                }
            }
            editor.apply()
            Toast.makeText(this, "✅ Save imported!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Import failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        GameStrings.init(this)

        val prefs = getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)

        // Load saved values
        binding.seekMaster.progress = prefs.getInt("master_volume", 80)
        binding.seekMusic.progress = prefs.getInt("music_volume", 70)
        binding.seekSfx.progress = prefs.getInt("sfx_volume", 80)
        binding.switchFps.isChecked = prefs.getBoolean("show_fps", false)
        binding.switchShake.isChecked = prefs.getBoolean("screen_shake", true)
        binding.switchVibrate.isChecked = prefs.getBoolean("vibrations_enabled", true)

        var currentFps = prefs.getInt("target_fps", 120)
        fun updateFpsButtons() {
            binding.btnFps120.setBackgroundResource(if (currentFps == 120) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.btnFps120.setTextColor(if (currentFps == 120) Color.WHITE else Color.parseColor("#B0BEC5"))

            binding.btnFps60.setBackgroundResource(if (currentFps == 60) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.btnFps60.setTextColor(if (currentFps == 60) Color.WHITE else Color.parseColor("#B0BEC5"))

            binding.btnFps30.setBackgroundResource(if (currentFps == 30) R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.btnFps30.setTextColor(if (currentFps == 30) Color.WHITE else Color.parseColor("#B0BEC5"))
        }
        updateFpsButtons()

        binding.btnFps120.setOnClickListener {
            currentFps = 120
            prefs.edit().putInt("target_fps", 120).apply()
            updateFpsButtons()
            SoundManager.play(SfxType.UI_CLICK)
        }
        binding.btnFps60.setOnClickListener {
            currentFps = 60
            prefs.edit().putInt("target_fps", 60).apply()
            updateFpsButtons()
            SoundManager.play(SfxType.UI_CLICK)
        }
        binding.btnFps30.setOnClickListener {
            currentFps = 30
            prefs.edit().putInt("target_fps", 30).apply()
            updateFpsButtons()
            SoundManager.play(SfxType.UI_CLICK)
        }

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

        binding.switchVibrate.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("vibrations_enabled", checked).apply()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnExportSave.setOnClickListener {
            exportLauncher.launch("tower_defense_save.json")
        }

        binding.btnImportSave.setOnClickListener {
            importLauncher.launch(arrayOf("application/json"))
        }

        binding.btnReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(GameStrings.resetTitle)
                .setMessage(GameStrings.resetMessage)
                .setPositiveButton(GameStrings.resetBtn) { _, _ ->
                    getSharedPreferences("tower_defense_prefs", Context.MODE_PRIVATE)
                        .edit().clear().apply()
                    finish()
                }
                .setNegativeButton(GameStrings.btnCancel, null)
                .show()
        }
    }
}
