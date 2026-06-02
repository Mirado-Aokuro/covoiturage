<?php

namespace database\seeders;

use App\Models\User;
use App\Models\Trip;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class DatabaseSeeder extends Seeder {
    public function run(): void {
        // 1. Création d'un Admin pour le dashboard
        User::create([
            'name' => 'Admin Global',
            'email' => 'admin@covoiturage.com',
            'password' => Hash::make('password'),
            'role' => 'admin',
        ]);

        // 2. Création de 5 Conducteurs
        $drivers = [];
        for ($i = 1; $i <= 5; $i++) {
            $drivers[] = User::create([
                'name' => "Conducteur $i",
                'email' => "driver$i@test.com",
                'password' => Hash::make('password'),
                'role' => 'driver',
            ]);
        }

        // Villes de test avec coordonnées géographiques approximatives (Exemple: France / Madagascar)
        $cities = [
            ['name' => 'Paris', 'lat' => 48.8566, 'lng' => 2.3522],
            ['name' => 'Lyon', 'lat' => 45.7640, 'lng' => 4.8357],
            ['name' => 'Marseille', 'lat' => 43.2965, 'lng' => 5.3698],
            ['name' => 'Tamatave', 'lat' => -18.1492, 'lng' => 49.4023],
            ['name' => 'Antananarivo', 'lat' => -18.8792, 'lng' => 47.5079]
        ];

        // 3. Création de 15 trajets géolocalisés
        for ($i = 0; $i < 15; $i++) {
            $dep = $cities[array_rand($cities)];
            $arr = $cities[array_rand($cities)];
            
            while($dep['name'] === $arr['name']) {
                $arr = $cities[array_rand($cities)];
            }

            Trip::create([
                'driver_id' => $drivers[array_rand($drivers)]->id,
                'departure_city' => $dep['name'],
                'arrival_city' => $arr['name'],
                'departure_time' => now()->addDays(rand(1, 10))->setHour(rand(6, 20))->setMinute(0),
                'available_seats' => rand(1, 4),
                'price' => rand(10, 50),
                'departure_latitude' => $dep['lat'],
                'departure_longitude' => $dep['lng'],
            ]);
        }
    }
}