<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use App\Models\Booking;
use App\Mail\BookingConfirmationMail;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Mail;

class BookingController extends Controller {
    
    public function store(Request $request) {
        $request->validate([
            'trip_id' => 'required|exists:trips,id',
            'seats' => 'required|integer|min:1'
        ]);

        $trip = Trip::findOrFail($request->trip_id);

        // Vérification basée sur le nom de clé attendu
        if ($trip->seats_available < $request->seats) {
            return response()->json(['message' => 'Pas assez de places disponibles.'], 422);
        }

        // Création de la réservation
        $booking = Booking::create([
            'trip_id' => $trip->id,
            'passenger_id' => $request->user()->id,
            'seats' => $request->seats,
            'status' => 'confirmed'
        ]);

        // Déduction des places directement sur la colonne
        $trip->decrement('seats_available', $request->seats);

        // Notification asynchrone par mail (Queue)
        $booking->load(['passenger', 'trip.driver']);
        Mail::to($request->user()->email)->queue(new BookingConfirmationMail($booking));

        return response()->json([
            'message' => 'Réservation effectuée avec succès.',
            'booking' => $booking
        ], 201);
    }

    public function index(Request $request) {
        // CRUCIAL : Android attend une liste d'objets TRIP.
        // On récupère les trajets associés aux réservations de l'utilisateur.
        $userId = $request->user()->id;
        
        $bookedTrips = Trip::with('driver')
            ->whereHas('bookings', function($query) use ($userId) {
                $query->where('passenger_id', $userId)->where('status', 'confirmed');
            })->get();

        return response()->json($bookedTrips);
    }
}