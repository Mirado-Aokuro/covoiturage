package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class Booking(
    val id: Int,
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("user_id") val userId: Int,
    val status: String // e.g., "pending", "confirmed", "cancelled"
)
