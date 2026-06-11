<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Booking extends Model {
    use HasFactory;

    // 🛠️ CORRECTION : Autorise 'seats' et 'seats_booked' pour s'aligner parfaitement avec le contrôleur
    protected $fillable = [
        'trip_id',
        'passenger_id',
        'seats',        // Utilisé dans le contrôleur Laravel
        'seats_booked', // Alternative historique
        'status',       // 'pending', 'confirmed', 'rejected', 'cancelled'
    ];

    /**
     * Une réservation appartient à un trajet spécifique.
     */
    public function trip() {
        return $this->belongsTo(Trip::class);
    }

    /**
     * Une réservation appartient à un passager (un utilisateur).
     * Configuration validée : fait la passerelle parfaite avec le contrôleur !
     */
    public function passenger() {
        return $this->belongsTo(User::class, 'passenger_id');
    }
}