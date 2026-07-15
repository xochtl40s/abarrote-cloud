package com.abarrote.abarroteapi.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SucursalDetalleResponse {

    private Long sucursalId;
    private String codigo;
    private String nombre;
    private String direccion;
    private String telefono;
    private Boolean activa;

    private Integer productosDiferentes;
    private Integer unidadesTotales;
    private BigDecimal valorInventario;

    private Integer numeroVentas;
    private BigDecimal totalVentas;
    private BigDecimal ventaPromedio;

    private List<ProductoStock> productos =
            new ArrayList<>();

    private List<CajeroEstadistica> cajeros =
            new ArrayList<>();

    public static class ProductoStock {

        private Long productoId;
        private String codigoBarras;
        private String producto;
        private Integer existencia;
        private Integer stockMinimo;
        private BigDecimal precioVenta;
        private BigDecimal valorInventario;

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public String getCodigoBarras() {
            return codigoBarras;
        }

        public void setCodigoBarras(String codigoBarras) {
            this.codigoBarras = codigoBarras;
        }

        public String getProducto() {
            return producto;
        }

        public void setProducto(String producto) {
            this.producto = producto;
        }

        public Integer getExistencia() {
            return existencia;
        }

        public void setExistencia(Integer existencia) {
            this.existencia = existencia;
        }

        public Integer getStockMinimo() {
            return stockMinimo;
        }

        public void setStockMinimo(Integer stockMinimo) {
            this.stockMinimo = stockMinimo;
        }

        public BigDecimal getPrecioVenta() {
            return precioVenta;
        }

        public void setPrecioVenta(BigDecimal precioVenta) {
            this.precioVenta = precioVenta;
        }

        public BigDecimal getValorInventario() {
            return valorInventario;
        }

        public void setValorInventario(
                BigDecimal valorInventario) {

            this.valorInventario = valorInventario;
        }
    }

    public static class CajeroEstadistica {

        private Long usuarioId;
        private String nombre;
        private String username;
        private String rol;
        private Boolean activo;

        private Integer numeroVentas;
        private BigDecimal totalVentas;
        private BigDecimal ventaPromedio;

        public Long getUsuarioId() {
            return usuarioId;
        }

        public void setUsuarioId(Long usuarioId) {
            this.usuarioId = usuarioId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRol() {
            return rol;
        }

        public void setRol(String rol) {
            this.rol = rol;
        }

        public Boolean getActivo() {
            return activo;
        }

        public void setActivo(Boolean activo) {
            this.activo = activo;
        }

        public Integer getNumeroVentas() {
            return numeroVentas;
        }

        public void setNumeroVentas(Integer numeroVentas) {
            this.numeroVentas = numeroVentas;
        }

        public BigDecimal getTotalVentas() {
            return totalVentas;
        }

        public void setTotalVentas(BigDecimal totalVentas) {
            this.totalVentas = totalVentas;
        }

        public BigDecimal getVentaPromedio() {
            return ventaPromedio;
        }

        public void setVentaPromedio(
                BigDecimal ventaPromedio) {

            this.ventaPromedio = ventaPromedio;
        }
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public Integer getProductosDiferentes() {
        return productosDiferentes;
    }

    public void setProductosDiferentes(
            Integer productosDiferentes) {

        this.productosDiferentes = productosDiferentes;
    }

    public Integer getUnidadesTotales() {
        return unidadesTotales;
    }

    public void setUnidadesTotales(
            Integer unidadesTotales) {

        this.unidadesTotales = unidadesTotales;
    }

    public BigDecimal getValorInventario() {
        return valorInventario;
    }

    public void setValorInventario(
            BigDecimal valorInventario) {

        this.valorInventario = valorInventario;
    }

    public Integer getNumeroVentas() {
        return numeroVentas;
    }

    public void setNumeroVentas(Integer numeroVentas) {
        this.numeroVentas = numeroVentas;
    }

    public BigDecimal getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(BigDecimal totalVentas) {
        this.totalVentas = totalVentas;
    }

    public BigDecimal getVentaPromedio() {
        return ventaPromedio;
    }

    public void setVentaPromedio(
            BigDecimal ventaPromedio) {

        this.ventaPromedio = ventaPromedio;
    }

    public List<ProductoStock> getProductos() {
        return productos;
    }

    public void setProductos(
            List<ProductoStock> productos) {

        this.productos = productos;
    }

    public List<CajeroEstadistica> getCajeros() {
        return cajeros;
    }

    public void setCajeros(
            List<CajeroEstadistica> cajeros) {

        this.cajeros = cajeros;
    }
}
