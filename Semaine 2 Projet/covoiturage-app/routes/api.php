<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\TripController;
use App\Http\Controllers\BookingController;

// --- ROUTES PUBLIQUES ---
Route::post('/login', [AuthController::class, 'login']);
Route::post('/register', [AuthController::class, 'register']);

// --- ROUTES SÉCURISÉES (SANCTUM) ---
Route::middleware('auth:sanctum')->group(function () {
    
    // Utilisateur connecté
    Route::get('/user', [AuthController::class, 'user']);
    
    // Gestion des Voyages (Trips)
    Route::get('/trips', [TripController::class, 'index']);
    Route::post('/trips', [TripController::class, 'store']);
    Route::get('/trips/{id}', [TripController::class, 'show']);
    Route::get('/my-published-trips', [TripController::class, 'myPublished']);

    // Gestion des Réservations (Bookings)
    Route::post('/bookings', [BookingController::class, 'store']);
    Route::get('/my-bookings', [BookingController::class, 'index']);
});