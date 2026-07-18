package com.abarrote.abarroteapi.multitenant.repository;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlugIgnoreCase(String slug);

    Optional<Tenant> findBySlugIgnoreCaseAndActivoTrue(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    List<Tenant> findAllByEstadoOrderByNombreAsc(
        EstadoTenant estado
    );

    List<Tenant> findAllByOrderByNombreAsc();
}
