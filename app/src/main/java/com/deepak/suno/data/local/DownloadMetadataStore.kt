package com.deepak.suno.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadMetadataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("suno_downloaded_meta", Context.MODE_PRIVATE)

    fun saveMetadata(title: String, artist: String, artworkUrl: String, fileName: String) {
        try {
            val json = JSONObject().apply {
                put("title", title)
                put("artist", artist)
                put("artworkUrl", artworkUrl)
                put("fileName", fileName)
            }
            val cleanTitleKey = title.lowercase().trim().replace(Regex("[^a-z0-9]"), "")
            val cleanFileKey = fileName.lowercase().trim().replace(Regex("[^a-z0-9]"), "")

            val currentSet = prefs.getStringSet("downloaded_keys", emptySet())?.toMutableSet() ?: mutableSetOf()
            currentSet.add(cleanTitleKey)
            currentSet.add(cleanFileKey)

            prefs.edit()
                .putString("title_$cleanTitleKey", json.toString())
                .putString("file_$cleanFileKey", json.toString())
                .putStringSet("downloaded_keys", currentSet)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isDownloaded(title: String, fileName: String? = null): Boolean {
        return try {
            val cleanTitleKey = title.lowercase().trim().replace(Regex("[^a-z0-9]"), "")
            val currentSet = prefs.getStringSet("downloaded_keys", emptySet()) ?: emptySet()
            if (currentSet.contains(cleanTitleKey)) return true
            if (fileName != null) {
                val cleanFileKey = fileName.lowercase().trim().replace(Regex("[^a-z0-9]"), "")
                if (currentSet.contains(cleanFileKey)) return true
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun getMetadata(title: String, fileName: String? = null): JSONObject? {
        return try {
            val cleanTitleKey = title.lowercase().trim().replace(Regex("[^a-z0-9]"), "")
            val rawTitle = prefs.getString("title_$cleanTitleKey", null)
            if (rawTitle != null) {
                return JSONObject(rawTitle)
            }
            if (fileName != null) {
                val cleanFileKey = fileName.lowercase().trim().replace(Regex("[^a-z0-9]"), "")
                val rawFile = prefs.getString("file_$cleanFileKey", null)
                if (rawFile != null) {
                    return JSONObject(rawFile)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}