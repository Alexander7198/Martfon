package com.example.martfon.data.repository

import com.example.martfon.data.network.retrofit.RetrofitInstance
import com.example.martfon.data.network.dto.MessageDto
import com.example.martfon.domain.model.Message
import java.util.UUID

class MessageRepository {
    private val messageApi = RetrofitInstance.messageApi

    suspend fun getMessages(): List<Message> {
        return try {
            val response = messageApi.getMessages()
            if (response.isSuccessful) {
                response.body()?.map { it.toDomain() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun sendMessage(text: String, senderId: String = "my_user_id"): Boolean {
        return try {
            val messageDto = MessageDto(
                id = UUID.randomUUID().toString(),
                text = text,
                senderId = senderId,
                timestamp = System.currentTimeMillis()
            )
            val response = messageApi.sendMessage(messageDto)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

private fun MessageDto.toDomain(): Message {
    return Message(
        id = this.id ?: UUID.randomUUID().toString(),
        text = this.text,
        senderId = this.senderId,
        timestamp = this.timestamp,
        isSentByMe = this.senderId == "my_user_id"
    )
}