package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songTitle: String,
    val artistName: String,
    val albumName: String = "",
    val albumArtUrl: String? = null,
    val spotifyUrl: String = "",
    val note: String,
    val mood: String = "🎵 General",
    val timestamp: Long = System.currentTimeMillis()
)
