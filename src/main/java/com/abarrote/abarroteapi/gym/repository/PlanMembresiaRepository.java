package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.PlanMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanMembresiaRepository
    extends JpaRepository<PlanMembresia, Long> {

    List<PlanMembresia>
        findAllByTenantIdAndActivoTrueOrderByPrecioAsc(
            Long tenantId
        );

    Optional<PlanMembresia> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    boolean existsByTenantIdAndNombreIgnoreCase(
        Long tenantId,
        String nombre
    );
}
