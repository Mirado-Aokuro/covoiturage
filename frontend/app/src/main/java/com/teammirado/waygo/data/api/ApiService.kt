package com.teammirado.waygo.data.api

import com.teammirado.waygo.data.model.*
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body credentials: Map<String, String>): Map<String, String>

    @POST("register")
    suspend fun register(@Body data: Map<String, String>): User

    @GET("user")
    suspend fun getCurrentUser(): User

    @GET("trips")
    suspend fun getTrips(
        @Query("departure_city") departure: String? = null,
        @Query("arrival_city") arrival: String? = null,
        @Query("date") date: String? = null
    ): List<Trip>

    @GET("trips/{id}")
    suspend fun getTripDetails(@Path("id") id: Int): Trip

    @POST("bookings")
    suspend fun createBooking(@Body bookingData: Map<String, Int>): Booking

    @GET("my-bookings")
    suspend fun getMyBookings(): List<Booking>

    @GET("trips/{id}/reviews")
    suspend fun getTripReviews(@Path("id") id: Int): List<Review>
    
    @POST("messages")
    suspend fun sendMessage(@Body message: Map<String, String>): Message
}
