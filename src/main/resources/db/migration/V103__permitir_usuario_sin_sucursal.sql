/*
 * Commerce Cloud
 *
 * Los usuarios de verticales que todavía no utilizan la estructura
 * heredada de sucursales —por ejemplo Gym Cloud— deben poder existir
 * sin sucursal_id.
 *
 * El aislamiento de empresa queda garantizado por tenant_id.
 */

ALTER TABLE usuario
    ALTER COLUMN sucursal_id DROP NOT NULL;

COMMENT ON COLUMN usuario.sucursal_id IS
    'Sucursal operativa opcional. Puede ser NULL para verticales que aún no utilizan la estructura heredada de sucursales.';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'usuario'
          AND column_name = 'sucursal_id'
          AND is_nullable <> 'YES'
    ) THEN
        RAISE EXCEPTION
            'No fue posible convertir usuario.sucursal_id en nullable';
    END IF;
END
$$;
