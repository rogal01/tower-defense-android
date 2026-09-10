package com.example.myapp

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.myapp.game.SfxType
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

object SoundManager {

    var masterVolume: Float = 0.8f
        private set
    var musicVolume: Float = 0.0f
        private set
    var sfxVolume: Float = 0.8f
        private set

    val effectiveSfx: Float get() = masterVolume * sfxVolume
    val effectiveMusic: Float get() = masterVolume * musicVolume

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SfxType, Int>()
    @Volatile private var initialized = false

    // Throttle: don't spam identical sound faster than every 35ms
    private val lastPlayTime = mutableMapOf<SfxType, Long>()
    private const val MIN_INTERVAL_MS = 35L

    private const val SAMPLE_RATE = 44100

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("tower_defense_settings", Context.MODE_PRIVATE)
        masterVolume = prefs.getInt("master_volume", 80) / 100f
        musicVolume = prefs.getInt("music_volume", 0) / 100f
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
        cacheContext = context.applicationContext
        loadSettings(context)

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(12).setAudioAttributes(attrs).build()

        val cacheDir = File(context.cacheDir, "sfx_v4")
        cacheDir.mkdirs()

        for (sfx in SfxType.entries) {
            val wavFile = File(cacheDir, "${sfx.name}.wav")
            if (!wavFile.exists() || wavFile.length() < 100) {
                val samples = generateSamples(sfx)
                writeWav(wavFile, samples)
            }
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

        // Organic micro-pitch modulation to eliminate acoustic fatigue on rapid sound events
        val pitch = when (sfx) {
            SfxType.ARROW_FIRE, SfxType.MAGIC_FIRE, SfxType.POISON_FIRE,
            SfxType.TESLA_FIRE, SfxType.ICE_FIRE, SfxType.FLAME_FIRE,
            SfxType.NECRO_FIRE, SfxType.BALLISTA_FIRE,
            SfxType.PLAYER_ATTACK, SfxType.ENEMY_DIE, SfxType.BASE_HIT -> {
                0.94f + (Math.random() * 0.12).toFloat()
            }
            SfxType.WEATHER_THUNDER -> {
                0.88f + (Math.random() * 0.24).toFloat()
            }
            else -> 1.0f
        }

        pool.play(id, vol, vol, 1, 0, pitch)
    }

    private var cacheContext: Context? = null

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        initialized = false
        cacheContext?.let { ctx ->
            val sfxDir = File(ctx.cacheDir, "sfx_v3")
            if (sfxDir.exists()) sfxDir.deleteRecursively()
        }
        cacheContext = null
    }

    // ─── High-Fidelity Procedural Sound Synthesis ───

    internal fun generateSamples(sfx: SfxType): ShortArray {
        return when (sfx) {
            // Towers
            SfxType.ARROW_FIRE -> bowShot(0.08, 1400.0, 0.45)
            SfxType.MAGIC_FIRE -> magicOrb(0.12, 650.0, 0.4)
            SfxType.CANNON_FIRE -> explosiveBlast(0.18, 90.0, 0.75)
            SfxType.POISON_FIRE -> acidSpit(0.10, 420.0, 0.38)
            SfxType.TESLA_FIRE -> electricArc(0.11, 0.55)
            SfxType.ICE_FIRE -> crystalFrost(0.13, 2400.0, 0.35)
            SfxType.FLAME_FIRE -> flameBurst(0.14, 250.0, 0.5)
            SfxType.NECRO_FIRE -> darkPulse(0.12, 130.0, 0.45)
            SfxType.BALLISTA_FIRE -> explosiveBlast(0.15, 120.0, 0.65)
            SfxType.VORTEX_FIRE -> vortexSwirl(0.14, 0.4)
            SfxType.HEALER_FIRE -> harpChime(0.22, doubleArrayOf(523.25, 659.25, 783.99), 0.35)

            // Combat & Enemies
            SfxType.ENEMY_DIE -> popCrunch(0.06, 520.0, 0.4)
            SfxType.BOSS_APPEAR -> epicHorn(0.45, 110.0, 0.7)
            SfxType.BASE_HIT -> heavyImpact(0.10, 70.0, 0.65)
            SfxType.PLAYER_ATTACK -> swordSlash(0.06, 0.35)

            // Powers
            SfxType.POWER_FIREBALL -> flameBurst(0.22, 180.0, 0.65)
            SfxType.POWER_FREEZE -> crystalFrost(0.26, 3200.0, 0.5)
            SfxType.POWER_HEAL -> harpChime(0.32, doubleArrayOf(440.0, 554.37, 659.25, 880.0), 0.45)
            SfxType.POWER_LIGHTNING -> electricArc(0.18, 0.75)

            // Progression & Fanfares
            SfxType.WAVE_START -> battleCall(0.28, 330.0, 0.55)
            SfxType.WAVE_COMPLETE -> harpChime(0.35, doubleArrayOf(523.25, 659.25, 783.99, 1046.50), 0.5)
            SfxType.COMBO -> harpChime(0.12, doubleArrayOf(659.25, 880.0), 0.4)
            SfxType.ACHIEVEMENT -> harpChime(0.45, doubleArrayOf(587.33, 739.99, 880.0, 1174.66), 0.55)
            SfxType.GAME_OVER -> defeatDrone(1.45, 0.75)
            SfxType.VICTORY -> triumphalFanfare(1.35, 0.75)

            // Management & Economy
            SfxType.TOWER_PLACE -> snapThud(0.08, 220.0, 0.5)
            SfxType.TOWER_UPGRADE -> powerAscend(0.18, 400.0, 1400.0, 0.45)
            SfxType.TOWER_SELL -> coinJingle(0.15, 0.45)
            SfxType.TOWER_ABILITY -> powerAscend(0.20, 300.0, 1800.0, 0.55)
            SfxType.DIAMOND_DROP -> gemRing(0.22, 1760.0, 0.45)
            SfxType.PLAYER_UPGRADE -> powerAscend(0.20, 440.0, 1320.0, 0.45)
            SfxType.UI_CLICK -> tactileClick(0.025, 480.0, 0.3)

            // Boss abilities
            SfxType.BOSS_CHARGE -> flameBurst(0.16, 220.0, 0.55)
            SfxType.BOSS_SUMMON -> vortexSwirl(0.18, 0.45)
            SfxType.BOSS_HEAL -> harpChime(0.20, doubleArrayOf(440.0, 554.37), 0.35)
            SfxType.BOSS_AOE -> explosiveBlast(0.22, 65.0, 0.7)
            SfxType.BOSS_SHIELD -> crystalFrost(0.18, 1800.0, 0.4)
            SfxType.BOSS_ROAR -> epicHorn(0.35, 95.0, 0.65)
            SfxType.BOSS_TELEPORT -> vortexSwirl(0.12, 0.45)
            SfxType.BOSS_DRAIN -> acidSpit(0.14, 300.0, 0.45)
            SfxType.BOSS_QUAKE -> heavyImpact(0.25, 50.0, 0.75)
            SfxType.BOSS_SPLIT -> popCrunch(0.12, 380.0, 0.5)

            // Weather events & Special combat
            SfxType.WEATHER_THUNDER -> thunderClap(0.75, 55.0, 0.85)
            SfxType.WEATHER_BLOOD_MOON -> bloodMoonDrone(0.65, 0.70)
            SfxType.WEATHER_ECLIPSE -> astralChime(0.55, 0.60)
            SfxType.HERO_SPECIAL -> bladeClash(0.20, 0.65)
            SfxType.REWARD_CHEST -> rewardChime(0.48, 0.65)
            SfxType.STUN_ZAP -> electricStun(0.14, 0.55)

            // Fusion reactions & Diamond milestones
            SfxType.FUSION_ELEMENTAL -> fusionElemental(0.32, 0.70)
            SfxType.FUSION_ARCANE -> fusionArcane(0.35, 0.65)
            SfxType.FUSION_DARK -> fusionDark(0.38, 0.75)
            SfxType.FUSION_SIEGE -> fusionSiege(0.40, 0.80)
            SfxType.DIAMOND_CHEST -> diamondChest(0.55, 0.70)
        }
    }

    // ─── Sound Design Synthesis Engines ───

    /** Bow release string snap with quick decaying air whoosh */
    private fun bowShot(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(101)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            // Sharp initial string pluck then rapid decay
            val pluckEnv = exp(-frac * 18.0)
            val whooshEnv = sin(PI * frac).pow(2.0) * 0.4
            val stringTone = sin(2.0 * PI * freq * t) + 0.4 * sin(4.0 * PI * freq * t)
            val noise = (rng.nextDouble() * 2.0 - 1.0) * whooshEnv
            val s = (stringTone * pluckEnv + noise) * vol
            out[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Warm mystical orb with shimmering upper octave */
    private fun magicOrb(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * exp(-frac * 2.5) * vol
            val mod = sin(2.0 * PI * 18.0 * t) * 40.0
            val s = sin(2.0 * PI * (freq + mod) * t) * 0.7 +
                    sin(2.0 * PI * (freq * 2.01) * t) * 0.3
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Punchy explosive kick with saturated sub-bass rumble */
    private fun explosiveBlast(dur: Double, startFreq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(202)
        var lpNoise = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val t = i.toDouble() / SAMPLE_RATE
            val env = (1.0 - frac).pow(2.2) * vol
            // Pitch drops rapidly like a bass drum
            val curFreq = startFreq * (1.0 - frac * 0.6)
            val sub = sin(2.0 * PI * curFreq * t)
            // Low-passed distorted blast noise
            val raw = rng.nextDouble() * 2.0 - 1.0
            lpNoise += 0.12 * (raw - lpNoise)
            val s = (sub * 0.65 + lpNoise * 0.65).coerceIn(-1.0, 1.0) * env
            out[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Electric arc with jittered micro-discharges */
    private fun electricArc(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(303)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = sin(PI * frac).pow(0.5) * vol
            val buzzFreq = 120.0 + rng.nextDouble() * 1800.0
            phase += 2.0 * PI * buzzFreq / SAMPLE_RATE
            val square = if (sin(phase) > 0.1) 0.6 else -0.6
            val crackle = (rng.nextDouble() * 2.0 - 1.0) * 0.4
            val s = (square + crackle) * env
            out[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Crystal frost shimmer */
    private fun crystalFrost(dur: Double, centerFreq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(404)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = exp(-frac * 4.0) * vol
            val mod = (rng.nextDouble() - 0.5) * 400.0
            val s1 = sin(2.0 * PI * (centerFreq + mod) * t) * 0.5
            val s2 = sin(2.0 * PI * (centerFreq * 1.5 + mod * 0.5) * t) * 0.35
            val s3 = sin(2.0 * PI * (centerFreq * 2.0) * t) * 0.15
            out[i] = ((s1 + s2 + s3) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Turbulent flame roar */
    private fun flameBurst(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(505)
        var lp = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * vol
            val raw = rng.nextDouble() * 2.0 - 1.0
            lp += 0.18 * (raw - lp)
            val rumble = sin(2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)) * 0.4
            val s = (lp * 0.7 + rumble) * env
            out[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Dark nether pulse with descending formant */
    private fun darkPulse(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = (1.0 - frac).pow(1.5) * vol
            val f = freq * (1.0 - frac * 0.4)
            val sub = sin(2.0 * PI * f * t) * 0.7 + sin(2.0 * PI * f * 2.5 * t) * 0.3
            out[i] = (sub * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Acid droplet pop */
    private fun acidSpit(dur: Double, baseFreq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * vol
            val curFreq = baseFreq + sin(PI * frac * 3.0) * 260.0
            val s = sin(2.0 * PI * curFreq * t)
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Ethereal vortex sweep */
    private fun vortexSwirl(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * vol
            val freq = 300.0 + sin(PI * frac) * 900.0
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val s = sin(phase) * 0.8 + sin(phase * 1.5) * 0.2
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Pure celestial harp arpeggio */
    private fun harpChime(dur: Double, notes: DoubleArray, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val noteDurSamples = n / notes.size
        for ((idx, noteFreq) in notes.withIndex()) {
            val start = idx * noteDurSamples
            for (i in 0 until (n - start)) {
                val t = i.toDouble() / SAMPLE_RATE
                val frac = i.toDouble() / (n - start)
                val env = exp(-frac * 5.0) * vol
                val tone = sin(2.0 * PI * noteFreq * t) * 0.75 +
                           sin(2.0 * PI * noteFreq * 2.0 * t) * 0.25
                val curVal = out[start + i].toInt()
                val addVal = (tone * env * 32767).toInt()
                out[start + i] = (curVal + addVal).coerceIn(-32768, 32767).toShort()
            }
        }
        applyDeclick(out)
        return out
    }

    /** Tactile haptic micro-click (warm 450Hz drop, no harsh treble) */
    private fun tactileClick(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = (1.0 - frac).pow(3.0) * vol
            val f = freq * (1.0 - frac * 0.6)
            out[i] = (sin(2.0 * PI * f * t) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Satisfying enemy defeat pop */
    private fun popCrunch(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(606)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = exp(-frac * 14.0) * vol
            val pitch = freq * (1.0 - frac * 0.7)
            val tone = sin(2.0 * PI * pitch * t)
            val crackle = (rng.nextDouble() * 2.0 - 1.0) * 0.3
            val s = (tone * 0.8 + crackle) * env
            out[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Powerful battle horn */
    private fun epicHorn(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val attack = (frac * 12.0).coerceAtMost(1.0)
            val decay = (1.0 - frac).coerceAtMost(1.0)
            val env = attack * decay * vol
            val brass = sin(2.0 * PI * freq * t) * 0.6 +
                        sin(4.0 * PI * freq * t) * 0.25 +
                        sin(6.0 * PI * freq * t) * 0.15
            out[i] = (brass * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Heavy ground impact thud */
    private fun heavyImpact(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = (1.0 - frac).pow(2.5) * vol
            val f = freq * (1.0 - frac * 0.4)
            out[i] = (sin(2.0 * PI * f * t) * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Quick sword whoosh */
    private fun swordSlash(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(707)
        var lp = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * vol
            val raw = rng.nextDouble() * 2.0 - 1.0
            lp += 0.25 * (raw - lp)
            out[i] = (lp * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Battle trumpet call */
    private fun battleCall(dur: Double, freq: Double, vol: Double): ShortArray {
        return epicHorn(dur, freq, vol)
    }

    /** Building construction thud */
    private fun snapThud(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = exp(-frac * 12.0) * vol
            val s = sin(2.0 * PI * freq * t) * 0.8 + sin(4.0 * PI * freq * t) * 0.2
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Ascending harmonic sweep for upgrade */
    private fun powerAscend(dur: Double, startF: Double, endF: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * vol
            val f = startF + (endF - startF) * frac.pow(1.5)
            phase += 2.0 * PI * f / SAMPLE_RATE
            val s = sin(phase) * 0.75 + sin(phase * 2.0) * 0.25
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Dual crystal coin jingle */
    private fun coinJingle(dur: Double, vol: Double): ShortArray {
        return harpChime(dur, doubleArrayOf(1567.98, 2093.00), vol)
    }

    /** Sparkling diamond / gem ring */
    private fun gemRing(dur: Double, freq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = exp(-frac * 4.5) * vol
            val s = sin(2.0 * PI * freq * t) * 0.7 + sin(2.0 * PI * (freq * 2.5) * t) * 0.3
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Powerful atmospheric thunderclap with sharp lightning snap and rolling sub-bass rumble */
    private fun thunderClap(dur: Double, subFreq: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(404)
        var lpNoise = 0.0
        var lpRumble = 0.0
        var phase1 = 0.0
        var phase2 = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n

            // Initial supersonic lightning strike snap (first 35ms)
            val snapEnv = if (t < 0.035) {
                (1.0 - t / 0.035).pow(2.0) * (rng.nextDouble() * 2.0 - 1.0)
            } else 0.0

            // Rolling thunder rumble envelope (swells slightly, then long decay)
            val rumbleEnv = if (t < 0.05) (t / 0.05) else (1.0 - frac).pow(1.6)

            // Multi-frequency low-pitched rolling bass
            val f1 = subFreq * (1.0 + 0.25 * sin(2.0 * PI * 3.5 * t))
            val f2 = subFreq * 1.5 * (1.0 - 0.2 * frac)
            phase1 += 2.0 * PI * f1 / SAMPLE_RATE
            phase2 += 2.0 * PI * f2 / SAMPLE_RATE
            val sub = sin(phase1) * 0.5 + sin(phase2) * 0.35

            // Low-pass filtered chaotic noise rumble
            val rawNoise = rng.nextDouble() * 2.0 - 1.0
            lpNoise += 0.06 * (rawNoise - lpNoise)
            lpRumble += 0.04 * (lpNoise - lpRumble)

            val s = (snapEnv * 0.6 + (sub * 0.55 + lpRumble * 0.45) * rumbleEnv).coerceIn(-1.0, 1.0) * vol
            out[i] = (s * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Ominous dark spectral choir drone with slow tremolo */
    private fun bloodMoonDrone(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val d2 = 73.42
        val d3 = 146.83
        val f3 = 174.61
        val ab3 = 207.65 // Diminished fifth tension
        var phaseBass = 0.0
        var phaseD = 0.0
        var phaseF = 0.0
        var phaseAb = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = if (frac < 0.2) (frac / 0.2) else (1.0 - frac).pow(1.2)
            val tremolo = 0.75 + 0.25 * sin(2.0 * PI * 4.2 * t)

            phaseBass += 2.0 * PI * d2 / SAMPLE_RATE
            phaseD += 2.0 * PI * d3 / SAMPLE_RATE
            phaseF += 2.0 * PI * f3 / SAMPLE_RATE
            phaseAb += 2.0 * PI * ab3 / SAMPLE_RATE

            val s = (sin(phaseBass) * 0.4 +
                     sin(phaseD) * 0.25 +
                     sin(phaseF) * 0.22 +
                     sin(phaseAb) * 0.18) * tremolo * env * vol
            out[i] = (s.coerceIn(-1.0, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Crystalline celestial chime cascade with ascending astral shimmer */
    private fun astralChime(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val freqs = doubleArrayOf(1046.50, 1318.51, 1567.98, 2093.00, 2637.02)
        val delays = doubleArrayOf(0.0, 0.06, 0.12, 0.18, 0.24)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            var s = 0.0
            for (k in freqs.indices) {
                val dt = t - delays[k]
                if (dt > 0.0) {
                    val chimeEnv = exp(-dt * 6.5) * (sin(PI * (dt * 18.0).coerceAtMost(0.5)))
                    val chimeTone = sin(2.0 * PI * freqs[k] * dt) + 0.3 * sin(2.0 * PI * (freqs[k] * 2.02) * dt)
                    s += chimeTone * chimeEnv * 0.32
                }
            }
            val mixed = (s * vol).coerceIn(-1.0, 1.0)
            out[i] = (mixed * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Heavy metallic blade strike with ringing inharmonic steel resonance */
    private fun bladeClash(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(505)
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val impactEnv = exp(-frac * 14.0)
            val ringEnv = exp(-frac * 5.0)

            val bassHit = sin(2.0 * PI * (120.0 * (1.0 - frac * 0.7)) * t) * impactEnv * 0.45
            val ringTone = (sin(2.0 * PI * 1820.0 * t) * 0.35 +
                            sin(2.0 * PI * 2740.0 * t) * 0.25 +
                            sin(2.0 * PI * 4150.0 * t) * 0.15) * ringEnv
            val slashNoise = (rng.nextDouble() * 2.0 - 1.0) * exp(-frac * 28.0) * 0.3

            val s = (bassHit + ringTone + slashNoise) * vol
            out[i] = (s.coerceIn(-1.0, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Glorious upward arpeggiated victory/chest fanfare */
    private fun rewardChime(dur: Double, vol: Double): ShortArray {
        return harpChime(dur, doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51), vol)
    }

    /** High-frequency rapid electrical zap and paralyzing discharge */
    private fun electricStun(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(606)
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = (1.0 - frac).pow(0.8) * vol
            val f = 1000.0 + sin(2.0 * PI * 60.0 * t) * 600.0
            phase += 2.0 * PI * f / SAMPLE_RATE
            val pulse = if (sin(phase) > 0.0) 0.55 else -0.55
            val zapCrack = (rng.nextDouble() * 2.0 - 1.0) * 0.35
            val s = (pulse + zapCrack) * env
            out[i] = (s.coerceIn(-1.0, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Elemental fusion flare: rising fiery sweep with crackling explosive tail */
    private fun fusionElemental(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(801)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = sin(PI * frac).pow(0.6) * exp(-frac * 3.0) * vol
            val freq = 550.0 * (1.0 - frac * 0.7)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val tone = sin(phase) + 0.3 * sin(phase * 2.0)
            val noise = (rng.nextDouble() * 2.0 - 1.0) * frac * 0.6
            val s = (tone * 0.7 + noise) * env
            out[i] = (s.coerceIn(-1.0, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Arcane fusion: shimmering celestial arpeggio with high resonance */
    private fun fusionArcane(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val chord = doubleArrayOf(659.25, 880.0, 1046.50, 1318.51) // E5, A5, C6, E6
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * exp(-frac * 2.2) * vol
            var s = 0.0
            for ((idx, freq) in chord.withIndex()) {
                val noteDelay = idx * 0.05
                if (t >= noteDelay) {
                    val noteFrac = (t - noteDelay) / (dur - noteDelay)
                    val noteEnv = exp(-noteFrac * 6.0)
                    s += sin(2.0 * PI * freq * (t - noteDelay)) * noteEnv * 0.25
                }
            }
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Dark/Void fusion: sub-bass implosion with heavy distortion */
    private fun fusionDark(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = sin(PI * frac) * exp(-frac * 2.5) * vol
            val freq = 90.0 * (1.0 - frac * 0.6)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val sRaw = sin(phase) + 0.4 * sin(phase * 1.5)
            val distorted = tanh(sRaw * 2.2)
            out[i] = (distorted * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Siege/Explosion fusion: thunderous ground slam with heavy low-end impact */
    private fun fusionSiege(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(909)
        var phase = 0.0
        for (i in 0 until n) {
            val frac = i.toDouble() / n
            val env = exp(-frac * 4.0) * vol
            val freq = 120.0 * exp(-frac * 6.0) + 40.0
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val boom = sin(phase) * 0.7
            val crunch = (rng.nextDouble() * 2.0 - 1.0) * exp(-frac * 12.0) * 0.5
            val s = (boom + crunch) * env
            out[i] = (s.coerceIn(-1.0, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Diamond chest: triumphant crystal chime fanfare */
    private fun diamondChest(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val freqs = doubleArrayOf(1046.50, 1318.51, 1567.98, 2093.00) // C6, E6, G6, C7
        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n
            val env = sin(PI * frac).pow(0.5) * exp(-frac * 1.8) * vol
            var s = 0.0
            for ((idx, f) in freqs.withIndex()) {
                val delay = idx * 0.06
                if (t >= delay) {
                    val noteFrac = (t - delay) / (dur - delay)
                    val noteEnv = exp(-noteFrac * 5.0)
                    s += (sin(2.0 * PI * f * (t - delay)) + 0.3 * sin(2.0 * PI * f * 2.0 * (t - delay))) * noteEnv * 0.25
                }
            }
            out[i] = (s * env * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Smooth linear fade in (32 samples) and fade out (64 samples) to prevent pops */
    private fun applyDeclick(samples: ShortArray) {
        val fadeIn = min(64, samples.size / 4)
        for (i in 0 until fadeIn) {
            val factor = i.toDouble() / fadeIn
            samples[i] = (samples[i] * factor).toInt().toShort()
        }
        val fadeOut = min(128, samples.size / 4)
        for (i in 0 until fadeOut) {
            val factor = i.toDouble() / fadeOut
            val idx = samples.size - 1 - i
            samples[idx] = (samples[idx] * factor).toInt().toShort()
        }
    }

    /** Epic, uplifting multi-phrase victory fanfare with brass chords, sparkling bells, and noble sustained harmony */
    private fun triumphalFanfare(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val notes = doubleArrayOf(261.63, 329.63, 392.00, 523.25)
        val noteStarts = doubleArrayOf(0.0, 0.12, 0.24, 0.38)
        val chordFreqs = doubleArrayOf(261.63, 392.00, 523.25, 659.25, 783.99)
        val bellFreqs = doubleArrayOf(1046.50, 1318.51, 1567.98, 2093.00)

        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            // 1. Initial fanfare arpeggio notes
            for (k in 0..2) {
                val dt = t - noteStarts[k]
                if (dt in 0.0..0.22) {
                    val env = (1.0 - dt / 0.22).pow(0.8) * sin((dt / 0.02).coerceAtMost(1.0) * (PI / 2))
                    val p = 2.0 * PI * notes[k] * dt
                    val brass = sin(p) + 0.35 * sin(2.0 * p) + 0.15 * sin(3.0 * p)
                    sample += brass * env * 0.28
                }
            }

            // 2. Grand climax chord (starts at 0.38s)
            val dtChord = t - noteStarts[3]
            if (dtChord > 0.0) {
                val chordDur = dur - noteStarts[3]
                val frac = dtChord / chordDur
                val attack = (dtChord / 0.04).coerceAtMost(1.0)
                val decay = (1.0 - frac).pow(1.1)
                val chordEnv = attack * decay

                var chordSum = 0.0
                for (cf in chordFreqs) {
                    val p = 2.0 * PI * cf * dtChord
                    val voice = sin(p) + 0.38 * sin(2.0 * p) + 0.18 * sin(3.0 * p) + 0.08 * sin(4.0 * p)
                    chordSum += voice
                }
                sample += (chordSum / chordFreqs.size) * chordEnv * 0.55

                // 3. Shimmering high bells at climax
                var bellSum = 0.0
                for (bf in bellFreqs) {
                    val bellEnv = exp(-dtChord * 4.2)
                    bellSum += sin(2.0 * PI * bf * dtChord) * bellEnv
                }
                sample += bellSum * 0.18
            }

            val mixed = (sample * vol).coerceIn(-1.0, 1.0)
            out[i] = (mixed * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Dark, heavy defeat collapse with falling sub-bass, somber minor chord, and melancholic resonant reverb tail */
    private fun defeatDrone(dur: Double, vol: Double): ShortArray {
        val n = (SAMPLE_RATE * dur).toInt()
        val out = ShortArray(n)
        val rng = java.util.Random(999)
        var phaseBass = 0.0
        var phaseD = 0.0
        var phaseF = 0.0
        var phaseA = 0.0
        var phaseToll = 0.0

        for (i in 0 until n) {
            val t = i.toDouble() / SAMPLE_RATE
            val frac = i.toDouble() / n

            val pitchDrop = 1.0 - 0.18 * (t / 0.70).coerceAtMost(1.0)
            val env = if (frac < 0.05) (frac / 0.05) else (1.0 - frac).pow(1.3)

            phaseBass += 2.0 * PI * (73.42 * pitchDrop) / SAMPLE_RATE // D2
            phaseD += 2.0 * PI * (146.83 * pitchDrop) / SAMPLE_RATE    // D3
            phaseF += 2.0 * PI * (174.61 * pitchDrop) / SAMPLE_RATE    // F3
            phaseA += 2.0 * PI * (220.00 * pitchDrop) / SAMPLE_RATE    // A3

            val minorChord = sin(phaseBass) * 0.45 +
                             sin(phaseD) * 0.28 +
                             sin(phaseF) * 0.22 +
                             sin(phaseA) * 0.18

            var toll = 0.0
            if (t > 0.35) {
                val dt = t - 0.35
                phaseToll += 2.0 * PI * 116.54 / SAMPLE_RATE // Bb2
                val tollEnv = exp(-dt * 3.0)
                toll = (sin(phaseToll) + 0.3 * sin(phaseToll * 2.76) + 0.15 * sin(phaseToll * 5.4)) * tollEnv * 0.35
            }

            val rumble = (rng.nextDouble() * 2.0 - 1.0) * exp(-frac * 3.5) * 0.08

            val s = (minorChord + toll + rumble) * env * vol
            out[i] = (s.coerceIn(-1.0, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
        applyDeclick(out)
        return out
    }

    /** Write ShortArray PCM data as standard 16-bit 44.1kHz WAV */
    private fun writeWav(file: File, samples: ShortArray) {
        val dataSize = samples.size * 2
        val buf = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray())
        buf.putInt(36 + dataSize)
        buf.put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray())
        buf.putInt(16)
        buf.putShort(1) // PCM
        buf.putShort(1) // Mono
        buf.putInt(SAMPLE_RATE)
        buf.putInt(SAMPLE_RATE * 2)
        buf.putShort(2)
        buf.putShort(16)
        buf.put("data".toByteArray())
        buf.putInt(dataSize)
        for (s in samples) buf.putShort(s)

        FileOutputStream(file).use { it.write(buf.array()) }
    }
}
