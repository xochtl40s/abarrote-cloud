#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
PORT="8080"

cd "$PROJECT"

echo "===================================="
echo "COMMERCE CLOUD"
echo "Iniciando aplicación..."
echo "===================================="

echo
echo "Directorio:"
pwd

echo
echo "Rama Git:"
git branch --show-current 2>/dev/null || true

echo
echo "Verificando puerto ${PORT}..."

PIDS="$(fuser "${PORT}/tcp" 2>/dev/null || true)"

if [ -n "$PIDS" ]; then
    echo "Existe una aplicación usando el puerto ${PORT}."
    echo "PID detectado:${PIDS}"
    echo "Deteniendo proceso anterior..."

    fuser -k "${PORT}/tcp" 2>/dev/null || true

    sleep 2
fi

if fuser "${PORT}/tcp" >/dev/null 2>&1; then
    echo "ERROR: no fue posible liberar el puerto ${PORT}."
    echo
    echo "Procesos:"
    lsof -i ":${PORT}" || true
    exit 1
fi

echo "Puerto ${PORT} disponible."

echo
echo "Compilando antes de iniciar..."

mvn clean compile

echo
echo "Iniciando Spring Boot..."

exec mvn spring-boot:run
