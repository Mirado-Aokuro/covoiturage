package com.teammirado.waygo.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    /**
     * Configuration pour 'adb reverse tcp:8000 tcp:8000'
     */
    private const val BASE_URL = "http://127.0.0.1:8000/api/"

    // Stockage dynamique du token
    var authToken: String? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            
            // On injecte le token s'il existe
            authToken?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
            
            requestBuilder.addHeader("Accept", "application/json")
            requestBuilder.addHeader("Content-Type", "application/json")
            
            chain.proceed(requestBuilder.build())
        }
        // Timeouts plus robustes pour le développement local
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Supprime le token (pour la déconnexion)
     */
    fun clearAuth() {
        authToken = null
    }
}
