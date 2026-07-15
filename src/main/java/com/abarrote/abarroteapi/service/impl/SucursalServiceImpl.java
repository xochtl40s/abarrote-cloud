package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.MovimientoInventario;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.MovimientoInventarioRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.service.SucursalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class SucursalServiceImpl
        implements SucursalService {

    private static final String CODIGO_MATRIZ = "MAT";

    private final SucursalRepository sucursalRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    private final MovimientoInventarioRepository
            movimientoInventarioRepository;

    private final UsuarioRepository usuarioRepository;

    public SucursalServiceImpl(
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository inventarioSucursalRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            UsuarioRepository usuarioRepository) {

        this.sucursalRepository = sucursalRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;

        this.movimientoInventarioRepository =
                movimientoInventarioRepository;

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sucursal> listarTodas() {

        /*
         * Solo se muestran sucursales operativas.
         * Las cerradas permanecen en base de datos
         * únicamente para conservar evidencia histórica.
         */
        return sucursalRepository
                .findByActivaTrueOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sucursal> listarActivas() {

        return sucursalRepository
                .findByActivaTrueOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Sucursal obtenerPorId(
            Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "El identificador de la sucursal es obligatorio"
            );
        }

        return sucursalRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "La sucursal solicitada no existe"
                        )
                );
    }

    @Override
    @Transactional
    public Sucursal crear(
            Sucursal sucursal) {

        validarSucursal(
                sucursal,
                null
        );

        normalizar(sucursal);

        sucursal.setId(null);
        sucursal.setActiva(true);

        /*
         * Una sucursal nueva no recibe productos
         * automáticamente.
         */
        return sucursalRepository.save(
                sucursal
        );
    }

    @Override
    @Transactional
    public Sucursal actualizar(
            Long id,
            Sucursal sucursal) {

        Sucursal existente =
                obtenerPorId(id);

        if (!Boolean.TRUE.equals(
                existente.getActiva())) {

            throw new IllegalStateException(
                    "No se puede modificar una sucursal cerrada"
            );
        }

        validarSucursal(
                sucursal,
                id
        );

        existente.setCodigo(
                sucursal.getCodigo()
        );

        existente.setNombre(
                sucursal.getNombre()
        );

        existente.setDireccion(
                sucursal.getDireccion()
        );

        existente.setTelefono(
                sucursal.getTelefono()
        );

        normalizar(existente);

        return sucursalRepository.save(
                existente
        );
    }

    @Override
    @Transactional
    public void cerrarSucursal(
            Long id,
            String usuarioResponsable) {

        Sucursal sucursalOrigen =
                obtenerPorId(id);

        if (!Boolean.TRUE.equals(
                sucursalOrigen.getActiva())) {

            throw new IllegalStateException(
                    "La sucursal ya se encuentra cerrada"
            );
        }

        if (CODIGO_MATRIZ.equalsIgnoreCase(
                sucursalOrigen.getCodigo())) {

            throw new IllegalStateException(
                    "La Sucursal Matriz no puede darse de baja"
            );
        }

        Sucursal sucursalMatriz =
                sucursalRepository
                        .findByCodigoIgnoreCase(
                                CODIGO_MATRIZ
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "No existe la Sucursal Matriz"
                                )
                        );

        if (!Boolean.TRUE.equals(
                sucursalMatriz.getActiva())) {

            throw new IllegalStateException(
                    "La Sucursal Matriz se encuentra inactiva"
            );
        }

        transferirInventarioAMatriz(
                sucursalOrigen,
                sucursalMatriz,
                normalizarUsuario(
                        usuarioResponsable
                )
        );

        reasignarUsuariosAMatriz(
                sucursalOrigen,
                sucursalMatriz
        );

        /*
         * Los registros de inventario de la sucursal cerrada
         * ya quedaron en cero y pueden eliminarse.
         */
        List<InventarioSucursal> inventariosOrigen =
                inventarioSucursalRepository
                        .findBySucursalIdOrderByProductoNombreAsc(
                                sucursalOrigen.getId()
                        );

        inventarioSucursalRepository.deleteAll(
                inventariosOrigen
        );

        /*
         * Baja lógica definitiva.
         * No se borra físicamente porque ventas y movimientos
         * históricos necesitan conservar la sucursal original.
         */
        sucursalOrigen.setActiva(false);

        sucursalRepository.save(
                sucursalOrigen
        );
    }

    private void transferirInventarioAMatriz(
            Sucursal sucursalOrigen,
            Sucursal sucursalMatriz,
            String usuarioResponsable) {

        List<InventarioSucursal> inventariosOrigen =
                inventarioSucursalRepository
                        .findBySucursalIdOrderByProductoNombreAsc(
                                sucursalOrigen.getId()
                        );

        for (InventarioSucursal inventarioOrigen
                : inventariosOrigen) {

            int cantidad =
                    valorSeguro(
                            inventarioOrigen.getExistencia()
                    );

            if (cantidad <= 0) {
                continue;
            }

            InventarioSucursal inventarioMatriz =
                    inventarioSucursalRepository
                            .findBySucursalIdAndProductoId(
                                    sucursalMatriz.getId(),
                                    inventarioOrigen
                                            .getProducto()
                                            .getId()
                            )
                            .orElseGet(
                                    () -> crearInventarioMatriz(
                                            sucursalMatriz,
                                            inventarioOrigen
                                    )
                            );

            int existenciaOrigenAnterior =
                    valorSeguro(
                            inventarioOrigen.getExistencia()
                    );

            int existenciaMatrizAnterior =
                    valorSeguro(
                            inventarioMatriz.getExistencia()
                    );

            inventarioOrigen.disminuirExistencia(
                    cantidad
            );

            inventarioMatriz.aumentarExistencia(
                    cantidad
            );

            inventarioSucursalRepository.save(
                    inventarioOrigen
            );

            inventarioSucursalRepository.save(
                    inventarioMatriz
            );

            registrarMovimientoCierre(
                    inventarioOrigen,
                    inventarioMatriz,
                    cantidad,
                    existenciaOrigenAnterior,
                    existenciaMatrizAnterior,
                    usuarioResponsable
            );
        }
    }

    private InventarioSucursal crearInventarioMatriz(
            Sucursal sucursalMatriz,
            InventarioSucursal inventarioOrigen) {

        InventarioSucursal inventarioMatriz =
                new InventarioSucursal();

        inventarioMatriz.setSucursal(
                sucursalMatriz
        );

        inventarioMatriz.setProducto(
                inventarioOrigen.getProducto()
        );

        inventarioMatriz.setExistencia(0);

        inventarioMatriz.setStockMinimo(
                inventarioOrigen.getStockMinimo() != null
                        ? inventarioOrigen.getStockMinimo()
                        : 5
        );

        return inventarioSucursalRepository.save(
                inventarioMatriz
        );
    }

    private void registrarMovimientoCierre(
            InventarioSucursal inventarioOrigen,
            InventarioSucursal inventarioMatriz,
            Integer cantidad,
            Integer existenciaOrigenAnterior,
            Integer existenciaMatrizAnterior,
            String usuarioResponsable) {

        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setProducto(
                inventarioOrigen.getProducto()
        );

        movimiento.setSucursalOrigen(
                inventarioOrigen.getSucursal()
        );

        movimiento.setSucursalDestino(
                inventarioMatriz.getSucursal()
        );

        movimiento.setCantidad(
                cantidad
        );

        movimiento.setExistenciaOrigenAnterior(
                existenciaOrigenAnterior
        );

        movimiento.setExistenciaOrigenNueva(
                inventarioOrigen.getExistencia()
        );

        movimiento.setExistenciaDestinoAnterior(
                existenciaMatrizAnterior
        );

        movimiento.setExistenciaDestinoNueva(
                inventarioMatriz.getExistencia()
        );

        movimiento.setUsuarioResponsable(
                usuarioResponsable
        );

        movimiento.setMotivo(
                "Cierre definitivo de sucursal. "
                        + "Inventario trasladado automáticamente a Matriz."
        );

        movimiento.setEstado(
                MovimientoInventario
                        .EstadoMovimiento
                        .APLICADO
        );

        movimientoInventarioRepository.save(
                movimiento
        );
    }

    private void reasignarUsuariosAMatriz(
            Sucursal sucursalOrigen,
            Sucursal sucursalMatriz) {

        List<Usuario> usuarios =
                usuarioRepository.findAll();

        for (Usuario usuario : usuarios) {

            if (usuario.getSucursal() == null
                    || usuario.getSucursal().getId() == null) {

                continue;
            }

            if (!usuario
                    .getSucursal()
                    .getId()
                    .equals(
                            sucursalOrigen.getId()
                    )) {

                continue;
            }

            usuario.setSucursal(
                    sucursalMatriz
            );

            usuarioRepository.save(
                    usuario
            );
        }
    }

    private void validarSucursal(
            Sucursal sucursal,
            Long sucursalIdActual) {

        if (sucursal == null) {

            throw new IllegalArgumentException(
                    "La información de la sucursal es obligatoria"
            );
        }

        if (sucursal.getCodigo() == null
                || sucursal.getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                    "El código de la sucursal es obligatorio"
            );
        }

        if (sucursal.getNombre() == null
                || sucursal.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de la sucursal es obligatorio"
            );
        }

        String codigo =
                sucursal
                        .getCodigo()
                        .trim()
                        .toUpperCase();

        if (codigo.length() > 20) {

            throw new IllegalArgumentException(
                    "El código no puede exceder 20 caracteres"
            );
        }

        if (sucursal.getNombre().trim().length() > 120) {

            throw new IllegalArgumentException(
                    "El nombre no puede exceder 120 caracteres"
            );
        }

        if (sucursal.getDireccion() != null
                && sucursal
                .getDireccion()
                .trim()
                .length() > 250) {

            throw new IllegalArgumentException(
                    "La dirección no puede exceder 250 caracteres"
            );
        }

        if (sucursal.getTelefono() != null
                && sucursal
                .getTelefono()
                .trim()
                .length() > 20) {

            throw new IllegalArgumentException(
                    "El teléfono no puede exceder 20 caracteres"
            );
        }

        sucursalRepository
                .findByCodigoIgnoreCase(codigo)
                .ifPresent(
                        encontrada -> {

                            boolean correspondeAOtraSucursal =
                                    sucursalIdActual == null
                                            || !encontrada
                                            .getId()
                                            .equals(
                                                    sucursalIdActual
                                            );

                            if (correspondeAOtraSucursal) {

                                throw new IllegalArgumentException(
                                        "Ya existe una sucursal con el código "
                                                + codigo
                                );
                            }
                        }
                );
    }

    private void normalizar(
            Sucursal sucursal) {

        sucursal.setCodigo(
                sucursal
                        .getCodigo()
                        .trim()
                        .toUpperCase()
        );

        sucursal.setNombre(
                sucursal
                        .getNombre()
                        .trim()
        );

        sucursal.setDireccion(
                limpiarTexto(
                        sucursal.getDireccion()
                )
        );

        sucursal.setTelefono(
                limpiarTexto(
                        sucursal.getTelefono()
                )
        );
    }

    private String limpiarTexto(
            String valor) {

        if (valor == null
                || valor.isBlank()) {

            return null;
        }

        return valor.trim();
    }

    private String normalizarUsuario(
            String usuarioResponsable) {

        if (usuarioResponsable == null
                || usuarioResponsable.isBlank()) {

            return "usuario-desconocido";
        }

        String usuario =
                usuarioResponsable.trim();

        return usuario.length() <= 100
                ? usuario
                : usuario.substring(0, 100);
    }

    private int valorSeguro(
            Integer valor) {

        return valor != null
                ? valor
                : 0;
    }
}
