#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
PACKAGE_PATH="src/main/java/com/abarrote/abarroteapi/multitenant/bootstrap"
JAVA_FILE="${PACKAGE_PATH}/TenantBootstrapInitializer.java"
PROPERTIES_FILE="src/main/resources/application.properties"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR=".backups/bootstrap-tenants-${TIMESTAMP}"

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR INSTALANDO BOOTSTRAP DE TENANTS"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR

cd "$PROJECT"

echo "============================================================"
echo " COMMERCE CLOUD"
echo " Bootstrap seguro de tenants iniciales"
echo "============================================================"

if [ ! -f pom.xml ]; then
    echo "ERROR: no existe pom.xml en:"
    pwd
    exit 1
fi

if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "ERROR: no existe:"
    echo "$PROPERTIES_FILE"
    exit 1
fi

if [ ! -f \
    "src/main/java/com/abarrote/abarroteapi/multitenant/repository/TenantRepository.java" ]; then

    echo "ERROR: primero debe instalarse el núcleo multi-tenant."
    exit 1
fi

echo
echo "=== RAMA ==="
git branch --show-current

echo
echo "=== ESTADO INICIAL ==="
git status --short

echo
echo "=== RESPALDO ==="

mkdir -p "$BACKUP_DIR"

cp "$PROPERTIES_FILE" \
   "$BACKUP_DIR/application.properties.bak"

if [ -f "$JAVA_FILE" ]; then
    cp "$JAVA_FILE" \
       "$BACKUP_DIR/TenantBootstrapInitializer.java.bak"
fi

echo "Respaldo:"
echo "$BACKUP_DIR"

mkdir -p "$PACKAGE_PATH"

echo
echo "=== CREANDO INITIALIZER ==="

cat > "$JAVA_FILE" <<'EOF'
package com.abarrote.abarroteapi.multitenant.bootstrap;

import com.abarrote.abarroteapi.multitenant.domain.EstadoTenant;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.domain.TipoNegocio;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Crea tenants iniciales únicamente cuando:
 *
 * commerce.bootstrap.tenants.enabled=true
 *
 * No expone endpoints públicos y no duplica registros porque valida el slug.
 */
@Component
@ConditionalOnProperty(
    name = "commerce.bootstrap.tenants.enabled",
    havingValue = "true"
)
public class TenantBootstrapInitializer
    implements ApplicationRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            TenantBootstrapInitializer.class
        );

    private final TenantRepository tenantRepository;

    public TenantBootstrapInitializer(
        TenantRepository tenantRepository
    ) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        LOGGER.info(
            "Iniciando bootstrap controlado de tenants"
        );

        crearTenantSiNoExiste(
            "Gym Titan Ecatepec",
            "gym-titan",
            TipoNegocio.GYM,
            "PROFESIONAL",
            LocalDate.of(2027, 7, 18)
        );

        crearTenantSiNoExiste(
            "Abarrotes Cloud Principal",
            "abarrotes-principal",
            TipoNegocio.ABARROTES,
            "PROFESIONAL",
            LocalDate.of(2027, 7, 18)
        );

        LOGGER.info(
            "Bootstrap de tenants terminado"
        );
    }

    private void crearTenantSiNoExiste(
        String nombre,
        String slug,
        TipoNegocio tipoNegocio,
        String plan,
        LocalDate fechaVencimiento
    ) {
        if (tenantRepository.existsBySlugIgnoreCase(slug)) {
            LOGGER.info(
                "Tenant existente; no se duplica: {}",
                slug
            );
            return;
        }

        Tenant tenant = new Tenant();
        tenant.setNombre(nombre);
        tenant.setSlug(slug);
        tenant.setTipoNegocio(tipoNegocio);
        tenant.setEstado(EstadoTenant.ACTIVO);
        tenant.setPlan(plan);
        tenant.setFechaVencimiento(fechaVencimiento);
        tenant.setActivo(true);

        Tenant guardado = tenantRepository.save(tenant);

        LOGGER.info(
            "Tenant creado. id={}, slug={}, tipo={}",
            guardado.getId(),
            guardado.getSlug(),
            guardado.getTipoNegocio()
        );
    }
}
EOF

echo "Initializer creado:"
echo "$JAVA_FILE"

echo
echo "=== HABILITANDO BOOTSTRAP LOCAL ==="

if grep -q \
    '^commerce\.bootstrap\.tenants\.enabled=' \
    "$PROPERTIES_FILE"; then

    sed -i \
        's/^commerce\.bootstrap\.tenants\.enabled=.*/commerce.bootstrap.tenants.enabled=true/' \
        "$PROPERTIES_FILE"
else
    cat >> "$PROPERTIES_FILE" <<'EOF'

# ============================================================
# Commerce Cloud - bootstrap inicial multi-tenant
# Desactivar después de confirmar la creación inicial.
# ============================================================
commerce.bootstrap.tenants.enabled=true
EOF
fi

echo
echo "Configuración aplicada:"

grep -n \
    'commerce.bootstrap.tenants.enabled' \
    "$PROPERTIES_FILE"

echo
echo "=== VALIDACIÓN ESTÁTICA ==="

if grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$JAVA_FILE" \
    "$PROPERTIES_FILE"; then

    echo "ERROR: existen marcadores de conflicto Git."
    exit 1
fi

git diff --check

echo
echo "=== COMPILACIÓN ==="

mvn clean compile

echo
echo "============================================================"
echo " BOOTSTRAP INSTALADO CORRECTAMENTE"
echo "============================================================"

echo
echo "Archivo creado:"
echo "$JAVA_FILE"

echo
echo "Propiedad activa:"
grep \
    'commerce.bootstrap.tenants.enabled' \
    "$PROPERTIES_FILE"

echo
echo "Estado Git:"
git status --short

echo
echo "Siguiente paso:"
echo "./tools/matar8080.sh 2>/dev/null || true"
echo "./tools/ejecutar.sh"
