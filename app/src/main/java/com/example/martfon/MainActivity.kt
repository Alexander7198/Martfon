package com.example.martfon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.martfon.domain.presentation.screens.ChatScreen
import com.example.martfon.ui.theme.MartfonTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MartfonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 🚫 Аутентификация ПОЛНОСТЬЮ ОТКЛЮЧЕНА
                    // Сразу показываем чат с тестовыми данными
                    ChatScreen(
                        chatId = "1f57594a-eea1-4a7e-8ff7-258ac90366a8",
                        firebaseToken = "test_user_123",     // Вы - test_user_123
                        otherUserId = "user_89892488093",    // Тестовый собеседник
                        onLogout = {
                            // При выходе просто показываем тот же экран
                            // Можно добавить перезапуск если нужно
                            println("🔄 Выход из тестового режима")
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    MartfonTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ChatScreen(
                chatId = "1f57594a-eea1-4a7e-8ff7-258ac90366a8",
                firebaseToken = "preview_token",
                otherUserId = "test_user_123",
                onLogout = {}
            )
        }
    }
}