package com.example.martfon.data.network.dto

data class MessageDto(
    val id: String? = null,
    val text: String,
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val chatId: String = "default_chat"
) {
    constructor() : this(null, "", "", 0, "")
}