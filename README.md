# 🎵 Suno - Modern Native Android Music Player

[![Download APK](https://img.shields.io/badge/Download-Suno.apk-7C4DFF?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Jigokunoo26/suno/raw/main/Suno.apk)
[![Android](https://img.shields.io/badge/Platform-Android_14_&_15-00E5FF?style=for-the-badge&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_1.9-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

**Suno** is a native, modern Android music player crafted with **Kotlin**, **Jetpack Compose (Material 3)**, and **AndroidX Media3 (ExoPlayer + MediaSessionService)**. It provides local offline music playback, dedicated **Downloads Vault**, and YouTube & Cloud Music streaming with direct **MP3 downloader**.

---

## 📥 Direct Download & Install

You can download and install the latest compiled APK directly on your Android phone:
👉 **[Download Suno.apk (Latest)](https://github.com/Jigokunoo26/suno/raw/main/Suno.apk)**

---

## ✨ Features

- **📱 Sleek Modern UI**: Deep obsidian theme with neon cyan & electric violet accents built strictly with Jetpack Compose Material 3.
- **📁 Dedicated Downloads Page**: View all downloaded MP3s in an offline vault with **Play All** and **Shuffle** controls.
- **🎧 Seamless Local & Streaming Playback**: Dual-engine Media3 ExoPlayer architecture capable of playing both offline files (`content://` Scoped Storage URIs) and online HTTP streams.
- **📥 High-Quality MP3 Downloader**: Downloads audio directly to `/Music/Suno` as `.mp3` with complete title, artist, album, and high-resolution cover artwork indexed immediately in Android's MediaStore.
- **🔍 YouTube & Cloud Search**: Search millions of tracks or paste YouTube links to stream or download for offline playback.
- **❤️ Favorites & Playlists**: Persistent offline storage using Room database with reactive StateFlow updates.
- **🌙 Background Playback & Lock-Screen Controls**: Powered by `MediaSessionService` with notification player controls and audio focus management.
- **⏱️ Sleep Timer & Speed Control**: Pitch-preserving speed adjustment (0.75x - 1.5x) and automatic sleep timer.

---

## 🏗️ Architecture & Component Overview

```
app/
 ├── data/
 │    ├── local/
 │    │    ├── dao/                <-- Room DAOs (FavoriteDao, PlaylistDao)
 │    │    ├── entity/             <-- Room Entities
 │    │    ├── DownloadMetadataStore.kt <-- Offline track metadata persistence
 │    │    └── SunoDatabase.kt     <-- Room database configuration
 │    ├── mediastore/
 │    │    └── MediaStoreScanner.kt <-- Scoped Storage MediaStore query engine
 │    ├── repository/
 │    │    ├── MusicRepository.kt  <-- Clean repository interface
 │    │    └── MusicRepositoryImpl.kt <-- Reactive Flow caching & filtering
 │    └── youtube/
 │         └── YouTubeService.kt   <-- Stream extraction & MP3 downloader
 ├── domain/
 │    └── model/                  <-- Immutable domain models (Song, Album, Artist, etc.)
 ├── player/
 │    ├── PlaybackService.kt       <-- Foreground MediaSessionService + ExoPlayer
 │    └── MusicController.kt       <-- MediaController IPC client
 ├── di/                           <-- Hilt Dependency Injection modules
 └── ui/
      ├── components/              <-- MiniPlayer, SongListItem, YouTubeTrackItem
      ├── screens/                 <-- LibraryScreen, NowPlayingSheet, LibraryViewModel
      └── theme/                   <-- Dark palette & typography tokens
```

---

## 🛠️ Building & Running Locally

### Requirements
- Android Studio Ladybug / Koala / Iguana
- JDK 17 (Microsoft OpenJDK or Eclipse Temurin)
- Android SDK (API Level 24 to 35)

### Build Debug APK:
```powershell
./gradlew assembleDebug
```

### Install Directly to USB Connected Device:
```powershell
./build_and_install.ps1
```

---

## 📄 License
Open source and built for music enthusiasts.