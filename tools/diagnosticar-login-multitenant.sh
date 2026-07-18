#!/usr/bin/env bash

set -Eeuo pipefail

PROJECT="/home/tonatiuh/commerce-cloud"
OUTPUT="$PROJECT/logs/diagnostico-login-multitenant.txt"

cd "$PROJECT"

mkdir -p logs

: > "$OUTPUT"

titulo() {
    echo >> "$OUTPUT"
    echo "============================================================" >> "$OUTPUT"
    echo "$1" >> "$OUTPUT"
    echo "============================================================" >> "$OUTPUT"
}

echo "============================================================"
echo " COMMERCE CLOUD"
echo " Diagnóstico de login multi-tenant"
echo "============================================================"

titulo "INFORMACIÓN GENERAL"

{
    echo "Fecha:"
    date

    echo
    echo "Proyecto:"
    pwd

    echo
    echo "Rama:"
    git branch --show-current

    echo
    echo "Estado Git:"
    git status --short
} >> "$OUTPUT"

titulo "SECURITY CONFIG"

SECURITY_FILE="$(
    find src/main/java \
        -type f \
        -name 'SecurityConfig.java' \
        | head -n 1
)"

if [ -n "$SECURITY_FILE" ]; then
    echo "Archivo: $SECURITY_FILE" >> "$OUTPUT"
    echo >> "$OUTPUT"
    sed -n '1,320p' "$SECURITY_FILE" >> "$OUTPUT"
else
    echo "NO ENCONTRADO" >> "$OUTPUT"
fi

titulo "ENTIDAD USUARIO"

USUARIO_FILE="$(
    find src/main/java \
        -type f \
        \( -name 'Usuario.java' -o -name 'User.java' \) \
        | head -n 1
)"

if [ -n "$USUARIO_FILE" ]; then
    echo "Archivo: $USUARIO_FILE" >> "$OUTPUT"
    echo >> "$OUTPUT"
    sed -n '1,360p' "$USUARIO_FILE" >> "$OUTPUT"
else
    echo "NO ENCONTRADO" >> "$OUTPUT"
fi

titulo "REPOSITORIO DE USUARIO"

USUARIO_REPOSITORY="$(
    find src/main/java \
        -type f \
        \( \
            -name 'UsuarioRepository.java' \
            -o \
            -name 'UserRepository.java' \
        \) \
        | head -n 1
)"

if [ -n "$USUARIO_REPOSITORY" ]; then
    echo "Archivo: $USUARIO_REPOSITORY" >> "$OUTPUT"
    echo >> "$OUTPUT"
    sed -n '1,260p' "$USUARIO_REPOSITORY" >> "$OUTPUT"
else
    echo "NO ENCONTRADO" >> "$OUTPUT"
fi

titulo "USER DETAILS SERVICE"

USER_DETAILS_FILE="$(
    grep -Ril \
        --include='*.java' \
        'implements UserDetailsService' \
        src/main/java \
        | head -n 1
)"

if [ -n "$USER_DETAILS_FILE" ]; then
    echo "Archivo: $USER_DETAILS_FILE" >> "$OUTPUT"
    echo >> "$OUTPUT"
    sed -n '1,360p' "$USER_DETAILS_FILE" >> "$OUTPUT"
else
    echo "NO ENCONTRADO" >> "$OUTPUT"
fi

titulo "CONTROLADORES DE LOGIN Y DASHBOARD"

grep -RniE \
    --include='*.java' \
    '@GetMapping.*(login|dashboard|inicio|admin|caja|gym)|@RequestMapping.*(login|dashboard|inicio|admin|caja|gym)' \
    src/main/java \
    >> "$OUTPUT" \
    || true

titulo "FORM LOGIN Y REDIRECCIONES"

grep -RniE \
    --include='*.java' \
    'formLogin|defaultSuccessUrl|successHandler|failureUrl|loginPage|logoutSuccessUrl' \
    src/main/java \
    >> "$OUTPUT" \
    || true

titulo "ROLES Y AUTORIDADES"

grep -RniE \
    --include='*.java' \
    'ROLE_|hasRole|hasAuthority|GrantedAuthority|SimpleGrantedAuthority' \
    src/main/java \
    >> "$OUTPUT" \
    || true

titulo "TABLAS RELACIONADAS CON USUARIO"

grep -RniE \
    --include='*.sql' \
    'CREATE TABLE.*usuario|ALTER TABLE.*usuario|tenant_id|username|password' \
    src/main/resources/db/migration \
    >> "$OUTPUT" \
    || true

titulo "TEMPLATE DE LOGIN"

LOGIN_TEMPLATE="$(
    find src/main/resources/templates \
        -type f \
        \( \
            -name 'login.html' \
            -o \
            -name '*login*.html' \
        \) \
        | head -n 1
)"

if [ -n "$LOGIN_TEMPLATE" ]; then
    echo "Archivo: $LOGIN_TEMPLATE" >> "$OUTPUT"
    echo >> "$OUTPUT"
    sed -n '1,380p' "$LOGIN_TEMPLATE" >> "$OUTPUT"
else
    echo "NO ENCONTRADO" >> "$OUTPUT"
fi

titulo "TEMPLATES DE DASHBOARD"

find src/main/resources/templates \
    -type f \
    \( \
        -iname '*dashboard*.html' \
        -o \
        -iname '*inicio*.html' \
        -o \
        -iname '*admin*.html' \
    \) \
    | sort \
    >> "$OUTPUT" \
    || true

titulo "COMPILACIÓN ACTUAL"

if mvn clean compile >> "$OUTPUT" 2>&1; then
    echo >> "$OUTPUT"
    echo "RESULTADO_COMPILACION=BUILD_SUCCESS" >> "$OUTPUT"
else
    echo >> "$OUTPUT"
    echo "RESULTADO_COMPILACION=BUILD_FAILURE" >> "$OUTPUT"

    echo
    echo "ERROR: el proyecto no compiló."
    echo "Revisa:"
    echo "$OUTPUT"
    exit 1
fi

echo
echo "============================================================"
echo " DIAGNÓSTICO GENERADO CORRECTAMENTE"
echo "============================================================"
echo
echo "Archivo:"
echo "$OUTPUT"
echo
echo "Resumen:"
grep -E \
    'Archivo:|NO ENCONTRADO|RESULTADO_COMPILACION' \
    "$OUTPUT" \
    || true
echo
echo "Ahora muestra el archivo con:"
echo
echo "cat $OUTPUT"
