<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Supervision Covoiturage</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        #map { 
            height: 450px; 
            width: 100%; 
            border-radius: 8px; 
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }
    </style>
</head>
<body class="bg-light">

    <nav class="navbar navbar-dark bg-dark">
        <div class="container-fluid">
            <span class="navbar-brand mb-0 h1">🚗 Covoiturage Admin</span>
        </div>
    </nav>

    <div class="container mt-4">
        <h2 class="mb-4">Tableau de bord de Supervision</h2>

        <div class="row mb-4">
            <div class="col-md-4">
                <div class="card text-white bg-primary mb-3">
                    <div class="card-body">
                        <h5 class="card-title">Utilisateurs inscrits</h5>
                        <p class="card-text fs-2">{{ $stats['users_count'] }}</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-white bg-success mb-3">
                    <div class="card-body">
                        <h5 class="card-title">Trajets créés</h5>
                        <p class="card-text fs-2">{{ $stats['trips_count'] }}</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-white bg-warning mb-3">
                    <div class="card-body">
                        <h5 class="card-title">Réservations globales</h5>
                        <p class="card-text fs-2">{{ $stats['bookings_count'] }}</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="card mb-4">
            <div class="card-header bg-dark text-white">
                <h5 class="mb-0">🗺️ Carte des trajets actifs géolocalisés</h5>
            </div>
            <div class="card-body p-2">
                <div id="map"></div>
            </div>
        </div>

        <div class="card mb-5">
            <div class="card-header bg-secondary text-white">
                <h5 class="mb-0">Derniers trajets publiés</h5>
            </div>
            <div class="card-body">
                <table class="table table-striped align-middle">
                    <thead>
                        <tr>
                            <th>Conducteur</th>
                            <th>Départ</th>
                            <th>Arrivée</th>
                            <th>Date & Heure</th>
                            <th>Places Initiales</th>
                            <th>Prix</th>
                        </tr>
                    </thead>
                    <tbody>
                        @foreach($latestTrips as $trip)
                            <tr>
                                <td><strong>{{ $trip->driver->name }}</strong></td>
                                <td>{{ $trip->departure_city }}</td>
                                <td>{{ $trip->arrival_city }}</td>
                                <td>{{ \Carbon\Carbon::parse($trip->departure_time)->format('d/m/Y H:i') }}</td>
                                <td><span class="badge bg-info text-dark">{{ $trip->available_seats }} places</span></td>
                                <td><strong>{{ $trip->price }} €</strong></td>
                            </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        // Centrage de base ajustable (Exemple ici positionné sur Madagascar)
        var map = L.map('map').setView([-18.8792, 47.5079], 6);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        var activeTrips = @json($activeTrips);

        activeTrips.forEach(function(trip) {
            if (trip.departure_latitude && trip.departure_longitude) {
                var marker = L.marker([trip.departure_latitude, trip.departure_longitude]).addTo(map);
                
                var popupContent = `
                    <div style="font-family: Arial, sans-serif;">
                        <h6 style="margin: 0 0 5px 0; color: #198754;">🚗 Trajet de ${trip.driver.name}</h6>
                        <b>Départ :</b> ${trip.departure_city}<br>
                        <b>Destination :</b> ${trip.arrival_city}<br>
                        <b>Prix :</b> ${trip.price} €<br>
                        <hr style="margin: 5px 0;">
                        <small>Places initiales : ${trip.available_seats}</small>
                    </div>
                `;
                marker.bindPopup(popupContent);
            }
        });

        // Ajustement dynamique des limites de vision de la carte s'il y a des marqueurs
        if (activeTrips.length > 0) {
            var markersArray = activeTrips.map(function(t) {
                if(t.departure_latitude && t.departure_longitude) {
                    return L.marker([t.departure_latitude, t.departure_longitude]);
                }
            }).filter(Boolean);
            
            if(markersArray.length > 0) {
                var group = new L.featureGroup(markersArray);
                map.fitBounds(group.getBounds().pad(0.1));
            }
        }
    </script>
</body>
</html>