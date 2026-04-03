package com.example.myapp

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.*

object MusicManager {
    private var audioTrack: AudioTrack? = null
    @Volatile private var playing = false
    private var thread: Thread? = null
    private const val SAMPLE_RATE = 22050

    fun start() {
        if (playing) return
        playing = true

        val bufSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(SAMPLE_RATE) // at least 1s buffer

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
            val chunkSize = SAMPLE_RATE / 4   // 250ms chunks
            val buf = ShortArray(chunkSize)
            var phase0 = 0.0
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var sampleIdx = 0L

            // Ambient music: layered sine drones in Dm pentatonic
            val baseFreqs = doubleArrayOf(73.42, 110.0, 146.83, 174.61) // D2, A2, D3, F3
            val lfoRate = 0.15  // slow oscillation

            while (playing) {
                val vol = SoundManager.effectiveMusic.toDouble()
                if (vol <= 0.0) {
                    // Silent — write zeros to keep stream alive
                    buf.fill(0)
                    track.write(buf, 0, chunkSize)
                    continue
                }

                for (i in 0 until chunkSize) {
                    val t = (sampleIdx + i).toDouble() / SAMPLE_RATE
                    val lfo = 0.5 + 0.5 * sin(2.0 * PI * lfoRate * t)
                    val lfo2 = 0.5 + 0.5 * sin(2.0 * PI * 0.08 * t)

                    // 4 layered tones with slow volume modulation
                    val s0 = sin(phase0) * 0.25 * lfo
                    val s1 = sin(phase1) * 0.15 * lfo2
                    val s2 = sin(phase2) * 0.12 * (1.0 - lfo * 0.5)
                    val s3 = sin(phase3) * 0.08 * lfo

                    val mix = (s0 + s1 + s2 + s3) * vol
                    buf[i] = (mix * 32767).toInt().coerceIn(-32768, 32767).toShort()

                    phase0 += 2.0 * PI * baseFreqs[0] / SAMPLE_RATE
                    phase1 += 2.0 * PI * baseFreqs[1] / SAMPLE_RATE
                    phase2 += 2.0 * PI * baseFreqs[2] / SAMPLE_RATE
                    phase3 += 2.0 * PI * baseFreqs[3] / SAMPLE_RATE
                }
                sampleIdx += chunkSize
                phase0 = phase0 % (2.0 * PI)
                phase1 = phase1 % (2.0 * PI)
                phase2 = phase2 % (2.0 * PI)
                phase3 = phase3 % (2.0 * PI)
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
