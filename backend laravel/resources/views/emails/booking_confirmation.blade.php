<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6;">
    <h2>Bonjour {{ $booking->passenger->name }},</h2>
    <p>Votre réservation pour le trajet <strong>{{ $booking->trip->departure_city }} ➔ {{ $booking->trip->arrival_city }}</strong> a bien été confirmée !</p>
    
    <ul>
        <li><strong>Départ :</strong> {{ \Carbon\Carbon::parse($booking->trip->departure_time)->format('d/m/Y à H:i') }}</li>
        <li><strong>Prix :</strong> {{ $booking->trip->price }} €</li>
        <li><strong>Conducteur :</strong> {{ $booking->trip->driver->name }}</li>
    </ul>

    <p>Bon voyage !</p>
</body>
</html>