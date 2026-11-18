package com.breathflow.app

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Audio for a practice:
 *  - a calm spoken voice ("Breathe in", "Breathe out", "Hold")
 *  - synthesised breath sounds for each inhale and exhale
 *  - singing-bowl chimes to start and finish a session
 */
class CueVoice(context: Context) {

    var voiceEnabled: Boolean = true
    var soundEnabled: Boolean = true

    private var ttsReady = false

    private val tts: TextToSpeech = TextToSpeech(context) { status ->
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) configureVoice()
    }

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val breathIn = pool.load(context, R.raw.breath_in, 1)
    private val breathOut = pool.load(context, R.raw.breath_out, 1)
    private val bowlChime = pool.load(context, R.raw.bowl_chime, 1)
    private val bowlEnd = pool.load(context, R.raw.bowl_end, 1)

    /** Slow, slightly lower voice that sounds unhurried. Picks the best offline English voice. */
    private fun configureVoice() {
        tts.language = Locale.US
        tts.setSpeechRate(0.78f)
        tts.setPitch(0.92f)
        tts.voices
            ?.filter { it.locale.language == "en" && !it.isNetworkConnectionRequired }
            ?.maxByOrNull { it.quality }
            ?.let { tts.voice = it }
    }

    /** Called when a breath phase begins: plays the breath sound and speaks the cue. */
    fun phaseStarted(step: Step, durationMs: Long) {
        if (soundEnabled) {
            when (step.type) {
                PhaseType.INHALE -> pool.play(breathIn, 0.8f, 0.8f, 1, 0, 1f)
                PhaseType.EXHALE -> pool.play(breathOut, 0.8f, 0.8f, 1, 0, 1f)
                else -> Unit
            }
        }
        if (voiceEnabled && durationMs >= 1500) {
            step.cue?.let { speak(it) }
        }
    }

    fun speak(text: String) {
        if (voiceEnabled && ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cue")
        }
    }

    fun chime(end: Boolean) {
        if (!soundEnabled) return
        pool.play(if (end) bowlEnd else bowlChime, 0.9f, 0.9f, 1, 0, 1f)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
        pool.release()
    }
}
