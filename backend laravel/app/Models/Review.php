<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Review extends Model {
    use HasFactory;

    protected $fillable = [
        'trip_id',
        'reviewer_id', // Celui qui note (Conducteur ou Passager)
        'reviewed_id', // Celui qui est noté (Conducteur ou Passager)
        'rating',
        'comment'
    ];

    /**
     * L'avis est lié à un trajet spécifique.
     */
    public function trip() {
        return $this->belongsTo(Trip::class);
    }

    /**
     * L'utilisateur qui a écrit l'avis.
     */
    public function reviewer() {
        return $this->belongsTo(User::class, 'reviewer_id');
    }

    /**
     * L'utilisateur qui reçoit l'avis.
     */
    public function reviewed() {
        return $this->belongsTo(User::class, 'reviewed_id');
    }
}