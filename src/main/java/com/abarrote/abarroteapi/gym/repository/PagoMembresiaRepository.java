package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.PagoMembresia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoMembresiaRepository
    extends JpaRepository<PagoMembresia, Long> {

    List<PagoMembresia>
        findAllByTenantIdAndMembresiaIdOrderByFechaPagoDesc(
            Long tenantId,
            Long membresiaId
        );
}
