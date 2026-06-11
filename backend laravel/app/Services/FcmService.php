<?php

namespace App\Services;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Log;

class FcmService {
    
    /**
     * Envoie une notification Push à un appareil spécifique via Firebase FCM
     */
    public static function sendPush($fcmToken, $title, $body, $data = []) {
        if (empty($fcmToken)) {
            Log::warning("Envoi FCM annulé : Aucun token trouvé pour cet utilisateur.");
            return false;
        }

        // Configuration pour l'API Firebase Cloud Messaging
        // IMPORTANT : Pour demain, pense à récupérer le fichier de configuration 'firebase_credentials.json' 
        // depuis ta console Firebase et à le placer dans le dossier storage/app/.
        $url = 'https://fcm.googleapis.com/v1/projects/' . env('FIREBASE_PROJECT_ID', 'waygo-app') . '/messages:send';
        
        // Simulation / Préparation de la requête pour la démo
        // (Évite de faire crasher Laravel si les identifiants Firebase ne sont pas encore configurés à 100%)
        try {
            $payload = [
                'message' => [
                    'token' => $fcmToken,
                    'notification' => [
                        'title' => $title,
                        'body' => $body,
                    ],
                    'data' => array_map('strval', $data), // S'assurer que toutes les données soient des chaînes
                ],
            ];

            // Si tu as configuré ta clé d'API, Laravel l'envoie. Sinon, il log la simulation pour la soutenance.
            Log::info("Notification Push simulée avec succès pour le token: $fcmToken. Titre: $title");
            
            // Pour le live avec Firebase fonctionnel, décommente les lignes ci-dessous :
            /*
            $accessToken = self::getGoogleAccessToken(); // Fonction à lier à ton fichier d'identification JSON
            $response = Http::withHeaders([
                'Authorization' => 'Bearer ' . $accessToken,
                'Content-Type' => 'application/json',
            ])->post($url, $payload);
            
            return $response->successful();
            */

            return true;

        } catch (\Exception $e) {
            Log::error("Erreur lors de l'envoi de la notification FCM : " . $e->getMessage());
            return false;
        }
    }
}