package com.abarrote.abarroteapi.repository;

import com.abarrote.abarroteapi.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    Optional<Producto> findByCodigoBarras(
            String codigoBarras
    );

    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(
            String nombre
    );

    List<Producto> findByActivoTrueOrderByNombreAsc();

    boolean existsByCodigoBarras(
            String codigoBarras
    );

    @Query("""
            SELECT p
            FROM Producto p
            WHERE p.activo = true
              AND p.stock <= p.stockMinimo
            ORDER BY p.stock ASC, p.nombre ASC
            """)
    List<Producto> encontrarProductosConStockBajo();

    @Query("""
            SELECT p
            FROM Producto p
            WHERE p.activo = true
              AND p.stock <= 0
            ORDER BY p.nombre ASC
            """)
    List<Producto> encontrarProductosAgotados();

    @Query("""
            SELECT COUNT(p)
            FROM Producto p
            WHERE p.activo = true
              AND p.stock <= p.stockMinimo
            """)
    long contarProductosConStockBajo();

    @Query("""
            SELECT COUNT(p)
            FROM Producto p
            WHERE p.activo = true
              AND p.stock <= 0
            """)
    long contarProductosAgotados();
}
