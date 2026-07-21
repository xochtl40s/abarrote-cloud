package com.abarrote.abarroteapi.saas.repository;

import com.abarrote.abarroteapi.saas.entity.SaasProspecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaasProspectoRepository
        extends JpaRepository<SaasProspecto, Long> {

    Optional<SaasProspecto> findByFolioIgnoreCase(String folio);

    boolean existsByFolioIgnoreCase(String folio);

    List<SaasProspecto> findAllByOrderByFechaCreacionDesc();

    /**
     * Los registros eliminados lógicamente no aparecen
     * en el portal comercial.
     */
    @Query(
        """
        SELECT p
        FROM SaasProspecto p
        WHERE UPPER(p.estado) <> 'ELIMINADO'
        ORDER BY p.fechaCreacion DESC
        """
    )
    List<SaasProspecto> findVisiblesOrderByFechaCreacionDesc();


    List<SaasProspecto> findByEstadoIgnoreCaseOrderByFechaCreacionDesc(
        String estado
    );

    long countByEstadoIgnoreCase(String estado);
}
