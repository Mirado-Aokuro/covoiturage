<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Trip extends Model {
    use HasFactory;

    protected $fillable = [
        'driver_id', 'departure_city', 'arrival_city', 'departure_latitude', 'departure_longitude',
        'arrival_latitude', 'arrival_longitude', 'departure_time', 'arrival_time', 'date', 
        'price', 'seats_available', 'amenities'
    ];

    // 🛠️ CORRECTION : On ne cache plus les clés requises par le Controller pour éviter le "null -> null"
    protected $hidden = ['created_at', 'updated_at'];

    // Injection automatique des alias pour la rétrocompatibilité Android
    protected $appends = [
        'driver_name', 'driver_rating', 'driver_reviews_count', 
        'departure', 'arrival'
    ];

    // Casting automatique pour s'assurer des bons types numériques transmis à Retrofit
    protected $casts = [
        'price' => 'float',
        'seats_available' => 'integer',
        'departure_latitude' => 'float',
        'departure_longitude' => 'float',
        'arrival_latitude' => 'float',
        'arrival_longitude' => 'float',
    ];

    /**
     * Un trajet appartient à un chauffeur (User).
     */
    public function driver() {
        return $this->belongsTo(User::class, 'driver_id');
    }

    /**
     * Un trajet peut avoir plusieurs réservations.
     */
    public function bookings() {
        return $this->hasMany(Booking::class);
    }

    // --- ACCESSEURS POUR CONVERTIR AU FORMAT RETROFIT ---

    public function getDriverNameAttribute() {
        return $this->driver ? $this->driver->name : 'Conducteur Inconnu';
    }

    public function getDriverRatingAttribute() {
        return $this->driver && isset($this->driver->rating) ? (float) $this->driver->rating : 5.0;
    }

    public function getDriverReviewsCountAttribute() {
        return $this->driver && isset($this->driver->trips_count) ? (int) $this->driver->trips_count : 0;
    }

    // Assure la transition si Android cherche 'departure' au lieu de 'departure_city'
    public function getDepartureAttribute() {
        return $this->departure_city;
    }

    // Assure la transition si Android cherche 'arrival' au lieu de 'arrival_city'
    public function getArrivalAttribute() {
        return $this->arrival_city;
    }

    // Transforme la chaîne "Climatisé,Wifi" en tableau ["Climatisé", "Wifi"] pour Android
    public function getAmenitiesAttribute($value) {
        if (empty($value)) return [];
        return is_array($value) ? $value : explode(',', $value);
    }

    // Mutateur pour stocker proprement le tableau Android ["Climatisé"] en string dans SQLite/MySQL
    public function setAmenitiesAttribute($value) {
        $this->attributes['amenities'] = is_array($value) ? implode(',', $value) : $value;
    }
}