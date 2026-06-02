package com.teammirado.waygo.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 💡 Rappel : Si ton Laravel tourne en local (localhost) sur ta machine Linux,
    // l'émulateur Android n'arrive pas à lire '127.0.0.1'. Remplace par "http://10.0.2.2:8000/api/"
    // VIA USB on utilise '127.0.0.1'
    private const val BASE_URL = "http://127.0.0.1:8000/api/"

    // Variable modifiable pour stocker le Token de session Sanctum après le Login
    var authToken: String? = null

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            
            // Si un token est disponible dans l'application, on l'injecte dynamiquement
            authToken?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            
            requestBuilder.addHeader("Accept", "application/json")
            
            chain.proceed(requestBuilder.build())
        }
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
     * Nettoie le token lors de la déconnexion
     */
    fun clearToken() {
        authToken = null
    }
}
