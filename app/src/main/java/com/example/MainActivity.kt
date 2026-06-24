package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.ToolRepository
import com.example.ui.MainScreen
import com.example.ui.ToolCraftViewModel
import com.example.ui.ToolCraftViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val database = AppDatabase.getDatabase(context)
        val repository = ToolRepository(database.toolDao())
        val viewModel: ToolCraftViewModel = viewModel(
          factory = ToolCraftViewModelFactory(repository)
        )
        MainScreen(viewModel = viewModel)
      }
    }
  }
}
