// Variables globales para guardar datos
let todasLasBicis = [];
let precioActualBici = 0;

document.addEventListener("DOMContentLoaded", async function() {
    // 1. Verificar si hay sesión (básico)
    if (!sessionStorage.getItem("usuarioLogueado")) {
        window.location.href = "index.html#login-anchor";
        return;
    }

    // Ejecutamos las 3 cargas en paralelo (nombre, estaciones y bicis)
    await Promise.all([
        cargarNombreUsuario(),
        cargarEstaciones(),
        cargarBicicletas()
    ]);

    // Listener para el filtro
    document.getElementById('filtro-estacion').addEventListener('change', filtrarBicicletas);

    // Listener para calcular precio en tiempo real en el modal
    document.getElementById('duracion').addEventListener('input', actualizarTotalModal);
});

// --- 1. CARGAR ESTACIONES (Para el select) ---
async function cargarEstaciones() {
    try {
        const response = await axios.get('/api/estaciones');
        const select = document.getElementById('filtro-estacion');

        response.data.forEach(est => {
            const option = document.createElement('option');
            option.value = est.nombre; // Filtramos por nombre de estación
            option.textContent = est.nombre;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error cargando estaciones", error);
    }
}

// --- 2. CARGAR BICICLETAS DISPONIBLES ---
async function cargarBicicletas() {
    const container = document.getElementById('bicicletas-container');

    try {
        // Usamos el endpoint específico que creaste para disponibles
        const response = await axios.get('/api/bicicletas/disponibles');
        todasLasBicis = response.data;

        renderizarBicicletas(todasLasBicis);

    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger text-center">No se pudieron cargar las bicicletas. ¿Has iniciado sesión?</div>`;
        console.error(error);
    }
}

// --- 3. RENDERIZAR TARJETAS (HTML Dinámico) ---
function renderizarBicicletas(listaBicis) {
    const container = document.getElementById('bicicletas-container');
    container.innerHTML = ''; // Limpiar loader

    if (listaBicis.length === 0) {
        container.innerHTML = `<div class="col-12 text-center text-muted py-5"><h4><i class="fas fa-bicycle"></i> No hay bicicletas disponibles con este filtro.</h4></div>`;
        return;
    }

    listaBicis.forEach(bici => {
        // Asignar imagen según tipo
        let imagenUrl = '';
        switch(bici.tipo) {
            case 'MONTANA':
                imagenUrl = 'img/montana.jpg';
                break;
            case 'ELECTRICA':
                imagenUrl = 'img/electrica.jpg';
                break;
            case 'PASEO':
                imagenUrl = 'img/paseo.jpg';
                break;
            default:
                imagenUrl = 'img/paseo.jpg';
                break;
        }

        // Traducción del tipo de bici para el usuario
        let tipoBonito = bici.tipo;
        if (bici.tipo === 'MONTANA') tipoBonito = 'Montaña';
        if (bici.tipo === 'ELECTRICA') tipoBonito = 'Eléctrica';
        if (bici.tipo === 'PASEO') tipoBonito = 'Paseo';

        const html = `
        <div class="col-md-6 col-lg-4 fade-in">
            <div class="card h-100 shadow-sm border-0 bike-card">
                <div class="position-relative">
                    <img src="${imagenUrl}" class="card-img-top" alt="Bicicleta" style="height: 200px; object-fit: cover;">
                    <span class="position-absolute top-0 end-0 badge bg-success m-2 shadow">
                        <i class="fas fa-check-circle"></i> Disponible
                    </span>
                </div>
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="card-title fw-bold mb-0">${tipoBonito}</h5>
                        <span class="text-success fw-bold h5">$${bici.precio} <small class="text-muted fs-6">/h</small></span>
                    </div>
                    <p class="card-text text-muted small">
                        <i class="fas fa-map-marker-alt text-danger"></i> ${bici.ubicacion}
                        <br>
                        <i class="fas fa-barcode"></i> Código: ${bici.codigoBicicleta}
                    </p>
                </div>
                <div class="card-footer bg-white border-0 pb-3 text-center">
                    <button class="btn btn-outline-success w-100 rounded-pill fw-bold"
                        onclick="abrirModalAlquiler(${bici.id}, '${bici.codigoBicicleta}', '${bici.ubicacion}', ${bici.precio})">
                        Alquilar Ahora
                    </button>
                </div>
            </div>
        </div>
        `;
        container.innerHTML += html;
    });
}

// --- 4. FILTRADO ---
function filtrarBicicletas() {
    const filtro = document.getElementById('filtro-estacion').value;

    if (filtro === 'todas') {
        renderizarBicicletas(todasLasBicis);
    } else {
        const filtradas = todasLasBicis.filter(b => b.ubicacion === filtro);
        renderizarBicicletas(filtradas);
    }
}

// --- 5. LÓGICA DEL MODAL ---
function abrirModalAlquiler(id, codigo, ubicacion, precio) {
    // Guardar datos en el modal
    document.getElementById('modal-bici-id').value = id;
    document.getElementById('modal-bici-codigo').textContent = `Bicicleta ${codigo}`;
    document.getElementById('modal-bici-ubicacion').textContent = ubicacion;
    document.getElementById('modal-bici-precio').textContent = `$${precio} / hora`;

    precioActualBici = precio;
    document.getElementById('duracion').value = 1;
    actualizarTotalModal();

    // Mostrar modal (Bootstrap)
    const modal = new bootstrap.Modal(document.getElementById('modalAlquiler'));
    modal.show();
}

function actualizarTotalModal() {
    const horas = document.getElementById('duracion').value;
    const total = (horas * precioActualBici).toFixed(2);
    document.getElementById('calculo-total').textContent = `$${total}`;
}

// --- 6. ACCIÓN: ALQUILAR (POST) ---
async function confirmarAlquiler() {
    const biciId = document.getElementById('modal-bici-id').value;
    const horas = document.getElementById('duracion').value;

    if (horas <= 0) {
        Swal.fire('Error', 'La duración debe ser al menos 1 hora', 'error');
        return;
    }

    try {
        // Mostrar cargando
        Swal.fire({ title: 'Procesando...', didOpen: () => Swal.showLoading() });

        const payload = {
            bicicletaId: parseInt(biciId),
            duracionEnHoras: parseInt(horas)
        };

        // Petición al backend
        const response = await axios.post('/api/alquileres', payload);

        if (response.status === 201) {
            // Cerrar modal
            const modalEl = document.getElementById('modalAlquiler');
            const modal = bootstrap.Modal.getInstance(modalEl);
            modal.hide();

            // Éxito (SweetAlert)
            await Swal.fire({
                icon: 'success',
                title: '¡Alquiler Iniciado!',
                text: 'Tu bicicleta ha sido desbloqueada. ¡Disfruta el viaje!',
                confirmButtonColor: '#198754'
            });

            // Recargar la lista para quitar la bici alquilada
            cargarBicicletas();
        }

    } catch (error) {
        console.error(error);
        let mensaje = 'No se pudo procesar el alquiler.';
        if (error.response && error.response.data) {
            mensaje = error.response.data; // Mensaje del backend ("Bicicleta ocupada")
        }
        Swal.fire('Oops...', mensaje, 'error');
    }
}

// --- FUNCIÓN PARA CARGAR EL NOMBRE ---
async function cargarNombreUsuario() {
    try {
        // 1. Llamar al endpoint que devuelve el DTO del usuario actual
        const response = await axios.get('/api/usuarios/actual');
        const usuario = response.data;

        // 2. Insertar el nombre en el span que creamos
        document.getElementById('navbar-username').textContent = usuario.nombre;

    } catch (error) {
        console.error("Error cargando nombre de usuario", error);
        // Si falla (sesión expirada), lo mandamos al index
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            window.location.href = 'index.html';
        }
    }
}