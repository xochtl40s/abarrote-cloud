package com.abarrote.abarroteapi.saas.repository;

import com.abarrote.abarroteapi.saas.entity.SaasProspecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaasProspectoRepository
        extends JpaRepository<SaasProspecto, Long> {

    Optional<SaasProspecto> findByFolioIgnoreCase(String folio);

    boolean existsByFolioIgnoreCase(String folio);

    List<SaasProspecto> findAllByOrderByFechaCreacionDesc();

    List<SaasProspecto> findByEstadoIgnoreCaseOrderByFechaCreacionDesc(
        String estado
    );

    long countByEstadoIgnoreCase(String estado);
}
