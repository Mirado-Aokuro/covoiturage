package com.teammirado.waygo.data

import com.teammirado.waygo.data.model.Trip

object MockData {
    val trips = listOf(
        Trip(
            id = 1,
            driverName = "Marie L.",
            driverRating = 4.9,
            driverReviewsCount = 124,
            departureCity = "Paris, Gare de Lyon",
            arrivalCity = "Orléans, Centre-ville",
            departureTime = "08:30",
            arrivalTime = "10:15",
            price = 12.50,
            seatsAvailable = 3,
            date = "2023-12-01",
            amenities = listOf("Climatisé", "Non-fumeur")
        ),
        Trip(
            id = 2,
            driverName = "Thomas B.",
            driverRating = 4.7,
            driverReviewsCount = 89,
            departureCity = "Versailles, Château",
            arrivalCity = "Chartres, Cathédrale",
            departureTime = "09:00",
            arrivalTime = "10:30",
            price = 8.00,
            seatsAvailable = 2,
            date = "2023-12-01",
            amenities = listOf("Climatisé")
        )
    )
}
