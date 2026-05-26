package com.teammirado.waygo.data.model

data class Trip(
    val id: Int,
    val departure: String,
    val arrival: String,
    val departure_latitude: Double? = null,
    val departure_longitude: Double? = null,
    val arrival_latitude: Double? = null,
    val arrival_longitude: Double? = null,
    val price: Double,
    val seatsAvailable: Int,
    val driverName: String,
    val date: String,
    val time: String,
    val description: String? = null
)
