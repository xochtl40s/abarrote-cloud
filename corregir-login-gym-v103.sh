#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
MIGRATION_DIR="src/main/resources/db/migration"
BOOTSTRAP_FILE="src/main/java/com/abarrote/abarroteapi/multitenant/bootstrap/UsuarioGymBootstrapInitializer.java"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR=".backups/fix-login-gym-v103-${TIMESTAMP}"

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR APLICANDO FIX LOGIN GYM V103"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR

cd "$PROJECT"

echo "============================================================"
echo " COMMERCE CLOUD"
echo " FIX V103 - Usuario Gym sin sucursal heredada"
echo "============================================================"

if [ ! -f pom.xml ]; then
    echo "ERROR: no existe pom.xml."
    exit 1
fi

if [ ! -f "$BOOTSTRAP_FILE" ]; then
    echo "ERROR: no existe:"
    echo "$BOOTSTRAP_FILE"
    exit 1
fi

mkdir -p "$BACKUP_DIR"
mkdir -p "$MIGRATION_DIR"

cp "$BOOTSTRAP_FILE" \
   "$BACKUP_DIR/UsuarioGymBootstrapInitializer.java.bak"

echo
echo "[1/5] Creando migración Flyway V103..."

cat > "$MIGRATION_DIR/V103__permitir_usuario_sin_sucursal.sql" <<'EOF'
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
EOF

echo
echo "[2/5] Reforzando bootstrap idempotente de admingym..."

cat > "$BOOTSTRAP_FILE" <<'EOF'
package com.abarrote.abarroteapi.multitenant.bootstrap;

import com.abarrote.abarroteapi.entity.Usuario;
import com.abarrote.abarroteapi.multitenant.domain.Tenant;
import com.abarrote.abarroteapi.multitenant.repository.TenantRepository;
import com.abarrote.abarroteapi.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(
    name = "commerce.bootstrap.login-multitenant.enabled",
    havingValue = "true"
)
public class UsuarioGymBootstrapInitializer
    implements ApplicationRunner {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(
            UsuarioGymBootstrapInitializer.class
        );

    private final UsuarioRepository usuarioRepository;

    private final TenantRepository tenantRepository;

    public UsuarioGymBootstrapInitializer(
        UsuarioRepository usuarioRepository,
        TenantRepository tenantRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        LOGGER.info(
            "Iniciando bootstrap de login multi-tenant"
        );

        Tenant tenantAbarrotes =
            tenantRepository
                .findBySlugIgnoreCaseAndActivoTrue(
                    "abarrotes-principal"
                )
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe el tenant "
                            + "abarrotes-principal"
                    )
                );

        Tenant tenantGym =
            tenantRepository
                .findBySlugIgnoreCaseAndActivoTrue(
                    "gym-titan"
                )
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe el tenant gym-titan"
                    )
                );

        Usuario admin =
            usuarioRepository
                .findByUsernameIgnoreCase("admin")
                .orElseThrow(
                    () -> new IllegalStateException(
                        "No existe el usuario admin"
                    )
                );

        /*
         * El administrador histórico pertenece al tenant
         * Abarrote Cloud y conserva su sucursal actual.
         */
        admin.setTenant(tenantAbarrotes);
        usuarioRepository.save(admin);

        Usuario adminGym =
            usuarioRepository
                .findByUsernameIgnoreCase("admingym")
                .orElseGet(
                    () -> {
                        Usuario nuevo =
                            new Usuario();

                        nuevo.setUsername("admingym");

                        return nuevo;
                    }
                );

        /*
         * Durante la prueba inicial se reutiliza el hash BCrypt
         * del usuario admin. Por lo tanto, admingym utiliza
         * temporalmente la misma contraseña.
         */
        adminGym.setNombre("Administrador Gym");
        adminGym.setPassword(admin.getPassword());
        adminGym.setRol("ADMIN");
        adminGym.setActivo(true);
        adminGym.setTenant(tenantGym);

        /*
         * Gym Cloud todavía no utiliza las sucursales heredadas
         * de Abarrote Cloud.
         */
        adminGym.setSucursal(null);

        Usuario guardado =
            usuarioRepository.saveAndFlush(adminGym);

        LOGGER.info(
            "Usuario Gym listo: id={}, username={}, "
                + "tenant={}, sucursal=null",
            guardado.getId(),
            guardado.getUsername(),
            tenantGym.getSlug()
        );

        LOGGER.info(
            "Login multi-tenant listo: "
                + "admin -> abarrotes-principal, "
                + "admingym -> gym-titan"
        );
    }
}
EOF

echo
echo "[3/5] Validando marcadores de conflicto..."

if grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    "$BOOTSTRAP_FILE" \
    "$MIGRATION_DIR/V103__permitir_usuario_sin_sucursal.sql"; then

    echo "ERROR: existen conflictos Git."
    exit 1
fi

git diff --check

echo
echo "[4/5] Compilando..."

mvn clean compile

echo
echo "[5/5] Mostrando configuración bootstrap..."

grep -n \
    'commerce.bootstrap.login-multitenant.enabled' \
    src/main/resources/application.properties \
    || true

echo
echo "============================================================"
echo " FIX V103 PREPARADO CORRECTAMENTE"
echo "============================================================"

echo
echo "Migración creada:"
echo "$MIGRATION_DIR/V103__permitir_usuario_sin_sucursal.sql"

echo
echo "Respaldo:"
echo "$BACKUP_DIR"

echo
echo "Estado Git:"
git status --short

echo
echo "Siguiente paso:"
echo "./tools/ejecutar.sh"
