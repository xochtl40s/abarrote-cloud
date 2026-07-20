package com.abarrote.abarroteapi.gym.repository;

import com.abarrote.abarroteapi.gym.domain.GymProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GymProductoRepository
    extends JpaRepository<GymProducto, Long> {

    List<GymProducto>
        findAllByTenantIdAndActivoTrueOrderByNombreAsc(
            Long tenantId
        );

    Optional<GymProducto> findByIdAndTenantId(
        Long id,
        Long tenantId
    );

    boolean existsByTenantIdAndCodigoIgnoreCase(
        Long tenantId,
        String codigo
    );

    long countByTenantIdAndActivoTrue(
        Long tenantId
    );

    long countByTenantIdAndActivoTrueAndExistenciaLessThanEqual(
        Long tenantId,
        Integer existencia
    );

    @Query("""
        SELECT p
          FROM GymProducto p
         WHERE p.tenant.id = :tenantId
           AND p.activo = true
           AND p.existencia <= p.stockMinimo
         ORDER BY p.existencia ASC, p.nombre ASC
        """)
    List<GymProducto> buscarProductosStockBajo(
        @Param("tenantId")
        Long tenantId
    );
}
