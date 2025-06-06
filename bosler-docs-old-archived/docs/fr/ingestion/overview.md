# Aperçu

## La configuration initiale

<div style="text-align: justify">Cette documentation vous guidera tout au long du processus d'établissement d'une connexion entre les données de votre organisation et la plateforme Bosler pour la première fois.

Il est important de noter que la configuration initiale de cette connexion est principalement une tâche de mise en réseau et doit être gérée par un membre de votre équipe informatique qui a de l'expérience en ingénierie réseau et qui connaît la topologie du réseau et les configurations de pare-feu de votre organisation.

![Bosler Network](../../_media/boslerdiagram.jpg)

## Concept

Afin de connecter des données à Bosler, les trois composants suivants doivent être correctement installés et configurés : l'Agent, la Source et le Lien.

### Agent

L'agent, un composant logiciel qui s'exécute au sein du réseau d'une organisation, sert d'intermédiaire sécurisé entre les sources de données de l'organisation et l'instance Bosler. Il est nécessaire pour se connecter à certaines sources de données, sauf si la source de données est une source basée sur le cloud à laquelle Bosler peut accéder directement.

De plus, un seul agent peut prendre en charge plusieurs sources de données et liens

- [En savoir plus sur l'architecture de l'agent.](../ingestion/installagent.md)

### Les sources de données

Pour établir une connexion avec Bosler, il est nécessaire d'utiliser un système de données externe appelé Source.

Ces sources peuvent inclure, entre autres, une base de données Postgres, un compartiment S3, un système de fichiers sur un serveur Linux, une instance SAP et une API REST sur Internet. Il convient de noter qu'avant d'établir une connexion à Bosler, la source doit être configurée de manière appropriée. De plus, il est important de comprendre qu'une source ne peut pas être directement accessible dans Bosler, car les données doivent être synchronisées dans un ensemble de données avant de pouvoir être utilisées.

### Liens

Un lien est chargé d'obtenir des données spécifiques d'une source et de les incorporer dans Bosler. Par exemple, si une source de base de données Postgres contient plusieurs tables, il est possible de configurer un lien pour ingérer une table particulière dans Bosler. Une fois qu'un lien a été exécuté avec succès, le résultat au sein de Bosler sera un ensemble de données, qui peut être utilisé dans tous les outils de traitement de données, de développement de modèles et d'analyse de Bosler.

- [En savoir plus sur les sources de données et les liens.](/docs/ingestion/datasources.md)
</div>
