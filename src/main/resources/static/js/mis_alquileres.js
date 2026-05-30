document.addEventListener("DOMContentLoaded", cargarAlquileres);

async function cargarAlquileres() {
    try {
        const response = await axios.get('/api/alquileres');
        const alquileres = response.data;

        const containerActivos = document.getElementById('container-activos');
        const tbodyHistorial = document.getElementById('tbody-historial');

        containerActivos.innerHTML = '';
        tbodyHistorial.innerHTML = '';

        let hayActivos = false;

        alquileres.forEach(alq => {
            if (alq.estado === 'INICIADO') {
                // Renderizar Tarjeta de Activo
                hayActivos = true;
                containerActivos.innerHTML += `
                    <div class="alert alert-success d-flex justify-content-between align-items-center" role="alert">
                        <div>
                            <h4 class="alert-heading fw-bold"><i class="fas fa-bicycle"></i> ${alq.codigoBicicleta}</h4>
                            <p class="mb-0">Iniciado el: ${new Date(alq.fechaInicio).toLocaleString()}</p>
                            <p class="mb-0 small">Ubicación Retiro: ${alq.ubicacionRetiro}</p>
                        </div>
                        <button class="btn btn-danger fw-bold shadow-sm" onclick="devolverBici(${alq.id})">
                            <i class="fas fa-stop-circle"></i> DEVOLVER AHORA
                        </button>
                    </div>
                `;
            } else {
                // Renderizar Fila de Historial
                tbodyHistorial.innerHTML += `
                    <tr>
                        <td><span class="badge bg-light text-dark border">${alq.codigoBicicleta}</span></td>
                        <td>${new Date(alq.fechaInicio).toLocaleDateString()}</td>
                        <td>${new Date(alq.fechaFin).toLocaleDateString()}</td>
                        <td class="fw-bold text-success">$${alq.costoTotal}</td>
                        <td><span class="badge bg-secondary">${alq.estado}</span></td>
                    </tr>
                `;
            }
        });

        if (!hayActivos) {
            containerActivos.innerHTML = '<p class="text-muted mb-0">No tienes viajes en curso.</p>';
        }
        if (tbodyHistorial.innerHTML === '') {
            document.getElementById('mensaje-historial').textContent = 'Aún no tienes historial de viajes.';
        }

    } catch (error) {
        console.error(error);
        // Si da 403/401 es que no está logueado
        window.location.href = 'index.html';
    }
}

async function devolverBici(id) {
    const result = await Swal.fire({
        title: '¿Finalizar viaje?',
        text: "La bicicleta se marcará como disponible y se calculará el costo final.",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#dc3545', // Rojo
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, devolver',
        cancelButtonText: 'Cancelar'
    });

    if (result.isConfirmed) {
        try {
            // Llamada al endpoint PATCH
            const response = await axios.patch(`/api/alquileres/finalizar/${id}`);

            await Swal.fire(
                '¡Devuelta!',
                `Viaje finalizado. Costo final: $${response.data.costoTotal}`,
                'success'
            );
            cargarAlquileres(); // Recargar la lista

        } catch (error) {
            Swal.fire('Error', 'No se pudo finalizar el viaje', 'error');
        }
    }
}