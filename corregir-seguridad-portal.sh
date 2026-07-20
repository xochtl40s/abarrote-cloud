#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
SECURITY_FILE="$PROJECT/src/main/java/com/abarrote/abarroteapi/config/SecurityConfig.java"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
BACKUP_DIR="$PROJECT/.backups/security-portal-$TIMESTAMP"

echo "============================================================"
echo " COMMERCE CLOUD"
echo " Corrección de rutas públicas del portal comercial"
echo "============================================================"

cd "$PROJECT"

if [[ ! -f "$SECURITY_FILE" ]]; then
    echo "ERROR: no existe:"
    echo "$SECURITY_FILE"
    exit 1
fi

mkdir -p "$BACKUP_DIR"
cp "$SECURITY_FILE" "$BACKUP_DIR/SecurityConfig.java"

echo
echo "Respaldo creado en:"
echo "$BACKUP_DIR/SecurityConfig.java"

python3 - "$SECURITY_FILE" <<'PYTHON'
from pathlib import Path
import re
import sys

security_file = Path(sys.argv[1])
text = security_file.read_text(encoding="utf-8")

marker = '"/commerce/**"'

if marker in text:
    print("Las rutas públicas del portal ya están configuradas.")
    sys.exit(0)

public_rule = (
    '.requestMatchers(\n'
    '                            "/",\n'
    '                            "/inicio",\n'
    '                            "/commerce-cloud",\n'
    '                            "/productos",\n'
    '                            "/commerce/**",\n'
    '                            "/css/**",\n'
    '                            "/js/**",\n'
    '                            "/images/**",\n'
    '                            "/favicon.ico"\n'
    '                    ).permitAll()\n'
    '                    '
)

patterns = [
    r'(\.requestMatchers\(\s*"/login")',
    r'(\.requestMatchers\(\s*HttpMethod\.)',
    r'(\.anyRequest\(\)\.authenticated\(\))',
    r'(\.anyRequest\(\)\.hasAnyRole\()',
    r'(\.anyRequest\(\)\.permitAll\(\))',
    r'(\.authenticated\(\)\s*;)',
]

for pattern in patterns:
    match = re.search(pattern, text, flags=re.MULTILINE)

    if match:
        start = match.start()
        text = text[:start] + public_rule + text[start:]
        security_file.write_text(text, encoding="utf-8")

        print("SecurityConfig actualizado correctamente.")
        print("La regla pública fue insertada antes de:")
        print(match.group(0))
        sys.exit(0)

authorize_patterns = [
    r'authorizeHttpRequests\s*\(\s*auth\s*->\s*auth',
    r'authorizeHttpRequests\s*\(\s*authorize\s*->\s*authorize',
    r'authorizeHttpRequests\s*\(\s*requests\s*->\s*requests',
]

for pattern in authorize_patterns:
    match = re.search(pattern, text, flags=re.MULTILINE)

    if match:
        insert_position = match.end()

        insertion = (
            '\n                    '
            + public_rule.strip()
        )

        text = (
            text[:insert_position]
            + insertion
            + text[insert_position:]
        )

        security_file.write_text(text, encoding="utf-8")

        print("SecurityConfig actualizado dentro de authorizeHttpRequests.")
        sys.exit(0)

print("""
ERROR: no fue posible identificar automáticamente la estructura.

No se modificó SecurityConfig.java.

Ejecuta:

sed -n '1,280p' src/main/java/com/abarrote/abarroteapi/config/SecurityConfig.java
""".strip())

sys.exit(1)
PYTHON

echo
echo "Verificando rutas agregadas..."

grep -n -A 12 -B 3 'commerce-cloud' "$SECURITY_FILE"

echo
echo "Validando sintaxis Git..."

git diff --check

echo
echo "Compilando..."

mvn clean compile

echo
echo "============================================================"
echo " CORRECCIÓN TERMINADA"
echo "============================================================"

git status --short
