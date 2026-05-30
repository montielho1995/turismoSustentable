document.getElementById('registro-form').addEventListener('submit', async function(event) {
    event.preventDefault();

    const statusDiv = document.getElementById('status-message');
    statusDiv.classList.add('d-none');
    statusDiv.innerHTML = '';

    // 1. Recolectar datos del formulario
    const payload = {
        nombre: document.getElementById('reg-nombre').value,
        apellido: document.getElementById('reg-apellido').value,
        email: document.getElementById('reg-email').value,
        fechaNacimiento: document.getElementById('reg-nacimiento').value, // Puede ser ""
        genero: document.getElementById('reg-genero').value,           // Puede ser ""
        password: document.getElementById('reg-password').value
    };

    // --- ¡LA CORRECCIÓN! ---
    // Si la fecha está vacía, la convertimos en 'null'.
    // Java no sabe convertir "" a LocalDate, pero sí entiende null.
    if (payload.fechaNacimiento === "") {
        payload.fechaNacimiento = null;
    }

    // Si el género está vacío (opción "Selecciona..."), lo volvemos 'null'.
    // Java no sabe convertir "" a un Enum, pero sí entiende null.
    if (payload.genero === "") {
        payload.genero = null;
    }
    // -------------------------

    try {
        // 3. Mostrar estado de carga
        Swal.fire({
            title: 'Registrando...',
            text: 'Estamos creando tu cuenta.',
            didOpen: () => { Swal.showLoading() },
            allowOutsideClick: false
        });

        // 4. Enviar Petición POST (Ahora con payload "limpio")
        const response = await axios.post('/api/usuarios', payload);

        // 5. Éxito
        Swal.fire({
            icon: 'success',
            title: '¡Registro Exitoso!',
            text: 'Tu cuenta ha sido creada. Ahora puedes iniciar sesión.',
            confirmButtonColor: '#198754'
        }).then(() => {
            window.location.href = 'index.html#login-anchor';
        });

    } catch (error) {
        // 6. Manejo de Errores
        Swal.close();

        if (error.response && error.response.status === 400) {
            // AHORA SÍ: El backend recibe 'null', @Valid se activa,
            // y el GlobalExceptionHandler devuelve la lista 'validationErrors'.
            if (error.response.data && error.response.data.validationErrors) {
                mostrarErrores(error.response.data.validationErrors);
            } else {
                // Fallback por si algo más falla
                mostrarErrores(['Error de formato (inesperado). Revisa los campos.']);
            }
        } else if (error.response && error.response.status === 500) {
            mostrarErrores(['El correo electrónico ya está en uso.']);
        } else {
            mostrarErrores(['Error de conexión. Inténtalo de nuevo más tarde.']);
        }
        console.error('Error de registro:', error);
    }
});

/**
 * Muestra una lista de errores de validación en el div de estado.
 * @param {string[]} errors - Lista de mensajes de error
 */
function mostrarErrores(errors) {
    const statusDiv = document.getElementById('status-message');
    let html = '<strong>Por favor, corrige los siguientes errores:</strong><ul>';

    errors.sort();

    errors.forEach(err => {
        html += `<li>${err}</li>`;
    });

    html += '</ul>';
    statusDiv.innerHTML = html;
    statusDiv.classList.remove('d-none');
}