package com.example.myapp

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.myapp.game.WeatherEvent
import kotlin.math.*

object MusicManager {

    enum class Track {
        MENU,
        BATTLE
    }

    private var audioTrack: AudioTrack? = null
    @Volatile private var playing = false
    @Volatile private var currentTrack = Track.MENU
    @Volatile var currentWeather: WeatherEvent = WeatherEvent.CLEAR
    @Volatile var isWaveActive: Boolean = false
    @Volatile var isBossPresent: Boolean = false
    private var thread: Thread? = null
    private const val SAMPLE_RATE = 44100

    // Fast 2048-entry sine wavetable for zero-CPU synthesis
    private val SINE_TABLE = FloatArray(2048) { i -> sin(i * 2.0 * PI / 2048).toFloat() }
    private inline fun fastSin(phase: Double): Float {
        val idx = ((phase / (2.0 * PI) * 2048).toInt() and 2047)
        return SINE_TABLE[idx]
    }

    fun updateGameState(weather: WeatherEvent, waveActive: Boolean, bossPresent: Boolean) {
        currentWeather = weather
        isWaveActive = waveActive
        isBossPresent = bossPresent
    }

    fun playTrack(track: Track) {
        currentTrack = track
        if (!playing) {
            start()
        }
    }

    fun start() {
        if (playing) return
        playing = true

        val bufSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(SAMPLE_RATE / 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(bufSize * 2)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()

        thread = Thread {
            val chunkSize = SAMPLE_RATE / 5   // 200ms per audio block (8820 samples)
            val buf = ShortArray(chunkSize)

            var phaseBass = 0.0
            var phasePad1 = 0.0
            var phasePad2 = 0.0
            var phaseLead = 0.0

            // Musical tempo: 105 BPM -> ~0.571s per beat -> ~25,183 samples per beat
            // 16th note = ~6,295 samples
            var beatSampleCounter = 0L
            val samplesPer16th = (SAMPLE_RATE * 60.0 / (105.0 * 4.0)).toInt()

            // Frequencies for D minor / Dorian scale
            // D2, F2, G2, A2, Bb2, C3, D3, E3, F3, G3, A3, C4, D4
            val d2 = 73.42
            val f2 = 87.31
            val g2 = 98.00
            val a2 = 110.00
            val bb2 = 116.54
            val c3 = 130.81
            val d3 = 146.83
            val f3 = 174.61
            val a3 = 220.00
            val c4 = 261.63
            val d4 = 293.66
            val e4 = 329.63
            val f4 = 349.23
            val a4 = 440.00

            // Chord progression across 4 bars (Dm -> Bb -> C -> Am)
            val padChords = arrayOf(
                doubleArrayOf(d3, f3, a3),  // Dm
                doubleArrayOf(bb2, d3, f3), // Bb
                doubleArrayOf(c3, e4 / 2.0, g2 * 2.0), // C
                doubleArrayOf(a2, c3, e4 / 2.0)  // Am
            )
            val bassNotes = doubleArrayOf(d2, bb2, c3, a2)

            // Battle melody motifs (16 steps per bar, 4 bars = 64 steps)
            val battleMelody = doubleArrayOf(
                d4, 0.0, f4, d4,   a4, 0.0, f4, 0.0,  d4, e4, f4, d4,   c4, 0.0, d4, 0.0,
                bb2 * 2, 0.0, d4, f4,  a4, 0.0, f4, 0.0,  bb2 * 2, d4, f4, a4,  g2 * 2, 0.0, f4, e4,
                c4, 0.0, e4, g2 * 2,  c4, d4, e4, 0.0,   g2 * 2, e4, d4, c4,  d4, 0.0, c4, 0.0,
                a3, 0.0, c4, e4,   a4, 0.0, g2 * 2, 0.0,  f4, e4, d4, c4,  d4, 0.0, 0.0, 0.0
            )

            // Menu melody motifs (peaceful, spacious harp arpeggios)
            val menuMelody = doubleArrayOf(
                d3, 0.0, a3, 0.0,  d4, 0.0, f4, 0.0,  a4, 0.0, f4, 0.0,  d4, 0.0, a3, 0.0,
                bb2, 0.0, f3, 0.0, bb2 * 2, 0.0, d4, 0.0, f4, 0.0, d4, 0.0, bb2 * 2, 0.0, f3, 0.0,
                c3, 0.0, g2 * 2, 0.0, c4, 0.0, e4, 0.0, g2 * 4, 0.0, e4, 0.0, c4, 0.0, g2 * 2, 0.0,
                a2, 0.0, e4 / 2, 0.0, a3, 0.0, c4, 0.0, e4, 0.0, c4, 0.0, a3, 0.0, e4 / 2, 0.0
            )

            var currentChordIdx = 0
            var step16th = 0
            var currentTargetLeadFreq = 0.0
            var currentLeadFreq = 0.0

            // Atmospheric weather synthesizers
            val atmoRng = java.util.Random(777)
            var rainLp1 = 0.0
            var rainLp2 = 0.0
            var targetRainGain = 0.0
            var currentRainGain = 0.0

            var phaseBloodDrone = 0.0
            var targetBloodGain = 0.0
            var currentBloodGain = 0.0

            var phaseAstral = 0.0
            var targetAstralGain = 0.0
            var currentAstralGain = 0.0

            while (playing) {
                val vol = SoundManager.effectiveMusic.toDouble()
                if (vol <= 0.0) {
                    buf.fill(0)
                    track.write(buf, 0, chunkSize)
                    continue
                }

                val isBattle = currentTrack == Track.BATTLE
                val melodyArr = if (isBattle) battleMelody else menuMelody

                // Weather audio targets in battle mode
                if (isBattle) {
                    targetRainGain = if (currentWeather == WeatherEvent.THUNDERSTORM) 0.08 else 0.0
                    targetBloodGain = if (currentWeather == WeatherEvent.BLOOD_MOON) 0.14 else 0.0
                    targetAstralGain = if (currentWeather == WeatherEvent.SOLAR_ECLIPSE) 0.12 else 0.0
                } else {
                    targetRainGain = 0.0
                    targetBloodGain = 0.0
                    targetAstralGain = 0.0
                }

                for (i in 0 until chunkSize) {
                    // Update musical step
                    if (beatSampleCounter % samplesPer16th == 0L) {
                        val activeStep = (step16th % 64)
                        currentChordIdx = (activeStep / 16) % 4
                        val rawMelody = melodyArr[activeStep]
                        if (rawMelody > 0.0) {
                            currentTargetLeadFreq = rawMelody
                        }
                        step16th++
                    }

                    val stepProgress = (beatSampleCounter % samplesPer16th).toDouble() / samplesPer16th
                    beatSampleCounter++

                    // Smooth portamento lead frequency glide
                    currentLeadFreq += (currentTargetLeadFreq - currentLeadFreq) * 0.005

                    // Lead envelope (gentle plucked decay)
                    val leadEnv = exp(-stepProgress * (if (isBattle) 4.0 else 2.5))

                    // Voice 1: Bass
                    val bassTarget = bassNotes[currentChordIdx]
                    val bassEnv = if (isBattle) exp(-(stepProgress % 0.5) * 6.0) else exp(-stepProgress * 3.0)
                    val sBass = fastSin(phaseBass) * bassEnv * 0.35

                    // Voice 2 & 3: Warm Pad Chords
                    val chord = padChords[currentChordIdx]
                    val sPad1 = fastSin(phasePad1) * 0.18
                    val sPad2 = fastSin(phasePad2) * 0.15

                    // Voice 4: Melody Lead (crystalline sine + soft 2nd harmonic)
                    val sLead = if (currentLeadFreq > 20.0) {
                        (fastSin(phaseLead) * 0.7f + fastSin(phaseLead * 2.0) * 0.3f) * leadEnv * (if (isBattle) 0.30 else 0.22)
                    } else 0.0

                    // Dynamic Percussion:
                    // During inter-wave preparation: calm acoustic arrangement (no kick)
                    // During active wave: driving combat kicks on 1 & 3
                    // Boss encounters: intense taiko/war-drum double-kicks
                    val sPerc = if (isBattle && isWaveActive) {
                        val isKickStep = if (isBossPresent) {
                            (step16th % 8 == 1 || step16th % 8 == 3 || step16th % 8 == 5)
                        } else {
                            (step16th % 8 == 1 || step16th % 8 == 5)
                        }
                        val kickEnv = if (isKickStep) exp(-stepProgress * (if (isBossPresent) 8.0 else 12.0)) else 0.0
                        val kickTone = fastSin(phaseBass * 0.75) * kickEnv * (if (isBossPresent) 0.36 else 0.25)
                        kickTone
                    } else 0.0

                    // Smooth slew for atmospheric weather audio gains
                    currentRainGain += (targetRainGain - currentRainGain) * 0.0003
                    currentBloodGain += (targetBloodGain - currentBloodGain) * 0.0003
                    currentAstralGain += (targetAstralGain - currentAstralGain) * 0.0003

                    // Weather Layer 1: Thunderstorm Pink-Noise Rainfall
                    val sRain = if (currentRainGain > 0.001) {
                        val rawNoise = atmoRng.nextDouble() * 2.0 - 1.0
                        rainLp1 += 0.07 * (rawNoise - rainLp1)
                        rainLp2 += 0.07 * (rainLp1 - rainLp2)
                        rainLp2 * currentRainGain
                    } else 0.0

                    // Weather Layer 2: Blood Moon Sub-Harmonic Drone (D1 36.71Hz)
                    val sBlood = if (currentBloodGain > 0.001) {
                        fastSin(phaseBloodDrone) * (0.8f + 0.2f * fastSin(phaseBloodDrone * 0.15)) * currentBloodGain
                    } else 0.0

                    // Weather Layer 3: Solar Eclipse Ethereal Astral Shimmer
                    val sAstral = if (currentAstralGain > 0.001) {
                        (fastSin(phaseAstral) * 0.6f + fastSin(phaseAstral * 1.5) * 0.4f) * currentAstralGain
                    } else 0.0

                    val mix = (sBass + sPad1 + sPad2 + sLead + sPerc + sRain + sBlood + sAstral) * vol
                    buf[i] = (mix * 32767).toInt().coerceIn(-32768, 32767).toShort()

                    // Advance oscillator phases
                    phaseBass += 2.0 * PI * bassTarget / SAMPLE_RATE
                    phasePad1 += 2.0 * PI * chord[0] / SAMPLE_RATE
                    phasePad2 += 2.0 * PI * chord[1] / SAMPLE_RATE
                    if (currentLeadFreq > 20.0) {
                        phaseLead += 2.0 * PI * currentLeadFreq / SAMPLE_RATE
                    }
                    if (currentBloodGain > 0.001) {
                        phaseBloodDrone += 2.0 * PI * 36.71 / SAMPLE_RATE
                    }
                    if (currentAstralGain > 0.001) {
                        phaseAstral += 2.0 * PI * (1174.66 + sin(beatSampleCounter * 0.0003) * 20.0) / SAMPLE_RATE
                    }
                }

                // Wrap phases to prevent precision drift
                phaseBass %= (2.0 * PI)
                phasePad1 %= (2.0 * PI)
                phasePad2 %= (2.0 * PI)
                phaseLead %= (2.0 * PI)
                phaseBloodDrone %= (2.0 * PI)
                phaseAstral %= (2.0 * PI)

                track.write(buf, 0, chunkSize)
            }
        }.apply {
            isDaemon = true
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    fun stop() {
        playing = false
        val t = thread
        thread = null
        t?.interrupt()
        try { t?.join(500) } catch (_: Exception) {}
        try {
            audioTrack?.stop()
        } catch (_: Exception) {}
        audioTrack?.release()
        audioTrack = null
    }
}
