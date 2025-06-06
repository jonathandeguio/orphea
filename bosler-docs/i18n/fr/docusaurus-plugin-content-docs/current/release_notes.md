# Relase Notes

---

## 26 Novembre 2024

### Améliorations de la recherche globale

- **Nouvelles fonctionnalités** :
  - Capacités de recherche étendues pour les types de ressources et les statuts.
  - Ajout de la fonctionnalité "Recherches récentes" pour simplifier les requêtes fréquentes.

### Simplification du Data Mart

- **Nouvelles fonctionnalités** :
  - Simplification des flux de travail pour la création et la gestion des Data Marts.
  - Réduction de la configuration nécessaire et amélioration de l’expérience utilisateur pour les utilisateurs non techniques.

### Améliorations des performances du connecteur Snowflake

- **Nouvelles fonctionnalités** :

  - Optimisation du connecteur Snowflake pour améliorer la vitesse de transfert des données et la fiabilité.
  - Amélioration des performances d’exécution des requêtes pour les ensembles de données de grande taille.

### Stockage des passeports amélioré et crypté

- **Nouvelles fonctionnalités** :
  - Mise à niveau du stockage des passeports avec cryptage pour une sécurité accrue des données.
  - Amélioration des performances pour le stockage et la récupération des informations liées aux passeports.

### Vidéos tutoriels dans la documentation

- **Nouvelles fonctionnalités** :
  - Ajout de vidéos tutoriels dans la documentation pour un apprentissage visuel.
  - Guides complets pour les cas d’utilisation courants et le dépannage.

### Journaux détaillés Spark

- **Nouvelles fonctionnalités** :

  - Introduction de journaux détaillés Spark pour un débogage avancé et des informations approfondies sur les performances.
  - Les journaux incluent des métriques détaillées, des opérations Spark et des traçages d'erreurs.

### Synchronisation du constructeur avec les sources de base de données

- **Nouvelles fonctionnalités** :

  - Les journaux de construction reflètent désormais le processus de synchronisation avec les sources de base de données pour une meilleure traçabilité.
  - Journalisation améliorée de l’état du constructeur pour capturer les opérations de base de données.

### Prétraitement des en-têtes

- **Nouvelles fonctionnalités** :
  - Introduction du prétraitement des en-têtes pour les requêtes API et les ensembles de données afin d'assurer une ingestion de données propre et standardisée.
  - Gestion automatisée des problèmes courants des en-têtes tels que le formatage et la validation.

### Prétraitement des fichiers CSV pour l'API et les ensembles de données

- **Nouvelles fonctionnalités** :
  - Pipelines de prétraitement des fichiers CSV améliorés pour les réponses API et les téléchargements de données.
  - Intégration transparente des capacités de prétraitement avec le connecteur API pour des transformations liées.

### Corrections de bugs :

- Résolution de problèmes liés aux graphiques, au fonctionnement des tableaux de bord, à la sécurité et à la gestion des dépôts.

---

## 14 Novembre 2024

### Connecteur API

Une solution améliorée pour préparer des ensembles de données à l'aide de requêtes REST API.

- **Nouvelles Fonctionnalités** :

  - Prise en charge de l'autorisation REST API, y compris les méthodes Bearer Token et API-KEY.
  - Capacité à effectuer des requêtes API groupées et à utiliser les données des requêtes précédentes pour les suivantes.
  - Transformation des réponses JSON directement en ensembles de données.

### Recherche Globale Améliorée

- La recherche globale a été améliorée pour permettre la recherche par type de ressource.
- Capacité de rechercher en fonction du statut.
- Fonctionnalité de recherches récentes.

### Corrections de Bugs et Améliorations\*\* :

- Résolution des problèmes liés aux graphiques, à la fonctionnalité des tableaux de bord, à la sécurité et à la gestion des dépôts.

---

## 27 Octobre 2024

### Gestion des Jetons d'Accès / Jetons de Rafraîchissement

Sécurisation améliorée des jetons pour réduire les risques d'exposition à des menaces potentielles.

- **Modifications** :

  - Les jetons sont désormais stockés en toute sécurité dans des cookies au lieu de `localStorage` pour atténuer les vulnérabilités XSS.
  - Durée de vie des jetons d'accès raccourcie (5-15 minutes) pour une sécurité accrue.
  - Les jetons de rafraîchissement ont désormais une validité de 7 jours, après quoi les utilisateurs doivent se réauthentifier avec leur mot de passe.

- **Corrections de Bugs et Améliorations** :
  - Résolution de divers bugs et améliorations des performances liées aux graphiques, au tableau de bord, à la sécurité et à la gestion des dépôts.

---

## 18 Octobre 2024

### Authentification à Facteurs Multiples (MFA)

Sécurité renforcée grâce à la génération de mots de passe uniques (OTP) via une application d'authentification.

- **Nouvelles Fonctionnalités** :

  - **Configuration MFA Simplifiée** : Connexion sécurisée activée via une application d'authentification avec un minimum de changements d'écran pour une expérience fluide.
  - **Codes de Récupération** : Les utilisateurs peuvent désormais générer des codes de récupération comme méthode de secours pour une connexion sécurisée.

- **Corrections de Bugs et Améliorations** :
  - Résolution de bugs et optimisations des performances liées aux graphiques, à la fonctionnalité des tableaux de bord, à la sécurité et à la gestion des dépôts.

---

## 1er Mai 2023

### Version Kepler

- **Nouvelles Fonctionnalités** :
  - Types de graphiques supplémentaires pour une meilleure visualisation des données.
  - Capacités de filtrage des tableaux de bord améliorées pour une analyse des données plus efficace.
  - Renforcement de la sécurité sur toute la plateforme pour une protection robuste des données.
  - Intégration de flux d'icônes dans toute la plateforme pour une meilleure expérience utilisateur.
  - Journaux de construction en temps réel activés via des sockets pour une surveillance transparente.

---

## 31 Mars 2023

### Mises à Jour de la Version Kepler

- **Améliorations** :
  - Option pour supprimer des graphiques du tableau de bord si nécessaire.
  - Demandes de confirmation avant de quitter une page avec des modifications non enregistrées sur les graphiques, aidant à éviter les pertes de données accidentelles.

### Général

- **Page des Journaux de Construction** :
  - Mise à jour de la mise en page et des fonctionnalités pour une meilleure convivialité et un accès aux informations.

---

## 17 Mars 2023

### Dépôt de Code

- **Nouvelles Fonctionnalités** :
  - Affichage des avatars des collaborateurs pour une meilleure visibilité de l'équipe.
  - Prise en charge des transformations SQL dans le dépôt.

### Kepler

- **Fonctionnalités Initiales** :
  - Introduction du premier ensemble de fonctionnalités de graphiques.

---
