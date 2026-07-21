package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Sucursal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository
        extends JpaRepository<Sucursal, Long> {

    /*
     * Consultas SaaS correctas.
     */

    @EntityGraph(attributePaths = "tenant")
    List<Sucursal>
        findByTenantIdAndActivaTrueOrderByNombreAsc(
            Long tenantId
        );

    @EntityGraph(attributePaths = "tenant")
    List<Sucursal>
        findByTenantIdOrderByNombreAsc(
            Long tenantId
        );

    @EntityGraph(attributePaths = "tenant")
    Optional<Sucursal>
        findByTenantIdAndCodigoIgnoreCase(
            Long tenantId,
            String codigo
        );

    boolean existsByTenantIdAndCodigoIgnoreCase(
        Long tenantId,
        String codigo
    );

    /*
     * Compatibilidad temporal con componentes heredados.
     *
     * No deben utilizarse para nuevas funcionalidades SaaS.
     */

    @EntityGraph(attributePaths = "tenant")
    List<Sucursal> findByActivaTrueOrderByNombreAsc();

    @EntityGraph(attributePaths = "tenant")
    Optional<Sucursal>
        findFirstByCodigoIgnoreCaseOrderByIdAsc(
            String codigo
        );

    default Optional<Sucursal> findByCodigoIgnoreCase(
            String codigo) {

        return findFirstByCodigoIgnoreCaseOrderByIdAsc(
            codigo
        );
    }

    boolean existsByCodigoIgnoreCase(
        String codigo
    );
}
