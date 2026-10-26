package com.example.martfon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.martfon.domain.presentation.screens.ChatScreen
import com.example.martfon.ui.theme.MartfonTheme
import androidx.compose.foundation.clickable



data class TestUser(
    val uid: String,
    val displayName: String,
    val phoneNumber: String = "",
    val status: String = "online"
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MartfonTheme {
                // Состояния для пользователей
                var currentUser by remember { mutableStateOf<TestUser?>(null) }
                var otherUser by remember { mutableStateOf<TestUser?>(null) }
                var showUserSelector by remember { mutableStateOf(true) }

                // Список тестовых пользователей
                val testUsers = listOf(
                    TestUser(
                        uid = "test_user_123",
                        displayName = "User_123",
                        phoneNumber = "+79991234567",
                        status = "online"
                    ),
                    TestUser(
                        uid = "user_89892488093",
                        displayName = "User_8093",
                        phoneNumber = "+89892488093",
                        status = "online"
                    )
                )

                if (showUserSelector) {
                    // Экран выбора пользователя
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Выберите пользователя",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )

                            testUsers.forEach { user ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable {
                                            currentUser = user
                                            otherUser = testUsers.first { it.uid != user.uid }
                                            showUserSelector = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = user.displayName,
                                            style = MaterialTheme.typography.titleLarge,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = user.uid,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Чат
                    ChatScreen(
                        chatId = "1f57594a-eea1-4a7e-8ff7-258ac90366a8",
                        firebaseToken = currentUser!!.uid,
                        otherUserId = otherUser!!.uid,
                        onLogout = {
                            // Выход - возвращаемся к выбору пользователя
                            showUserSelector = true
                        },
                        onSwitchUser = {
                            // Смена пользователей (меняем местами)
                            val temp = currentUser
                            currentUser = otherUser
                            otherUser = temp
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
            // Для превью показываем просто текст
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Приложение запущено")
            }
        }
    }
}