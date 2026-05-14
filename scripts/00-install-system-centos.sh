#!/usr/bin/env bash
# =============================================================================
# Orphea Platform — Installation système CentOS 7 / 8 / 9 / Stream
# Cible   : VM CentOS propre, exécuté en root ou via sudo
# Usage   : sudo bash 00-install-system-centos.sh
#
# Ce script installe et configure :
#   - Les paquets système nécessaires (via yum ou dnf selon la version)
#   - Docker Engine CE + Docker Compose plugin
#   - L'utilisateur système "orphea" + le groupe docker
#   - L'arborescence de données /orphea
#   - Le module FUSE (requis par Snap)
#   - SELinux : configuration permissive pour Docker (sans désactiver)
#   - Firewalld : ouverture des ports applicatifs
#   - sysctl kernel tuning (Elasticsearch, Docker, réseau)
#   - Limites système (ulimit nofile/nproc)
#   - Le fichier /etc/orphea/saml.yml (SAML SSO skeleton)
# =============================================================================
set -euo pipefail

# --- Couleurs ---
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; NC='\033[0m'

info()    { echo -e "${BLUE}[INFO]${NC}   $*"; }
success() { echo -e "${GREEN}[OK]${NC}     $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}   $*"; }
error()   { echo -e "${RED}[ERROR]${NC}  $*"; exit 1; }
section() { echo -e "\n${CYAN}══════════════════════════════════════════${NC}"; \
            echo -e "${CYAN}  $*${NC}"; \
            echo -e "${CYAN}══════════════════════════════════════════${NC}"; }

ORPHEA_USER="orphea"
ORPHEA_DATA="/orphea"
REPO_DIR="/opt/orphea/repo"

# =============================================================================
# Pré-requis : root
# =============================================================================
[[ $EUID -ne 0 ]] && error "Ce script doit être exécuté en root : sudo bash $0"

# =============================================================================
# [0] Détection de la version CentOS / RHEL / AlmaLinux / Rocky
# =============================================================================
section "[0/10] Détection du système"

if [[ ! -f /etc/os-release ]]; then
  error "Fichier /etc/os-release introuvable — OS non supporté"
fi

source /etc/os-release
OS_ID="${ID:-unknown}"
OS_VERSION_ID="${VERSION_ID:-0}"
OS_MAJOR="${OS_VERSION_ID%%.*}"
OS_NAME="${NAME:-unknown}"

info "Système détecté : ${OS_NAME} ${VERSION_ID} (${ID})"

# Vérification de la compatibilité
case "${OS_ID}" in
  centos|rhel|almalinux|rocky|ol)
    info "Distribution compatible détectée : ${OS_ID}"
    ;;
  *)
    warn "Distribution '${OS_ID}' non testée — le script est conçu pour CentOS/RHEL/AlmaLinux/Rocky"
    warn "Continuer quand même ? [y/N]"
    read -r CONFIRM
    [[ "${CONFIRM}" != "y" && "${CONFIRM}" != "Y" ]] && exit 0
    ;;
esac

# Choix du gestionnaire de paquets
if [[ "${OS_MAJOR}" == "7" ]]; then
  PKG_MGR="yum"
  info "Gestionnaire de paquets : yum (CentOS/RHEL 7)"
  warn "╔══════════════════════════════════════════════════════════╗"
  warn "║ CentOS 7 est en fin de vie depuis juin 2024              ║"
  warn "║ Migration vers CentOS Stream 9 / AlmaLinux / Rocky       ║"
  warn "║ fortement recommandée pour un usage en production         ║"
  warn "╚══════════════════════════════════════════════════════════╝"
elif [[ "${OS_MAJOR}" == "8" ]]; then
  PKG_MGR="dnf"
  info "Gestionnaire de paquets : dnf (CentOS/RHEL/Stream 8)"
  if echo "${OS_NAME}" | grep -qi "centos linux 8"; then
    warn "CentOS Linux 8 (non-Stream) est en fin de vie depuis déc. 2021"
    warn "Utilisez CentOS Stream 8, AlmaLinux 8 ou Rocky 8"
  fi
elif [[ "${OS_MAJOR}" == "9" ]]; then
  PKG_MGR="dnf"
  info "Gestionnaire de paquets : dnf (CentOS/RHEL/Stream 9)"
else
  warn "Version majeure '${OS_MAJOR}' non reconnue — utilisation de dnf par défaut"
  PKG_MGR="dnf"
fi

success "Configuration : ${PKG_MGR} sur ${OS_NAME} ${OS_VERSION_ID}"

# =============================================================================
# [1] Dépôt EPEL + mise à jour du système
# =============================================================================
section "[1/10] Dépôt EPEL et mise à jour système"

# Installer EPEL (nécessaire pour jq, htop, python3-pip sur CentOS 7)
if ! rpm -q epel-release &>/dev/null; then
  if [[ "${OS_MAJOR}" == "7" ]]; then
    yum install -y epel-release
  elif [[ "${OS_MAJOR}" == "8" ]]; then
    dnf install -y epel-release
    # Sur CentOS Stream 8 : activer PowerTools pour certaines dépendances
    dnf config-manager --set-enabled powertools 2>/dev/null || \
    dnf config-manager --set-enabled crb         2>/dev/null || \
    true
  elif [[ "${OS_MAJOR}" == "9" ]]; then
    dnf install -y epel-release
    # CRB (CodeReady Builder) remplace PowerTools sur RHEL9/Stream9
    dnf config-manager --set-enabled crb 2>/dev/null || true
  fi
  success "Dépôt EPEL installé"
else
  warn "EPEL déjà installé"
fi

# Mise à jour du système
info "Mise à jour du système (peut prendre quelques minutes)..."
${PKG_MGR} update -y -q
success "Système mis à jour"

# =============================================================================
# [2] Paquets système de base
# =============================================================================
section "[2/10] Installation des paquets système"

# Paquets communs à toutes versions
COMMON_PKGS=(
  curl wget git unzip tar
  openssl openssl-devel
  gcc gcc-c++ make
  jq net-tools bind-utils
  vim htop
  ca-certificates
  fuse fuse-libs
)

# Paquets spécifiques par version
if [[ "${OS_MAJOR}" == "7" ]]; then
  VERSION_PKGS=(
    yum-utils
    device-mapper-persistent-data
    lvm2
    libpq-devel
    python3
    python3-pip
    python3-setuptools
  )
elif [[ "${OS_MAJOR}" == "8" ]]; then
  VERSION_PKGS=(
    dnf-utils
    dnf-plugins-core
    libpq-devel
    python3
    python3-pip
    python3-setuptools
    python3-devel
  )
else
  # CentOS 9 / Stream 9 / RHEL 9
  VERSION_PKGS=(
    dnf-utils
    dnf-plugins-core
    libpq-devel
    python3
    python3-pip
    python3-setuptools
    python3-devel
  )
fi

info "Installation des paquets communs..."
${PKG_MGR} install -y -q "${COMMON_PKGS[@]}"

info "Installation des paquets spécifiques ${OS_NAME} ${OS_MAJOR}..."
${PKG_MGR} install -y -q "${VERSION_PKGS[@]}"

success "Paquets système installés"

# Vérifications de version
info "Versions installées :"
echo "  git     : $(git --version 2>/dev/null | head -1)"
echo "  python3 : $(python3 --version 2>/dev/null)"
echo "  openssl : $(openssl version 2>/dev/null)"

# =============================================================================
# [3] Installation de Docker Engine CE
# =============================================================================
section "[3/10] Installation de Docker Engine CE"

if command -v docker &>/dev/null; then
  warn "Docker déjà installé : $(docker --version)"
  warn "Vérification de Docker Compose plugin..."
  if ! docker compose version &>/dev/null; then
    info "Installation du plugin Docker Compose manquant..."
    ${PKG_MGR} install -y docker-compose-plugin 2>/dev/null || \
      warn "docker-compose-plugin non disponible — voir étape manuelle ci-dessous"
  fi
else
  info "Ajout du dépôt Docker CE pour CentOS ${OS_MAJOR}..."

  if [[ "${OS_MAJOR}" == "7" ]]; then
    yum-config-manager \
      --add-repo https://download.docker.com/linux/centos/docker-ce.repo

    info "Installation de Docker CE sur CentOS 7..."
    yum install -y \
      docker-ce \
      docker-ce-cli \
      containerd.io \
      docker-compose-plugin

  else
    # CentOS 8/9 / Stream / RHEL 8/9
    # Supprimer podman/buildah natifs qui conflictuent avec Docker CE
    info "Suppression des paquets conflictuels (podman, buildah)..."
    ${PKG_MGR} remove -y podman buildah 2>/dev/null || true

    dnf config-manager \
      --add-repo https://download.docker.com/linux/centos/docker-ce.repo

    info "Installation de Docker CE sur ${OS_NAME} ${OS_MAJOR}..."
    dnf install -y \
      docker-ce \
      docker-ce-cli \
      containerd.io \
      docker-buildx-plugin \
      docker-compose-plugin
  fi

  # Activation et démarrage de Docker
  systemctl enable docker
  systemctl start docker

  # Vérification
  if docker run --rm hello-world &>/dev/null; then
    success "Docker Engine opérationnel : $(docker --version)"
    docker rmi hello-world &>/dev/null || true
  else
    warn "Docker installé mais le test hello-world a échoué — vérifiez SELinux ou les groupes"
  fi
fi

# Vérification Docker Compose
if docker compose version &>/dev/null; then
  success "Docker Compose : $(docker compose version)"
else
  error "Docker Compose plugin non disponible — installation Docker incomplète"
fi

# =============================================================================
# [4] Utilisateur système orphea
# =============================================================================
section "[4/10] Utilisateur système orphea"

if id "${ORPHEA_USER}" &>/dev/null; then
  warn "L'utilisateur ${ORPHEA_USER} existe déjà"
else
  useradd -m -s /bin/bash "${ORPHEA_USER}"
  success "Utilisateur ${ORPHEA_USER} créé"
fi

# Ajout au groupe docker
usermod -aG docker "${ORPHEA_USER}"
success "Utilisateur ${ORPHEA_USER} ajouté au groupe docker"

# Répertoire repo
mkdir -p "${REPO_DIR}"
chown -R "${ORPHEA_USER}:${ORPHEA_USER}" /opt/orphea
success "Répertoire repo créé : ${REPO_DIR}"

# =============================================================================
# [5] Arborescence de données /orphea
# =============================================================================
section "[5/10] Arborescence de données /orphea"

mkdir -p \
  "${ORPHEA_DATA}/boson/logs/accessLogs" \
  "${ORPHEA_DATA}/boson/data" \
  "${ORPHEA_DATA}/snap/artifactory" \
  "${ORPHEA_DATA}/snap/logs" \
  "${ORPHEA_DATA}/snap/db/data" \
  "${ORPHEA_DATA}/snap/db/dbscripts" \
  "${ORPHEA_DATA}/tycho/superset_home" \
  "${ORPHEA_DATA}/postgres/boson" \
  "${ORPHEA_DATA}/postgres/tycho" \
  "${ORPHEA_DATA}/redis" \
  "${ORPHEA_DATA}/ssl/certs" \
  "${ORPHEA_DATA}/ssl/keys" \
  /etc/orphea

chown -R "${ORPHEA_USER}:${ORPHEA_USER}" "${ORPHEA_DATA}"
chmod -R 755 "${ORPHEA_DATA}"
chown root:root /etc/orphea
chmod 755 /etc/orphea

success "Arborescence /orphea créée"
info "Répertoires créés :"
find "${ORPHEA_DATA}" -maxdepth 3 -type d | sed 's|^|    |'

# =============================================================================
# [6] Module FUSE (requis par Snap)
# =============================================================================
section "[6/10] Module FUSE"

# Charger fuse immédiatement
modprobe fuse 2>/dev/null && success "Module fuse chargé" || warn "Module fuse déjà actif ou non disponible"

# Persistance au démarrage
if [[ ! -f /etc/modules-load.d/fuse.conf ]]; then
  echo "fuse" > /etc/modules-load.d/fuse.conf
  success "fuse ajouté à /etc/modules-load.d/fuse.conf (chargement auto au boot)"
fi

# Vérification
if [[ -c /dev/fuse ]]; then
  success "/dev/fuse disponible (requis par Snap)"
else
  warn "/dev/fuse absent — Snap pourrait ne pas démarrer"
  warn "Vérifiez : ls -la /dev/fuse et modprobe fuse"
fi

# =============================================================================
# [7] SELinux — Configuration sans désactivation
# =============================================================================
section "[7/10] Configuration SELinux"

SELINUX_STATUS=$(getenforce 2>/dev/null || echo "Disabled")
info "SELinux status : ${SELINUX_STATUS}"

if [[ "${SELINUX_STATUS}" == "Disabled" ]]; then
  warn "SELinux est désactivé sur ce système — aucune configuration SELinux requise"

elif [[ "${SELINUX_STATUS}" == "Enforcing" || "${SELINUX_STATUS}" == "Permissive" ]]; then

  info "Configuration SELinux pour Docker et Orphea..."

  # --- Vérifier que policycoreutils-python est disponible ---
  if [[ "${OS_MAJOR}" == "7" ]]; then
    ${PKG_MGR} install -y -q policycoreutils-python 2>/dev/null || true
  else
    ${PKG_MGR} install -y -q policycoreutils-python-utils 2>/dev/null || \
    ${PKG_MGR} install -y -q python3-policycoreutils 2>/dev/null || true
  fi

  # --- Booléens SELinux pour Docker ---
  # Permet aux containers de gérer les cgroups
  setsebool -P container_manage_cgroup 1 2>/dev/null && \
    success "SELinux: container_manage_cgroup = on" || \
    warn "SELinux: impossible de positionner container_manage_cgroup"

  # Permet l'utilisation de FUSE dans les containers (requis par Snap)
  setsebool -P use_fusefs_home_dirs 1 2>/dev/null && \
    success "SELinux: use_fusefs_home_dirs = on" || \
    warn "SELinux: impossible de positionner use_fusefs_home_dirs"

  # --- Contextes SELinux pour les volumes Docker ---
  info "Application des contextes SELinux sur /orphea..."

  if command -v semanage &>/dev/null; then
    # Contexte container_file_t : accès R/W depuis les containers Docker
    semanage fcontext -a -t container_file_t "/orphea(/.*)?" 2>/dev/null || \
    semanage fcontext -m -t container_file_t "/orphea(/.*)?" 2>/dev/null || \
    warn "semanage fcontext : entrée existante ou erreur — tentative de restorecon"
    restorecon -Rv /orphea 2>/dev/null | tail -5 || true
    success "Contexte SELinux container_file_t appliqué sur /orphea"
  else
    warn "semanage non disponible — application du contexte avec chcon"
    chcon -Rt svirt_sandbox_file_t /orphea 2>/dev/null && \
      success "chcon svirt_sandbox_file_t appliqué sur /orphea" || \
      warn "chcon échoué — les volumes Docker pourraient être refusés par SELinux"
  fi

  # Contexte pour /etc/orphea (lecture seule par les containers)
  if command -v semanage &>/dev/null; then
    semanage fcontext -a -t container_file_t "/etc/orphea(/.*)?" 2>/dev/null || \
    semanage fcontext -m -t container_file_t "/etc/orphea(/.*)?" 2>/dev/null || true
    restorecon -Rv /etc/orphea 2>/dev/null || true
  else
    chcon -Rt svirt_sandbox_file_t /etc/orphea 2>/dev/null || true
  fi

  # --- Politique pour conteneur Snap (privileged + seccomp:unconfined) ---
  # Docker en mode privileged contourne déjà la plupart des restrictions SELinux
  # Le drapeau ":z" ou ":Z" dans les volumes docker-compose permet aussi
  # d'appliquer automatiquement le bon contexte SELinux

  success "Configuration SELinux terminée"
  info "Statut final SELinux : $(getenforce)"
  info ""
  warn "IMPORTANT — Labels SELinux sur les volumes Docker :"
  warn "  Si un container refuse d'accéder à /orphea, ajoutez ':z' aux volumes"
  warn "  dans docker-compose (ex: /orphea/snap:/orphea/snap:z)"
  warn "  ':z'  = contexte partagé entre containers"
  warn "  ':Z'  = contexte privé (un seul container)"

else
  warn "Statut SELinux inconnu : ${SELINUX_STATUS}"
fi

# =============================================================================
# [8] Firewalld — Ouverture des ports applicatifs
# =============================================================================
section "[8/10] Configuration du pare-feu (firewalld)"

if systemctl is-active firewalld &>/dev/null; then
  info "firewalld actif — configuration des règles"
  FIREWALL_ACTIVE=true
elif systemctl is-enabled firewalld &>/dev/null; then
  info "firewalld activé mais non démarré — démarrage..."
  systemctl start firewalld
  FIREWALL_ACTIVE=true
else
  warn "firewalld non actif — vérification d'iptables..."
  if command -v iptables &>/dev/null; then
    warn "iptables détecté — configuration manuelle des ports requise"
    info "Commandes iptables équivalentes :"
    echo "  iptables -A INPUT -p tcp --dport 80   -j ACCEPT  # Frontend"
    echo "  iptables -A INPUT -p tcp --dport 8080 -j ACCEPT  # Boson API"
    echo "  iptables -A INPUT -p tcp --dport 8082 -j ACCEPT  # Snap"
    echo "  iptables -A INPUT -p tcp --dport 8088 -j ACCEPT  # Tycho"
  fi
  FIREWALL_ACTIVE=false
fi

if [[ "${FIREWALL_ACTIVE}" == "true" ]]; then
  # Ports applicatifs Orphea
  declare -A PORTS=(
    ["80/tcp"]="Frontend React (Nginx)"
    ["8080/tcp"]="Boson API (Spring Boot)"
    ["8082/tcp"]="Snap — Artifact Manager (Nginx proxy)"
    ["8088/tcp"]="Tycho — Apache Superset BI"
  )

  for PORT_PROTO in "${!PORTS[@]}"; do
    SERVICE_NAME="${PORTS[$PORT_PROTO]}"
    if ! firewall-cmd --query-port="${PORT_PROTO}" --permanent &>/dev/null; then
      firewall-cmd --permanent --add-port="${PORT_PROTO}"
      success "Port ouvert : ${PORT_PROTO} (${SERVICE_NAME})"
    else
      warn "Port déjà ouvert : ${PORT_PROTO} (${SERVICE_NAME})"
    fi
  done

  # Autoriser le trafic interne Docker (réseau bridge orphea-network)
  if ! firewall-cmd --query-zone=trusted --query-interface=docker0 --permanent &>/dev/null 2>&1; then
    firewall-cmd --permanent --zone=trusted --add-interface=docker0 2>/dev/null || true
    info "Interface docker0 ajoutée à la zone trusted"
  fi

  # Masquerade pour que les containers puissent accéder à Internet (build Docker)
  firewall-cmd --permanent --add-masquerade 2>/dev/null || true

  # Rechargement des règles
  firewall-cmd --reload
  success "firewalld rechargé avec les nouvelles règles"

  info "Règles actives :"
  firewall-cmd --list-ports 2>/dev/null | tr ' ' '\n' | sed 's|^|    |'
fi

# =============================================================================
# [9] Paramètres kernel et limites système
# =============================================================================
section "[9/10] Kernel tuning et limites système"

# --- sysctl ---
cat > /etc/sysctl.d/99-orphea.conf << 'EOF'
# Orphea Platform — kernel tuning
# Requis par Elasticsearch (ELK stack optionnel)
vm.max_map_count = 262144

# Réseau
net.core.somaxconn           = 65535
net.ipv4.tcp_max_syn_backlog = 65535
net.ipv4.ip_forward          = 1

# Docker networking
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1

# Fichiers
fs.file-max = 1048576
fs.inotify.max_user_watches  = 524288
fs.inotify.max_user_instances = 512
EOF

# Charger le module br_netfilter pour que les paramètres bridge s'appliquent
modprobe br_netfilter 2>/dev/null || true
if [[ ! -f /etc/modules-load.d/br_netfilter.conf ]]; then
  echo "br_netfilter" > /etc/modules-load.d/br_netfilter.conf
fi

sysctl --system -q 2>/dev/null | grep -E "orphea|max_map|somaxconn|ip_forward" || true
success "Paramètres sysctl appliqués (/etc/sysctl.d/99-orphea.conf)"

# --- ulimits ---
cat > /etc/security/limits.d/orphea.conf << 'EOF'
# Orphea Platform — limites de fichiers et processus
orphea  soft  nofile  65536
orphea  hard  nofile  65536
orphea  soft  nproc   65536
orphea  hard  nproc   65536
root    soft  nofile  65536
root    hard  nofile  65536
EOF

success "Limites ulimit configurées (/etc/security/limits.d/orphea.conf)"

# Sur CentOS 7, pam_limits peut nécessiter une activation explicite
if [[ "${OS_MAJOR}" == "7" ]]; then
  if ! grep -q "pam_limits" /etc/pam.d/common-session 2>/dev/null; then
    # CentOS 7 active pam_limits via /etc/pam.d/system-auth par défaut — ok
    info "CentOS 7 : pam_limits actif via system-auth (défaut)"
  fi
fi

# --- Swapoff (optionnel, recommandé pour performance Java/Spark) ---
SWAP_TOTAL=$(free -m | awk '/^Swap:/{print $2}')
if [[ "${SWAP_TOTAL}" -gt 0 ]]; then
  warn "Swap détecté (${SWAP_TOTAL} Mo) — désactivation recommandée pour Spark/Java"
  warn "Pour désactiver maintenant : swapoff -a && sed -i '/swap/d' /etc/fstab"
  warn "(non exécuté automatiquement pour ne pas perdre de données)"
fi

# =============================================================================
# [10] Fichier SAML SSO minimal
# =============================================================================
section "[10/10] Configuration SAML et vérifications finales"

if [[ ! -f /etc/orphea/saml.yml ]]; then
  # Génération des certificats auto-signés pour le SP SAML2
  # (requis même si SAML non utilisé — Spring refuse de démarrer sans le bean)
  info "Génération des certificats SAML auto-signés..."
  mkdir -p /etc/orphea

  if command -v openssl &>/dev/null; then
    openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 \
      -out /etc/orphea/saml-sp.key 2>/dev/null
    openssl req -new -x509 -key /etc/orphea/saml-sp.key \
      -out /etc/orphea/saml-sp.crt -days 3650 \
      -subj "/CN=orphea-saml-sp" 2>/dev/null
    # Certificat IdP fictif (identique au SP — à remplacer par le vrai cert IdP)
    cp /etc/orphea/saml-sp.crt /etc/orphea/saml-idp.crt
    chmod 600 /etc/orphea/saml-sp.key
    chmod 644 /etc/orphea/saml-sp.crt /etc/orphea/saml-idp.crt
    success "Certificats SAML générés dans /etc/orphea/"
  else
    warn "openssl absent — créez manuellement /etc/orphea/saml-sp.key et saml-sp.crt"
  fi

  # Fichier saml.yml squelette — nécessaire pour que Spring crée le bean
  # RelyingPartyRegistrationRepository (même sans IdP réel configuré).
  # Pour activer SAML SSO, remplacez les valeurs asserting-party par
  # celles de votre fournisseur d'identité.
  cat > /etc/orphea/saml.yml << 'SAML_EOF'
# =============================================================================
# Orphea — Configuration SAML2 SSO
# Référencé par : SAML2_SSO_CONFIG=file:/etc/orphea/saml.yml
#
# Mode d'authentification par défaut : local (login/password)
# Pour activer le SAML SSO, configurez la section asserting-party avec
# les métadonnées de votre fournisseur d'identité (Azure AD, Okta, etc.)
# =============================================================================

platform-default-login: password

spring:
  security:
    saml2:
      relyingparty:
        registration:
          orphea:
            entity-id: "{baseUrl}/saml2/service-provider-metadata/{registrationId}"
            signing:
              credentials:
                - private-key-location: file:/etc/orphea/saml-sp.key
                  certificate-location: file:/etc/orphea/saml-sp.crt
            asserting-party:
              # --- À REMPLACER par votre IdP réel ---
              entity-id: https://idp.example.com
              single-sign-on:
                url: https://idp.example.com/sso/saml
                binding: POST
              verification:
                credentials:
                  - certificate-location: file:/etc/orphea/saml-idp.crt
SAML_EOF
  chmod 644 /etc/orphea/saml.yml
  success "Fichier /etc/orphea/saml.yml créé"
else
  # Vérifier que platform-default-login est présent (ancienne version minimale)
  if ! grep -q "platform-default-login" /etc/orphea/saml.yml; then
    warn "/etc/orphea/saml.yml incomplet — ajout de platform-default-login: password"
    sed -i '1s/^/platform-default-login: password\n\n/' /etc/orphea/saml.yml
  fi
  info "/etc/orphea/saml.yml déjà présent"
fi

# =============================================================================
# Vérifications finales
# =============================================================================
echo ""
info "=== Vérification de l'installation ==="
echo ""

check() {
  local label="$1"
  local cmd="$2"
  if eval "${cmd}" &>/dev/null; then
    success "${label}"
  else
    warn "${label} — VÉRIFIER MANUELLEMENT"
  fi
}

check "Docker Engine"           "docker --version"
check "Docker Compose plugin"   "docker compose version"
check "Utilisateur orphea"      "id orphea"
check "orphea dans groupe docker" "id orphea | grep -q docker"
check "/orphea accessible"      "test -d /orphea"
check "/dev/fuse disponible"    "test -c /dev/fuse"
check "/etc/orphea/saml.yml"    "test -f /etc/orphea/saml.yml"
check "vm.max_map_count=262144" "sysctl vm.max_map_count | grep -q 262144"
check "ip_forward=1"            "sysctl net.ipv4.ip_forward | grep -q 1"

if [[ "${FIREWALL_ACTIVE:-false}" == "true" ]]; then
  check "Port 80 ouvert"   "firewall-cmd --query-port=80/tcp"
  check "Port 8080 ouvert" "firewall-cmd --query-port=8080/tcp"
  check "Port 8082 ouvert" "firewall-cmd --query-port=8082/tcp"
  check "Port 8088 ouvert" "firewall-cmd --query-port=8088/tcp"
fi

# =============================================================================
# Résumé final
# =============================================================================
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   Installation système Orphea terminée sur ${OS_NAME} ${OS_MAJOR}${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
info "Répertoire du repo à remplir    : ${REPO_DIR}"
info "Répertoire des données          : ${ORPHEA_DATA}"
info "Configuration SAML              : /etc/orphea/saml.yml"
info "Utilisateur applicatif          : ${ORPHEA_USER}"
echo ""
warn "Prochaines étapes — dans l'ordre :"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  ÉTAPE 1 — Copier le repo Orphea sur le serveur                    │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "  Depuis votre poste local :"
echo "    scp -r /chemin/vers/Orphea_repo ${ORPHEA_USER}@SERVEUR:${REPO_DIR}/"
echo "  Ou via Git :"
echo "    git clone <url-du-repo> ${REPO_DIR}"
echo "    chown -R ${ORPHEA_USER}:${ORPHEA_USER} ${REPO_DIR}"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  ÉTAPE 2 — Installer les composants techniques (runtimes)           │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "  Installe : Java 11, Gradle, Node.js 18, Yarn, Python 3.8/3.9,"
echo "             PostgreSQL 16, Redis, Nginx, kubectl, Helm"
echo ""
echo "    sudo bash ${REPO_DIR}/scripts/install-components-centos.sh"
echo "    # ou via le point d'entrée unifié :"
echo "    sudo bash ${REPO_DIR}/scripts/orphea-centos.sh components"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  ÉTAPE 3 — Configurer les variables d'environnement (secrets)       │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "    sudo -u ${ORPHEA_USER} bash ${REPO_DIR}/scripts/01-setup-env.sh"
echo "    # Génère : ${REPO_DIR}/scripts/.env.orphea"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  ÉTAPE 4 — Builder les images Docker (15–40 min)                    │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "    sudo -u ${ORPHEA_USER} bash ${REPO_DIR}/scripts/02-build.sh all"
echo "    # ou : sudo -u ${ORPHEA_USER} bash ${REPO_DIR}/scripts/orphea-centos.sh build all"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  ÉTAPE 5 — Démarrer la plateforme                                   │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "    sudo -u ${ORPHEA_USER} bash ${REPO_DIR}/scripts/03-start-centos.sh all"
echo "    # ou : sudo -u ${ORPHEA_USER} bash ${REPO_DIR}/scripts/orphea-centos.sh start all"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  ÉTAPE 6 — Vérifier la santé de la plateforme                       │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "    bash ${REPO_DIR}/scripts/05-healthcheck.sh"
echo "    # ou : bash ${REPO_DIR}/scripts/orphea-centos.sh health"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  OPTIONNEL — SELinux : vérifier les refus après démarrage           │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "    ausearch -m avc -ts recent | audit2why"
echo "    bash ${REPO_DIR}/scripts/orphea-centos.sh selinux"
echo ""
echo "  ┌─────────────────────────────────────────────────────────────────────┐"
echo "  │  OPTIONNEL — Démarrage automatique via systemd                      │"
echo "  └─────────────────────────────────────────────────────────────────────┘"
echo "    sudo cp ${REPO_DIR}/scripts/orphea-platform-centos.service /etc/systemd/system/orphea-platform.service"
echo "    sudo cp ${REPO_DIR}/scripts/orphea-snap-centos.service     /etc/systemd/system/orphea-snap.service"
echo "    sudo systemctl daemon-reload"
echo "    sudo systemctl enable orphea-platform orphea-snap"
echo ""

# Avertissement redémarrage si kernel mis à jour
if [[ -f /var/run/reboot-required ]] || \
   rpm -qa --last kernel 2>/dev/null | head -1 | grep -q "$(uname -r)" | grep -v "$(uname -r)"; then
  warn "Un redémarrage peut être nécessaire si le kernel a été mis à jour."
  warn "Vérifiez : needs-restarting -r (paquet: yum-utils)"
fi

echo ""
