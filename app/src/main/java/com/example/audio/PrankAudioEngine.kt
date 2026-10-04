package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class PrankSoundType(val displayName: String, val category: String, val emoji: String) {
    AIR_HORN("Воздушный горн", "Сигналы", "📢"),
    FART_CLASSIC("Классический пук", "Пуки", "💨"),
    FART_WET("Мокрый пук", "Пуки", "💦"),
    FART_SNIPER("Пук-снайпер", "Пуки", "🎯"),
    FART_EPIC("Эпический грохот", "Пуки", "🌪️"),
    POLICE_SIREN("Полиция", "Сигналы", "🚨"),
    GLASS_BREAK("Разбитое стекло", "Разрушения", "💥"),
    DOORBELL("Дверной звонок", "Быт", "🔔"),
    CAR_ALARM("Сигнализация авто", "Сигналы", "🚗"),
    SCREAMER("Жуткий крик", "Ужасы", "😱"),
    MOSQUITO("Комар в ухе", "Быт", "🦟"),
    HAIR_CLIPPER_BURST("Вжик машинки", "Инструменты", "✂️"),
    TASER_ZAP("Разряд током", "Инструменты", "⚡"),
    CARTOON_LAUGH("Смех из ситкома", "Люди", "😂"),
    PHONE_RING("Звонок телефона", "Быт", "📱")
}

class PrankAudioEngine {
    private val sampleRate = 44100
    private val soundCache = ConcurrentHashMap<PrankSoundType, ShortArray>()
    private val scope = CoroutineScope(Dispatchers.Default)

    private var activeLoopTrack: AudioTrack? = null
    private var loopJob: Job? = null
    private var activeLoopType: String? = null

    init {
        // Pre-render common one-shot sounds in background for zero-latency response
        scope.launch {
            for (type in PrankSoundType.values()) {
                if (type != PrankSoundType.HAIR_CLIPPER_BURST && type != PrankSoundType.TASER_ZAP) {
                    getOrGenerateSound(type)
                }
            }
        }
    }

    fun playSound(type: PrankSoundType, volume: Float = 1.0f) {
        scope.launch {
            try {
                val samples = getOrGenerateSound(type)
                val bufferSize = samples.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.setVolume(volume.coerceIn(0.1f, 1.0f))
                track.write(samples, 0, samples.size)
                track.play()

                // Release track after playback finished
                val durationMs = (samples.size * 1000L) / sampleRate + 200
                kotlinx.coroutines.delay(durationMs)
                try {
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startTaserContinuous(voltageLevel: Int = 1) {
        if (activeLoopType == "TASER_$voltageLevel") return
        stopLoop()
        activeLoopType = "TASER_$voltageLevel"

        loopJob = scope.launch {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            activeLoopTrack = track
            track.play()

            val chunk = ShortArray(1024)
            var phase = 0.0
            val baseFreq = when (voltageLevel) {
                0 -> 45.0
                1 -> 65.0
                else -> 90.0
            }

            while (isActive) {
                for (i in chunk.indices) {
                    phase += 2 * PI * baseFreq / sampleRate
                    if (phase > 2 * PI) phase -= 2 * PI

                    // Core AC buzz with odd harmonics
                    val buzz = sin(phase) + 0.6 * sin(3 * phase) + 0.3 * sin(5 * phase)
                    // Sharp electrical spark transient snaps
                    val sparkChance = if (voltageLevel == 2) 0.07 else 0.04
                    val spark = if (Random.nextDouble() < sparkChance) {
                        (Random.nextDouble() * 2.0 - 1.0) * 1.5
                    } else {
                        (Random.nextDouble() * 2.0 - 1.0) * 0.15
                    }

                    val sample = ((buzz * 0.4 + spark * 0.6) * 28000).toInt()
                    chunk[i] = sample.coerceIn(-32768, 32767).toShort()
                }
                track.write(chunk, 0, chunk.size)
            }
        }
    }

    fun startClipperContinuous(isCutting: Boolean) {
        val targetType = if (isCutting) "CLIPPER_CUTTING" else "CLIPPER_IDLE"
        if (activeLoopType == targetType) return
        stopLoop()
        activeLoopType = targetType

        loopJob = scope.launch {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            activeLoopTrack = track
            track.play()

            val chunk = ShortArray(1024)
            var phase1 = 0.0
            var phase2 = 0.0
            val motorFreq = if (isCutting) 98.0 else 122.0
            val subFreq = motorFreq * 2.0

            while (isActive) {
                for (i in chunk.indices) {
                    phase1 += 2 * PI * motorFreq / sampleRate
                    if (phase1 > 2 * PI) phase1 -= 2 * PI
                    phase2 += 2 * PI * subFreq / sampleRate
                    if (phase2 > 2 * PI) phase2 -= 2 * PI

                    val motorWave = sin(phase1) + 0.45 * sin(phase2) + 0.25 * sin(phase1 * 3)
                    val frictionNoise = if (isCutting) {
                        (Random.nextDouble() * 2.0 - 1.0) * 0.45
                    } else {
                        (Random.nextDouble() * 2.0 - 1.0) * 0.12
                    }

                    val combined = (motorWave * 0.6 + frictionNoise * 0.4) * (if (isCutting) 31000 else 24000)
                    chunk[i] = combined.toInt().coerceIn(-32768, 32767).toShort()
                }
                track.write(chunk, 0, chunk.size)
            }
        }
    }

    fun stopLoop() {
        activeLoopType = null
        loopJob?.cancel()
        loopJob = null
        try {
            activeLoopTrack?.stop()
            activeLoopTrack?.release()
        } catch (_: Exception) {}
        activeLoopTrack = null
    }

    private fun getOrGenerateSound(type: PrankSoundType): ShortArray {
        return soundCache.getOrPut(type) {
            when (type) {
                PrankSoundType.AIR_HORN -> generateAirHorn()
                PrankSoundType.FART_CLASSIC -> generateFart(durationSec = 0.9, baseFreq = 120.0, dropFactor = 0.4, wetness = 0.3)
                PrankSoundType.FART_WET -> generateFart(durationSec = 1.3, baseFreq = 95.0, dropFactor = 0.5, wetness = 0.8)
                PrankSoundType.FART_SNIPER -> generateFart(durationSec = 0.3, baseFreq = 240.0, dropFactor = 0.8, wetness = 0.1)
                PrankSoundType.FART_EPIC -> generateFart(durationSec = 2.8, baseFreq = 78.0, dropFactor = 0.3, wetness = 0.6)
                PrankSoundType.POLICE_SIREN -> generatePoliceSiren()
                PrankSoundType.GLASS_BREAK -> generateGlassBreak()
                PrankSoundType.DOORBELL -> generateDoorbell()
                PrankSoundType.CAR_ALARM -> generateCarAlarm()
                PrankSoundType.SCREAMER -> generateScreamer()
                PrankSoundType.MOSQUITO -> generateMosquito()
                PrankSoundType.HAIR_CLIPPER_BURST -> generateClipperBurst()
                PrankSoundType.TASER_ZAP -> generateTaserZap()
                PrankSoundType.CARTOON_LAUGH -> generateLaugh()
                PrankSoundType.PHONE_RING -> generatePhoneRingtone()
            }
        }
    }

    // --- SOUND GENERATORS ---

    private fun generateAirHorn(): ShortArray {
        val duration = 1.4
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)

        val f1 = 440.0 // A4
        val f2 = 554.37 // C#5
        val f3 = 659.25 // E5
        val f4 = 880.0 // A5

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Attack envelope and subtle pitch decay
            val pitchBend = 1.0 - 0.05 * (t / duration)
            val envelope = when {
                t < 0.05 -> t / 0.05
                t > duration - 0.25 -> (duration - t) / 0.25
                else -> 1.0
            }

            var wave = sin(2 * PI * f1 * pitchBend * t) * 0.4 +
                    sin(2 * PI * f2 * pitchBend * t) * 0.3 +
                    sin(2 * PI * f3 * pitchBend * t) * 0.2 +
                    sin(2 * PI * f4 * pitchBend * t) * 0.15

            // Saturation / brass distortion clipping
            wave = (wave * 1.8).coerceIn(-1.0, 1.0)
            data[i] = (wave * envelope * 31500).toInt().toShort()
        }
        return data
    }

    private fun generateFart(
        durationSec: Double,
        baseFreq: Double,
        dropFactor: Double,
        wetness: Double
    ): ShortArray {
        val totalSamples = (sampleRate * durationSec).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val currentFreq = baseFreq * (1.0 - progress * dropFactor)
            phase += 2 * PI * currentFreq / sampleRate

            val env = sin(PI * progress) // Bell curve envelope
            val jitter = 1.0 + 0.3 * sin(2 * PI * 28.0 * (i.toDouble() / sampleRate))
            val tone = sin(phase * jitter)

            val noise = (Random.nextDouble() * 2.0 - 1.0) * wetness
            var output = tone * (1.0 - wetness * 0.4) + noise * 0.7
            output = (output * 1.4).coerceIn(-1.0, 1.0)

            data[i] = (output * env * 30000).toInt().toShort()
        }
        return data
    }

    private fun generatePoliceSiren(): ShortArray {
        val duration = 2.4
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // 2 complete up-and-down cycles in 2.4 seconds
            val sweep = 0.5 * (1.0 + sin(2 * PI * 0.833 * t - PI / 2))
            val freq = 650.0 + sweep * 550.0 // 650Hz to 1200Hz

            phase += 2 * PI * freq / sampleRate
            val sample = sin(phase) + 0.3 * sin(3 * phase)
            data[i] = (sample * 28000).toInt().toShort()
        }
        return data
    }

    private fun generateGlassBreak(): ShortArray {
        val duration = 1.2
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-7.0 * t) // fast decay
            val whiteNoise = Random.nextDouble() * 2.0 - 1.0
            // High frequency crystalline resonances
            val chime = sin(2 * PI * 3400.0 * t) * exp(-12.0 * t) +
                    sin(2 * PI * 5800.0 * t) * exp(-18.0 * t)

            val mixed = (whiteNoise * 0.75 * env + chime * 0.4)
            data[i] = (mixed * 30000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return data
    }

    private fun generateDoorbell(): ShortArray {
        val duration = 1.8
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)

        val split = (sampleRate * 0.6).toInt() // "Ding" then "Dong"
        val fDing = 659.25 // E5
        val fDong = 523.25 // C5

        for (i in 0 until totalSamples) {
            val isFirstTone = i < split
            val toneTime = if (isFirstTone) i.toDouble() / sampleRate else (i - split).toDouble() / sampleRate
            val freq = if (isFirstTone) fDing else fDong
            val env = exp(-2.8 * toneTime)

            val tone = sin(2 * PI * freq * toneTime) + 0.35 * sin(2 * PI * freq * 2.0 * toneTime)
            data[i] = (tone * env * 29000).toInt().toShort()
        }
        return data
    }

    private fun generateCarAlarm(): ShortArray {
        val duration = 2.0
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val step = (t * 8.0).toInt() % 2
            val freq = if (step == 0) 950.0 else 1350.0
            phase += 2 * PI * freq / sampleRate
            val sample = if (sin(phase) > 0) 0.8 else -0.8 // Square pulse horn
            data[i] = (sample * 27000).toInt().toShort()
        }
        return data
    }

    private fun generateScreamer(): ShortArray {
        val duration = 1.6
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val chaosFreq = 1100.0 + sin(2 * PI * 18.0 * t) * 600.0 + (Random.nextDouble() * 300.0)
            phase += 2 * PI * chaosFreq / sampleRate

            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.4
            val tone = sin(phase)
            val env = if (t < 0.1) t / 0.1 else exp(-0.8 * (t - 0.1))

            val combined = (tone * 0.7 + noise) * env
            data[i] = (combined * 32000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return data
    }

    private fun generateMosquito(): ShortArray {
        val duration = 2.5
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 14500.0 + sin(2 * PI * 2.5 * t) * 600.0
            phase += 2 * PI * freq / sampleRate
            val sample = sin(phase) * 0.7
            data[i] = (sample * 22000).toInt().toShort()
        }
        return data
    }

    private fun generateClipperBurst(): ShortArray {
        val duration = 0.6
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            phase += 2 * PI * 110.0 / sampleRate
            val tone = sin(phase) + 0.3 * sin(2 * phase)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.3
            data[i] = ((tone + noise) * 26000).toInt().toShort()
        }
        return data
    }

    private fun generateTaserZap(): ShortArray {
        val duration = 0.7
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            phase += 2 * PI * 65.0 / sampleRate
            val tone = sin(phase) + 0.5 * sin(3 * phase)
            val spark = if (Random.nextDouble() < 0.08) (Random.nextDouble() * 2.0 - 1.0) * 1.5 else 0.0
            data[i] = ((tone * 0.4 + spark * 0.6) * 31000).toInt().coerceIn(-32768, 32767).toShort()
        }
        return data
    }

    private fun generateLaugh(): ShortArray {
        val duration = 2.0
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)

        val chuckles = 6
        val chuckleDuration = duration / chuckles
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val chuckIndex = (t / chuckleDuration).toInt()
            val subT = t - chuckIndex * chuckleDuration
            val env = sin(PI * (subT / chuckleDuration)).coerceAtLeast(0.0)

            val baseF = 260.0 + chuckIndex * 15.0
            val tone = sin(2 * PI * baseF * t) + 0.4 * sin(2 * PI * baseF * 2.0 * t)
            data[i] = (tone * env * 27000).toInt().toShort()
        }
        return data
    }

    private fun generatePhoneRingtone(): ShortArray {
        val duration = 2.4
        val totalSamples = (sampleRate * duration).toInt()
        val data = ShortArray(totalSamples)

        // Marimba ringtone notes
        val notes = doubleArrayOf(587.33, 659.25, 783.99, 880.0, 987.77, 1174.66)
        val noteLength = duration / (notes.size * 2)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val noteIdx = ((t / noteLength).toInt()) % notes.size
            val noteT = t % noteLength
            val freq = notes[noteIdx]
            val env = exp(-8.0 * noteT)

            val tone = sin(2 * PI * freq * noteT) + 0.3 * sin(2 * PI * freq * 3.0 * noteT)
            data[i] = (tone * env * 28000).toInt().toShort()
        }
        return data
    }
}
