-- ============================================================
-- COMMERCE CLOUD II
-- Migración V108
--
-- Crea el tenant interno de plataforma y el usuario:
--
--     usuario: super-admin
--     password temporal: admin
--     rol: SUPER_ADMIN
--
-- IMPORTANTE:
-- La contraseña se almacena con BCrypt mediante pgcrypto.
-- ============================================================


-- ============================================================
-- 1. HABILITAR FUNCIONES CRIPTOGRÁFICAS
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- 2. AMPLIAR TIPOS DE NEGOCIO DEL TENANT
-- ============================================================

ALTER TABLE tenant
    DROP CONSTRAINT IF EXISTS ck_tenant_tipo_negocio;

ALTER TABLE tenant
    ADD CONSTRAINT ck_tenant_tipo_negocio
    CHECK (
        tipo_negocio IN (
            'PLATFORM',
            'ABARROTES',
            'GYM',
            'FARMACIA',
            'COFFEE_SHOP',
            'RESTAURANTE',
            'OTRO'
        )
    );


-- ============================================================
-- 3. CREAR TENANT INTERNO DE COMMERCE CLOUD
-- ============================================================

INSERT INTO tenant (
    nombre,
    slug,
    tipo_negocio,
    estado,
    plan,
    fecha_alta,
    fecha_actualizacion,
    fecha_vencimiento,
    activo
)
SELECT
    'Commerce Cloud Platform',
    'commerce-cloud-platform',
    'PLATFORM',
    'ACTIVO',
    'ENTERPRISE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM tenant
    WHERE LOWER(slug) =
          LOWER('commerce-cloud-platform')
);


-- ============================================================
-- 4. CREAR USUARIO SUPER ADMINISTRADOR
-- ============================================================

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
    'Super Administrador Commerce Cloud',
    'super-admin',
    crypt(
        'admin',
        gen_salt('bf', 10)
    ),
    'SUPER_ADMIN',
    TRUE,
    NULL,
    plataforma.id
FROM tenant plataforma
WHERE LOWER(plataforma.slug) =
      LOWER('commerce-cloud-platform')
  AND NOT EXISTS (
      SELECT 1
      FROM usuario existente
      WHERE LOWER(existente.username) =
            LOWER('super-admin')
  );


-- ============================================================
-- 5. CORREGIR USUARIO SI YA EXISTÍA
-- ============================================================

UPDATE usuario
SET
    nombre = 'Super Administrador Commerce Cloud',
    password = crypt(
        'admin',
        gen_salt('bf', 10)
    ),
    rol = 'SUPER_ADMIN',
    activo = TRUE,
    sucursal_id = NULL,
    tenant_id = (
        SELECT id
        FROM tenant
        WHERE LOWER(slug) =
              LOWER('commerce-cloud-platform')
        LIMIT 1
    )
WHERE LOWER(username) =
      LOWER('super-admin');


-- ============================================================
-- 6. DOCUMENTACIÓN
-- ============================================================

COMMENT ON CONSTRAINT ck_tenant_tipo_negocio
ON tenant IS
    'Tipos de negocio permitidos. PLATFORM representa la administración global de Commerce Cloud.';


-- ============================================================
-- 7. VALIDACIONES DE SEGURIDAD
-- ============================================================

DO $$
DECLARE
    tenant_platform_id BIGINT;
    usuario_super_admin_id BIGINT;
    rol_super_admin VARCHAR(100);
BEGIN

    SELECT id
    INTO tenant_platform_id
    FROM tenant
    WHERE LOWER(slug) =
          LOWER('commerce-cloud-platform')
    LIMIT 1;

    IF tenant_platform_id IS NULL THEN
        RAISE EXCEPTION
            'No fue posible crear el tenant PLATFORM';
    END IF;

    SELECT
        id,
        rol
    INTO
        usuario_super_admin_id,
        rol_super_admin
    FROM usuario
    WHERE LOWER(username) =
          LOWER('super-admin')
    LIMIT 1;

    IF usuario_super_admin_id IS NULL THEN
        RAISE EXCEPTION
            'No fue posible crear el usuario super-admin';
    END IF;

    IF UPPER(rol_super_admin) <> 'SUPER_ADMIN' THEN
        RAISE EXCEPTION
            'El usuario super-admin no tiene el rol SUPER_ADMIN';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM usuario
        WHERE id = usuario_super_admin_id
          AND tenant_id = tenant_platform_id
          AND activo = TRUE
    ) THEN
        RAISE EXCEPTION
            'El usuario super-admin no quedó asociado correctamente al tenant PLATFORM';
    END IF;

END
$$;


-- ============================================================
-- FIN MIGRACIÓN V108
-- ============================================================
