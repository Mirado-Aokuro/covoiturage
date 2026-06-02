<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use App\Models\User;
use App\Models\Booking;

class AdminController extends Controller {
    
    public function dashboard() {
        // 1. Statistiques globales
        $stats = [
            'users_count' => User::count(),
            'trips_count' => Trip::count(),
            'bookings_count' => Booking::count(),
        ];

        // 2. Trajets géolocalisés valides pour la carte Leaflet
        $activeTrips = Trip::with('driver')
            ->whereNotNull('departure_latitude')
            ->whereNotNull('departure_longitude')
            ->get();

        // 3. Les 5 derniers trajets pour le tableau
        $latestTrips = Trip::with('driver')->latest()->take(5)->get();

        // 4. Envoi groupé à la vue unique
        return view('admin.dashboard', compact('stats', 'activeTrips', 'latestTrips'));
    }
}