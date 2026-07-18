#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
PORT="8080"
BASE_URL="http://localhost:${PORT}"
LOG_DIR="${PROJECT}/logs"
LOG_FILE="${LOG_DIR}/validacion-gym.log"
PID_FILE="${LOG_DIR}/validacion-gym.pid"
MAX_INTENTOS=90

APP_PID=""

mostrar_error() {
    echo
    echo "============================================================"
    echo " ERROR DURANTE LA VALIDACIÓN GYM"
    echo "============================================================"
    echo "Línea: $1"
    echo "Comando: $2"
    echo
    echo "Últimas líneas del log:"
    if [ -f "$LOG_FILE" ]; then
        tail -n 80 "$LOG_FILE" || true
    fi
}

limpiar() {
    if [ -n "${APP_PID:-}" ]; then
        if kill -0 "$APP_PID" 2>/dev/null; then
            echo
            echo "Deteniendo aplicación temporal PID ${APP_PID}..."
            kill "$APP_PID" 2>/dev/null || true

            for _ in {1..15}; do
                if ! kill -0 "$APP_PID" 2>/dev/null; then
                    break
                fi
                sleep 1
            done

            if kill -0 "$APP_PID" 2>/dev/null; then
                kill -9 "$APP_PID" 2>/dev/null || true
            fi
        fi
    fi

    rm -f "$PID_FILE"
}

trap 'mostrar_error "$LINENO" "$BASH_COMMAND"' ERR
trap limpiar EXIT INT TERM

cd "$PROJECT"

mkdir -p "$LOG_DIR"

: > "$LOG_FILE"

echo "============================================================"
echo " COMMERCE CLOUD GYM"
echo " Validación técnica autónoma"
echo "============================================================"

echo
echo "[1/10] Validando directorio del proyecto..."

if [ ! -f pom.xml ]; then
    echo "ERROR: no existe pom.xml."
    exit 1
fi

if [ ! -d src/main/java/com/abarrote/abarroteapi/gym ]; then
    echo "ERROR: no existe el módulo Gym."
    exit 1
fi

if [ ! -f src/main/resources/db/migration/V101__crear_modulo_gym.sql ]; then
    echo "ERROR: no existe la migración V101."
    exit 1
fi

echo "OK: estructura encontrada."

echo
echo "[2/10] Validando herramientas..."

for comando in java mvn curl git fuser; do
    if ! command -v "$comando" >/dev/null 2>&1; then
        echo "ERROR: no se encontró el comando: $comando"
        exit 1
    fi
done

echo "OK: herramientas disponibles."

echo
echo "[3/10] Revisando conflictos Git..."

if grep -RniE \
    '^(<<<<<<<|=======|>>>>>>>)' \
    src/main/java \
    src/main/resources; then

    echo "ERROR: existen marcadores de conflicto Git."
    exit 1
fi

git diff --check

echo "OK: código sin conflictos."

echo
echo "[4/10] Compilando proyecto..."

mvn clean compile

echo "OK: BUILD SUCCESS."

echo
echo "[5/10] Liberando puerto ${PORT}..."

PIDS="$(fuser "${PORT}/tcp" 2>/dev/null || true)"

if [ -n "$PIDS" ]; then
    echo "Procesos encontrados:${PIDS}"
    fuser -k "${PORT}/tcp" 2>/dev/null || true
    sleep 3
fi

if fuser "${PORT}/tcp" >/dev/null 2>&1; then
    echo "ERROR: el puerto ${PORT} continúa ocupado."
    lsof -i ":${PORT}" 2>/dev/null || true
    exit 1
fi

echo "OK: puerto ${PORT} disponible."

echo
echo "[6/10] Iniciando Spring Boot en segundo plano..."

nohup mvn spring-boot:run \
    > "$LOG_FILE" \
    2>&1 &

APP_PID="$!"

echo "$APP_PID" > "$PID_FILE"

echo "PID temporal: $APP_PID"
echo "Log: $LOG_FILE"

echo
echo "[7/10] Esperando inicio de la aplicación..."

APLICACION_LISTA="false"

for intento in $(seq 1 "$MAX_INTENTOS"); do
    if ! kill -0 "$APP_PID" 2>/dev/null; then
        echo
        echo "ERROR: Spring Boot terminó antes de iniciar."
        echo
        echo "Últimas líneas del log:"
        tail -n 100 "$LOG_FILE"
        exit 1
    fi

    HTTP_CODE="$(
        curl \
            --silent \
            --output /dev/null \
            --write-out '%{http_code}' \
            "${BASE_URL}/login" \
            || true
    )"

    if [ "$HTTP_CODE" != "000" ]; then
        APLICACION_LISTA="true"
        echo
        echo "Aplicación disponible."
        echo "Código HTTP de /login: ${HTTP_CODE}"
        break
    fi

    if grep -q \
        "APPLICATION FAILED TO START" \
        "$LOG_FILE"; then

        echo
        echo "ERROR: Spring Boot reportó fallo de inicio."
        tail -n 100 "$LOG_FILE"
        exit 1
    fi

    printf "."

    sleep 1
done

echo

if [ "$APLICACION_LISTA" != "true" ]; then
    echo "ERROR: la aplicación no inició después de ${MAX_INTENTOS} segundos."
    tail -n 120 "$LOG_FILE"
    exit 1
fi

echo
echo "[8/10] Validando endpoints protegidos..."

LOGIN_CODE="$(
    curl \
        --silent \
        --output /dev/null \
        --write-out '%{http_code}' \
        "${BASE_URL}/login"
)"

TENANT_CODE="$(
    curl \
        --silent \
        --output /dev/null \
        --write-out '%{http_code}' \
        "${BASE_URL}/api/tenants"
)"

GYM_CODE="$(
    curl \
        --silent \
        --output /dev/null \
        --write-out '%{http_code}' \
        "${BASE_URL}/api/gym/gym-titan/clientes"
)"

echo "/login: ${LOGIN_CODE}"
echo "/api/tenants: ${TENANT_CODE}"
echo "/api/gym/gym-titan/clientes: ${GYM_CODE}"

if [ "$LOGIN_CODE" = "000" ]; then
    echo "ERROR: login no accesible."
    exit 1
fi

if [ "$TENANT_CODE" != "302" ] && [ "$TENANT_CODE" != "200" ]; then
    echo "ERROR: respuesta inesperada de /api/tenants: ${TENANT_CODE}"
    exit 1
fi

if [ "$GYM_CODE" != "302" ] && [ "$GYM_CODE" != "200" ]; then
    echo "ERROR: respuesta inesperada del endpoint Gym: ${GYM_CODE}"
    exit 1
fi

echo "OK: seguridad y endpoints responden."

echo
echo "[9/10] Revisando logs de arranque..."

if grep -q \
    "APPLICATION FAILED TO START" \
    "$LOG_FILE"; then

    echo "ERROR: se encontró fallo de aplicación."
    tail -n 100 "$LOG_FILE"
    exit 1
fi

if ! grep -q \
    "Started AbarroteApiApplication" \
    "$LOG_FILE"; then

    echo "ERROR: no se encontró confirmación de inicio."
    tail -n 100 "$LOG_FILE"
    exit 1
fi

echo "OK: Spring Boot inició correctamente."

echo
echo "[10/10] Resumen Git..."

git status --short

echo
echo "============================================================"
echo " VALIDACIÓN COMMERCE CLOUD GYM EXITOSA"
echo "============================================================"

echo
echo "Resultados:"
echo "  BUILD SUCCESS"
echo "  Spring Boot iniciado"
echo "  Puerto ${PORT} funcional"
echo "  Login accesible"
echo "  API Tenant protegida"
echo "  API Gym protegida"
echo "  Migración V101 presente"

echo
echo "Fragmentos relevantes del log:"

grep -E \
    "Flyway|Current version|Migrating schema|Started AbarroteApiApplication|Bootstrap.*Gym|Tenant creado|Tenant existente" \
    "$LOG_FILE" \
    | tail -n 40 \
    || true

echo
echo "La aplicación temporal se detendrá automáticamente."
