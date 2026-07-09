#!/bin/bash

PID=$(lsof -t -i:8080)

if [ -z "$PID" ]; then
    echo "✅ El puerto 8080 ya está libre."
else
    echo "🛑 Terminando proceso con PID: $PID"
    kill -9 "$PID"
    echo "✅ Puerto 8080 liberado."
fi
