<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use Illuminate\Http\Request;

class TripController extends Controller {
    
    /**
     * 🚗 RECHERCHE DE TRAJETS - ALIGNÉE 100% SUR LA BASE DE DONNÉES (DEPARTURE_CITY / ARRIVAL_CITY)
     */
    public function index(Request $request) {
        // Nettoyage des filtres reçus depuis Android (alignés sur la BDD)
        $departureCity = trim($request->query('departure_city') ?? '');
        $arrivalCity = trim($request->query('arrival_city') ?? '');
        $date = trim($request->query('date') ?? '');
        $driverName = trim($request->query('driver_name') ?? '');

        // On ne montre que les trajets qui ont encore des places disponibles
        $query = Trip::with('driver')->where('seats_available', '>', 0);

        // 1. Filtrage par Ville de Départ (Recherche partielle insensible à la casse)
        if ($departureCity !== '') {
            $query->where('departure_city', 'LIKE', '%' . $departureCity . '%');
        }

        // 2. Filtrage par Ville d'Arrivée (Recherche partielle insensible à la casse)
        if ($arrivalCity !== '') {
            $query->where('arrival_city', 'LIKE', '%' . $arrivalCity . '%');
        }

        // 3. Filtrage par Date
        if ($date !== '') {
            $query->whereDate('date', $date);
        }

        // 4. Filtrage par Nom de conducteur
        if ($driverName !== '') {
            $query->whereHas('driver', function($q) use ($driverName) {
                $q->where('name', 'LIKE', '%' . $driverName . '%');
            });
        }

        // 5. Tri chronologique par heure de départ
        $query->orderBy('departure_time', 'asc');

        // 6. Transformation propre pour Android Studio
        $trips = $query->get()->map(function($trip) {
            return [
                'id' => $trip->id,
                'departure_city' => $trip->departure_city,
                'arrival_city' => $trip->arrival_city,
                'price' => (double)$trip->price,
                'date' => $trip->date, 
                'departure_time' => $trip->departure_time,
                'seats_available' => $trip->seats_available,
                'departure_latitude' => (double)$trip->departure_latitude,
                'departure_longitude' => (double)$trip->departure_longitude,
                'arrival_latitude' => (double)$trip->arrival_latitude,
                'arrival_longitude' => (double)$trip->arrival_longitude,
                'driver_name' => $trip->driver->name ?? 'Conducteur Anonyme',
                'driver_rating' => (double)($trip->driver->rating ?? 5.0),
            ];
        });

        return response()->json($trips, 200);
    }

    public function store(Request $request) {
        $validated = $request->validate([
            'departure_city' => 'required|string',
            'arrival_city' => 'required|string',
            'departure_latitude' => 'required|numeric',
            'departure_longitude' => 'required|numeric',
            'arrival_latitude' => 'required|numeric',
            'arrival_longitude' => 'required|numeric',
            'departure_time' => 'required',
            'date' => 'required|date|after_or_equal:today',
            'price' => 'required|numeric|min:0',
            'seats_available' => 'required|integer|min:1',
        ]);

        $validated['date'] = date('Y-m-d', strtotime($validated['date']));

        $trip = $request->user()->trips()->create($validated);

        return response()->json($trip, 201);
    }

    public function show($id) {
        return response()->json(Trip::with('driver')->findOrFail($id));
    }

    public function myPublished(Request $request) {
        $trips = Trip::where('driver_id', $request->user()->id)->get();
        return response()->json($trips);
    }

    public function update(Request $request, $id) {
        $trip = Trip::findOrFail($id);
        
        if ($trip->driver_id !== $request->user()->id) {
            return response()->json(['message' => 'Vous n\'êtes pas le propriétaire de ce trajet.'], 403);
        }

        $validated = $request->validate([
            'departure_city' => 'sometimes|string',
            'arrival_city' => 'sometimes|string',
            'departure_latitude' => 'sometimes|numeric',
            'departure_longitude' => 'sometimes|numeric',
            'arrival_latitude' => 'sometimes|numeric',
            'arrival_longitude' => 'sometimes|numeric',
            'departure_time' => 'sometimes',
            'date' => 'sometimes|date|after_or_equal:today',
            'price' => 'sometimes|numeric|min:0',
            'seats_available' => 'sometimes|integer|min:1',
        ]);

        if (isset($validated['date'])) {
            $validated['date'] = date('Y-m-d', strtotime($validated['date']));
        }

        $trip->update($validated);

        return response()->json([
            'message' => 'Trajet mis à jour avec succès.',
            'trip' => $trip
        ]);
    }

    public function destroy(Request $request, $id) {
        $trip = Trip::findOrFail($id);

        if ($trip->driver_id !== $request->user()->id) {
            return response()->json(['message' => 'Vous n\'êtes pas le propriétaire de ce trajet.'], 403);
        }

        $trip->delete();

        return response()->json([
            'message' => 'Trajet annulé et supprimé avec succès.'
        ]);
    }

    public function updateLocation(Request $request, $id) {
        $trip = Trip::findOrFail($id);

        if ($trip->driver_id !== $request->user()->id) {
            return response()->json(['message' => 'Action non autorisée. Vous n\'êtes pas le conducteur.'], 403);
        }

        $validated = $request->validate([
            'current_latitude' => 'required|numeric',
            'current_longitude' => 'required|numeric',
        ]);

        $trip->update([
            'departure_latitude' => $validated['current_latitude'],
            'departure_longitude' => $validated['current_longitude']
        ]);

        return response()->json([
            'status' => 'Position mise à jour',
            'latitude' => $trip->departure_latitude,
            'longitude' => $trip->departure_longitude
        ]);
    }

    public function getLocation($id) {
        $trip = Trip::select('id', 'departure_latitude', 'departure_longitude', 'driver_id')->findOrFail($id);
        
        return response()->json([
            'trip_id' => $trip->id,
            'latitude' => (double)$trip->departure_latitude,
            'longitude' => (double)$trip->departure_longitude
        ]);
    }
}