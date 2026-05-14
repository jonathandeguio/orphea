#!/usr/bin/env bash
# =============================================================================
# Orphea Platform — Script 00 : Installation système Ubuntu 22.04 / 24.04 LTS
# Cible : VM Ubuntu propre, exécuté en tant que root ou sudo
# Usage : sudo bash 00-install-system.sh
#
# Chemins :
#   Application : /opt/orphea      (code source / scripts)
#   Données     : /opt/orphea/data (volumes Docker : postgres, redis, frontend…)
#   Config      : /etc/orphea      (saml.yml, certificats…)
# =============================================================================
set -euo pipefail

ORPHEA_INSTALL_DIR="/opt/orphea"
ORPHEA_DATA="${ORPHEA_INSTALL_DIR}/data"
ORPHEA_USER="orphea"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC}    $*"; }
success() { echo -e "${GREEN}[OK]${NC}      $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}    $*"; }
error()   { echo -e "${RED}[ERROR]${NC}   $*"; exit 1; }
section() { echo -e "\n${CYAN}─── $* ───${NC}"; }

[[ $EUID -ne 0 ]] && error "Ce script doit être exécuté en root : sudo bash $0"

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║   Orphea Platform — Installation Ubuntu      ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════╝${NC}"
echo ""

# =============================================================================
section "1/7 — Mise à jour du système"
# =============================================================================
apt-get update -qq
apt-get upgrade -y -qq
apt-get install -y -qq \
  curl wget git unzip tar \
  build-essential libssl-dev \
  ca-certificates gnupg lsb-release \
  apt-transport-https software-properties-common \
  fuse openssl jq net-tools \
  python3-pip \
  vim htop

success "Paquets système installés"

# =============================================================================
section "2/7 — Installation de Docker Engine"
# =============================================================================
if command -v docker &>/dev/null; then
  warn "Docker déjà installé : $(docker --version)"
else
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg

  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
    | tee /etc/apt/sources.list.d/docker.list > /dev/null

  apt-get update -qq
  apt-get install -y -qq \
    docker-ce docker-ce-cli containerd.io docker-compose-plugin

  systemctl enable docker
  systemctl start docker
  success "Docker $(docker --version) installé"
fi

# =============================================================================
section "3/7 — Création de l'utilisateur orphea"
# =============================================================================
if id "${ORPHEA_USER}" &>/dev/null; then
  warn "Utilisateur ${ORPHEA_USER} existe déjà"
else
  useradd -m -s /bin/bash "${ORPHEA_USER}"
  success "Utilisateur ${ORPHEA_USER} créé"
fi

usermod -aG docker "${ORPHEA_USER}"
success "Utilisateur ${ORPHEA_USER} ajouté au groupe docker"

# =============================================================================
section "4/7 — Création de l'arborescence dans /opt/orphea"
# =============================================================================
# Répertoire d'installation principal
mkdir -p "${ORPHEA_INSTALL_DIR}"

# Répertoires de données (volumes Docker)
mkdir -p \
  "${ORPHEA_DATA}/postgres/boson" \
  "${ORPHEA_DATA}/postgres/tycho" \
  "${ORPHEA_DATA}/redis" \
  "${ORPHEA_DATA}/boson/logs/accessLogs" \
  "${ORPHEA_DATA}/boson/data" \
  "${ORPHEA_DATA}/dataset" \
  "${ORPHEA_DATA}/file" \
  "${ORPHEA_DATA}/repositories" \
  "${ORPHEA_DATA}/spark-streaming" \
  "${ORPHEA_DATA}/snap/artifactory" \
  "${ORPHEA_DATA}/snap/logs" \
  "${ORPHEA_DATA}/snap/db/data" \
  "${ORPHEA_DATA}/snap/db/dbscripts" \
  "${ORPHEA_DATA}/tycho/superset_home" \
  "${ORPHEA_DATA}/frontend/build" \
  "${ORPHEA_DATA}/ssl/certs" \
  "${ORPHEA_DATA}/ssl/keys" \
  /etc/orphea

chown -R "${ORPHEA_USER}:${ORPHEA_USER}" "${ORPHEA_INSTALL_DIR}"
chown -R "${ORPHEA_USER}:${ORPHEA_USER}" /etc/orphea
chmod -R 755 "${ORPHEA_INSTALL_DIR}"

success "Arborescence ${ORPHEA_INSTALL_DIR} créée"

# =============================================================================
section "5/7 — Configuration du module FUSE (requis par Snap)"
# =============================================================================
modprobe fuse 2>/dev/null || warn "Module FUSE déjà chargé ou non disponible"
if ! grep -q "^fuse$" /etc/modules 2>/dev/null; then
  echo "fuse" >> /etc/modules
fi
success "FUSE configuré"

# =============================================================================
section "6/7 — Optimisation kernel et limites système"
# =============================================================================
cat > /etc/sysctl.d/99-orphea.conf << 'EOF'
# Orphea platform — kernel tuning
vm.max_map_count=262144
net.core.somaxconn=65535
net.ipv4.tcp_max_syn_backlog=65535
fs.file-max=1048576
EOF
sysctl --system -q
success "Paramètres kernel appliqués (vm.max_map_count, fs.file-max)"

cat > /etc/security/limits.d/orphea.conf << 'EOF'
orphea soft nofile 65536
orphea hard nofile 65536
orphea soft nproc  65536
orphea hard nproc  65536
root   soft nofile 65536
root   hard nofile 65536
EOF
success "Limites ulimit configurées"

# =============================================================================
section "7/7 — Fichier SAML minimal + résumé"
# =============================================================================
if [[ ! -f /etc/orphea/saml.yml ]]; then
  cat > /etc/orphea/saml.yml << 'EOF'
# =============================================================================
# Orphea Platform — Configuration SAML SSO
# platform-default-login : password | saml2
#   - password : authentification locale (par défaut)
#   - saml2    : authentification via un IdP SAML (Azure AD, Okta…)
# Si vous n'utilisez pas le SSO SAML, conservez platform-default-login: password
# =============================================================================
platform-default-login: password
spring:
  security:
    saml2:
      relyingparty:
        registration:
          Orphea-SSO:
            assertingparty:
              singlesignon:
                sign-request: false
                url: https://login.microsoftonline.com/CHANGEME_TENANT_ID/saml2
              entity-id: http://orphea.io
            entity-id: http://orphea.io
            acs:
              location: ${BASE_URL}/api/sso/callback
EOF
  success "Fichier /etc/orphea/saml.yml créé"
fi

# =============================================================================
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   Installation système Orphea terminée       ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════╝${NC}"
echo ""
info "Répertoires créés :"
echo "  Application : ${ORPHEA_INSTALL_DIR}/"
echo "  Données     : ${ORPHEA_DATA}/"
echo "  Config      : /etc/orphea/"
echo ""
info "Prochaines étapes :"
echo "  1. Cloner le repo dans ${ORPHEA_INSTALL_DIR} (si pas déjà fait) :"
echo "     git clone <repo_url> ${ORPHEA_INSTALL_DIR}"
echo ""
echo "  2. Configurer l'environnement :"
echo "     cd ${ORPHEA_INSTALL_DIR}/scripts && bash 01-setup-env.sh"
echo ""
warn "Ports à ouvrir si UFW est actif :"
echo "  ufw allow 80/tcp    # Frontend"
echo "  ufw allow 8080/tcp  # API Boson"
echo "  ufw allow 8082/tcp  # Snap (optionnel)"
echo "  ufw allow 8088/tcp  # Tycho/Superset (optionnel)"
echo ""
