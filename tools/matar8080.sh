#!/usr/bin/env bash

set -Eeuo pipefail

PORT="8080"

echo "===================================="
echo "COMMERCE CLOUD"
echo "Deteniendo aplicación en puerto ${PORT}"
echo "===================================="

PIDS="$(fuser "${PORT}/tcp" 2>/dev/null || true)"

if [ -z "$PIDS" ]; then
    echo "No existe ninguna aplicación usando el puerto ${PORT}."
    exit 0
fi

echo "PID detectado:${PIDS}"

fuser -k "${PORT}/tcp" 2>/dev/null || true

sleep 2

if fuser "${PORT}/tcp" >/dev/null 2>&1; then
    echo "ERROR: el puerto ${PORT} continúa ocupado."
    lsof -i ":${PORT}" || true
    exit 1
fi

echo "Aplicación detenida correctamente."
echo "Puerto ${PORT} libre."
