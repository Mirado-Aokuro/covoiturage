<?php

use App\Http\Controllers\AdminController;
use Illuminate\Support\Facades\Route;

// Dans un vrai projet, ajoutez un middleware d'authentification web ici
Route::get('/admin/dashboard', [AdminController::class, 'dashboard'])->name('admin.dashboard');