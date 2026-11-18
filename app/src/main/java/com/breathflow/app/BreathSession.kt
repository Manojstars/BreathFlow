package com.breathflow.app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** How fast the practice runs. Scales every phase duration. */
enum class Pace(val label: String, val factor: Float) {
    SLOW("Slow", 1.25f),
    NORMAL("Normal", 1f),
    BRISK("Brisk", 0.8f)
}

data class BreathState(
    val running: Boolean = false,
    val finished: Boolean = false,
    val phase: PhaseType = PhaseType.INHALE,
    val phaseProgress: Float = 0f,   // 0..1 within the current phase
    val cycle: Int = 0,
    val totalCycles: Int = 0,
    val secondsLeft: Int = 0,        // seconds left in the current phase
    val sessionSecondsLeft: Int? = null  // null when the session has no time limit
)

/** Runs a technique's steps on a timer, speaking cues and publishing state for the UI. */
class BreathSession(
    private val scope: CoroutineScope,
    private val voice: CueVoice
) {
    private val _state = MutableStateFlow(BreathState())
    val state: StateFlow<BreathState> = _state.asStateFlow()

    private var job: Job? = null

    /** @param lengthMinutes 0 = no time limit (runs until its cycles end or the user stops). */
    fun start(technique: Technique, pace: Pace, lengthMinutes: Int) {
        stop()
        val limitMs = if (lengthMinutes == 0) null else lengthMinutes * 60_000L

        job = scope.launch {
            val sessionStart = System.nanoTime()
            var cycle = 0

            voice.chime(end = false)
            _state.value = BreathState(
                running = true,
                totalCycles = technique.cycles,
                sessionSecondsLeft = limitMs?.let { (it / 1000).toInt() }
            )

            session@ while (isActive) {
                if (technique.cycles != 0 && cycle >= technique.cycles) break
                cycle++

                for (step in technique.steps) {
                    val durationMs = (step.millis * pace.factor).toLong()
                    val begin = System.nanoTime()

                    voice.phaseStarted(step, durationMs)

                    while (isActive) {
                        val now = System.nanoTime()
                        val elapsedMs = (now - begin) / 1_000_000
                        val totalElapsedMs = (now - sessionStart) / 1_000_000

                        if (limitMs != null && totalElapsedMs >= limitMs) break@session

                        val progress = (elapsedMs.toFloat() / durationMs).coerceIn(0f, 1f)
                        _state.value = BreathState(
                            running = true,
                            phase = step.type,
                            phaseProgress = progress,
                            cycle = cycle,
                            totalCycles = technique.cycles,
                            secondsLeft = ((durationMs - elapsedMs).coerceAtLeast(0) + 999).div(1000).toInt(),
                            sessionSecondsLeft = limitMs?.let {
                                ((it - totalElapsedMs).coerceAtLeast(0) / 1000).toInt()
                            }
                        )

                        if (elapsedMs >= durationMs) break
                        delay(16)
                    }
                }
            }

            // Reached here only when the session finished on its own, not when cancelled.
            voice.speak("Session complete")
            voice.chime(end = true)
            _state.value = BreathState(
                running = false,
                finished = true,
                cycle = cycle,
                totalCycles = technique.cycles,
                sessionSecondsLeft = 0
            )
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        _state.value = BreathState()
    }
}
