-- ============================================================
-- COMMERCE CLOUD III
-- V109
--
-- Objetivos:
--
-- 1. Relacionar cada sucursal con un tenant.
-- 2. Migrar las sucursales existentes sin borrar información.
-- 3. Cambiar la unicidad global de codigo por:
--
--        tenant_id + codigo
--
-- 4. Permitir que cada tenant tenga su propia sucursal MAT.
-- ============================================================

ALTER TABLE sucursal
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

-- ============================================================
-- PASO 1
-- Determinar el tenant de sucursales que ya tienen usuarios.
--
-- Si una sucursal tiene usuarios relacionados, se toma el
-- tenant asignado a dichos usuarios.
-- ============================================================

UPDATE sucursal s
SET tenant_id = origen.tenant_id
FROM (
    SELECT
        u.sucursal_id,
        MIN(u.tenant_id) AS tenant_id
    FROM usuario u
    WHERE u.sucursal_id IS NOT NULL
      AND u.tenant_id IS NOT NULL
    GROUP BY u.sucursal_id
) origen
WHERE s.id = origen.sucursal_id
  AND s.tenant_id IS NULL;

-- ============================================================
-- PASO 2
-- Migrar sucursales heredadas sin usuarios asociados.
--
-- Se asignan al primer tenant operativo que no sea PLATFORM.
-- Esto conserva los datos históricos de Abarrote Cloud.
-- ============================================================

DO $$
DECLARE
    v_tenant_legacy BIGINT;
BEGIN
    SELECT id
      INTO v_tenant_legacy
      FROM tenant
     WHERE tipo_negocio <> 'PLATFORM'
     ORDER BY id
     LIMIT 1;

    IF EXISTS (
        SELECT 1
          FROM sucursal
         WHERE tenant_id IS NULL
    ) THEN
        IF v_tenant_legacy IS NULL THEN
            RAISE EXCEPTION
                'No existe un tenant operativo para migrar las sucursales heredadas';
        END IF;

        UPDATE sucursal
           SET tenant_id = v_tenant_legacy
         WHERE tenant_id IS NULL;
    END IF;
END
$$;

-- ============================================================
-- PASO 3
-- Crear relación con tenant.
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_sucursal_tenant'
    ) THEN
        ALTER TABLE sucursal
            ADD CONSTRAINT fk_sucursal_tenant
            FOREIGN KEY (tenant_id)
            REFERENCES tenant(id);
    END IF;
END
$$;

ALTER TABLE sucursal
    ALTER COLUMN tenant_id SET NOT NULL;

-- ============================================================
-- PASO 4
-- El código deja de ser único globalmente.
-- ============================================================

ALTER TABLE sucursal
    DROP CONSTRAINT IF EXISTS uk_sucursal_codigo;

DROP INDEX IF EXISTS uk_sucursal_codigo;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'uk_sucursal_tenant_codigo'
    ) THEN
        ALTER TABLE sucursal
            ADD CONSTRAINT uk_sucursal_tenant_codigo
            UNIQUE (tenant_id, codigo);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_sucursal_tenant
    ON sucursal(tenant_id);

CREATE INDEX IF NOT EXISTS idx_sucursal_tenant_activa
    ON sucursal(tenant_id, activa);

-- ============================================================
-- VALIDACIONES
-- ============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM sucursal
         WHERE tenant_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Existen sucursales sin tenant después de V109';
    END IF;

    IF EXISTS (
        SELECT
            tenant_id,
            UPPER(codigo)
        FROM sucursal
        GROUP BY
            tenant_id,
            UPPER(codigo)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Existen códigos de sucursal duplicados dentro del mismo tenant';
    END IF;
END
$$;
