/**
 * Función Universal de Logout.
 * Cierra la sesión del backend (POST), limpia el frontend (sessionStorage),
 * muestra un "Toast" de éxito y redirige al index.
 */
async function logout() {
    
    // 1. Definir el "Toast"
    const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 1500, // Dura 1.5 segundos
        timerProgressBar: true
    });

    try {
        // 2. Llama al backend (POST)
        await axios.post('/api/logout'); 
    } catch (error) {
        console.error("La sesión del backend ya había expirado o falló el logout:", error);
    }

    // 3. Limpiar la sesión del frontend (SIEMPRE)
    sessionStorage.removeItem("usuarioLogueado");
    sessionStorage.removeItem("usuarioRol");

    // 4. Mostrar la notificación de éxito
    Toast.fire({
        icon: 'success',
        title: '¡Sesión cerrada con éxito!'
    });

    // 5. Esperar 1.5s y redirigir al index
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 1500);
}