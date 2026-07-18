CREATE TABLE IF NOT EXISTS gym_cliente (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    numero_cliente VARCHAR(30) NOT NULL,

    nombre VARCHAR(100) NOT NULL,

    apellido_paterno VARCHAR(100),

    apellido_materno VARCHAR(100),

    telefono VARCHAR(30),

    email VARCHAR(150),

    fecha_nacimiento DATE,

    contacto_emergencia VARCHAR(150),

    telefono_emergencia VARCHAR(30),

    estado VARCHAR(20) NOT NULL
        DEFAULT 'ACTIVO',

    fecha_registro TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_cliente_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_gym_cliente_tenant_numero
        UNIQUE (tenant_id, numero_cliente),

    CONSTRAINT ck_gym_cliente_estado
        CHECK (
            estado IN (
                'ACTIVO',
                'INACTIVO',
                'BLOQUEADO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_gym_cliente_tenant
    ON gym_cliente(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_cliente_nombre
    ON gym_cliente(
        tenant_id,
        nombre,
        apellido_paterno
    );

CREATE TABLE IF NOT EXISTS gym_plan_membresia (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    nombre VARCHAR(100) NOT NULL,

    descripcion VARCHAR(500),

    duracion_dias INTEGER NOT NULL,

    precio NUMERIC(12,2) NOT NULL,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_plan_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT uk_gym_plan_tenant_nombre
        UNIQUE (tenant_id, nombre),

    CONSTRAINT ck_gym_plan_duracion
        CHECK (duracion_dias > 0),

    CONSTRAINT ck_gym_plan_precio
        CHECK (precio >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_plan_tenant
    ON gym_plan_membresia(tenant_id);

CREATE TABLE IF NOT EXISTS gym_membresia (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    cliente_id BIGINT NOT NULL,

    plan_id BIGINT NOT NULL,

    fecha_inicio DATE NOT NULL,

    fecha_fin DATE NOT NULL,

    precio_contratado NUMERIC(12,2) NOT NULL,

    estado VARCHAR(20) NOT NULL
        DEFAULT 'PENDIENTE',

    fecha_creacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_gym_membresia_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_gym_membresia_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES gym_cliente(id),

    CONSTRAINT fk_gym_membresia_plan
        FOREIGN KEY (plan_id)
        REFERENCES gym_plan_membresia(id),

    CONSTRAINT ck_gym_membresia_estado
        CHECK (
            estado IN (
                'PENDIENTE',
                'ACTIVA',
                'VENCIDA',
                'CANCELADA',
                'SUSPENDIDA'
            )
        ),

    CONSTRAINT ck_gym_membresia_fechas
        CHECK (fecha_fin >= fecha_inicio),

    CONSTRAINT ck_gym_membresia_precio
        CHECK (precio_contratado >= 0)
);

CREATE INDEX IF NOT EXISTS idx_gym_membresia_tenant
    ON gym_membresia(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_membresia_cliente
    ON gym_membresia(
        tenant_id,
        cliente_id
    );

CREATE INDEX IF NOT EXISTS idx_gym_membresia_vencimiento
    ON gym_membresia(
        tenant_id,
        fecha_fin
    );

CREATE TABLE IF NOT EXISTS gym_pago_membresia (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    membresia_id BIGINT NOT NULL,

    importe NUMERIC(12,2) NOT NULL,

    metodo_pago VARCHAR(30) NOT NULL,

    referencia VARCHAR(100),

    fecha_pago TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    observaciones VARCHAR(500),

    CONSTRAINT fk_gym_pago_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_gym_pago_membresia
        FOREIGN KEY (membresia_id)
        REFERENCES gym_membresia(id),

    CONSTRAINT ck_gym_pago_importe
        CHECK (importe > 0),

    CONSTRAINT ck_gym_pago_metodo
        CHECK (
            metodo_pago IN (
                'EFECTIVO',
                'TARJETA',
                'TRANSFERENCIA',
                'OTRO'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_gym_pago_tenant
    ON gym_pago_membresia(tenant_id);

CREATE INDEX IF NOT EXISTS idx_gym_pago_membresia
    ON gym_pago_membresia(
        tenant_id,
        membresia_id
    );

CREATE INDEX IF NOT EXISTS idx_gym_pago_fecha
    ON gym_pago_membresia(
        tenant_id,
        fecha_pago
    );
