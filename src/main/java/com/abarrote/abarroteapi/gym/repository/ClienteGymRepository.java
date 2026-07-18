package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.ClienteGym;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteGymRepository
    extends JpaRepository<ClienteGym, Long> {

    List<ClienteGym> findAllByTenantIdOrderByNombreAsc(
        Long tenantId
    );

    Optional<ClienteGym> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    Optional<ClienteGym>
        findByTenantIdAndNumeroClienteIgnoreCase(
            Long tenantId,
            String numeroCliente
        );

    boolean existsByTenantIdAndNumeroClienteIgnoreCase(
        Long tenantId,
        String numeroCliente
    );
}
