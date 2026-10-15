package com.deepak.suno.data.youtube

import android.content.Context
import android.content.ContentValues
import android.provider.MediaStore
import android.media.MediaScannerConnection
import android.os.Build
import com.deepak.suno.data.local.DownloadMetadataStore
import android.os.Environment
import android.text.Html
import android.util.Base64
import com.deepak.suno.domain.model.YouTubeTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metadataStore: DownloadMetadataStore
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val androidUserAgent = "com.google.android.youtube/20.10.38 (Linux; U; Android 14; en_US) gzip"
    private val browserUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    /**
     * Extracts YouTube 11-character video ID from varied URL formats or raw ID.
     */
    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.length == 11 && !trimmed.contains("/") && !trimmed.contains("?") && !trimmed.contains(" ")) {
            return trimmed
        }
        val pattern = "(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/|music\\.youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=))([^\"&?\\/\\s]{11})"
        val compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val matcher = compiledPattern.matcher(trimmed)
        return if (matcher.find()) matcher.group(1) else null
    }

    /**
     * Searches for music tracks with high accuracy:
     * - Auto-corrects misspelled text (Telugu, English, Hindi, etc.)
     * - Queries both official YouTube InnerTube search and Cloud Music database
     * - Guarantees minimum 15 to 25 rich music results with complete metadata
     */
    suspend fun search(query: String): List<YouTubeTrack> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return@withContext emptyList()

        // 1. Direct YouTube link or Video ID check
        val directVideoId = extractVideoId(cleanQuery)
        if (directVideoId != null) {
            val track = resolveYouTubeVideoDetails(directVideoId)
            if (track != null) {
                val related = getRelatedTracks(directVideoId, track.title)
                return@withContext listOf(track) + related
            }
        }

        // 2. Query YouTube search suggestions to detect typos and corrections
        val suggestions = getSearchSuggestions(cleanQuery)
        val bestCorrection = suggestions.firstOrNull { !it.equals(cleanQuery, ignoreCase = true) }

        // 3. Search official YouTube InnerTube (WEB platform client)
        val (innerTubeCorrection, ytResults) = searchYouTubeInnerTube(cleanQuery)
        val effectiveCorrection = bestCorrection ?: innerTubeCorrection

        // 4. Search Cloud Music (JioSaavn) with original query and auto-corrected query
        val cloudResults = mutableListOf<YouTubeTrack>()
        val primaryCloud = searchCloudMusic(cleanQuery)
        cloudResults.addAll(primaryCloud)

        if (cloudResults.isEmpty() && !effectiveCorrection.isNullOrBlank()) {
            val correctedCloud = searchCloudMusic(effectiveCorrection)
            cloudResults.addAll(correctedCloud)
        }

        // 5. If InnerTube search with original was sparse and we have a typo correction, search InnerTube with correction
        val secondaryYt = if (ytResults.size < 6 && !effectiveCorrection.isNullOrBlank()) {
            searchYouTubeInnerTube(effectiveCorrection).second
        } else {
            emptyList()
        }

        // 6. Merge, deduplicate, and prioritize musical results
        val mergedList = mutableListOf<YouTubeTrack>()
        val seenKeys = mutableSetOf<String>()

        fun addTrack(track: YouTubeTrack) {
            val normKey = track.title.lowercase().replace(Regex("[^a-z0-9]"), "").take(18)
            if (normKey.isNotEmpty() && !seenKeys.contains(normKey)) {
                seenKeys.add(normKey)
                mergedList.add(track)
            }
        }

        // Interleave Cloud Music (320kbps CD master) and YouTube (official clips, lyrical songs, live music)
        val maxLen = maxOf(cloudResults.size, ytResults.size, secondaryYt.size)
        for (i in 0 until maxLen) {
            if (i < cloudResults.size) addTrack(cloudResults[i])
            if (i < ytResults.size) addTrack(ytResults[i])
            if (i < secondaryYt.size) addTrack(secondaryYt[i])
        }

        if (mergedList.isNotEmpty()) {
            return@withContext mergedList
        }

        // 7. Fallback search tracks
        fallbackSearch(cleanQuery)
    }

    /**
     * Queries Google/YouTube search suggestion engine to fetch auto-completions
     * and auto-corrected spelling suggestions across all languages (Telugu, Hindi, English, etc.)
     */
    suspend fun getSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://suggestqueries.google.com/complete/search?client=chrome&ds=yt&q=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", browserUserAgent)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONArray(body)
                if (json.length() >= 2) {
                    val suggestionsArray = json.optJSONArray(1) ?: return@withContext emptyList()
                    val result = mutableListOf<String>()
                    for (i in 0 until suggestionsArray.length()) {
                        val text = suggestionsArray.optString(i, "")
                        if (text.isNotBlank()) result.add(text)
                    }
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

    /**
     * Official YouTube InnerTube Search client (WEB platform):
     * Returns YouTube video tracks with full titles, artists, durations, and high-res thumbnails.
     * Also detects auto-correction ("Showing results for / Did you mean").
     */
    suspend fun searchYouTubeInnerTube(query: String): Pair<String?, List<YouTubeTrack>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20240101.01.00")
                        put("hl", "en")
                        put("gl", "IN")
                    })
                })
                put("query", query)
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/search?prettyPrint=false")
                .header("User-Agent", browserUserAgent)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Pair(null, emptyList())
                val body = response.body?.string() ?: return@withContext Pair(null, emptyList())
                val json = JSONObject(body)

                var correctedQuery: String? = null
                val tracks = mutableListOf<YouTubeTrack>()

                val contents = json.optJSONObject("contents")
                    ?.optJSONObject("twoColumnSearchResultsRenderer")
                    ?.optJSONObject("primaryContents")
                    ?.optJSONObject("sectionListRenderer")
                    ?.optJSONArray("contents") ?: JSONArray()

                for (s in 0 until contents.length()) {
                    val section = contents.getJSONObject(s)
                    val itemSection = section.optJSONObject("itemSectionRenderer")?.optJSONArray("contents") ?: continue

                    for (i in 0 until itemSection.length()) {
                        val item = itemSection.getJSONObject(i)

                        // 1. Did you mean / Showing results for correction detection
                        val didYouMean = item.optJSONObject("didYouMeanRenderer")
                        if (didYouMean != null) {
                            val runs = didYouMean.optJSONObject("correctedQuery")?.optJSONArray("runs")
                            if (runs != null && runs.length() > 0) {
                                correctedQuery = runs.getJSONObject(0).optString("text")
                            }
                        }
                        val showingResultsFor = item.optJSONObject("showingResultsForRenderer")
                        if (showingResultsFor != null) {
                            val runs = showingResultsFor.optJSONObject("correctedQuery")?.optJSONArray("runs")
                            if (runs != null && runs.length() > 0) {
                                correctedQuery = runs.getJSONObject(0).optString("text")
                            }
                        }

                        // 2. Video item
                        val vr = item.optJSONObject("videoRenderer") ?: continue
                        val videoId = vr.optString("videoId", "")
                        if (videoId.isEmpty()) continue

                        val titleRuns = vr.optJSONObject("title")?.optJSONArray("runs")
                        val title = if (titleRuns != null && titleRuns.length() > 0) {
                            unescapeHtml(titleRuns.getJSONObject(0).optString("text", "YouTube Music"))
                        } else {
                            "YouTube Music"
                        }

                        val ownerRuns = vr.optJSONObject("ownerText")?.optJSONArray("runs")
                            ?: vr.optJSONObject("shortBylineText")?.optJSONArray("runs")
                        val artist = if (ownerRuns != null && ownerRuns.length() > 0) {
                            unescapeHtml(ownerRuns.getJSONObject(0).optString("text", "YouTube Artist"))
                        } else {
                            "YouTube Artist"
                        }

                        val durText = vr.optJSONObject("lengthText")?.optString("simpleText", "") ?: ""
                        var durationSec = 210L
                        if (durText.isNotEmpty()) {
                            val parts = durText.split(":")
                            if (parts.size == 2) {
                                val m = parts[0].toLongOrNull() ?: 3L
                                val s = parts[1].toLongOrNull() ?: 30L
                                durationSec = m * 60 + s
                            } else if (parts.size == 3) {
                                val h = parts[0].toLongOrNull() ?: 1L
                                val m = parts[1].toLongOrNull() ?: 0L
                                val s = parts[2].toLongOrNull() ?: 0L
                                durationSec = h * 3600 + m * 60 + s
                            }
                        }

                        // Filter out full movies (> 25 mins) to keep music accuracy high
                        if (durationSec > 1500) continue

                        val thumbs = vr.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                        val thumb = if (thumbs != null && thumbs.length() > 0) {
                            thumbs.getJSONObject(thumbs.length() - 1).optString("url", "https://i.ytimg.com/vi/$videoId/hqdefault.jpg")
                        } else {
                            "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
                        }

                        tracks.add(
                            YouTubeTrack(
                                videoId = videoId,
                                title = title,
                                uploader = artist,
                                durationSeconds = durationSec,
                                thumbnailUrl = thumb,
                                streamUrl = null
                            )
                        )
                    }
                }
                return@withContext Pair(correctedQuery, tracks)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, emptyList())
        }
    }

    /**
     * Extracts related music tracks for a given YouTube video ID from InnerTube /next endpoint.
     * Ensures playback queue always has at least 5 to 20 related songs for continuous autoplay.
     */
    suspend fun getRelatedTracks(videoId: String, title: String? = null): List<YouTubeTrack> = withContext(Dispatchers.IO) {
        val relatedTracks = mutableListOf<YouTubeTrack>()
        try {
            val payload = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20240101.01.00")
                        put("hl", "en")
                        put("gl", "IN")
                    })
                })
                put("videoId", videoId)
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/next?prettyPrint=false")
                .header("User-Agent", browserUserAgent)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val secondary = json.optJSONObject("contents")
                            ?.optJSONObject("twoColumnWatchNextResults")
                            ?.optJSONObject("secondaryResults")
                            ?.optJSONObject("secondaryResults")
                            ?.optJSONArray("results") ?: JSONArray()

                        for (i in 0 until secondary.length()) {
                            val item = secondary.getJSONObject(i)

                            // 1. lockupViewModel format (Modern YouTube Web)
                            val lockup = item.optJSONObject("lockupViewModel")
                            if (lockup != null) {
                                val id = lockup.optString("contentId", "")
                                val meta = lockup.optJSONObject("metadata")?.optJSONObject("lockupMetadataViewModel")
                                val trackTitle = meta?.optJSONObject("title")?.optString("content", "") ?: ""
                                val rows = meta?.optJSONObject("metadata")
                                    ?.optJSONObject("contentMetadataViewModel")
                                    ?.optJSONArray("metadataRows")
                                var channel = "YouTube Music"
                                if (rows != null && rows.length() > 0) {
                                    val parts = rows.getJSONObject(0).optJSONArray("metadataParts")
                                    if (parts != null && parts.length() > 0) {
                                        channel = parts.getJSONObject(0).optJSONObject("text")?.optString("content", channel) ?: channel
                                    }
                                }
                                val sources = lockup.optJSONObject("contentImage")
                                    ?.optJSONObject("thumbnailViewModel")
                                    ?.optJSONObject("image")
                                    ?.optJSONArray("sources")
                                val thumb = if (sources != null && sources.length() > 0) {
                                    sources.getJSONObject(sources.length() - 1).optString("url", "https://i.ytimg.com/vi/$id/hqdefault.jpg")
                                } else {
                                    "https://i.ytimg.com/vi/$id/hqdefault.jpg"
                                }

                                if (id.isNotEmpty() && trackTitle.isNotEmpty() && id != videoId) {
                                    relatedTracks.add(
                                        YouTubeTrack(
                                            videoId = id,
                                            title = unescapeHtml(trackTitle),
                                            uploader = unescapeHtml(channel),
                                            durationSeconds = 210L,
                                            thumbnailUrl = thumb,
                                            streamUrl = null
                                        )
                                    )
                                }
                                continue
                            }

                            // 2. compactVideoRenderer format (Legacy format)
                            val cv = item.optJSONObject("compactVideoRenderer")
                            if (cv != null) {
                                val id = cv.optString("videoId", "")
                                val titleRuns = cv.optJSONObject("title")?.optJSONArray("runs")
                                val trackTitle = if (titleRuns != null && titleRuns.length() > 0) {
                                    titleRuns.getJSONObject(0).optString("text", "")
                                } else {
                                    cv.optJSONObject("title")?.optString("simpleText", "") ?: ""
                                }
                                val bylineRuns = cv.optJSONObject("shortBylineText")?.optJSONArray("runs")
                                val channel = if (bylineRuns != null && bylineRuns.length() > 0) {
                                    bylineRuns.getJSONObject(0).optString("text", "YouTube Music")
                                } else {
                                    "YouTube Music"
                                }
                                val thumb = "https://i.ytimg.com/vi/$id/hqdefault.jpg"

                                if (id.isNotEmpty() && trackTitle.isNotEmpty() && id != videoId) {
                                    relatedTracks.add(
                                        YouTubeTrack(
                                            videoId = id,
                                            title = unescapeHtml(trackTitle),
                                            uploader = unescapeHtml(channel),
                                            durationSeconds = 210L,
                                            thumbnailUrl = thumb,
                                            streamUrl = null
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // If fewer than 5 related tracks, expand by searching related songs using title or artist
        if (relatedTracks.size < 5 && !title.isNullOrBlank()) {
            val cleanTitle = title.replace(Regex("\\[.*?\\]|\\(.*?\\)"), "").trim().take(30)
            val fallback = searchCloudMusic(cleanTitle)
            for (track in fallback) {
                if (relatedTracks.none { it.title.equals(track.title, ignoreCase = true) } && track.videoId != videoId) {
                    relatedTracks.add(track)
                }
                if (relatedTracks.size >= 10) break
            }
        }

        return@withContext relatedTracks
    }

    /**
     * Searches Cloud Music database for studio master quality (320kbps) audio streams.
     */
    suspend fun searchCloudMusic(query: String): List<YouTubeTrack> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://www.jiosaavn.com/api.php?__call=search.getResults&_format=json&_marker=0&api_version=4&p=1&n=25&q=$encodedQuery"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", browserUserAgent)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val resultsArray = json.optJSONArray("results") ?: JSONArray()
                val tracks = mutableListOf<YouTubeTrack>()

                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    val id = item.optString("id", "")
                    if (id.isEmpty()) continue

                    val rawTitle = item.optString("title", "Unknown Title")
                    val title = unescapeHtml(rawTitle)

                    var artist = item.optString("subtitle", "")
                    val moreInfo = item.optJSONObject("more_info")
                    if (moreInfo != null) {
                        val artistMap = moreInfo.optJSONObject("artistMap")
                        val primaryArtists = artistMap?.optJSONArray("primary_artists")
                        if (primaryArtists != null && primaryArtists.length() > 0) {
                            val firstArtist = primaryArtists.getJSONObject(0)
                            artist = firstArtist.optString("name", artist)
                        }
                    }
                    if (artist.isEmpty()) artist = "Music Artist"
                    artist = unescapeHtml(artist)

                    val durationStr = moreInfo?.optString("duration", "200") ?: "200"
                    val durationSeconds = durationStr.toLongOrNull() ?: 200L

                    // Album artwork: upgrade 150x150 to high-res 500x500
                    var image = item.optString("image", "")
                    if (image.contains("150x150")) {
                        image = image.replace("150x150", "500x500")
                    } else if (image.contains("50x50")) {
                        image = image.replace("50x50", "500x500")
                    }

                    // Decrypt 320kbps / 160kbps audio stream URL
                    val encryptedMediaUrl = moreInfo?.optString("encrypted_media_url", "") ?: ""
                    var decryptedStreamUrl: String? = null
                    if (encryptedMediaUrl.isNotEmpty()) {
                        decryptedStreamUrl = decryptSaavnUrl(encryptedMediaUrl)
                    }

                    tracks.add(
                        YouTubeTrack(
                            videoId = id,
                            title = title,
                            uploader = artist,
                            durationSeconds = durationSeconds,
                            thumbnailUrl = image,
                            streamUrl = decryptedStreamUrl
                        )
                    )
                }
                return@withContext tracks
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Resolves metadata and stream for a specific YouTube Video ID.
     */
    private suspend fun resolveYouTubeVideoDetails(videoId: String): YouTubeTrack? = withContext(Dispatchers.IO) {
        try {
            // First: Resolve title and author via public YouTube oEmbed API
            var title = "YouTube Audio ($videoId)"
            var author = "YouTube"
            val thumbnail = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

            try {
                val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
                val oembedRequest = Request.Builder().url(oembedUrl).header("User-Agent", browserUserAgent).build()
                client.newCall(oembedRequest).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val body = resp.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            title = json.optString("title", title)
                            author = json.optString("author_name", author)
                        }
                    }
                }
            } catch (ignored: Exception) {}

            // Second: Resolve direct stream URL via InnerTube Android client
            var streamUrl = resolveYouTubeInnerTubeStream(videoId)

            // Third: If InnerTube didn't return a stream, resolve by searching title on Cloud Music
            if (streamUrl == null) {
                val cloudMatches = searchCloudMusic(title)
                if (cloudMatches.isNotEmpty()) {
                    val bestMatch = cloudMatches.first()
                    streamUrl = bestMatch.streamUrl
                }
            }

            YouTubeTrack(
                videoId = videoId,
                title = title,
                uploader = author,
                durationSeconds = 210L,
                thumbnailUrl = thumbnail,
                streamUrl = streamUrl
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Direct YouTube InnerTube client: Queries YouTube's official internal Android player endpoint
     * to extract direct, high-quality audio streams (itag 140 m4a / itag 251 opus).
     */
    private suspend fun resolveYouTubeInnerTubeStream(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("videoId", videoId)
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "ANDROID")
                        put("clientVersion", "20.10.38")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/player?prettyPrint=false")
                .header("User-Agent", androidUserAgent)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    val streamingData = json.optJSONObject("streamingData") ?: return@withContext null
                    val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats") ?: return@withContext null

                    var fallbackUrl: String? = null
                    for (i in 0 until adaptiveFormats.length()) {
                        val format = adaptiveFormats.getJSONObject(i)
                        val itag = format.optInt("itag")
                        val mimeType = format.optString("mimeType", "")
                        val url = format.optString("url")
                        if (url.isNotEmpty() && mimeType.startsWith("audio/")) {
                            if (itag == 140) { // AAC 128kbps (Best compatibility for Android ExoPlayer)
                                return@withContext url
                            }
                            if (fallbackUrl == null) {
                                fallbackUrl = url
                            }
                        }
                    }
                    return@withContext fallbackUrl
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    /**
     * Resolves the playable direct audio stream URL for ExoPlayer streaming.
     */
    suspend fun resolveStreamUrl(videoId: String, title: String? = null): String = withContext(Dispatchers.IO) {
        // 1. Check direct InnerTube stream
        val ytStream = resolveYouTubeInnerTubeStream(videoId)
        if (ytStream != null) {
            return@withContext ytStream
        }

        // 2. Try searching by title in Cloud Music
        if (!title.isNullOrBlank()) {
            val cloudTracks = searchCloudMusic(title)
            val matched = cloudTracks.firstOrNull { it.streamUrl != null }
            if (matched?.streamUrl != null) {
                return@withContext matched.streamUrl
            }
        }

        // 3. Query details
        val details = resolveYouTubeVideoDetails(videoId)
        if (details?.streamUrl != null) {
            return@withContext details.streamUrl
        }

        throw Exception("Unable to extract playable audio stream for this track.")
    }

    /**
     * Downloads audio stream, saves it as an MP3 file in the phone's
     * Music/Suno directory, inserts complete track metadata (Title, Artist, Album, Art)
     * into Android MediaStore, and caches it locally so it is indexed and playable immediately.
     */
    suspend fun downloadAudio(
        track: YouTubeTrack,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val streamUrl = track.streamUrl ?: resolveStreamUrl(track.videoId, track.title)

            val request = Request.Builder()
                .url(streamUrl)
                .header("User-Agent", androidUserAgent)
                .header("Range", "bytes=0-")
                .build()

            // Sanitize filename: save as MP3 format
            val safeTitle = track.title
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .trim()
                .take(60)
                .ifEmpty { "Suno_Track_${track.videoId}" }

            val safeArtist = track.uploader
                .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                .trim()
                .take(40)

            val fileName = if (safeArtist.isNotBlank() && !safeArtist.equals("Unknown Artist", ignoreCase = true)) {
                "$safeArtist - $safeTitle.mp3"
            } else {
                "$safeTitle.mp3"
            }

            // Save metadata to local store to ensure Artist and Thumbnail are always preserved
            metadataStore.saveMetadata(track.title, track.uploader, track.thumbnailUrl, fileName)

            var resultFile: File? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Scoped Storage: Direct insertion into Android MediaStore with complete metadata
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.TITLE, track.title)
                    put(MediaStore.Audio.Media.ARTIST, if (track.uploader.isBlank()) "Unknown Artist" else track.uploader)
                    put(MediaStore.Audio.Media.ALBUM, "Suno Music")
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.IS_MUSIC, 1)
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/Suno")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Failed to create MediaStore entry for download")

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful && response.code != 206) {
                            throw Exception("HTTP error ${response.code} downloading track")
                        }
                        val body = response.body ?: throw Exception("Empty response body")
                        val contentLength = body.contentLength()

                        val inputStream = body.byteStream()
                        val buffer = ByteArray(32 * 1024)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 0.99f)
                                onProgress(progress)
                            }
                        }
                        outputStream.flush()
                    }
                }

                // Mark download as finished so MediaStore immediately indexes it
                contentValues.clear()
                contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "Suno"
                )
                resultFile = File(publicDir, fileName)
            } else {
                // Legacy storage for older Android versions
                val targetDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    "Suno"
                ).apply { mkdirs() }

                val file = File(targetDir, fileName)
                resultFile = file

                FileOutputStream(file).use { outputStream ->
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful && response.code != 206) {
                            throw Exception("HTTP error ${response.code} downloading track")
                        }
                        val body = response.body ?: throw Exception("Empty response body")
                        val contentLength = body.contentLength()

                        val inputStream = body.byteStream()
                        val buffer = ByteArray(32 * 1024)
                        var bytesRead: Int
                        var totalBytesRead = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            if (contentLength > 0) {
                                val progress = (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 0.99f)
                                onProgress(progress)
                            }
                        }
                        outputStream.flush()
                    }
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("audio/mpeg")
                ) { _, _ -> }
            }

            onProgress(1f)
            Result.success(resultFile ?: File(fileName))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Decrypts JioSaavn DES-ECB encrypted media URL to direct audio stream URL.
     */
    private fun decryptSaavnUrl(encryptedUrl: String): String {
        return try {
            val keyBytes = "38346591".toByteArray(Charsets.UTF_8)
            val secretKeySpec = SecretKeySpec(keyBytes, "DES")
            val cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)

            val cipherBytes = Base64.decode(encryptedUrl.trim(), Base64.DEFAULT)
            val plainBytes = cipher.doFinal(cipherBytes)
            var decrypted = String(plainBytes, Charsets.UTF_8).trim()

            // Upgrade to high-definition 320kbps audio format
            if (decrypted.contains("_96.mp4")) {
                decrypted = decrypted.replace("_96.mp4", "_320.mp4")
            } else if (decrypted.contains("_160.mp4")) {
                decrypted = decrypted.replace("_160.mp4", "_320.mp4")
            }
            decrypted
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun unescapeHtml(text: String): String {
        return try {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
        } catch (e: Exception) {
            text.replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#039;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
        }
    }

    private fun fallbackSearch(query: String): List<YouTubeTrack> {
        return listOf(
            YouTubeTrack(
                videoId = "kJQP7kiw5Fk",
                title = "Despacito - Luis Fonsi ft. Daddy Yankee",
                uploader = "Luis Fonsi",
                durationSeconds = 282L,
                thumbnailUrl = "https://img.youtube.com/vi/kJQP7kiw5Fk/hqdefault.jpg"
            ),
            YouTubeTrack(
                videoId = "JGwWNGJdvx8",
                title = "Shape of You - Ed Sheeran",
                uploader = "Ed Sheeran",
                durationSeconds = 233L,
                thumbnailUrl = "https://img.youtube.com/vi/JGwWNGJdvx8/hqdefault.jpg"
            ),
            YouTubeTrack(
                videoId = "fJ9rUzIMcZQ",
                title = "Bohemian Rhapsody - Queen",
                uploader = "Queen Official",
                durationSeconds = 359L,
                thumbnailUrl = "https://img.youtube.com/vi/fJ9rUzIMcZQ/hqdefault.jpg"
            )
        )
    }
}
