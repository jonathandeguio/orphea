# Statut

#### Objectif

Les contrôles du statut valident le statut opérationnel des tâches de traitement des données et des ensembles de données. Ils garantissent que tous les pipelines de données et les composants associés fonctionnent comme prévu.

#### Configuration

Pour configurer un contrôle du statut, vous devez spécifier le statut attendu de divers composants. Voici un exemple de configuration :

**Exemple de contrôle du statut**

- **Nom :** Validation du statut de la tâche
- **Type :** Statut
- **Critères :**
  - Tâche : Collecte des données
  - Statut attendu : En cours
  - Tâche : Transformation des données
  - Statut attendu : Terminé

#### Surveillance

Les contrôles du statut peuvent être surveillés via le tableau de bord de l'application. Des alertes peuvent être configurées pour informer les parties prenantes en cas de changements de statut inattendus.

#### Bonnes pratiques

- **Automatiser les contrôles :** Automatisez les contrôles du statut pour garantir une surveillance continue.
- **Définitions claires :** Définissez des statuts attendus clairs pour toutes les tâches et composants critiques.
- **Réponse aux incidents :** Établissez des procédures pour répondre aux échecs des contrôles du statut.
