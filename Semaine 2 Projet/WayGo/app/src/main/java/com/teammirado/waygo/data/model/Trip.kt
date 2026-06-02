package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class Trip(
    val id: Int,
    @SerializedName("driver_name") val driverName: String,
    @SerializedName("driver_image_res") val driverImageRes: Int? = null,
    @SerializedName("driver_rating") val driverRating: Double,
    @SerializedName("driver_reviews_count") val driverReviewsCount: Int,
    val departure: String,
    val arrival: String,
    @SerializedName("departure_time") val departureTime: String,
    @SerializedName("arrival_time") val arrivalTime: String,
    val price: Double,
    @SerializedName("seats_available") val seatsAvailable: Int,
    val date: String,
    val amenities: List<String> = emptyList(),
    @SerializedName("departure_latitude") val departureLatitude: Double? = null,
    @SerializedName("departure_longitude") val departureLongitude: Double? = null,
    @SerializedName("arrival_latitude") val arrivalLatitude: Double? = null,
    @SerializedName("arrival_longitude") val arrivalLongitude: Double? = null,
    val description: String? = null
)
