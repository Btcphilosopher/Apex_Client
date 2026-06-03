package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.ApexLauncherDashboard
import com.example.ui.GameRunnerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ApexViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: ApexViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          if (viewModel.activeRunningGame != null) {
            GameRunnerScreen(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          } else {
            ApexLauncherDashboard(
              viewModel = viewModel,
              modifier = Modifier.padding(innerPadding)
            )
          }
        }
      }
    }
  }
}

