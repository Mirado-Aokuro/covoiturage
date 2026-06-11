<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WayGo · Panel Administration</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8fafc;
            color: #1e293b;
        }
        .navbar {
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
        }
        .stat-card {
            border: none;
            border-radius: 16px;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 2px 4px -1px rgba(0,0,0,0.03);
            background: #ffffff;
        }
        .stat-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1);
        }
        .icon-shape {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.25rem;
        }
        .card-custom {
            border: none;
            border-radius: 16px;
            background: #ffffff;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
            overflow: hidden;
        }
        #map { 
            height: 460px; 
            width: 100%; 
            border-radius: 12px;
        }
        .table modern-table th {
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.75rem;
            letter-spacing: 0.5px;
            color: #64748b;
        }
        .avatar-placeholder {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            background-color: #e2e8f0;
            color: #475569;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-weight: 600;
            font-size: 0.8rem;
        }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark py-3">
        <div class="container">
            <span class="navbar-brand d-flex align-items-center fw-bold gap-2">
                <i class="fa-solid fa-car-side text-info fs-4"></i> WayGo <span class="badge bg-info text-dark fw-medium fs-7">Admin</span>
            </span>
            <div class="text-white-50 small">
                <i class="fa-regular fa-clock me-1"></i> Supervision temps réel
            </div>
        </div>
    </nav>

    <div class="container mt-4 mb-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h2 class="fw-bold mb-1 text-slate-800">Vue d'ensemble</h2>
                <p class="text-muted mb-0">Activités globales de la plateforme de covoiturage universitaire</p>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <div class="col-md-4">
                <div class="card stat-card p-3">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <span class="text-muted text-uppercase small fw-bold">Utilisateurs</span>
                            <h3 class="fw-bold mb-0 mt-1">{{ $stats['users_count'] }}</h3>
                        </div>
                        <div class="icon-shape bg-primary-subtle text-primary">
                            <i class="fa-solid fa-users"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card p-3">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <span class="text-muted text-uppercase small fw-bold">Trajets Actifs</span>
                            <h3 class="fw-bold mb-0 mt-1">{{ $stats['trips_count'] }}</h3>
                        </div>
                        <div class="icon-shape bg-success-subtle text-success">
                            <i class="fa-solid fa-route"></i>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card stat-card p-3">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <span class="text-muted text-uppercase small fw-bold">Réservations</span>
                            <h3 class="fw-bold mb-0 mt-1">{{ $stats['bookings_count'] }}</h3>
                        </div>
                        <div class="icon-shape bg-warning-subtle text-warning">
                            <i class="fa-solid fa-ticket"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="card card-custom mb-4">
            <div class="card-header bg-white border-bottom py-3 d-flex align-items-center gap-2">
                <i class="fa-solid fa-map-location-dot text-secondary"></i>
                <h5 class="fw-bold mb-0 text-dark" style="font-size: 1.1rem;">Cartographie des trajets géolocalisés</h5>
            </div>
            <div class="card-body p-3">
                <div id="map"></div>
            </div>
        </div>

        <div class="card card-custom">
            <div class="card-header bg-white border-bottom py-3 d-flex align-items-center gap-2">
                <i class="fa-solid fa-list-ul text-secondary"></i>
                <h5 class="fw-bold mb-0 text-dark" style="font-size: 1.1rem;">Dernières offres de trajets publiées</h5>
            </div>
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0 modern-table">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">Conducteur</th>
                            <th>Axe de voyage</th>
                            <th>Date & Heure Départ</th>
                            <th>Disponibilité</th>
                            <th class="text-end pe-4">Participation</th>
                        </tr>
                    </thead>
                    <tbody>
                        @forelse($latestTrips as $trip)
                            <tr>
                                <td class="ps-4">
                                    <div class="d-flex align-items-center gap-2">
                                        <div class="avatar-placeholder">
                                            {{ strtoupper(substr($trip->driver->name ?? 'C', 0, 1)) }}
                                        </div>
                                        <span class="fw-semibold text-dark">{{ $trip->driver->name ?? 'Conducteur' }}</span>
                                    </div>
                                </td>
                                <td>
                                    <div class="d-flex align-items-center gap-2 font-medium">
                                        <span class="text-primary fw-medium">{{ $trip->departure_city }}</span>
                                        <i class="fa-solid fa-arrow-right text-muted fs-8"></i>
                                        <span class="text-success fw-medium">{{ $trip->arrival_city }}</span>
                                    </div>
                                </td>
                                <td class="text-secondary small">
                                    <i class="fa-regular fa-calendar-days me-1"></i>
                                    {{ \Carbon\Carbon::parse($trip->departure_time)->format('d/m/Y à H:i') }}
                                </td>
                                <td>
                                    <span class="badge bg-info-subtle text-info-emphasis rounded-pill px-2.5 py-1.5 small fw-medium">
                                        {{ $trip->available_seats }} places libres
                                    </span>
                                </td>
                                <td class="text-end pe-4 fw-bold text-dark fs-6">
                                    {{ number_format($trip->price, 2, ',', ' ') }} €
                                </td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="5" class="text-center py-4 text-muted">Aucun trajet publié pour le moment.</td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        // Configuration initiale de la carte centrée sur Madagascar
        var map = L.map('map').setView([-18.8792, 47.5079], 6);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        var activeTrips = @json($activeTrips);

        activeTrips.forEach(function(trip) {
            if (trip.departure_latitude && trip.departure_longitude) {
                var marker = L.marker([trip.departure_latitude, trip.departure_longitude]).addTo(map);
                
                var popupContent = `
                    <div style="font-family: 'Inter', sans-serif; padding: 5px; min-width: 180px;">
                        <h6 style="margin: 0 0 8px 0; font-weight: 700; color: #0284c7;"><i class="fa-solid fa-user-circle"></i> Conducteur : ${trip.driver ? trip.driver.name : 'Anonyme'}</h6>
                        <div style="font-size: 0.85rem; line-height: 1.4;">
                            <b><i class="fa-solid fa-location-dot text-primary"></i></b> ${trip.departure_city}<br>
                            <b><i class="fa-solid fa-flag-checkered text-success"></i></b> ${trip.arrival_city}<br>
                            <span style="display:inline-block; margin-top:5px;" class="badge bg-dark">Prix : ${trip.price} €</span>
                        </div>
                    </div>
                `;
                marker.bindPopup(popupContent);
            }
        });

        // Recentrage automatique dynamique selon les marqueurs disponibles
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