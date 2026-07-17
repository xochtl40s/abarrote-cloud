#!/usr/bin/env bash

set -Eeuo pipefail

# ============================================================
# ABARROTE CLOUD III
# Verificación controlada de primera migración Flyway
# ============================================================

PROJECT_DIR="/home/tonatiuh/abarrote-cloud/backend"
EVIDENCE_DIR="${PROJECT_DIR}/docs/evidencias-flyway"
LOG_FILE="${EVIDENCE_DIR}/arranque-flyway.log"
SUMMARY_FILE="${EVIDENCE_DIR}/resultado-flyway.txt"
COUNTS_BEFORE_FILE="${EVIDENCE_DIR}/conteos-antes.txt"
COUNTS_AFTER_FILE="${EVIDENCE_DIR}/conteos-despues.txt"
HISTORY_FILE="${EVIDENCE_DIR}/flyway-schema-history.txt"
STRUCTURE_FILE="${EVIDENCE_DIR}/estructura-flyway.txt"

EXPECTED_BRANCH="feature/inventario-multisucursal"

PSQL_BIN="${PSQL_BIN:-/usr/lib/postgresql/18/bin/psql}"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-abarrote_db}"
DB_USER="${DB_USER:-postgres}"

APP_PID=""

cleanup() {
    if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" 2>/dev/null; then
        echo
        echo "Deteniendo aplicación con PID ${APP_PID}..."
        kill "${APP_PID}" 2>/dev/null || true
        wait "${APP_PID}" 2>/dev/null || true
    fi
}

trap cleanup EXIT INT TERM

fail() {
    echo
    echo "ERROR: $1"
    echo "Proceso detenido. No hagas commit ni push."
    exit 1
}

print_section() {
    echo
    echo "============================================================"
    echo "$1"
    echo "============================================================"
}

run_psql() {
    PGPASSWORD="${DB_PASSWORD:-}" \
    "${PSQL_BIN}" \
        -X \
        -v ON_ERROR_STOP=1 \
        -h "${DB_HOST}" \
        -p "${DB_PORT}" \
        -U "${DB_USER}" \
        -d "${DB_NAME}" \
        "$@"
}

capture_counts() {
    local output_file="$1"

    run_psql \
        --pset pager=off \
        --tuples-only \
        --no-align \
        --field-separator='|' \
        -c "
            SELECT 'categoria', COUNT(*) FROM public.categoria
            UNION ALL
            SELECT 'producto', COUNT(*) FROM public.producto
            UNION ALL
            SELECT 'sucursal', COUNT(*) FROM public.sucursal
            UNION ALL
            SELECT 'usuario', COUNT(*) FROM public.usuario
            UNION ALL
            SELECT 'inventario_sucursal', COUNT(*)
              FROM public.inventario_sucursal
            UNION ALL
            SELECT 'venta', COUNT(*) FROM public.venta
            UNION ALL
            SELECT 'detalle_venta', COUNT(*)
              FROM public.detalle_venta
            UNION ALL
            SELECT 'movimiento_inventario', COUNT(*)
              FROM public.movimiento_inventario
            ORDER BY 1;
        " > "${output_file}"
}

cd "${PROJECT_DIR}"
mkdir -p "${EVIDENCE_DIR}"

: > "${SUMMARY_FILE}"

print_section "1. VALIDACIÓN DEL ENTORNO"

CURRENT_BRANCH="$(git branch --show-current)"

echo "Proyecto: ${PROJECT_DIR}"
echo "Rama actual: ${CURRENT_BRANCH}"
echo "Rama esperada: ${EXPECTED_BRANCH}"
echo "Base de datos: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "Usuario DB: ${DB_USER}"
echo "Cliente psql: ${PSQL_BIN}"

[[ "${CURRENT_BRANCH}" == "${EXPECTED_BRANCH}" ]] ||
    fail "La rama activa no es ${EXPECTED_BRANCH}."

[[ -x "${PSQL_BIN}" ]] ||
    fail "No existe el cliente PostgreSQL 18 en ${PSQL_BIN}."

"${PSQL_BIN}" --version

print_section "2. ESTADO GIT"

git status --short
git status

print_section "3. ARCHIVOS OBLIGATORIOS"

REQUIRED_FILES=(
    "pom.xml"
    "src/main/resources/application.properties"
    "src/main/resources/application-render.properties"
    "src/main/resources/db/migration/V1__estructura_inicial.sql"
    "src/main/resources/db/migration/V2__normalizar_multisucursal.sql"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [[ ! -f "${file}" ]]; then
        fail "Falta el archivo obligatorio: ${file}"
    fi

    echo "OK: ${file}"
done

print_section "4. CONFIGURACIÓN FLYWAY Y HIBERNATE"

{
    echo "===== pom.xml / Flyway ====="
    grep -n -C 2 -E \
        "flyway-core|flyway-database-postgresql|flyway.version" \
        pom.xml || true

    echo
    echo "===== application.properties ====="
    grep -n -E \
        "spring.flyway|spring.jpa.hibernate.ddl-auto|spring.jpa.open-in-view|spring.datasource" \
        src/main/resources/application.properties || true

    echo
    echo "===== application-render.properties ====="
    grep -n -E \
        "spring.flyway|spring.jpa.hibernate.ddl-auto|spring.jpa.open-in-view|spring.datasource" \
        src/main/resources/application-render.properties || true

    echo
    echo "===== V1 ====="
    sed -n '1,400p' \
        src/main/resources/db/migration/V1__estructura_inicial.sql

    echo
    echo "===== V2 ====="
    sed -n '1,500p' \
        src/main/resources/db/migration/V2__normalizar_multisucursal.sql
} | tee "${STRUCTURE_FILE}"

grep -Eq \
    '^[[:space:]]*spring\.jpa\.hibernate\.ddl-auto[[:space:]]*=[[:space:]]*validate[[:space:]]*$' \
    src/main/resources/application.properties ||
    fail "application.properties no tiene spring.jpa.hibernate.ddl-auto=validate."

grep -Eq \
    '^[[:space:]]*spring\.flyway\.baseline-on-migrate[[:space:]]*=[[:space:]]*true[[:space:]]*$' \
    src/main/resources/application.properties ||
    fail "application.properties no tiene temporalmente baseline-on-migrate=true."

grep -Eq \
    '^[[:space:]]*spring\.jpa\.hibernate\.ddl-auto[[:space:]]*=[[:space:]]*validate[[:space:]]*$' \
    src/main/resources/application-render.properties ||
    fail "application-render.properties no tiene ddl-auto=validate."

grep -Eq \
    '^[[:space:]]*spring\.flyway\.baseline-on-migrate[[:space:]]*=[[:space:]]*true[[:space:]]*$' \
    src/main/resources/application-render.properties ||
    fail "application-render.properties no tiene temporalmente baseline-on-migrate=true."

print_section "5. CONEXIÓN A POSTGRESQL"

run_psql \
    --pset pager=off \
    -c "
        SELECT
            current_database() AS base,
            current_user AS usuario,
            version() AS version;
    "

print_section "6. ESTADO PREVIO DE FLYWAY"

run_psql \
    --pset pager=off \
    -c "
        SELECT to_regclass(
            'public.flyway_schema_history'
        ) AS flyway_schema_history;
    "

print_section "7. CONTEOS ANTES DE FLYWAY"

capture_counts "${COUNTS_BEFORE_FILE}"
cat "${COUNTS_BEFORE_FILE}"

print_section "8. COMPILACIÓN"

if [[ -x "./tools/matar8080.sh" ]]; then
    ./tools/matar8080.sh
else
    echo "Advertencia: no existe tools/matar8080.sh."
fi

mvn clean compile

print_section "9. PRIMER ARRANQUE CON FLYWAY"

: > "${LOG_FILE}"

if [[ -x "./tools/ejecutar.sh" ]]; then
    ./tools/ejecutar.sh > "${LOG_FILE}" 2>&1 &
else
    mvn spring-boot:run > "${LOG_FILE}" 2>&1 &
fi

APP_PID=$!

echo "Aplicación iniciada con PID ${APP_PID}."
echo "Esperando el resultado del arranque..."

STARTED=false
FAILED=false

for attempt in $(seq 1 120); do
    if grep -q \
        "Started AbarroteApiApplication" \
        "${LOG_FILE}"; then

        STARTED=true
        break
    fi

    if grep -Eq \
        "APPLICATION FAILED TO START|BUILD FAILURE|Migration.*failed|FlywayException|Validate failed|Schema-validation" \
        "${LOG_FILE}"; then

        FAILED=true
        break
    fi

    if ! kill -0 "${APP_PID}" 2>/dev/null; then
        FAILED=true
        break
    fi

    sleep 1
done

print_section "10. LOGS RELEVANTES"

grep -Ei \
    "flyway|migration|baseline|schema history|hikari|postgresql|started|failed|exception|error" \
    "${LOG_FILE}" | tail -250 || true

if [[ "${FAILED}" == "true" ]]; then
    echo
    echo "Últimas 250 líneas del log:"
    tail -250 "${LOG_FILE}"
    fail "La aplicación o Flyway falló durante el arranque."
fi

if [[ "${STARTED}" != "true" ]]; then
    echo
    echo "Últimas 250 líneas del log:"
    tail -250 "${LOG_FILE}"
    fail "No se encontró Started AbarroteApiApplication."
fi

print_section "11. FLYWAY_SCHEMA_HISTORY"

run_psql \
    --pset pager=off \
    -c "
        SELECT
            installed_rank,
            version,
            description,
            type,
            script,
            installed_by,
            installed_on,
            execution_time,
            success
        FROM public.flyway_schema_history
        ORDER BY installed_rank;
    " | tee "${HISTORY_FILE}"

print_section "12. VALIDACIÓN AUTOMÁTICA DEL HISTORIAL"

BASELINE_COUNT="$(
    run_psql \
        --tuples-only \
        --no-align \
        -c "
            SELECT COUNT(*)
            FROM public.flyway_schema_history
            WHERE version = '1'
              AND type = 'BASELINE'
              AND success = true;
        " | tr -d '[:space:]'
)"

V2_COUNT="$(
    run_psql \
        --tuples-only \
        --no-align \
        -c "
            SELECT COUNT(*)
            FROM public.flyway_schema_history
            WHERE version = '2'
              AND type = 'SQL'
              AND success = true;
        " | tr -d '[:space:]'
)"

FAILED_MIGRATIONS="$(
    run_psql \
        --tuples-only \
        --no-align \
        -c "
            SELECT COUNT(*)
            FROM public.flyway_schema_history
            WHERE success = false;
        " | tr -d '[:space:]'
)"

[[ "${BASELINE_COUNT}" == "1" ]] ||
    fail "No existe exactamente un BASELINE versión 1 exitoso."

[[ "${V2_COUNT}" == "1" ]] ||
    fail "No existe exactamente una migración SQL versión 2 exitosa."

[[ "${FAILED_MIGRATIONS}" == "0" ]] ||
    fail "Existe al menos una migración fallida."

print_section "13. CONTEOS DESPUÉS DE FLYWAY"

capture_counts "${COUNTS_AFTER_FILE}"
cat "${COUNTS_AFTER_FILE}"

print_section "14. COMPARACIÓN DE CONTEOS"

if diff -u \
    "${COUNTS_BEFORE_FILE}" \
    "${COUNTS_AFTER_FILE}"; then

    echo "OK: Flyway no modificó los conteos existentes."
else
    fail "Los conteos antes y después son diferentes."
fi

print_section "15. VALIDACIONES DE INTEGRIDAD"

run_psql \
    --pset pager=off \
    -c "
        SELECT
            COUNT(*) FILTER (
                WHERE existencia < 0
            ) AS inventarios_negativos,
            COUNT(*) FILTER (
                WHERE stock_minimo < 0
            ) AS stock_minimo_negativo
        FROM public.inventario_sucursal;
    "

run_psql \
    --pset pager=off \
    -c "
        SELECT
            COUNT(*) AS usuarios_sin_sucursal
        FROM public.usuario
        WHERE sucursal_id IS NULL;
    "

run_psql \
    --pset pager=off \
    -c "
        SELECT
            COUNT(*) AS ventas_sin_sucursal
        FROM public.venta
        WHERE sucursal_id IS NULL;
    "

run_psql \
    --pset pager=off \
    -c "
        SELECT
            COUNT(*) AS movimientos_mismo_origen_destino
        FROM public.movimiento_inventario
        WHERE sucursal_origen_id IS NOT NULL
          AND sucursal_destino_id IS NOT NULL
          AND sucursal_origen_id = sucursal_destino_id;
    "

print_section "16. RESUMEN"

{
    echo "ABARROTE CLOUD III - RESULTADO PRIMERA MIGRACIÓN"
    echo
    echo "Fecha: $(date --iso-8601=seconds)"
    echo "Rama: ${CURRENT_BRANCH}"
    echo "Base: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
    echo
    echo "BASELINE versión 1: OK"
    echo "Migración SQL versión 2: OK"
    echo "Migraciones fallidas: 0"
    echo "Conteos antes y después: IGUALES"
    echo "Arranque Spring Boot: OK"
    echo
    echo "Evidencias:"
    echo "- ${LOG_FILE}"
    echo "- ${HISTORY_FILE}"
    echo "- ${COUNTS_BEFORE_FILE}"
    echo "- ${COUNTS_AFTER_FILE}"
    echo "- ${STRUCTURE_FILE}"
} | tee "${SUMMARY_FILE}"

echo
echo "============================================================"
echo "PRIMERA MIGRACIÓN FLYWAY VALIDADA"
echo "============================================================"
echo
echo "Todavía no desactives baseline-on-migrate."
echo "Todavía no hagas commit ni push hasta revisar las evidencias."
