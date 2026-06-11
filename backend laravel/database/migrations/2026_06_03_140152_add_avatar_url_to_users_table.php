<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::table('users', function (Blueprint $table) {
            // On ajoute la colonne après le mot de passe, et elle peut être vide (nullable)
            $table->string('avatar_url')->nullable()->after('password');
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::table('users', function (Blueprint $table) {
            // En cas de retour en arrière, on supprime la colonne
            $table->dropColumn('avatar_url');
        });
    }
};