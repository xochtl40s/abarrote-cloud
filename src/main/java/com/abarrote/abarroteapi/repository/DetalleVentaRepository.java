package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.DetalleVenta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Long> {

    @Query("""
            SELECT
                d.producto.id,
                d.producto.nombre,
                SUM(d.cantidad),
                SUM(d.subtotal)
            FROM DetalleVenta d
            WHERE d.venta.estado =
                  com.abarrote.abarroteapi.entity.Venta.EstadoVenta.COMPLETADA
              AND d.venta.fechaHora BETWEEN :inicio AND :fin
            GROUP BY
                d.producto.id,
                d.producto.nombre
            ORDER BY SUM(d.cantidad) DESC
            """)
    List<Object[]> buscarProductosMasVendidos(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );
}
