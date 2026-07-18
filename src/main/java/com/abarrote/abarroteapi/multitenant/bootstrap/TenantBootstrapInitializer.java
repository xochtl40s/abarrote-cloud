package com.abarrote.abarroteapi.multitenant.bootstrap;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Crea tenants iniciales únicamente cuando:
 *
 * commerce.bootstrap.tenants.enabled=true
 *
 * No expone endpoints públicos y no duplica registros porque valida el slug.
 */
@Component
@ConditionalOnProperty(
    name = "commerce.bootstrap.tenants.enabled",
    havingValue = "true"
)
public class TenantBootstrapInitializer
    implements ApplicationRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            TenantBootstrapInitializer.class
        );

    private final TenantRepository tenantRepository;

    public TenantBootstrapInitializer(
        TenantRepository tenantRepository
    ) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LOGGER.info(
            "Iniciando bootstrap controlado de tenants"
        );

        crearTenantSiNoExiste(
            "Gym Titan Ecatepec",
            "gym-titan",
            TipoNegocio.GYM,
            "PROFESIONAL",
            LocalDate.of(2027, 7, 18)
        );

        crearTenantSiNoExiste(
            "Abarrotes Cloud Principal",
            "abarrotes-principal",
            TipoNegocio.ABARROTES,
            "PROFESIONAL",
            LocalDate.of(2027, 7, 18)
        );

        LOGGER.info(
            "Bootstrap de tenants terminado"
        );
    }

    private void crearTenantSiNoExiste(
        String nombre,
        String slug,
        TipoNegocio tipoNegocio,
        String plan,
        LocalDate fechaVencimiento
    ) {
        if (tenantRepository.existsBySlugIgnoreCase(slug)) {
            LOGGER.info(
                "Tenant existente; no se duplica: {}",
                slug
            );
            return;
        }

        Tenant tenant = new Tenant();
        tenant.setNombre(nombre);
        tenant.setSlug(slug);
        tenant.setTipoNegocio(tipoNegocio);
        tenant.setEstado(EstadoTenant.ACTIVO);
        tenant.setPlan(plan);
        tenant.setFechaVencimiento(fechaVencimiento);
        tenant.setActivo(true);

        Tenant guardado = tenantRepository.save(tenant);

        LOGGER.info(
            "Tenant creado. id={}, slug={}, tipo={}",
            guardado.getId(),
            guardado.getSlug(),
            guardado.getTipoNegocio()
        );
    }
}
