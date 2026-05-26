<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use App\Models\User;
use App\Models\Booking;

class AdminController extends Controller {
    public function dashboard() {
        $stats = [
            'users_count' => User::count(),
            'trips_count' => Trip::count(),
            'bookings_count' => Booking::count(),
        ];

        $latestTrips = Trip::with('driver')->latest()->take(5)->get();

        return view('admin.dashboard', compact('stats', 'latestTrips'));
    }
}