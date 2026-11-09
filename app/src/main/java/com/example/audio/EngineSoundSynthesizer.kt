package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.EngineRevSound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class EngineSoundSynthesizer {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var isPlaying = false

    @Volatile
    private var isMuted = false

    @Volatile
    private var throttlePressed = false

    @Volatile
    private var targetRpm = 850f

    @Volatile
    private var currentRpm = 850f

    @Volatile
    private var redlineRpm = 8500f

    @Volatile
    private var currentSound = EngineRevSound.FLAT_6

    @Volatile
    private var limiterCut = false

    private val _rpmState = MutableStateFlow(850f)
    val rpmState = _rpmState.asStateFlow()

    private val _isLimiterBanging = MutableStateFlow(false)
    val isLimiterBanging = _isLimiterBanging.asStateFlow()

    fun setSoundProfile(sound: EngineRevSound, redline: Int) {
        currentSound = sound
        redlineRpm = redline.toFloat()
        if (currentRpm > redlineRpm) {
            currentRpm = redlineRpm * 0.9f
        }
    }

    fun setThrottle(pressed: Boolean) {
        throttlePressed = pressed
        targetRpm = if (pressed) redlineRpm + 200f else 850f
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        return isMuted
    }

    fun isCurrentlyMuted(): Boolean = isMuted

    fun start() {
        if (isPlaying) return
        isPlaying = true

        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()

        synthesisJob = scope.launch {
            val chunkSamples = 1024
            val buffer = ShortArray(chunkSamples)
            var phaseMain = 0.0
            var phaseHarmonic = 0.0
            var phaseSub = 0.0
            var phaseTurbo = 0.0
            var popCooldown = 0

            while (isActive && isPlaying) {
                // Smooth RPM physics
                val target = targetRpm
                if (throttlePressed) {
                    // Accelerate rapidly
                    val accelerationRate = (redlineRpm - 800f) / 18.0f // ~0.35s to redline
                    currentRpm += accelerationRate

                    if (currentRpm >= redlineRpm) {
                        currentRpm = redlineRpm - Random.nextFloat() * 450f
                        limiterCut = true
                        _isLimiterBanging.value = true
                    } else {
                        limiterCut = false
                        _isLimiterBanging.value = false
                    }
                } else {
                    // Decelerate with natural engine braking
                    currentRpm += (target - currentRpm) * 0.12f
                    if (currentRpm < 850f) currentRpm = 850f
                    limiterCut = false
                    _isLimiterBanging.value = false
                }

                _rpmState.value = currentRpm

                // Synthesize PCM samples
                val cylinders = currentSound.cylinders
                val firingFreq = (currentRpm / 60.0) * (cylinders / 2.0).coerceAtLeast(1.0)
                val turboFreq = 1200.0 + (currentRpm / redlineRpm) * 3500.0
                val volume = if (isMuted) 0.0f else (0.28f + (currentRpm / redlineRpm) * 0.45f)

                for (i in 0 until chunkSamples) {
                    val dt = 1.0 / sampleRate
                    phaseMain += 2.0 * PI * firingFreq * dt
                    phaseHarmonic += 2.0 * PI * (firingFreq * 2.0) * dt
                    phaseSub += 2.0 * PI * (firingFreq * 0.5) * dt
                    if (currentSound.turboSpool) {
                        phaseTurbo += 2.0 * PI * turboFreq * dt
                    }

                    if (phaseMain > 2.0 * PI) phaseMain -= 2.0 * PI
                    if (phaseHarmonic > 2.0 * PI) phaseHarmonic -= 2.0 * PI
                    if (phaseSub > 2.0 * PI) phaseSub -= 2.0 * PI
                    if (phaseTurbo > 2.0 * PI) phaseTurbo -= 2.0 * PI

                    // Core cylinder combustion waveform
                    val waveMain = (sin(phaseMain) + 0.5 * sin(phaseHarmonic) + 0.3 * sin(phaseSub)).toFloat()

                    // Asymmetric exhaust raspiness
                    val raspy = if (waveMain > 0) waveMain * 1.15f else waveMain * 0.85f

                    // Turbo whistle if applicable
                    val turboWhistle = if (currentSound.turboSpool && currentRpm > 3000f) {
                        (sin(phaseTurbo) * 0.12f * (currentRpm / redlineRpm)).toFloat()
                    } else {
                        0f
                    }

                    // Limiter pop & crackle (exhaust fire pops!)
                    var popNoise = 0f
                    if (limiterCut || (popCooldown > 0)) {
                        popNoise = (Random.nextFloat() * 2f - 1f) * 0.8f
                        if (popCooldown > 0) popCooldown--
                    } else if (!throttlePressed && currentRpm > 4500f && Random.nextFloat() < 0.003f) {
                        // overrun crackles
                        popNoise = (Random.nextFloat() * 2f - 1f) * 0.7f
                        popCooldown = 180
                    }

                    val rawSample = (raspy + turboWhistle + popNoise) * volume
                    // Soft clip / limiter
                    val clamped = rawSample.coerceIn(-0.95f, 0.95f)
                    buffer[i] = (clamped * 32767).toInt().toShort()
                }

                audioTrack?.write(buffer, 0, chunkSamples)
            }
        }
    }

    fun stop() {
        isPlaying = false
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _isLimiterBanging.value = false
    }
}
