package com.example.martfon.domain.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.martfon.domain.presentation.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var phoneNumber by remember { mutableStateOf("+7 999 123-45-67") }
    var verificationCode by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val verificationId by viewModel.verificationId.collectAsState()

    val cleanPhoneNumber = phoneNumber.replace(Regex("[^0-9+]"), "")

    // Следим за успешным входом
    LaunchedEffect(viewModel.loginSuccess) {
        viewModel.loginSuccess.collect { isSuccess ->
            if (isSuccess) {
                val token = viewModel.getAuthToken()
                if (token != null) {
                    println("✅ Передаю токен в MainActivity: $token")
                    onLoginSuccess(token)
                } else {
                    println("❌ Токен не найден!")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Вход в чат",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Номер телефона") },
            placeholder = { Text("+7 999 123-45-67") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && verificationId == null
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (verificationId != null) {
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Код из Telegram") },
                placeholder = { Text("6 цифр") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Text(
                text = "Код отправлен в Telegram!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (error != null) {
            Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isButtonEnabled = !isLoading && (
                (verificationId == null && cleanPhoneNumber.length >= 10) ||
                        (verificationId != null && verificationCode.length == 6)
                )

        Button(
            onClick = {
                if (verificationId == null) {
                    viewModel.requestCode(cleanPhoneNumber)
                } else {
                    viewModel.verifyCode(verificationCode)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isButtonEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        ) {
            Text(if (verificationId == null) "Отправить код" else "Войти")
        }
    }
}