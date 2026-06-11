<?php

namespace App\Http\Controllers;

use App\Models\Trip;
use App\Models\Booking;
use Illuminate\Http\Request;

class PaymentController extends Controller {

    /**
     * 💳 SIMULATION DE PAIEMENT STRIPE POUR LA SOUTENANCE (SANS CLÉ API)
     */
    public function createPaymentIntent(Request $request) {
        $request->validate([
            'booking_id' => 'required|exists:bookings,id',
        ]);

        try {
            // 1. Récupérer la réservation et le trajet associé avec le chauffeur
            $booking = Booking::with('trip.driver')->findOrFail($request->booking_id);
            $trip = $booking->trip;

            // 2. Calcul du montant total (Prix du trajet × Nombre de places)
            $totalAmount = $trip->price * $booking->seats;

            // 3. LOGIQUE DU SPLIT PAYMENT (Rémunération Conducteur)
            // 10% de commission pour l'application WayGo, 90% pour le chauffeur
            $commissionPlatform = $totalAmount * 0.10; 
            $driverEarnings = $totalAmount - $commissionPlatform;

            // 4. Génération de faux jetons au format exact de Stripe
            // On imite parfaitement la structure des réponses de l'API Stripe
            $fakePaymentIntentId = 'pi_mock_' . bin2hex(random_bytes(12));
            $fakeClientSecret = $fakePaymentIntentId . '_secret_' . bin2hex(random_bytes(10));

            // 5. Envoi de la réponse JSON attendue par le SDK Android
            return response()->json([
                'client_secret'   => $fakeClientSecret,
                'ephemeral_key'   => $fakePaymentIntentId, 
                'publishable_key' => 'pk_test_WayGoMadaCarpooling2026', // Fausse clé publique pour la démo
                'amount_total'    => (double)$totalAmount,
                'driver_receives' => (double)$driverEarnings
            ], 200);

        } catch (\Exception $e) {
            return response()->json([
                'error' => 'Impossible de simuler le paiement Stripe : ' . $e->getMessage()
            ], 500);
        }
    }
}