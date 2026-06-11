<?php

use App\Http\Controllers\AdminController;
use Illuminate\Support\Facades\Route;

// 📊 BACK-OFFICE ADMIN & RAPPORT (SEMAINE 3)
Route::get('/admin/dashboard', [AdminController::class, 'dashboard'])->name('admin.dashboard');
Route::get('/admin/export-report', [AdminController::class, 'exportReport'])->name('admin.export');