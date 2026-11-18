package com.breathflow.app.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.breathflow.app.PhaseType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

// Staircase the glowing dot climbs on the inhale, normalised 0..1.
private val staircase = listOf(
    Offset(0.06f, 0.42f), Offset(0.18f, 0.42f),
    Offset(0.18f, 0.30f), Offset(0.30f, 0.30f),
    Offset(0.30f, 0.18f), Offset(0.42f, 0.18f),
    Offset(0.42f, 0.08f), Offset(0.52f, 0.08f)
)

// The slide down on the exhale.
private val slideOut = listOf(Offset(0.52f, 0.08f), Offset(0.95f, 0.42f))

// Fixed starfield: x, y, and a per-star seed for twinkling.
private val stars: List<Triple<Float, Float, Float>> = run {
    val rng = Random(3)
    List(56) { Triple(rng.nextFloat(), rng.nextFloat(), rng.nextFloat()) }
}

/** Point at fraction [t] (0..1) along a polyline, measured by distance. */
private fun pointAlong(points: List<Offset>, t: Float): Offset {
    val clamped = t.coerceIn(0f, 1f)
    val lengths = points.zipWithNext { a, b -> hypot(b.x - a.x, b.y - a.y) }
    var target = clamped * lengths.sum()
    for (i in lengths.indices) {
        val seg = lengths[i]
        if (target <= seg && seg > 0f) {
            val f = target / seg
            val a = points[i]
            val b = points[i + 1]
            return Offset(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f)
        }
        target -= seg
    }
    return points.last()
}

/** One smooth lung lobe. [mirror] flips it for the right lung. */
private fun lobePath(
    cx: Float,
    top: Float,
    bottom: Float,
    innerX: Float,
    outerX: Float,
    mirror: Boolean
): Path {
    fun m(x: Float) = if (mirror) 2 * cx - x else x
    val h = bottom - top
    val path = Path()
    path.moveTo(m(innerX), top)
    // Upper curve out to the outer edge
    path.cubicTo(
        m(outerX + (innerX - outerX) * 0.1f), top - h * 0.04f,
        m(outerX), top + h * 0.18f,
        m(outerX), top + h * 0.55f
    )
    // Outer edge down and round the base
    path.cubicTo(
        m(outerX), bottom - h * 0.02f,
        m(outerX + (innerX - outerX) * 0.4f), bottom,
        m(innerX - (innerX - outerX) * 0.15f), bottom
    )
    // Inner edge back up to the top
    path.cubicTo(
        m(innerX), bottom - h * 0.1f,
        m(innerX), top + h * 0.4f,
        m(innerX), top
    )
    path.close()
    return path
}

/**
 * The breathing visual: a glowing dot climbs a staircase on each inhale ("sip, sip, sip"),
 * holds at the top, slides down on the exhale. Lungs fill and empty with the breath,
 * surrounded by a pulsing halo, over a slowly drifting starfield.
 */
@Composable
fun LungsAnimation(
    phase: PhaseType,
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Slow ambient clock (0..1 every 60 s) for the stars and gentle drift.
    val transition = rememberInfiniteTransition(label = "ambient")
    val ambient by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient"
    )

    val gold = Color(0xFFF2B84B)
    val goldLight = Color(0xFFFFD98A)
    val lavender = Color(0xFFC9A0F5)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // How full the lungs are: 0 = empty, 1 = full.
        val fill = when (phase) {
            PhaseType.INHALE, PhaseType.SIP -> progress
            PhaseType.HOLD_FULL -> 1f
            PhaseType.EXHALE -> 1f - progress
            PhaseType.HOLD_EMPTY -> 0f
            PhaseType.OBSERVE, PhaseType.HUM -> 0.5f - 0.5f * cos(progress * 2f * PI.toFloat())
        }

        // 1. Background: deep indigo glow
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2E2770), Color(0xFF0E0B26)),
                center = Offset(cx, h * 0.35f),
                radius = h * 0.85f
            )
        )

        // 2. Starfield, gently twinkling
        stars.forEach { (sx, sy, seed) ->
            val twinkle = 0.15f + 0.6f * abs(sin(ambient * 2f * PI.toFloat() * (2f + seed) + seed * 12f))
            drawCircle(
                color = Color.White.copy(alpha = twinkle),
                radius = (0.8f + seed * 1.6f).dp.toPx(),
                center = Offset(sx * w, sy * h)
            )
        }

        // 3. Staircase: soft glow pass, then a crisp bright line
        val stairs = staircase.map { Offset(it.x * w, it.y * h) }
        val stairPath = Path().apply {
            moveTo(stairs[0].x, stairs[0].y)
            stairs.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = stairPath,
            color = gold.copy(alpha = 0.22f),
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = stairPath,
            color = goldLight,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Slide-out line for the exhale
        drawLine(
            color = Color(0xFF8FA3FF).copy(alpha = 0.45f),
            start = Offset(slideOut[0].x * w, slideOut[0].y * h),
            end = Offset(slideOut[1].x * w, slideOut[1].y * h),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 4. Glowing dot position for the current phase
        val dotNormalised = when (phase) {
            PhaseType.INHALE, PhaseType.SIP -> pointAlong(staircase, progress)
            PhaseType.HOLD_FULL -> staircase.last()
            PhaseType.EXHALE -> pointAlong(slideOut, progress)
            PhaseType.HOLD_EMPTY -> staircase.first()
            PhaseType.OBSERVE, PhaseType.HUM -> pointAlong(staircase, fill)
        }
        val dot = Offset(dotNormalised.x * w, dotNormalised.y * h)

        // 5. Lungs with a pulsing halo
        val scale = 0.85f + 0.15f * fill
        val lungCenterY = h * 0.70f
        val top = h * 0.52f
        val bottom = h * 0.88f
        val inner = cx - w * 0.035f
        val outer = cx - w * (0.05f + 0.20f * scale)

        val haloBase = w * 0.34f * scale
        for (i in 0 until 3) {
            drawCircle(
                color = lavender.copy(alpha = (0.22f - i * 0.06f) * (0.4f + fill * 0.6f)),
                radius = haloBase * (1.02f + i * 0.14f),
                center = Offset(cx, lungCenterY),
                style = Stroke(width = 1.5f.dp.toPx())
            )
        }

        val lungBrush = Brush.verticalGradient(
            colors = listOf(lavender, gold),
            startY = top,
            endY = bottom
        )
        listOf(false, true).forEach { mirror ->
            val lobe = lobePath(cx, top, bottom, inner, outer, mirror)
            drawPath(path = lobe, brush = lungBrush, alpha = 0.92f)
            drawPath(
                path = lobe,
                color = goldLight.copy(alpha = 0.5f),
                style = Stroke(width = 1.5f.dp.toPx())
            )
        }

        // Trachea and bronchi
        val trachea = Color(0xFFE6D2FF).copy(alpha = 0.85f)
        drawLine(
            color = trachea,
            start = Offset(cx, h * 0.40f),
            end = Offset(cx, h * 0.54f),
            strokeWidth = 7.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = trachea,
            start = Offset(cx, h * 0.54f),
            end = Offset(cx - w * 0.06f, h * 0.62f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = trachea,
            start = Offset(cx, h * 0.54f),
            end = Offset(cx + w * 0.06f, h * 0.62f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 6. The glowing dot, drawn last so it sits on top
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(gold.copy(alpha = 0.75f), gold.copy(alpha = 0.15f), Color.Transparent),
                center = dot,
                radius = 70.dp.toPx()
            ),
            radius = 70.dp.toPx(),
            center = dot
        )
        drawCircle(color = Color(0xFFFFF4D6), radius = 7.dp.toPx(), center = dot)
    }
}
