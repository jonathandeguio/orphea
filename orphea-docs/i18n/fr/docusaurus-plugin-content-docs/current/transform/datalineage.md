# Traçabilité des données

Un pipeline de données est un système qui gère le flux de données provenant de diverses sources vers une destination commune. Il est utilisé pour intégrer les données de différents systèmes et créer une vue unifiée des données de l'organisation. Les pipelines de données sont généralement supervisés par une équipe ou une personne qui s'assure que les données circulent régulièrement et de manière fiable dans le pipeline.

Les pipelines de données impliquent généralement plusieurs étapes, notamment la synchronisation des données, l'imposition de schémas, la combinaison de données et la possibilité pour les équipes de créer des cas d'utilisation basés sur la base de données commune. En plus de ces fonctionnalités communes, les pipelines de données peuvent être classés en fonction de facteurs tels que l'échelle des données, les exigences de latence et la complexité de la maintenance.

Le type de pipeline utilisé dans Orphea est :

- Bezier

![Bezier](../../../../../docs/_media/docs_ss/Transform/transform/DataLineage.gif)

## Setup

Le processus de configuration du pipeline dans Orphea implique l'utilisation d'une interface pointer-cliquer pour configurer un pipeline rapidement et facilement. Les utilisateurs techniques peuvent se concentrer sur des descriptions déclaratives du pipeline et des résultats souhaités, tandis que les utilisateurs moins techniques peuvent créer des pipelines via une approche schématique simplifiée.

## Planification des builds

La planification des builds est une étape essentielle dans la construction d'un pipeline, car les consommateurs de données en aval s'attendent à ce que les données soient mises à jour régulièrement. La fréquence du flux de données via un pipeline est déterminée par les besoins de l'entreprise.
Par exemple, certains pipelines peuvent fonctionner uniquement une fois par semaine ou quotidiennement, tandis que d'autres fonctionnent toutes les heures ou plus fréquemment.

## Qualité

La qualité des données est un aspect important des pipelines de données, et il est essentiel de vérifier la qualité des entrées et des sorties à chaque étape. Les données synchronisées à partir des systèmes source incluent souvent des valeurs indéfinies et des données mal formatées ou incohérentes. Le nettoyage et la normalisation des données sont au cœur du processus de construction du pipeline.

## Sécurité

La sécurité et la gouvernance sont des aspects essentiels des pipelines de données. Les primitives de sécurité de la plate-forme de Orphea offrent les meilleures capacités de leur catégorie pour sécuriser une base de données et garantir que les données sensibles sont traitées de manière appropriée.

## Soutien

Une fois qu'un pipeline est publié en production, il est important de réfléchir à la longévité du pipeline d'un point de vue organisationnel.

Les processus de support pour la maintenance des pipelines doivent être étoffés, les attentes doivent être clairement définies et la documentation doit être disponible afin que les pipelines restent de haute qualité même lorsqu'ils sont transférés d'une équipe à une autre.

### Data Lineage offre une variété de fonctionnalités, telles que : -

Le graphique de Bézier facilite la compréhension de la lignée des données.
La possibilité de localiser et de découvrir facilement des ensembles de données.
La possibilité de visualiser le flux de données d'une cellule mère à une autre.
L'option de rechercher des ensembles de données à l'aide des noms de projet, de table et de colonne.
La possibilité de naviguer dans les projets Orphea pour examiner les données.
La possibilité d'examiner les pipelines via une interface intuitive telle que Bézier.

Sur la page d'accueil de Orphea, vous pouvez visualiser les pipelines d'ensembles de données. Cliquez simplement sur les pipelines qui vous dirigeront vers la page Bézier.

La possibilité de développer ou de réduire les parents et les enfants des ensembles de données.
La possibilité d'afficher simultanément les attributs d'un groupe de tables.
La possibilité de visualiser le pipeline à l'aide de couleurs, telles que la mise en évidence de tables obsolètes.
La possibilité d'explorer les détails des données, tels que son schéma, la date de sa dernière création et le code qui a généré les données.
La capacité de collaborer avec des collègues.
La possibilité de créer des instantanés de pipeline à partager avec d'autres utilisateurs.

![Bezier](../../../../../docs/_media/docs_ss/Transform/transform/DataLClose.gif)
Ici, dans cette image ci-dessus, vous pouvez observer le flux de données.

:::tip
Vous pouvez changer la couleur des nœuds en fonction du groupe de nœuds avec la liste déroulante en haut à droite de l'écran.
:::
