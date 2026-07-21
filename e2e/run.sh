#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

export E2E_BASE_URL=http://localhost
export DATABASE_URL=postgresql://minerva-e2e:e2e-password@localhost:15432/minerva-e2e
export E2E_UID="$(id -u)"
export E2E_GID="$(id -g)"

compose=(docker compose --project-name minerva-e2e --file ../compose.e2e.yml)

cleanup() {
  "${compose[@]}" down --volumes --remove-orphans
}

trap cleanup EXIT
cleanup
"${compose[@]}" up --build --detach --wait postgres backend frontend

"${compose[@]}" run --rm playwright bun x playwright test "$@"
