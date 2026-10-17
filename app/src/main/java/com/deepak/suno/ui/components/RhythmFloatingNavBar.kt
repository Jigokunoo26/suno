package com.deepak.suno.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepak.suno.ui.screens.LibraryTab
import com.deepak.suno.ui.theme.GlassTokens

/**
 * Rhythm's signature Floating Navigation Dock (matching Screen 1 of Rhythm).
 * Features:
 * - Center floating pill dock with Home and Library items that expand when active.
 * - Separate circular floating Search action button on the right.
 * - Smooth bouncy spring animations and translucent glass styling.
 */
@Composable
fun RhythmFloatingNavBar(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main Floating Pill Dock
        Surface(
            shape = CircleShape,
            color = Color(0xFF1E1B24).copy(alpha = 0.95f),
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color.White.copy(alpha = 0.12f)
            ),
            modifier = Modifier.height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Pill
                RhythmNavItem(
                    label = "Home",
                    icon = Icons.Rounded.Home,
                    isSelected = selectedTab == LibraryTab.HOME,
                    onClick = { onTabSelected(LibraryTab.HOME) }
                )

                // Library Pill
                val isLibraryActive = selectedTab != LibraryTab.HOME && selectedTab != LibraryTab.YOUTUBE
                RhythmNavItem(
                    label = "Library",
                    icon = Icons.Rounded.LibraryMusic,
                    isSelected = isLibraryActive,
                    onClick = {
                        if (isLibraryActive) {
                            onTabSelected(LibraryTab.SONGS)
                        } else {
                            onTabSelected(LibraryTab.SONGS)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Right Circular Search Button (matching Rhythm Screen 1)
        val isSearchActive = selectedTab == LibraryTab.YOUTUBE
        val searchInteractionSource = remember { MutableInteractionSource() }
        val isSearchPressed by searchInteractionSource.collectIsPressedAsState()
        val searchScale by animateFloatAsState(
            targetValue = if (isSearchPressed) 0.90f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "searchScale"
        )

        Surface(
            onClick = onOpenSearch,
            interactionSource = searchInteractionSource,
            shape = CircleShape,
            color = if (isSearchActive) MaterialTheme.colorScheme.primary else Color(0xFF1E1B24).copy(alpha = 0.95f),
            contentColor = if (isSearchActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSearchActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .size(56.dp)
                .scale(searchScale)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun RhythmNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "navContainer"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "navContent"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier
            .height(44.dp)
            .scale(scale)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (isSelected) 18.dp else 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )

            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
