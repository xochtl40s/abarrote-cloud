-- ============================================================
-- COMMERCE CLOUD III
-- V110
--
-- Permite la eliminación lógica de prospectos y empresas.
--
-- Estados válidos:
--
-- NUEVO
-- EN_PROCESO
-- ACTIVADO
-- RECHAZADO
-- ELIMINADO
-- ============================================================

ALTER TABLE saas_prospecto
    DROP CONSTRAINT IF EXISTS ck_saas_prospecto_estado;

ALTER TABLE saas_prospecto
    ADD CONSTRAINT ck_saas_prospecto_estado
    CHECK (
        estado IN (
            'NUEVO',
            'EN_PROCESO',
            'ACTIVADO',
            'RECHAZADO',
            'ELIMINADO'
        )
    );

-- ============================================================
-- VALIDACIÓN
-- ============================================================

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM saas_prospecto
        WHERE estado NOT IN (
            'NUEVO',
            'EN_PROCESO',
            'ACTIVADO',
            'RECHAZADO',
            'ELIMINADO'
        )
    ) THEN
        RAISE EXCEPTION
            'Existen prospectos con estados no soportados';
    END IF;
END
$$;
