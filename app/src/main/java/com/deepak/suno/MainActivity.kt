package com.deepak.suno

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.deepak.suno.ui.components.MiniPlayer
import com.deepak.suno.ui.components.PermissionPrompt
import com.deepak.suno.ui.screens.LibraryScreen
import com.deepak.suno.ui.screens.LibraryViewModel
import com.deepak.suno.ui.screens.NowPlayingSheet
import com.deepak.suno.ui.theme.SunoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SunoTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Determine the appropriate storage permission based on Android OS version
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasAudioPermission by remember {
        mutableStateOf(false)
    }

    // Permission launcher for runtime permission request flow
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[audioPermission] == true
        hasAudioPermission = granted
        if (granted) {
            viewModel.refresh()
        }
    }

    // Check permission on initial launch
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED
        hasAudioPermission = isGranted

        if (isGranted) {
            viewModel.refresh()
        }
    }

    // Toast notifications for user messages (Streaming, Downloading, Success, Error)
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (!hasAudioPermission) {
        PermissionPrompt(
            onRequestPermission = {
                val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                permissionLauncher.launch(permissionsToRequest)
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LibraryScreen(
                uiState = uiState,
                onTabSelected = viewModel::onTabSelected,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onSongClicked = viewModel::playSong,
                onToggleFavorite = viewModel::toggleFavorite,
                onCreatePlaylist = viewModel::createPlaylist,
                onSearchYouTube = viewModel::searchYouTube,
                onStreamYouTube = viewModel::streamYouTubeTrack,
                onDownloadYouTube = viewModel::downloadYouTubeTrack,
                onRefresh = viewModel::refresh
            )


            // Floating MiniPlayer docked above the navigation bar
            if (uiState.playerState.currentSong != null) {
                MiniPlayer(
                    playerState = uiState.playerState,
                    onPlayPause = viewModel::playPause,
                    onSkipNext = viewModel::skipNext,
                    onClick = { viewModel.setNowPlayingSheetVisible(true) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Expandable full-screen Now Playing Modal Sheet
            if (uiState.isNowPlayingSheetVisible && uiState.playerState.currentSong != null) {
                val currentSong = uiState.playerState.currentSong!!
                val isFav = uiState.favorites.contains(currentSong.id)

                NowPlayingSheet(
                    playerState = uiState.playerState,
                    isFavorite = isFav,
                    onToggleFavorite = { viewModel.toggleFavorite(currentSong.id) },
                    onDismiss = { viewModel.setNowPlayingSheetVisible(false) },
                    onPlayPause = viewModel::playPause,
                    onSeekTo = viewModel::seekTo,
                    onSkipNext = viewModel::skipNext,
                    onSkipPrevious = viewModel::skipPrevious,
                    onToggleShuffle = viewModel::toggleShuffle,
                    onToggleRepeat = viewModel::toggleRepeat,
                    onSetPlaybackSpeed = viewModel::setPlaybackSpeed,
                    onSetSleepTimer = viewModel::setSleepTimer
                )
            }
        }
    }
}
