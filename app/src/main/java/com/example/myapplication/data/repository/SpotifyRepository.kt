package com.example.myapplication.data.repository

import android.util.Base64
import android.util.Log
import com.example.myapplication.data.model.SpotifyTokenResponse
import com.example.myapplication.data.model.SpotifyTrack
import com.example.myapplication.data.remote.SpotifyApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SpotifyRepository {

    private var clientId: String = ""
    private var clientSecret: String = ""
    private var userAccessToken: String? = null

    private var cachedToken: String? = null
    private var tokenExpirationTime: Long = 0

    var lastErrorMessage: String? = null
        private set

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val apiService: SpotifyApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyApiService::class.java)
    }

    fun setCredentials(id: String, secret: String) {
        this.clientId = sanitize(id)
        this.clientSecret = sanitize(secret)
        this.cachedToken = null
        this.lastErrorMessage = null
        Log.d("SpotifyRepository", "Credentials set. ID length: ${clientId.length}, Secret length: ${clientSecret.length}")
    }

    fun setUserAccessToken(token: String) {
        this.userAccessToken = sanitize(token)
        this.lastErrorMessage = null
    }

    private fun sanitize(input: String): String {
        return input.trim()
            .replace("\"", "")
            .replace("'", "")
            .replace(" ", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace("\t", "")
    }

    suspend fun searchTracks(query: String): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        val token = if (!userAccessToken.isNullOrBlank()) userAccessToken else getValidClientToken()
        if (token.isNullOrBlank()) {
            val err = lastErrorMessage ?: "Missing or invalid Spotify Client ID / Client Secret"
            Log.w("SpotifyRepository", "Search aborted: $err")
            return@withContext Result.failure(Exception(err))
        }

        try {
            val response = apiService.searchTracks("Bearer $token", query)
            val tracks = response.tracks?.items?.map { it.toDomainModel() } ?: emptyList()
            Log.d("SpotifyRepository", "Search found ${tracks.size} tracks for query: $query")
            Result.success(tracks)
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Search failed: ${e.localizedMessage}", e)
            lastErrorMessage = "Search Error: ${e.localizedMessage}"
            Result.failure(e)
        }
    }

    suspend fun getRecentlyPlayed(): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        val token = userAccessToken
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Requires User OAuth Token"))
        }

        try {
            val response = apiService.getRecentlyPlayed("Bearer $token")
            val tracks = response.items?.mapNotNull { it.track?.toDomainModel() } ?: emptyList()
            Result.success(tracks)
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Get Recently Played failed: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    suspend fun getLikedSongs(): Result<List<SpotifyTrack>> = withContext(Dispatchers.IO) {
        val token = userAccessToken
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Requires User OAuth Token"))
        }

        try {
            val response = apiService.getLikedSongs("Bearer $token")
            val tracks = response.items?.mapNotNull { it.track?.toDomainModel() } ?: emptyList()
            Result.success(tracks)
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Get Liked Songs failed: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    private suspend fun getValidClientToken(): String? = withContext(Dispatchers.IO) {
        val cleanId = sanitize(clientId)
        val cleanSecret = sanitize(clientSecret)

        if (cleanId.isBlank() || cleanSecret.isBlank()) {
            lastErrorMessage = "Client ID or Client Secret is empty. Please enter them in Spotify Settings."
            return@withContext null
        }

        val now = System.currentTimeMillis()
        if (!cachedToken.isNullOrBlank() && now < tokenExpirationTime) {
            return@withContext cachedToken
        }

        try {
            val credentials = "$cleanId:$cleanSecret"
            val encodedAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP).trim()

            val requestBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build()

            val request = Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .header("Authorization", encodedAuth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val tokenResponse = Gson().fromJson(responseBody, SpotifyTokenResponse::class.java)
                cachedToken = tokenResponse.accessToken
                tokenExpirationTime = now + (tokenResponse.expiresIn - 60) * 1000L
                lastErrorMessage = null
                Log.d("SpotifyRepository", "Spotify Client Token obtained successfully!")
                return@withContext cachedToken
            } else {
                Log.e("SpotifyRepository", "Spotify Token HTTP ${response.code}: $responseBody")
                lastErrorMessage = "Spotify HTTP ${response.code}: $responseBody"
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("SpotifyRepository", "Token fetch exception: ${e.localizedMessage}", e)
            lastErrorMessage = "Network Error: ${e.localizedMessage}"
            return@withContext null
        }
    }
}
