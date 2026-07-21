-- ============================================================
-- COMMERCE CLOUD II
-- NÚCLEO COMERCIAL SAAS
-- Migración Flyway V106
-- ============================================================
--
-- Esta migración crea la base comercial necesaria para:
--
-- 1. Administrar planes SaaS.
-- 2. Registrar prospectos desde la landing.
-- 3. Gestionar suscripciones de empresas.
-- 4. Registrar acciones en una bitácora.
-- 5. Preparar el proceso de activación de tenants.
--
-- No modifica todavía las tablas tenant ni usuario.
-- ============================================================


-- ============================================================
-- 1. PLANES SAAS
-- ============================================================

CREATE TABLE saas_plan (
    id BIGSERIAL PRIMARY KEY,

    codigo VARCHAR(30) NOT NULL,

    nombre VARCHAR(100) NOT NULL,

    descripcion VARCHAR(500),

    precio_mensual NUMERIC(12,2) NOT NULL DEFAULT 0,

    precio_anual NUMERIC(12,2) NOT NULL DEFAULT 0,

    max_usuarios INTEGER,

    max_sucursales INTEGER,

    max_mesas INTEGER,

    permite_reportes_avanzados BOOLEAN NOT NULL DEFAULT FALSE,

    permite_asistente_ia BOOLEAN NOT NULL DEFAULT FALSE,

    permite_exportacion_excel BOOLEAN NOT NULL DEFAULT FALSE,

    permite_soporte_prioritario BOOLEAN NOT NULL DEFAULT FALSE,

    dias_prueba INTEGER NOT NULL DEFAULT 0,

    activo BOOLEAN NOT NULL DEFAULT TRUE,

    orden_visual INTEGER NOT NULL DEFAULT 0,

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_saas_plan_codigo
        UNIQUE (codigo),

    CONSTRAINT ck_saas_plan_precio_mensual
        CHECK (precio_mensual >= 0),

    CONSTRAINT ck_saas_plan_precio_anual
        CHECK (precio_anual >= 0),

    CONSTRAINT ck_saas_plan_max_usuarios
        CHECK (
            max_usuarios IS NULL
            OR max_usuarios > 0
        ),

    CONSTRAINT ck_saas_plan_max_sucursales
        CHECK (
            max_sucursales IS NULL
            OR max_sucursales > 0
        ),

    CONSTRAINT ck_saas_plan_max_mesas
        CHECK (
            max_mesas IS NULL
            OR max_mesas > 0
        ),

    CONSTRAINT ck_saas_plan_dias_prueba
        CHECK (dias_prueba >= 0)
);


CREATE INDEX idx_saas_plan_activo
    ON saas_plan(activo);


CREATE INDEX idx_saas_plan_orden_visual
    ON saas_plan(orden_visual);


COMMENT ON TABLE saas_plan IS
    'Catálogo comercial de planes disponibles en Commerce Cloud II.';


COMMENT ON COLUMN saas_plan.codigo IS
    'Código único del plan, por ejemplo BASICO, PRO o ENTERPRISE.';


COMMENT ON COLUMN saas_plan.max_usuarios IS
    'Cantidad máxima de usuarios. NULL significa sin límite.';


COMMENT ON COLUMN saas_plan.max_sucursales IS
    'Cantidad máxima de sucursales. NULL significa sin límite.';


COMMENT ON COLUMN saas_plan.max_mesas IS
    'Cantidad máxima de mesas para Restaurante Cloud. NULL significa sin límite.';


-- ============================================================
-- 2. PROSPECTOS COMERCIALES
-- ============================================================

CREATE TABLE saas_prospecto (
    id BIGSERIAL PRIMARY KEY,

    folio VARCHAR(40) NOT NULL,

    nombre_negocio VARCHAR(150) NOT NULL,

    propietario VARCHAR(150) NOT NULL,

    correo VARCHAR(180) NOT NULL,

    whatsapp VARCHAR(30) NOT NULL,

    ciudad VARCHAR(120),

    tipo_negocio VARCHAR(30) NOT NULL,

    numero_mesas INTEGER,

    plan_solicitado_id BIGINT,

    estado VARCHAR(30) NOT NULL DEFAULT 'NUEVO',

    origen VARCHAR(50) NOT NULL DEFAULT 'LANDING',

    observaciones VARCHAR(1000),

    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_contacto TIMESTAMP,

    fecha_pago TIMESTAMP,

    fecha_activacion TIMESTAMP,

    tenant_id BIGINT,

    atendido_por_usuario_id BIGINT,

    CONSTRAINT uk_saas_prospecto_folio
        UNIQUE (folio),

    CONSTRAINT fk_saas_prospecto_plan
        FOREIGN KEY (plan_solicitado_id)
        REFERENCES saas_plan(id),

    CONSTRAINT fk_saas_prospecto_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_saas_prospecto_atendido_por
        FOREIGN KEY (atendido_por_usuario_id)
        REFERENCES usuario(id),

    CONSTRAINT ck_saas_prospecto_tipo_negocio
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

    CONSTRAINT ck_saas_prospecto_estado
        CHECK (
            estado IN (
                'NUEVO',
                'CONTACTADO',
                'EN_NEGOCIACION',
                'PENDIENTE_PAGO',
                'PAGADO',
                'ACTIVADO',
                'RECHAZADO',
                'CANCELADO'
            )
        ),

    CONSTRAINT ck_saas_prospecto_numero_mesas
        CHECK (
            numero_mesas IS NULL
            OR numero_mesas > 0
        )
);


CREATE INDEX idx_saas_prospecto_estado
    ON saas_prospecto(estado);


CREATE INDEX idx_saas_prospecto_fecha_registro
    ON saas_prospecto(fecha_registro);


CREATE INDEX idx_saas_prospecto_correo
    ON saas_prospecto(correo);


CREATE INDEX idx_saas_prospecto_whatsapp
    ON saas_prospecto(whatsapp);


CREATE INDEX idx_saas_prospecto_tipo_negocio
    ON saas_prospecto(tipo_negocio);


CREATE INDEX idx_saas_prospecto_tenant
    ON saas_prospecto(tenant_id);


COMMENT ON TABLE saas_prospecto IS
    'Solicitudes comerciales recibidas desde la landing de Commerce Cloud II.';


COMMENT ON COLUMN saas_prospecto.folio IS
    'Folio comercial único generado al registrar la solicitud.';


COMMENT ON COLUMN saas_prospecto.numero_mesas IS
    'Cantidad solicitada de mesas para Restaurante Cloud.';


COMMENT ON COLUMN saas_prospecto.estado IS
    'Estado del proceso comercial y de activación del prospecto.';


-- ============================================================
-- 3. SUSCRIPCIONES SAAS
-- ============================================================

CREATE TABLE saas_suscripcion (

    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT NOT NULL,

    plan_id BIGINT NOT NULL,

    prospecto_id BIGINT,

    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',

    modalidad VARCHAR(20) NOT NULL DEFAULT 'MENSUAL',

    precio_contratado NUMERIC(12,2) NOT NULL,

    descuento NUMERIC(12,2) NOT NULL DEFAULT 0,

    fecha_inicio DATE NOT NULL,

    fecha_vencimiento DATE NOT NULL,

    fecha_cancelacion DATE,

    renovacion_automatica BOOLEAN NOT NULL DEFAULT FALSE,

    dias_gracia INTEGER NOT NULL DEFAULT 5,

    observaciones VARCHAR(1000),

    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_suscripcion_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_suscripcion_plan
        FOREIGN KEY (plan_id)
        REFERENCES saas_plan(id),

    CONSTRAINT fk_suscripcion_prospecto
        FOREIGN KEY (prospecto_id)
        REFERENCES saas_prospecto(id),

    CONSTRAINT uk_suscripcion_tenant
        UNIQUE (tenant_id),

    CONSTRAINT ck_suscripcion_estado
        CHECK (
            estado IN (
                'PRUEBA',
                'ACTIVA',
                'VENCIDA',
                'SUSPENDIDA',
                'CANCELADA'
            )
        ),

    CONSTRAINT ck_suscripcion_modalidad
        CHECK (
            modalidad IN (
                'MENSUAL',
                'ANUAL'
            )
        ),

    CONSTRAINT ck_suscripcion_precio
        CHECK (precio_contratado >= 0),

    CONSTRAINT ck_suscripcion_descuento
        CHECK (descuento >= 0),

    CONSTRAINT ck_suscripcion_dias_gracia
        CHECK (dias_gracia >= 0)
);


CREATE INDEX idx_suscripcion_estado
    ON saas_suscripcion(estado);


CREATE INDEX idx_suscripcion_vencimiento
    ON saas_suscripcion(fecha_vencimiento);


CREATE INDEX idx_suscripcion_plan
    ON saas_suscripcion(plan_id);


CREATE INDEX idx_suscripcion_tenant
    ON saas_suscripcion(tenant_id);


COMMENT ON TABLE saas_suscripcion IS
'Controla la suscripción vigente de cada empresa SaaS.';


COMMENT ON COLUMN saas_suscripcion.estado IS
'Estado comercial actual de la empresa.';


COMMENT ON COLUMN saas_suscripcion.modalidad IS
'Mensual o anual.';


COMMENT ON COLUMN saas_suscripcion.fecha_vencimiento IS
'Fecha en la que expira el servicio contratado.';


-- ============================================================
-- 4. BITÁCORA DE AUDITORÍA SAAS
-- ============================================================

CREATE TABLE saas_bitacora (
    id BIGSERIAL PRIMARY KEY,

    tenant_id BIGINT,

    prospecto_id BIGINT,

    usuario_id BIGINT,

    accion VARCHAR(80) NOT NULL,

    entidad VARCHAR(80) NOT NULL,

    entidad_id BIGINT,

    descripcion VARCHAR(1000) NOT NULL,

    valor_anterior TEXT,

    valor_nuevo TEXT,

    direccion_ip VARCHAR(80),

    user_agent VARCHAR(500),

    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_saas_bitacora_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenant(id),

    CONSTRAINT fk_saas_bitacora_prospecto
        FOREIGN KEY (prospecto_id)
        REFERENCES saas_prospecto(id),

    CONSTRAINT fk_saas_bitacora_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
);


CREATE INDEX idx_saas_bitacora_fecha
    ON saas_bitacora(fecha_evento);


CREATE INDEX idx_saas_bitacora_accion
    ON saas_bitacora(accion);


CREATE INDEX idx_saas_bitacora_entidad
    ON saas_bitacora(entidad, entidad_id);


CREATE INDEX idx_saas_bitacora_tenant
    ON saas_bitacora(tenant_id);


CREATE INDEX idx_saas_bitacora_prospecto
    ON saas_bitacora(prospecto_id);


CREATE INDEX idx_saas_bitacora_usuario
    ON saas_bitacora(usuario_id);


COMMENT ON TABLE saas_bitacora IS
    'Registro de auditoría para operaciones ejecutadas desde el portal Super Admin.';


COMMENT ON COLUMN saas_bitacora.accion IS
    'Acción ejecutada, por ejemplo ACTIVAR_EMPRESA, CAMBIAR_PLAN o SUSPENDER_TENANT.';


COMMENT ON COLUMN saas_bitacora.entidad IS
    'Tipo de entidad afectada, por ejemplo TENANT, PROSPECTO o SUSCRIPCION.';


COMMENT ON COLUMN saas_bitacora.valor_anterior IS
    'Representación del estado anterior del registro modificado.';


COMMENT ON COLUMN saas_bitacora.valor_nuevo IS
    'Representación del nuevo estado del registro modificado.';

-- ============================================================
-- 5. PLANES INICIALES
-- ============================================================

INSERT INTO saas_plan (
    codigo,
    nombre,
    descripcion,
    precio_mensual,
    precio_anual,
    max_usuarios,
    max_sucursales,
    max_mesas,
    permite_reportes_avanzados,
    permite_asistente_ia,
    permite_exportacion_excel,
    permite_soporte_prioritario,
    dias_prueba,
    activo,
    orden_visual
)
VALUES
(
    'BASICO',
    'Plan Básico',
    'Ideal para pequeños negocios.',
    299.00,
    2990.00,
    3,
    1,
    10,
    FALSE,
    FALSE,
    TRUE,
    FALSE,
    15,
    TRUE,
    1
),
(
    'PRO',
    'Plan Profesional',
    'Plan recomendado para la mayoría de los clientes.',
    699.00,
    6990.00,
    15,
    5,
    40,
    TRUE,
    TRUE,
    TRUE,
    FALSE,
    15,
    TRUE,
    2
),
(
    'ENTERPRISE',
    'Enterprise',
    'Empresas con múltiples sucursales y usuarios.',
    1499.00,
    14990.00,
    NULL,
    NULL,
    NULL,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    30,
    TRUE,
    3
)
ON CONFLICT (codigo)
DO NOTHING;



-- ============================================================
-- FIN MIGRACION V106
-- ============================================================
