package com.deepak.suno.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material Design 3 Expressive Shape System (Rhythm style)
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Expressive shape tokens inspired by Rhythm
object ExpressiveShapeTokens {
    val Full = CircleShape
    val ExtraLarge = RoundedCornerShape(32.dp)
    val Large = RoundedCornerShape(28.dp)
    val Medium = RoundedCornerShape(20.dp)
    val Small = RoundedCornerShape(14.dp)
    val ExtraSmall = RoundedCornerShape(10.dp)

    val BottomSheet = RoundedCornerShape(
        topStart = 32.dp,
        topEnd = 32.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
}

// Custom shapes for music components (Rhythm style)
object MusicShapes {
    val PlayerCard = RoundedCornerShape(32.dp)
    val PlayerControls = CircleShape
    val MiniPlayerFloating = RoundedCornerShape(24.dp)
    val GlassDock = RoundedCornerShape(28.dp)

    val AlbumCoverLarge = RoundedCornerShape(26.dp)
    val AlbumCover = RoundedCornerShape(18.dp)
    val AlbumCoverSmall = RoundedCornerShape(14.dp)

    val SongCard = RoundedCornerShape(18.dp)
    val PlaylistCard = RoundedCornerShape(22.dp)
    val ArtistCard = RoundedCornerShape(26.dp)

    val SearchBar = CircleShape
    val FilterChip = CircleShape
    val ActionPill = CircleShape
}
