package com.example.martfon.data.network.api

import com.example.martfon.data.network.dto.MessageDto
import com.example.martfon.data.network.dto.ChatDto
import com.example.martfon.data.network.dto.UserDto
import com.example.martfon.data.network.dto.LoginRequest
import com.example.martfon.data.network.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.*

interface MessageApi {

    // ===========================================
    // АУТЕНТИФИКАЦИЯ
    // ===========================================
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ===========================================
    // ЧАТЫ
    // ===========================================
    @POST("api/chats")
    suspend fun createChat(
        @Header("Authorization") token: String,
        @Body chat: ChatDto
    ): Response<ChatDto>

    @GET("api/chats")
    suspend fun getChats(
        @Header("Authorization") token: String
    ): Response<List<ChatDto>>

    // ===========================================
    // СООБЩЕНИЯ (обновленные)
    // ===========================================
    @GET("api/messages")
    suspend fun getMessages(
        @Header("Authorization") token: String,
        @Query("chatId") chatId: String
    ): Response<List<MessageDto>>

    @POST("api/messages")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body message: MessageDto
    ): Response<MessageDto>

    // ===========================================
    // ПОЛЬЗОВАТЕЛИ
    // ===========================================
    @GET("api/users")
    suspend fun getUsers(
        @Header("Authorization") token: String
    ): Response<List<UserDto>>
}