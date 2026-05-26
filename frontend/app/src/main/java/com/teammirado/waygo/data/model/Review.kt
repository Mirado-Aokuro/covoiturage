package com.teammirado.waygo.data.model

data class Review(
    val id: Int,
    val tripId: Int,
    val userId: Int,
    val rating: Int,
    val comment: String,
    val userName: String? = null
)
