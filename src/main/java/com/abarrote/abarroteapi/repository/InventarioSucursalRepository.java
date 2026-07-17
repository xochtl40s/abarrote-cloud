package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.InventarioSucursal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioSucursalRepository
        extends JpaRepository<InventarioSucursal, Long> {

    List<InventarioSucursal>
    findBySucursalIdOrderByProductoNombreAsc(
            Long sucursalId
    );

    List<InventarioSucursal>
    findByProductoIdOrderBySucursalNombreAsc(
            Long productoId
    );

    Optional<InventarioSucursal>
    findBySucursalIdAndProductoId(
            Long sucursalId,
            Long productoId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT inventario
            FROM InventarioSucursal inventario
            JOIN FETCH inventario.sucursal sucursal
            JOIN FETCH inventario.producto producto
            WHERE sucursal.id = :sucursalId
              AND producto.id = :productoId
            """)
    Optional<InventarioSucursal>
    buscarParaActualizar(
            @Param("sucursalId")
            Long sucursalId,

            @Param("productoId")
            Long productoId
    );

    @Query("""
            SELECT COALESCE(
                SUM(inventario.existencia),
                0
            )
            FROM InventarioSucursal inventario
            WHERE inventario.producto.id = :productoId
            """)
    Long obtenerExistenciaTotalProducto(
            @Param("productoId")
            Long productoId
    );

    @Query("""
            SELECT COALESCE(
                SUM(inventario.existencia),
                0
            )
            FROM InventarioSucursal inventario
            """)
    Long obtenerExistenciaTotalGeneral();
}
