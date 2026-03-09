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
                // Создаем ViewModel с userPreferences
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(userPreferences)
                )

                var isLoggedIn by remember { mutableStateOf(false) }
                var authToken by remember { mutableStateOf<String?>(null) }

                // Проверяем состояние авторизации
                LaunchedEffect(Unit) {
                    userPreferences.isLoggedIn.collect { loggedIn ->
                        isLoggedIn = loggedIn
                        if (loggedIn) {
                            userPreferences.authToken.collect { token ->
                                authToken = token
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn) {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { token ->
                                authToken = token
                                isLoggedIn = true
                            }
                        )
                    } else {
                        ChatScreen(
                            chatId = "1f57594a-eea1-4a7e-8ff7-258ac90366a8",
                            firebaseToken = authToken ?: "",
                            onLogout = {
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

// Фабрика для создания ViewModel с параметрами
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
                onLogout = {}
            )
        }
    }
}