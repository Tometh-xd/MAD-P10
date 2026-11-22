package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class SpotifyTrack(
    val id: String,
    val name: String,
    val artistName: String,
    val albumName: String,
    val albumArtUrl: String?,
    val previewUrl: String?,
    val spotifyUrl: String
)

// DTOs for Spotify Web API JSON Parsing
data class SpotifySearchResponse(
    @SerializedName("tracks") val tracks: SpotifyTracksPaging?
)

data class SpotifyTracksPaging(
    @SerializedName("items") val items: List<SpotifyTrackDto>?
)

data class SpotifyTrackDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artists") val artists: List<SpotifyArtistDto>?,
    @SerializedName("album") val album: SpotifyAlbumDto?,
    @SerializedName("preview_url") val previewUrl: String?,
    @SerializedName("external_urls") val externalUrls: SpotifyExternalUrlsDto?
) {
    fun toDomainModel(): SpotifyTrack {
        val mainArtist = artists?.firstOrNull()?.name ?: "Unknown Artist"
        val artUrl = album?.images?.firstOrNull()?.url
        val webUrl = externalUrls?.spotify ?: "https://open.spotify.com/track/$id"
        return SpotifyTrack(
            id = id,
            name = name,
            artistName = mainArtist,
            albumName = album?.name ?: "Single",
            albumArtUrl = artUrl,
            previewUrl = previewUrl,
            spotifyUrl = webUrl
        )
    }
}

data class SpotifyArtistDto(
    @SerializedName("name") val name: String
)

data class SpotifyAlbumDto(
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<SpotifyImageDto>?
)

data class SpotifyImageDto(
    @SerializedName("url") val url: String,
    @SerializedName("height") val height: Int?,
    @SerializedName("width") val width: Int?
)

data class SpotifyExternalUrlsDto(
    @SerializedName("spotify") val spotify: String?
)

data class SpotifyTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class SpotifyRecentlyPlayedResponse(
    @SerializedName("items") val items: List<SpotifyPlayHistoryItemDto>?
)

data class SpotifyPlayHistoryItemDto(
    @SerializedName("track") val track: SpotifyTrackDto?
)

data class SpotifySavedTracksResponse(
    @SerializedName("items") val items: List<SpotifySavedTrackItemDto>?
)

data class SpotifySavedTrackItemDto(
    @SerializedName("track") val track: SpotifyTrackDto?
)
