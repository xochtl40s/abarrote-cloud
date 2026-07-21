package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    private final CustomSuccessHandler customSuccessHandler;

    public SecurityConfig(
            UserDetailsServiceImpl userDetailsService,
            CustomSuccessHandler customSuccessHandler) {

        this.userDetailsService =
                userDetailsService;

        this.customSuccessHandler =
                customSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                /*
                 * Actualmente el POS utiliza peticiones JSON sin token
                 * CSRF. Más adelante se puede habilitar CSRF solamente
                 * para formularios administrativos.
                 */
                .csrf(
                        csrf ->
                                csrf.disable()
                )

                .authorizeHttpRequests(
                        auth -> auth

                                /*
                                 * Recursos públicos.
                                 */
                                .requestMatchers(
                            "/",
                            "/inicio",
                            "/commerce-cloud",
                            "/productos",
                            "/commerce/**",
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/favicon.ico"
                    ).permitAll()
                    .requestMatchers(
                                        "/login",
                                        "/error",
                                        "/api/version",
                                        "/css/**",
                                        "/js/**",
                                        "/images/**",
                                        "/favicon.ico"
                                )
                                .permitAll()

                                /*
                                 * Ventas y catálogo POS.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/ventas/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "CAJERO"
                                )

                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/ventas/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "CAJERO"
                                )

                                /*
                                 * Consulta general de productos.
                                 */
                                .requestMatchers(
                                        HttpMethod.GET,
                                        "/api/productos/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "CAJERO"
                                )

                                /*
                                 * Administración de productos.
                                 */
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/productos/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.PUT,
                                        "/api/productos/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        HttpMethod.DELETE,
                                        "/api/productos/**"
                                )
                                .hasRole("ADMIN")

                                /*
                                 * APIs administrativas.
                                 */
                                .requestMatchers(
                                        "/api/usuarios/**",
                                        "/api/categorias/**",
                                        "/api/reportes/**"
                                )
                                .hasRole("ADMIN")


                                /*
                                 * Restaurante Cloud.
                                 */
                                .requestMatchers(
                                        "/restaurante/dashboard",
                                        "/restaurante/dashboard/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        "/restaurante/mesero",
                                        "/restaurante/mesero/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "MESERO"
                                )

                                .requestMatchers(
                                        "/restaurante/api/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "MESERO"
                                )

                                /*
                                 * Todas las pantallas administrativas
                                 * requieren rol ADMIN.
                                 */
                                .requestMatchers(
                                        "/admin",
                                        "/admin/**"
                                )
                                .hasRole("ADMIN")

                                /*
                                 * El POS puede ser utilizado por
                                 * cajeros y administradores.
                                 */
                                .requestMatchers(
                                        "/pos",
                                        "/pos/**"
                                )
                                .hasAnyRole(
                                        "ADMIN",
                                        "CAJERO"
                                )

                                .anyRequest()
                                .authenticated()
                )

                .formLogin(
                        form -> form
                                .loginPage("/login")
                                .successHandler(
                                        customSuccessHandler
                                )
                                .failureUrl(
                                        "/login?error"
                                )
                                .permitAll()
                )

                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl(
                                        "/login?logout"
                                )
                                .invalidateHttpSession(
                                        true
                                )
                                .clearAuthentication(
                                        true
                                )
                                .deleteCookies(
                                        "JSESSIONID"
                                )
                                .permitAll()
                )

                /*
                 * Evita mostrar la página Whitelabel 403.
                 *
                 * CAJERO intentando abrir /admin:
                 *     redirige a /pos?accesoDenegado
                 *
                 * ADMIN intentando una ruta no permitida:
                 *     redirige a /admin?accesoDenegado
                 */
                .exceptionHandling(
                        exceptions ->
                                exceptions
                                        .authenticationEntryPoint(
                                                (
                                                        request,
                                                        response,
                                                        exception
                                                ) -> response.sendRedirect(
                                                        "/login?sesionRequerida"
                                                )
                                        )
                                        .accessDeniedHandler(
                                                (
                                                        request,
                                                        response,
                                                        exception
                                                ) -> {

                                                    Authentication authentication =
                                                            org.springframework
                                                                    .security
                                                                    .core
                                                                    .context
                                                                    .SecurityContextHolder
                                                                    .getContext()
                                                                    .getAuthentication();

                                                    if (authentication == null
                                                            || !authentication.isAuthenticated()
                                                            || "anonymousUser"
                                                            .equalsIgnoreCase(
                                                                    authentication.getName()
                                                            )) {

                                                        response.sendRedirect(
                                                                "/login?sesionRequerida"
                                                        );

                                                        return;
                                                    }

                                                    boolean esAdministrador =
                                                            authentication
                                                                    .getAuthorities()
                                                                    .stream()
                                                                    .map(
                                                                            GrantedAuthority
                                                                                    ::getAuthority
                                                                    )
                                                                    .anyMatch(
                                                                            "ROLE_ADMIN"
                                                                                    ::equals
                                                                    );

                                                    boolean esCajero =
                                                            authentication
                                                                    .getAuthorities()
                                                                    .stream()
                                                                    .map(
                                                                            GrantedAuthority
                                                                                    ::getAuthority
                                                                    )
                                                                    .anyMatch(
                                                                            "ROLE_CAJERO"
                                                                                    ::equals
                                                                    );

                                                    if (esAdministrador) {

                                                        response.sendRedirect(
                                                                "/admin?accesoDenegado"
                                                        );

                                                        return;
                                                    }

                                                    if (esCajero) {

                                                        response.sendRedirect(
                                                                "/pos?accesoDenegado"
                                                        );

                                                        return;
                                                    }

                                                    response.sendError(
                                                            HttpServletResponse
                                                                    .SC_FORBIDDEN,
                                                            "Acceso denegado"
                                                    );
                                                }
                                        )
                )

                /*
                 * Una sesión por navegador.
                 * Las pestañas comparten la misma sesión.
                 */
                .sessionManagement(
                        session ->
                                session
                                        .sessionFixation(
                                                fixation ->
                                                        fixation
                                                                .migrateSession()
                                        )
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(
                userDetailsService
        );

        provider.setPasswordEncoder(
                passwordEncoder()
        );

        return new ProviderManager(
                provider
        );
    }
}
