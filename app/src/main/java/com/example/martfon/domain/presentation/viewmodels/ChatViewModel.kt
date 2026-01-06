package com.example.martfon.domain.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martfon.data.repository.MessageRepository
import com.example.martfon.domain.model.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = MessageRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _newMessageText = MutableStateFlow("")
    val newMessageText: StateFlow<String> = _newMessageText.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _messages.value = repository.getMessages() // Исправлено здесь
        }
    }

    fun updateMessageText(newText: String) {
        _newMessageText.value = newText
    }

    fun sendMessage() {
        val textToSend = _newMessageText.value.trim()
        if (textToSend.isNotEmpty()) {
            viewModelScope.launch {
                val success = repository.sendMessage(textToSend)
                if (success) {
                    _newMessageText.value = ""
                    loadMessages()
                }
            }
        }
    }
}