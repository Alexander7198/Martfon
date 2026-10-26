package com.example.martfon.data.network.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("token")
    val token: String
)