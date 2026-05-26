package com.teammirado.waygo.data.model

data class Booking(
    val id: Int,
    val tripId: Int,
    val userId: Int,
    val status: String // e.g., "pending", "confirmed", "cancelled"
)
