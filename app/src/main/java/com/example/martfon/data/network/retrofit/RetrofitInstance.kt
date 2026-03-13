package com.example.martfon.data.network.retrofit

import com.example.martfon.data.network.api.MessageApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // ⚠️ ВАЖНО: 127.0.0.1 работает ТОЛЬКО в эмуляторе!
    // Для реального устройства используйте IP компьютера в сети: http://192.168.1.106:8080/
    private const val BASE_URL = "http://192.168.1.106:8080/"  // Для эмулятора Android
    // private const val BASE_URL = "http://192.168.1.106:8080/"  // Для реального устройства

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            // Добавляем логирование запросов
            val request = chain.request()
            println("📤 Request: ${request.method} ${request.url}")
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val messageApi: MessageApi by lazy {
        retrofit.create(MessageApi::class.java)
    }
}