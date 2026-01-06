package com.example.martfon.data.network.api

import com.example.martfon.data.network.dto.MessageDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MessageApi {
    @GET("messages")
    suspend fun getMessages(): Response<List<MessageDto>> // Возвращаем Response

    @POST("messages")
    suspend fun sendMessage(@Body message: MessageDto): Response<MessageDto> // Возвращаем Response
}