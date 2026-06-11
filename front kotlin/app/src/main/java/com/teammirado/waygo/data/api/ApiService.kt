package com.teammirado.waygo.data.api

import com.teammirado.waygo.data.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login")
    suspend fun login(@Body credentials: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body data: RegisterRequest): RegisterResponse

    @GET("user")
    suspend fun getCurrentUser(): User

    @PUT("user")
    suspend fun updateProfile(@Body profileData: UpdateProfileRequest): User

    @Multipart
    @POST("user/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): User

    @PUT("user/fcm-token")
    suspend fun updateFcmToken(@Body tokenData: FcmTokenRequest)

    @POST("payments/create-intent")
    suspend fun createPaymentIntent(@Body data: Map<String, Int>): StripePaymentResponse

    @GET("trips")
    suspend fun getTrips(
        @Query("departure_city") departure: String? = null,
        @Query("arrival_city") arrival: String? = null,
        @Query("date") date: String? = null,
        @Query("driver_name") driverName: String? = null
    ): List<Trip>

    @GET("trips/{id}")
    suspend fun getTripDetails(@Path("id") id: Int): Trip

    @POST("trips")
    suspend fun publishTrip(@Body tripData: PublishTripRequest): Trip

    @PUT("trips/{id}")
    suspend fun updateTrip(@Path("id") id: Int, @Body tripData: PublishTripRequest): Trip

    @DELETE("trips/{id}")
    suspend fun deleteTrip(@Path("id") id: Int)

    // --- RÉSERVATIONS ---
    @POST("bookings")
    suspend fun createBooking(@Body bookingData: CreateBookingRequest): Booking

    @GET("my-bookings")
    suspend fun getMyBookings(): List<Trip>

    @GET("my-published-trips")
    suspend fun getMyPublishedTrips(): List<Trip>

    @GET("trips/{id}/bookings")
    suspend fun getTripBookings(@Path("id") tripId: Int): List<Booking>

    @POST("bookings/{id}/confirm")
    suspend fun confirmBooking(@Path("id") bookingId: Int): Booking

    @POST("bookings/{id}/reject")
    suspend fun rejectBooking(@Path("id") bookingId: Int): Booking

    // ✅ Correction : Plus robuste pour la démo
    @DELETE("bookings/{id}")
    suspend fun cancelBooking(@Path("id") bookingId: Int): Response<ResponseBody>

    // --- MESSAGERIE & AVIS ---
    @POST("reviews")
    suspend fun leaveReview(@Body reviewData: ReviewRequest): Response<ResponseBody>
}
