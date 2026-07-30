# Gap Analysis — Connecteurs MoveToData vs Metabase

> Document PO — MoveToData v2026-07  
> Auteur : Analyse automatisée (Claude Code) à partir du code source du repo + catalogue Metabase officiel  
> Fichiers sources analysés :
> - `boson/src/main/java/io/movetodata/connect/library/enums/SourceTypeEnum.java`
> - `boson/src/main/java/io/movetodata/connect/library/services/JDBCService.java`
> - `boson/src/main/java/io/movetodata/connect/library/enums/RestAPITypeEnum.java`
> - `boson/src/main/java/io/movetodata/connect/library/enums/RestAPIAuthTypeEnum.java`
> - `boson/src/main/java/io/movetodata/connect/controllers/SharePointController.java`
> - `frontend/src/Apps/Connect/Enums/SourceTypeEnum.ts`
> - `frontend/src/Apps/Connect/Sources/Source.constants.tsx`

---

## 1. Connecteurs MoveToData actuels (inventaire code)

### 1.1 Connecteurs actifs (GA — pleinement fonctionnels en production)

| # | Connecteur | Type | Auth supportée | Fichier(s) source principal |
|---|-----------|------|---------------|----------------------------|
| 1 | PostgreSQL | SQL / JDBC | Login+Password | `JDBCService.java`, `SourceTypeEnum.java` |
| 2 | MySQL | SQL / JDBC | Login+Password | `JDBCService.java` |
| 3 | Oracle (21+) | SQL / JDBC | Login+Password | `JDBCService.java` |
| 4 | Microsoft SQL Server | SQL / JDBC | Login+Password | `JDBCService.java` |
| 5 | MariaDB | SQL / JDBC | Login+Password | `JDBCService.java` |
| 6 | Snowflake | Cloud DW / JDBC | Login+Password, Key Pair (BouncyCastle) | `JDBCService.java` |
| 7 | File / Folder Upload | Fichier (CSV, Excel…) | N/A (upload direct) | `UploadService.java` |
| 8 | SharePoint (Microsoft) | Cloud / REST OAuth2 | Token OAuth2 Azure AD | `SharePointController.java`, `SharePointConnectorService.java` |
| 9 | REST API / Webhook | API HTTP | None, Bearer Token, API Key | `WebhookService.java`, `RestAPIAuthTypeEnum.java` |

**Total actif : 9 connecteurs**

Notes techniques importantes :
- **JDBC** : chaque connecteur bénéficie de la gestion des dialects SQL (LIMIT/ROWNUM/TOP), du pool de connexions, et d'un test de connexion (`TestConnection.view.tsx`).
- **Snowflake** : authentification par clé privée chiffrée (PKCS8) supportée, avec warehouse, schema et user role configurables — niveau de maturité enterprise.
- **REST API/Webhook** : backend complet (types RAW / FORMDATA / JSON, auth None/Bearer/ApiKey), mais **l'entrée UI est actuellement désactivée** dans `Source.constants.tsx` (`disabled: true`). Le backend est prêt.
- **SharePoint** : implémentation OAuth2 Azure AD via `SharePointConnectorService`. Endpoint actif `/api/connect/source/{id}/children/{folder_id}`. Afficher dans l'UI si ce n'est pas encore le cas.

### 1.2 Connecteurs présents dans l'UI mais désactivés (roadmap court terme)

Ces connecteurs sont définis dans `Source.constants.tsx` avec `disabled: true`. L'icône et le label existent mais aucun backend n'est implémenté (sauf REST API qui l'est).

| # | Connecteur | Type | Backend existant ? |
|---|-----------|------|-------------------|
| 10 | IBM DB2 | SQL / JDBC | Non (JDBC driver à ajouter) |
| 11 | SAP (HANA) | SQL / JDBC | Non (JDBC driver à ajouter) |
| 12 | REST API | API HTTP | Oui (WebhookService) — UI à activer |
| 13 | Amazon Redshift | Cloud DW / JDBC | Non (JDBC driver à ajouter) |
| 14 | Google AlloyDB | Cloud SQL / JDBC | Non (PostgreSQL-compatible, trivial) |

**Total avec roadmap UI : 14 connecteurs**

---

## 2. Catalogue Metabase complet

Source : https://www.metabase.com/docs/latest/databases/connecting et https://www.metabase.com/data_sources/ (juillet 2026)

### 2.1 Connecteurs officiels Metabase (maintenus par l'équipe core)

| # | Connecteur | Type |
|---|-----------|------|
| 1 | Amazon Athena | Cloud / Serverless SQL |
| 2 | Amazon Redshift | Cloud DW |
| 3 | BigQuery (Google) | Cloud DW |
| 4 | ClickHouse | OLAP / Analytics DB |
| 5 | Databricks | Cloud Data Lakehouse |
| 6 | Druid | OLAP / Time-series |
| 7 | MariaDB | SQL / JDBC |
| 8 | Microsoft SQL Server | SQL / JDBC |
| 9 | MongoDB | NoSQL / Document |
| 10 | MySQL | SQL / JDBC |
| 11 | Oracle Database | SQL / JDBC |
| 12 | PostgreSQL | SQL / JDBC |
| 13 | Presto (PrestoDB) | Distributed SQL |
| 14 | Snowflake | Cloud DW |
| 15 | SparkSQL | Big Data / SQL |
| 16 | SQLite | SQL / Embedded |
| 17 | Starburst (Trino) | Distributed SQL |
| 18 | Vertica | Analytics DW |

**Total officiel : 18 connecteurs**

### 2.2 Connecteurs communautaires Metabase (self-hosted uniquement)

| # | Connecteur | Type |
|---|-----------|------|
| 19 | Apache Pinot | OLAP / Real-time |
| 20 | Cube | Semantic Layer |
| 21 | Dremio | Data Lakehouse |
| 22 | DuckDB | OLAP / Embedded |
| 23 | Exasol | Analytics DW |
| 24 | Firebolt | Cloud OLAP |
| 25 | Hydra | Cloud DW (PostgreSQL-based) |
| 26 | Materialize | Streaming SQL |
| 27 | Ocient | Analytics DW |
| 28 | StarRocks | OLAP |
| 29 | Teradata | Enterprise DW |

**Total communautaire : 11 connecteurs**

---

## 3. Gap analysis (tableau comparatif)

### 3.1 Connecteurs présents dans les deux plateformes

| Connecteur | MoveToData | Metabase |
|-----------|-----------|---------|
| PostgreSQL | Active (GA) | Officiel |
| MySQL | Active (GA) | Officiel |
| Oracle | Active (GA) | Officiel |
| Microsoft SQL Server | Active (GA) | Officiel |
| MariaDB | Active (GA) | Officiel |
| Snowflake | Active (GA) | Officiel |
| SparkSQL | Interne (non exposé user) | Officiel |

### 3.2 Connecteurs présents dans Metabase mais absents de MoveToData

| Connecteur | Priorité (voir §4) | Type |
|-----------|-------------------|------|
| ClickHouse | P1.1 | OLAP / JDBC |
| Databricks | P1.2 | Lakehouse / JDBC |
| MongoDB | P1.3 | NoSQL |
| Vertica | P2 | DW / JDBC |
| Presto / Trino | P2 | Distributed SQL / JDBC |
| Starburst | P2 | Distributed SQL / JDBC |
| Amazon Redshift | P2* | DW / JDBC (déjà en UI) |
| SQLite | P3 | Embedded / JDBC |
| Amazon Athena | P3* | Serverless SQL / JDBC |
| Druid | P4 | OLAP / JDBC |
| BigQuery | P4* | GCP DW / SDK |
| Apache Pinot | P4 | OLAP |
| DuckDB | P4 | Embedded OLAP |
| Dremio | P4 | Lakehouse |
| Teradata | P4 | Enterprise DW / JDBC |
| Exasol | P4 | DW / JDBC |

> (*) Redshift, Athena, BigQuery : connecteurs vers des systèmes US cloud (AWS / GCP). Pas de problème de souveraineté — MoveToData **lit** la donnée source sans l'héberger sur ces infras. Le traitement et la visualisation restent sur l'infra EU. À documenter clairement pour éviter toute confusion marketing.

### 3.3 Connecteurs présents dans MoveToData mais absents de Metabase (avantages)

| Connecteur | Avantage différenciant |
|-----------|----------------------|
| SharePoint (Microsoft) | Seul outil de la comparaison avec un connecteur SharePoint natif. Critère fort en context enterprise EU (usage massif M365). |
| File / Folder Upload | Metabase ne supporte pas l'ingestion de fichiers CSV/Excel directement. MoveToData permet l'auto-provisioning de données sans base. Avantage PME et utilisateurs non-techniques. |
| REST API / Webhook | Metabase n'a pas de connecteur HTTP/API. MoveToData peut ingérer des données depuis n'importe quelle API tierce avec auth Bearer/APIKey. Différenciateur fort vs Palantir (qui le propose) et Metabase. |
| ODBC Bridge (planifié) | Pont générique vers toute source legacy disposant d'un driver ODBC : IBM AS/400, Progress OpenEdge, Sybase ASE, Microsoft Access, FileMaker, ERP propriétaires… Aucun concurrent direct (Metabase, Grafana, Superset) ne propose ce pont. Différenciateur enterprise fort pour les migrations et l'interopérabilité legacy. |

---

## 4. Backlog priorisé des connecteurs manquants

Critères de priorisation :
- **Popularité enterprise** : fréquence d'usage sur le marché cible EU
- **Effort d'intégration** : S = JDBC driver standard (1-2j dev) / M = driver custom ou SDK (1 semaine) / L = architecture non-JDBC (2+ semaines)
- **Impact parité Metabase** : combien de points de friction cela retire dans les comparaisons commerciales
- **Souveraineté** : Neutre = lecture source externe OK / Attention = intégration profonde US infra

### Tier 1 — Must Have (fort impact commercial : parité Metabase OU différenciateur legacy stratégique)

| Priorité | Connecteur | Popularité | Effort | Raison |
|---------|-----------|-----------|--------|--------|
| P1.0 | **ODBC Bridge** | Très haute (legacy enterprise, industrie, banques) | M — unixODBC + JDBC-ODBC bridge (voir §5) | **Différenciateur beyond-Metabase.** Pont générique couvrant potentiellement des dizaines de sources legacy (AS/400, Progress, Sybase, Access, ERP propriétaires). Aucun concurrent direct ne le propose. Un seul effort d'intégration ouvre l'accès à toutes les bases non JDBC. Impact direct sur les comptes industriels, bancaires et du secteur public EU. |
| P1.1 | ClickHouse | Très haute (analytics moderne, EU-friendly) | S — JDBC via `com.clickhouse:clickhouse-jdbc` | Devenu la DB analytics de référence pour les data stacks modernes. JDBC standard, intégration triviale. Gap visible dans les POC. |
| P1.2 | Databricks | Très haute (enterprise, Palantir comparison) | S — JDBC via Simba/Databricks JDBC | Databricks est mentionné systématiquement dans les comparaisons enterprise avec Palantir. JDBC Databricks disponible, OAuth supportable. |
| P1.3 | MongoDB | Haute (NoSQL dominant) | M — MongoDB Spark Connector ou driver dédié (pas de JDBC standard) | Premier NoSQL manquant. Nécessite une intégration via MongoDB Spark Connector (déjà dans la stack Spark). Effort modéré. |

### Tier 2 — Should Have (parité Metabase, JDBC disponible)

| Priorité | Connecteur | Popularité | Effort | Raison |
|---------|-----------|-----------|--------|--------|
| P2.1 | Amazon Redshift | Haute (enterprise DW historique) | S — JDBC `com.amazon.redshift.jdbc.Driver` + déjà en UI | UI existante, juste brancher le JDBC. Migration simple depuis le connecteur Postgres (Redshift en est compatible). |
| P2.2 | Vertica | Haute (analytics DW enterprise EU) | S — JDBC via Vertica JDBC driver | Très présent dans les entreprises FR/DE. Driver JDBC officiel disponible. |
| P2.3 | Presto / Trino | Moyenne-haute (infras lakehouse open source) | S — JDBC via `io.trino:trino-jdbc` | Alternative open source à Databricks. Driver JDBC Trino mature. Starburst partage le même driver. |
| P2.4 | Starburst | Moyenne (enterprise Trino) | S — même driver que Trino | Factoring avec Trino : un seul effort pour deux connecteurs. |
| P2.5 | SparkSQL (user-facing) | Haute (déjà dans MTD en interne) | S — déjà implémenté en interne, exposer comme connecteur externe | `SPARKSQL` est dans `SourceTypeEnum.java` marqué "internal only". L'exposer comme connecteur vers un Spark externe est une semaine de travail max. |

### Tier 3 — Nice to Have (complétude, cas d'usage spécifiques)

| Priorité | Connecteur | Popularité | Effort | Raison |
|---------|-----------|-----------|--------|--------|
| P3.1 | SQLite | Moyenne (dev, PME, prototypage) | S — JDBC `org.xerial:sqlite-jdbc` | Effort minimal. Utile pour les équipes dev et les démos. |
| P3.2 | DuckDB | Croissante (OLAP embedded, data science) | M — driver JDBC dédié ou JDBC via DuckDB JDBC | Montée en puissance dans la communauté data. Positionnement "alternative moderne". |
| P3.3 | IBM DB2 | Haute (legacy enterprise, banques, assurance) | S — JDBC `com.ibm.db2.jcc.DB2Driver`, déjà en UI | Déjà dans l'UI (`disabled: true`). Activer avec le driver IBM DB2 JDBC. Important pour les comptes bancaires et industriels EU. |
| P3.4 | SAP HANA | Haute (enterprise EU, industrie) | M — JDBC via SAP HANA JDBC Driver (licence SAP) | Déjà dans l'UI (`disabled: true`). Licensing SAP peut être un frein mais impact commercial fort en EU (SAP est dominant dans l'industrie européenne). |
| P3.5 | Google AlloyDB | Faible (nouveau) | S — compatible PostgreSQL JDBC, déjà en UI | Déjà en UI (`disabled: true`). Compatible PostgreSQL standard : `org.postgresql.Driver` suffit potentiellement. |
| P3.6 | Amazon Athena | Moyenne | S — JDBC via `com.simba.athena.jdbc.Driver` | Utile pour les clients qui ont des data lakes S3. Souveraineté OK (lecture seule vers source externe). |

### Tier 4 — Backlog long terme

ClickHouse remplace Druid/Pinot dans la plupart des nouveaux projets. Ces connecteurs ont une utilité marginale.

| Connecteur | Note |
|-----------|------|
| Teradata | Enterprise legacy, mais JDBC disponible. Impact fort si ciblage grands comptes. |
| Exasol | Niche analytique EU (Allemagne). JDBC disponible. |
| Apache Pinot | Remplacé par ClickHouse dans 80% des nouveaux projets. |
| Dremio | Concurrent direct (lakehouse), connecteur symbolique. |
| BigQuery | GCP/Google. JDBC simulé via SDK. Effort M. Impact commercial sur les clients GCP. |
| Druid | Niche temps-réel. Couvert partiellement par ClickHouse. |

---

## 5. Connecteurs différenciants MoveToData (avantages concurrentiels)

Ces connecteurs sont **absents de Metabase** et représentent des arguments commerciaux directs :

### SharePoint (Microsoft 365)
- **Unicité** : Aucun des concurrents directs (Metabase, Grafana, Superset) ne propose un connecteur SharePoint natif.
- **Impact commercial** : Plus de 300 millions d'utilisateurs M365 en enterprise. Critère décisionnel fréquent dans les PME et ETI européennes.
- **Message marketing** : "Branchez directement vos fichiers SharePoint, sans exporter, sans pipeline intermédiaire."
- **Statut** : Implémenté (controller + service actifs). Vérifier que l'UI est bien exposée et documentée.

### File / Folder Upload (CSV, Excel, JSON)
- **Unicité** : Metabase ne permet pas l'upload de fichiers comme source de données (uniquement des bases de données).
- **Impact commercial** : Cas d'usage #1 des équipes non-techniques. Réduit la friction à l'adoption (quick wins).
- **Message marketing** : "Importez vos fichiers Excel en 30 secondes et créez vos premiers dashboards sans DBA."
- **Statut** : Implémenté (`UploadService.java`).

### REST API / Webhook (HTTP)
- **Unicité** : Ni Metabase ni la plupart des BI tools ne proposent d'ingestion via API REST native.
- **Impact commercial** : Cas d'usage critique pour connecter des SaaS tiers (CRM, ERP, outils métier) sans pipeline ETL.
- **Message marketing** : "Connectez n'importe quel SaaS via API REST en quelques minutes. Pas d'ETL, pas de code."
- **Statut** : Backend complet (`WebhookService.java`, types RAW/FORMDATA/JSON, auth Bearer/APIKey). **UI désactivée — action immédiate requise pour activer.**
- **Action PO** : Débloquer ce connecteur dans l'UI (`disabled: true` à retirer dans `Source.constants.tsx`) et ajouter les écrans de configuration. C'est un avantage concurrentiel majeur actuellement caché.

### ODBC Bridge (Generic Legacy Connector) — planifié P1.0

#### Cas d'usage et valeur business

ODBC (Open Database Connectivity) est un standard universel permettant à toute application de se connecter à n'importe quelle base de données disposant d'un pilote ODBC. Le connecteur ODBC de MoveToData agit comme un pont générique couvrant d'un seul effort toutes les sources de données qui ne disposent pas d'un driver JDBC natif, en particulier l'écosystème legacy enterprise.

Sources couvertes par un unique connecteur ODBC :

| Source | Secteur cible | Notes |
|--------|--------------|-------|
| IBM AS/400 / iSeries (DB2 for i) | Industrie, logistique, banques FR/BE | Driver ODBC IBM iAccess — aucune autre solution JDBC directe |
| Progress OpenEdge | ERP propriétaires (industrie EU) | Très répandu en PME industrielle européenne |
| Sybase ASE (SAP Adaptive Server) | Banques, télécoms legacy | Historiquement dominant dans les banques européennes |
| Microsoft Access / Excel ODBC | PME, collectivités, secteur public | Cas d'usage fréquent dans les administrations FR |
| FileMaker Pro | PME, professions libérales | Ecosystème Apple enterprise |
| Informix | Telecom, assurance legacy | IBM Informix ODBC driver |
| ERP / SIRH propriétaires | Tout secteur | Tout ERP exposant un driver ODBC (Sage, Cegid, Infor…) |

**Quand l'utiliser :** Quand une source de données n'est disponible dans aucun autre connecteur MoveToData et que l'éditeur fournit un pilote ODBC (ce qui est le cas de la quasi-totalité des systèmes commerciaux de plus de 10 ans).

**Public cible :**
- Grandes entreprises et ETI avec un SI historique hétérogène (stacks 1990-2010)
- DSI souhaitant consolider des données issues de plusieurs ERP/SIRH sans projet de migration
- Intégrateurs data spécialisés en modernisation de SI legacy
- Secteur public (administrations avec AS/400 et bases Access encore en production)

**Positionnement différenciant vs Metabase :** Metabase ne propose aucun équivalent. L'argument commercial est direct : "MoveToData se connecte à votre AS/400, votre Progress ou votre Sybase — Metabase ne peut pas."

**Positionnement différenciant vs Palantir :** Palantir Foundry supporte des sources ODBC via ses connecteurs enterprise payants. MoveToData peut revendiquer la même capacité avec une architecture open source et souveraine.

#### Spec technique

**Contrainte Java 8+ :** Le pont JDBC-ODBC standard (`sun.jdbc.odbc.JdbcOdbcDriver`) a été supprimé depuis Java 8. L'implémentation nécessite une approche alternative.

**Architecture recommandée (Docker-first) :**

1. **unixODBC + FreeTDS (open source, recommandé)** — composant principal
   - Intégrer `unixODBC` et les drivers ODBC open source (`FreeTDS` pour Sybase/MSSQL legacy, `libmdbtools` pour Access) dans l'image Docker MoveToData
   - Utiliser un bridge JDBC-ODBC JNI léger : `com.hynnet:odbc-bridge` ou équivalent open source compatible Linux
   - Configuration via les fichiers `odbc.ini` (DSN système) et `odbcinst.ini` (déclaration des drivers)
   - Connexion JDBC : `jdbc:odbc:DSN=<nom_dsn>` ou connection string complète

2. **Alternative commerciale (Enterprise uniquement)** : Easysoft JDBC-ODBC Bridge ou OpenLink ODBC — licencié par client, à réserver à l'offre Enterprise si les drivers open source ne couvrent pas tous les cas.

**Paramètres de configuration exposés dans l'UI :**

| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| Mode de connexion | Enum (DSN / Connection String) | Oui | DSN = nom de la source configurée sur le serveur ; Connection String = chaîne complète |
| DSN Name | String | Si mode DSN | Nom du Data Source Name déclaré dans `odbc.ini` |
| Connection String | String | Si mode Connection String | Ex. `Driver={FreeTDS};Server=myserver;Port=1433;Database=mydb` |
| Username | String | Non | Identifiant de connexion à la source |
| Password | String (masqué) | Non | Mot de passe |
| Charset / Encoding | String | Non | Ex. `UTF-8`, `ISO-8859-1` — critique pour les bases legacy |
| SQL Dialect | Enum | Non | Pour adapter les requêtes : ANSI / T-SQL / PL/SQL |

**Effort estimé :** M — environ 1 semaine
- Jour 1-2 : Configuration de l'image Docker (unixODBC, FreeTDS, drivers OS) + test de connexion AS/400 et Sybase en environnement dev
- Jour 3 : Implémentation du service Java (JDBC via bridge JNI, pool de connexions HikariCP)
- Jour 4 : Intégration dans `SourceTypeEnum.java` + `JDBCService.java` + ajout du dialect ANSI générique
- Jour 5 : UI (formulaire DSN/Connection String dans `Source.constants.tsx`) + tests d'intégration

**Risques techniques :**
- Les drivers ODBC sont des binaires natifs OS-dépendants : l'image Docker doit embarquer tous les drivers cibles. Prévoir une image base étendue (+50-100 Mo).
- Certains drivers ODBC (AS/400 IBM iAccess) nécessitent une installation côté serveur MoveToData, non côté client. Documenter clairement pour l'offre Enterprise self-hosted.
- L'encodage des données legacy (EBCDIC, Latin-1) peut nécessiter une conversion explicite. Exposer le paramètre `Charset` dans l'UI.

**Souveraineté :** Neutre. MoveToData lit la source externe via ODBC sans héberger la donnée sur une infrastructure tierce. Compatible avec le positionnement souveraineté EU.

---

## 6. Bilan et objectif 25 connecteurs (Open Source)

Le positionnement open source fixe un plafond de 25 connecteurs actifs.

| Statut | Nombre | Détail |
|--------|--------|--------|
| Actifs GA | 9 | PostgreSQL, MySQL, Oracle, MSSQL, MariaDB, Snowflake, File Upload, SharePoint, REST API (backend) |
| Roadmap UI court terme (< 1 sprint) | 3 | REST API UI (activer), IBM DB2, Google AlloyDB |
| Tier 1 (< 1 mois) | 4 | **ODBC Bridge**, ClickHouse, Databricks, MongoDB |
| Tier 2 (< 3 mois) | 5 | Redshift, Vertica, Trino, Starburst, SparkSQL user-facing |
| **Total atteignable** | **21** | |
| Tier 3 complémentaire | 4 | SQLite, DuckDB, SAP HANA, Athena |
| **Total cible 25** | **25** | Objectif open source atteint |

> Note : Druid a été déplacé du Tier 3 vers le Tier 4 (remplacé par ClickHouse dans la majorité des nouveaux projets). ODBC Bridge prend sa place dans la cible 25 connecteurs avec un impact business nettement supérieur.

**Recommandation PO** : Démarrer par ODBC Bridge en sprint 1 en parallèle de ClickHouse. ODBC ouvre des dizaines de sources legacy en un seul ticket, ce qui dépasse immédiatement la parité Metabase sur un segment enterprise que Metabase ne sert pas. ClickHouse et Databricks restent les arguments les plus fréquents dans les comparaisons avec Palantir Foundry — les traiter dans le même sprint.

---

## 7. Alertes de cohérence positioning

**Alerte 1 — REST API UI désactivée**  
Le connecteur REST API/Webhook a un backend complet mais son entrée UI est commentée `disabled: true` dans `Source.constants.tsx`. C'est l'un des 3 différenciateurs majeurs vs Metabase. Action immédiate.

**Alerte 2 — SparkSQL "internal only"**  
SparkSQL est marqué `// For internal usage only` dans `SourceTypeEnum.java` mais Metabase l'expose comme connecteur officiel. L'exposer comme connecteur vers un cluster Spark externe consolide le positionnement "alternative Palantir" (Palantir supporte nativement Spark).

**Alerte 3 — AlloyDB est un connecteur Google Cloud**  
AlloyDB (Google) est présent en UI mais désactivé. Avant de l'activer, documenter que c'est un connecteur **source** (lecture vers une base externe GCP), ce qui ne contredit pas la souveraineté EU — MoveToData lui-même reste sur infra EU. Éviter toute ambiguité dans le message marketing.

**Alerte 4 — IBM DB2 et SAP en UI roadmap**  
Ces deux connecteurs sont présents en UI (`disabled: true`). Ils représentent des comptes enterprise EU critiques (banques, industrie). Les prioriser en Tier 3 plutôt que de les laisser dans un état "visible mais inaccessible" qui dégrade l'expérience de démonstration.

**Alerte 5 — ODBC Bridge : image Docker à anticiper dès le sprint 1**  
L'implémentation ODBC nécessite des modifications de l'image Docker de base (ajout unixODBC, FreeTDS, éventuellement libmdbtools). Cette dépendance doit être planifiée en amont du sprint pour ne pas bloquer la livraison. Ouvrir le ticket DevOps en même temps que le ticket dev ODBC.
