package com.deepak.suno.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern Glassmorphic Design System Tokens and Utilities for Suno.
 * Inspired by Rhythm's Expressive UI, enhanced with a frosted glassy finish,
 * translucent specular highlights, smooth ambient glow aura, and refined micro-interactions.
 */
object GlassTokens {
    // Glass Surface Tints
    val DarkGlassBackground = Color(0xCC0E0D12)
    val DarkGlassSurface = Color(0x991E1C26)
    val DarkGlassSurfaceHigh = Color(0xAA2B2738)
    val DarkGlassSurfaceHighest = Color(0xBF3B364D)

    val LightGlassBackground = Color(0xDDF8F5FF)
    val LightGlassSurface = Color(0xAAEAE3F7)
    val LightGlassSurfaceHigh = Color(0xBAE0D5F3)

    // Glass Border Strokes (Translucent specular highlights)
    val GlassBorderDark = Color(0x28FFFFFF)
    val GlassBorderSubtleDark = Color(0x18FFFFFF)
    val GlassBorderVibrantDark = Color(0x40D0BCFF)

    val GlassBorderLight = Color(0x38FFFFFF)
    val GlassBorderSubtleLight = Color(0x206750A4)

    // Glass Highlight Gradient (Top-left to bottom-right sheen)
    val GlassSheenBrushDark = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.04f),
            Color.Transparent,
            Color.White.copy(alpha = 0.08f)
        ),
        start = Offset(0f, 0f),
        end = Offset(400f, 600f)
    )

    val GlassCardBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.28f),
            Color.White.copy(alpha = 0.08f),
            Color.Transparent,
            Color.White.copy(alpha = 0.14f)
        ),
        start = Offset(0f, 0f),
        end = Offset(300f, 300f)
    )

    val GlassActiveGlowBrush = Brush.linearGradient(
        colors = listOf(
            PrimaryDark.copy(alpha = 0.65f),
            ElectricVioletLight.copy(alpha = 0.45f),
            HotRose.copy(alpha = 0.35f)
        )
    )
}

/**
 * Glassmorphic surface container with frosted translucent tint,
 * subtle specular border, and elevation.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = GlassTokens.DarkGlassSurface,
    borderColor: Color = GlassTokens.GlassBorderDark,
    borderWidth: Dp = 1.dp,
    shadowElevation: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.linearGradient(
                        colors = listOf(
                            borderColor,
                            borderColor.copy(alpha = borderColor.alpha * 0.4f),
                            Color.Transparent,
                            borderColor.copy(alpha = borderColor.alpha * 0.7f)
                        )
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Ambient Aura Glow effect for the Now Playing album art and hero cards.
 * Undulates softly to create a living, breathing backdrop.
 */
@Composable
fun AmbientAuraGlow(
    modifier: Modifier = Modifier,
    primaryColor: Color = PrimaryDark,
    secondaryColor: Color = HotRose,
    tertiaryColor: Color = NeonCyan,
    blurRadius: Dp = 36.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "auraGlow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.48f * pulse),
                        secondaryColor.copy(alpha = 0.32f * pulse),
                        tertiaryColor.copy(alpha = 0.16f * pulse),
                        Color.Transparent
                    )
                )
            )
            .blur(blurRadius)
    )
}

/**
 * Modifier extension to apply glassmorphic backdrop sheen.
 */
fun Modifier.glassmorphicBackground(
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = GlassTokens.DarkGlassSurface,
    borderColor: Color = GlassTokens.GlassBorderDark,
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(containerColor)
    .border(
        BorderStroke(
            borderWidth,
            Brush.linearGradient(
                colors = listOf(
                    borderColor,
                    borderColor.copy(alpha = borderColor.alpha * 0.35f),
                    Color.Transparent,
                    borderColor.copy(alpha = borderColor.alpha * 0.65f)
                )
            )
        ),
        shape = shape
    )
