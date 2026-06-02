<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use Illuminate\Http\Request;

class TripController extends Controller {
    
    public function index(Request $request) {
        $query = Trip::with('driver');

        // Note: J'ai ajusté pour correspondre au modèle Android (departure au lieu de departure_city)
        if ($request->filled('departure_city')) {
            $query->where('departure', 'LIKE', '%' . $request->departure_city . '%');
        }

        if ($request->filled('arrival_city')) {
            $query->where('arrival', 'LIKE', '%' . $request->arrival_city . '%');
        }

        if ($request->filled('date')) {
            $query->whereDate('date', $request->date);
        }

        return response()->json($query->get());
    }

    public function store(Request $request) {
        $validated = $request->validate([
            'departure' => 'required|string', // Android envoie 'departure'
            'arrival' => 'required|string',
            'departure_latitude' => 'required|numeric',
            'departure_longitude' => 'required|numeric',
            'arrival_latitude' => 'nullable|numeric',
            'arrival_longitude' => 'nullable|numeric',
            'departure_time' => 'required',
            'arrival_time' => 'nullable',
            'date' => 'required|date|after_or_equal:today',
            'price' => 'required|numeric|min:0',
            'seats_available' => 'required|integer|min:1',
            'amenities' => 'nullable|array'
        ]);

        // On associe le voyage à l'utilisateur connecté (le chauffeur)
        $trip = $request->user()->trips()->create($validated);

        // IMPORTANT : Android attend l'objet Trip directement pour le parser
        return response()->json($trip, 201);
    }

    public function show($id) {
        // failOrFail pour renvoyer une 404 propre si pas trouvé
        return response()->json(Trip::with('driver')->findOrFail($id));
    }

    public function myPublished(Request $request) {
        // On récupère les trajets où l'utilisateur est le driver
        $trips = Trip::where('driver_id', $request->user()->id)->get();
        return response()->json($trips);
    }
}