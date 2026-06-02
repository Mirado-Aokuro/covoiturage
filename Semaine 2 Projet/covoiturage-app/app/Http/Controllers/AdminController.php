<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use App\Models\User;
use App\Models\Booking;

class AdminController extends Controller {
    public function dashboard() {
        // 1. Récupération des statistiques globales
        $stats = [
            'users_count' => User::count(),
            'trips_count' => Trip::count(),
            'bookings_count' => Booking::count(),
        ];

        // 2. Récupérer les trajets géolocalisés pour la carte Leaflet
        $activeTrips = Trip::with('driver')
            ->whereNotNull('departure_latitude')
            ->whereNotNull('departure_longitude')
            ->get();

        // 3. Récupérer les 5 derniers trajets pour le tableau de suivi
        $latestTrips = Trip::with('driver')->latest()->take(5)->get();

        // 4. Un seul et unique retour qui envoie TOUT à la vue Blade
        return view('admin.dashboard', compact('stats', 'activeTrips', 'latestTrips'));
    }
}