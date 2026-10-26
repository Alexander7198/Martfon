package com.example.martfon.data.network.dto

import com.google.gson.annotations.SerializedName

data class ChatDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String?,

    @SerializedName("type")
    val type: String,

    @SerializedName("created_by")
    val createdBy: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("avatar_url")
    val avatarUrl: String? = null,

    @SerializedName("description")
    val description: String? = null
)