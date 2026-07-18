CREATE TABLE IF NOT EXISTS tenant (
    id BIGSERIAL PRIMARY KEY,

    nombre VARCHAR(150) NOT NULL,

    slug VARCHAR(80) NOT NULL,

    tipo_negocio VARCHAR(30) NOT NULL,

    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

    plan VARCHAR(30) NOT NULL DEFAULT 'BASICO',

    fecha_alta TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_vencimiento DATE,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_tenant_slug
        UNIQUE (slug),

    CONSTRAINT ck_tenant_tipo_negocio
        CHECK (
            tipo_negocio IN (
                'ABARROTES',
                'GYM',
                'FARMACIA',
                'COFFEE_SHOP',
                'RESTAURANTE',
                'OTRO'
            )
        ),

    CONSTRAINT ck_tenant_estado
        CHECK (
            estado IN (
                'ACTIVO',
                'SUSPENDIDO',
                'VENCIDO',
                'CANCELADO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_tenant_slug
    ON tenant(slug);

CREATE INDEX IF NOT EXISTS idx_tenant_estado
    ON tenant(estado);

CREATE INDEX IF NOT EXISTS idx_tenant_tipo_negocio
    ON tenant(tipo_negocio);
