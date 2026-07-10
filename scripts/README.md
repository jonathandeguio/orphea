# MoveToData Platform — Scripts d'installation Ubuntu

Ensemble complet de scripts pour installer et démarrer la plateforme MoveToData sur Ubuntu 22.04 LTS.

## Architecture de la plateforme

```
┌─────────────────────────────────────────────────────────────────┐
│                     MOVETODATA PLATFORM                             │
│                                                                 │
│  ┌──────────────┐  ┌─────────────────────────────────────────┐ │
│  │   Frontend   │  │               Boson API                 │ │
│  │  React 18    │  │   Java 11 · Spring Boot · Spark 3.4.3   │ │
│  │  Nginx :80   │  │             Port :8080                  │ │
│  └──────────────┘  └───────────────┬─────────────────────────┘ │
│                                    │                            │
│  ┌─────────────────────────────────┼─────────────────────────┐ │
│  │          Infrastructure         │                          │ │
│  │  PostgreSQL :5432   Redis :6379 │                          │ │
│  └─────────────────────────────────┴─────────────────────────┘ │
│                                                                 │
│  ┌──────────────────────────────┐  ┌─────────────────────────┐ │
│  │          SNAP (opt.)         │  │      TYCHO (opt.)        │ │
│  │  Artifact Manager · :8082   │  │  Superset BI · :8088     │ │
│  │  Java · Nginx · PG16        │  │  Python · Celery · Redis  │ │
│  └──────────────────────────────┘  └─────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Fichiers disponibles

| Fichier | Rôle |
|---------|------|
| `movetodata.sh` | **Point d'entrée principal** — toutes les commandes |
| `00-install-system.sh` | Installation Docker + dépendances Ubuntu |
| `01-setup-env.sh` | Génération du fichier `.env.movetodata` (secrets) |
| `02-build.sh` | Build des images Docker |
| `03-start.sh` | Démarrage de la plateforme |
| `04-stop.sh` | Arrêt propre |
| `05-healthcheck.sh` | Vérification de santé complète |
| `06-logs.sh` | Consultation des logs |
| `07-restart.sh` | Redémarrage / rebuild d'un service |
| `docker-compose.core.yml` | Stack principale (Boson + Frontend + PG + Redis) |
| `docker-compose.snap.yml` | Stack Snap (artifact manager) |
| `docker-compose.tycho.yml` | Stack Tycho (Apache Superset) |
| `movetodata-platform.service` | Service systemd — stack core |
| `movetodata-snap.service` | Service systemd — Snap |

## Démarrage rapide (VM Ubuntu propre)

```bash
# 1. Installation système (une seule fois, en root)
sudo bash scripts/00-install-system.sh

# 2. Configuration des variables (secrets, URLs)
bash scripts/01-setup-env.sh

# 3. Build des images (15–40 min)
bash scripts/02-build.sh

# 4. Démarrage
bash scripts/03-start.sh

# 5. Vérification
bash scripts/05-healthcheck.sh
```

## Usage via le point d'entrée unifié

```bash
# Installation
sudo bash scripts/movetodata.sh install

# Configuration
bash scripts/movetodata.sh env

# Build
bash scripts/movetodata.sh build all

# Démarrage
bash scripts/movetodata.sh start all

# État
bash scripts/movetodata.sh status

# Healthcheck
bash scripts/movetodata.sh health

# Logs en temps réel
bash scripts/movetodata.sh logs boson --follow

# Restart avec rebuild
bash scripts/movetodata.sh update boson

# Arrêt
bash scripts/movetodata.sh stop all
```

## Ports exposés

| Port | Service | Description |
|------|---------|-------------|
| 80   | Frontend | SPA React (Nginx) |
| 8080 | Boson API | Spring Boot + Spark |
| 8082 | Snap | Artifact manager (Nginx proxy) |
| 8088 | Tycho | Apache Superset BI |

## Variables d'environnement obligatoires

Éditez `.env.movetodata` après génération :

| Variable | Description |
|----------|-------------|
| `BASE_URL` | URL publique de la plateforme |
| `BOSON_DB_PASSWORD` | Mot de passe PostgreSQL Boson |
| `TOKEN_SECRET` | Secret JWT (min 64 chars) |
| `GITHUB_OAUTH_CLIENT_ID` | OAuth2 GitHub |
| `GITHUB_OAUTH_CLIENT_SECRET` | OAuth2 GitHub |
| `GOOGLE_OAUTH_CLIENT_ID` | OAuth2 Google |
| `GOOGLE_OAUTH_CLIENT_SECRET` | OAuth2 Google |

## Service systemd (démarrage automatique)

```bash
sudo cp scripts/movetodata-platform.service /etc/systemd/system/
sudo cp scripts/movetodata-snap.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable movetodata-platform movetodata-snap
sudo systemctl start movetodata-platform
```

## Prérequis système

- Ubuntu 22.04 LTS
- CPU : 8 vCPU minimum (16 recommandés)
- RAM : 16 Go minimum (32 Go recommandés)
- Disque : 100 Go minimum (200 Go recommandés)
- Docker Engine 24+, Docker Compose v2
- Module kernel FUSE (pour Snap)
