package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

data class PublishTripRequest(
    @SerializedName("departure_city") val departureCity: String,
    @SerializedName("arrival_city") val arrivalCity: String,
    @SerializedName("departure_latitude") val departureLatitude: Double,
    @SerializedName("departure_longitude") val departureLongitude: Double,
    @SerializedName("arrival_latitude") val arrivalLatitude: Double,
    @SerializedName("arrival_longitude") val arrivalLongitude: Double,
    val date: String,
    @SerializedName("departure_time") val departureTime: String,
    val price: Double,
    @SerializedName("seats_available") val seatsAvailable: Int
)

data class CreateBookingRequest(
    @SerializedName("trip_id") val tripId: Int,
    val seats: Int
)

data class UpdateProfileRequest(
    val name: String? = null,
    @SerializedName("vehicle_model") val vehicleModel: String? = null,
    @SerializedName("vehicle_plate") val vehiclePlate: String? = null
)

data class ReviewRequest(
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("rated_user_id", alternate = ["driver_id"]) val ratedUserId: Int? = null
)
