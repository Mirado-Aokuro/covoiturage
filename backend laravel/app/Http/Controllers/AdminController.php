<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use App\Models\User;
use App\Models\Booking;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class AdminController extends Controller {
    
    public function dashboard() {
        // 1. STATISTIQUES GLOBALES ENRICHIES (SEMAINE 3)
        $totalUsers = User::count();
        $totalTrips = Trip::count();
        $totalBookings = Booking::count();

        // Calcul du Chiffre d'Affaires Réel (Uniquement sur les réservations 'confirmed')
        $totalRevenue = DB::table('bookings')
            ->join('trips', 'bookings.trip_id', '=', 'trips.id')
            ->where('bookings.status', 'confirmed')
            ->sum(DB::raw('bookings.seats * trips.price'));

        // Split Payment : 10% Plateforme, 90% Chauffeurs
        $platformCommission = $totalRevenue * 0.10;
        $driversEarnings = $totalRevenue * 0.90;

        $stats = [
            'users_count' => $totalUsers,
            'trips_count' => $totalTrips,
            'bookings_count' => $totalBookings,
            'total_revenue' => round($totalRevenue, 2),
            'platform_commission' => round($platformCommission, 2),
            'drivers_earnings' => round($driversEarnings, 2),
        ];

        // 2. ANALYSE DES ZONES POPULAIRES & TRAJETS / JOUR (SEMAINE 3)
        $popularDestinations = Trip::select('departure_city', DB::raw('count(*) as total'))
            ->groupBy('departure_city')
            ->orderBy('total', 'desc')
            ->take(3)
            ->get();

        $tripsPerDay = Trip::select(DB::raw('DATE(date) as day'), DB::raw('count(*) as total'))
            ->groupBy('day')
            ->orderBy('day', 'asc')
            ->take(7)
            ->get();

        // 3. TRAJETS GÉOLOCALISÉS (Pour ta carte Leaflet.js)
        $activeTrips = Trip::with('driver')
            ->whereNotNull('departure_latitude')
            ->whereNotNull('departure_longitude')
            ->get()
            ->map(function ($trip) {
                return [
                    'departure_latitude' => $trip->departure_latitude,
                    'departure_longitude' => $trip->departure_longitude,
                    'departure_city' => $trip->departure_city ?? $trip->departure, 
                    'arrival_city' => $trip->arrival_city ?? $trip->arrival,         
                    'price' => $trip->price,
                    'available_seats' => $trip->seats_available, 
                    'driver' => [
                        'name' => $trip->driver->name ?? 'Conducteur Anonyme'
                    ]
                ];
            });

        // 4. LES 5 DERNIERS TRAJETS (Pour ton tableau Bootstrap)
        $latestTripsRaw = Trip::with('driver')->latest()->take(5)->get();
        
        $latestTrips = $latestTripsRaw->map(function ($trip) {
            return (object)[
                'departure_city' => $trip->departure_city ?? $trip->departure,
                'arrival_city' => $trip->arrival_city ?? $trip->arrival,
                'departure_time' => $trip->date . ' ' . ($trip->departure_time ?? '00:00:00'),
                'available_seats' => $trip->seats_available,
                'price' => $trip->price,
                'driver' => (object)[
                    'name' => $trip->driver->name ?? 'Conducteur Anonyme'
                ]
            ];
        });

        // 5. ENVOI GROUPÉ À LA VUE BLADE
        return view('admin.dashboard', compact('stats', 'activeTrips', 'latestTrips', 'popularDestinations', 'tripsPerDay'));
    }

    /**
     * 📊 EXPORT DES RAPPORTS MENSUELS (SEMAINE 3)
     * Permet de télécharger un condensé des chiffres clés de l'application
     */
    public function exportReport() {
        $bookings = Booking::with(['trip', 'passenger'])->where('status', 'confirmed')->get();
        
        $csvHeader = ["ID Réservation", "Passager", "Trajet", "Places", "Prix Total", "Commission (10%)", "Date"];
        $csvData = [];

        foreach ($bookings as $booking) {
            $totalPrice = $booking->seats * $booking->trip->price;
            $csvData[] = [
                $booking->id,
                $booking->passenger->name ?? 'Anonyme',
                ($booking->trip->departure_city ?? $booking->trip->departure) . ' -> ' . ($booking->trip->arrival_city ?? $booking->trip->arrival),
                $booking->seats,
                $totalPrice . ' €',
                ($totalPrice * 0.10) . ' €',
                $booking->created_at->format('Y-m-d')
            ];
        }

        // Génération dynamique du fichier téléchargeable directement depuis le navigateur de l'admin
        $filename = "WayGo_Rapport_Mensuel_" . date('Y-m-d') . ".csv";
        $handle = fopen('php://output', 'w');
        
        header('Content-Type: text/csv');
        header('Content-Disposition: attachment; filename="' . $filename . '"');

        fputcsv($handle, $csvHeader);
        foreach ($csvData as $row) {
            fputcsv($handle, $row);
        }
        
        fclose($handle);
        exit;
    }
}