<?php

use App\Http\Controllers\ApiController;
use Illuminate\Support\Facades\Route;

// Route publique pour l'affichage des trajets avec filtres
Route::get('/trips', [ApiController::class, 'indexTrips']);

// Route de connexion
Route::post('/login', [ApiController::class, 'login']);

// Exemple de routes protégées par rôles
Route::middleware(['auth:sanctum'])->group(function () {
    Route::middleware(['role:driver'])->group(function () {
        // Routes réservées aux conducteurs ici (ex: créer un trajet)
    });
});