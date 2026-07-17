#!/bin/bash

echo "===================================="
echo "ABARROTE CLOUD"
echo "Compilando..."
echo "===================================="

cd "$(dirname "$0")/.."

mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "===================================="
    echo "BUILD SUCCESS"
    echo "===================================="
else
    echo ""
    echo "===================================="
    echo "BUILD FAILURE"
    echo "===================================="
fi
