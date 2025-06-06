# Niveau des Temps

#### Objectif

Les vérifications au niveau des tâches visent à valider la réussite de l'exécution des tâches qui produisent des ensembles de données. Elles garantissent que chaque tâche se termine correctement et que les données produites sont conformes aux attentes.

#### Configuration

Pour configurer une vérification au niveau des tâches, vous devez spécifier les critères de succès et de performance. Voici un exemple de configuration :

**Exemple de Vérification au Niveau des Tâches**

- **Nom de la Tâche:** Valider la collecte de Données
- **Type:** Niveau de la Tâche
- **Critères:**
  - Succès
  - Durée: Maximum 1 heure

#### Surveillance

Les vérifications au niveau des tâches peuvent être surveillées via le tableau de bord de l'application. Les alertes peuvent être configurées pour notifier les parties prenantes en cas d'échec ou de dépassement du temps alloué.

#### Bonnes Pratiques

- **Définir des critères clairs:** Spécifiez des critères de réussite précis pour chaque tâche.
- **Automatisation:** Automatisez les vérifications pour garantir une surveillance continue.
- **Analyse des échecs:** Analysez les échecs de tâches pour identifier les points de défaillance et améliorer les processus.

---

### Vérifications au Niveau des Constructions

#### Objectif

Les vérifications au niveau des constructions visent à garantir que les constructions se terminent avec succès et dans un délai prévu. Elles surveillent la stabilité et la performance des processus de construction d'ensembles de données.

#### Configuration

Pour configurer une vérification au niveau des constructions, vous devez spécifier les critères de succès et de performance. Voici un exemple de configuration :

**Exemple de Vérification au Niveau des Constructions**

- **Nom de la Construction:** Construction de Transformation des Données
- **Type:** Niveau de la Construction
- **Critères:**
  - Succès
  - Durée: Maximum 2 heures

#### Surveillance

Les vérifications au niveau des constructions peuvent être surveillées via le tableau de bord de l'application. Les alertes peuvent être configurées pour notifier les parties prenantes en cas d'échec ou de dépassement du temps alloué.

#### Bonnes Pratiques

- **Optimisation des processus:** Optimisez les processus de construction pour minimiser les temps d'exécution.
- **Surveillance continue:** Utilisez des outils de surveillance continue pour détecter les anomalies en temps réel.
- **Documentation:** Documentez les configurations de construction pour faciliter la maintenance et les mises à jour.

---

### Vérifications de la Fraîcheur

#### Objectif

Les vérifications de la fraîcheur des données visent à garantir que les ensembles de données sont à jour. Elles vérifient que les données sont actualisées dans les délais prévus, assurant ainsi leur pertinence et leur utilité.

#### Configuration

Pour configurer une vérification de la fraîcheur, vous devez définir l'âge maximum acceptable des données. Voici un exemple de configuration :

**Exemple de Vérification de la Fraîcheur**

- **Nom:** Fraîcheur de l'Ensemble de Données
- **Type:** Fraîcheur
- **Critères:**
  - Âge Maximum: 24 heures

#### Surveillance

Les vérifications de la fraîcheur peuvent être surveillées via le tableau de bord de l'application. Les alertes peuvent être configurées pour notifier les parties prenantes lorsque les données ne sont pas actualisées dans les délais prévus.

#### Bonnes Pratiques

- **Planification des mises à jour:** Planifiez les mises à jour régulières des ensembles de données.
- **Automatisation des flux de travail:** Automatisez les flux de travail pour garantir la mise à jour des données en temps opportun.
- **Révision des critères:** Révisez régulièrement les critères de fraîcheur pour qu'ils correspondent aux exigences de l'entreprise.
