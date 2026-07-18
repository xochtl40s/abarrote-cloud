ALTER TABLE usuario
    ADD COLUMN IF NOT EXISTS tenant_id BIGINT;

DO $$
DECLARE
    tenant_abarrotes_id BIGINT;
BEGIN
    SELECT id
      INTO tenant_abarrotes_id
      FROM tenant
     WHERE LOWER(slug) = LOWER('abarrotes-principal')
     LIMIT 1;

    IF tenant_abarrotes_id IS NULL THEN
        RAISE EXCEPTION
            'No existe el tenant abarrotes-principal. Ejecute primero el bootstrap de tenants.';
    END IF;

    UPDATE usuario
       SET tenant_id = tenant_abarrotes_id
     WHERE tenant_id IS NULL;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'fk_usuario_tenant'
    ) THEN
        ALTER TABLE usuario
            ADD CONSTRAINT fk_usuario_tenant
            FOREIGN KEY (tenant_id)
            REFERENCES tenant(id);
    END IF;
END
$$;

ALTER TABLE usuario
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_usuario_tenant
    ON usuario(tenant_id);

CREATE INDEX IF NOT EXISTS idx_usuario_tenant_activo
    ON usuario(tenant_id, activo);
