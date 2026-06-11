<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\Booking;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Storage;

class AuthController extends Controller {
    
    // 🔑 INSCRIPTION (Register)
    public function register(Request $request) {
        $validated = $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'password' => 'required|string|min:6',
            'role' => 'required|string|in:passenger,driver' 
        ]);

        $user = User::create([
            'name' => $validated['name'],
            'email' => $validated['email'],
            'password' => Hash::make($validated['password']),
            'role' => $validated['role'], 
            'rating' => 0.0
        ]);

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'token' => $token,
            'user' => $this->appendUserStats($user)
        ], 201);
    }

    // 🔑 CONNEXION (Login)
    public function login(Request $request) {
        // 🛠️ COMPTE ADMIN DE SECOURS AUTOMATIQUE
        if (!User::where('email', 'admin@covoiturage.com')->exists()) {
            User::create([
                'name' => 'Admin Cov',
                'email' => 'admin@covoiturage.com',
                'password' => Hash::make('password'),
                'role' => 'admin',
                'rating' => 5.0
            ]);
        }

        // 🛠️ COMPTE CONDUCTEUR DÉDIÉ AUTOMATIQUE
        if (!User::where('email', 'driver@covoiturage.com')->exists()) {
            User::create([
                'name' => 'Mikey Chauffeur',
                'email' => 'driver@covoiturage.com',
                'password' => Hash::make('password'),
                'role' => 'driver',
                'vehicle_model' => 'Sprinter Mercedes',
                'vehicle_plate' => '1234 TAB',
                'rating' => 4.8
            ]);
        }

        // 🛠️ COMPTE PASSAGER DÉDIÉ AUTOMATIQUE
        if (!User::where('email', 'passenger@covoiturage.com')->exists()) {
            User::create([
                'name' => 'Antoine Passager',
                'email' => 'passenger@covoiturage.com',
                'password' => Hash::make('password'),
                'role' => 'passenger',
                'rating' => 5.0
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
            'user' => $this->appendUserStats($user)
        ]);
    }

    // 👤 RÉCUPÉRATION DE L'UTILISATEUR CONNECTÉ
    public function user(Request $request) {
        return response()->json($this->appendUserStats($request->user()));
    }

    // 👤 MODIFICATION DU PROFIL TEXTE - @PUT("user")
    public function updateProfile(Request $request) {
        $user = $request->user();

        // Validation souple pour éviter les blocages 422
        $validated = $request->validate([
            'name' => 'sometimes|string|max:255',
            'email' => 'sometimes|string|email|max:255|unique:users,email,' . $user->id,
            'vehicle_model' => 'sometimes|nullable|string|max:255',
            'vehicle_plate' => 'sometimes|nullable|string|max:255',
        ]);

        // Mise à jour des informations en Base de Données
        $user->update($validated);

        return response()->json($this->appendUserStats($user));
    }

    // 📸 MISE À JOUR DE L'AVATAR - @Multipart / @POST("user/avatar")
    public function updateAvatar(Request $request) {
        // Parfois, Retrofit n'envoie pas le bon Content-Type de l'image, on assouplit la validation à 'required' ou 'file'
        $request->validate([
            'avatar' => 'required|file|max:4096', // Augmenté à 4Mo pour les photos de smartphones
        ]);

        $user = $request->user();

        if ($request->hasFile('avatar')) {
            // Nettoyage de l'ancien avatar s'il existe
            if ($user->avatar_url) {
                $oldPath = str_replace(asset('storage/'), '', $user->avatar_url);
                Storage::disk('public')->delete($oldPath);
            }

            // Sauvegarde de l'image
            $path = $request->file('avatar')->store('avatars', 'public');
            $user->avatar_url = asset('storage/' . $path);
            $user->save();

            return response()->json($this->appendUserStats($user));
        }

        return response()->json(['message' => 'Aucun fichier reçu ou fichier invalide.'], 400);
    }

    /**
     * 🛠️ FONCTION PRIVÉE : Formate l'objet User avec toutes les clés requises par Android
     */
    private function appendUserStats($user) {
        $user->is_verified = true;
        
        // Garantir que les champs ne sont jamais manquants (évite les crashs côté Android)
        $user->vehicle_model = $user->vehicle_model ?? ($user->role === 'driver' ? "Véhicule non configuré" : null);
        $user->vehicle_plate = $user->vehicle_plate ?? ($user->role === 'driver' ? "---" : null);
        $user->rating = (double) ($user->rating ?? 5.0);

        if ($user->role === 'driver') {
            $user->trips_count = $user->trips()->count(); 
            $user->vehicle_info = $user->vehicle_model; 
        } else {
            $user->bookings_count = Booking::where('passenger_id', $user->id)->count(); 
        }

        return $user;
    }
}