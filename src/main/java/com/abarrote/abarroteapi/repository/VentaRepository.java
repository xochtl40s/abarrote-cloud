package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository
        extends JpaRepository<Venta, Long> {

    List<Venta>
    findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Venta>
    findByUsuarioIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Venta>
    findBySucursalIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long sucursalId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<Venta>
    findBySucursalIdAndUsuarioIdAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long sucursalId,
            Long usuarioId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Query("""
            SELECT COALESCE(SUM(venta.total), 0)
            FROM Venta venta
            WHERE venta.fechaHora BETWEEN :inicio AND :fin
              AND venta.estado =
                  com.abarrote.abarroteapi.entity.Venta.EstadoVenta.COMPLETADA
            """)
    BigDecimal sumTotalVentasPorFechaHora(
            @Param("inicio")
            LocalDateTime inicio,

            @Param("fin")
            LocalDateTime fin
    );

    @Query("""
            SELECT COALESCE(SUM(venta.total), 0)
            FROM Venta venta
            WHERE venta.sucursal.id = :sucursalId
              AND venta.fechaHora BETWEEN :inicio AND :fin
              AND venta.estado =
                  com.abarrote.abarroteapi.entity.Venta.EstadoVenta.COMPLETADA
            """)
    BigDecimal sumTotalVentasPorSucursalYFechaHora(
            @Param("sucursalId")
            Long sucursalId,

            @Param("inicio")
            LocalDateTime inicio,

            @Param("fin")
            LocalDateTime fin
    );

    @Query("""
            SELECT DISTINCT venta
            FROM Venta venta
            LEFT JOIN FETCH venta.usuario usuario
            LEFT JOIN FETCH venta.sucursal sucursal
            LEFT JOIN FETCH venta.detalles detalle
            LEFT JOIN FETCH detalle.producto producto
            WHERE venta.fechaHora BETWEEN :inicio AND :fin
            ORDER BY venta.fechaHora DESC
            """)
    List<Venta> buscarCorteConDetalle(
            @Param("inicio")
            LocalDateTime inicio,

            @Param("fin")
            LocalDateTime fin
    );

}
