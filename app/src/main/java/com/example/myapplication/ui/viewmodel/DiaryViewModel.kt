package com.example.myapplication.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.DiaryStorage
import com.example.myapplication.data.model.DiaryEntry
import com.example.myapplication.data.model.SpotifyTrack
import com.example.myapplication.data.repository.SpotifyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiaryViewModel(
    private val storage: DiaryStorage,
    private val spotifyRepository: SpotifyRepository = SpotifyRepository()
) : ViewModel() {

    private val _diaryEntries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryEntries: StateFlow<List<DiaryEntry>> = _diaryEntries.asStateFlow()

    // Spotify Source Tab: "search", "recently_played", "liked", "custom"
    private val _songSourceTab = MutableStateFlow("search")
    val songSourceTab: StateFlow<String> = _songSourceTab.asStateFlow()

    // Spotify Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SpotifyTrack>>(emptyList())
    val searchResults: StateFlow<List<SpotifyTrack>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _apiErrorMessage = MutableStateFlow<String?>(null)
    val apiErrorMessage: StateFlow<String?> = _apiErrorMessage.asStateFlow()

    // New Entry Form State
    private val _selectedTrack = MutableStateFlow<SpotifyTrack?>(null)
    val selectedTrack: StateFlow<SpotifyTrack?> = _selectedTrack.asStateFlow()

    private val _noteText = MutableStateFlow("")
    val noteText: StateFlow<String> = _noteText.asStateFlow()

    private val _selectedMood = MutableStateFlow("✨ Happy")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    val availableMoods = listOf(
        "✨ Happy",
        "🌧️ Nostalgic",
        "🔥 Energetic",
        "😌 Chill",
        "💔 Heartbreak",
        "🌙 Night Vibes",
        "🚗 Roadtrip"
    )

    init {
        val savedId = storage.getSpotifyClientId()
        val savedSecret = storage.getSpotifyClientSecret()
        val savedToken = storage.getSpotifyUserToken()
        if (savedId.isNotBlank() && savedSecret.isNotBlank()) {
            spotifyRepository.setCredentials(savedId, savedSecret)
        }
        if (savedToken.isNotBlank()) {
            spotifyRepository.setUserAccessToken(savedToken)
        }
        loadEntries()
    }

    private fun loadEntries() {
        _diaryEntries.value = storage.getEntries()
    }

    fun setSongSourceTab(tab: String) {
        _songSourceTab.value = tab
        viewModelScope.launch {
            _isSearching.value = true
            _apiErrorMessage.value = null
            when (tab) {
                "recently_played" -> {
                    val result = spotifyRepository.getRecentlyPlayed()
                    _searchResults.value = result.getOrDefault(emptyList())
                    _apiErrorMessage.value = spotifyRepository.lastErrorMessage
                }
                "liked" -> {
                    val result = spotifyRepository.getLikedSongs()
                    _searchResults.value = result.getOrDefault(emptyList())
                    _apiErrorMessage.value = spotifyRepository.lastErrorMessage
                }
                "search" -> {
                    onSearchQueryChanged(_searchQuery.value)
                }
                else -> {
                    _searchResults.value = emptyList()
                }
            }
            _isSearching.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
                _apiErrorMessage.value = null
                return@launch
            }
            _isSearching.value = true
            _apiErrorMessage.value = null
            val result = spotifyRepository.searchTracks(query)
            _searchResults.value = result.getOrDefault(emptyList())
            _apiErrorMessage.value = spotifyRepository.lastErrorMessage
            _isSearching.value = false
        }
    }

    fun selectTrack(track: SpotifyTrack?) {
        _selectedTrack.value = track
    }

    fun selectCustomTrack(title: String, artist: String) {
        val cleanTitle = title.trim().ifBlank { "Custom Song" }
        val cleanArtist = artist.trim().ifBlank { "Unknown Artist" }
        val customTrack = SpotifyTrack(
            id = "custom_${System.currentTimeMillis()}",
            name = cleanTitle,
            artistName = cleanArtist,
            albumName = "Custom Entry",
            albumArtUrl = null,
            previewUrl = null,
            spotifyUrl = "https://open.spotify.com/search/${cleanTitle.replace(" ", "%20")}"
        )
        _selectedTrack.value = customTrack
    }

    fun setSpotifyCredentials(clientId: String, clientSecret: String) {
        storage.saveSpotifyCredentials(clientId, clientSecret)
        spotifyRepository.setCredentials(clientId, clientSecret)
        if (_searchQuery.value.isNotBlank()) {
            onSearchQueryChanged(_searchQuery.value)
        }
    }

    fun setUserToken(token: String) {
        storage.saveSpotifyUserToken(token)
        spotifyRepository.setUserAccessToken(token)
        setSongSourceTab(_songSourceTab.value)
    }

    fun onNoteTextChanged(note: String) {
        _noteText.value = note
    }

    fun onMoodSelected(mood: String) {
        _selectedMood.value = mood
    }

    fun saveDiaryEntry(onSuccess: () -> Unit) {
        val track = _selectedTrack.value ?: return
        val note = _noteText.value.trim()
        if (note.isBlank()) return

        val entry = DiaryEntry(
            id = System.currentTimeMillis(),
            songTitle = track.name,
            artistName = track.artistName,
            albumName = track.albumName,
            albumArtUrl = track.albumArtUrl,
            spotifyUrl = track.spotifyUrl,
            note = note,
            mood = _selectedMood.value,
            timestamp = System.currentTimeMillis()
        )

        storage.saveEntry(entry)
        loadEntries()
        resetAddForm()
        onSuccess()
    }

    fun deleteEntry(entry: DiaryEntry) {
        storage.deleteEntry(entry.id)
        loadEntries()
    }

    private fun resetAddForm() {
        _selectedTrack.value = null
        _noteText.value = ""
        _selectedMood.value = "✨ Happy"
        _searchQuery.value = ""
        _songSourceTab.value = "search"
        _searchResults.value = emptyList()
        _apiErrorMessage.value = null
    }

    fun shareEntry(context: Context, entry: DiaryEntry) {
        val shareText = """
            🎵 Music Diary:
            "${entry.songTitle}" by ${entry.artistName}
            Mood: ${entry.mood}
            
            Memory: "${entry.note}"
            
            Spotify Link: ${entry.spotifyUrl}
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Music Diary")
        context.startActivity(shareIntent)
    }
}

class DiaryViewModelFactory(
    private val storage: DiaryStorage
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(storage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
