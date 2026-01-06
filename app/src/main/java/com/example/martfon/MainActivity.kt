package com.example.martfon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.martfon.domain.presentation.screens.ChatScreen
import com.example.martfon.ui.theme.MartfonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent() // Используем отдельную функцию
        }
    }
}

@Composable
fun AppContent() {
    MartfonTheme { // Используем правильное имя темы
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    AppContent()
}