# Notebook

Le notebook constitue un élément essentiel des flux de travail en science des données. Au sein de Orphea, nous avons intégré de manière transparente la fonctionnalité de notebook dans notre infrastructure de dépôt de code.

Ce notebook facilite l'accès aux ensembles de données sécurisés hébergés sur la plateforme, permettant aux utilisateurs de mener des analyses en parallèle de l'exécution du code.

De plus, le notebook sert d'environnement précieux pour tester le code avant son intégration dans le pipeline, garantissant la robustesse et la fiabilité des flux de travail de traitement des données.

Vous devez utiliser certaines des fonctions internes de Orphea pour accéder aux ensembles de données.

Voici un exemple de cellule :

```python

from orphea.notebook import ReadDataFrame
import pyspark.sql.functions as F
import pandas as pd

source_dataset1 = "/Projects/Test-RM/Données/Test Preview/orders_2023-04-09T1050"

spark_df = ReadDataFrame(source_dataset1)

df = spark_df.limit(1000).toPandas()
df.head()

```

:::info
Dans l'exemple ci-dessus, l'utilisateur lit un ensemble de données à partir d'un projet au sein de la plateforme.
:::
