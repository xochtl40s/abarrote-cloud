package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ItemVentaRequest;
import com.abarrote.abarroteapi.dto.VentaRequest;
import com.abarrote.abarroteapi.entity.DetalleVenta;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.VentaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {

        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Venta guardar(Venta venta) {
        if (venta.getFechaHora() == null) {
            venta.setFechaHora(LocalDateTime.now());
        }
        return ventaRepository.save(venta);
    }

    @Override
    public Venta registrarVenta(VentaRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "cajero";
        
        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Venta venta = new Venta();
        venta.setFechaHora(LocalDateTime.now());
        venta.setUsuario(usuario);
        venta.setEstado(Venta.EstadoVenta.COMPLETADA);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemVentaRequest item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente: " + producto.getNombre() 
                    + " (disponible: " + producto.getStock() + ", solicitado: " + item.getCantidad() + ")");
            }

            BigDecimal subtotal = producto.getPrecioVenta()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);

            venta.agregarDetalle(detalle);

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            total = total.add(subtotal);
        }

        venta.setTotal(total);
        return ventaRepository.save(venta);
    }
}
