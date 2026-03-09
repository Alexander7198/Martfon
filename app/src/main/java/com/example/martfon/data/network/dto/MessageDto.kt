package com.example.martfon.data.network.dto

data class MessageDto(
    val id: String? = null,
    val chat_id: String,
    val sender_firebase_uid: String? = null,
    val type: String = "text",
    val content: String? = null,
    val media_url: String? = null,
    val file_name: String? = null,
    val file_size: Int? = null,
    val created_at: String? = null,
    val is_edited: Boolean = false,
    val is_deleted: Boolean = false
)