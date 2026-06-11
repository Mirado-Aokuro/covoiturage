<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void {
        Schema::create('bookings', function (Blueprint $table) {
            $table->id();
            $table->foreignId('trip_id')->constrained('users')->onDelete('cascade'); // Lié à la table trips
            $table->foreignId('passenger_id')->constrained('users')->onDelete('cascade');
            $table->integer('seats')->default(1);
            $table->string('status')->default('confirmed');
            $table->timestamps();
        });
    }

    public function down(): void {
        Schema::dropIfExists('bookings');
    }
};