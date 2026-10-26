package com.example.martfon.domain.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martfon.data.local.UserPreferences
import com.example.martfon.data.repository.TelegramAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val authRepository = TelegramAuthRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId: StateFlow<String?> = _verificationId.asStateFlow()

    private var currentPhoneNumber: String = ""
    private var _authToken: String? = null

    init {
        // Проверяем, есть ли сохраненная сессия
        checkSavedSession()
    }

    private fun checkSavedSession() {
        viewModelScope.launch {
            userPreferences.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    // Если есть сохраненная сессия, сразу переходим в чат
                    _loginSuccess.value = true
                }
            }
        }
    }

    fun requestCode(phoneNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            currentPhoneNumber = phoneNumber

            try {
                val result = authRepository.requestCode(phoneNumber)
                result.onSuccess { verificationId ->
                    _verificationId.value = verificationId
                    println("✅ Код отправлен, verificationId: $verificationId")
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Ошибка отправки кода"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка отправки кода"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun verifyCode(code: String) {
        val verificationId = _verificationId.value
        if (verificationId == null) {
            _error.value = "Сначала запросите код"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = authRepository.verifyCode(
                    phoneNumber = currentPhoneNumber,
                    code = code,
                    verificationId = verificationId
                )

                result.onSuccess { token ->
                    _authToken = token
                    // ✅ Сохраняем данные в DataStore
                    userPreferences.saveAuthData(token, currentPhoneNumber)
                    _loginSuccess.value = true
                    println("✅ Вход выполнен, данные сохранены")
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Ошибка входа"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка входа"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAuthData()
            _authToken = null
            _loginSuccess.value = false
            _verificationId.value = null
            currentPhoneNumber = ""
        }
    }

    fun getAuthToken(): String? = _authToken
}