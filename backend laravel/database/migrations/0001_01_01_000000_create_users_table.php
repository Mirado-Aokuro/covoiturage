<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void {
        Schema::create('users', function (Blueprint $table) {
            $table->id();
            $table->string('name');
            $table->string('email')->unique();
            $table->string('password');
            
            // Gestion stricte des rôles via ENUM
            $table->enum('role', ['passenger', 'driver', 'admin'])->default('passenger');
            
            // 🛠️ AJOUTS EXIGÉS PAR L'AGENT ANDROID
            $table->string('vehicle_model')->nullable(); // Modèle du véhicule du chauffeur
            $table->string('vehicle_plate')->nullable(); // Plaque d'immatriculation
            $table->double('rating')->default(0);        // Note globale de l'utilisateur

            $table->rememberToken();
            $table->timestamps();
        });
    }

    public function down(): void {
        Schema::dropIfExists('users');
    }
};