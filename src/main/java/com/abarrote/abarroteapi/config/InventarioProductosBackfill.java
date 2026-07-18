package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(30)
public class InventarioProductosBackfill
        implements CommandLineRunner {

    private static final String CODIGO_MATRIZ =
            "MAT";

    private final ProductoRepository productoRepository;

    private final SucursalRepository sucursalRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    public InventarioProductosBackfill(
            ProductoRepository productoRepository,
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository
                    inventarioSucursalRepository) {

        this.productoRepository =
                productoRepository;

        this.sucursalRepository =
                sucursalRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;
    }

    @Override
    @Transactional
    public void run(
            String... args) {

        Sucursal matriz =
                obtenerMatriz();

        if (matriz == null) {

            System.out.println(
                    "[INVENTARIO-BACKFILL] "
                            + "No existe sucursal MAT activa. "
                            + "No se realizó reparación."
            );

            return;
        }

        List<Producto> productos =
                productoRepository
                        .findByActivoTrueOrderByNombreAsc();

        int reparados =
                0;

        for (Producto producto : productos) {

            boolean existe =
                    inventarioSucursalRepository
                            .findBySucursalIdAndProductoId(
                                    matriz.getId(),
                                    producto.getId()
                            )
                            .isPresent();

            if (existe) {
                continue;
            }

            InventarioSucursal inventario =
                    new InventarioSucursal();

            inventario.setSucursal(
                    matriz
            );

            inventario.setProducto(
                    producto
            );

            inventario.setExistencia(
                    producto.getStock() != null
                            ? producto.getStock()
                            : 0
            );

            inventario.setStockMinimo(
                    producto.getStockMinimo() != null
                            ? producto.getStockMinimo()
                            : 5
            );

            inventarioSucursalRepository.save(
                    inventario
            );

            reparados++;
        }

        inventarioSucursalRepository.flush();

        System.out.println(
                "[INVENTARIO-BACKFILL] "
                        + "Productos reparados en MAT: "
                        + reparados
        );
    }

    private Sucursal obtenerMatriz() {

        Sucursal matriz =
                sucursalRepository
                        .findByCodigoIgnoreCase(
                                CODIGO_MATRIZ
                        )
                        .orElse(
                                null
                        );

        if (matriz != null
                && Boolean.TRUE.equals(
                        matriz.getActiva()
                )) {

            return matriz;
        }

        List<Sucursal> activas =
                sucursalRepository
                        .findByActivaTrueOrderByNombreAsc();

        return activas.size() == 1
                ? activas.get(0)
                : null;
    }
}
