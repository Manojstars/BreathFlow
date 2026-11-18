package com.breathflow.app

/** The kind of breath phase. Drives the animation and the spoken cue. */
enum class PhaseType(val label: String) {
    INHALE("Breathe in"),
    HOLD_FULL("Hold"),
    EXHALE("Breathe out"),
    HOLD_EMPTY("Hold"),
    SIP("Sip in"),
    OBSERVE("Observe"),
    HUM("Hum")
}

/**
 * One phase in a cycle. [cue] is spoken at the start of the phase.
 * Pass cue = null for phases too fast to speak (e.g. Kapalabhati).
 */
data class Step(
    val type: PhaseType,
    val millis: Long,
    val cue: String? = type.label
)

data class Technique(
    val id: String,
    val name: String,
    val origin: String,
    val summary: String,
    val steps: List<Step>,
    val cycles: Int = 0,          // 0 = run until stopped
    val caution: String? = null
)

val allTechniques = listOf(
    Technique(
        id = "box",
        name = "Box breathing",
        origin = "Western · used by Navy SEALs and clinicians",
        summary = "Four equal counts: in, hold, out, hold. Steadies focus and calms stress quickly.",
        steps = listOf(
            Step(PhaseType.INHALE, 4000),
            Step(PhaseType.HOLD_FULL, 4000),
            Step(PhaseType.EXHALE, 4000),
            Step(PhaseType.HOLD_EMPTY, 4000)
        )
    ),
    Technique(
        id = "478",
        name = "4-7-8 relaxing breath",
        origin = "Western · Dr. Andrew Weil",
        summary = "Inhale 4, hold 7, exhale 8. The long exhale encourages sleep and slows the heart rate.",
        steps = listOf(
            Step(PhaseType.INHALE, 4000),
            Step(PhaseType.HOLD_FULL, 7000),
            Step(PhaseType.EXHALE, 8000)
        ),
        cycles = 4
    ),
    Technique(
        id = "sigh",
        name = "Physiological sigh",
        origin = "Western · Stanford research, 2023",
        summary = "Two sips in through the nose, then one long slow exhale. The fastest known way to lower stress in the moment.",
        steps = listOf(
            Step(PhaseType.SIP, 1200, "Sip in"),
            Step(PhaseType.SIP, 800, "Top up"),
            Step(PhaseType.EXHALE, 6000, "Long, slow breath out")
        ),
        cycles = 6
    ),
    Technique(
        id = "resonance",
        name = "Coherent breathing",
        origin = "Western · heart-rate variability research",
        summary = "About 5.5 breaths per minute, even in and out. Balances the nervous system.",
        steps = listOf(
            Step(PhaseType.INHALE, 5500),
            Step(PhaseType.EXHALE, 5500)
        )
    ),
    Technique(
        id = "equal",
        name = "Equal breathing",
        origin = "Western · simple relaxation method",
        summary = "Breathe in and out for the same length. Simple, good for beginners.",
        steps = listOf(
            Step(PhaseType.INHALE, 4000),
            Step(PhaseType.EXHALE, 4000)
        )
    ),
    Technique(
        id = "pursed",
        name = "Pursed-lip breathing",
        origin = "Western · used in COPD and breathlessness care",
        summary = "Breathe in through the nose, then out slowly through pursed lips. Eases breathlessness.",
        steps = listOf(
            Step(PhaseType.INHALE, 2000),
            Step(PhaseType.EXHALE, 4000, "Out slowly through pursed lips")
        )
    ),
    Technique(
        id = "belly",
        name = "Diaphragmatic belly breathing",
        origin = "Western · core of most relaxation programs",
        summary = "Breathe deep into the belly, not the chest. Longer exhales than inhales.",
        steps = listOf(
            Step(PhaseType.INHALE, 4000, "Breathe into your belly"),
            Step(PhaseType.EXHALE, 6000)
        )
    ),
    Technique(
        id = "anapana",
        name = "Anapana (Vipassana)",
        origin = "Vipassana · Buddhist, Goenka tradition",
        summary = "Do not control the breath. Simply notice its natural flow at the nostrils and the upper lip. Builds steady awareness.",
        steps = listOf(
            Step(PhaseType.OBSERVE, 8000, "Notice the natural breath at the nostrils")
        )
    ),
    Technique(
        id = "nadi",
        name = "Nadi Shodhana",
        origin = "Yoga · alternate-nostril breathing",
        summary = "Alternate nostrils to balance the two sides of the body and calm the mind.",
        steps = listOf(
            Step(PhaseType.INHALE, 4000, "Inhale through the left nostril"),
            Step(PhaseType.HOLD_FULL, 4000),
            Step(PhaseType.EXHALE, 4000, "Exhale through the right nostril"),
            Step(PhaseType.INHALE, 4000, "Inhale through the right nostril"),
            Step(PhaseType.HOLD_FULL, 4000),
            Step(PhaseType.EXHALE, 4000, "Exhale through the left nostril")
        )
    ),
    Technique(
        id = "bhramari",
        name = "Bhramari (bee breath)",
        origin = "Yoga · humming breath",
        summary = "Inhale, then hum softly on the exhale. The vibration soothes and helps you unwind.",
        steps = listOf(
            Step(PhaseType.INHALE, 4000),
            Step(PhaseType.HUM, 8000, "Hum like a bee")
        )
    ),
    Technique(
        id = "ujjayi",
        name = "Ujjayi (ocean breath)",
        origin = "Yoga · used in Vinyasa practice",
        summary = "A slight constriction at the back of the throat makes a soft ocean sound. Steady and grounding.",
        steps = listOf(
            Step(PhaseType.INHALE, 5000, "Inhale with a soft ocean sound"),
            Step(PhaseType.EXHALE, 5000, "Exhale with the same soft sound")
        )
    ),
    Technique(
        id = "kapalabhati",
        name = "Kapalabhati (skull shining)",
        origin = "Yoga · energising breath",
        summary = "Short, sharp exhales with passive inhales. Energises and clears the head.",
        steps = listOf(
            Step(PhaseType.EXHALE, 800, cue = null),
            Step(PhaseType.INHALE, 800, cue = null)
        ),
        caution = "Do not practise if you are pregnant or have high blood pressure, heart disease, or epilepsy. Stop if you feel dizzy."
    )
)
