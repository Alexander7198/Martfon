package com.example.martfon.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martfon.data.network.api.MessageApi
import com.example.martfon.data.network.dto.StatusRequest
import com.example.martfon.data.network.dto.UserDto
import com.example.martfon.data.network.retrofit.RetrofitInstance  // ✅ Добавьте импорт!
import com.example.martfon.data.repository.MessageRepository
import com.example.martfon.domain.model.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel : ViewModel() {
    private val messageRepository = MessageRepository()

    // ✅ ИНИЦИАЛИЗИРУЕМ apiService!
    private val apiService = RetrofitInstance.messageApi

    private var currentChatId: String = "1f57594a-eea1-4a7e-8ff7-258ac90366a8"
    private var authToken: String? = null

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _newMessageText = MutableStateFlow("")
    val newMessageText: StateFlow<String> = _newMessageText.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ===== СТАТУС =====
    private var statusUpdateJob: Job? = null

    fun startStatusUpdates() {
        println("🚀🚀🚀 startStatusUpdates() ВЫЗВАН")
        statusUpdateJob = viewModelScope.launch {
            while (true) {
                println("🔄🔄🔄 Запускаем updateStatus")
                updateStatus("online")
                delay(30000)
            }
        }
    }

    fun stopStatusUpdates() {
        statusUpdateJob?.cancel()
        viewModelScope.launch {
            updateStatus("offline")
        }
    }

    private suspend fun updateStatus(status: String) {
        if (authToken == null) {
            println("❌ authToken == null в updateStatus")
            return
        }

        try {
            println("🟡 Пытаюсь обновить статус на: $status")
            val response = apiService.updateStatus("Bearer $authToken", StatusRequest(status))
            if (response.isSuccessful) {
                println("✅ Статус обновлен: $status")
            } else {
                println("❌ Ошибка ответа: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            println("❌ Исключение: ${e.message}")
        }
    }

    suspend fun getOtherUserStatus(userId: String): UserDto? {
        return try {
            println("📡 Запрашиваю статус пользователя: $userId")
            val response = apiService.getUserStatus(userId)
            println("📡 Код ответа: ${response.code()}")

            if (response.isSuccessful) {
                val user = response.body()
                println("✅ Получен статус: ${user?.status}")
                user
            } else {
                println("❌ Ошибка получения статуса: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            println("❌ Исключение при запросе статуса: ${e.message}")
            null
        }
    }

    // ===== ОСТАЛЬНОЙ КОД =====
    fun setAuthToken(token: String) {
        authToken = token
        messageRepository.setToken(token)
    }

    fun setChatId(chatId: String) {
        currentChatId = chatId
        loadMessages()
    }

    fun loadMessages() {
        if (authToken == null) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = messageRepository.getMessages(currentChatId)
                result.onSuccess { messageResponses ->
                    _messages.value = messageResponses.map { it.toDomain() }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Ошибка загрузки"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMessageText(text: String) {
        _newMessageText.value = text
    }

    fun sendMessage() {
        val text = _newMessageText.value.trim()
        if (text.isEmpty() || authToken == null) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val tempMessage = Message(
                id = UUID.randomUUID().toString(),
                text = text,
                senderId = "me",
                timestamp = System.currentTimeMillis(),
                isSentByMe = true
            )
            _messages.value = listOf(tempMessage) + _messages.value
            _newMessageText.value = ""

            try {
                val result = messageRepository.sendMessage(currentChatId, text)
                result.onSuccess {
                    loadMessages()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Ошибка отправки"
                    loadMessages()
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка отправки"
                loadMessages()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshMessages() {
        loadMessages()
    }

    private fun MessageRepository.MessageResponse.toDomain(): Message {
        return Message(
            id = this.id ?: UUID.randomUUID().toString(),
            text = this.content ?: "",
            senderId = this.sender_firebase_uid ?: "unknown",
            timestamp = parseTimestamp(this.created_at),
            isSentByMe = this.sender_firebase_uid == "test_user_123"
        )
    }

    private fun parseTimestamp(dateStr: String?): Long {
        return try {
            if (dateStr != null) {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                format.parse(dateStr)?.time ?: System.currentTimeMillis()
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}