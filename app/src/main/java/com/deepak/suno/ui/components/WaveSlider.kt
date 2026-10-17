package com.deepak.suno.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Rhythm-inspired Expressive WaveSlider.
 * Displays undulating, alive sine waves on the active playback track while music is playing,
 * transitioning smoothly to an elegant linear pill when paused.
 * Features full interactive scrubbing, thumb glow, and precise duration timestamps.
 */
@Composable
fun WaveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    isPlaying: Boolean = true,
    activeTrackColor: Color = MaterialTheme.colorScheme.primary,
    inactiveTrackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 5.dp,
    waveAmplitude: Dp = 4.dp,
    wavelengthDp: Dp = 32.dp
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
    val normalizedValue = ((value - valueRange.start) / rangeSpan).coerceIn(0f, 1f)

    val displayProgress = if (isDragging) dragProgress else normalizedValue

    // Wave animation phase driver
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnimation")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Smooth wave amplitude animation based on playback state
    val animatedAmplitudeFraction by animateFloatAsState(
        targetValue = if (isPlaying && !isDragging) 1f else 0.1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "waveAmp"
    )

    // Thumb interaction scale
    val thumbScale by animateFloatAsState(
        targetValue = if (isDragging) 1.35f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "thumbScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .pointerInput(valueRange) {
                detectTapGestures(
                    onPress = { offset ->
                        val trackWidth = size.width
                        if (trackWidth > 0) {
                            val newNorm = (offset.x / trackWidth).coerceIn(0f, 1f)
                            val targetVal = valueRange.start + newNorm * rangeSpan
                            onValueChange(targetVal)
                            onValueChangeFinished?.invoke()
                        }
                    }
                )
            }
            .pointerInput(valueRange) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        val newNorm = (offset.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newNorm
                        onValueChange(valueRange.start + newNorm * rangeSpan)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val newNorm = (change.position.x / size.width).coerceIn(0f, 1f)
                        dragProgress = newNorm
                        onValueChange(valueRange.start + newNorm * rangeSpan)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(28.dp)) {
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val strokePx = strokeWidth.toPx()
            val ampPx = waveAmplitude.toPx() * animatedAmplitudeFraction
            val wavelengthPx = wavelengthDp.toPx()

            val thumbX = (displayProgress * width).coerceIn(0f, width)

            // 1. Draw Inactive Track (Right side)
            if (thumbX < width) {
                drawLine(
                    color = inactiveTrackColor,
                    start = Offset(thumbX, centerY),
                    end = Offset(width, centerY),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw Active Track (Left side with Sine Wave when playing)
            if (thumbX > 0f) {
                if (animatedAmplitudeFraction > 0.15f) {
                    val wavePath = Path()
                    wavePath.moveTo(0f, centerY)

                    var x = 0f
                    val stepPx = 3f
                    while (x <= thumbX) {
                        val fractionAlong = (x / thumbX).coerceIn(0f, 1f)
                        // Smoothly taper wave near ends
                        val envelope = sin(fractionAlong * PI.toFloat()).coerceIn(0f, 1f)
                        val y = centerY + sin((x / wavelengthPx) * (2f * PI.toFloat()) - wavePhase) * (ampPx * envelope)
                        wavePath.lineTo(x, y)
                        x += stepPx
                    }
                    wavePath.lineTo(thumbX, centerY)

                    drawPath(
                        path = wavePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                activeTrackColor.copy(alpha = 0.85f),
                                activeTrackColor,
                                thumbColor
                            ),
                            startX = 0f,
                            endX = thumbX.coerceAtLeast(1f)
                        ),
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                } else {
                    // Straight linear active track when paused or scrubbed
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                activeTrackColor.copy(alpha = 0.85f),
                                activeTrackColor
                            ),
                            startX = 0f,
                            endX = thumbX.coerceAtLeast(1f)
                        ),
                        start = Offset(0f, centerY),
                        end = Offset(thumbX, centerY),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Thumb Indicator with specular outer glow
            val baseThumbRadius = 8.dp.toPx() * thumbScale
            val glowRadius = baseThumbRadius * 1.8f

            // Ambient outer glow
            drawCircle(
                color = thumbColor.copy(alpha = 0.22f),
                radius = glowRadius,
                center = Offset(thumbX, centerY)
            )

            // Outer thumb ring
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = baseThumbRadius,
                center = Offset(thumbX, centerY)
            )

            // Inner core thumb
            drawCircle(
                color = thumbColor,
                radius = baseThumbRadius * 0.72f,
                center = Offset(thumbX, centerY)
            )
        }
    }
}
