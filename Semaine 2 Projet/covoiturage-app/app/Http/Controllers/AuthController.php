<?php

namespace App\Http\Controllers;

use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class AuthController extends Controller {
    
    public function register(Request $request) {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'password' => 'required|string|min:6',
        ]);

        $user = User::create([
            'name' => $validated['name'],
            'email' => $validated['email'],
            'password' => Hash::make($validated['password']),
        ]);

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'token' => $token,
            'user' => $user
        ], 201);
    }

    public function login(Request $request) {
        // 🛠️ COMPTE DE SECOURS FORCÉ : S'il n'existe pas en base, on le crée proprement
        if (!User::where('email', 'admin@covoiturage.com')->exists()) {
            User::create([
                'name' => 'Admin Test',
                'email' => 'admin@covoiturage.com',
                'password' => Hash::make('password'),
                'role' => 'passenger'
            ]);
        }

        $request->validate([
            'email' => 'required|email',
            'password' => 'required',
        ]);

        $user = User::where('email', $request->email)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json(['message' => 'Identifiants incorrects.'], 401);
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'token' => $token,
            'user' => $user
        ]);
    }

    public function user(Request $request) {
        $user = $request->user();
        $user->trips_count = $user->trips()->count();
        $user->rating = 4.8; 
        $user->is_verified = true;
        
        return response()->json($user);
    }
}