package com.abarrote.abarroteapi.saas.service.impl;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.saas.dto.TenantProvisioningResult;
import com.abarrote.abarroteapi.saas.entity.SaasProspecto;
import com.abarrote.abarroteapi.saas.repository.SaasProspectoRepository;
import com.abarrote.abarroteapi.saas.service.TenantProvisioningService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Locale;

@Service
public class TenantProvisioningServiceImpl
        implements TenantProvisioningService {

    private static final String MAYUSCULAS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ";

    private static final String MINUSCULAS =
            "abcdefghijkmnopqrstuvwxyz";

    private static final String NUMEROS =
            "23456789";

    private static final String ESPECIALES =
            "@#$%";

    private static final String TODOS =
            MAYUSCULAS
                    + MINUSCULAS
                    + NUMEROS
                    + ESPECIALES;

    private final SaasProspectoRepository prospectoRepository;
    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final SecureRandom secureRandom;

    public TenantProvisioningServiceImpl(
            SaasProspectoRepository prospectoRepository,
            TenantRepository tenantRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {

        this.prospectoRepository = prospectoRepository;
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.secureRandom = new SecureRandom();
    }

    @Override
    @Transactional
    public TenantProvisioningResult activarProspecto(
            String folio,
            Long atendidoPorUsuarioId) {

        SaasProspecto prospecto =
                prospectoRepository
                        .findByFolioIgnoreCase(folio)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "No existe el prospecto con folio: "
                                                + folio
                                )
                        );

        /*
         * La operación es idempotente.
         *
         * Si el tenant ya existe, no se crea otro.
         * Solo se genera una contraseña temporal nueva.
         */
        if (prospecto.getTenantId() != null) {
            return regenerarCredenciales(
                    prospecto,
                    atendidoPorUsuarioId
            );
        }

        return crearTenantYAdministrador(
                prospecto,
                atendidoPorUsuarioId
        );
    }

    private TenantProvisioningResult crearTenantYAdministrador(
            SaasProspecto prospecto,
            Long atendidoPorUsuarioId) {

        TipoNegocio tipoNegocio =
                convertirTipoNegocio(
                        prospecto.getTipoNegocio()
                );

        String plan =
                obtenerCodigoPlan(
                        prospecto.getPlanSolicitadoId()
                );

        String slug =
                obtenerSlugDisponible(
                        crearSlug(
                                prospecto.getNombreNegocio()
                        )
                );

        Tenant tenant = new Tenant();

        tenant.setNombre(
                prospecto.getNombreNegocio()
        );

        tenant.setSlug(slug);
        tenant.setTipoNegocio(tipoNegocio);
        tenant.setEstado(EstadoTenant.ACTIVO);
        tenant.setPlan(plan);
        tenant.setFechaVencimiento(
                LocalDate.now().plusMonths(1)
        );
        tenant.setActivo(true);

        Tenant tenantGuardado =
                tenantRepository.saveAndFlush(tenant);

        String username =
                obtenerUsernameDisponible(
                        crearUsernameBase(
                                prospecto.getNombreNegocio()
                        )
                );

        String passwordTemporal =
                generarPasswordTemporal();

        Usuario administrador = new Usuario();

        administrador.setNombre(
                prospecto.getPropietario()
        );

        administrador.setUsername(username);

        administrador.setPassword(
                passwordEncoder.encode(passwordTemporal)
        );

        administrador.setRol("ADMIN");
        administrador.setActivo(true);
        administrador.setTenant(tenantGuardado);
        administrador.setSucursal(null);

        usuarioRepository.saveAndFlush(administrador);

        prospecto.setTenantId(
                tenantGuardado.getId()
        );

        prospecto.setEstado("ACTIVADO");

        prospecto.setAtendidoPorUsuarioId(
                atendidoPorUsuarioId
        );

        prospectoRepository.saveAndFlush(prospecto);

        return construirResultado(
                prospecto,
                tenantGuardado,
                administrador,
                passwordTemporal,
                false
        );
    }

    private TenantProvisioningResult regenerarCredenciales(
            SaasProspecto prospecto,
            Long atendidoPorUsuarioId) {

        Tenant tenant =
                tenantRepository
                        .findById(prospecto.getTenantId())
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "El prospecto está activado, "
                                                + "pero su tenant no existe."
                                )
                        );

        Usuario administrador =
                usuarioRepository
                        .findFirstByTenantIdAndRolIgnoreCaseOrderByIdAsc(
                                tenant.getId(),
                                "ADMIN"
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "El tenant no tiene un usuario "
                                                + "administrador."
                                )
                        );

        String passwordTemporal =
                generarPasswordTemporal();

        administrador.setPassword(
                passwordEncoder.encode(passwordTemporal)
        );

        administrador.setActivo(true);

        usuarioRepository.saveAndFlush(administrador);

        prospecto.setEstado("ACTIVADO");

        prospecto.setAtendidoPorUsuarioId(
                atendidoPorUsuarioId
        );

        prospectoRepository.saveAndFlush(prospecto);

        return construirResultado(
                prospecto,
                tenant,
                administrador,
                passwordTemporal,
                true
        );
    }

    private TenantProvisioningResult construirResultado(
            SaasProspecto prospecto,
            Tenant tenant,
            Usuario administrador,
            String passwordTemporal,
            boolean regeneradas) {

        return new TenantProvisioningResult(
                tenant.getId(),
                tenant.getNombre(),
                tenant.getSlug(),
                tenant.getTipoNegocio().name(),
                tenant.getPlan(),
                administrador.getUsername(),
                passwordTemporal,
                prospecto.getPropietario(),
                prospecto.getFolio(),
                regeneradas
        );
    }

    private TipoNegocio convertirTipoNegocio(
            String valor) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    "El prospecto no tiene tipo de negocio."
            );
        }

        try {
            return TipoNegocio.valueOf(
                    valor.trim().toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException exception) {

            throw new IllegalArgumentException(
                    "Tipo de negocio no soportado: "
                            + valor
            );
        }
    }

    private String obtenerCodigoPlan(
            Long planId) {

        if (planId == null) {
            return "BASICO";
        }

        return jdbcTemplate.query(
                """
                SELECT codigo
                FROM saas_plan
                WHERE id = ?
                  AND activo = TRUE
                """,
                resultSet -> {

                    if (resultSet.next()) {
                        return resultSet.getString("codigo");
                    }

                    return "BASICO";
                },
                planId
        );
    }

    private String obtenerSlugDisponible(
            String base) {

        String candidato = base;
        int consecutivo = 2;

        while (tenantRepository
                .existsBySlugIgnoreCase(candidato)) {

            String sufijo =
                    "-" + consecutivo;

            candidato =
                    recortar(
                            base,
                            80 - sufijo.length()
                    )
                            + sufijo;

            consecutivo++;
        }

        return candidato;
    }

    private String obtenerUsernameDisponible(
            String base) {

        String candidato = base;
        int consecutivo = 2;

        while (usuarioRepository
                .findByUsernameIgnoreCase(candidato)
                .isPresent()) {

            String sufijo =
                    String.valueOf(consecutivo);

            candidato =
                    recortar(
                            base,
                            20 - sufijo.length()
                    )
                            + sufijo;

            consecutivo++;
        }

        return candidato;
    }

    private String crearSlug(
            String nombreNegocio) {

        String slug =
                normalizarTexto(nombreNegocio)
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]+", "-")
                        .replaceAll("-{2,}", "-")
                        .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            slug = "empresa";
        }

        return recortar(slug, 80);
    }

    private String crearUsernameBase(
            String nombreNegocio) {

        String limpio =
                normalizarTexto(nombreNegocio)
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]", "");

        if (limpio.isBlank()) {
            limpio = "empresa";
        }

        return recortar(
                "admin." + limpio,
                20
        );
    }

    private String normalizarTexto(
            String valor) {

        if (valor == null) {
            return "";
        }

        return Normalizer
                .normalize(
                        valor.trim(),
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}", "");
    }

    private String generarPasswordTemporal() {

        StringBuilder password =
                new StringBuilder();

        password.append(
                caracterAleatorio(MAYUSCULAS)
        );

        password.append(
                caracterAleatorio(MINUSCULAS)
        );

        password.append(
                caracterAleatorio(NUMEROS)
        );

        password.append(
                caracterAleatorio(ESPECIALES)
        );

        while (password.length() < 12) {
            password.append(
                    caracterAleatorio(TODOS)
            );
        }

        return mezclar(password.toString());
    }

    private char caracterAleatorio(
            String caracteres) {

        return caracteres.charAt(
                secureRandom.nextInt(
                        caracteres.length()
                )
        );
    }

    private String mezclar(
            String valor) {

        char[] caracteres =
                valor.toCharArray();

        for (int i = caracteres.length - 1;
             i > 0;
             i--) {

            int posicion =
                    secureRandom.nextInt(i + 1);

            char temporal =
                    caracteres[i];

            caracteres[i] =
                    caracteres[posicion];

            caracteres[posicion] =
                    temporal;
        }

        return new String(caracteres);
    }

    private String recortar(
            String valor,
            int longitudMaxima) {

        if (valor.length() <= longitudMaxima) {
            return valor;
        }

        return valor.substring(
                0,
                longitudMaxima
        );
    }
}
