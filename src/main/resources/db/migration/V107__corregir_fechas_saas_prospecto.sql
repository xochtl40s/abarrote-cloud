-- ============================================================
-- COMMERCE CLOUD II
-- Migración V107
-- Corrección de columnas de auditoría en saas_prospecto
-- ============================================================

ALTER TABLE saas_prospecto
    ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP;

ALTER TABLE saas_prospecto
    ADD COLUMN IF NOT EXISTS fecha_actualizacion TIMESTAMP;

-- Reparar registros existentes antes de declarar NOT NULL.
UPDATE saas_prospecto
SET fecha_creacion = CURRENT_TIMESTAMP
WHERE fecha_creacion IS NULL;

UPDATE saas_prospecto
SET fecha_actualizacion = COALESCE(
    fecha_creacion,
    CURRENT_TIMESTAMP
)
WHERE fecha_actualizacion IS NULL;

ALTER TABLE saas_prospecto
    ALTER COLUMN fecha_creacion
    SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE saas_prospecto
    ALTER COLUMN fecha_actualizacion
    SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE saas_prospecto
    ALTER COLUMN fecha_creacion
    SET NOT NULL;

ALTER TABLE saas_prospecto
    ALTER COLUMN fecha_actualizacion
    SET NOT NULL;

COMMENT ON COLUMN saas_prospecto.fecha_creacion IS
    'Fecha en que el prospecto fue registrado en Commerce Cloud.';

COMMENT ON COLUMN saas_prospecto.fecha_actualizacion IS
    'Fecha de la última modificación del prospecto.';

CREATE INDEX IF NOT EXISTS idx_saas_prospecto_fecha_creacion
    ON saas_prospecto(fecha_creacion DESC);
