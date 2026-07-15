package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(10)
public class InventarioMultisucursalInitializer
        implements ApplicationRunner {

    private final SucursalRepository sucursalRepository;

    private final ProductoRepository productoRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    public InventarioMultisucursalInitializer(
            SucursalRepository sucursalRepository,
            ProductoRepository productoRepository,
            InventarioSucursalRepository inventarioSucursalRepository) {

        this.sucursalRepository = sucursalRepository;
        this.productoRepository = productoRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;
    }

    @Override
    @Transactional
    public void run(
            ApplicationArguments args) {

        Sucursal matriz =
                obtenerOCrearSucursal(
                        "MAT",
                        "Sucursal Matriz",
                        "Sucursal principal"
                );

        Sucursal sucursalNorte =
                obtenerOCrearSucursal(
                        "NOR",
                        "Sucursal Norte",
                        "Sucursal zona norte"
                );

        Sucursal sucursalSur =
                obtenerOCrearSucursal(
                        "SUR",
                        "Sucursal Sur",
                        "Sucursal zona sur"
                );

        List<Producto> productos =
                productoRepository
                        .findByActivoTrueOrderByNombreAsc();

        for (Producto producto : productos) {

            crearInventarioSiNoExiste(
                    matriz,
                    producto,
                    producto.getStock() != null
                            ? producto.getStock()
                            : 0
            );

            crearInventarioSiNoExiste(
                    sucursalNorte,
                    producto,
                    0
            );

            crearInventarioSiNoExiste(
                    sucursalSur,
                    producto,
                    0
            );
        }
    }

    private Sucursal obtenerOCrearSucursal(
            String codigo,
            String nombre,
            String direccion) {

        return sucursalRepository
                .findByCodigoIgnoreCase(codigo)
                .orElseGet(
                        () -> {

                            Sucursal sucursal =
                                    new Sucursal();

                            sucursal.setCodigo(codigo);
                            sucursal.setNombre(nombre);
                            sucursal.setDireccion(direccion);
                            sucursal.setActiva(true);

                            return sucursalRepository.save(
                                    sucursal
                            );
                        }
                );
    }

    private void crearInventarioSiNoExiste(
            Sucursal sucursal,
            Producto producto,
            Integer existenciaInicial) {

        boolean existe =
                inventarioSucursalRepository
                        .findBySucursalIdAndProductoId(
                                sucursal.getId(),
                                producto.getId()
                        )
                        .isPresent();

        if (existe) {
            return;
        }

        InventarioSucursal inventario =
                new InventarioSucursal();

        inventario.setSucursal(sucursal);
        inventario.setProducto(producto);

        inventario.setExistencia(
                existenciaInicial != null
                        ? existenciaInicial
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
    }
}
