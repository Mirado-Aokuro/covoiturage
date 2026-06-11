<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Sanctum\HasApiTokens;

class User extends Authenticatable {
    use HasApiTokens, HasFactory;

    /**
     * Les attributs qui peuvent être assignés en masse.
     *
     * @var array<int, string>
     */
    protected $fillable = [
        'name', 
        'email', 
        'password', 
        'role',
        'avatar_url', // Autorise l'upload de photo de profil
        'fcm_token'   // Autorise l'enregistrement du jeton Firebase pour les pushs (Semaine 3)
    ];

    /**
     * Les attributs qui doivent être masqués pour les réponses JSON.
     *
     * @var array<int, string>
     */
    protected $hidden = [
        'password', 
        'remember_token',
        'created_at',
        'updated_at'
    ];

    /**
     * Casting automatique pour s'assurer des bons types numériques envoyés à Android
     */
    protected $casts = [
        'email_verified_at' => 'datetime',
        'password' => 'hashed',
        'rating' => 'float',
        'trips_count' => 'integer'
    ];

    /**
     * Un utilisateur peut proposer plusieurs trajets en tant que conducteur.
     */
    public function trips() {
        return $this->hasMany(Trip::class, 'driver_id');
    }

    /**
     * Un utilisateur peut effectuer plusieurs réservations en tant que passager.
     */
    public function bookings() {
        return $this->hasMany(Booking::class, 'passenger_id');
    }
}