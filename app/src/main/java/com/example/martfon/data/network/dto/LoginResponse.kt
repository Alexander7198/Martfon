package com.example.martfon.data.network.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("firebase_uid")  //
    val firebaseUid: String,  //

    @SerializedName("user")
    val user: UserDto
)