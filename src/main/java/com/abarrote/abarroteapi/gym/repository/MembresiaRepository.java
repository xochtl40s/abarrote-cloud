package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.EstadoMembresia;
import com.abarrote.abarroteapi.gym.domain.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MembresiaRepository
    extends JpaRepository<Membresia, Long> {

    Optional<Membresia> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    List<Membresia>
        findAllByTenantIdAndClienteIdOrderByFechaInicioDesc(
            Long tenantId,
            Long clienteId
        );

    List<Membresia>
        findAllByTenantIdAndEstadoAndFechaFinBefore(
            Long tenantId,
            EstadoMembresia estado,
            LocalDate fecha
        );
}
