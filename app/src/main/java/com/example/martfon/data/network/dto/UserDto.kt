package com.example.martfon.data.network.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("firebase_uid")
    val firebaseUid: String? = null,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("display_name")
    val displayName: String? = null,

    @SerializedName("avatar_url")
    val avatarUrl: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("last_seen")
    val lastSeen: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)