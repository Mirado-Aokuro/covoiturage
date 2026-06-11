package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class Booking(
    val id: Int,
    @SerializedName("trip_id") val tripId: Int,
    
    // ✅ Accepte "user_id" ou "passenger_id" (renvoyé par ton nouveau contrôleur)
    @SerializedName("user_id", alternate = ["passenger_id"]) 
    val userId: Int,
    
    // ✅ Accepte tous les formats possibles de ton contrôleur
    @SerializedName("user_name", alternate = ["name", "passenger_name"]) 
    val userName: String? = null,
    
    @SerializedName("user_avatar", alternate = ["avatar", "passenger_avatar"]) 
    val userAvatar: String? = null,
    
    val seats: Int = 1,
    val status: String, // "pending", "confirmed", "rejected", "cancelled"
    
    val user: User? = null
) {
    val displayUserName: String
        get() = userName ?: user?.name ?: "Passager"
}
