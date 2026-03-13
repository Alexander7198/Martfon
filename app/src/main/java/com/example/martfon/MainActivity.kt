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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.martfon.data.local.UserPreferences
import com.example.martfon.domain.presentation.screens.ChatScreen
import com.example.martfon.domain.presentation.screens.LoginScreen
import com.example.martfon.domain.presentation.viewmodels.LoginViewModel
import com.example.martfon.ui.theme.MartfonTheme

class MainActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)

        setContent {
            MartfonTheme {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(userPreferences)
                )

                var isLoggedIn by remember { mutableStateOf(false) }
                var authToken by remember { mutableStateOf<String?>(null) }
                var currentUserId by remember { mutableStateOf<String?>(null) }
                var otherUserId by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    println("🔍 Запускаем проверку авторизации")
                    userPreferences.isLoggedIn.collect { loggedIn ->
                        println("📱 userPreferences.isLoggedIn = $loggedIn")
                        isLoggedIn = loggedIn
                        if (loggedIn) {
                            userPreferences.authToken.collect { token ->
                                println("🔑 Получен токен из preferences: $token")
                                authToken = token
                                currentUserId = token
                                otherUserId = "test_user_123"
                                println("👤 currentUserId = $currentUserId, otherUserId = $otherUserId")
                            }
                        } else {
                            println("❌ Пользователь не авторизован")
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn) {
                        println("👀 Показываем LoginScreen")
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { token ->
                                println("✅ Успешный вход, получен токен: ${token.substring(0, 10)}...")
                                authToken = token
                                isLoggedIn = true
                                currentUserId = token
                                otherUserId = "test_user_123"
                                println("👤 Установлены ID: current=$currentUserId, other=$otherUserId")
                            }
                        )
                    } else {
                        println("👀👀👀 Показываем ChatScreen")
                        println("📦 chatId = 1f57594a-eea1-4a7e-8ff7-258ac90366a8")
                        println("🔑 firebaseToken = ${authToken?.substring(0, 10)}...")
                        println("👤 otherUserId = $otherUserId")

                        ChatScreen(
                            chatId = "1f57594a-eea1-4a7e-8ff7-258ac90366a8",
                            firebaseToken = authToken ?: "",
                            otherUserId = otherUserId ?: "test_user_123",
                            onLogout = {
                                println("🚪 Выход из аккаунта")
                                loginViewModel.logout()
                                isLoggedIn = false
                            }
                        )
                    }
                }
            }
        }
    }
}

class LoginViewModelFactory(
    private val userPreferences: UserPreferences
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
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