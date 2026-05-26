package com.teammirado.waygo.data

import com.teammirado.waygo.data.model.Trip

object MockData {
    val trips = listOf(
        Trip(1, "Paris", "Lyon", 25.0, 3, "Jean Dupont", "2023-12-01", "08:00"),
        Trip(2, "Marseille", "Nice", 15.0, 2, "Marie Curie", "2023-12-01", "10:30"),
        Trip(3, "Bordeaux", "Toulouse", 20.0, 4, "Pierre Martin", "2023-12-02", "14:00"),
        Trip(4, "Lille", "Paris", 18.0, 1, "Sophie Bernard", "2023-12-02", "17:45")
    )
}
