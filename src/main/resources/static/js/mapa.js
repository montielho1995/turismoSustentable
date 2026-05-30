document.addEventListener("DOMContentLoaded", function() {
    
    // 1. Inicializar el mapa (Coordenadas de Río Grande, Tierra del Fuego)
    // Ajusta estas coordenadas si tus estaciones están en otro lado exacto.
    var map = L.map('map').setView([-53.7876, -67.7002], 14); 

    // 2. Cargar capa de mapa (OpenStreetMap - Gratis)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors'
    }).addTo(map);

    // 3. Conectar con tu API (Backend)
    fetch('/api/estaciones')
        .then(response => response.json())
        .then(estaciones => {
            
            estaciones.forEach(estacion => {
                // Definir color según disponibilidad (Heurística de Visibilidad)
                let colorIcon = 'blue';
                if (estacion.bicicletasDisponibles === 0) colorIcon = 'red';
                else if (estacion.bicicletasDisponibles < 3) colorIcon = 'gold';
                else colorIcon = 'green';

                // Crear icono personalizado (usando una API de iconos simples)
                var icon = new L.Icon({
                    iconUrl: `https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-${colorIcon}.png`,
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41]
                });

                // Simular coordenadas (IMPORTANTE: Como EstacionDTO aún no tiene lat/lng,
                // vamos a "inventarlas" basándonos en el ID para que aparezcan en el mapa.
                // EN EL FUTURO: Debes agregar latitud y longitud a tu entidad Estacion).
                
                // Offset pequeño para separar los marcadores si no tienes coordenadas reales
                let lat = -53.7876 + (Math.random() * 0.02 - 0.01); 
                let lng = -67.7002 + (Math.random() * 0.02 - 0.01);

                // Crear marcador
                L.marker([lat, lng], {icon: icon}).addTo(map)
                    .bindPopup(`
                        <div class="text-center">
                            <h5>${estacion.nombre}</h5>
                            <p><i class="fa-solid fa-location-dot"></i> ${estacion.direccion}</p>
                            <hr>
                            <p><strong>Disponibles:</strong> ${estacion.bicicletasDisponibles}</p>
                            <p class="text-muted"><small>Espacios libres: ${estacion.espaciosLibres}</small></p>
                            <button class="btn btn-sm btn-primary w-100" onclick="alquilarBici(${estacion.id})">
                                Ver Bicicletas
                            </button>
                        </div>
                    `);
            });
        })
        .catch(error => console.error('Error cargando estaciones:', error));
});

function alquilarBici(estacionId) {
    alert("Aquí iríamos a la página de selección de bicis para la estación: " + estacionId);
    // window.location.href = `estacion.html?id=${estacionId}`;
}