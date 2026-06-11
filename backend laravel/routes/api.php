<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\TripController;
use App\Http\Controllers\BookingController;
use App\Http\Controllers\ReviewController;

// --- ROUTES PUBLIQUES (AUTHENTIFICATION) ---
Route::post('/login', [AuthController::class, 'login']);
Route::post('/register', [AuthController::class, 'register']);

// --- ROUTES SÉCURISÉES (SANCTUM) ---
Route::middleware('auth:sanctum')->group(function () {
    
    // 👤 1. PROFIL UTILISATEUR
    Route::get('/user', [AuthController::class, 'user']);                 // @GET("user")
    Route::put('/user', [AuthController::class, 'updateProfile']);        // @PUT("user")
    Route::post('/user/avatar', [AuthController::class, 'updateAvatar']); // @Multipart @POST("user/avatar")

    // 🎒 2. TRAJETS & RECHERCHE
    Route::get('/trips', [TripController::class, 'index']);               // @GET("trips") -> Avec filtres de recherche
    Route::post('/trips', [TripController::class, 'store']);              // @POST("trips") -> Publier un trajet
    Route::get('/trips/{id}', [TripController::class, 'show']);           // @GET("trips/{id}") -> Détails du trajet
    Route::put('/trips/{id}', [TripController::class, 'update']);         // @PUT("trips/{id}") -> Modifier un trajet
    Route::delete('/trips/{id}', [TripController::class, 'destroy']);     // @DELETE("trips/{id}") -> Annuler un trajet (Conducteur)
    
    // Historique spécifique selon le rôle
    Route::get('/my-published-trips', [TripController::class, 'myPublished']); // @GET("my-published-trips") -> Trajets créés par le chauffeur

    // 🚗 3. RÉSERVATIONS & ACTIONS DE VALIDATION
    Route::post('/bookings', [BookingController::class, 'store']);        // @POST("bookings") -> Réserver des places (Passager)
    Route::get('/my-bookings', [BookingController::class, 'index']);       // @GET("my-bookings") -> Trajets réservés par le passager
    Route::delete('/bookings/{id}', [BookingController::class, 'destroy']); // @DELETE("bookings/{id}") -> Annuler réservation (Passager/Chauffeur)

    // Gestion des demandes de réservations pour les conducteurs
    Route::get('/trips/{id}/bookings', [BookingController::class, 'tripBookings']); // @GET("trips/{id}/bookings") -> Liste des demandes reçues
    Route::post('/bookings/{id}/confirm', [BookingController::class, 'confirm']);   // @POST("bookings/{id}/confirm") -> Accepter un passager
    Route::post('/bookings/{id}/reject', [BookingController::class, 'reject']);     // @POST("bookings/{id}/reject") -> Refuser un passager

    // ⭐️ 4. AVIS BILATÉRAUX (SEMAINE 3)
    Route::get('/users/{userId}/reviews', [ReviewController::class, 'index']);    // @GET -> Voir les avis d'un profil
    Route::post('/reviews', [ReviewController::class, 'store']);                  // @POST -> Déposer un avis conducteur/passager

    // 📡 5. GESTION DU GPS EN TEMPS RÉEL (SEMAINE 3)
    Route::post('/trips/{id}/location', [TripController::class, 'updateLocation']); // @POST -> Conducteur envoie sa position
    Route::get('/trips/{id}/location', [TripController::class, 'getLocation']);    // @GET -> Passager récupère la position
});