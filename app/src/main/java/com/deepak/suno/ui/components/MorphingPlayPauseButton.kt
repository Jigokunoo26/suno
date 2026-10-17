package com.deepak.suno.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rhythm signature Morphing Play/Pause Button.
 * Morphs between a smooth circle (when paused) and a 9-sided expressive cookie shape
 * that gently rotates during active playback. Features tactile spring physics and glass glow.
 */
@Composable
fun MorphingPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    isBuffering: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "morphPressScale"
    )

    // Morph progress: 0f = Circle (Paused), 1f = Expressive Cookie (Playing)
    val morphProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "morphProgress"
    )

    // Gentle slow ambient rotation during playback
    val infiniteTransition = rememberInfiniteTransition(label = "morphRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Draw the morphing shape canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height
            val center = Offset(width / 2f, height / 2f)
            val baseRadius = (width.coerceAtMost(height) / 2f) * 0.94f

            rotate(degrees = if (isPlaying) rotationAngle else 0f, pivot = center) {
                val path = Path()
                val lobes = 9
                val stepAngle = (2f * PI / (lobes * 16)).toFloat()
                var first = true

                // Smoothly modulate radius between pure circle and 9-lobed undulating cookie
                var angle = 0f
                while (angle <= 2f * PI) {
                    val lobeModulation = sin(lobes * angle) * (baseRadius * 0.12f * morphProgress)
                    val r = baseRadius + lobeModulation
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)

                    if (first) {
                        path.moveTo(x, y)
                        first = false
                    } else {
                        path.lineTo(x, y)
                    }
                    angle += stepAngle
                }
                path.close()

                // Draw gradient filled shape
                drawPath(
                    path = path,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.92f)
                        ),
                        center = center,
                        radius = baseRadius * 1.2f
                    ),
                    style = Fill
                )
            }
        }

        // Center Icon (Buffering spinner OR Play/Pause)
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(size * 0.44f),
                color = contentColor,
                strokeWidth = 3.dp
            )
        } else {
            Crossfade(
                targetState = isPlaying,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                label = "iconMorph"
            ) { playing ->
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playing) "Pause" else "Play",
                    tint = contentColor,
                    modifier = Modifier.size(size * 0.52f)
                )
            }
        }
    }
}
