<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Trip extends Model {
    use HasFactory;

    protected $fillable = ['driver_id', 'departure_city', 'arrival_city', 'departure_time', 'available_seats', 'price', 'departure_latitude', 'departure_longitude'];

    public function driver() {
        return $this->belongsTo(User::class, 'driver_id');
    }

    public function bookings() {
        return $this->hasMany(Booking::class);
    }
}