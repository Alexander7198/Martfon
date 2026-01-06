package com.example.martfon.domain.model

data class Message(
    val id: String,
    val text: String,
    val senderId: String,
    val timestamp: Long,
    val isSentByMe: Boolean
)