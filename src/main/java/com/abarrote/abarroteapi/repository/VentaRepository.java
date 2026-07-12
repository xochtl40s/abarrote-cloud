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
public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime inicio, LocalDateTime fin);

    List<Venta> findByUsuarioIdAndFechaHoraBetweenOrderByFechaHoraDesc(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.fechaHora BETWEEN :inicio AND :fin AND v.estado = 'COMPLETADA'")
    BigDecimal sumTotalVentasPorFechaHora(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
