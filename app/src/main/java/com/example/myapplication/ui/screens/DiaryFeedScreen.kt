package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.model.DiaryEntry
import com.example.myapplication.ui.theme.BorderLight
import com.example.myapplication.ui.theme.GreenPrimary
import com.example.myapplication.ui.theme.SurfaceLight
import com.example.myapplication.ui.theme.TextDark
import com.example.myapplication.ui.theme.TextGray
import com.example.myapplication.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryFeedScreen(
    viewModel: DiaryViewModel,
    onNavigateToAdd: () -> Unit
) {
    val entries by viewModel.diaryEntries.collectAsState()
    val context = LocalContext.current
    var showSpotifyConfigDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Music Diary",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSpotifyConfigDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Spotify API Setup",
                            tint = GreenPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Your diary is empty.",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 16.sp,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToAdd,
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Add First Song Entry", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        SimpleDiaryCard(
                            entry = entry,
                            onShare = { viewModel.shareEntry(context, entry) },
                            onDelete = { viewModel.deleteEntry(entry) },
                            onOpenSpotify = {
                                if (entry.spotifyUrl.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(entry.spotifyUrl))
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSpotifyConfigDialog) {
        SpotifyConfigDialog(
            viewModel = viewModel,
            onDismiss = { showSpotifyConfigDialog = false }
        )
    }
}

@Composable
fun SimpleDiaryCard(
    entry: DiaryEntry,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onOpenSpotify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Cover art + Song Info + Mood
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!entry.albumArtUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = entry.albumArtUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = GreenPrimary)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.songTitle,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.artistName,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = entry.mood,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Note Memory
            Text(
                text = entry.note,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                color = TextDark,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Date + Action buttons (Share, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formattedDate = DateFormat.format("dd MMM yyyy", entry.timestamp).toString()
                Text(
                    text = formattedDate,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = TextGray
                )

                Row {
                    TextButton(onClick = onOpenSpotify) {
                        Text("Spotify", color = GreenPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = GreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
