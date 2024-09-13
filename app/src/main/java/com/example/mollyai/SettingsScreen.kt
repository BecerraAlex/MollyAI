package com.example.mollyai

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Step 1: Create the settings page Composable function
@Composable
fun SettingsScreen() {
    Text("Settings Page")  // This is a simple placeholder text for the Settings screen
}

// Step 2: Add a preview to see it in Android Studio's preview window
@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen()
}
