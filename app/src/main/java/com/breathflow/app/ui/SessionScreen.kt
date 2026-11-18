package com.breathflow.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.breathflow.app.BreathSession
import com.breathflow.app.CueVoice
import com.breathflow.app.Pace
import com.breathflow.app.Prefs
import com.breathflow.app.Technique

private val lengthOptions = listOf(0 to "Open", 5 to "5 min", 10 to "10 min", 15 to "15 min", 20 to "20 min")

private fun mmss(totalSeconds: Int): String = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)

@Composable
fun SessionScreen(
    technique: Technique,
    voice: CueVoice,
    prefs: Prefs,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val session = remember(technique.id) { BreathSession(scope, voice) }
    val state by session.state.collectAsState()

    var pace by remember { mutableStateOf(prefs.pace) }
    var lengthMin by remember { mutableStateOf(prefs.lengthMinutes) }
    var voiceOn by remember { mutableStateOf(voice.voiceEnabled) }
    var soundOn by remember { mutableStateOf(voice.soundEnabled) }

    // Stop the timer when leaving this screen.
    DisposableEffect(technique.id) {
        onDispose { session.stop() }
    }

    val status = when {
        state.running -> state.phase.label
        state.finished -> "Complete"
        else -> "Ready"
    }

    val detail = when {
        state.running -> buildString {
            append("${state.secondsLeft}s")
            if (state.totalCycles == 0) {
                append(" · round ${state.cycle}")
            } else {
                append(" · round ${state.cycle} of ${state.totalCycles}")
            }
            state.sessionSecondsLeft?.let { append(" · ${mmss(it)} left") }
        }
        state.finished -> "Well done · ${state.cycle} rounds"
        else -> technique.summary
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                session.stop()
                onBack()
            }) {
                Text("← Back", color = MaterialTheme.colorScheme.primary)
            }
        }
        Text(
            text = technique.name,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Animation
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            LungsAnimation(
                phase = state.phase,
                progress = if (state.running) state.phaseProgress else 0f,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "Breathing guide. $status" }
            )
        }

        Text(
            text = status,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Controls card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ChoiceRow(
                    label = "Pace",
                    options = Pace.entries.map { it to it.label },
                    selected = pace,
                    onSelect = {
                        pace = it
                        prefs.pace = it
                    }
                )
                Spacer(Modifier.height(8.dp))
                ChoiceRow(
                    label = "Length",
                    options = lengthOptions,
                    selected = lengthMin,
                    onSelect = {
                        lengthMin = it
                        prefs.lengthMinutes = it
                    }
                )

                Spacer(Modifier.height(12.dp))

                ToggleRow(
                    label = "Voice cues",
                    checked = voiceOn,
                    onChange = {
                        voiceOn = it
                        voice.voiceEnabled = it
                        prefs.voiceOn = it
                    }
                )
                ToggleRow(
                    label = "Breath sounds & chimes",
                    checked = soundOn,
                    onChange = {
                        soundOn = it
                        voice.soundEnabled = it
                        prefs.soundOn = it
                    }
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (state.running) session.stop() else session.start(technique, pace, lengthMin)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = when {
                            state.running -> "Stop"
                            state.finished -> "Start again"
                            else -> "Begin"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                technique.caution?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "⚠ $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun <T> ChoiceRow(
    label: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (value, text) ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = { Text(text) }
            )
        }
    }
}
