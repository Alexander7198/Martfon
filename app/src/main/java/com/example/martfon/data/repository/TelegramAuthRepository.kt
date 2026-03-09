package com.example.martfon.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TelegramAuthRepository {
    private val client = OkHttpClient()
    private val gson = Gson()

    // Для эмулятора
    private val baseUrl = "http://192.168.1.106:5001"  // IP из лога сервера!
    // Для реального устройства в одной сети:
    // private val baseUrl = "http://192.168.1.106:5001"
    // Для продакшена:
    // private val baseUrl = "https://ваш-сервер.ру"

    data class CodeRequest(val phoneNumber: String)
    data class VerifyRequest(
        val phoneNumber: String,
        val code: String,
        val verificationId: String
    )
    data class AuthResponse(
        val success: Boolean,
        val token: String?,
        val phone: String?,
        val error: String?,
        val verificationId: String?
    )

    suspend fun requestCode(phoneNumber: String): Result<String> =
        suspendCancellableCoroutine { continuation ->

            // ✅ Новый способ создания RequestBody
            val jsonBody = gson.toJson(CodeRequest(phoneNumber))
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/api/auth/request-code")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string()
                        val authResponse = gson.fromJson(body, AuthResponse::class.java)

                        if (response.isSuccessful && authResponse.success) {
                            // Возвращаем verificationId для идентификации сессии
                            val verificationId = authResponse.verificationId ?: phoneNumber
                            continuation.resume(Result.success(verificationId))
                        } else {
                            val errorMsg = authResponse.error ?: "Unknown error"
                            continuation.resumeWithException(Exception(errorMsg))
                        }
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    } finally {
                        response.close()
                    }
                }
            })
        }

    suspend fun verifyCode(
        phoneNumber: String,
        code: String,
        verificationId: String
    ): Result<String> = suspendCancellableCoroutine { continuation ->

        // ✅ Новый способ создания RequestBody
        val jsonBody = gson.toJson(VerifyRequest(phoneNumber, code, verificationId))
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/api/auth/verify-code")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = response.body?.string()
                    val authResponse = gson.fromJson(body, AuthResponse::class.java)

                    if (response.isSuccessful && authResponse.success) {
                        authResponse.token?.let {
                            continuation.resume(Result.success(it))
                        } ?: continuation.resumeWithException(Exception("No token received"))
                    } else {
                        val errorMsg = authResponse.error ?: "Unknown error"
                        continuation.resumeWithException(Exception(errorMsg))
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