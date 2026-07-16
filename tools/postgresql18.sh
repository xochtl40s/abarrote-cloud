#!/usr/bin/env bash

export PG18_BIN="/usr/lib/postgresql/18/bin"

export PGDUMP="${PG18_BIN}/pg_dump"
export PGRESTORE="${PG18_BIN}/pg_restore"
export PSQL="${PG18_BIN}/psql"

echo "PostgreSQL 18 configurado:"
"${PGDUMP}" --version
"${PGRESTORE}" --version
"${PSQL}" --version
