-- ============================================================
-- ABARROTE CLOUD
-- V2 - NORMALIZACIÓN MULTISUCURSAL
-- ============================================================

-- ------------------------------------------------------------
-- 1. Garantizar existencia de Sucursal Matriz
-- ------------------------------------------------------------

INSERT INTO sucursal (
    activa,
    codigo,
    direccion,
    fecha_creacion,
    nombre,
    telefono
)
SELECT
    TRUE,
    'MAT',
    'Sucursal principal',
    CURRENT_TIMESTAMP,
    'Sucursal Matriz',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM sucursal
    WHERE UPPER(codigo) = 'MAT'
);

-- ------------------------------------------------------------
-- 2. Normalizar código MAT
-- ------------------------------------------------------------

UPDATE sucursal
SET
    codigo = UPPER(TRIM(codigo)),
    nombre = TRIM(nombre)
WHERE
    codigo <> UPPER(TRIM(codigo))
    OR nombre <> TRIM(nombre);

-- ------------------------------------------------------------
-- 3. Asignar usuarios históricos sin sucursal a Matriz
-- ------------------------------------------------------------

UPDATE usuario
SET sucursal_id = (
    SELECT id
    FROM sucursal
    WHERE UPPER(codigo) = 'MAT'
    ORDER BY id
    LIMIT 1
)
WHERE sucursal_id IS NULL;

-- ------------------------------------------------------------
-- 4. Asignar ventas históricas a la sucursal del usuario
-- ------------------------------------------------------------

UPDATE venta AS v
SET sucursal_id = u.sucursal_id
FROM usuario AS u
WHERE
    v.usuario_id = u.id
    AND v.sucursal_id IS NULL
    AND u.sucursal_id IS NOT NULL;

-- ------------------------------------------------------------
-- 5. Si todavía existe alguna venta sin sucursal,
--    asignarla directamente a Matriz
-- ------------------------------------------------------------

UPDATE venta
SET sucursal_id = (
    SELECT id
    FROM sucursal
    WHERE UPPER(codigo) = 'MAT'
    ORDER BY id
    LIMIT 1
)
WHERE sucursal_id IS NULL;

-- ------------------------------------------------------------
-- 6. Normalizar valores nulos de inventario
-- ------------------------------------------------------------

UPDATE inventario_sucursal
SET existencia = 0
WHERE existencia IS NULL;

UPDATE inventario_sucursal
SET stock_minimo = 0
WHERE stock_minimo IS NULL;

UPDATE inventario_sucursal
SET fecha_actualizacion = CURRENT_TIMESTAMP
WHERE fecha_actualizacion IS NULL;

-- ------------------------------------------------------------
-- 7. Crear índices adicionales de consulta
-- ------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_usuario_sucursal
    ON usuario(sucursal_id);

CREATE INDEX IF NOT EXISTS idx_usuario_activo
    ON usuario(activo);

CREATE INDEX IF NOT EXISTS idx_producto_activo
    ON producto(activo);

CREATE INDEX IF NOT EXISTS idx_producto_categoria
    ON producto(categoria_id);

CREATE INDEX IF NOT EXISTS idx_venta_estado
    ON venta(estado);

CREATE INDEX IF NOT EXISTS idx_venta_sucursal_fecha
    ON venta(
        sucursal_id,
        fecha_hora
    );

CREATE INDEX IF NOT EXISTS idx_movimiento_usuario
    ON movimiento_inventario(usuario_responsable);

-- ------------------------------------------------------------
-- 8. Restricciones comerciales
-- ------------------------------------------------------------

ALTER TABLE usuario
    ALTER COLUMN sucursal_id
    SET NOT NULL;

ALTER TABLE venta
    ALTER COLUMN sucursal_id
    SET NOT NULL;

-- ------------------------------------------------------------
-- 9. Restricciones CHECK agregadas de forma segura
-- ------------------------------------------------------------

ALTER TABLE inventario_sucursal
    DROP CONSTRAINT IF EXISTS
        ck_inventario_existencia_no_negativa;

ALTER TABLE inventario_sucursal
    ADD CONSTRAINT
        ck_inventario_existencia_no_negativa
    CHECK (existencia >= 0);

ALTER TABLE inventario_sucursal
    DROP CONSTRAINT IF EXISTS
        ck_inventario_stock_minimo_no_negativo;

ALTER TABLE inventario_sucursal
    ADD CONSTRAINT
        ck_inventario_stock_minimo_no_negativo
    CHECK (stock_minimo >= 0);

ALTER TABLE producto
    DROP CONSTRAINT IF EXISTS
        ck_producto_stock_no_negativo;

ALTER TABLE producto
    ADD CONSTRAINT
        ck_producto_stock_no_negativo
    CHECK (stock >= 0);

ALTER TABLE producto
    DROP CONSTRAINT IF EXISTS
        ck_producto_stock_minimo_no_negativo;

ALTER TABLE producto
    ADD CONSTRAINT
        ck_producto_stock_minimo_no_negativo
    CHECK (stock_minimo >= 0);

ALTER TABLE venta
    DROP CONSTRAINT IF EXISTS
        ck_venta_total_no_negativo;

ALTER TABLE venta
    ADD CONSTRAINT
        ck_venta_total_no_negativo
    CHECK (total >= 0);
