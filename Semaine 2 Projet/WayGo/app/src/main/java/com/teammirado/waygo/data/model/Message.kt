package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class Message(
    val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    val content: String,
    val timestamp: String
)
