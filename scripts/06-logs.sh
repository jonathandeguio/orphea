#!/usr/bin/env bash
# =============================================================================
# Orphea Platform — Script 06 : Consultation des logs
# Usage : bash 06-logs.sh [--service boson|frontend|snap|tycho|db|redis|all]
#                         [--tail N] [--follow]
# =============================================================================
set -uo pipefail

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
info() { echo -e "${BLUE}[LOGS]${NC} $*"; }

SERVICE="boson"
TAIL=100
FOLLOW=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --service|-s) SERVICE="$2"; shift 2 ;;
    --tail|-n)    TAIL="$2"; shift 2 ;;
    --follow|-f)  FOLLOW=true; shift ;;
    *)            SERVICE="$1"; shift ;;
  esac
done

FOLLOW_FLAG=""
${FOLLOW} && FOLLOW_FLAG="-f"

logs() {
  local container="$1"
  local label="$2"
  info "=== Logs ${label} (${container}) ==="
  docker logs "${container}" --tail "${TAIL}" ${FOLLOW_FLAG} 2>/dev/null \
    || echo "  Container ${container} introuvable ou non démarré"
  echo ""
}

case "${SERVICE}" in
  boson)
    logs "orphea-boson" "Boson API"
    # Logs Tomcat access logs
    ACCESS_LOG_DIR="${ORPHEA_MOUNT_PATH:-/orphea}/logs/boson/accessLogs"
    if [[ -d "${ACCESS_LOG_DIR}" ]]; then
      info "=== Access logs Tomcat ==="
      ls -lah "${ACCESS_LOG_DIR}/" 2>/dev/null | tail -5
      LAST_LOG=$(ls -t "${ACCESS_LOG_DIR}"/ 2>/dev/null | head -1)
      [[ -n "${LAST_LOG}" ]] && tail -${TAIL} "${ACCESS_LOG_DIR}/${LAST_LOG}"
    fi
    ;;
  frontend)
    logs "orphea-frontend" "Frontend Nginx"
    ;;
  snap)
    logs "orphea-snap" "Snap API"
    logs "orphea-snap-ui" "Snap UI"
    logs "orphea-snap-proxy" "Snap Proxy Nginx"
    ;;
  tycho)
    logs "orphea-tycho" "Tycho App"
    logs "orphea-tycho-worker" "Tycho Worker"
    logs "orphea-tycho-beat" "Tycho Beat"
    ;;
  db)
    logs "orphea-boson-db" "PostgreSQL Boson"
    logs "orphea-snap-db" "PostgreSQL Snap"
    logs "orphea-tycho-db" "PostgreSQL Tycho"
    ;;
  redis)
    logs "orphea-redis" "Redis"
    ;;
  all)
    logs "orphea-boson-db" "PostgreSQL Boson"
    logs "orphea-redis" "Redis"
    logs "orphea-boson" "Boson API"
    logs "orphea-frontend" "Frontend"
    logs "orphea-snap" "Snap API"
    logs "orphea-tycho" "Tycho App"
    logs "orphea-tycho-worker" "Tycho Worker"
    ;;
  *)
    # Essai direct avec le nom donné
    logs "orphea-${SERVICE}" "${SERVICE}"
    ;;
esac
