<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard Supervision Covoiturage</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
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

        <div class="card">
            <div class="card-header bg-secondary text-white">
                <h5 class="mb-0">Derniers trajets publiés</h5>
            </div>
            <div class="card-body">
                <table class="table table-striped">
                    <thead>
                        <tr>
                            <th>Conducteur</th>
                            <th>Départ</th>
                            <th>Arrivée</th>
                            <th>Date & Heure</th>
                            <th>Places</th>
                            <th>Prix</th>
                        </tr>
                    </thead>
                    <tbody>
                        @foreach($latestTrips as $trip)
                            <tr>
                                <td>{{ $trip->driver->name }}</td>
                                <td>{{ $trip->departure_city }}</td>
                                <td>{{ $trip->arrival_city }}</td>
                                <td>{{ \Carbon\Carbon::parse($trip->departure_time)->format('d/m/Y H:i') }}</td>
                                <td>{{ $trip->available_seats }}</td>
                                <td>{{ $trip->price }} €</td>
                            </tr>
                        @endforeach
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</body>
</html>