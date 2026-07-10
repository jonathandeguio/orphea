# Liens d'ensemble de données

La création d'un lien vous permet d'extraire des données spécifiques d'une source et de les importer dans MoveToData. Par exemple, si vous avez une source de base de données relationnelle contenant plusieurs tables, vous pouvez configurer un lien pour sélectionner une table spécifique et l'importer dans MoveToData.
L'établissement d'un lien entraîne la création d'un ensemble de données MoveToData dans un projet sélectionné. Le processus de transfert des données depuis la source vers le dataset est effectué lors du build. Bien que les builds puissent être lancés manuellement, leur planification est une pratique plus courante.

Pour configurer un lien, procédez comme suit :

- Identifiez les données spécifiques de la source que vous souhaitez ingérer dans MoveToData. Il peut s'agir d'une table spécifique d'une base de données, par exemple.

- Déterminez le projet et l'emplacement au sein de MoveToData où les données seront envoyées. Ce sera l'emplacement du dataset MoveToData qui est créé.

- Paramétrez le lien en ajoutant un horaire. Cette planification déterminera quand les données seront automatiquement synchronisées de la source vers les datasets. Notez que les builds peuvent également être déclenchés manuellement.

Une fois ces étapes terminées, le lien sera configuré et prêt à être utilisé. Les données seront ingérées de la source dans MoveToData selon l'horaire défini dans la configuration.
