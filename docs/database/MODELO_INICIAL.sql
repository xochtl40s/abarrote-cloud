-- Documento inicial de diseño.
-- Todavía no ejecutar en producción.

CREATE TABLE tenant (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    tipo_negocio VARCHAR(30) NOT NULL,
    plan VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento DATE
);

CREATE TABLE plan_membresia (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    duracion_dias INTEGER NOT NULL,
    precio NUMERIC(12,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_plan_membresia_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_plan_membresia_tenant_nombre
        UNIQUE (tenant_id, nombre),

    CONSTRAINT ck_plan_membresia_duracion
        CHECK (duracion_dias > 0),

    CONSTRAINT ck_plan_membresia_precio
        CHECK (precio > 0)
);

CREATE INDEX idx_plan_membresia_tenant
    ON plan_membresia(tenant_id);
