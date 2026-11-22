package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.BorderLight
import com.example.myapplication.ui.theme.GreenPrimary
import com.example.myapplication.ui.theme.SurfaceLight
import com.example.myapplication.ui.theme.TextDark
import com.example.myapplication.ui.theme.TextGray
import com.example.myapplication.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: DiaryViewModel,
    onNavigateBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val songSourceTab by viewModel.songSourceTab.collectAsState()
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val noteText by viewModel.noteText.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val apiErrorMessage by viewModel.apiErrorMessage.collectAsState()

    var customSongTitle by remember { mutableStateOf("") }
    var customArtistName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Entry", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, color = TextDark) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Song Selection Header
            Text(
                text = "1. Select or Enter a Song",
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTrack == null) {
                // Spotify Source Tabs (Search, Recent, Liked, Custom)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = songSourceTab == "search",
                            onClick = { viewModel.setSongSourceTab("search") },
                            label = { Text("Search", fontFamily = FontFamily.SansSerif) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = songSourceTab == "recently_played",
                            onClick = { viewModel.setSongSourceTab("recently_played") },
                            label = { Text("Recent", fontFamily = FontFamily.SansSerif) },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = songSourceTab == "liked",
                            onClick = { viewModel.setSongSourceTab("liked") },
                            label = { Text("Liked", fontFamily = FontFamily.SansSerif) },
                            leadingIcon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = songSourceTab == "custom",
                            onClick = { viewModel.setSongSourceTab("custom") },
                            label = { Text("Add Own Song", fontFamily = FontFamily.SansSerif) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (songSourceTab == "custom") {
                    // Custom Song Input Form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Enter Song Details Manually", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customSongTitle,
                                onValueChange = { customSongTitle = it },
                                label = { Text("Song Title") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customArtistName,
                                onValueChange = { customArtistName = it },
                                label = { Text("Artist Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (customSongTitle.isNotBlank()) {
                                        viewModel.selectCustomTrack(customSongTitle, customArtistName)
                                    }
                                },
                                enabled = customSongTitle.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Use This Song", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    if (songSourceTab == "search") {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search any song or artist...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreenPrimary) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GreenPrimary,
                                unfocusedBorderColor = BorderLight
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        if (searchResults.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val emptyMessage = apiErrorMessage ?: when (songSourceTab) {
                                    "search" -> if (searchQuery.isBlank()) "Type a song or artist name above to search Spotify..." else "No Spotify results found for '$searchQuery'. Try another search or use 'Add Own Song'."
                                    "recently_played" -> "No Recently Played tracks found. Connect Spotify token or search above!"
                                    "liked" -> "No Liked Songs found. Connect Spotify token or search above!"
                                    else -> "No songs available."
                                }
                                Text(
                                    text = emptyMessage,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp,
                                    color = TextGray
                                )
                            }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(4.dp)) {
                                items(searchResults) { track ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectTrack(track) }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!track.albumArtUrl.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = track.albumArtUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp)).background(GreenPrimary.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = GreenPrimary)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(track.name, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(track.artistName, fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = TextGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Selected Song Display Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    border = BorderStroke(1.dp, GreenPrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!selectedTrack?.albumArtUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = selectedTrack?.albumArtUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)).background(GreenPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(selectedTrack?.name ?: "", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                            Text(selectedTrack?.artistName ?: "", fontFamily = FontFamily.SansSerif, fontSize = 13.sp, color = TextGray)
                        }
                        IconButton(onClick = { viewModel.selectTrack(null) }) {
                            Icon(Icons.Default.Close, contentDescription = "Change", tint = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mood Section
            Text(
                text = "2. Select Mood",
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.availableMoods) { mood ->
                    val isSelected = selectedMood == mood
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onMoodSelected(mood) },
                        label = { Text(mood, fontFamily = FontFamily.SansSerif, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note Section
            Text(
                text = "3. Write Your Everyday Memory / Note",
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GreenPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { viewModel.onNoteTextChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                placeholder = { Text("Write your thoughts or diary entry for today...") },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderLight
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            val canSave = selectedTrack != null && noteText.trim().isNotBlank()

            Button(
                onClick = {
                    viewModel.saveDiaryEntry { onNavigateBack() }
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = SurfaceLight,
                    disabledContentColor = TextGray
                )
            ) {
                Text("Save Entry", fontFamily = FontFamily.SansSerif, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
