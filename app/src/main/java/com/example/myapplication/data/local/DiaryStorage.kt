package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.model.DiaryEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DiaryStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("music_diary_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getEntries(): List<DiaryEntry> {
        val json = prefs.getString("entries_list", null) ?: return defaultSampleEntries()
        return try {
            val type = object : TypeToken<List<DiaryEntry>>() {}.type
            gson.fromJson(json, type) ?: defaultSampleEntries()
        } catch (e: Exception) {
            defaultSampleEntries()
        }
    }

    fun saveEntry(entry: DiaryEntry) {
        val current = getEntries().toMutableList()
        current.add(0, entry)
        saveList(current)
    }

    fun deleteEntry(id: Long) {
        val current = getEntries().filter { it.id != id }
        saveList(current)
    }

    private fun saveList(list: List<DiaryEntry>) {
        val json = gson.toJson(list)
        prefs.edit().putString("entries_list", json).apply()
    }

    fun saveSpotifyCredentials(clientId: String, clientSecret: String) {
        prefs.edit()
            .putString("spotify_client_id", clientId)
            .putString("spotify_client_secret", clientSecret)
            .apply()
    }

    fun getSpotifyClientId(): String = prefs.getString("spotify_client_id", "") ?: ""
    fun getSpotifyClientSecret(): String = prefs.getString("spotify_client_secret", "") ?: ""

    fun saveSpotifyUserToken(token: String) {
        prefs.edit().putString("spotify_user_token", token).apply()
    }

    fun getSpotifyUserToken(): String = prefs.getString("spotify_user_token", "") ?: ""

    private fun defaultSampleEntries(): List<DiaryEntry> {
        return listOf(
            DiaryEntry(
                id = 1,
                songTitle = "Blinding Lights",
                artistName = "The Weeknd",
                albumArtUrl = "https://i.scdn.co/image/ab67616d0000b273886370c04766067b540e3642",
                spotifyUrl = "https://open.spotify.com/track/0VjIjW4GlUZAMYd2vXMi3b",
                note = "Great start of the week! Felt super energetic listening to this track on my morning walk.",
                mood = "🔥 Energetic",
                timestamp = System.currentTimeMillis() - 86400000L
            ),
            DiaryEntry(
                id = 2,
                songTitle = "As It Was",
                artistName = "Harry Styles",
                albumArtUrl = "https://i.scdn.co/image/ab67616d0000b273b46f74097655d7f353caab14",
                spotifyUrl = "https://open.spotify.com/track/4DvkF65SpRjLjGvi7flCjB",
                note = "Relaxing evening at home. This song always puts me in a good mood.",
                mood = "✨ Happy",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
