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

        LOGGER.info(
            "Iniciando bootstrap de login multi-tenant"
        );

        Tenant tenantAbarrotes =
            tenantRepository
                .findBySlugIgnoreCaseAndActivoTrue(
                    "abarrotes-principal"
                )
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe el tenant "
                            + "abarrotes-principal"
                    )
                );

        Tenant tenantGym =
            tenantRepository
                .findBySlugIgnoreCaseAndActivoTrue(
                    "gym-titan"
                )
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe el tenant gym-titan"
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
         * El administrador histórico pertenece al tenant
         * Abarrote Cloud y conserva su sucursal actual.
         */
        admin.setTenant(tenantAbarrotes);
        usuarioRepository.save(admin);

        Usuario adminGym =
            usuarioRepository
                .findByUsernameIgnoreCase("admingym")
                .orElseGet(
                    () -> {
                        Usuario nuevo =
                            new Usuario();

                        nuevo.setUsername("admingym");

                        return nuevo;
                    }
                );

        /*
         * Durante la prueba inicial se reutiliza el hash BCrypt
         * del usuario admin. Por lo tanto, admingym utiliza
         * temporalmente la misma contraseña.
         */
        adminGym.setNombre("Administrador Gym");
        adminGym.setPassword(admin.getPassword());
        adminGym.setRol("ADMIN");
        adminGym.setActivo(true);
        adminGym.setTenant(tenantGym);

        /*
         * Gym Cloud todavía no utiliza las sucursales heredadas
         * de Abarrote Cloud.
         */
        adminGym.setSucursal(null);

        Usuario guardado =
            usuarioRepository.saveAndFlush(adminGym);

        LOGGER.info(
            "Usuario Gym listo: id={}, username={}, "
                + "tenant={}, sucursal=null",
            guardado.getId(),
            guardado.getUsername(),
            tenantGym.getSlug()
        );

        LOGGER.info(
            "Login multi-tenant listo: "
                + "admin -> abarrotes-principal, "
                + "admingym -> gym-titan"
        );
    }
}
