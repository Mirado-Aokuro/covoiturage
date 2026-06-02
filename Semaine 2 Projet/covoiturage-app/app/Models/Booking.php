<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Booking extends Model {
    use HasFactory;

    // Les champs que l'on autorise à être remplis via l'API Android
    protected $fillable = [
        'trip_id',
        'passenger_id',
        'seats_booked',
        'status', // 'pending', 'accepted', 'rejected', 'cancelled'
    ];

    /**
     * Une réservation appartient à un trajet spécifique.
     */
    public function trip() {
        return $this->belongsTo(Trip::class);
    }

    /**
     * Une réservation appartient à un passager (un utilisateur).
     */
    public function passenger() {
        return $this->belongsTo(User::class, 'passenger_id');
    }
}