CREATE TABLE IF NOT EXISTS gym_producto (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    codigo VARCHAR(50) NOT NULL,

    nombre VARCHAR(150) NOT NULL,

    descripcion VARCHAR(500),

    categoria VARCHAR(100),

    precio NUMERIC(12,2) NOT NULL,

    costo NUMERIC(12,2),

    existencia INTEGER NOT NULL DEFAULT 0,

    stock_minimo INTEGER NOT NULL DEFAULT 0,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_producto_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_gym_producto_tenant_codigo
        UNIQUE (tenant_id, codigo),

    CONSTRAINT ck_gym_producto_precio
        CHECK (precio >= 0),

    CONSTRAINT ck_gym_producto_costo
        CHECK (costo IS NULL OR costo >= 0),

    CONSTRAINT ck_gym_producto_existencia
        CHECK (existencia >= 0),

    CONSTRAINT ck_gym_producto_stock_minimo
        CHECK (stock_minimo >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_producto_tenant
    ON gym_producto(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_producto_nombre
    ON gym_producto(tenant_id, nombre);

CREATE INDEX IF NOT EXISTS idx_gym_producto_categoria
    ON gym_producto(tenant_id, categoria);

CREATE TABLE IF NOT EXISTS gym_venta (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    usuario_username VARCHAR(50) NOT NULL,

    total NUMERIC(12,2) NOT NULL,

    metodo_pago VARCHAR(30) NOT NULL,

    fecha_venta TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_venta_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT ck_gym_venta_total
        CHECK (total >= 0),

    CONSTRAINT ck_gym_venta_metodo_pago
        CHECK (
            metodo_pago IN (
                'EFECTIVO',
                'TARJETA',
                'TRANSFERENCIA',
                'OTRO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_gym_venta_tenant
    ON gym_venta(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_venta_fecha
    ON gym_venta(tenant_id, fecha_venta);

CREATE TABLE IF NOT EXISTS gym_venta_detalle (
    id BIGSERIAL PRIMARY KEY,

    venta_id BIGINT NOT NULL,

    producto_id BIGINT NOT NULL,

    cantidad INTEGER NOT NULL,

    precio_unitario NUMERIC(12,2) NOT NULL,

    subtotal NUMERIC(12,2) NOT NULL,

    CONSTRAINT fk_gym_detalle_venta
        FOREIGN KEY (venta_id)
        REFERENCES gym_venta(id),

    CONSTRAINT fk_gym_detalle_producto
        FOREIGN KEY (producto_id)
        REFERENCES gym_producto(id),

    CONSTRAINT ck_gym_detalle_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT ck_gym_detalle_precio
        CHECK (precio_unitario >= 0),

    CONSTRAINT ck_gym_detalle_subtotal
        CHECK (subtotal >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_detalle_venta
    ON gym_venta_detalle(venta_id);

CREATE INDEX IF NOT EXISTS idx_gym_detalle_producto
    ON gym_venta_detalle(producto_id);
