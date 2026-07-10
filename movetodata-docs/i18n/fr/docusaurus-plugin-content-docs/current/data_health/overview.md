# Apreçu

#### Vue d'ensemble

Notre application propose des vérifications de santé des données robustes pour surveiller et valider la qualité, la cohérence et la fraîcheur de vos ensembles de données. Ces vérifications aident à garantir que vos pipelines de données fonctionnent sans accroc et que vos données répondent aux normes requises. Ce document présente les différents types de vérifications de santé des données disponibles et explique comment les configurer et les utiliser efficacement.

#### Types de Vérifications de Santé

1. **Vérifications au Niveau des Tâches**

   - **Objectif:** Valider la réussite de l'exécution des tâches qui produisent des ensembles de données.
   - **Configuration:** Configurées pour s'exécuter après la fin d'une tâche afin de s'assurer qu'elle répond aux critères définis.

2. **Vérifications au Niveau des Constructions**

   - **Objectif:** Garantir que les constructions se terminent avec succès et dans un délai prévu.
   - **Configuration:** Appliquées aux constructions d'ensembles de données pour surveiller leur temps d'exécution et leur taux de réussite.

3. **Vérifications de la Fraîcheur**

   - **Objectif:** Vérifier que les ensembles de données sont à jour.
   - **Configuration:** Comparer l'heure de la dernière mise à jour de l'ensemble de données avec l'heure actuelle pour s'assurer que les données sont actualisées comme prévu.

4. **Vérifications du Contenu**

   - **Objectif:** Valider le contenu des ensembles de données par rapport à des règles prédéfinies.
   - **Configuration:** Implémenter des règles pour vérifier les valeurs des données, les plages, l'unicité et d'autres propriétés liées au contenu.

5. **Vérifications du Schéma**
   - **Objectif:** Garantir que le schéma de l'ensemble de données correspond à la structure attendue.
   - **Configuration:** Comparer le schéma actuel avec le schéma attendu et valider les noms de colonnes, les types de données et les contraintes.

#### Configuration des Vérifications de Santé

1. **Définir les Vérifications**

   - Spécifiez le type de vérification que vous souhaitez implémenter.
   - Définissez les critères et les seuils pour chaque vérification.

2. **Appliquer les Vérifications**

   - Attachez des vérifications aux ensembles de données, aux tâches ou aux constructions en utilisant l'interface de configuration.
   - Assurez-vous que les vérifications s'exécutent à des étapes appropriées dans votre pipeline de données.

3. **Surveillance et Alertes**
   - Configurez des notifications pour les échecs de vérification afin d'alerter les parties prenantes concernées.
   - Utilisez le tableau de bord de surveillance pour voir l'état de toutes les vérifications de santé.

#### Exemples de Configurations

**Exemple de Vérification au Niveau des Tâches**

- **Nom de la Tâche:** Valider l'Ingestion de Données
- **Type:** Niveau de la Tâche
- **Critères:**
  - Succès
  - Durée: Maximum 1 heure

**Exemple de Vérification au Niveau des Constructions**

- **Nom de la Construction:** Construction de Transformation des Données
- **Type:** Niveau de la Construction
- **Critères:**
  - Succès
  - Durée: Maximum 2 heures

**Exemple de Vérification de la Fraîcheur**

- **Nom:** Fraîcheur de l'Ensemble de Données
- **Type:** Fraîcheur
- **Critères:**
  - Âge Maximum: 24 heures

**Exemple de Vérification du Contenu**

- **Nom:** Validité des Données
- **Type:** Contenu
- **Critères:**
  - Unique: Colonne ID
  - Plage: Colonne Valeur (Min: 0, Max: 100)

**Exemple de Vérification du Schéma**

- **Nom:** Validation du Schéma
- **Type:** Schéma
- **Critères:**
  - Correspondre au Schéma Attendu:
    - Colonne: ID, Type: Entier
    - Colonne: Nom, Type: Chaîne
    - Colonne: Valeur, Type: Flottant

#### Bonnes Pratiques

- **Mises à Jour Régulières:** Gardez vos vérifications de santé à jour pour refléter les changements dans vos pipelines de données et les exigences de l'entreprise.
- **Surveillance Automatisée:** Automatisez les vérifications de santé et la surveillance pour assurer une validation continue sans intervention manuelle.
- **Couverture Complète:** Utilisez une combinaison de différents types de vérifications pour couvrir divers aspects de la qualité des données et de la santé des pipelines.

En implémentant ces vérifications de santé des données, vous pouvez garantir la fiabilité, l'exactitude et la ponctualité de vos données, soutenant ainsi une meilleure prise de décision et une efficacité opérationnelle accrue.
