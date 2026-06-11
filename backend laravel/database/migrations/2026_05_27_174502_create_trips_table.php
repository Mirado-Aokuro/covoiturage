<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void {
        Schema::create('trips', function (Blueprint $table) {
            $table->id();
            $table->foreignId('driver_id')->constrained('users')->onDelete('cascade');
            $table->string('departure_city');
            $table->string('arrival_city');
            
            // 🛠️ CHANGEMENT : Séparation de la Date et de l'Heure pour correspondre à Android
            $table->date('date'); 
            $table->string('departure_time'); 
            
            $table->integer('seats_available');
            $table->decimal('price', 8, 2);
            
            // Coordonnées géographiques (Départ et Arrivée)
            $table->decimal('departure_latitude', 10, 8)->nullable();
            $table->decimal('departure_longitude', 11, 8)->nullable();
            $table->decimal('arrival_latitude', 10, 8)->nullable();
            $table->decimal('arrival_longitude', 11, 8)->nullable();
            
            $table->timestamps();
        });
    }

    public function down(): void {
        Schema::dropIfExists('trips');
    }
};