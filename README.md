# 🎧 Suno - Modern Local Android Music Player

**Suno** is a native Android music player crafted with **Kotlin**, **Jetpack Compose (Material 3)**, and **Jetpack Media3 (ExoPlayer + MediaSessionService)**. Built strictly according to the architecture plan documented in `Untitled.docx`.

---

## 🏗️ Architecture & Component Overview

```
app/
 ├── data/
 │    ├── mediastore/
 │    │    └── MediaStoreScanner.kt       <-- Queries MediaStore using Scoped Storage URIs
 │    └── repository/
 │         ├── MusicRepository.kt         <-- Clean Repository interface
 │         └── MusicRepositoryImpl.kt     <-- Caching, filtering, Flow<List<Song>>
 ├── domain/
 │    └── model/
 │         ├── Song.kt                    <-- Scoped URI audio model
 │         ├── Album.kt                   <-- Aggregated album model
 │         ├── Artist.kt                  <-- Aggregated artist model
 │         └── PlayerState.kt             <-- Immutable playback state snapshot
 ├── player/
 │    ├── PlaybackService.kt              <-- MediaSessionService + ExoPlayer (Background)
 │    └── MusicController.kt              <-- MediaController IPC wrapper for UI ViewModels
 ├── di/
 │    └── AppModule.kt                    <-- Hilt Dependency Injection bindings
 ├── ui/
 │    ├── theme/                          <-- Color, Type, Material 3 Dark Palette
 │    ├── components/
 │    │    ├── SongListItem.kt            <-- Coil album artwork + Animated indicator
 │    │    ├── MiniPlayer.kt              <-- Docked bottom bar with progress line
 │    │    └── PermissionPrompt.kt        <-- Scoped audio permission rationale UI
 │    └── screens/
 │         ├── LibraryViewModel.kt        <-- MVVM state manager (StateFlow)
 │         ├── LibraryScreen.kt           <-- Tabs (Songs/Albums/Artists), Search
 │         └── NowPlayingSheet.kt         <-- Modal player, slider seek, audio controls
 └── MainActivity.kt                      <-- Edge-to-edge Compose entry & permission launcher
```

---

## 💡 Educational Deep-Dive: How It Works

### 1. Why `MediaSessionService` instead of playing audio in `Activity`?
An `Activity` in Android is only tied to the screen. If the user switches apps, locks their phone, or receives a phone call:
- An `Activity` can be destroyed by the Android OS to reclaim memory.
- A `MediaSessionService` runs as an independent Android `Service`. By pairing it with `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, the Android OS prioritizes the process, keeping music playing with the screen off.
- Media3 automatically handles the lock-screen notification and system quick-settings media tiles without you having to write notification builders manually.

### 2. Scoped Storage & `MediaStore`
Starting with Android 10 (API 29) and enforced in Android 13+ (API 33):
- Apps should **not** scan the file tree directly (`/storage/emulated/0/...`).
- Instead, Android provides `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`. Android's background daemon indexes song titles, artists, albums, and embedded ID3 tags.
- In `MediaStoreScanner.kt`, we query this database and construct content URIs (`content://media/external/audio/media/1234`), which are secure and compliant.

### 3. Audio Focus & Peripheral Disconnects
Inside `PlaybackService.kt`:
- `setAudioAttributes(..., true)`: Automatically ducks or pauses audio when an incoming call arrives or navigation speaks.
- `setHandleAudioBecomingNoisy(true)`: Instantly pauses audio when headphones are unplugged to prevent public blaring.

### 4. Local Database with Room (Favorites & Custom Playlists)
- **Entities & DAOs**: We store favorited song IDs (`FavoriteEntity`) and custom user playlists (`PlaylistEntity`, `PlaylistSongCrossRef`) locally.
- **Reactive Queries**: Room returns `Flow<List<Long>>` and `Flow<List<PlaylistEntity>>`. When a song is favorited, Room triggers a reactive update across the entire UI automatically!
- **Clean Separation**: Media metadata lives in Android's MediaStore, while user state (favorites, playlists, play counts) lives in Room.

### 5. Dependency Injection with Hilt
- Eliminates hard-coded dependencies and makes the code modular.
- `@HiltAndroidApp` creates the DI graph on application boot.
- `@HiltViewModel` allows `LibraryViewModel` to effortlessly receive `MusicRepository` and `MusicController`.
- `DatabaseModule` provisions `SunoDatabase`, `FavoriteDao`, and `PlaylistDao`.

### 6. Advanced Playback Features
- **Sleep Timer**: A background coroutine timer automatically fades and pauses playback when the duration finishes.
- **Playback Speed**: ExoPlayer natively supports real-time pitch-preserving time stretching (0.75x to 1.5x) via `mediaController.setPlaybackSpeed()`.

### 7. Dual-Engine Online Streaming & MP3/M4A Downloader
- **Direct YouTube InnerTube Extraction**: Connects directly to YouTube's Android player endpoint (`youtubei/v1/player` with client `ANDROID 20.10.38`), extracting direct audio-only streams (itag 140 AAC m4a / itag 251 Opus) without intermediary proxies.
- **High-Bitrate Studio Music Cloud**: Connects to high-speed CDNs with DES-ECB decryption (`38346591` key), streaming crystal-clear 320kbps CD-quality audio files.
- **Configured ExoPlayer DataSource**: `PlaybackService` uses `DefaultHttpDataSource.Factory()` configured with user agent `com.google.android.youtube/20.10.38` and cross-protocol redirects to prevent 403 blocks.
- **Smart URL & Song Search**: Accepts song names, artist names, or direct YouTube links (`youtube.com/watch?v=...`, `youtu.be/...`).
- **Live UI Feedback**: Stream button displays a circular loading indicator while resolving; Download button displays live percentage progress and linear progress bars.
- **MediaScanner Auto-Indexing**: Uses Android's `MediaScannerConnection` upon download completion, immediately making the downloaded file appear in your local **Songs**, **Albums**, and **Artists** tabs!


---

## 🚀 How to Run in Android Studio

1. **Install Android Studio**:
   - Download the latest stable version of [Android Studio](https://developer.android.com/studio) (e.g. Ladybug / Iguana).
   - Bundles JDK 17 and Android SDK automatically.
2. **Open the Project**:
   - Open Android Studio -> Click **Open** -> Select the folder: `c:\Users\edeep\suno`.
3. **Gradle Sync**:
   - Android Studio will detect `settings.gradle.kts` and `gradle/libs.versions.toml`, download dependencies, and sync automatically.
4. **Run on Device**:
   - Connect an Android phone with **USB Debugging** enabled.
   - Click the green **Run (Play)** button in Android Studio.
   - The app will prompt for Audio Storage permission, scan your music files, and start playing!
