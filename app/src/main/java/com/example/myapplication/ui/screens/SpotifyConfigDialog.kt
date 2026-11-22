package com.example.myapplication.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.GreenPrimary
import com.example.myapplication.ui.theme.TextDark
import com.example.myapplication.ui.theme.TextGray
import com.example.myapplication.ui.viewmodel.DiaryViewModel

@Composable
fun SpotifyConfigDialog(
    viewModel: DiaryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var clientId by remember { mutableStateOf("") }
    var clientSecret by remember { mutableStateOf("") }
    var userToken by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = GreenPrimary, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "Spotify Connection",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Tip: You can always use 'Add Own Song' to type any song manually without logging in! Or login via Spotify below:",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = clientId,
                    onValueChange = { clientId = it },
                    label = { Text("Spotify Client ID", fontFamily = FontFamily.SansSerif) },
                    placeholder = { Text("Enter 32-char Client ID") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 1-Click Login Button with response_type=code
                Button(
                    onClick = {
                        val cleanId = clientId.trim()
                        if (cleanId.isBlank()) {
                            Toast.makeText(context, "Please enter your Spotify Client ID first!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.setSpotifyCredentials(cleanId, "")
                            val redirectUri = "myapplication://spotify-callback"
                            val scope = "user-read-recently-played%20user-library-read%20playlist-read-private"
                            val authUrl = "https://accounts.spotify.com/authorize?client_id=$cleanId&response_type=code&redirect_uri=$redirectUri&scope=$scope"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                            context.startActivity(intent)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Login with Spotify (Code Flow)", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Or enter Client Secret / Manual Token:",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = clientSecret,
                    onValueChange = { clientSecret = it },
                    label = { Text("Spotify Client Secret", fontFamily = FontFamily.SansSerif) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userToken,
                    onValueChange = { userToken = it },
                    label = { Text("Manual OAuth Token", fontFamily = FontFamily.SansSerif) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (clientId.isNotBlank()) {
                        viewModel.setSpotifyCredentials(clientId.trim(), clientSecret.trim())
                    }
                    if (userToken.isNotBlank()) {
                        viewModel.setUserToken(userToken.trim())
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Save Credentials", fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontFamily = FontFamily.SansSerif)
            }
        }
    )
}
