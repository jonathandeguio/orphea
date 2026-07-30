# Spec technique — unixODBC + FreeTDS dans l'image Docker boson

**Statut** : A implémenter  
**Date** : 2026-07-29  
**Auteur** : PO MoveToData  
**Cible** : DevOps / Backend  
**Fichier pilote** : `boson/Dockerfile`

---

## Contexte et état actuel

Le connecteur ODBC est implémenté dans `JDBCService.java`. Il utilise le driver `com.hynnet.odbc.Driver` (bridge JDBC-ODBC JNI) qui appelle unixODBC via la couche native. Sans les paquets OS et la configuration ODBC dans le conteneur, toute connexion ODBC échoue au runtime avec `ClassNotFoundException` ou `UnsatisfiedLinkError`.

### Etat actuel (manquant)

| Element | Etat |
|---|---|
| `unixodbc`, `unixodbc-dev` dans l'image | ABSENT |
| `freetds-bin`, `freetds-dev`, `tdsodbc` dans l'image | ABSENT |
| `/etc/odbcinst.ini` | ABSENT |
| `/etc/odbc.ini` | ABSENT |
| `com.hynnet:odbc-bridge` dans `build.gradle` | ABSENT |

### Fichiers a modifier

```
boson/Dockerfile
boson/build.gradle
boson/dependencies/packages.txt        ← nouveau contenu
boson/dependencies/odbcinst.ini        ← nouveau fichier
boson/dependencies/odbc.ini            ← nouveau fichier
```

---

## 1. Paquets OS — `boson/dependencies/packages.txt`

Ajouter les 5 lignes suivantes a la fin du fichier existant (ne pas supprimer les lignes existantes) :

```
tzdata
curl
vim
git
python3
python3-pip
jq
openssl
tini
unixodbc
unixodbc-dev
freetds-bin
freetds-dev
tdsodbc
```

Les paquets sont installés automatiquement par le bloc `apt-get` existant dans le Dockerfile :

```dockerfile
RUN \
    apt-get -qq update && \
    apt-get -qq upgrade -y && \
    DEBIAN_FRONTEND=noninteractive apt-get install -qq -y $(tr -d '\r' < dependencies/packages.txt) && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
```

Aucune modification du Dockerfile sur ce point — le mécanisme existant suffit.

---

## 2. Configuration ODBC — fichiers a créer

### 2a. `boson/dependencies/odbcinst.ini`

Ce fichier enregistre le driver FreeTDS auprès d'unixODBC. Il sera copié dans `/etc/odbcinst.ini` dans le conteneur.

```ini
[ODBC Drivers]
FreeTDS = Installed

[FreeTDS]
Description = FreeTDS ODBC Driver (Sybase / MS SQL Server legacy)
Driver      = /usr/lib/x86_64-linux-gnu/odbc/libtdsodbc.so
Setup       = /usr/lib/x86_64-linux-gnu/odbc/libtdsS.so
FileUsage   = 1
UsageCount  = 1
```

> Note architecture : le chemin `/usr/lib/x86_64-linux-gnu/odbc/` est valide pour Debian amd64 (image `gradle:7.6.1-jdk11`). Si l'image evolue vers arm64, remplacer `x86_64-linux-gnu` par `aarch64-linux-gnu`.

### 2b. `boson/dependencies/odbc.ini`

Ce fichier contient un DSN d'exemple et un DSN vide template. Il sera copié dans `/etc/odbc.ini`.

```ini
; /etc/odbc.ini — Data Source Names systeme
; Ajouter un bloc [NomDuDSN] par source ODBC declaree.
; Le champ "Driver" doit correspondre exactement au nom de section dans /etc/odbcinst.ini.

[ODBC Data Sources]
; Declarer ici le nom de chaque DSN, exemple :
; MaSQLServer = FreeTDS

; --- Template DSN (a dupliquer et adapter) ---
[MaSQLServer]
Description = Exemple de connexion SQL Server via FreeTDS
Driver      = FreeTDS
Server      = 192.168.1.100
Port        = 1433
Database    = ma_base
TDS_Version = 7.4

; --- DSN Sybase ASE ---
; [MonSybase]
; Description = Connexion Sybase ASE
; Driver      = FreeTDS
; Server      = sybase-host
; Port        = 5000
; Database    = ma_base
; TDS_Version = 5.0
```

---

## 3. Modifications du `boson/Dockerfile`

Ajouter le bloc suivant **immediatement apres** le bloc `RUN apt-get` existant (apres `rm -rf /var/lib/apt/lists/*`), soit vers la ligne 79 du Dockerfile actuel :

```dockerfile
# --- ODBC/FreeTDS : configuration systeme ---
# Copie des fichiers de configuration ODBC dans le conteneur
COPY dependencies/odbcinst.ini /etc/odbcinst.ini
COPY dependencies/odbc.ini     /etc/odbc.ini

# Variables d'environnement pour que unixODBC trouve les fichiers de config
ENV ODBCSYSINI=/etc
ENV ODBCINI=/etc/odbc.ini

# Verification au build : lister les drivers enregistres
RUN odbcinst -q -d && echo "unixODBC drivers OK" || echo "WARNING: odbcinst check failed"
```

### Position exacte dans le Dockerfile (patch contextuel)

```dockerfile
# Avant (existant, lignes ~73-78) :
RUN \
    apt-get -qq update && \
    apt-get -qq upgrade -y && \
    DEBIAN_FRONTEND=noninteractive apt-get install -qq -y $(tr -d '\r' < dependencies/packages.txt) && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Apres (ajouter ces lignes juste apres) :
COPY dependencies/odbcinst.ini /etc/odbcinst.ini
COPY dependencies/odbc.ini     /etc/odbc.ini
ENV ODBCSYSINI=/etc
ENV ODBCINI=/etc/odbc.ini
RUN odbcinst -q -d && echo "unixODBC drivers OK" || echo "WARNING: odbcinst check failed"
```

---

## 4. Dependance Gradle — `boson/build.gradle`

La dependance JDBC-ODBC bridge est **absente** du `build.gradle` actuel. Le code `JDBCService.java` (ligne 125) charge explicitement `com.hynnet.odbc.Driver`.

### Ligne a ajouter dans la section `dependencies {}` de `boson/build.gradle`

Ajouter apres le bloc des drivers JDBC existants (apres la ligne `net.snowflake:snowflake-jdbc`, vers la ligne 80) :

```groovy
    // JDBC-ODBC bridge — necessite unixODBC installe dans l'image Docker
    // Driver class : com.hynnet.odbc.Driver
    implementation 'com.hynnet:odbc-bridge:1.0.3'
```

### Disponibilite de l'artefact

`com.hynnet:odbc-bridge:1.0.3` est publie sur Maven Central. Si le build echoue avec `Could not resolve com.hynnet:odbc-bridge:1.0.3`, deux options :

**Option A — Ajouter le repository JitPack :**
```groovy
repositories {
    // ... repos existants ...
    maven { url 'https://jitpack.io' }
}
```

**Option B — JAR local (fallback) :**
Telecharger le JAR depuis `https://repo1.maven.org/maven2/com/hynnet/odbc-bridge/1.0.3/odbc-bridge-1.0.3.jar`, le deposer dans `boson/dependencies/libs/`, puis dans `build.gradle` :
```groovy
implementation fileTree(dir: 'dependencies/libs', include: ['*.jar'])
```

---

## 5. Fichiers de configuration complets (copier-coller prets)

### `/etc/odbcinst.ini` (contenu final dans le conteneur)

```ini
[ODBC Drivers]
FreeTDS = Installed

[FreeTDS]
Description = FreeTDS ODBC Driver (Sybase / MS SQL Server legacy)
Driver      = /usr/lib/x86_64-linux-gnu/odbc/libtdsodbc.so
Setup       = /usr/lib/x86_64-linux-gnu/odbc/libtdsS.so
FileUsage   = 1
UsageCount  = 1
```

### `/etc/odbc.ini` (contenu final dans le conteneur)

```ini
[ODBC Data Sources]
MaSQLServer = FreeTDS

[MaSQLServer]
Description = Exemple SQL Server via FreeTDS
Driver      = FreeTDS
Server      = 192.168.1.100
Port        = 1433
Database    = ma_base
TDS_Version = 7.4
```

---

## 6. Exemple de DSN pour se connecter a SQL Server via FreeTDS

### Mode DSN (champ `server` = nom du DSN declare dans `odbc.ini`)

Dans l'UI MoveToData, configurer la source ODBC comme suit :

| Champ UI | Valeur |
|---|---|
| Type | ODBC |
| Serveur (Server) | `MaSQLServer` ← nom du DSN dans `/etc/odbc.ini` |
| Base de donnees (Database) | laisser vide |
| Login | `sa` |
| Mot de passe | `monMotDePasse` |

La JDBC URL generee sera : `jdbc:odbc:MaSQLServer`

### Mode Connection String (sans DSN predeclaré)

| Champ UI | Valeur |
|---|---|
| Type | ODBC |
| Serveur (Server) | laisser vide |
| Base de donnees (Database) | `Driver={FreeTDS};Server=192.168.1.100;Port=1433;Database=ma_base;TDS_Version=7.4` |
| Login | `sa` |
| Mot de passe | `monMotDePasse` |

La JDBC URL generee sera : `jdbc:odbc:Driver={FreeTDS};Server=192.168.1.100;Port=1433;Database=ma_base;TDS_Version=7.4`

---

## 7. Commandes de validation dans le conteneur

Executer ces commandes pour valider que unixODBC et FreeTDS sont correctement installes :

```bash
# 1. Verifier que unixODBC est installe
docker exec movetodata-boson odbcinst --version

# 2. Lister les drivers ODBC enregistres (doit afficher [FreeTDS])
docker exec movetodata-boson odbcinst -q -d

# 3. Lister les DSN systeme declares dans /etc/odbc.ini
docker exec movetodata-boson odbcinst -q -s

# 4. Verifier que libtdsodbc.so est present au chemin attendu
docker exec movetodata-boson ls -la /usr/lib/x86_64-linux-gnu/odbc/libtdsodbc.so

# 5. Tester la connexion au DSN "MaSQLServer" (remplacer user/pwd)
docker exec movetodata-boson isql -v MaSQLServer sa monMotDePasse

# 6. Verifier les variables d'environnement ODBC
docker exec movetodata-boson sh -c 'echo "ODBCSYSINI=$ODBCSYSINI" && echo "ODBCINI=$ODBCINI"'

# 7. Verifier que le JAR bridge est dans le classpath (cherche dans /app)
docker exec movetodata-boson find /app -name "odbc-bridge*.jar"

# 8. Test FreeTDS direct (hors ODBC) vers un serveur SQL Server
docker exec movetodata-boson tsql -H 192.168.1.100 -p 1433 -U sa -P monMotDePasse
```

### Sortie attendue pour la commande 2 (`odbcinst -q -d`)

```
[FreeTDS]
```

### Sortie attendue pour la commande 4 (chemin du driver)

```
-rw-r--r-- 1 root root XXXXX /usr/lib/x86_64-linux-gnu/odbc/libtdsodbc.so
```

Si le fichier est absent, verifier que `tdsodbc` est bien installe :
```bash
docker exec movetodata-boson dpkg -l | grep tdsodbc
```

---

## 8. Recap des changements (checklist DevOps)

```
[ ] boson/dependencies/packages.txt  — ajouter : unixodbc unixodbc-dev freetds-bin freetds-dev tdsodbc
[ ] boson/dependencies/odbcinst.ini  — creer (contenu section 2a)
[ ] boson/dependencies/odbc.ini      — creer (contenu section 2b)
[ ] boson/Dockerfile                 — ajouter bloc COPY + ENV + RUN apres le bloc apt-get (section 3)
[ ] boson/build.gradle               — ajouter implementation 'com.hynnet:odbc-bridge:1.0.3' (section 4)
[ ] Rebuild image : docker compose -f scripts/docker-compose.core.yml build boson
[ ] Valider avec commandes section 7
```

---

## 9. Notes de compatibilite et risques

| Point | Detail |
|---|---|
| JDK 11 | `sun.jdbc.odbc.JdbcOdbcDriver` supprime depuis Java 8 — le bridge `com.hynnet:odbc-bridge` est obligatoire. |
| TDS_Version | Utiliser `7.4` pour SQL Server 2012+. Pour SQL Server 2000/2005 : `7.1`. Pour Sybase ASE : `5.0`. |
| Architecture ARM | Si migration vers ARM64 (Apple Silicon, Graviton), changer le chemin du `.so` : `/usr/lib/aarch64-linux-gnu/odbc/libtdsodbc.so`. |
| Securite des credentials | Ne jamais mettre de credentials dans `odbc.ini`. Les credentials sont passes au runtime par le connecteur via `DriverManager.getConnection(url, user, password)`. |
| Souverainete europeenne | unixODBC et FreeTDS sont des logiciels open source sans dependance infrastructure US — conformes au positionnement MoveToData. |
| Image de base | `gradle:7.6.1-jdk11` = Debian Bullseye. Si l'image de base change (ex: passage a `eclipse-temurin:11-jre-jammy`), les chemins apt et `.so` restent identiques (Ubuntu Jammy = meme arborescence Debian). |
