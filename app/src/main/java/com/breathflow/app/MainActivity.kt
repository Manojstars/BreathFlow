package com.breathflow.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.breathflow.app.ui.BreathFlowTheme
import com.breathflow.app.ui.SessionScreen
import com.breathflow.app.ui.TechniqueListScreen

class MainActivity : ComponentActivity() {

    private lateinit var voice: CueVoice
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep the screen awake while a practice is open.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        prefs = Prefs(this)
        voice = CueVoice(this).apply {
            voiceEnabled = prefs.voiceOn
            soundEnabled = prefs.soundOn
        }

        setContent {
            BreathFlowTheme {
                BreathFlowApp(voice, prefs)
            }
        }
    }

    override fun onDestroy() {
        voice.shutdown()
        super.onDestroy()
    }
}

@Composable
fun BreathFlowApp(voice: CueVoice, prefs: Prefs) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val technique = allTechniques.firstOrNull { it.id == selectedId }

    // Android back button: from a practice return to the list, rather than closing the app.
    BackHandler(enabled = technique != null) { selectedId = null }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (technique == null) {
            TechniqueListScreen(onSelect = {
                prefs.lastTechniqueId = it.id
                selectedId = it.id
            })
        } else {
            SessionScreen(
                technique = technique,
                voice = voice,
                prefs = prefs,
                onBack = { selectedId = null }
            )
        }
    }
}
