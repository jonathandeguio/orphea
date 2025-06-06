# Code Repository

Code Repositories est un IDE basé sur le Web qui permet aux utilisateurs d'écrire et de collaborer sur du code prêt pour la production au sein de Bosler. La plate-forme dispose d'une interface conviviale pour interagir avec les référentiels Git et offre diverses fonctionnalités supplémentaires telles que:

- Effectuer des actions courantes de contrôle de version Git telles que la création de branches, la validation et le balisage des versions via l'interface Web.

- Prise en charge de la collaboration et de la révision du code via des demandes d'extraction, avec des autorisations personnalisables pour garantir la qualité du code.

- Des outils intégrés pour améliorer l'expérience de création de code, y compris IntelliSense, le linting de code, la vérification des erreurs et des boîtes de dialogue d'aide enrichies.

# Types de référentiels

Code Repositories offre un support pour la création de différents types de référentiels, chacun avec son propre ensemble de fonctionnalités.

- Les référentiels de transformations sont conçus pour créer une logique de transformation de données. Ils fournissent des outils pour prévisualiser et déboguer les transformations et prennent en charge plusieurs langages de programmation tels que Python, et SQL.
- Les référentiels de fonctions sont destinés à écrire une logique métier pouvant être exécutée rapidement dans un cadre opérationnel. Ils sont livrés avec une prise en charge intégrée pour accéder aux données de l'ontologie Bosler et incluent la saisie semi-automatique PySpark basée sur les types de données Ontology.

De plus, l'environnement permet de prévisualiser les fonctions pendant leur création, et toutes les fonctions sont écrites en PySpark et SQL. Code Repositories fournit également un support pour le développement de modèles.

# Transformer les données

La transformation d'ensembles de données fait référence au processus de manipulation, de modification ou de conversion de données d'une forme à une autre afin de les rendre plus adaptées à l'analyse, à la modélisation ou à d'autres fins.

Bosler utilise PySpark, un langage de programmation qui permet d'interagir avec Apache Spark, un puissant framework de traitement de données. Avec PySpark, vous pouvez rapidement et facilement travailler avec de très grands ensembles de données sur plusieurs serveurs, ce qui peut apporter des améliorations significatives en termes de performances et de fiabilité.

DataFrame : un DataFrame est une structure de données semblable à une table composée de colonnes et de lignes nommées. Sa structure ressemble à une base de données SQL, mais elle n'est pas relationnelle. Une fois créé, un DataFrame ne peut pas être modifié, mais il peut être utilisé pour créer un nouveau DataFrame avec des données transformées. Bien que les ensembles de données puissent être écrasés, Bosler garde une trace de l'historique des versions, de sorte que vous pouvez toujours revenir aux versions précédentes. Les transformations DataFrame sont évaluées paresseusement, ce qui signifie qu'une série de tâches est évaluée comme une seule action et exécutée uniquement lorsqu'une génération est lancée.

RDD : Resilient Distributed Datasets est la structure de données fondamentale qui prend en charge les opérations DataFrame. En décomposant le DataFrame en sous-ensembles qui ne se chevauchent pas et en les répartissant sur un cluster d'ordinateurs (nœuds). PySpark peut exécuter des transformations en parallèle sur plusieurs nœuds. Bien que ce processus se déroule en arrière-plan, il est essentiel de garder à l'esprit lorsque vous travaillez avec PySpark.

Les Spark DataFrames sont spécialement conçus et optimisés pour gérer des quantités massives de données structurées pouvant aller de pétaoctets à des ensembles de données encore plus volumineux. Cette fonctionnalité permet aux DataFrames de traiter et de manipuler des données dans un environnement informatique distribué, ce qui les rend idéales pour les applications Big Data.

PySpark génère des ensembles de données entièrement nouveaux par opposition à SQL, qui produit des ensembles de résultats de table virtuelle. Cette fonctionnalité permet la création de nouveaux ensembles de données basés sur des ensembles de données dérivés. De plus, Bosler, un système d'exploitation de données, relie automatiquement les ensembles de données via des relations arborescentes dirigées, ce qui aide à suivre la lignée des données des transformations Spark via Bézier. Cela fournit un moyen d'explorer les dépendances qui entrent dans la création d'un ensemble de données et d'où proviennent ces ensembles de données.
