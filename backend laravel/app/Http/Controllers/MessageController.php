<?php

namespace App\Http\Controllers;

use App\Models\Message;
use Illuminate\Http\Request;

class MessageController extends Controller {
    
    // Envoyer un message
    public function store(Request $request) {
        $validated = $request->validate([
            'trip_id' => 'required|exists:trips,id',
            'content' => 'required|string',
        ]);

        $message = Message::create([
            'trip_id' => $validated['trip_id'],
            'sender_id' => $request->user()->id,
            'content' => $validated['content'],
        ]);

        return response()->json($message, 201);
    }

    // Récupérer la discussion d'un trajet
    public function getMessages($trip_id) {
        $messages = Message::where('trip_id', $trip_id)
            ->with('sender')
            ->orderBy('created_at', 'asc')
            ->get();
            
        return response()->json($messages);
    }
}