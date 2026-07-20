package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.GymVenta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GymVentaRepository
    extends JpaRepository<GymVenta, Long> {

    @EntityGraph(
        attributePaths = {
            "detalles",
            "detalles.producto"
        }
    )
    List<GymVenta>
        findTop50ByTenantIdOrderByFechaVentaDesc(
            Long tenantId
        );

    long countByTenantId(Long tenantId);
}
