package DSII.TurismoSustentable.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.WebAttributes;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpMethod;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebAuthorization {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

// REGLAS DE AUTORIZACION - IMPLEMENTACION DE ROLES
        http.authorizeRequests()

                // 1. RUTAS PÚBLICAS Y ESTÁTICOS (PermitAll para carga del frontend)
                .antMatchers("/", "/index.html", "/registro.html", "/css/**", "/js/**", "/img/**", "/*.ico").permitAll()

                // 2. APIs PÚBLICAS Y DE AUTENTICACIÓN
                .antMatchers(HttpMethod.POST, "/api/usuarios").permitAll() // Registro
                .antMatchers("/api/login", "/api/logout").permitAll()

                // PERMITE VER ESTACIONES EN EL INDEX
                .antMatchers(HttpMethod.GET, "/api/estaciones").permitAll()

                // PAGINAS PRIVADAS
                .antMatchers("/alquiler.html", "/mis_alquileres.html").hasAnyAuthority("ADMIN", "CLIENTE")
                .antMatchers("/admin.html").hasAnyAuthority("ADMIN")

                // 3. EXCEPCIONES ESPECÍFICAS (Acciones de CLIENTE/ADMIN)
                .antMatchers(HttpMethod.POST, "/api/alquileres").hasAnyAuthority("ADMIN", "CLIENTE")
                .antMatchers(HttpMethod.PATCH, "/api/alquileres/finalizar/**").hasAnyAuthority("ADMIN", "CLIENTE")

                // 4. RESTRICCIONES Duras (Solo ADMIN)
                // El orden de las restricciones duras es importante:

                // Protege tu dashboard y cualquier ruta futura de admin.
                .antMatchers("/api/admin/**").hasAuthority("ADMIN")

                // --- INICIO DE LAS NUEVAS REGLAS PARA ESTACIONES ---
                // (Las ponemos aquí para ser específicos)
                .antMatchers(HttpMethod.POST, "/api/estaciones").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/estaciones/**").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/estaciones/**").hasAuthority("ADMIN")
                // --- FIN DE LAS NUEVAS REGLAS ---

                // REGLAS GENERALES DE ADMIN (Catch-all)
                // Bloquea todo POST que no sea /usuarios o /alquileres
                .antMatchers(HttpMethod.POST, "/api/**").hasAuthority("ADMIN")
                // Bloquea el resto de acciones a todas las rutas de la API
                .antMatchers(HttpMethod.PUT, "/api/**").hasAuthority("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("ADMIN")

                // 5. ACCESOS DE LECTURA (GET)
                // Esta regla YA PERMITE a CLIENTE y ADMIN ver /api/estaciones (GET)
                .antMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority("ADMIN", "CLIENTE")

                // Regla de cierre: Niega CUALQUIER otra cosa que no hayamos permitido explícitamente.
                .anyRequest().denyAll();

        // CONFIGURACION DEL LOGIN/LOGOUT (FormLogin) - Mantiene la configuración REST
        http.formLogin()
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // CONFIGURACION REST (Reemplaza HTML por códigos HTTP)
        http.csrf().disable();
        http.headers().frameOptions().disable();

        http.exceptionHandling().authenticationEntryPoint((req, res, exc) ->
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        http.formLogin().successHandler((req, res, auth) ->
                clearAuthenticationAttributes(req));

        http.formLogin().failureHandler((req, res, exc) ->
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());

        return http.build();
    }

    // Función de utilidad requerida por el successHandler
    private void clearAuthenticationAttributes (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute (WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}