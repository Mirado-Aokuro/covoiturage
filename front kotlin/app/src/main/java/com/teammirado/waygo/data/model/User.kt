package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String? = null, // Optionnel car absent de certaines réponses login
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("trips_count") val tripsCount: Int = 0,
    @SerializedName("rating") val rating: Double = 0.0,
    val preferences: String? = null,
    @SerializedName("vehicle_model") val vehicleModel: String? = null,
    @SerializedName("vehicle_plate") val vehiclePlate: String? = null,
    @SerializedName("is_verified") val isVerified: Boolean = false
)
