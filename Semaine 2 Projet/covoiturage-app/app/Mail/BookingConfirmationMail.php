<?php

namespace App\Mail;

use App\Models\Booking;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Mail\Mailable;
use Illuminate\Mail\Mailables\Content;
use Illuminate\Mail\Mailables\Envelope;
use Illuminate\Queue\SerializesModels;

// Le "implements ShouldQueue" force Laravel à envoyer l'email en arrière-plan
class BookingConfirmationMail extends Mailable implements ShouldQueue {
    use Queueable, SerializesModels;

    public $booking;

    public function __construct(Booking $booking) {
        $this->booking = $booking;
    }

    public function envelope(): Envelope {
        return new Envelope(
            subject: 'Confirmation de votre réservation de covoiturage',
        );
    }

    public function content(): Content {
        return new Content(
            view: 'emails.booking_confirmation',
        );
    }
}