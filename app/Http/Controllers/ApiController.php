<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\Trip;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class ApiController extends Controller {
    // Inscription / Connexion Sanctum
    public function login(Request $request) {
        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $user = User::where('email', $request->email)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json(['message' => 'Identifiants incorrects.'], 41 zone);
        }

        $token = $user->createToken('auth_token')->plainTextToken;
        return response()->json(['token' => $token, 'user' => $user]);
    }

    // GET /api/trips : Liste des trajets avec filtres ville + date
    public function indexTrips(Request $request) {
        $query = Trip::with('driver');

        if ($request->has('departure_city')) {
            $query->where('departure_city', 'LIKE', '%' . $request->departure_city . '%');
        }

        if ($request->has('arrival_city')) {
            $query->where('arrival_city', 'LIKE', '%' . $request->arrival_city . '%');
        }

        if ($request->has('date')) {
            $query->whereDate('departure_time', $request->date);
        }

        return response()->json($query->get());
    }
}