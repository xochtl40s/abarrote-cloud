package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.InventarioSucursalResponse;
import com.abarrote.abarroteapi.dto.MovimientoInventarioResponse;
import com.abarrote.abarroteapi.dto.TransferenciaInventarioRequest;
import com.abarrote.abarroteapi.entity.InventarioSucursal;
import com.abarrote.abarroteapi.entity.MovimientoInventario;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Sucursal;
import com.abarrote.abarroteapi.repository.InventarioSucursalRepository;
import com.abarrote.abarroteapi.repository.MovimientoInventarioRepository;
import com.abarrote.abarroteapi.repository.SucursalRepository;
import com.abarrote.abarroteapi.service.TransferenciaInventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TransferenciaInventarioServiceImpl
        implements TransferenciaInventarioService {

    private final SucursalRepository sucursalRepository;

    private final InventarioSucursalRepository
            inventarioSucursalRepository;

    private final MovimientoInventarioRepository
            movimientoInventarioRepository;

    public TransferenciaInventarioServiceImpl(
            SucursalRepository sucursalRepository,
            InventarioSucursalRepository inventarioSucursalRepository,
            MovimientoInventarioRepository movimientoInventarioRepository) {

        this.sucursalRepository = sucursalRepository;

        this.inventarioSucursalRepository =
                inventarioSucursalRepository;

        this.movimientoInventarioRepository =
                movimientoInventarioRepository;
    }

    @Override
    @Transactional
    public MovimientoInventarioResponse transferir(
            TransferenciaInventarioRequest request,
            String usuarioResponsable) {

        validarSolicitud(request);

        Sucursal sucursalOrigen =
                obtenerSucursalActiva(
                        request.getSucursalOrigenId(),
                        "origen"
                );

        Sucursal sucursalDestino =
                obtenerSucursalActiva(
                        request.getSucursalDestinoId(),
                        "destino"
                );

        InventarioSucursal inventarioOrigenExistente =
                inventarioSucursalRepository
                        .findBySucursalIdAndProductoId(
                                sucursalOrigen.getId(),
                                request.getProductoId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "El producto seleccionado no tiene "
                                                + "existencia registrada en "
                                                + sucursalOrigen.getNombre()
                                )
                        );

        Producto producto =
                inventarioOrigenExistente.getProducto();

        /*
         * La sucursal de destino puede no tener todavía
         * el producto. En ese caso se crea su registro
         * con existencia inicial cero.
         */
        crearInventarioDestinoSiNoExiste(
                sucursalDestino,
                producto,
                inventarioOrigenExistente.getStockMinimo()
        );

        Long primerSucursalId =
                Math.min(
                        sucursalOrigen.getId(),
                        sucursalDestino.getId()
                );

        Long segundaSucursalId =
                Math.max(
                        sucursalOrigen.getId(),
                        sucursalDestino.getId()
                );

        InventarioSucursal primerInventario =
                obtenerInventarioBloqueado(
                        primerSucursalId,
                        producto.getId()
                );

        InventarioSucursal segundoInventario =
                obtenerInventarioBloqueado(
                        segundaSucursalId,
                        producto.getId()
                );

        InventarioSucursal inventarioOrigen;

        InventarioSucursal inventarioDestino;

        if (primerInventario
                .getSucursal()
                .getId()
                .equals(sucursalOrigen.getId())) {

            inventarioOrigen = primerInventario;
            inventarioDestino = segundoInventario;

        } else {

            inventarioOrigen = segundoInventario;
            inventarioDestino = primerInventario;
        }

        int existenciaOrigenAnterior =
                valorSeguro(
                        inventarioOrigen.getExistencia()
                );

        int existenciaDestinoAnterior =
                valorSeguro(
                        inventarioDestino.getExistencia()
                );

        if (existenciaOrigenAnterior
                < request.getCantidad()) {

            throw new IllegalArgumentException(
                    "Existencia insuficiente en "
                            + sucursalOrigen.getNombre()
                            + ". Disponible: "
                            + existenciaOrigenAnterior
                            + ", solicitado: "
                            + request.getCantidad()
            );
        }

        inventarioOrigen.disminuirExistencia(
                request.getCantidad()
        );

        inventarioDestino.aumentarExistencia(
                request.getCantidad()
        );

        inventarioSucursalRepository.save(
                inventarioOrigen
        );

        inventarioSucursalRepository.save(
                inventarioDestino
        );

        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setProducto(producto);
        movimiento.setSucursalOrigen(sucursalOrigen);
        movimiento.setSucursalDestino(sucursalDestino);

        movimiento.setCantidad(
                request.getCantidad()
        );

        movimiento.setExistenciaOrigenAnterior(
                existenciaOrigenAnterior
        );

        movimiento.setExistenciaOrigenNueva(
                inventarioOrigen.getExistencia()
        );

        movimiento.setExistenciaDestinoAnterior(
                existenciaDestinoAnterior
        );

        movimiento.setExistenciaDestinoNueva(
                inventarioDestino.getExistencia()
        );

        movimiento.setUsuarioResponsable(
                normalizarUsuario(
                        usuarioResponsable
                )
        );

        movimiento.setMotivo(
                construirMotivo(request)
        );

        movimiento.setEstado(
                MovimientoInventario
                        .EstadoMovimiento
                        .APLICADO
        );

        MovimientoInventario movimientoGuardado =
                movimientoInventarioRepository.save(
                        movimiento
                );

        return convertirMovimiento(
                movimientoGuardado
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sucursal> listarSucursalesActivas() {

        return sucursalRepository
                .findByActivaTrueOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventarioSucursalResponse>
    listarInventarioCompleto() {

        List<InventarioSucursal> inventarios =
                inventarioSucursalRepository.findAll();

        inventarios.sort(
                Comparator
                        .comparing(
                                (InventarioSucursal inventario) ->
                                        inventario
                                                .getSucursal()
                                                .getNombre(),
                                String.CASE_INSENSITIVE_ORDER
                        )
                        .thenComparing(
                                (InventarioSucursal inventario) ->
                                        inventario
                                                .getProducto()
                                                .getNombre(),
                                String.CASE_INSENSITIVE_ORDER
                        )
        );

        List<InventarioSucursalResponse> respuesta =
                new ArrayList<>();

        for (InventarioSucursal inventario
                : inventarios) {

            respuesta.add(
                    convertirInventario(inventario)
            );
        }

        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse>
    listarMovimientos() {

        List<MovimientoInventarioResponse> respuesta =
                new ArrayList<>();

        for (MovimientoInventario movimiento
                : movimientoInventarioRepository
                .findAllByOrderByFechaMovimientoDesc()) {

            respuesta.add(
                    convertirMovimiento(movimiento)
            );
        }

        return respuesta;
    }

    private void crearInventarioDestinoSiNoExiste(
            Sucursal sucursalDestino,
            Producto producto,
            Integer stockMinimoOrigen) {

        boolean yaExiste =
                inventarioSucursalRepository
                        .findBySucursalIdAndProductoId(
                                sucursalDestino.getId(),
                                producto.getId()
                        )
                        .isPresent();

        if (yaExiste) {
            return;
        }

        InventarioSucursal inventarioDestino =
                new InventarioSucursal();

        inventarioDestino.setSucursal(
                sucursalDestino
        );

        inventarioDestino.setProducto(
                producto
        );

        inventarioDestino.setExistencia(0);

        inventarioDestino.setStockMinimo(
                stockMinimoOrigen != null
                        ? stockMinimoOrigen
                        : 5
        );

        inventarioSucursalRepository.saveAndFlush(
                inventarioDestino
        );
    }

    private Sucursal obtenerSucursalActiva(
            Long sucursalId,
            String tipo) {

        Sucursal sucursal =
                sucursalRepository
                        .findById(sucursalId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "La sucursal de "
                                                + tipo
                                                + " no existe"
                                )
                        );

        if (!Boolean.TRUE.equals(
                sucursal.getActiva())) {

            throw new IllegalArgumentException(
                    "La sucursal de "
                            + tipo
                            + " se encuentra inactiva"
            );
        }

        return sucursal;
    }

    private InventarioSucursal
    obtenerInventarioBloqueado(
            Long sucursalId,
            Long productoId) {

        return inventarioSucursalRepository
                .buscarParaActualizar(
                        sucursalId,
                        productoId
                )
                .orElseThrow(
                        () -> new IllegalStateException(
                                "No se pudo preparar el inventario "
                                        + "para realizar la transferencia"
                        )
                );
    }

    private void validarSolicitud(
            TransferenciaInventarioRequest request) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "La solicitud de transferencia es obligatoria"
            );
        }

        if (request.getSucursalOrigenId() == null) {

            throw new IllegalArgumentException(
                    "Selecciona la sucursal de origen"
            );
        }

        if (request.getSucursalDestinoId() == null) {

            throw new IllegalArgumentException(
                    "Selecciona la sucursal de destino"
            );
        }

        if (request
                .getSucursalOrigenId()
                .equals(request.getSucursalDestinoId())) {

            throw new IllegalArgumentException(
                    "La sucursal de origen y destino deben ser diferentes"
            );
        }

        if (request.getProductoId() == null) {

            throw new IllegalArgumentException(
                    "Selecciona un producto"
            );
        }

        if (request.getCantidad() == null
                || request.getCantidad() <= 0) {

            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }

        if (request.getMotivo() == null
                || request.getMotivo().isBlank()) {

            throw new IllegalArgumentException(
                    "El motivo es obligatorio"
            );
        }

        if (request.getMotivo().trim().length() > 250) {

            throw new IllegalArgumentException(
                    "El motivo no puede exceder 250 caracteres"
            );
        }

        if (request.getObservaciones() != null
                && request
                .getObservaciones()
                .trim()
                .length() > 240) {

            throw new IllegalArgumentException(
                    "Las observaciones no pueden exceder 240 caracteres"
            );
        }
    }

    private String construirMotivo(
            TransferenciaInventarioRequest request) {

        String motivo =
                request.getMotivo().trim();

        if (request.getObservaciones() == null
                || request.getObservaciones().isBlank()) {

            return motivo;
        }

        return motivo
                + " | Observaciones: "
                + request.getObservaciones().trim();
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

    private InventarioSucursalResponse
    convertirInventario(
            InventarioSucursal inventario) {

        InventarioSucursalResponse response =
                new InventarioSucursalResponse();

        response.setInventarioId(
                inventario.getId()
        );

        response.setSucursalId(
                inventario.getSucursal().getId()
        );

        response.setSucursalCodigo(
                inventario.getSucursal().getCodigo()
        );

        response.setSucursalNombre(
                inventario.getSucursal().getNombre()
        );

        response.setProductoId(
                inventario.getProducto().getId()
        );

        response.setProductoNombre(
                inventario.getProducto().getNombre()
        );

        response.setCodigoBarras(
                inventario
                        .getProducto()
                        .getCodigoBarras()
        );

        response.setExistencia(
                valorSeguro(
                        inventario.getExistencia()
                )
        );

        response.setStockMinimo(
                valorSeguro(
                        inventario.getStockMinimo()
                )
        );

        return response;
    }

    private MovimientoInventarioResponse
    convertirMovimiento(
            MovimientoInventario movimiento) {

        MovimientoInventarioResponse response =
                new MovimientoInventarioResponse();

        response.setId(
                movimiento.getId()
        );

        response.setFolio(
                movimiento.getFolio()
        );

        response.setProducto(
                movimiento.getProducto().getNombre()
        );

        response.setCodigoBarras(
                movimiento
                        .getProducto()
                        .getCodigoBarras()
        );

        response.setSucursalOrigen(
                movimiento
                        .getSucursalOrigen()
                        .getNombre()
        );

        response.setSucursalDestino(
                movimiento
                        .getSucursalDestino()
                        .getNombre()
        );

        response.setCantidad(
                movimiento.getCantidad()
        );

        response.setExistenciaOrigenAnterior(
                movimiento
                        .getExistenciaOrigenAnterior()
        );

        response.setExistenciaOrigenNueva(
                movimiento
                        .getExistenciaOrigenNueva()
        );

        response.setExistenciaDestinoAnterior(
                movimiento
                        .getExistenciaDestinoAnterior()
        );

        response.setExistenciaDestinoNueva(
                movimiento
                        .getExistenciaDestinoNueva()
        );

        response.setUsuarioResponsable(
                movimiento.getUsuarioResponsable()
        );

        response.setMotivo(
                movimiento.getMotivo()
        );

        response.setEstado(
                movimiento.getEstado().name()
        );

        response.setFechaMovimiento(
                movimiento.getFechaMovimiento()
        );

        return response;
    }
}
