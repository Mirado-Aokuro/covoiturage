package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class Review(
    val id: Int,
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("user_id") val userId: Int,
    val rating: Int,
    val comment: String,
    @SerializedName("user_name") val userName: String? = null
)
