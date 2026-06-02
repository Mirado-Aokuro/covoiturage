package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

/**
 * --- REQUÊTES (Android -> Laravel) ---
 */

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String
)

/**
 * --- RÉPONSES (Laravel -> Android) ---
 */

data class LoginResponse(
    val token: String,
    val user: User
)

data class RegisterResponse(
    val token: String,
    val user: User
)
