package com.example.myapplication.data.remote

import com.example.myapplication.data.model.SpotifyRecentlyPlayedResponse
import com.example.myapplication.data.model.SpotifySavedTracksResponse
import com.example.myapplication.data.model.SpotifySearchResponse
import com.example.myapplication.data.model.SpotifyTokenResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SpotifyApiService {

    @GET("v1/search")
    suspend fun searchTracks(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("type") type: String = "track",
        @Query("limit") limit: Int = 20
    ): SpotifySearchResponse

    @GET("v1/me/player/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): SpotifyRecentlyPlayedResponse

    @GET("v1/me/tracks")
    suspend fun getLikedSongs(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): SpotifySavedTracksResponse

    @FormUrlEncoded
    @POST("https://accounts.spotify.com/api/token")
    suspend fun getAccessToken(
        @Header("Authorization") basicAuth: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse
}
