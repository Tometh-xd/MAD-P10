package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.DiaryStorage
import com.example.myapplication.ui.screens.AddEntryScreen
import com.example.myapplication.ui.screens.DiaryFeedScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.DiaryViewModel
import com.example.myapplication.ui.viewmodel.DiaryViewModelFactory

class MainActivity : ComponentActivity() {

    private var currentIntentState by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntentState = intent
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MusicDiaryApp(intent = currentIntentState)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentState = intent
    }
}

@Composable
fun MusicDiaryApp(intent: Intent?) {
    val context = LocalContext.current
    val storage = remember { DiaryStorage(context.applicationContext) }
    val viewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModelFactory(storage)
    )

    // Extract Spotify Token from Deep Link redirect if user logs in via browser
    LaunchedEffect(intent) {
        val uri = intent?.data
        if (uri != null && uri.scheme == "myapplication" && uri.host == "spotify-callback") {
            val code = uri.getQueryParameter("code")
            val accessToken = uri.getQueryParameter("access_token")
            val fragment = uri.fragment
            if (!accessToken.isNullOrBlank()) {
                viewModel.setUserToken(accessToken)
                Toast.makeText(context, "Connected to Spotify successfully!", Toast.LENGTH_LONG).show()
            } else if (!code.isNullOrBlank()) {
                Toast.makeText(context, "Spotify Auth Code received! Tip: You can also use 'Add Own Song' anytime.", Toast.LENGTH_LONG).show()
            } else if (fragment != null && fragment.contains("access_token=")) {
                val token = fragment.substringAfter("access_token=").substringBefore("&")
                if (token.isNotBlank()) {
                    viewModel.setUserToken(token)
                    Toast.makeText(context, "Connected to Spotify successfully!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "feed"
    ) {
        composable("feed") {
            DiaryFeedScreen(
                viewModel = viewModel,
                onNavigateToAdd = {
                    navController.navigate("add")
                }
            )
        }
        composable("add") {
            AddEntryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
