# Gap Analysis — Data Visualisation MoveToData vs ECharts

> Document rédigé par : Product Owner MoveToData  
> Date : 2026-07-28  
> Contexte : Atteindre la parité fonctionnelle avec Metabase (25 types de graphiques) en s'appuyant sur le catalogue Apache ECharts. Ce document est directement actionnable par un développeur React/ECharts.

---

## 1. Graphiques actuellement implémentés

Le code de référence est `frontend/src/Apps/Kepler/`. Les types sont déclarés dans `charts.config.tsx` et exposés dans `SliderController.tsx`. Le moteur de rendu principal est `KeplerEChart.tsx` pour les types ECharts ; les exceptions sont le tableau (React custom) et la carte (Kepler.gl/Leaflet).

| # | Nom interne (chartType) | Nom affiché | Type ECharts natif | Fichier factory principal | Statut |
|---|---|---|---|---|---|
| 1 | `pieChart` | Pie / Donut | `pie` | `getPieChartOptions.ts` | Actif |
| 2 | `bigNumber` | Big Number (KPI) | `graphic` (texte SVG) | `getBigChartOptions.ts` | Actif |
| 3 | `wordCloudChart` | Word Cloud | extension `echarts-wordcloud` | `getWordCloudChartOptions.ts` | Actif |
| 4 | `horizontalBarChart` | Horizontal Bar | `bar` (axes inversés) | `getHorizontalBarChartOptions.ts` | Actif |
| 5 | `VerticalAxisChart` + `seriesType: barChart` | Bar Chart (vertical) | `bar` | `getVerticalAxisChartsOptions.ts` | Actif |
| 6 | `VerticalAxisChart` + `seriesType: lineChart` | Line Chart | `line` | `getVerticalAxisChartsOptions.ts` | Actif |
| 7 | `VerticalAxisChart` + `seriesType: lineAreaChart` | Area Chart | `line` + `areaStyle` | `getVerticalAxisChartsOptions.ts` | Actif |
| 8 | `VerticalAxisChart` + `seriesType: scatterChart` | Scatter Chart | `scatter` | `getVerticalAxisChartsOptions.ts` | Actif |
| 9 | `radarChart` | Radar / Spider | `radar` | `getRadarChartOptions.ts` | Actif |
| 10 | `sunBurstChart` | Sunburst | `sunburst` | `getSunBurstChartOptions.ts` | Actif |
| 11 | `gaugeChart` | Gauge / Speedometer | `gauge` | `getGaugeChartOptions.ts` | Actif |
| 12 | `treeMapChart` | Treemap | `treemap` | `getTreeMapChartOptions.ts` | Actif |
| 13 | `waterFallChart` | Waterfall | `bar` (stack trick) | `getWaterFallChartOptions.ts` | Actif |
| 14 | `table` | Data Table | Composant React custom | `KeplerTableChart.tsx` | Actif |
| 15 | `mapChart` | Map (géographique) | Kepler.gl + Leaflet | `KeplerMapChart.jsx` | **Désactivé** (disabled:true dans SliderController) |
| 16 | `parameterChart` | Paramètre / Filtre | Widget UI (pas de viz) | `getParameterChartOptions.ts` | Actif |

**Note architecture :** `VerticalAxisChart` est un conteneur unique qui délègue le `type` ECharts en fonction du champ `seriesType` de chaque série. Ce patron permet des graphiques combinés (bar + line sur un même axe), ce qui correspond au type "Combo" de Metabase. C'est un avantage fort à documenter dans la communication produit.

**Compte de types visuels distincts actifs : 14** (on exclut `mapChart` désactivé et `parameterChart` qui est un filtre, non une visualisation de données). Le `VerticalAxisChart` multi-series couvre 4 sous-types distincts.

---

## 2. Catalogue ECharts complet

Source : https://echarts.apache.org/en/cheat-sheet.html (consulté 2026-07-28) + ECharts 6.x (juillet 2025).

### 2.1 Series (types de graphiques natifs)

| Catégorie | Type ECharts (`series.type`) | Depuis |
|---|---|---|
| **Basiques** | `bar` | v1 |
| | `line` | v1 |
| | `pie` | v1 |
| | `scatter` | v1 |
| | `effectScatter` (scatter animé) | v3 |
| **Financiers** | `candlestick` | v3 |
| | `boxplot` | v3 |
| **Statistiques** | `heatmap` | v3 |
| | `parallel` | v3 |
| **Hiérarchiques** | `tree` | v3 |
| | `treemap` | v3 |
| | `sunburst` | v3 |
| **Relationnels** | `graph` | v2 |
| | `sankey` | v3 |
| | `chord` (nouveau) | v6 |
| **Flux** | `lines` (flux géo) | v3 |
| | `themeRiver` | v3 |
| **Indicateurs** | `gauge` | v2 |
| | `funnel` | v2 |
| **Spéciaux** | `radar` | v1 |
| | `pictorialBar` | v3 |
| | `map` | v2 |
| | `custom` (personnalisé) | v3 |

**Extensions officielles (npm)**

| Extension | Fonctionnalité |
|---|---|
| `echarts-wordcloud` | Word Cloud (déjà intégré dans MoveToData) |
| `echarts-gl` | 3D Bar, 3D Scatter, 3D Surface, Globe |
| `echarts-liquidfill` | Jauge de remplissage liquide |
| `echarts-stat` | Régression, clustering, histogramme |

### 2.2 Systèmes de coordonnées

`grid` (cartésien 2D), `polar` (polaire), `geo` (géographique), `singleAxis`, `calendar`.

---

## 3. Gap Analysis détaillée

Les types ECharts présents dans la bibliothèque et absents de MoveToData sont listés ci-dessous avec leur spec fonctionnelle et technique. Les types jugés hors-scope (3D, `custom`, `lines` géo) sont mentionnés en fin de section pour mémoire.

---

### 3.1 Funnel — Entonnoir

**Priorité : P0 (indispensable pour parité Metabase)**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser des étapes d'un processus avec des volumes décroissants. Exemples : taux de conversion d'un pipeline de ventes (Leads → Qualifiés → Opportunités → Clients), étapes d'un pipeline ETL (records ingérés → validés → transformés → chargés), entonnoir d'onboarding utilisateur sur la plateforme.
- **Public cible :** Data analysts, responsables sales/marketing, ops.
- **Valeur différenciante MoveToData :** Complémente le Gauge pour le suivi de KPIs de transformation. Type présent dans Metabase, Superset, et Palantir Foundry.
- **Quand l'utiliser :** Données ordonnées représentant des étapes successives où chaque étape est un sous-ensemble de la précédente. Minimum 2 étapes, idéalement 4-7.

#### Spec technique

- **Composant ECharts :** `series.type: 'funnel'`
- **Options clés :**
  ```ts
  series: [{
    type: 'funnel',
    left: '10%',
    width: '80%',
    min: 0,
    max: 100,           // valeur max pour calcul des proportions
    minSize: '0%',
    maxSize: '100%',
    sort: 'descending', // ou 'ascending' (pyramide inversée)
    gap: 2,             // écart entre tranches
    label: { show: true, position: 'inside' },
    labelLine: { length: 10, lineStyle: { width: 1 } },
    data: [
      { value: 60, name: 'Visite' },
      { value: 40, name: 'Qualification' },
      { value: 20, name: 'Opportunité' },
      { value: 80, name: 'Clôture' }
    ]
  }]
  ```
- **Mapping données → config :**
  - Colonne dimension (string) → `data[].name`
  - Colonne métrique (number) → `data[].value`
  - Tri par valeur décroissante recommandé par défaut
- **Fichier factory à créer :** `frontend/src/Apps/Kepler/chart/chartOptionsFactory/getFunnelChartOptions.ts`
- **Fichier config à modifier :** `charts.config.tsx` (ajout `funnelChart` dans `eChartTypes` et objet de config)
- **Fichier slider à modifier :** `SliderController.tsx` (ajout entrée `funnelChart`)
- **Customizer à créer :** `customizeForm/FunnelChartCustomizer.tsx` (options : sort direction, gap, minSize, maxSize, label position)
- **Effort estimé : S** — Le pattern query est identique à `pieChart` (dimension + métrique). Le rendu ECharts est natif. Environ 1-2 jours de dev.

---

### 3.2 Heatmap — Carte de chaleur

**Priorité : P0 (indispensable pour parité Metabase)**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser l'intensité d'une valeur sur deux axes catégoriels ou temporels. Exemples : volume d'erreurs par heure × jour de la semaine (debugging pipeline), corrélation entre colonnes d'un dataset, nombre de requêtes par dataset × utilisateur (usage analytics interne MoveToData), performance d'un modèle ML par paramètre.
- **Public cible :** Data engineers, data scientists, DBA.
- **Valeur différenciante :** Type emblématique de l'analyse exploratoire de données. Présent dans Metabase, Superset, Kibana. Absent = gap perçu fort par les data engineers.
- **Quand l'utiliser :** Trois colonnes minimum : axe X (catégoriel/temps), axe Y (catégoriel), valeur (numérique). Idéal pour tables croisant 2 dimensions et 1 métrique.

#### Spec technique

- **Composant ECharts :** `series.type: 'heatmap'`
- **Options clés :**
  ```ts
  // Heatmap cartésien (le plus courant)
  xAxis: { type: 'category', data: xCategories },
  yAxis: { type: 'category', data: yCategories },
  visualMap: {
    min: 0,
    max: maxValue,
    calculable: true,
    orient: 'horizontal',
    left: 'center',
    bottom: '15%',
    inRange: { color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4'] }
  },
  series: [{
    type: 'heatmap',
    data: [[xIndex, yIndex, value], ...], // format [col, row, val]
    label: { show: true },
    emphasis: { itemStyle: { shadowBlur: 10 } }
  }]
  ```
- **Mapping données → config :**
  - Colonne X (string ou date) → `xAxis.data` + `data[][0]`
  - Colonne Y (string) → `yAxis.data` + `data[][1]`
  - Colonne valeur (number) → `data[][2]` + `visualMap.min/max`
- **QueryForm :** Nécessite un contrôleur à 3 champs (X, Y, Valeur). Peut réutiliser `XAxisController` et `DimensionController` pour X/Y. Le champ valeur suit le pattern standard des séries.
- **Fichier factory à créer :** `getHeatmapChartOptions.ts`
- **Fichier config à modifier :** `charts.config.tsx` (nouveau type `heatmapChart`, `isAxisChart: true`, `isSingleSeries: false`)
- **Customizer à créer :** `HeatmapCustomizer.tsx` (palette de couleurs via `visualMap`, affichage labels dans cellules, min/max manuel)
- **Effort estimé : M** — Le schéma de données (3 colonnes) diffère du pattern habituel. Il faut un nouveau contrôleur QueryForm dédié et une transformation backend pour produire le format `[x, y, val]`. Environ 3-4 jours de dev.

---

### 3.3 Boxplot — Boîte à moustaches

**Priorité : P1**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser la distribution statistique d'une variable numérique (médiane, quartiles, outliers). Exemples : distribution des temps de réponse d'un pipeline par source de données, distribution des valeurs d'une colonne pour auditer un dataset, comparaison de distributions entre plusieurs groupes (A/B test).
- **Public cible :** Data scientists, data analysts avancés.
- **Valeur différenciante :** Aucun concurrent direct dans la liste Metabase (Metabase ne l'a pas nativement), ce qui est un avantage MoveToData vs Metabase pour les profils data science. Présent dans Superset.
- **Quand l'utiliser :** Une colonne catégorielle de groupement (facultative) + une colonne numérique à analyser. ECharts calcule automatiquement les statistiques via `echarts-stat`.

#### Spec technique

- **Composant ECharts :** `series.type: 'boxplot'`
- **Options clés :**
  ```ts
  // Option A : données pré-calculées (backend envoie [min, Q1, median, Q3, max])
  series: [{
    type: 'boxplot',
    data: [
      [min, Q1, median, Q3, max], // une boîte par catégorie
    ],
    itemStyle: { color: '#4a8ede', borderColor: '#2c5f9e' }
  }]
  // Option B : calcul côté frontend via echarts-stat
  // import { transform } from 'echarts-stat'
  // const result = transform.boxplot(rawData)
  ```
- **Recommandation :** Déléguer le calcul des quartiles au backend (Spring Boot) pour supporter de grands datasets. Le backend expose `[min, Q1, median, Q3, max]` par groupe.
- **Mapping données → config :**
  - Colonne groupe (string, optionnel) → `xAxis.data`
  - Colonne valeur (number) → agrégation backend → `data[][]`
- **Endpoint backend nouveau :** `POST /api/chart/boxplot` — reçoit `{ datasetId, column, groupBy? }`, retourne les 5 statistiques par groupe.
- **Fichier factory à créer :** `getBoxplotChartOptions.ts`
- **Effort estimé : M** — Nécessite un nouveau type d'agrégation backend. Le rendu ECharts est simple mais le pipeline de données est spécifique. Environ 3-5 jours (back + front).

---

### 3.4 Candlestick — Chandeliers japonais (OHLC)

**Priorité : P2**

#### Spec fonctionnelle

- **Cas d'usage :** Représenter des données financières OHLC (Open, High, Low, Close) sur une période de temps. Exemples : visualisation de cours d'actions ou de cryptomonnaies, analyse de séries temporelles avec 4 valeurs agrégées par période (ex : min, max, moyenne début/fin de période dans un contexte non financier).
- **Public cible :** Analystes financiers, traders, équipes data de fintech.
- **Segment cible :** Enterprise. Moins pertinent en open source / freemium.
- **Quand l'utiliser :** Exactement 4 colonnes numériques + 1 colonne temporelle.

#### Spec technique

- **Composant ECharts :** `series.type: 'candlestick'`
- **Options clés :**
  ```ts
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value', scale: true },
  series: [{
    type: 'candlestick',
    // data format: [open, close, low, high]
    data: [[open, close, low, high], ...],
    itemStyle: {
      color: '#06b96b',       // bullish (close > open)
      color0: '#ef4665',      // bearish (close < open)
      borderColor: '#06b96b',
      borderColor0: '#ef4665'
    }
  }]
  ```
- **Mapping données → config :**
  - Colonne date/temps → `xAxis.data`
  - 4 colonnes numériques → `data[][0..3]` (ordre : open, close, low, high)
- **QueryForm :** Formulaire spécifique avec 4 sélecteurs de colonnes nommés (Open, High, Low, Close).
- **Fichier factory à créer :** `getCandlestickChartOptions.ts`
- **Effort estimé : M** — Formulaire de saisie spécifique (4 colonnes obligatoires), validation stricte. Environ 3 jours de dev.

---

### 3.5 Sankey — Diagramme de flux Sankey

**Priorité : P1**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser des flux entre nœuds avec proportionnalité des volumes. Exemples : flux de données entre systèmes sources → transformations → destinations (data lineage simplifié), répartition budgétaire (budget → départements → postes), parcours utilisateur (page A → page B → conversion/sortie), flux de records par étape d'un pipeline ETL.
- **Public cible :** Data engineers (lineage), directeurs data, architectes data.
- **Valeur différenciante :** Particulièrement fort dans le contexte data platform pour visualiser les pipelines. Aucun concurrent direct natif dans Metabase. Présent dans Grafana.
- **Quand l'utiliser :** Données sous forme de triplets (source, cible, valeur). Minimum 2 nœuds, idéalement 3-8 niveaux.

#### Spec technique

- **Composant ECharts :** `series.type: 'sankey'`
- **Options clés :**
  ```ts
  series: [{
    type: 'sankey',
    layout: 'none',
    emphasis: { focus: 'adjacency' },
    // Liste de nœuds uniques
    nodes: [
      { name: 'Source A' },
      { name: 'Transform B' },
      { name: 'Target C' }
    ],
    // Liste de liens avec valeurs
    links: [
      { source: 'Source A', target: 'Transform B', value: 5 },
      { source: 'Transform B', target: 'Target C', value: 3 }
    ],
    lineStyle: { color: 'gradient', curveness: 0.5 },
    label: { position: 'right' },
    orient: 'horizontal' // ou 'vertical'
  }]
  ```
- **Mapping données → config :**
  - Colonne source (string) → `links[].source` + `nodes[]`
  - Colonne cible (string) → `links[].target` + `nodes[]`
  - Colonne valeur (number) → `links[].value`
  - La liste des nœuds est déduite automatiquement des sources et cibles uniques
- **Transformation backend :** Le backend doit retourner `{ nodes: [{name}], links: [{source, target, value}] }`. Nécessite un endpoint spécifique ou une transformation frontend à partir d'un dataset `(source, target, value)`.
- **Fichier factory à créer :** `getSankeyChartOptions.ts`
- **QueryForm :** 3 sélecteurs (Source, Target, Value). Pattern proche de `radarChart`.
- **Effort estimé : M** — La structure de données diffère des séries standard. La transformation `(source, target, value)` → `{nodes, links}` est à gérer côté frontend ou backend. Environ 3-4 jours de dev.

---

### 3.6 Tree — Arbre hiérarchique

**Priorité : P2**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser une hiérarchie sous forme d'arbre avec nœuds expansibles/collapsibles. Exemples : arbre de classification (taxonomie produit, hiérarchie organisationnelle), arbre de décision simplifié, vue hiérarchique des datasets/dossiers dans l'explorateur MoveToData.
- **Public cible :** Data analysts, managers, architectes.
- **Quand l'utiliser :** Données hiérarchiques parent-enfant (colonnes : `id`, `parent_id`, `label`, `value` optionnel). Structure similaire à `sunBurstChart` mais avec layout linéaire plutôt que radial.

#### Spec technique

- **Composant ECharts :** `series.type: 'tree'`
- **Options clés :**
  ```ts
  series: [{
    type: 'tree',
    data: [{
      name: 'Root',
      children: [{
        name: 'Child A',
        children: [{ name: 'Leaf 1' }, { name: 'Leaf 2' }]
      }]
    }],
    top: '5%', left: '7%', bottom: '5%', right: '20%',
    symbolSize: 7,
    label: { position: 'left', verticalAlign: 'middle', fontSize: 12 },
    leaves: { label: { position: 'right', verticalAlign: 'middle' } },
    expandAndCollapse: true,
    animationDuration: 550,
    orient: 'LR', // 'LR', 'RL', 'TB', 'BT'
    layout: 'orthogonal' // ou 'radial'
  }]
  ```
- **Mapping données → config :**
  - Format identique à `sunBurstChart` (données hiérarchiques imbriquées). Peut **réutiliser** le `HierarchyController.tsx` et la transformation de données de `sunBurstChart`.
- **Fichier factory à créer :** `getTreeChartOptions.ts`
- **Effort estimé : S** — La transformation de données est partagée avec `sunBurstChart` (déjà implémenté). Seule la configuration ECharts diffère. Environ 1-2 jours de dev.

---

### 3.7 Parallel — Coordonnées parallèles

**Priorité : P2**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser des données multidimensionnelles (plus de 3 variables) pour détecter des patterns et des corrélations entre dimensions. Exemples : analyse de la qualité d'un dataset (score qualité par colonne × règle de qualité × seuil × couverture), comparaison de KPIs multi-dimensions pour plusieurs entités, profiling de segments utilisateurs sur plusieurs métriques.
- **Public cible :** Data scientists, analystes avancés.
- **Quand l'utiliser :** 3 dimensions numériques minimum, idéalement 5-10. Chaque ligne de données devient une "ligne" qui traverse toutes les axes parallèles.

#### Spec technique

- **Composant ECharts :** `series.type: 'parallel'`
- **Options clés :**
  ```ts
  parallelAxis: [
    { dim: 0, name: 'Dimension A' },
    { dim: 1, name: 'Dimension B' },
    { dim: 2, name: 'Dimension C' },
  ],
  series: [{
    type: 'parallel',
    lineStyle: { width: 1, opacity: 0.5 },
    data: [
      [val_A, val_B, val_C],  // une ligne par enregistrement
      ...
    ]
  }]
  ```
- **QueryForm :** Multi-sélecteur de colonnes numériques (pattern proche de `radarChart`).
- **Fichier factory à créer :** `getParallelChartOptions.ts`
- **Effort estimé : M** — Requiert un QueryForm spécifique pour sélectionner N dimensions. La transformation de données est nouvelle. Environ 3 jours de dev.

---

### 3.8 EffectScatter — Scatter animé

**Priorité : P3**

#### Spec fonctionnelle

- **Cas d'usage :** Variante du scatter avec animation de pulsation sur les points pour attirer l'attention sur des valeurs remarquables (anomalies, outliers, points critiques). Exemples : alertes data quality sur une carte de dispersion, visualisation d'anomalies dans un flux temps réel.
- **Public cible :** Data engineers, ops.
- **Quand l'utiliser :** Même usage que `scatter` + mise en avant d'un sous-ensemble de points.

#### Spec technique

- **Composant ECharts :** `series.type: 'effectScatter'`
- **Options clés :**
  ```ts
  series: [{
    type: 'effectScatter',
    symbolSize: 20,
    rippleEffect: { brushType: 'stroke', scale: 4, period: 4 },
    data: [{ value: [x, y], name: 'Anomalie détectée' }]
  }]
  ```
- **Effort estimé : S** — Extension directe du `scatterChart` existant. Peut être ajouté comme `seriesType` supplémentaire dans `VerticalAxisChart`. Environ 1 jour de dev.

---

### 3.9 ThemeRiver — Rivière thématique

**Priorité : P3**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser l'évolution temporelle de la proportion de plusieurs catégories (stream graph). Exemples : répartition du trafic par type de requête dans le temps, évolution des catégories d'erreurs dans les logs, usage des connecteurs MoveToData dans le temps.
- **Public cible :** Data engineers, DevOps.
- **Quand l'utiliser :** Données avec colonne temps + colonne catégorie + colonne valeur. Minimum 3 catégories sur une série temporelle.

#### Spec technique

- **Composant ECharts :** `series.type: 'themeRiver'`
- **Options clés :**
  ```ts
  singleAxis: { type: 'time', top: 50, bottom: 50 },
  series: [{
    type: 'themeRiver',
    emphasis: { focus: 'series' },
    data: [
      ['2024-01-01', 20, 'Catégorie A'],
      ['2024-01-01', 15, 'Catégorie B'],
      // format: [date, value, category]
    ]
  }]
  ```
- **Effort estimé : M** — Système de coordonnées `singleAxis` différent des graphiques actuels. Nouveau pattern de données. Environ 2-3 jours de dev.

---

### 3.10 PictorialBar — Barre pictographique

**Priorité : P3**

#### Spec fonctionnelle

- **Cas d'usage :** Barre classique avec des symboles répétés (icônes) à la place des rectangles plein, pour rendre les dashboards exécutifs plus visuels. Exemples : nombre d'utilisateurs actifs représentés par des silhouettes, capacité de stockage utilisé vs disponible.
- **Public cible :** Décideurs, utilisateurs non-techniques.
- **Quand l'utiliser :** Même usage que le bar chart vertical, mais pour des présentations executives ou des dashboards publics.

#### Spec technique

- **Composant ECharts :** `series.type: 'pictorialBar'`
- **Options clés :**
  ```ts
  series: [{
    type: 'pictorialBar',
    symbol: 'path://M...',   // SVG path ou emoji shape
    symbolRepeat: true,
    symbolSize: ['80%', '60%'],
    data: [{ value: 5, name: 'Catégorie A' }]
  }]
  ```
- **Effort estimé : M** — Nécessite une UI de sélection de symboles. Environ 2-3 jours de dev.

---

### 3.11 Graph — Réseau / Graphe de relations

**Priorité : P2**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser les relations entre entités. Exemples : graphe de lignage de données (quels datasets alimentent quels autres), réseau de dépendances entre pipelines, réseau de collaboration entre utilisateurs d'une organisation, graphe d'entités d'un schéma de base de données relationnelle.
- **Public cible :** Data engineers, architectes data, DBA.
- **Valeur différenciante MoveToData :** Particulièrement pertinent pour une data platform où la compréhension des dépendances entre assets est centrale. Lien direct avec la fonctionnalité de data lineage.
- **Quand l'utiliser :** Données avec colonnes `source` et `target` (arêtes). Les nœuds peuvent avoir une taille proportionnelle à une métrique.

#### Spec technique

- **Composant ECharts :** `series.type: 'graph'`
- **Options clés :**
  ```ts
  series: [{
    type: 'graph',
    layout: 'force', // 'none', 'circular', 'force'
    roam: true,
    label: { show: true, position: 'right' },
    edgeSymbol: ['none', 'arrow'],
    force: { repulsion: 100 },
    nodes: [
      { id: '0', name: 'Dataset A', symbolSize: 50, category: 0 },
      { id: '1', name: 'Pipeline B', symbolSize: 30, category: 1 }
    ],
    links: [
      { source: '0', target: '1' }
    ],
    categories: [{ name: 'Dataset' }, { name: 'Pipeline' }]
  }]
  ```
- **Mapping données → config :**
  - Colonne source + colonne target (strings) → `nodes` + `links`
  - Colonne valeur (number, optionnel) → `nodes[].symbolSize`
  - Colonne catégorie (string, optionnel) → `nodes[].category`
- **QueryForm :** 2 sélecteurs obligatoires (Source, Target) + 2 optionnels (Valeur, Catégorie). Pattern proche de Sankey.
- **Effort estimé : L** — Structure de données complexe, transformation non-standard. L'interaction utilisateur (roam, drag) requiert une gestion d'état spécifique. Environ 5-7 jours de dev.

---

### 3.12 Chord — Diagramme de cordes (ECharts 6)

**Priorité : P3**

#### Spec fonctionnelle

- **Cas d'usage :** Visualiser des relations bidirectionnelles entre entités et leur intensité sous forme circulaire. Exemples : flux d'échanges de données entre équipes, matrice de corrélation entre KPIs, flux d'imports/exports entre systèmes.
- **Public cible :** Analystes avancés, architectes data.

#### Spec technique

- **Composant ECharts :** `series.type: 'chord'` (disponible depuis ECharts 6, juillet 2025)
- **Note :** Vérifier la version d'ECharts installée dans le projet (`package.json`) avant d'implémenter. Si `echarts < 6.0.0`, une migration est nécessaire.
- **Effort estimé : M** — Nouveau type récent. Documentation limitée. Environ 3-4 jours de dev.

---

### 3.13 Types hors-scope (non recommandés pour implémentation)

| Type | Raison d'exclusion |
|---|---|
| `map` (ECharts geo) | Déjà couvert par `mapChart` (Kepler.gl) avec plus de fonctionnalités. Réactiver le `mapChart` existant plutôt qu'ajouter ce type. |
| `lines` (flux géo) | Dépend de la couche cartographique. Intégrable comme extension du `mapChart` existant. |
| `custom` | Pas un type utilisateur final. Réservé aux développeurs pour des besoins très spécifiques. |
| `echarts-gl` (3D) | Surcharge de rendu, usage rare en data platform métier. Hors roadmap v1. |
| `liquidfill` | Usage très spécifique (dashboards marketing). Non prioritaire. |

---

## 4. Backlog priorisé

### Critères de scoring

- **Impact (1-5) :** Fréquence d'usage estimée × valeur perçue par l'utilisateur data × contribution au compte des 25 types Metabase.
- **Effort (S/M/L) :** S = 1-2j dev, M = 3-5j dev, L = 6j+ dev.
- **Ratio I/E :** Score d'attractivité (Impact × facilité). S=3, M=2, L=1.

| Rang | Type | Nom interne suggéré | Impact (1-5) | Effort | Ratio I/E | Justification |
|---|---|---|---|---|---|---|
| 1 | Funnel | `funnelChart` | 5 | S | **15** | Type Metabase manquant. Usage universel (sales, ops, ETL). Pattern de données identique à pie. Implémentation la plus rapide avec le plus fort impact. |
| 2 | Heatmap | `heatmapChart` | 5 | M | **10** | Type Metabase manquant. Emblématique de l'analyse data. Permet la visualisation de matrices de corrélation et de patterns temporels. Fort signal de maturité pour les data engineers. |
| 3 | Sankey | `sankeyChart` | 4 | M | **8** | Aucun équivalent Metabase (avantage MoveToData). Très pertinent pour data platform : visualise les pipelines et le lineage. Forte valeur différenciante vs concurrence. |
| 4 | Tree | `treeChart` | 3 | S | **9** | Réutilise la logique de `sunBurstChart`. Coût de développement minimal. Complète la couverture hiérarchique. |
| 5 | EffectScatter | `effectScatterChart` | 3 | S | **9** | Extension du scatter existant. 1 jour de dev. Permet des dashboards temps réel avec alertes visuelles. |
| 6 | Graph / Network | `graphChart` | 4 | L | **4** | Très pertinent pour data lineage et analyse relationnelle. Effort élevé justifié uniquement si la fonctionnalité de lineage est sur la roadmap produit. |
| 7 | Boxplot | `boxplotChart` | 4 | M | **8** | Absent de Metabase (avantage MoveToData). Indispensable pour data science. Nécessite un endpoint backend dédié. |
| 8 | Candlestick | `candlestickChart` | 3 | M | **6** | Segment Enterprise / fintech. À cibler après les fondamentaux. |
| 9 | Parallel | `parallelChart` | 3 | M | **6** | Analyse multidimensionnelle avancée. Public data science. Pertinent post-MVP des 25 types. |
| 10 | ThemeRiver | `themeRiverChart` | 2 | M | **4** | Niche. À inclure uniquement si le compte de 25 types n'est pas atteint avec les types ci-dessus. |
| 11 | PictorialBar | `pictorialBarChart` | 2 | M | **4** | Usage cosmétique. Faible valeur pour les data analysts. Pertinent pour dashboards exécutifs. |
| 12 | Chord | `chordChart` | 2 | M | **4** | Nouveau dans ECharts 6. Peu documenté. Reporter après stabilisation de la lib. |

---

### Stratégie d'atteinte des 25 types (parité Metabase)

**Types actifs actuels : 14** (hors `mapChart` désactivé et `parameterChart`)

**Plan pour atteindre 25 types actifs :**

| Action | Types gagnés | Types cumulés |
|---|---|---|
| Baseline actuelle | — | 14 |
| Réactiver `mapChart` (déjà codé, lever le `disabled: true`) | +1 | 15 |
| Implémenter Funnel (Rang 1) | +1 | 16 |
| Implémenter Heatmap (Rang 2) | +1 | 17 |
| Implémenter Sankey (Rang 3) | +1 | 18 |
| Implémenter Tree (Rang 4) | +1 | 19 |
| Implémenter EffectScatter (Rang 5) | +1 | 20 |
| Implémenter Boxplot (Rang 7) | +1 | 21 |
| Implémenter Candlestick (Rang 8) | +1 | 22 |
| Implémenter Parallel (Rang 9) | +1 | 23 |
| Implémenter Graph/Network (Rang 6) | +1 | 24 |
| Implémenter ThemeRiver (Rang 10) | +1 | **25** |

**Cible atteinte avec les rangs 1-5 + 7-10 + réactivation mapChart = 25 types.**

---

## 5. Notes d'architecture pour le développeur

### 5.1 Pattern d'intégration d'un nouveau type ECharts

Chaque nouveau type suit le même chemin dans le code :

1. **`charts.config.tsx`** : Ajouter l'objet de config du type (meta, customization) et l'ajouter dans le tableau `eChartTypes`.
2. **`getXxxChartOptions.ts`** : Créer le fichier factory qui retourne la config ECharts complète à partir de `{ chartData, chartCustomization, dimensions }`.
3. **`chartOptionsFactory/index.ts`** : Brancher le nouveau factory dans le switch/dispatch central.
4. **`SliderController.tsx`** : Ajouter l'entrée dans `sliderOptions` avec l'icône et le `chartType`.
5. **`customizeForm/XxxCustomizer.tsx`** : Créer le formulaire de personnalisation (couleurs, labels, options spécifiques).
6. **`customizeForm/index.tsx`** : Brancher le nouveau Customizer dans le switch.
7. **`DashboardAddChart.utils.tsx`** : Ajouter l'icône pour le type dans `getChartIcon()`.
8. **`KeplerChartDataTable.tsx`** : Gérer l'affichage tabulaire des données pour le type.

### 5.2 Pattern de données par famille

| Famille | Format réponse backend | Types concernés |
|---|---|---|
| **Axe X + séries** | `{ xAxisData: string[], series: [{ id, type, seriesData: {col: val[]} }] }` | VerticalAxisChart, horizontalBarChart, waterFallChart |
| **Dimension + valeur** | `{ data: [{ name, value }] }` | pieChart, funnelChart |
| **Hiérarchique** | `{ data: { name, value, children: [...] } }` | sunBurstChart, treeMapChart, treeChart |
| **Triplet relationnel** | `{ nodes: [{name}], links: [{source, target, value}] }` | sankeyChart, graphChart |
| **Matrice** | `{ data: [[x, y, value]] }` | heatmapChart |
| **Statistique** | `{ data: [[min, Q1, median, Q3, max]] }` | boxplotChart |
| **OHLC** | `{ dates: string[], data: [[open, close, low, high]] }` | candlestickChart |
| **Multi-dim** | `{ axes: string[], data: number[][] }` | parallelChart |
| **Temporel catégoriel** | `{ data: [[date, value, category]] }` | themeRiverChart |

### 5.3 Dépendances npm à vérifier

Avant implémentation, vérifier dans `package.json` :

```json
// Déjà présent (supposé)
"echarts": "^5.x.x",
"echarts-wordcloud": "^2.x.x",

// À ajouter si boxplot calculé côté frontend
"echarts-stat": "^1.x.x",

// Si migration ECharts 6 pour chord
"echarts": "^6.0.0"
```

**Attention :** Une migration d'ECharts 5 vers ECharts 6 peut introduire des breaking changes. Auditer les options de config existantes avant de migrer pour `chordChart`.

---

## 6. User Stories synthétiques (pour le backlog sprint)

```
US-VIZ-01 : Funnel Chart
En tant qu'analyste business, je veux créer un graphique en entonnoir
à partir d'une dimension et d'une métrique, afin de visualiser les taux
de conversion entre étapes successives.
Critères d'acceptation :
- L'entonnoir s'affiche dans le SliderController
- L'ordre des tranches peut être inversé (ascending/descending)
- Les labels affichent valeur et/ou pourcentage
- Le graphique respecte le système de thèmes de couleur existant

US-VIZ-02 : Heatmap
En tant que data engineer, je veux créer une heatmap à partir de deux
colonnes catégorielles et une valeur numérique, afin d'identifier des
patterns d'intensité sur deux dimensions simultanément.
Critères d'acceptation :
- Le QueryForm expose trois sélecteurs (Axe X, Axe Y, Valeur)
- La palette de couleur est personnalisable via le customizer
- Le min/max de l'échelle peut être défini manuellement
- Les valeurs sont affichables dans les cellules

US-VIZ-03 : Sankey
En tant qu'architecte data, je veux créer un diagramme Sankey depuis
un dataset (source, cible, valeur) afin de visualiser les flux de données
entre systèmes.
Critères d'acceptation :
- Le QueryForm expose trois sélecteurs (Source, Target, Valeur)
- Les nœuds sont auto-générés depuis les colonnes source et target
- L'orientation (horizontal/vertical) est paramétrable
- Le graphique est interactif (hover sur flux = highlight)

US-VIZ-04 : Réactivation mapChart
En tant qu'utilisateur, je veux accéder au type "Map" dans le sélecteur
de graphiques, afin de visualiser mes données géographiques.
Critères d'acceptation :
- disabled: false dans SliderController.tsx
- Le bouton s'affiche sans le label "(subscription needed)"
- La fonctionnalité existante de KeplerMapChart fonctionne correctement
```

---

*Sources consultées : [Apache ECharts Cheat Sheet](https://echarts.apache.org/en/cheat-sheet.html) — [Apache ECharts Features](https://echarts.apache.org/en/feature.html) — [ECharts Chart Types DeepWiki](https://deepwiki.com/apache/echarts/4-chart-types)*
