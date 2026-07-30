# Specs fonctionnelles et techniques — Features IA MoveToData

> Document PO/Dev — MoveToData v2026-07
> Auteur : Product Owner (Claude Code) — basé sur `.claude/agents/ai.md` et l'architecture existante
> Public cible : développeurs backend/frontend et Product Owner

---

## Préambule technique

### Service IA : `movetodata-ai`

Service Python indépendant exposé en interne sur le port **8090**. Boson (Spring Boot) lui sert de proxy via `/api/ai/**` en propageant le JWT utilisateur. Le service IA doit démarrer et s'arrêter indépendamment de Boson — Boson ne doit jamais crasher si `movetodata-ai` est absent (**graceful degradation obligatoire**).

```
Stack :
  - FastAPI + uvicorn
  - LangChain / LlamaIndex (orchestration LLM)
  - Ollama client (HTTP local)
  - sentence-transformers (nomic-embed-text)
  - scikit-learn, Prophet, PyOD, XGBoost
  - SQLAlchemy (accès sources via proxy JDBC ou direct)
  - Redis (cache réponses LLM et embeddings)
```

### Contraintes souveraineté — non négociables

- Zéro appel runtime vers OpenAI, Anthropic API US, Google Vertex AI, AWS Bedrock.
- Les données utilisateur ne quittent jamais l'infrastructure MoveToData.
- Aucun SDK tiers ne doit transférer des données à un tiers sans consentement explicite.
- Fallback cloud autorisé uniquement : **Mistral AI API (FR)**, RGPD-OK, données traitées en UE.
- Toute dépendance LLM ou ML doit être Apache 2.0 / MIT / BSD.

### Modèles souverains retenus

| Usage | Modèle | Provider | Licence |
|-------|--------|----------|---------|
| Text-to-SQL | `deepseek-coder:6.7b` | Ollama self-hosted | MIT |
| Chat général | `mistral:7b` ou `llama3:8b` | Ollama self-hosted | Apache 2.0 |
| Embeddings | `nomic-embed-text` | Ollama self-hosted | Apache 2.0 |
| Forecasting | Prophet | Self-hosted Python | MIT |
| Anomaly detection | IsolationForest | scikit-learn self-hosted | BSD |
| Fallback cloud EU | `mistral-small` | Mistral AI (FR) | Commercial RGPD-OK |

---

## F1 — Text-to-SQL

**Priorité** : P1
**Effort estimé** : M
**Dépendances** : Au moins une source JDBC active (PostgreSQL, MySQL, Snowflake, etc.), `JdbcUtils.isValidDQLQuery` opérationnel, service `movetodata-ai` déployé, Ollama avec `deepseek-coder:6.7b` chargé.

### User story

En tant qu'analyste de données, je veux poser une question en langage naturel sur ma source connectée afin d'obtenir un graphique sans écrire de SQL.

### Parcours utilisateur (happy path)

1. L'utilisateur ouvre le module **Explorer** et sélectionne une source active.
2. Il clique sur le bouton "Demander à l'IA" (icône assistant, barre supérieure d'Explorer).
3. Une zone de saisie textuelle apparaît au-dessus de la grille de résultats.
4. Il tape : "Quel est le chiffre d'affaires par région ce mois-ci ?"
5. Un indicateur de chargement s'affiche pendant la génération SQL (max 10 s).
6. Le SQL généré est affiché dans un panneau repliable "SQL généré" (lecture seule par défaut).
7. La requête s'exécute automatiquement ; les résultats s'affichent dans la grille Explorer.
8. Un bouton "Visualiser dans Kepler" permet d'envoyer le résultat vers le module **Kepler** comme un graphique standard.
9. En cas de SQL invalide ou refusé par la whitelist DQL, un message d'erreur explicite s'affiche avec suggestion de reformuler.

### Critères d'acceptation

- [ ] La question est envoyée au endpoint `/api/ai/text-to-sql` avec `sourceId` et `question`.
- [ ] Le schéma de la source (nom des tables, colonnes, types) est injecté dans le prompt LLM sans envoyer de données réelles.
- [ ] Le SQL généré est systématiquement validé par `JdbcUtils.isValidDQLQuery` avant toute exécution ; toute requête non-DQL (INSERT, UPDATE, DROP…) est rejetée.
- [ ] La confiance (`confidence`) est affichée à l'utilisateur (ex : "Confiance : 92 %").
- [ ] Si `confidence < 0.6`, un avertissement "Vérifiez le SQL avant exécution" est affiché.
- [ ] L'utilisateur peut modifier le SQL dans le panneau dédié avant de relancer l'exécution.
- [ ] Le résultat peut être envoyé vers Kepler en un clic et s'affiche comme un graphique ECharts standard.
- [ ] La fonctionnalité est désactivée (bouton grisé + tooltip explicatif) si aucune source JDBC n'est active.
- [ ] Aucun contenu des tables (lignes de données) n'est envoyé au LLM — uniquement le schéma.
- [ ] Le modèle utilisé (`deepseek-coder:6.7b` via Ollama) est loggué côté `movetodata-ai` pour audit.
- [ ] Un test d'intégration couvre le endpoint avec un mock LLM (pas de dépendance Ollama en CI).

### Contraintes souveraineté

- Seul le **schéma** (DDL, noms de colonnes, types) est transmis au LLM — jamais les données ligne par ligne.
- Le modèle `deepseek-coder:6.7b` tourne en local via Ollama ; si Ollama est indisponible, le fallback est `mistral-small` via Mistral AI FR (pas d'autre fallback autorisé).
- Le schéma est extrait côté Boson via `SourceService` et passé au service IA en payload — il ne transite pas par un tiers.

### Wireframe / comportement UI

**Module : Explorer**

- Barre supérieure d'Explorer : ajout d'un bouton "IA" à droite du sélecteur de source. Clic : affiche une zone `<textarea>` sur une ligne, placeholder "Posez votre question en français ou anglais…".
- Sous la zone de saisie : bouton "Générer le SQL" + indicateur spinner pendant l'appel.
- Panneau repliable "SQL généré" (accordéon, fermé par défaut) : affiche le SQL en monospace, bouton "Copier", bouton "Modifier" (active l'édition inline), bouton "Exécuter".
- Badge de confiance affiché à droite du titre du panneau (vert > 80 %, orange 60-80 %, rouge < 60 %).
- Résultats affichés dans la grille Explorer existante.
- Bouton "Ouvrir dans Kepler" dans la barre de résultats — comportement identique à l'envoi manuel d'une `KeplerQuery`.

### Interface API (résumé)

```
POST /api/ai/text-to-sql
Authorization: Bearer {jwt}

Payload :
{
  "sourceId": 42,
  "question": "Quel est le CA par région ce mois-ci ?"
}

Réponse 200 :
{
  "sql": "SELECT region, SUM(ca) FROM ventes WHERE date >= DATE_TRUNC('month', NOW()) GROUP BY region",
  "confidence": 0.92,
  "model": "deepseek-coder:6.7b",
  "tokensUsed": 312
}

Réponse 400 (SQL non-DQL ou requête refusée) :
{
  "error": "INVALID_DQL",
  "message": "La requête générée n'est pas une requête de lecture. Reformulez votre question.",
  "generatedSql": "DROP TABLE ventes"
}

Réponse 503 (Ollama indisponible, pas de fallback configuré) :
{
  "error": "LLM_UNAVAILABLE",
  "message": "Le service IA est temporairement indisponible."
}
```

Proxy Boson : `POST /api/ai/text-to-sql` → délègue à `movetodata-ai:8090/text-to-sql` avec le JWT propagé.

---

## F2 — Augmented Analytics

**Priorité** : P1
**Effort estimé** : L
**Dépendances** : Source active avec données tabulaires, service `movetodata-ai` déployé, Prophet + scikit-learn + PyOD installés, module Kepler opérationnel (affichage `InsightCard` et `AnomalyBadge`).

### User story

En tant qu'analyste, je veux que la plateforme détecte automatiquement des anomalies, génère des prévisions et segmente mes données afin de gagner du temps sur l'analyse exploratoire.

### Parcours utilisateur (happy path)

1. L'utilisateur ouvre un graphique existant dans **Kepler** ou sélectionne une colonne dans **Explorer**.
2. Un bouton "Analyser avec l'IA" (ou icône sparkle) est visible dans la barre d'outils du graphique.
3. Un panneau latéral s'ouvre avec 4 onglets : "Anomalies", "Prévision", "Segments", "Résumé".
4. L'utilisateur sélectionne l'onglet "Anomalies" et choisit les colonnes X (date) et Y (valeur).
5. Il clique "Lancer l'analyse" ; un spinner s'affiche pendant le calcul (max 30 s pour datasets < 100k lignes).
6. Les points anomaliques sont mis en évidence sur le graphique Kepler avec un `AnomalyBadge` (point rouge cerclé).
7. Une `InsightCard` apparaît sous le graphique : "3 anomalies détectées entre le 12 et le 15 mars 2026. Pic inhabituel de +230 % sur la métrique CA."
8. Pour l'onglet "Prévision" : l'utilisateur choisit l'horizon (7 / 30 / 90 jours) et la prévision s'ajoute au graphique en pointillés avec intervalle de confiance.
9. Pour "Segments" : le résultat du clustering s'affiche comme un scatter plot coloré par segment dans Kepler.
10. Pour "Résumé" : un texte généré par LLM décrit le dataset en 3-5 phrases.

### Critères d'acceptation

- [ ] Les 4 types d'analyse (`anomaly`, `forecast`, `cluster`, `summary`) sont implémentés et testés indépendamment.
- [ ] Détection d'anomalies : IsolationForest (scikit-learn) avec seuil de contamination configurable (défaut : 5 %) ; résultat : liste d'index de lignes anomaliques + score.
- [ ] Forecasting : Prophet avec intervalles de confiance (80 % et 95 %) ; les points futurs sont distingués visuellement (trait pointillé).
- [ ] Clustering : K-Means (k auto-détecté par elbow method, max k=10) ou DBSCAN si données non-convexes ; chaque cluster est coloré différemment dans Kepler.
- [ ] Résumé textuel généré par LLM (`mistral:7b`) à partir des statistiques descriptives du dataset — jamais à partir des données brutes ligne par ligne.
- [ ] Les résultats IA sont retournés comme des `KeplerQuery` standards et s'intègrent au pipeline de rendu ECharts existant.
- [ ] `AnomalyBadge` apparaît sur les graphiques de type line/bar quand des anomalies sont présentes.
- [ ] `InsightCard` est cliquable et affiche le détail de l'insight (valeur, date, écart).
- [ ] L'analyse est limitée à 100 000 lignes par appel (au-delà, message d'avertissement + échantillonnage automatique).
- [ ] Le temps de réponse est inférieur à 30 s pour < 10 000 lignes, inférieur à 90 s pour < 100 000 lignes.
- [ ] Aucune ligne de données brute n'est transmise au LLM pour le résumé — uniquement les statistiques descriptives (min, max, moyenne, écart-type, p25, p75, shape).
- [ ] Tests d'intégration avec datasets de fixtures pour chaque type d'analyse.

### Contraintes souveraineté

- Les calculs d'anomalies, forecasting et clustering sont réalisés **entièrement localement** (scikit-learn, Prophet, PyOD) — zéro appel LLM pour ces traitements.
- Le LLM (`mistral:7b` via Ollama) est appelé uniquement pour le résumé textuel, et uniquement avec des statistiques agrégées (pas de données individuelles).
- Si le LLM est indisponible, les 3 autres analyses (anomaly, forecast, cluster) restent disponibles — le résumé affiche "Service IA indisponible" sans bloquer les autres fonctions.

### Wireframe / comportement UI

**Module : Kepler (graphiques existants) + Explorer**

- Barre d'outils graphique Kepler : icône "IA / sparkle" à côté des icônes d'export existantes.
- Clic : panneau latéral droit (drawer, 400 px) avec 4 onglets.
- Onglet Anomalies : sélecteurs colonne X / Y + bouton "Détecter" + affichage liste d'anomalies (date, valeur, score) + overlay sur le graphique principal.
- Onglet Prévision : sélecteur horizon + bouton "Prévoir" + ajout de la série "Prévision" au graphique Kepler (pointillés bleus, zone de confiance semi-transparente).
- Onglet Segments : sélecteurs colonnes X / Y + bouton "Segmenter" + nouveau graphique scatter dans Kepler coloré par cluster.
- Onglet Résumé : bouton "Générer le résumé" + affichage texte dans une card grisée.
- `AnomalyBadge` : pastille rouge avec nombre d'anomalies, affichée en haut à droite de chaque graphique concerné. Clic : ouvre le drawer sur l'onglet Anomalies.
- `InsightCard` : carte sous le graphique (fond bleu pâle), titre + corps texte + lien "Voir les détails".

### Interface API (résumé)

```
POST /api/ai/insights
Authorization: Bearer {jwt}

Payload :
{
  "sourceId": 42,
  "columnX": "date",
  "columnY": "ca",
  "type": "anomaly" | "forecast" | "cluster" | "summary",
  "options": {
    "forecastHorizon": 30,
    "clusterK": null,
    "anomalyContamination": 0.05
  }
}

Réponse 200 (anomaly) :
{
  "type": "anomaly",
  "insights": [
    { "rowIndex": 142, "date": "2026-03-12", "value": 48200, "score": 0.91 }
  ],
  "chartData": { /* KeplerQuery overlay */ },
  "model": "IsolationForest (scikit-learn 1.4)"
}

Réponse 200 (forecast) :
{
  "type": "forecast",
  "insights": [ { "date": "2026-08-01", "predicted": 52000, "lower": 44000, "upper": 61000 } ],
  "chartData": { /* série Kepler supplémentaire */ },
  "model": "Prophet 1.1.5"
}

Réponse 200 (summary) :
{
  "type": "summary",
  "text": "Ce jeu de données couvre 18 mois de ventes et contient 3 segments distincts...",
  "model": "mistral:7b (Ollama)"
}
```

---

## F3 — Assistant IA conversationnel

**Priorité** : P2
**Effort estimé** : L
**Dépendances** : F1 (Text-to-SQL) opérationnel, PostgreSQL disponible pour persistance historique, service `movetodata-ai` déployé avec SSE activé, module Kepler opérationnel.

### User story

En tant qu'utilisateur de la plateforme, je veux pouvoir dialoguer avec un assistant IA dans une sidebar persistante afin d'explorer mes données, générer des graphiques et comprendre mes résultats sans changer d'outil.

### Parcours utilisateur (happy path)

1. L'utilisateur clique sur l'icône "Assistant IA" dans la navigation globale (disponible dans Kepler, Explorer, Connect).
2. Une sidebar de chat s'ouvre à droite (300-400 px, superposée ou push du contenu selon la résolution).
3. L'historique de la session courante est chargé depuis PostgreSQL (les 50 derniers messages max).
4. Le contexte actif (source ouverte + graphiques visibles) est affiché en header de la sidebar.
5. L'utilisateur tape "Montre-moi les ventes du mois dernier par produit sous forme de camembert."
6. La réponse arrive en streaming (SSE) — les tokens s'affichent progressivement.
7. L'assistant génère le SQL (F1), exécute la requête et insère un graphique en camembert directement dans la sidebar (miniature) avec un bouton "Ouvrir dans Kepler".
8. L'utilisateur demande "Pourquoi la région Nord sous-performe ?" — l'assistant répond avec une analyse textuelle et suggère un drill-down.
9. L'utilisateur peut effacer l'historique de la conversation avec un bouton "Nouvelle conversation".
10. La sidebar est fermable et rouvrira au même état (contexte + historique) lors de la prochaine ouverture.

### Critères d'acceptation

- [ ] La sidebar `AiAssistant` est accessible depuis Kepler, Explorer et Connect sans rechargement de page.
- [ ] L'historique de conversation est persisté par utilisateur dans PostgreSQL (table `ai_chat_messages`, soft-delete sur "Nouvelle conversation").
- [ ] Le contexte IA inclut : `sourceId` actif, liste des `chartId` ouverts dans Kepler, et le schéma de la source (pas les données).
- [ ] Les réponses sont streamées via Server-Sent Events (SSE) ; chaque token est affiché dès réception.
- [ ] L'assistant peut : générer un graphique (délègue à F1 + Kepler), décrire un résultat, suggérer des analyses (délègue à F2), répondre en langage naturel.
- [ ] Les graphiques générés dans la sidebar sont des miniatures cliquables qui ouvrent le graphique complet dans Kepler.
- [ ] Le bouton "Nouvelle conversation" efface l'affichage et crée une nouvelle session (soft-delete des messages précédents en base, non supprimés physiquement).
- [ ] L'historique est limité à 50 messages chargés ; les messages plus anciens sont archivés et accessibles via un bouton "Charger plus".
- [ ] Si `movetodata-ai` est indisponible, la sidebar affiche un message "Assistant temporairement indisponible" sans crasher la page.
- [ ] Le modèle utilisé est loggué pour chaque message (audit souveraineté).
- [ ] Tests : mock SSE en CI pour valider le streaming sans dépendance Ollama.

### Contraintes souveraineté

- Le contexte transmis au LLM contient le schéma de source et l'historique de conversation — jamais les données ligne par ligne.
- L'historique de conversation est stocké en PostgreSQL sur l'infrastructure MoveToData — pas de stockage côté provider LLM.
- Modèle principal : `mistral:7b` via Ollama self-hosted. Fallback : `mistral-small` via Mistral AI FR.
- La sidebar n'intègre aucun SDK tiers de chat (pas de Intercom, Crisp, Drift ou équivalent US).

### Wireframe / comportement UI

**Disponible dans : Kepler, Explorer, Connect**

- Icône "bulle de chat + étoile" dans la barre de navigation globale (en haut à droite), persistante sur tous les modules.
- Clic : sidebar droite animée (slide-in, 380 px de large). Sur mobile / petit écran : overlay plein écran.
- Header sidebar : nom de la source active + indicateur "3 graphiques ouverts" (contextuel). Bouton "Fermer" (X) + bouton "Nouvelle conversation" (icône crayon).
- Zone de messages : scroll vertical, bulles utilisateur (droite, fond bleu) et bulles assistant (gauche, fond gris).
- Streaming : les tokens apparaissent progressivement ; un curseur clignotant indique la génération en cours.
- Graphiques inline : miniature ECharts 200×120 px dans la bulle assistant. Bouton "Ouvrir dans Kepler" sous la miniature.
- Zone de saisie : `<textarea>` auto-resize (1-4 lignes), bouton Envoyer, raccourci `Ctrl+Entrée`.
- État vide (nouvelle conversation) : 3 suggestions de questions prédéfinies selon la source active (ex : "Quelle est la tendance des 30 derniers jours ?").

### Interface API (résumé)

```
POST /api/ai/chat   (SSE streaming)
Authorization: Bearer {jwt}
Content-Type: application/json
Accept: text/event-stream

Payload :
{
  "sessionId": "uuid-v4",
  "message": "Montre-moi les ventes du mois dernier par produit sous forme de camembert.",
  "context": {
    "sourceId": 42,
    "openChartIds": [101, 102]
  }
}

Flux SSE (chunks) :
data: {"type": "token", "content": "Voici"}
data: {"type": "token", "content": " les ventes"}
data: {"type": "chart", "keplerQuery": { /* KeplerQuery standard */ }}
data: {"type": "done", "model": "mistral:7b", "tokensUsed": 487}

Réponse erreur (non-streaming) :
HTTP 503 : { "error": "LLM_UNAVAILABLE" }

GET /api/ai/chat/history?sessionId={uuid}&limit=50
→ { "messages": [ { "role": "user"|"assistant", "content": "...", "createdAt": "..." } ] }

DELETE /api/ai/chat/session/{sessionId}  (soft-delete)
→ HTTP 204
```

---

## F4 — Smart Connector

**Priorité** : P2
**Effort estimé** : S
**Dépendances** : Module Connect opérationnel, au moins un connecteur JDBC actif, service `movetodata-ai` déployé, 25 types de graphiques Kepler disponibles.

### User story

En tant qu'administrateur ou analyste connectant une nouvelle source de données, je veux que la plateforme me suggère automatiquement les graphiques les plus pertinents et m'informe de la qualité des données afin de démarrer l'analyse immédiatement après la connexion.

### Parcours utilisateur (happy path)

1. L'utilisateur est dans le module **Connect** et vient de valider la connexion à une nouvelle source (ex : base PostgreSQL "ventes_2026").
2. Au lieu d'arriver sur une page vide, un écran "Analyse de votre source" s'affiche pendant 5-15 s avec une barre de progression.
3. La plateforme affiche 3 sections :
   - **Score qualité données** : jauge globale (0-100) avec détail par dimension (complétude, doublons, outliers).
   - **Types de données détectés** : badges colorés (time series, catégoriel, numérique, géographique).
   - **Graphiques suggérés** : 3 à 6 miniatures de graphiques Kepler pré-configurés, avec titre explicatif.
4. L'utilisateur clique sur "Ouvrir dans Kepler" sur l'une des suggestions.
5. Le graphique s'ouvre dans Kepler avec la configuration pré-remplie (colonnes, type de graphique).
6. L'utilisateur peut ignorer les suggestions et aller directement dans Explorer.

### Critères d'acceptation

- [ ] L'analyse Smart Connector est déclenchée automatiquement à chaque nouvelle connexion réussie dans Connect.
- [ ] Le score qualité est calculé sur un échantillon de 1 000 lignes maximum (pas de scan complet de la source).
- [ ] Dimensions du score qualité : complétude (% de valeurs non-nulles), unicité (% de doublons), cohérence des types, ratio d'outliers.
- [ ] La détection de type de données (time series, catégoriel, numérique, géographique) est basée sur les noms de colonnes + types SQL + distribution des valeurs sur l'échantillon.
- [ ] Les suggestions de graphiques sont choisies parmi les 25 types disponibles dans Kepler (référence : `dataviz-echarts-gap-analysis.md`).
- [ ] Chaque suggestion contient : type de graphique, colonnes pré-sélectionnées, titre suggéré, raison de la suggestion (1 phrase).
- [ ] Le bouton "Ouvrir dans Kepler" crée un graphique Kepler pré-configuré avec les colonnes suggérées.
- [ ] L'analyse est rejouable manuellement depuis l'écran de détail d'une source dans Connect (bouton "Ré-analyser la source").
- [ ] Si l'analyse échoue (timeout, erreur), la connexion reste valide et l'utilisateur accède à Connect normalement.
- [ ] Le score qualité est affiché de façon permanente dans la liste des sources de Connect (badge coloré sur chaque source).
- [ ] Tests : fixture avec un dataset de qualité connue pour valider le scoring.

### Contraintes souveraineté

- L'analyse est entièrement locale (scikit-learn + règles heuristiques Python) — aucun LLM n'est appelé pour le scoring qualité et la détection de type.
- Le LLM peut être appelé optionnellement pour générer les titres de graphiques suggérés (uniquement avec les métadonnées schéma + types détectés, pas les données).
- L'échantillon de 1 000 lignes ne quitte jamais l'infrastructure MoveToData.

### Wireframe / comportement UI

**Module : Connect — écran post-connexion**

- Après validation d'une connexion dans Connect, redirection vers une page intermédiaire "Analyse de votre source [Nom source]" au lieu d'un retour immédiat à la liste.
- Barre de progression animée (3 étapes : "Lecture du schéma", "Analyse qualité", "Suggestions de graphiques").
- Section Score qualité : grande jauge circulaire centrale (score global 0-100, couleur : rouge < 40, orange 40-70, vert > 70) + 4 jauges linéaires en dessous (complétude, unicité, cohérence, outliers).
- Section Types détectés : badges horizontaux ("Séries temporelles", "Données catégorielles", "Valeurs géographiques"…) avec icône.
- Section Graphiques suggérés : grille 3 colonnes de cards. Chaque card : miniature de graphique (placeholder ECharts), titre, raison, bouton "Ouvrir dans Kepler".
- Bouton "Ignorer et aller dans Explorer" (lien secondaire en bas de page).
- Dans la liste des sources Connect : badge qualité (vert/orange/rouge) affiché sur chaque source. Tooltip au hover : détail des 4 dimensions.

### Interface API (résumé)

```
POST /api/ai/smart-connector/analyze
Authorization: Bearer {jwt}

Payload :
{
  "sourceId": 42,
  "sampleSize": 1000
}

Réponse 200 :
{
  "qualityScore": {
    "global": 78,
    "completeness": 92,
    "uniqueness": 85,
    "consistency": 71,
    "outlierRatio": 0.03
  },
  "detectedTypes": ["time_series", "categorical", "numeric"],
  "chartSuggestions": [
    {
      "chartType": "line",
      "columnX": "date",
      "columnY": "ca",
      "title": "Évolution du CA dans le temps",
      "reason": "Colonne 'date' détectée comme série temporelle (format ISO-8601, 365 valeurs distinctes)."
    },
    {
      "chartType": "bar",
      "columnX": "region",
      "columnY": "ca",
      "title": "CA par région",
      "reason": "Colonne 'region' catégorielle avec 8 modalités."
    }
  ],
  "analyzedAt": "2026-07-30T14:32:00Z",
  "model": "heuristic+scikit-learn (no LLM)"
}

GET /api/ai/smart-connector/score/{sourceId}
→ { "qualityScore": { ... }, "analyzedAt": "..." }
```

---

## Tableau récapitulatif

| Feature | Priorité | Effort | Valeur métier | Dépendances |
|---------|----------|--------|---------------|-------------|
| F1 — Text-to-SQL | P1 | M | Accès données sans SQL — réduit la dépendance aux équipes data. Différenciateur vs Metabase (absent) et argument Palantir (Apollo). | Ollama + deepseek-coder, JdbcUtils DQL whitelist |
| F2 — Augmented Analytics | P1 | L | Insights proactifs sans configuration — valeur immédiate pour les analystes métier. Différenciateur majeur vs Metabase. | F1 partiel, Prophet, scikit-learn, PyOD |
| F3 — Assistant conversationnel | P2 | L | Réduction du time-to-insight — interface unifiée pour toutes les analyses. Argument enterprise fort. | F1 complet, F2 partiel, PostgreSQL, SSE |
| F4 — Smart Connector | P2 | S | Réduction du time-to-first-chart après connexion — améliore l'onboarding et réduit le churn. | Connect opérationnel, 25 graphiques Kepler |

---

## Recommandation de séquençage sprint

### Sprint 1 — Infrastructure IA + F4 (2 semaines)

Déployer d'abord le service `movetodata-ai` (FastAPI, Ollama, Redis) et intégrer le proxy Boson `/api/ai/**`. Implémenter F4 (Smart Connector) en parallèle : effort S, aucune dépendance LLM forte, valeur immédiate à la connexion d'une source. F4 permet de valider l'infrastructure IA sans risque LLM.

### Sprint 2 — F1 Text-to-SQL (2 semaines)

F1 est la feature la plus visible et la plus simple à démontrer (démo Palantir). Elle réutilise `JdbcUtils` existant et valide le pattern LLM → KeplerQuery. Les critères d'acceptation sont précis et testables sans LLM réel (mock).

### Sprint 3 — F2 Augmented Analytics, partie locale (3 semaines)

Implémenter anomaly detection, forecasting et clustering (zéro LLM requis pour ces 3 types). Le résumé textuel (LLM) peut être livré en fin de sprint ou reporté. Cette séquence réduit le risque de blocage sur Ollama.

### Sprint 4 — F3 Assistant conversationnel (3 semaines)

F3 dépend de F1 et F2. C'est la feature la plus complexe (SSE, persistance historique, gestion du contexte multi-module). À implémenter en dernier pour capitaliser sur les patterns validés en Sprint 2-3.

**Justification de la séquence** : les features P1 (F1, F2) livrent la valeur métier différenciante. F4 est mis en premier car il sécurise l'infrastructure et améliore l'onboarding sans dépendance LLM forte. F3 est dernier car son impact dépend de F1 et F2 pour être démontrable.

---

## Risques produit

### R1 — Hallucinations LLM (Text-to-SQL, Résumé)

**Description** : le LLM génère un SQL syntaxiquement valide mais sémantiquement incorrect (mauvaise table, mauvais agrégat), ou un résumé textuel inexact.
**Mitigation** : affichage systématique du SQL généré avant exécution, score de confiance visible, mode "édition manuelle" accessible, whitelist DQL obligatoire.
**Résiduel** : l'utilisateur reste responsable de la validation du SQL. Documenter dans la UI.

### R2 — Latence et performance LLM

**Description** : `deepseek-coder:6.7b` sur CPU peut prendre 15-30 s par requête sur du matériel modeste. L'assistant conversationnel avec contexte long (50 messages) peut dépasser 60 s.
**Mitigation** : cache Redis sur les requêtes identiques, streaming SSE pour masquer la latence perçue, GPU recommandé pour les déploiements Enterprise, timeout de 30 s avec message d'erreur explicite.
**Résiduel** : les déploiements freemium/open source sur CPU auront une expérience dégradée. Documenter la configuration matérielle minimale recommandée.

### R3 — Souveraineté — dérive vers des providers non souverains

**Description** : un développeur intègre par inadvertance un SDK tiers (OpenAI, Langfuse cloud US, etc.) ou configure un fallback vers AWS Bedrock.
**Mitigation** : audit automatique des dépendances Python (`pip-audit` + liste noire CI), documentation explicite des providers interdits dans `.claude/agents/ai.md`, revue de code obligatoire sur tout changement de dépendance IA.
**Résiduel** : le fallback Mistral AI FR est souverain mais commercial. S'assurer que le DPA (Data Processing Agreement) Mistral est signé avant activation.

### R4 — Adoption utilisateur (Text-to-SQL, Assistant)

**Description** : les utilisateurs ne font pas confiance aux résultats générés par l'IA et n'adoptent pas les features.
**Mitigation** : transparence totale (SQL généré visible, modèle affiché, score de confiance), possibilité de modifier le SQL, feedback utilisateur sur chaque résultat (pouce haut/bas), onboarding avec exemples de questions pour chaque source.
**Résiduel** : courbe d'apprentissage sur la formulation des questions. Prévoir des suggestions de questions contextuelles (déjà inclus dans F3).

### R5 — Qualité du schéma source (Text-to-SQL)

**Description** : si la source connectée a des noms de tables/colonnes cryptiques (ex : `T_FACT_001`, `COL_A`), le LLM ne peut pas générer de SQL pertinent.
**Mitigation** : permettre à l'utilisateur d'ajouter des descriptions de colonnes dans Connect (enrichissement métadonnées). Injecter ces descriptions dans le prompt.
**Résiduel** : feature de documentation de schéma non incluse dans ce sprint — à planifier en backlog post-F1.

### R6 — Scalabilité du service IA en open source

**Description** : en mode freemium/open source, un seul Ollama servi localement peut être saturé par des requêtes concurrentes.
**Mitigation** : file d'attente Redis sur `movetodata-ai`, concurrence limitée à 2 requêtes LLM simultanées en open source, message "IA occupée, réessayez dans quelques secondes".
**Résiduel** : les limites de concurrence deviennent un levier de conversion Pro/Enterprise (pas de limite de concurrence en Pro).
