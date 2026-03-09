package com.example.martfon.data.repository

import com.google.firebase.FirebaseException  // ✅ Добавлен импорт
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    // Включаем тестовый режим
    init {
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }

    suspend fun sendVerificationCode(phoneNumber: String): Result<String> =
        suspendCancellableCoroutine { continuation ->
            // Проверяем, что номер не пустой
            if (phoneNumber.isBlank()) {
                continuation.resumeWithException(Exception("Phone number cannot be empty"))
                return@suspendCancellableCoroutine
            }

            // Для тестового номера устанавливаем авто-код
            auth.firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(
                phoneNumber,
                "123456"
            )

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        // Авто-верификация для тестов
                        continuation.resume(Result.success("auto_verified"))
                    }

                    override fun onVerificationFailed(e: FirebaseException) {  // ✅ Теперь тип правильный
                        continuation.resumeWithException(e)
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        continuation.resume(Result.success(verificationId))
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }

    suspend fun verifyCode(verificationId: String, code: String): Result<Boolean> =
        suspendCancellableCoroutine { continuation ->
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(true))
                    } else {
                        continuation.resumeWithException(task.exception ?: Exception("Ошибка входа"))
                    }
                }
        }
}