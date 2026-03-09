package com.example.martfon.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martfon.data.repository.MessageRepository
import com.example.martfon.domain.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel : ViewModel() {
    private val messageRepository = MessageRepository()

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

            // Оптимистичное обновление
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
                    // Сообщение успешно отправлено, обновляем список
                    loadMessages()
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Ошибка отправки"
                    // Откатываем оптимистичное обновление при ошибке
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
            isSentByMe = this.sender_firebase_uid == "test_user_123" // Сравните с вашим UID
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