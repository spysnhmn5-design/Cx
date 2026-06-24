package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.BuildConfig

@Composable
fun MainScreen(
    viewModel: ToolCraftViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    
    // Safely retrieve the Gemini API Key from BuildConfig
    val apiKey = try {
        BuildConfig.GEMINI_API_KEY
    } catch (e: Exception) {
        ""
    }.trim()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Crossfade(
            targetState = currentScreen,
            modifier = Modifier.padding(innerPadding),
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        apiKey = apiKey
                    )
                }
                is Screen.Create -> {
                    CreateScreen(
                        viewModel = viewModel,
                        apiKey = apiKey,
                        prompt = screen.prompt
                    )
                }
                is Screen.Sandbox -> {
                    SandboxScreen(
                        viewModel = viewModel,
                        apiKey = apiKey,
                        tool = screen.tool
                    )
                }
                is Screen.Editor -> {
                    EditorScreen(
                        viewModel = viewModel,
                        tool = screen.tool
                    )
                }
            }
        }
    }
}
