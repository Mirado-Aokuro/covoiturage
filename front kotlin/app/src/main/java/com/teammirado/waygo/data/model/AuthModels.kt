package com.teammirado.waygo.data.model

import com.google.gson.annotations.SerializedName

/**
 * --- REQUÊTES ---
 */
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String, val role: String)
data class FcmTokenRequest(@SerializedName("fcm_token") val fcmToken: String)

/**
 * --- STRIPE ---
 */
data class StripePaymentResponse(
    @SerializedName("client_secret", alternate = ["paymentIntent"]) val paymentIntentClientSecret: String? = null,
    @SerializedName("customer") val customerId: String? = null,
    @SerializedName("ephemeral_key", alternate = ["ephemeralKey"]) val ephemeralKeySecret: String? = null,
    @SerializedName("publishable_key", alternate = ["publishableKey"]) val publishableKey: String? = null
)

/**
 * --- RÉPONSES ---
 */
data class UserResponse(val id: Int, val name: String, val email: String, val role: String? = null)
data class LoginResponse(val token: String, val user: UserResponse)
data class RegisterResponse(val token: String, val user: UserResponse)
