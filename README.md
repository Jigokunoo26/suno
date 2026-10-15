# 🎵 Suno - Modern Native Android Music Player

[![Download APK](https://img.shields.io/badge/Download-Suno.apk-7C4DFF?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Jigokunoo26/suno/raw/main/Suno.apk)
[![Android](https://img.shields.io/badge/Platform-Android_14_&_15-00E5FF?style=for-the-badge&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_1.9-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

**Suno** is a native, modern Android music player crafted with **Kotlin**, **Jetpack Compose (Material 3)**, and **AndroidX Media3 (ExoPlayer + MediaSessionService)**. It unites local offline music playback, dedicated **Downloads Vault**, YouTube & Cloud Music streaming with continuous **autoplay queue**, and direct **MP3 downloader**.

---

## 📥 Direct Download & Install

Download and install any version directly on your Android phone:

| Version | Release Notes | Direct Download | File Size |
|:---|:---|:---:|:---:|
| **v1.4.0 (Latest)** | Continuous Online Playback Queue, YouTube-Accurate Search with Typo & Multilingual Auto-Correction, Interactive Queue Drawer, Infinite Autoplay | [Download Suno.apk](https://github.com/Jigokunoo26/suno/raw/main/Suno.apk) | ~23.1 MB |
| **v1.3.0 (Archive)** | Complete Playlist Creation & Track Picker, Movie Soundtrack Merging, Bottom Sliding Navigation Bar | [Download APK](https://github.com/Jigokunoo26/suno/raw/main/apks/Suno-v1.3.0.apk) | ~23.8 MB |
| **v1.2.0 (Archive)** | Spotify Home & Responsive Now Playing Build Archive | [Download APK](https://github.com/Jigokunoo26/suno/raw/main/apks/Suno-v1.2.0.apk) | ~22.9 MB |
| **v1.1.0 (Archive)** | Material 3 Expressive Build Archive (Rhythm-style) | [Download APK](https://github.com/Jigokunoo26/suno/raw/main/apks/Suno-v1.1.0-rhythm-expressive.apk) | ~22.8 MB |
| **v1.0.0 (Classic)** | Initial Release with Downloads Vault, Cloud Music, YouTube MP3 downloader | [Download v1.0.0 APK](https://github.com/Jigokunoo26/suno/raw/main/apks/Suno-v1.0.0-classic.apk) | ~22.9 MB |

---

## ✨ Features ("What Is What")

### 🔄 Continuous Online Playback Queue & Infinite Autoplay
- **Never Stops at One Song**: When streaming online music, Suno automatically stages a full playback queue (minimum 5+ tracks) from search results or related tracks.
- **Instant Start + Background Resolution**: The selected track starts playing immediately (~250ms), while subsequent tracks in the queue have their audio streams resolved concurrently in the background.
- **Infinite Radio / Autoplay**: When approaching the end of the queue, Suno dynamically queries related music from YouTube to append more songs, ensuring nonstop music playback.

### 🔍 YouTube-Accurate Search with Typo & Multilingual Auto-Correction
- **Intelligent Spell-Correction**: Integrated with Google/YouTube Suggest API to catch typos and misspelled text (e.g., *"samjavaragmana"* ➔ *"samajavaragamana"*, *"puspa 2 songs"* ➔ *"pushpa 2 songs"*, *"devera chutamale"* ➔ *"devara chuttamalle"*).
- **Native Multilingual Support**: Seamlessly searches queries in Telugu script (e.g., *"పుష్ప 2 పాటలు"*), Romanized Telugu, Hindi, Tamil, English, and regional music.
- **Dual Engine Architecture**: Combines official YouTube InnerTube video tracks (official music videos, lyrical songs, teasers) with Cloud Music (studio master 320kbps CD-quality audio).

### 📋 Interactive Playback Queue Drawer
- **Now Playing Queue Viewer**: Tap the Queue icon in Now Playing to open an interactive drawer displaying the active song with live animated equalizer bars and upcoming queued tracks.
- **Direct Track Skipping**: Tap any queued track to immediately jump to it.
- **One-Tap "Play All (Queue)"**: Start continuous playback of an entire search results list with a single button.

### 📥 High-Quality MP3 Downloader & Downloads Vault
- **Direct MP3 Downloads**: Downloads audio streams directly to `/Music/Suno` as `.mp3` files with complete title, artist, album, and high-resolution (500x500) cover artwork.
- **Instant MediaStore Indexing**: Uses Scoped Storage APIs to index downloaded tracks immediately into Android's system MediaStore.
- **Dedicated Downloads Page**: Offline vault displaying all downloaded MP3s with dedicated **Play All** and **Shuffle** controls.

### 🎧 Dual-Engine Media3 Audio Playback
- **Unified Engine**: Single player architecture capable of playing offline storage files (`content://` Scoped Storage URIs) and online HTTP/HTTPS audio streams without mode-switching.
- **Background Playback & Lock-Screen Controls**: Powered by `MediaSessionService` with lock-screen notification controls, album art, seekbar, and audio focus management.

### 📱 Modern Expressive Material 3 UI
- **Obsidian Theme**: Deep obsidian background with neon cyan & electric violet accents.
- **Spotify-Inspired Home Screen**: Time-aware dynamic greeting (Good morning/afternoon/evening), "Heavy Rotation" listening count badges, and "Jump Back In" recently played tracks.
- **Expressive Now Playing**: Ambient album art aura glow, fluid scrubber, pitch-preserving playback speed (0.75x–1.5x), and sleep timer (15m, 30m, 60m).

### ❤️ Favorites & Custom Playlists
- **Persistent Storage**: Room Database with reactive StateFlow updates.
- **Playlist Management**: Create custom playlists, add or remove tracks, delete playlists, and play full queues.

---

## ⚙️ How Suno Manages Everything ("How It Works")

```
                                ┌─────────────────────────────────┐
                                │        User Interface           │
                                │   (Jetpack Compose Material 3)  │
                                └───────────────┬─────────────────┘
                                                │
                                       StateFlow & Events
                                                │
                                ┌───────────────▼─────────────────┐
                                │       LibraryViewModel          │
                                │   (Queue, Search, State Sync)   │
                                └───────┬───────────────┬─────────┘
                                        │               │
                     ┌──────────────────┘               └──────────────────┐
                     ▼                                                     ▼
        ┌─────────────────────────┐                           ┌─────────────────────────┐
        │     MusicController     │                           │     YouTubeService      │
        │  (MediaController IPC)  │                           │(InnerTube + Cloud Music)│
        └────────────┬────────────┘                           └────────────┬────────────┘
                     │                                                     │
         IPC Bound Connection                                Parallel Stream Extraction
                     │                                                     │
        ┌────────────▼────────────┐                           ┌────────────▼────────────┐
        │     PlaybackService     │                           │    MusicRepository      │
        │ (MediaSessionService +  │                           │ (Room Database + Cache +│
        │    ExoPlayer Engine)    │                           │    MediaStore Scanner)  │
        └─────────────────────────┘                           └─────────────────────────┘
```

### 1. Audio Playback Management (`PlaybackService` & `MusicController`)
- **Foreground MediaSessionService**: Playback runs inside `PlaybackService`, an Android Service that survives when the app is backgrounded, device is locked, or screen is rotated.
- **Audio Focus & Headphone Events**: Automatically pauses playback during incoming phone calls or GPS voice navigation and pauses when headphones are unplugged (`setHandleAudioBecomingNoisy(true)`).
- **IPC Controller**: `MusicController` communicates with `PlaybackService` via Media3's `MediaController` IPC interface, exposing reactive `StateFlow<PlayerState>` for position tickers, queue indices, and play/pause state.

### 2. Online Stream Resolution & Queue Lifecycle (`YouTubeService` & `LibraryViewModel`)
- **Queue Pipeline**: When an online track is tapped, the selected track's audio stream is resolved and passed to ExoPlayer for instant playback.
- **Background Resolution Worker**: Remaining tracks in the queue are resolved in parallel batches using coroutines (`Dispatchers.IO`) and dynamically appended to ExoPlayer (`controller.addMediaItems()`).
- **Autoplay Radio Trigger**: When playback advances to the final two tracks in the queue, `MusicController` notifies `LibraryViewModel` through `onNearQueueEndListener`, which fetches additional related tracks and appends them to maintain an endless queue.

### 3. Search Engine & Typo Tolerance (`YouTubeService`)
- **Three-Tier Search**:
  1. *Suggestion Tier*: Queries Google/YouTube autocomplete endpoint (`client=chrome&ds=yt`) to resolve misspelled queries in any language.
  2. *InnerTube Tier*: Sends JSON payloads to YouTube's `youtubei/v1/search` endpoint (WEB client) to extract verified video metadata, durations, and thumbnails.
  3. *Cloud Music Tier*: Queries JioSaavn API to retrieve pre-decrypted 320kbps CD-quality audio streams.
- **Intelligent Merging**: Deduplicates tracks by normalized title similarity and prioritizes high-bitrate studio audio streams.

### 4. Storage & MediaStore Synchronization (`MediaStoreScanner` & `DownloadMetadataStore`)
- **Scoped Storage Compliance**: On Android 10+ (API 29–35), downloads are inserted directly into `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI` with `RELATIVE_PATH = Music/Suno` and `IS_PENDING = 1` during download, then marked `IS_PENDING = 0` on completion.
- **Immediate Indexing**: Downloaded files are indexed by Android's MediaStore immediately, making them discoverable across the entire device and inside Suno's offline Downloads Vault without requiring a device restart.

### 5. Local Persistence Architecture (`Room Database`)
- **Room DAOs**: Manages user favorites (`FavoriteDao`), playlists (`PlaylistDao`), and track play statistics (`SongStatsDao`).
- **Reactive Flow Mapping**: Database changes are emitted as Kotlin `Flow`s and combined in `LibraryViewModel` via `combine()` operators into a single unified `LibraryUiState`.

---

## 📄 License
Open source and built for music enthusiasts.