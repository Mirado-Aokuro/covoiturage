package com.teammirado.waygo.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String, // "driver", "passenger", "admin"
    val avatarUrl: String? = null
)
