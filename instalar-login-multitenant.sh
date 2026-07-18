#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
JAVA_BASE="src/main/java/com/abarrote/abarroteapi"
TEMPLATES="src/main/resources/templates"
MIGRATIONS="src/main/resources/db/migration"
PROPERTIES="src/main/resources/application.properties"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP=".backups/login-multitenant-${TIMESTAMP}"

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR INSTALANDO LOGIN MULTITENANT"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR

cd "$PROJECT"

echo "============================================================"
echo " COMMERCE CLOUD"
echo " V102 - Login inteligente multi-tenant"
echo "============================================================"

test -f pom.xml
test -f "$JAVA_BASE/entity/Usuario.java"
test -f "$JAVA_BASE/repository/UsuarioRepository.java"
test -f "$JAVA_BASE/service/impl/UserDetailsServiceImpl.java"
test -f "$JAVA_BASE/config/CustomSuccessHandler.java"
test -f "$JAVA_BASE/multitenant/domain/Tenant.java"
test -f "$JAVA_BASE/multitenant/repository/TenantRepository.java"

mkdir -p \
    "$BACKUP" \
    "$JAVA_BASE/security" \
    "$JAVA_BASE/gym/web" \
    "$JAVA_BASE/multitenant/bootstrap" \
    "$TEMPLATES/gym" \
    "$MIGRATIONS"

cp "$JAVA_BASE/entity/Usuario.java" \
   "$BACKUP/Usuario.java.bak"

cp "$JAVA_BASE/repository/UsuarioRepository.java" \
   "$BACKUP/UsuarioRepository.java.bak"

cp "$JAVA_BASE/service/impl/UserDetailsServiceImpl.java" \
   "$BACKUP/UserDetailsServiceImpl.java.bak"

cp "$JAVA_BASE/config/CustomSuccessHandler.java" \
   "$BACKUP/CustomSuccessHandler.java.bak"

cp "$TEMPLATES/login.html" \
   "$BACKUP/login.html.bak"

echo
echo "[1/9] Creando migración V102..."

cat > "$MIGRATIONS/V102__agregar_tenant_a_usuario.sql" <<'EOF'
ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

DO $$
DECLARE
    tenant_abarrotes_id BIGINT;
BEGIN
    SELECT id
      INTO tenant_abarrotes_id
      FROM tenant
     WHERE LOWER(slug) = LOWER('abarrotes-principal')
     LIMIT 1;

    IF tenant_abarrotes_id IS NULL THEN
        RAISE EXCEPTION
            'No existe el tenant abarrotes-principal. Ejecute primero el bootstrap de tenants.';
    END IF;

    UPDATE usuario
       SET tenant_id = tenant_abarrotes_id
     WHERE tenant_id IS NULL;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_usuario_tenant'
    ) THEN
        ALTER TABLE usuario
            ADD CONSTRAINT fk_usuario_tenant
            FOREIGN KEY (tenant_id)
            REFERENCES tenant(id);
    END IF;
END
$$;

ALTER TABLE usuario
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_usuario_tenant
    ON usuario(tenant_id);

CREATE INDEX IF NOT EXISTS idx_usuario_tenant_activo
    ON usuario(tenant_id, activo);
EOF

echo
echo "[2/9] Reemplazando entidad Usuario..."

cat > "$JAVA_BASE/entity/Usuario.java" <<'EOF'
package com.abarrote.abarroteapi.entity;

import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(
        message = "El nombre no puede estar vacío"
    )
    @Column(
        nullable = false,
        length = 120
    )
    private String nombre;

    @NotBlank(
        message = "El nombre de usuario no puede estar vacío"
    )
    @Size(
        min = 4,
        max = 20,
        message = "El login debe tener entre 4 y 20 caracteres"
    )
    @Column(
        nullable = false,
        unique = true,
        length = 20
    )
    private String username;

    @NotBlank(
        message = "La contraseña es obligatoria"
    )
    @Column(nullable = false)
    private String password;

    @Column(
        nullable = false,
        length = 30
    )
    private String rol;

    @Column(nullable = false)
    private Boolean activo = true;

    /*
     * Un usuario pertenece obligatoriamente a un tenant.
     *
     * El tenant determina el vertical que debe abrirse
     * después del login:
     *
     * ABARROTES -> /admin o /pos
     * GYM       -> /gym/dashboard
     */
    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_usuario_tenant"
        )
    )
    private Tenant tenant;

    /*
     * Sucursal continúa siendo opcional porque un usuario
     * Gym todavía no utiliza la estructura de sucursales
     * heredada de Abarrote Cloud.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sucursal_id",
        foreignKey = @ForeignKey(
            name = "fk_usuario_sucursal"
        )
    )
    private Sucursal sucursal;

    public Usuario() {
    }

    public Usuario(
        String nombre,
        String username,
        String password,
        String rol
    ) {
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.rol = rol;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }
}
EOF

echo
echo "[3/9] Reemplazando UsuarioRepository..."

cat > "$JAVA_BASE/repository/UsuarioRepository.java" <<'EOF'
package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository
    extends JpaRepository<Usuario, Long> {

    /*
     * Carga usuario, sucursal y tenant en la misma consulta.
     *
     * Esto evita LazyInitializationException cuando
     * open-in-view está deshabilitado.
     */
    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario> findByUsernameIgnoreCase(
        String username
    );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    Optional<Usuario> findConSucursalById(
        Long id
    );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    List<Usuario> findBySucursalIdOrderByNombreAsc(
        Long sucursalId
    );

    @EntityGraph(
        attributePaths = {
            "sucursal",
            "tenant"
        }
    )
    List<Usuario>
        findBySucursalIdAndActivoTrueOrderByNombreAsc(
            Long sucursalId
        );

    @EntityGraph(
        attributePaths = {
            "tenant"
        }
    )
    List<Usuario> findByTenantIdOrderByNombreAsc(
        Long tenantId
    );
}
EOF

echo
echo "[4/9] Creando principal autenticado multi-tenant..."

cat > "$JAVA_BASE/security/CommerceUserPrincipal.java" <<'EOF'
package com.abarrote.abarroteapi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CommerceUserPrincipal extends User {

    private final Long usuarioId;

    private final String nombre;

    private final Long tenantId;

    private final String tenantSlug;

    private final String tenantNombre;

    private final String tipoNegocio;

    private final Long sucursalId;

    public CommerceUserPrincipal(
        Long usuarioId,
        String nombre,
        String username,
        String password,
        Collection<? extends GrantedAuthority> authorities,
        Long tenantId,
        String tenantSlug,
        String tenantNombre,
        String tipoNegocio,
        Long sucursalId
    ) {
        super(
            username,
            password,
            authorities
        );

        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.tenantId = tenantId;
        this.tenantSlug = tenantSlug;
        this.tenantNombre = tenantNombre;
        this.tipoNegocio = tipoNegocio;
        this.sucursalId = sucursalId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenantSlug() {
        return tenantSlug;
    }

    public String getTenantNombre() {
        return tenantNombre;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public boolean esGym() {
        return "GYM".equalsIgnoreCase(tipoNegocio);
    }

    public boolean esAbarrotes() {
        return !esGym();
    }

    public boolean tieneRol(String rol) {
        String autoridadEsperada =
            rol.startsWith("ROLE_")
                ? rol
                : "ROLE_" + rol;

        return getAuthorities()
            .stream()
            .anyMatch(
                authority ->
                    autoridadEsperada.equals(
                        authority.getAuthority()
                    )
            );
    }
}
EOF

echo
echo "[5/9] Reemplazando UserDetailsServiceImpl..."

cat > "$JAVA_BASE/service/impl/UserDetailsServiceImpl.java" <<'EOF'
package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl
    implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(
        UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
        String username
    ) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(
                () -> new UsernameNotFoundException(
                    "Usuario no encontrado: " + username
                )
            );

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException(
                "Usuario desactivado: " + username
            );
        }

        Tenant tenant = usuario.getTenant();

        if (tenant == null) {
            throw new UsernameNotFoundException(
                "El usuario no tiene un tenant asignado: "
                    + username
            );
        }

        String role = usuario.getRol();

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }

        List<SimpleGrantedAuthority> authorities =
            new ArrayList<>();

        authorities.add(
            new SimpleGrantedAuthority(role)
        );

        Long sucursalId =
            usuario.getSucursal() != null
                ? usuario.getSucursal().getId()
                : null;

        return new CommerceUserPrincipal(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getUsername(),
            usuario.getPassword(),
            authorities,
            tenant.getId(),
            tenant.getSlug(),
            tenant.getNombre(),
            tenant.getTipoNegocio().name(),
            sucursalId
        );
    }
}
EOF

echo
echo "[6/9] Reemplazando CustomSuccessHandler..."

cat > "$JAVA_BASE/config/CustomSuccessHandler.java" <<'EOF'
package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler
    implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {

        HttpSession session = request.getSession(true);

        if (
            authentication.getPrincipal()
                instanceof CommerceUserPrincipal principal
        ) {
            guardarContextoEnSesion(
                session,
                principal
            );

            if (principal.esGym()) {
                response.sendRedirect(
                    request.getContextPath()
                        + "/gym/dashboard"
                );
                return;
            }

            if (principal.tieneRol("CAJERO")) {
                response.sendRedirect(
                    request.getContextPath()
                        + "/pos"
                );
                return;
            }

            response.sendRedirect(
                request.getContextPath()
                    + "/admin"
            );

            return;
        }

        /*
         * Compatibilidad temporal por si algún proceso interno
         * todavía crea un User estándar de Spring Security.
         */
        boolean esAdministrador =
            authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        boolean esCajero =
            authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CAJERO"::equals);

        if (esAdministrador) {
            response.sendRedirect(
                request.getContextPath()
                    + "/admin"
            );
            return;
        }

        if (esCajero) {
            response.sendRedirect(
                request.getContextPath()
                    + "/pos"
            );
            return;
        }

        response.sendRedirect(
            request.getContextPath()
                + "/login?sinDestino"
        );
    }

    private void guardarContextoEnSesion(
        HttpSession session,
        CommerceUserPrincipal principal
    ) {
        session.setAttribute(
            "usuarioId",
            principal.getUsuarioId()
        );

        session.setAttribute(
            "usuarioNombre",
            principal.getNombre()
        );

        session.setAttribute(
            "tenantId",
            principal.getTenantId()
        );

        session.setAttribute(
            "tenantSlug",
            principal.getTenantSlug()
        );

        session.setAttribute(
            "tenantNombre",
            principal.getTenantNombre()
        );

        session.setAttribute(
            "tipoNegocio",
            principal.getTipoNegocio()
        );

        session.setAttribute(
            "sucursalId",
            principal.getSucursalId()
        );
    }
}
EOF

echo
echo "[7/9] Creando dashboard Gym..."

cat > "$JAVA_BASE/gym/web/GymDashboardController.java" <<'EOF'
package com.abarrote.abarroteapi.gym.web;

import com.abarrote.abarroteapi.gym.repository.ClienteGymRepository;
import com.abarrote.abarroteapi.gym.repository.PlanMembresiaRepository;
import com.abarrote.abarroteapi.security.CommerceUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GymDashboardController {

    private final ClienteGymRepository clienteRepository;

    private final PlanMembresiaRepository planRepository;

    public GymDashboardController(
        ClienteGymRepository clienteRepository,
        PlanMembresiaRepository planRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.planRepository = planRepository;
    }

    @GetMapping("/gym/dashboard")
    public String dashboard(
        @AuthenticationPrincipal
        CommerceUserPrincipal principal,
        Model model
    ) {
        if (principal == null) {
            return "redirect:/login?sesionRequerida";
        }

        if (!principal.esGym()) {
            return "redirect:/admin?moduloIncorrecto";
        }

        int totalClientes =
            clienteRepository
                .findAllByTenantIdOrderByNombreAsc(
                    principal.getTenantId()
                )
                .size();

        int totalPlanes =
            planRepository
                .findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
                    principal.getTenantId()
                )
                .size();

        model.addAttribute(
            "usuarioNombre",
            principal.getNombre()
        );

        model.addAttribute(
            "tenantNombre",
            principal.getTenantNombre()
        );

        model.addAttribute(
            "tenantSlug",
            principal.getTenantSlug()
        );

        model.addAttribute(
            "totalClientes",
            totalClientes
        );

        model.addAttribute(
            "totalPlanes",
            totalPlanes
        );

        return "gym/dashboard";
    }

    @GetMapping("/gym")
    public String inicioGym() {
        return "redirect:/gym/dashboard";
    }
}
EOF

cat > "$TEMPLATES/gym/dashboard.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>
    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0">

    <title>Gym Cloud | Commerce Cloud</title>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            min-height: 100vh;
            font-family:
                "Segoe UI",
                system-ui,
                sans-serif;
            color: #f8fafc;
            background:
                radial-gradient(
                    circle at top right,
                    #4c1d95 0,
                    transparent 32%
                ),
                linear-gradient(
                    135deg,
                    #09090b,
                    #111827
                );
        }

        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 1rem;
            padding: 1rem 2rem;
            border-bottom: 1px solid #27272a;
            background: rgba(9, 9, 11, 0.82);
            backdrop-filter: blur(12px);
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 0.8rem;
        }

        .brand-icon {
            font-size: 2rem;
        }

        .brand h1 {
            color: #c084fc;
            font-size: 1.35rem;
        }

        .brand p {
            color: #94a3b8;
            font-size: 0.82rem;
        }

        .logout {
            padding: 0.7rem 1rem;
            border: 1px solid #ef4444;
            border-radius: 10px;
            color: #fecaca;
            text-decoration: none;
            background: rgba(239, 68, 68, 0.12);
        }

        .main {
            width: min(1200px, 92%);
            margin: 0 auto;
            padding: 3rem 0;
        }

        .hero {
            margin-bottom: 2rem;
        }

        .hero span {
            display: inline-block;
            margin-bottom: 0.7rem;
            padding: 0.35rem 0.7rem;
            border: 1px solid #7e22ce;
            border-radius: 999px;
            color: #d8b4fe;
            background: rgba(126, 34, 206, 0.18);
        }

        .hero h2 {
            margin-bottom: 0.5rem;
            font-size: clamp(2rem, 5vw, 3.3rem);
        }

        .hero p {
            color: #94a3b8;
            font-size: 1.05rem;
        }

        .cards {
            display: grid;
            grid-template-columns:
                repeat(auto-fit, minmax(220px, 1fr));
            gap: 1.2rem;
        }

        .card {
            min-height: 170px;
            padding: 1.4rem;
            border: 1px solid #27272a;
            border-radius: 18px;
            background: rgba(24, 24, 27, 0.82);
            box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2);
        }

        .card-icon {
            margin-bottom: 1rem;
            font-size: 2rem;
        }

        .card h3 {
            margin-bottom: 0.5rem;
            color: #e9d5ff;
        }

        .card .number {
            margin-bottom: 0.4rem;
            color: #c084fc;
            font-size: 2rem;
            font-weight: 800;
        }

        .card p {
            color: #94a3b8;
            line-height: 1.5;
        }

        .module-status {
            display: inline-block;
            margin-top: 0.8rem;
            color: #86efac;
            font-size: 0.85rem;
        }

        @media (max-width: 600px) {
            .topbar {
                align-items: flex-start;
                padding: 1rem;
            }

            .main {
                width: 90%;
                padding-top: 2rem;
            }
        }
    </style>
</head>

<body>

<header class="topbar">

    <div class="brand">

        <div class="brand-icon">
            🏋️
        </div>

        <div>
            <h1>Gym Cloud</h1>
            <p>Vertical de Commerce Cloud</p>
        </div>

    </div>

    <a
        class="logout"
        th:href="@{/logout}">
        Cerrar sesión
    </a>

</header>

<main class="main">

    <section class="hero">

        <span
            th:text="${tenantSlug}">
            gym-titan
        </span>

        <h2>
            Bienvenido,
            <span
                th:text="${usuarioNombre}">
                Administrador Gym
            </span>
        </h2>

        <p>
            Administrando
            <strong
                th:text="${tenantNombre}">
                Gym Titan Ecatepec
            </strong>
        </p>

    </section>

    <section class="cards">

        <article class="card">

            <div class="card-icon">
                👥
            </div>

            <h3>Clientes</h3>

            <div
                class="number"
                th:text="${totalClientes}">
                0
            </div>

            <p>
                Socios registrados exclusivamente
                para este gimnasio.
            </p>

            <span class="module-status">
                ● Módulo activo
            </span>

        </article>

        <article class="card">

            <div class="card-icon">
                🎫
            </div>

            <h3>Planes</h3>

            <div
                class="number"
                th:text="${totalPlanes}">
                0
            </div>

            <p>
                Planes de membresía disponibles
                para los clientes.
            </p>

            <span class="module-status">
                ● Módulo activo
            </span>

        </article>

        <article class="card">

            <div class="card-icon">
                📅
            </div>

            <h3>Membresías</h3>

            <p>
                Control de vigencia, activación,
                suspensión y vencimiento.
            </p>

            <span class="module-status">
                ● Base implementada
            </span>

        </article>

        <article class="card">

            <div class="card-icon">
                💳
            </div>

            <h3>Pagos</h3>

            <p>
                Registro de pagos en efectivo,
                tarjeta y transferencia.
            </p>

            <span class="module-status">
                ● Base implementada
            </span>

        </article>

    </section>

</main>

</body>
</html>
EOF

echo
echo "[8/9] Creando bootstrap para admingym..."

cat > "$JAVA_BASE/multitenant/bootstrap/UsuarioGymBootstrapInitializer.java" <<'EOF'
package com.abarrote.abarroteapi.multitenant.bootstrap;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(
    name = "commerce.bootstrap.login-multitenant.enabled",
    havingValue = "true"
)
public class UsuarioGymBootstrapInitializer
    implements ApplicationRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            UsuarioGymBootstrapInitializer.class
        );

    private final UsuarioRepository usuarioRepository;

    private final TenantRepository tenantRepository;

    public UsuarioGymBootstrapInitializer(
        UsuarioRepository usuarioRepository,
        TenantRepository tenantRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        Tenant tenantAbarrotes =
            tenantRepository
                .findBySlugIgnoreCaseAndActivoTrue(
                    "abarrotes-principal"
                )
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe abarrotes-principal"
                    )
                );

        Tenant tenantGym =
            tenantRepository
                .findBySlugIgnoreCaseAndActivoTrue(
                    "gym-titan"
                )
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe gym-titan"
                    )
                );

        Usuario admin =
            usuarioRepository
                .findByUsernameIgnoreCase("admin")
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe el usuario admin"
                    )
                );

        /*
         * Garantiza que el administrador histórico quede
         * relacionado con Abarrote Cloud.
         */
        admin.setTenant(tenantAbarrotes);

        usuarioRepository.save(admin);

        Usuario adminGym =
            usuarioRepository
                .findByUsernameIgnoreCase("admingym")
                .orElseGet(
                    () -> new Usuario(
                        "Administrador Gym",
                        "admingym",
                        admin.getPassword(),
                        "ADMIN"
                    )
                );

        /*
         * Para la primera prueba admingym utiliza la misma
         * contraseña cifrada que admin.
         */
        adminGym.setNombre("Administrador Gym");
        adminGym.setPassword(admin.getPassword());
        adminGym.setRol("ADMIN");
        adminGym.setActivo(true);
        adminGym.setTenant(tenantGym);
        adminGym.setSucursal(null);

        usuarioRepository.save(adminGym);

        LOGGER.info(
            "Login multi-tenant listo: "
                + "admin -> abarrotes-principal, "
                + "admingym -> gym-titan"
        );
    }
}
EOF

echo
echo "[9/9] Reemplazando login por Commerce Cloud..."

cat > "$TEMPLATES/login.html" <<'EOF'
<!DOCTYPE html>
<html
    lang="es"
    xmlns:th="http://www.thymeleaf.org">

<head>

    <meta charset="UTF-8">

    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0">

    <meta
        name="theme-color"
        content="#101827">

    <title>Login | Commerce Cloud</title>

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.2rem;
            font-family:
                "Segoe UI",
                system-ui,
                sans-serif;
            background:
                radial-gradient(
                    circle at top left,
                    rgba(37, 99, 235, 0.35),
                    transparent 35%
                ),
                radial-gradient(
                    circle at bottom right,
                    rgba(147, 51, 234, 0.25),
                    transparent 35%
                ),
                #080d18;
        }

        .login-container {
            width: 100%;
            max-width: 430px;
            padding: 2.5rem;
            border: 1px solid #273449;
            border-radius: 22px;
            background: rgba(15, 23, 42, 0.94);
            box-shadow: 0 24px 80px rgba(0, 0, 0, 0.48);
        }

        .login-header {
            margin-bottom: 2rem;
            text-align: center;
        }

        .logo {
            margin-bottom: 0.6rem;
            font-size: 3.5rem;
        }

        h1 {
            color: #60a5fa;
            font-size: 1.9rem;
        }

        .subtitle {
            margin-top: 0.35rem;
            color: #94a3b8;
        }

        .verticals {
            margin-top: 0.8rem;
            color: #cbd5e1;
            font-size: 0.78rem;
        }

        .message {
            margin-bottom: 1rem;
            padding: 0.8rem;
            border-radius: 9px;
            text-align: center;
            font-size: 0.9rem;
        }

        .success {
            color: #86efac;
            border: 1px solid rgba(34, 197, 94, 0.35);
            background: rgba(34, 197, 94, 0.1);
        }

        .error {
            color: #fca5a5;
            border: 1px solid rgba(239, 68, 68, 0.35);
            background: rgba(239, 68, 68, 0.1);
        }

        .form-group {
            margin-bottom: 1.1rem;
        }

        label {
            display: block;
            margin-bottom: 0.45rem;
            color: #cbd5e1;
            font-size: 0.9rem;
            font-weight: 700;
        }

        .password-wrapper {
            position: relative;
        }

        .form-control {
            width: 100%;
            min-height: 50px;
            padding: 0.9rem 1rem;
            border: 2px solid #334155;
            border-radius: 11px;
            outline: none;
            color: #f8fafc;
            background: #111827;
            font-size: 16px;
        }

        .form-control:focus {
            border-color: #60a5fa;
        }

        .password-wrapper .form-control {
            padding-right: 3rem;
        }

        .toggle-password {
            position: absolute;
            top: 50%;
            right: 0.6rem;
            transform: translateY(-50%);
            border: none;
            color: #cbd5e1;
            background: transparent;
            font-size: 1.1rem;
            cursor: pointer;
        }

        .btn-login {
            width: 100%;
            min-height: 50px;
            margin-top: 0.3rem;
            border: none;
            border-radius: 11px;
            color: white;
            background:
                linear-gradient(
                    135deg,
                    #2563eb,
                    #7c3aed
                );
            font-size: 1rem;
            font-weight: 800;
            cursor: pointer;
        }

        .footer {
            margin-top: 1.4rem;
            color: #64748b;
            text-align: center;
            font-size: 0.75rem;
        }

        @media (max-width: 500px) {
            .login-container {
                padding: 1.5rem;
            }
        }
    </style>

</head>

<body>

<div class="login-container">

    <header class="login-header">

        <div class="logo">
            ☁️
        </div>

        <h1>
            Commerce Cloud
        </h1>

        <p class="subtitle">
            Una plataforma, múltiples negocios
        </p>

        <p class="verticals">
            🏪 Abarrotes · 🏋️ Gimnasios · Más verticales próximamente
        </p>

    </header>

    <div
        th:if="${param.logout}"
        class="message success">
        ✅ Has cerrado sesión correctamente
    </div>

    <div
        th:if="${param.error}"
        class="message error">
        ❌ Usuario o contraseña incorrectos
    </div>

    <div
        th:if="${param.sesionRequerida}"
        class="message error">
        🔒 Debes iniciar sesión
    </div>

    <form
        th:action="@{/login}"
        method="post">

        <div class="form-group">

            <label for="usernameInput">
                👤 Usuario
            </label>

            <input
                id="usernameInput"
                class="form-control"
                type="text"
                name="username"
                placeholder="Ingresa tu usuario"
                autocomplete="username"
                required
                autofocus>

        </div>

        <div class="form-group">

            <label for="passwordInput">
                🔒 Contraseña
            </label>

            <div class="password-wrapper">

                <input
                    id="passwordInput"
                    class="form-control"
                    type="password"
                    name="password"
                    placeholder="Ingresa tu contraseña"
                    autocomplete="current-password"
                    required>

                <button
                    type="button"
                    class="toggle-password"
                    onclick="togglePassword()"
                    aria-label="Mostrar u ocultar contraseña">
                    👁️
                </button>

            </div>

        </div>

        <button
            type="submit"
            class="btn-login">
            Entrar a Commerce Cloud
        </button>

    </form>

    <div class="footer">
        El sistema abrirá automáticamente el módulo de tu empresa.
    </div>

</div>

<script>
    function togglePassword() {
        const input =
            document.getElementById(
                "passwordInput"
            );

        const button =
            document.querySelector(
                ".toggle-password"
            );

        if (input.type === "password") {
            input.type = "text";
            button.textContent = "🙈";
        } else {
            input.type = "password";
            button.textContent = "👁️";
        }
    }
</script>

</body>
</html>
EOF

echo
echo "Habilitando bootstrap de usuario Gym..."

if grep -q \
    '^commerce\.bootstrap\.login-multitenant\.enabled=' \
    "$PROPERTIES"; then

    sed -i \
        's/^commerce\.bootstrap\.login-multitenant\.enabled=.*/commerce.bootstrap.login-multitenant.enabled=true/' \
        "$PROPERTIES"
else
    cat >> "$PROPERTIES" <<'EOF'

# Commerce Cloud - Login multi-tenant
commerce.bootstrap.login-multitenant.enabled=true
EOF
fi

echo
echo "Validando conflictos..."

if grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$JAVA_BASE" \
    "$TEMPLATES" \
    "$MIGRATIONS/V102__agregar_tenant_a_usuario.sql"; then

    echo "ERROR: existen conflictos Git."
    exit 1
fi

git diff --check

echo
echo "Compilando..."

mvn clean compile

echo
echo "============================================================"
echo " LOGIN MULTITENANT INSTALADO"
echo "============================================================"
echo
echo "Respaldo:"
echo "$BACKUP"
echo
echo "Migración:"
echo "$MIGRATIONS/V102__agregar_tenant_a_usuario.sql"
echo
echo "Resultado:"
echo "admin    -> Abarrote Cloud"
echo "admingym -> Gym Cloud"
echo
echo "IMPORTANTE:"
echo "admingym utiliza inicialmente la contraseña de admin."
echo
echo "Estado Git:"
git status --short
