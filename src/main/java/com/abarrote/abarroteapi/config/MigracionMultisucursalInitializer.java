package com.abarrote.abarroteapi.config;

import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.repository.VentaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(20)
public class MigracionMultisucursalInitializer
        implements ApplicationRunner {

    private static final String CODIGO_MATRIZ =
            "MAT";

    private final SucursalRepository sucursalRepository;

    private final UsuarioRepository usuarioRepository;

    private final VentaRepository ventaRepository;

    public MigracionMultisucursalInitializer(
            SucursalRepository sucursalRepository,
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository) {

        this.sucursalRepository = sucursalRepository;
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    @Transactional
    public void run(
            ApplicationArguments args) {

        Sucursal matriz =
                sucursalRepository
                        .findByCodigoIgnoreCase(
                                CODIGO_MATRIZ
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "No existe la sucursal matriz MAT. "
                                                + "Verifica InventarioMultisucursalInitializer."
                                )
                        );

        migrarUsuarios(matriz);
        migrarVentas(matriz);
    }

    private void migrarUsuarios(
            Sucursal matriz) {

        List<Usuario> usuarios =
                usuarioRepository.findAll();

        boolean existenCambios =
                false;

        for (Usuario usuario : usuarios) {

            if (usuario.getSucursal() == null) {

                usuario.setSucursal(matriz);
                existenCambios = true;
            }
        }

        if (existenCambios) {

            usuarioRepository.saveAll(
                    usuarios
            );
        }
    }

    private void migrarVentas(
            Sucursal matriz) {

        List<Venta> ventas =
                ventaRepository.findAll();

        boolean existenCambios =
                false;

        for (Venta venta : ventas) {

            if (venta.getSucursal() == null) {

                Sucursal sucursalUsuario =
                        venta.getUsuario() != null
                                ? venta
                                .getUsuario()
                                .getSucursal()
                                : null;

                venta.setSucursal(
                        sucursalUsuario != null
                                ? sucursalUsuario
                                : matriz
                );

                existenCambios = true;
            }
        }

        if (existenCambios) {

            ventaRepository.saveAll(
                    ventas
            );
        }
    }
}
