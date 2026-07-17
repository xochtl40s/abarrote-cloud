#!/bin/bash

cd "$(dirname "$0")/.."

echo "===================================="
echo "ABARROTE CLOUD"
echo "Iniciando aplicación..."
echo "===================================="

mvn spring-boot:run
