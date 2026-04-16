package com.example.myapp

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.SoundPool
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

enum class SfxType {
    ARROW_FIRE, MAGIC_FIRE, CANNON_FIRE, POISON_FIRE, TESLA_FIRE, ICE_FIRE,
    ENEMY_DIE, BOSS_APPEAR,
    POWER_FIREBALL, POWER_FREEZE, POWER_HEAL, POWER_LIGHTNING,
    WAVE_START, WAVE_COMPLETE, COMBO, ACHIEVEMENT,
    GAME_OVER, VICTORY,
    TOWER_PLACE, TOWER_UPGRADE, TOWER_SELL, TOWER_ABILITY,
    BASE_HIT, PLAYER_ATTACK,
    BOSS_CHARGE, BOSS_SUMMON, BOSS_HEAL, BOSS_AOE,
    BOSS_SHIELD, BOSS_SCREECH, BOSS_TELEPORT, BOSS_DRAIN, BOSS_QUAKE, BOSS_SPLIT,
    DIAMOND_DROP, PLAYER_UPGRADE, UI_CLICK
}

object SoundManager {

    var masterVolume: Float = 0.8f
        private set
    var musicVolume: Float = 0.7f
        private set
    var sfxVolume: Float = 0.8f
        private set

    val effectiveSfx: Float get() = masterVolume * sfxVolume
    val effectiveMusic: Float get() = masterVolume * musicVolume

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SfxType, Int>()
    @Volatile private var initialized = false
    private var cacheDir: File? = null

    // Throttle: don't spam the same sound faster than every 40ms
    private val lastPlayTime = mutableMapOf<SfxType, Long>()
    private const val MIN_INTERVAL_MS = 40L

    private const val SAMPLE_RATE = 22050

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)
        masterVolume = prefs.getInt("master_volume", 80) / 100f
        musicVolume = prefs.getInt("music_volume", 70) / 100f
        sfxVolume = prefs.getInt("sfx_volume", 80) / 100f
    }

    fun isShakeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("screen_shake", true)
    }

    fun isShowFps(context: Context): Boolean {
        val prefs = context.getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)
        return prefs.getBoolean("show_fps", false)
    }

    @Synchronized
    fun init(context: Context) {
        if (initialized) return
        initialized = true
        loadSettings(context)

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attrs).build()

        cacheDir = File(context.cacheDir, "sfx").apply { mkdirs() }

        for (sfx in SfxType.entries) {
            val samples = generateSamples(sfx)
            val wavFile = File(cacheDir, "${sfx.name}.wav")
            writeWav(wavFile, samples)
            soundIds[sfx] = soundPool!!.load(wavFile.absolutePath, 1)
        }
    }

    fun play(sfx: SfxType) {
        val pool = soundPool ?: return
        val id = soundIds[sfx] ?: return
        val vol = effectiveSfx
        if (vol <= 0f) return

        val now = System.currentTimeMillis()
        val last = lastPlayTime[sfx] ?: 0L
        if (now - last < MIN_INTERVAL_MS) return
        lastPlayTime[sfx] = now

        pool.play(id, vol, vol, 1, 0, 1f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        initialized = false
        // Clean up cached WAV files
        cacheDir?.let { sfxDir ->
            if (sfxDir.exists()) sfxDir.deleteRecursively()
        }
        cacheDir = null
    }

    // --- Procedural sound generation ---

    private fun generateSamples(sfx: SfxType): ShortArray {
        return when (sfx) {
            SfxType.ARROW_FIRE -> chirp(0.06, 1200.0, 600.0, 0.5)
            SfxType.MAGIC_FIRE -> shimmer(0.1, 800.0, 0.4)
            SfxType.CANNON_FIRE -> boom(0.12, 120.0, 0.7)
            SfxType.POISON_FIRE -> noise(0.07, 400.0, 0.35)
            SfxType.TESLA_FIRE -> zap(0.08, 0.6)
            SfxType.ICE_FIRE -> shimmer(0.08, 2200.0, 0.35)
            SfxType.ENEMY_DIE -> chirp(0.05, 800.0, 300.0, 0.4)
            SfxType.BOSS_APPEAR -> horn(0.3, 150.0, 0.6)
            SfxType.POWER_FIREBALL -> sweep(0.15, 200.0, 900.0, 0.6)
            SfxType.POWER_FREEZE -> shimmer(0.2, 2500.0, 0.45)
            SfxType.POWER_HEAL -> chime(0.25, doubleArrayOf(523.0, 659.0, 784.0), 0.4)
            SfxType.POWER_LIGHTNING -> zap(0.12, 0.7)
            SfxType.WAVE_START -> horn(0.2, 440.0, 0.5)
            SfxType.COMBO -> chirp(0.08, 600.0, 1200.0, 0.4)
            SfxType.ACHIEVEMENT -> chime(0.3, doubleArrayOf(784.0, 988.0, 1175.0), 0.45)
            SfxType.GAME_OVER -> chime(0.35, doubleArrayOf(440.0, 349.0, 262.0), 0.5)
            SfxType.VICTORY -> chime(0.4, doubleArrayOf(523.0, 659.0, 784.0, 1047.0), 0.5)
            SfxType.TOWER_PLACE -> boom(0.06, 200.0, 0.45)
            SfxType.TOWER_UPGRADE -> sweep(0.12, 500.0, 1500.0, 0.4)
            SfxType.TOWER_SELL -> chirp(0.08, 1200.0, 400.0, 0.35)
            SfxType.TOWER_ABILITY -> sweep(0.15, 300.0, 1800.0, 0.55)
            SfxType.BASE_HIT -> boom(0.05, 80.0, 0.5)
            SfxType.PLAYER_ATTACK -> chirp(0.04, 900.0, 500.0, 0.35)
            SfxType.WAVE_COMPLETE -> chime(0.2, doubleArrayOf(523.0, 784.0), 0.35)
            SfxType.BOSS_CHARGE -> sweep(0.1, 300.0, 1200.0, 0.55)
            SfxType.BOSS_SUMMON -> shimmer(0.15, 600.0, 0.4)
            SfxType.BOSS_HEAL -> chime(0.15, doubleArrayOf(659.0, 784.0), 0.35)
            SfxType.BOSS_AOE -> boom(0.15, 100.0, 0.65)
            SfxType.BOSS_SHIELD -> chirp(0.1, 1500.0, 2500.0, 0.35)
            SfxType.BOSS_SCREECH -> horn(0.18, 180.0, 0.55)
            SfxType.BOSS_TELEPORT -> sweep(0.08, 2000.0, 400.0, 0.4)
            SfxType.BOSS_DRAIN -> sweep(0.1, 800.0, 200.0, 0.45)
            SfxType.BOSS_QUAKE -> boom(0.2, 60.0, 0.7)
            SfxType.BOSS_SPLIT -> noise(0.1, 300.0, 0.45)
            SfxType.DIAMOND_DROP -> chime(0.12, doubleArrayOf(1047.0, 1319.0), 0.35)
            SfxType.PLAYER_UPGRADE -> sweep(0.15, 400.0, 1200.0, 0.4)
            SfxType.UI_CLICK -> chirp(0.03, 1000.0, 800.0, 0.25)
        }
    }

    /** Frequency sweep (up or down chirp) */
    private fun chirp(dur: Double, freqStart: Double, freqEnd: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val freq = freqStart + (freqEnd - freqStart) * frac
            val env = (1.0 - frac) * vol
            out[i] = (sin(2.0 * PI * freq * t) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Frequency sweep (explicit up-sweep for power sounds) */
    private fun sweep(dur: Double, freqStart: Double, freqEnd: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val freq = freqStart + (freqEnd - freqStart) * frac
            val env = sin(PI * frac) * vol // bell envelope
            phase += 2.0 * PI * freq / SAMPLE_RATE
            out[i] = (sin(phase) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Deep boom (low frequency with fast decay) */
    private fun boom(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = (1.0 - frac).pow(2.0) * vol
            val f = freq * (1.0 - frac * 0.5) // pitch drops
            out[i] = (sin(2.0 * PI * f * t) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Electric zap (square wave + noise) */
    private fun zap(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(42)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = (1.0 - frac) * vol
            val freq = 150.0 + 2000.0 * (1.0 - frac)
            val square = if (sin(2.0 * PI * freq * t) > 0) 1.0 else -1.0
            val noiseVal = (rng.nextDouble() * 2.0 - 1.0) * 0.3
            out[i] = ((square * 0.7 + noiseVal) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Shimmer (high frequency with random modulation) */
    private fun shimmer(dur: Double, centerFreq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(7)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * vol
            val modFreq = centerFreq + (rng.nextDouble() - 0.5) * 600.0
            out[i] = (sin(2.0 * PI * modFreq * t) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Chime (sequential notes for achievements/victory/game-over) */
    private fun chime(dur: Double, freqs: DoubleArray, vol: Double): ShortArray {
        val totalSamples = (SAMPLE_RATE * dur).toInt()
        val noteLen = totalSamples / freqs.size
        val out = ShortArray(totalSamples)
        for ((idx, freq) in freqs.withIndex()) {
            val start = idx * noteLen
            for (i in 0 until noteLen) {
                if (start + i >= totalSamples) break
                val t = i.toDouble() / SAMPLE_RATE
                val noteFrac = i.toDouble() / noteLen
                val env = (1.0 - noteFrac).pow(0.5) * vol
                out[start + i] = (sin(2.0 * PI * freq * t) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
            }
        }
        return out
    }

    /** Sustained horn tone with attack */
    private fun horn(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val attack = (frac * 10.0).coerceAtMost(1.0)
            val decay = (1.0 - frac).coerceAtMost(1.0)
            val env = attack * decay * vol
            val s = sin(2.0 * PI * freq * t) * 0.7 + sin(4.0 * PI * freq * t) * 0.3
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Filtered noise burst */
    private fun noise(dur: Double, centerFreq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(99)
        var prev = 0.0
        val rc = 1.0 / (2.0 * PI * centerFreq)
        val dtSample = 1.0 / SAMPLE_RATE
        val alpha = dtSample / (rc + dtSample)
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = (1.0 - frac) * vol
            val raw = rng.nextDouble() * 2.0 - 1.0
            prev += alpha * (raw - prev)
            out[i] = (prev * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        return out
    }

    /** Write ShortArray PCM data as a WAV file */
    private fun writeWav(file: File, samples: ShortArray) {
        val dataSize = samples.size * 2
        val buf = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        // RIFF header
        buf.put("RIFF".toByteArray())
        buf.putInt(36 + dataSize)
        buf.put("WAVE".toByteArray())
        // fmt chunk
        buf.put("fmt ".toByteArray())
        buf.putInt(16) // chunk size
        buf.putShort(1) // PCM
        buf.putShort(1) // mono
        buf.putInt(SAMPLE_RATE)
        buf.putInt(SAMPLE_RATE * 2) // byte rate
        buf.putShort(2) // block align
        buf.putShort(16) // bits per sample
        // data chunk
        buf.put("data".toByteArray())
        buf.putInt(dataSize)
        for (s in samples) buf.putShort(s)

        FileOutputStream(file).use { it.write(buf.array()) }
    }
}
