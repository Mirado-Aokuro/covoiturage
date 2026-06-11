<?php

namespace Database\Seeders;

use App\Models\User;
use App\Models\Trip;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\DB;

class DatabaseSeeder extends Seeder {
    public function run(): void {
        
        // 🛠️ CORRECTION POUR SQLITE : Désactiver temporairement les contraintes
        DB::statement('PRAGMA foreign_keys = OFF;');
        
        User::truncate();
        Trip::truncate();
        
        // Réactiver les contraintes après le nettoyage
        DB::statement('PRAGMA foreign_keys = ON;');

        // 1. Création des comptes statiques indispensables pour l'application Android
        User::create([
            'name' => 'Admin Global',
            'email' => 'admin@covoiturage.com',
            'password' => Hash::make('password'),
            'role' => 'admin',
            'rating' => 5.0,
        ]);

        $defaultDriver = User::create([
            'name' => 'Mikey Kun', // 👈 Précédemment "Mikey Chauffeur", rendu plus naturel
            'email' => 'driver@covoiturage.com',
            'password' => Hash::make('password'),
            'role' => 'driver',
            'vehicle_model' => 'Sprinter Mercedes',
            'vehicle_plate' => '1234 TAB',
            'rating' => 4.8,
        ]);

        User::create([
            'name' => 'Antoine .M',
            'email' => 'passenger@covoiturage.com',
            'password' => Hash::make('password'),
            'role' => 'passenger',
            'rating' => 4.9,
        ]);

        // 2. 🛠️ MODIFICATION : Création de conducteurs avec des noms totalement uniques
        $fakeDriversData = [
            ['name' => 'Jack Gray', 'email' => 'jack@test.com', 'car' => 'Peugeot 406', 'plate' => '5678 TAA'],
            ['name' => 'Mickael Miller', 'email' => 'mickael@test.com', 'car' => 'Hyundai Starex', 'plate' => '9101 TAI'],
            ['name' => 'Eric Gordon', 'email' => 'eric@test.com', 'car' => 'Mazda Van', 'plate' => '2468 TAF'],
            ['name' => 'Bradley Raimond', 'email' => 'bradley@test.com', 'car' => 'Toyota Hiace', 'plate' => '1357 TAN'],
        ];

        $drivers = [$defaultDriver];
        foreach ($fakeDriversData as $data) {
            $drivers[] = User::create([
                'name' => $data['name'],
                'email' => $data['email'],
                'password' => Hash::make('password'),
                'role' => 'driver',
                'vehicle_model' => $data['car'],
                'vehicle_plate' => $data['plate'],
                'rating' => rand(40, 50) / 10, // Note réaliste entre 4.0 et 5.0
            ]);
        }

        // Villes de test recentrées exclusivement sur Madagascar avec coordonnées géographiques réelles
        $cities = [
            ['name' => 'Antananarivo', 'lat' => -18.8792, 'lng' => 47.5079],
            ['name' => 'Tamatave', 'lat' => -18.1492, 'lng' => 49.4023],
            ['name' => 'Majunga', 'lat' => -15.7167, 'lng' => 46.3167],
            ['name' => 'Fianarantsoa', 'lat' => -21.4500, 'lng' => 47.0833],
            ['name' => 'Toliara', 'lat' => -23.3500, 'lng' => 43.6667],
            ['name' => 'Antsiranana', 'lat' => -12.2778, 'lng' => 49.2917]
        ];

        // 3. Création de 15 trajets géolocalisés cohérents à Madagascar
        for ($i = 0; $i < 15; $i++) {
            $dep = $cities[array_rand($cities)];
            $arr = $cities[array_rand($cities)];
            
            // Éviter qu'une ville de départ soit identique à la ville d'arrivée
            while($dep['name'] === $arr['name']) {
                $arr = $cities[array_rand($cities)];
            }

            // Génération d'une instance Carbon pour manipuler facilement la date et l'heure
            $randomDateTime = now()->addDays(rand(1, 10))->setHour(rand(6, 18))->setMinute(0)->setSecond(0);

            Trip::create([
                'driver_id' => $drivers[array_rand($drivers)]->id,
                'departure_city' => $dep['name'],
                'arrival_city' => $arr['name'],
                
                // 🛠️ ALIGNEMENT ANDROID : Séparation stricte de la date et de l'heure au format texte
                'date' => $randomDateTime->format('Y-m-d'),
                'departure_time' => $randomDateTime->format('H:i'),
                
                'seats_available' => rand(2, 4), 
                'price' => rand(15, 45), 
                'departure_latitude' => $dep['lat'],
                'departure_longitude' => $dep['lng'],
                'arrival_latitude' => $arr['lat'],
                'arrival_longitude' => $arr['lng'],
            ]);
        }
    }
}