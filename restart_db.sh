#!/usr/bin/env bash
set -euo pipefail

# Resolve the folder this script lives in, even if called from elsewhere
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DB_NAME="comp3005_a3"
DB_USER="${DB_USER:-postgres}"
PSQL_BIN="${PSQL_BIN:-psql}"

# Prefer explicit SQL_FILE env var; else try ./School.sql then ./sql/School.sql relative to script
if [[ -n "${SQL_FILE-}" ]]; then
  SQL_PATH="$SQL_FILE"
elif [[ -f "$SCRIPT_DIR/School.sql" ]]; then
  SQL_PATH="$SCRIPT_DIR/School.sql"
elif [[ -f "$SCRIPT_DIR/sql/School.sql" ]]; then
  SQL_PATH="$SCRIPT_DIR/sql/School.sql"
else
  echo "ERROR: Could not find School.sql. Looked in:"
  echo "  $SCRIPT_DIR/School.sql"
  echo "  $SCRIPT_DIR/sql/School.sql"
  echo "Tip: run with SQL_FILE=/full/path/to/School.sql ./restart_db.sh"
  exit 1
fi

echo "===> Dropping database '$DB_NAME' (if exists)…"
"$PSQL_BIN" -U "$DB_USER" -c "DROP DATABASE IF EXISTS $DB_NAME;"

echo "===> Creating database '$DB_NAME'…"
"$PSQL_BIN" -U "$DB_USER" -c "CREATE DATABASE $DB_NAME;"

echo "===> Running $SQL_PATH on $DB_NAME…"
"$PSQL_BIN" -U "$DB_USER" -d "$DB_NAME" -f "$SQL_PATH"

echo "===> Done."
