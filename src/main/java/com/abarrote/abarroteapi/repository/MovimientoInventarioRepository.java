package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    Optional<MovimientoInventario> findByFolio(
            String folio
    );

    List<MovimientoInventario>
    findAllByOrderByFechaMovimientoDesc();

    List<MovimientoInventario>
    findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );

    List<MovimientoInventario>
    findBySucursalOrigenIdOrSucursalDestinoIdOrderByFechaMovimientoDesc(
            Long sucursalOrigenId,
            Long sucursalDestinoId
    );

    List<MovimientoInventario>
    findByProductoIdOrderByFechaMovimientoDesc(
            Long productoId
    );
}
