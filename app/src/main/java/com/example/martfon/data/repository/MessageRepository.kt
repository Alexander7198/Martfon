package com.example.martfon.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MessageRepository {
    private val client = OkHttpClient()
    private val gson = Gson()

    // URL вашего Python сервера (чат-сервера, не Telegram бота!)
    private val baseUrl = "http://192.168.1.106:8080"  // IP вашего сервера

    private var authToken: String? = null

    data class MessageRequest(
        val chatId: String,
        val text: String,
        val type: String = "text"
    )

    data class MessageResponse(
        val id: String?,
        val chat_id: String,
        val sender_firebase_uid: String?,
        val content: String?,
        val created_at: String?
    )

    fun setToken(token: String) {
        authToken = token
    }

    suspend fun sendMessage(chatId: String, text: String): Result<MessageResponse> =
        suspendCancellableCoroutine { continuation ->

            if (authToken == null) {
                continuation.resumeWithException(Exception("No auth token"))
                return@suspendCancellableCoroutine
            }

            val requestBody = MessageRequest(
                chatId = chatId,
                text = text,
                type = "text"
            )

            val jsonBody = gson.toJson(requestBody)
            val body = jsonBody.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/api/messages")
                .post(body)
                .addHeader("Authorization", "Bearer ${authToken}")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        if (response.isSuccessful) {
                            val messageResponse = gson.fromJson(responseBody, MessageResponse::class.java)
                            continuation.resume(Result.success(messageResponse))
                        } else {
                            continuation.resumeWithException(Exception("Error: ${response.code}"))
                        }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    } finally {
                        response.close()
                    }
                }
            })
        }

    suspend fun getMessages(chatId: String): Result<List<MessageResponse>> =
        suspendCancellableCoroutine { continuation ->

            if (authToken == null) {
                continuation.resumeWithException(Exception("No auth token"))
                return@suspendCancellableCoroutine
            }

            val request = Request.Builder()
                .url("$baseUrl/api/messages?chatId=$chatId")
                .get()
                .addHeader("Authorization", "Bearer ${authToken}")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        if (response.isSuccessful) {
                            val messages = gson.fromJson(responseBody, Array<MessageResponse>::class.java).toList()
                            continuation.resume(Result.success(messages))
                        } else {
                            continuation.resumeWithException(Exception("Error: ${response.code}"))
                        }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    } finally {
                        response.close()
                    }
                }
            })
        }
}