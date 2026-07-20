package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembresiaRepository
    extends JpaRepository<Membresia, Long> {

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    Optional<Membresia> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    List<Membresia>
        findAllByTenantIdAndClienteIdOrderByFechaInicioDesc(
            Long tenantId,
            Long clienteId
        );

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    List<Membresia>
        findAllByTenantIdOrderByFechaCreacionDesc(
            Long tenantId
        );

    List<Membresia>
        findAllByTenantIdAndEstadoAndFechaFinBefore(
            Long tenantId,
            EstadoMembresia estado,
            LocalDate fecha
        );

    @EntityGraph(
        attributePaths = {
            "cliente",
            "plan"
        }
    )
    List<Membresia>
        findAllByTenantIdAndEstadoAndFechaFinBetweenOrderByFechaFinAsc(
            Long tenantId,
            EstadoMembresia estado,
            LocalDate fechaInicial,
            LocalDate fechaFinal
        );

    long countByTenantIdAndEstado(
        Long tenantId,
        EstadoMembresia estado
    );

    long countByTenantIdAndFechaFinBeforeAndEstado(
        Long tenantId,
        LocalDate fecha,
        EstadoMembresia estado
    );
}
