package com.abarrote.abarroteapi.service.impl;

import com.abarrote.abarroteapi.dto.ItemVentaRequest;
import com.abarrote.abarroteapi.dto.VentaRequest;
import com.abarrote.abarroteapi.entity.DetalleVenta;
import com.abarrote.abarroteapi.entity.Producto;
import com.abarrote.abarroteapi.entity.Venta;
import com.abarrote.abarroteapi.repository.ProductoRepository;
import com.abarrote.abarroteapi.repository.VentaRepository;
import com.abarrote.abarroteapi.service.VentaService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository) {

        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }


    @Override
    public Venta guardar(Venta venta) {
        return ventaRepository.save(venta);
    }


    @Override
    public Venta registrarVenta(VentaRequest request) {

        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;


        for(ItemVentaRequest item : request.getItems()) {

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> 
                    new RuntimeException("Producto no encontrado"));


            if(producto.getStock() < item.getCantidad()) {
                throw new RuntimeException(
                    "Stock insuficiente: " + producto.getNombre()
                );
            }


            BigDecimal subtotal =
                    producto.getPrecioVenta()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));


            DetalleVenta detalle = new DetalleVenta();

            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(subtotal);


            venta.agregarDetalle(detalle);


            producto.setStock(
                producto.getStock() - item.getCantidad()
            );

            productoRepository.save(producto);


            total = total.add(subtotal);
        }


        venta.setTotal(total);


        return ventaRepository.save(venta);
    }
}
