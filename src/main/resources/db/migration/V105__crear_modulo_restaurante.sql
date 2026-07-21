-- ============================================================
-- COMMERCE CLOUD
-- RESTAURANTE CLOUD
-- Migración V105
-- ============================================================

-- ============================================================
-- 1. TENANT RESTAURANTE
-- ============================================================

INSERT INTO tenant (
    nombre,
    slug,
    tipo_negocio,
    estado,
    plan,
    fecha_alta,
    fecha_actualizacion,
    activo
)
SELECT
    'Restaurante Cloud Principal',
    'restaurante-principal',
    'RESTAURANTE',
    'ACTIVO',
    'BASICO',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM tenant
    WHERE LOWER(slug) = LOWER('restaurante-principal')
);

-- ============================================================
-- 2. MESAS
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurante_mesa (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    numero INTEGER NOT NULL,

    nombre VARCHAR(100),

    capacidad INTEGER NOT NULL DEFAULT 4,

    estado VARCHAR(30) NOT NULL DEFAULT 'LIBRE',

    activa BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_restaurante_mesa_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_restaurante_mesa_tenant_numero
        UNIQUE (tenant_id, numero),

    CONSTRAINT ck_restaurante_mesa_capacidad
        CHECK (capacidad > 0),

    CONSTRAINT ck_restaurante_mesa_estado
        CHECK (
            estado IN (
                'LIBRE',
                'OCUPADA',
                'RESERVADA',
                'INACTIVA'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_restaurante_mesa_tenant
    ON restaurante_mesa(tenant_id);

CREATE INDEX IF NOT EXISTS idx_restaurante_mesa_estado
    ON restaurante_mesa(tenant_id, estado);

-- ============================================================
-- 3. CATEGORÍAS
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurante_categoria (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    nombre VARCHAR(100) NOT NULL,

    descripcion VARCHAR(255),

    activa BOOLEAN NOT NULL DEFAULT TRUE,

    orden_visual INTEGER NOT NULL DEFAULT 0,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_restaurante_categoria_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_restaurante_categoria_tenant_nombre
        UNIQUE (tenant_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_restaurante_categoria_tenant
    ON restaurante_categoria(tenant_id);

-- ============================================================
-- 4. PLATILLOS
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurante_platillo (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    categoria_id BIGINT NOT NULL,

    codigo VARCHAR(50) NOT NULL,

    nombre VARCHAR(150) NOT NULL,

    descripcion VARCHAR(500),

    precio NUMERIC(12,2) NOT NULL,

    costo NUMERIC(12,2) NOT NULL DEFAULT 0,

    imagen_url VARCHAR(500),

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    disponible BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_restaurante_platillo_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_restaurante_platillo_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES restaurante_categoria(id),

    CONSTRAINT uk_restaurante_platillo_tenant_codigo
        UNIQUE (tenant_id, codigo),

    CONSTRAINT ck_restaurante_platillo_precio
        CHECK (precio >= 0),

    CONSTRAINT ck_restaurante_platillo_costo
        CHECK (costo >= 0)
);

CREATE INDEX IF NOT EXISTS idx_restaurante_platillo_tenant
    ON restaurante_platillo(tenant_id);

CREATE INDEX IF NOT EXISTS idx_restaurante_platillo_categoria
    ON restaurante_platillo(categoria_id);

CREATE INDEX IF NOT EXISTS idx_restaurante_platillo_disponible
    ON restaurante_platillo(
        tenant_id,
        activo,
        disponible
    );

-- ============================================================
-- 5. PEDIDOS
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurante_pedido (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    mesa_id BIGINT NOT NULL,

    mesero_usuario_id BIGINT NOT NULL,

    folio VARCHAR(50) NOT NULL,

    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO',

    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0,

    descuento NUMERIC(12,2) NOT NULL DEFAULT 0,

    total NUMERIC(12,2) NOT NULL DEFAULT 0,

    fecha_apertura TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_cierre TIMESTAMP,

    observaciones VARCHAR(1000),

    CONSTRAINT fk_restaurante_pedido_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_restaurante_pedido_mesa
        FOREIGN KEY (mesa_id)
        REFERENCES restaurante_mesa(id),

    CONSTRAINT fk_restaurante_pedido_mesero
        FOREIGN KEY (mesero_usuario_id)
        REFERENCES usuario(id),

    CONSTRAINT uk_restaurante_pedido_tenant_folio
        UNIQUE (tenant_id, folio),

    CONSTRAINT ck_restaurante_pedido_estado
        CHECK (
            estado IN (
                'ABIERTO',
                'EN_PREPARACION',
                'LISTO',
                'ENTREGADO',
                'CERRADO',
                'CANCELADO'
            )
        ),

    CONSTRAINT ck_restaurante_pedido_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT ck_restaurante_pedido_descuento
        CHECK (descuento >= 0),

    CONSTRAINT ck_restaurante_pedido_total
        CHECK (total >= 0)
);

CREATE INDEX IF NOT EXISTS idx_restaurante_pedido_tenant
    ON restaurante_pedido(tenant_id);

CREATE INDEX IF NOT EXISTS idx_restaurante_pedido_estado
    ON restaurante_pedido(tenant_id, estado);

CREATE INDEX IF NOT EXISTS idx_restaurante_pedido_fecha
    ON restaurante_pedido(tenant_id, fecha_apertura);

CREATE INDEX IF NOT EXISTS idx_restaurante_pedido_mesero
    ON restaurante_pedido(
        tenant_id,
        mesero_usuario_id
    );

-- Una mesa solo puede tener un pedido activo.
CREATE UNIQUE INDEX IF NOT EXISTS
    uk_restaurante_pedido_mesa_abierto
ON restaurante_pedido (
    tenant_id,
    mesa_id
)
WHERE estado IN (
    'ABIERTO',
    'EN_PREPARACION',
    'LISTO',
    'ENTREGADO'
);

-- ============================================================
-- 6. DETALLE DEL PEDIDO
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurante_pedido_detalle (
    id BIGSERIAL PRIMARY KEY,

    pedido_id BIGINT NOT NULL,

    platillo_id BIGINT NOT NULL,

    cantidad INTEGER NOT NULL,

    precio_unitario NUMERIC(12,2) NOT NULL,

    subtotal NUMERIC(12,2) NOT NULL,

    observaciones VARCHAR(500),

    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_restaurante_detalle_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES restaurante_pedido(id),

    CONSTRAINT fk_restaurante_detalle_platillo
        FOREIGN KEY (platillo_id)
        REFERENCES restaurante_platillo(id),

    CONSTRAINT ck_restaurante_detalle_cantidad
        CHECK (cantidad > 0),

    CONSTRAINT ck_restaurante_detalle_precio
        CHECK (precio_unitario >= 0),

    CONSTRAINT ck_restaurante_detalle_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT ck_restaurante_detalle_estado
        CHECK (
            estado IN (
                'PENDIENTE',
                'EN_PREPARACION',
                'LISTO',
                'ENTREGADO',
                'CANCELADO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_restaurante_detalle_pedido
    ON restaurante_pedido_detalle(pedido_id);

CREATE INDEX IF NOT EXISTS idx_restaurante_detalle_platillo
    ON restaurante_pedido_detalle(platillo_id);

-- ============================================================
-- 7. PAGOS
-- ============================================================

CREATE TABLE IF NOT EXISTS restaurante_pago (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    pedido_id BIGINT NOT NULL,

    importe NUMERIC(12,2) NOT NULL,

    metodo_pago VARCHAR(30) NOT NULL,

    referencia VARCHAR(150),

    fecha_pago TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    usuario_username VARCHAR(100) NOT NULL,

    CONSTRAINT fk_restaurante_pago_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_restaurante_pago_pedido
        FOREIGN KEY (pedido_id)
        REFERENCES restaurante_pedido(id),

    CONSTRAINT ck_restaurante_pago_importe
        CHECK (importe > 0),

    CONSTRAINT ck_restaurante_pago_metodo
        CHECK (
            metodo_pago IN (
                'EFECTIVO',
                'TARJETA',
                'TRANSFERENCIA',
                'MIXTO',
                'OTRO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_restaurante_pago_tenant
    ON restaurante_pago(tenant_id);

CREATE INDEX IF NOT EXISTS idx_restaurante_pago_pedido
    ON restaurante_pago(pedido_id);

CREATE INDEX IF NOT EXISTS idx_restaurante_pago_fecha
    ON restaurante_pago(tenant_id, fecha_pago);

-- ============================================================
-- 8. USUARIOS RESTAURANTE
-- ============================================================
-- La contraseña inicial será la misma del usuario admin.
-- Esto copia el hash BCrypt, no la contraseña visible.

INSERT INTO usuario (
    nombre,
    username,
    password,
    rol,
    activo,
    sucursal_id,
    tenant_id
)
SELECT
    'Administrador Restaurante',
    'adminrest',
    admin.password,
    'ADMIN',
    TRUE,
    NULL,
    restaurante.id
FROM usuario admin
CROSS JOIN tenant restaurante
WHERE LOWER(admin.username) = LOWER('admin')
  AND LOWER(restaurante.slug) =
      LOWER('restaurante-principal')
  AND NOT EXISTS (
      SELECT 1
      FROM usuario existente
      WHERE LOWER(existente.username) =
            LOWER('adminrest')
  );

INSERT INTO usuario (
    nombre,
    username,
    password,
    rol,
    activo,
    sucursal_id,
    tenant_id
)
SELECT
    'Mesero 1',
    'mesero1',
    admin.password,
    'MESERO',
    TRUE,
    NULL,
    restaurante.id
FROM usuario admin
CROSS JOIN tenant restaurante
WHERE LOWER(admin.username) = LOWER('admin')
  AND LOWER(restaurante.slug) =
      LOWER('restaurante-principal')
  AND NOT EXISTS (
      SELECT 1
      FROM usuario existente
      WHERE LOWER(existente.username) =
            LOWER('mesero1')
  );

INSERT INTO usuario (
    nombre,
    username,
    password,
    rol,
    activo,
    sucursal_id,
    tenant_id
)
SELECT
    'Mesero 2',
    'mesero2',
    admin.password,
    'MESERO',
    TRUE,
    NULL,
    restaurante.id
FROM usuario admin
CROSS JOIN tenant restaurante
WHERE LOWER(admin.username) = LOWER('admin')
  AND LOWER(restaurante.slug) =
      LOWER('restaurante-principal')
  AND NOT EXISTS (
      SELECT 1
      FROM usuario existente
      WHERE LOWER(existente.username) =
            LOWER('mesero2')
  );

INSERT INTO usuario (
    nombre,
    username,
    password,
    rol,
    activo,
    sucursal_id,
    tenant_id
)
SELECT
    'Mesero 3',
    'mesero3',
    admin.password,
    'MESERO',
    TRUE,
    NULL,
    restaurante.id
FROM usuario admin
CROSS JOIN tenant restaurante
WHERE LOWER(admin.username) = LOWER('admin')
  AND LOWER(restaurante.slug) =
      LOWER('restaurante-principal')
  AND NOT EXISTS (
      SELECT 1
      FROM usuario existente
      WHERE LOWER(existente.username) =
            LOWER('mesero3')
  );

-- ============================================================
-- 9. MESAS DEMO
-- ============================================================

INSERT INTO restaurante_mesa (
    tenant_id,
    numero,
    nombre,
    capacidad,
    estado,
    activa
)
SELECT
    tenant_rest.id,
    numero_mesa,
    'Mesa ' || numero_mesa,
    CASE
        WHEN numero_mesa <= 4 THEN 4
        WHEN numero_mesa <= 8 THEN 6
        ELSE 8
    END,
    'LIBRE',
    TRUE
FROM tenant tenant_rest
CROSS JOIN generate_series(1, 10) numero_mesa
WHERE LOWER(tenant_rest.slug) =
      LOWER('restaurante-principal')
ON CONFLICT (
    tenant_id,
    numero
)
DO NOTHING;

-- ============================================================
-- 10. CATEGORÍAS DEMO
-- ============================================================

INSERT INTO restaurante_categoria (
    tenant_id,
    nombre,
    descripcion,
    activa,
    orden_visual
)
SELECT
    tenant_rest.id,
    datos.nombre,
    datos.descripcion,
    TRUE,
    datos.orden_visual
FROM tenant tenant_rest
CROSS JOIN (
    VALUES
        ('Entradas', 'Platillos para iniciar', 1),
        ('Sopas', 'Sopas y caldos', 2),
        ('Ensaladas', 'Ensaladas frescas', 3),
        ('Platos fuertes', 'Especialidades principales', 4),
        ('Tacos', 'Tacos y antojitos', 5),
        ('Hamburguesas', 'Hamburguesas y complementos', 6),
        ('Bebidas', 'Bebidas frías y calientes', 7),
        ('Postres', 'Postres de la casa', 8)
) AS datos(
    nombre,
    descripcion,
    orden_visual
)
WHERE LOWER(tenant_rest.slug) =
      LOWER('restaurante-principal')
ON CONFLICT (
    tenant_id,
    nombre
)
DO NOTHING;

-- ============================================================
-- 11. PLATILLOS DEMO
-- ============================================================

INSERT INTO restaurante_platillo (
    tenant_id,
    categoria_id,
    codigo,
    nombre,
    descripcion,
    precio,
    costo,
    activo,
    disponible
)
SELECT
    tenant_rest.id,
    categoria.id,
    datos.codigo,
    datos.nombre,
    datos.descripcion,
    datos.precio,
    datos.costo,
    TRUE,
    TRUE
FROM tenant tenant_rest
JOIN (
    VALUES
        (
            'BEB-001',
            'Agua natural',
            'Bebidas',
            'Botella de agua natural',
            25.00::NUMERIC,
            8.00::NUMERIC
        ),
        (
            'BEB-002',
            'Refresco',
            'Bebidas',
            'Refresco de lata',
            35.00::NUMERIC,
            14.00::NUMERIC
        ),
        (
            'BEB-003',
            'Café',
            'Bebidas',
            'Café americano',
            40.00::NUMERIC,
            10.00::NUMERIC
        ),
        (
            'SOP-001',
            'Sopa del día',
            'Sopas',
            'Sopa preparada diariamente',
            65.00::NUMERIC,
            22.00::NUMERIC
        ),
        (
            'ENS-001',
            'Ensalada César',
            'Ensaladas',
            'Lechuga, crutones, queso y aderezo',
            95.00::NUMERIC,
            38.00::NUMERIC
        ),
        (
            'HAM-001',
            'Hamburguesa clásica',
            'Hamburguesas',
            'Carne, queso, lechuga, jitomate y papas',
            135.00::NUMERIC,
            58.00::NUMERIC
        ),
        (
            'TAC-001',
            'Tacos al pastor',
            'Tacos',
            'Orden de tres tacos al pastor',
            75.00::NUMERIC,
            30.00::NUMERIC
        ),
        (
            'PLA-001',
            'Pechuga asada',
            'Platos fuertes',
            'Pechuga con ensalada y arroz',
            145.00::NUMERIC,
            61.00::NUMERIC
        ),
        (
            'PLA-002',
            'Pasta Alfredo',
            'Platos fuertes',
            'Pasta con salsa cremosa',
            125.00::NUMERIC,
            48.00::NUMERIC
        ),
        (
            'POS-001',
            'Flan napolitano',
            'Postres',
            'Rebanada de flan de la casa',
            55.00::NUMERIC,
            18.00::NUMERIC
        )
) AS datos(
    codigo,
    nombre,
    categoria_nombre,
    descripcion,
    precio,
    costo
)
ON TRUE
JOIN restaurante_categoria categoria
  ON categoria.tenant_id = tenant_rest.id
 AND LOWER(categoria.nombre) =
     LOWER(datos.categoria_nombre)
WHERE LOWER(tenant_rest.slug) =
      LOWER('restaurante-principal')
ON CONFLICT (
    tenant_id,
    codigo
)
DO NOTHING;
