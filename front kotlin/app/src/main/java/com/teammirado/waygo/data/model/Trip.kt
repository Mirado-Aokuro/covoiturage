package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class Trip(
    val id: Int,
    @SerializedName("booking_id") val bookingId: Int? = null,
    @SerializedName("driver_id") val driverId: Int? = null,
    @SerializedName("driver_name") val driverName: String? = "Conducteur",
    @SerializedName("driver_image_res") val driverImageRes: Int? = null,
    @SerializedName("driver_rating") val driverRating: Double? = 0.0,
    @SerializedName("driver_reviews_count") val driverReviewsCount: Int? = 0,
    
    // ✅ Ultra-souple : accepte les deux noms de colonnes
    @SerializedName("departure_city", alternate = ["departure"]) 
    val departureCity: String? = "Ville inconnue",
    
    @SerializedName("arrival_city", alternate = ["arrival"]) 
    val arrivalCity: String? = "Ville inconnue",

    @SerializedName("departure_time") val departureTime: String? = null,
    @SerializedName("arrival_time") val arrivalTime: String? = null,
    val price: Double = 0.0,
    @SerializedName("seats_available") val seatsAvailable: Int? = 1,
    val date: String? = null,
    val amenities: List<String> = emptyList(),
    @SerializedName("departure_latitude") val departureLatitude: Double? = null,
    @SerializedName("departure_longitude") val departureLongitude: Double? = null,
    @SerializedName("arrival_latitude") val arrivalLatitude: Double? = null,
    @SerializedName("arrival_longitude") val arrivalLongitude: Double? = null,
    val description: String? = null
)
