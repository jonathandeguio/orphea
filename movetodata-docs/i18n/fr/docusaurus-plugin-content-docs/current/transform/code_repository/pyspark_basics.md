# Principes de base de PySpark

Ce guide vous aidera à transformer différents ensembles de données dans MoveToData. Voici un tutoriel étape par étape pour transformer des données :

- Connectez-vous à votre compte
- Sélectionnez Projets dans le menu de la barre latérale
- Sélectionnez votre dossier sous le tableau des projets
- Sélectionnez vos dossiers particuliers pour ouvrir le jeu de données
- En haut à droite de l'écran, cliquez sur référentiel
- Vous serez redirigé vers la page Code Workbook and Repository.
- Vous pouvez écrire du code en Python.

Dans les référentiels de code, au début de votre script Python, vous devez généralement inclure une instruction d'importation pour accéder à diverses fonctions fournies par des bibliothèques ou des modules externes.

Voici à quoi ressemblerait votre page :

```python
 from movetodata.functions import funnel
 import pyspark.sql.functions as F
```

<code>source_dataset</code>: fait référence à un DataFrame qui représente un Dataset stocké dans MoveToData.

<code>target_dataset</code> : dans cette fonction, vous pouvez définir une série de transformations que vous souhaitez voir appliquées à <code>source_dataset</code>. Une fois que vous avez déclenché une génération avec votre code, les résultats sont enregistrés dans un nouveau fichier Dataset dans MoveToData, que vous pouvez explorer une fois la génération terminée.

## Exemple de Code d'Illustration

Le fragment de code suivant illustre l'utilisation de plusieurs ensembles de données à la fois en tant que sources et cibles, avec la possibilité de passer des variables à travers le décorateur :

```python
from movetodata.functions import funnel, Source, Target
from pyspark.sql import functions as F


@funnel(
    var1="test",
    first_source=Source('/path/to/first/source/dataset'),
    second_source=Source('/path/to/second/source/dataset'),
    first_target=Target('/path/to/first/target/dataset'),
    second_target=Target('/path/to/second/target/dataset'),
)
def my_compute_function(first_source, second_source, first_target, second_target):

    first_source_df = first_source.dataframe()
    second_source_df = second_source.dataframe()

    # Filtering data in the first source dataset based on a specific condition
    first_source_df = first_source_df.filter(first_source_df.Payment_Type == "Visa")

    # Writing the filtered data from the first source dataset to the first target dataset
    first_target.write_dataframe(first_source_df.dataframe())

    # Writing the data from the second source dataset to the second target dataset without any modifications
    second_target.write_dataframe(second_source_df.dataframe())

```

## Filtration

Ce code ci-dessous filtrera la trame de données sur la colonne Payment_Type pour "Visa":

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type == "Visa")

    target.write_dataframe(target_df))
```

Ce code ci-dessous filtrera la trame de données sur la colonne Payment_Type pour "MasterCard":

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type == "MasterCard")

    target.write_dataframe(target_df))
```

Ce code ci-dessous filtrera la trame de données sur la colonne en "MasterCard" et changera le nom de la colonne en "Date de transaction":

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type == "MasterCard")
    target_df = target.dfwithColumn("Transaction Date", F.to_date(target_df["Transaction date"], "M/d/yyyy HH:mm"))

    target.write_dataframe(target_df))
```

Ce code ci-dessous filtrera le dataframe sur la colonne State pour "England":

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().State == "England")

    target.write_dataframe(target_df))
```

Ce code ci-dessous filtrera le dataframe sur la colonne State pour "Scotland":

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().State == "Scotland")

    target.write_dataframe(target_df))
```

## Jointures

Ce code ci-dessous joindra les deux dataframes (sources) dans un nouveau dataframe.

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1),
        source2=source_dataset2)
def user_transform_function4(source1, source 2):
    target_df = source2.dataframe().union(source1.dataframe()) #réunissant les deux

    target.write_dataframe(target_df))
```

:::info
En traitement des données, il est possible d'avoir plusieurs ensembles de données sources en même temps ; et il est également possible d'avoir plusieurs cibles. Vous pouvez même passer des variables dans le décorateur.
:::

## Changements de colonne

Ce code ci-dessous changera le nom de la colonne du dataframe :

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type == "MasterCard")
    target_df = target_df.withColumnRenamed("Product", F.to_number(target_df["Product no."], "Product"))

    target.write_dataframe(target_df))
```

## Date et horodatage

Ce code ci-dessous sera converti en date et horodatage de la trame de données :

```python
@funnel(target=Target(target_dataset),
        source1=Source(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type == "MasterCard")
    target_df = target_df.withColumn("Transaction_date", F.to_date(target_df["Transaction date"], "M/d/yyyy HH:mm"))

    target.write_dataframe(target_df))
```

# Transformation de Données - Fiche de Référence (Extraits de Code)

Ce rapport propose quelques extraits de code pour les applications de référentiels de code, afin de faciliter le développement et de partager les meilleures pratiques.

Imports

Les fonctions SQL permettent d'effectuer de nombreuses actions de transformation de base. Ajoutez cette ligne au début de votre fichier de transformation.

```python
from pyspark.sql import functions as F
```

## Manipuler les colonnes

Sélectionnez uniquement un sous-ensemble de colonnes (y compris le réarrangement des colonnes)

```python
myColumns = \['name1', 'name2', 'name10'\]

df = df.select(\*myColumns)
```

Sélectionnez uniquement un sous-ensemble de colonnes (en spécifiant une liste d'exclusion)

```python
myUnwantedColumns = \['name1', 'name2', 'name10'\]

df = df.drop(\*myUnwantedColumns)
```

Filtrer sur la valeur d'une colonne

Égal:

```python
myUnwantedColumns = \['name1', 'name2', 'name10'\]

df = df.drop(\*myUnwantedColumns)

df = df.filter(F.col('myColumnName') == 'myValue') # if string dataType

df = df.filter(F.col('myColumnName') == True) # if boolean dataType

df = df.filter(F.col('myColumnName').isNull()) # to keep only records with null value in 'myColumnName' (or isNotNull())
```

\# ou dans une liste:

```python
inclusion_list = \['myValue1', 'myValue2', 'myValue3'\]

df = df.filter(F.col('myColumnName').isin(inclusion_list))
```

pas égal:

```python
df = df.filter(F.col('myColumnName') != 'myValue')
```

\# ou PAS dans une liste:

```python
exclusion_list = \['myValue1', 'myValue2', 'myValue3'\]

df = df.filter(~F.col('myColumnName').isin(exclusion_list))
```

Soyez très vigilant avec les valeurs Null (par défaut considérées comme ne correspondant pas)

\# De cette manière, les enregistrements avec des valeurs Null sont également filtrés :

```python
df = df.filter(F.col('Name') != 'John Doe')
```

\# De cette manière, les valeurs Null sont préservées (non exclues) :

```python
df = df.filter(~F.col('Name').eqNullSafe('John Doe')
```

Renommer les colonnes

Colonne unique:

```python
df = df.withColumnRenamed('old_name', 'new_name')
```

Plusieurs colonnes basées sur une modification générique (comme l'ajout d'un préfixe ou d'un suffixe):

```python
df = df.select(\*\[F.col(c).alias(f"myPrefix\_{c}\_mySuffix") for c in df.columns\])
```

Plusieurs colonnes basées sur une décision de l'utilisateur définie dans un dictionnaire

```python
myRename_dict = {

'old_name1': 'new_name1',

'old_name2': 'new_name2',

'old_name3': 'new_name3',

...

}

df = df.select(\*\[F.col(c).alias(myRename_dict\[c\]) for c in myRename_dict.keys()\])

## Join datasets

### Easy case where both datasets (df1 & df2 do have the same column name used for join

df = df1.join(df2,

'column_name',

'left'

)
```

Principaux types de jointure (d'autres types peuvent être trouvés dans la documentation):

- left : tous les enregistrements de df1 et seulement les enregistrements de df2 correspondants
- inner : tous les enregistrements qui sont dans df1 et df2 et dont les correspondances sont trouvées
- full : tous les enregistrements de df1 et df2, peu importe les correspondances

### Cas complexe où les noms de colonnes ne correspondent pas ou si plusieurs conditions sont nécessaires pour définir la jointure

```python
df = df1.join(df2,

(F.col('name1') == F.col('name2')) & (F.col('name3') == F.col('name4')),

'left'

)
```

Principaux opérateurs pour plusieurs conditions (d'autres opérateurs peuvent être trouvés dans la documentation):

- & : AND (condition1 et condition2 doivent correspondre)
- | : OR (au moins condition1 ou condition2 doit correspondre)

### Cas complexe avec toutes les colonnes de df1 et df2 renommées

```python
df = df1.select(\*\[F.col(c).alias(f"df1\_{c}") for c in df1.columns\]).join(

df2.select(\*\[F.col(c).alias(f"df2\_{c}") for c in df2.columns\]),

(F.col('name1') == F.col('name2')) & (F.col('name3') == F.col('name4')),

'left'

)
```

## Reformater les données

Si vous voulez reformater le tableau comme dans l'exemple ci-dessous

### Table de départ

| Name     | Tag_name    | Tag_value |
| -------- | ----------- | --------- |
| ProjectA | Status      | Open      |
| ---      | ---         | ---       |
| ProjectA | Sponsorship | IM        |
| ---      | ---         | ---       |

### Table finale

| Name     | Status | Sponsorship |
| -------- | ------ | ----------- |
| ProjectA | Open   | IM          |
| ---      | ---    | ---         |

Veuillez utiliser ce code

```python
def pivoting(source_df):

source_df = source_df \\

.withColumn("tag_name", F.regexp_replace(F.col("tag_name"), " ", "\_"))

source_df = source_df \\

.groupBy("name") \\

.pivot("tag_name") \\

.agg(F.max("tag_value"))

return source_df
```

## Créer une nouvelle colonne calculée

Créer une colonne booléenne indiquant si une autre colonne contient une valeur d'une liste

```python
df = df.withColumn('myNewColumn', F.col('sourceColumn').isin(inclusion_list))
```

Créer une colonne booléenne indiquant si une autre colonne contient une 'searched_string' :

```python
df = df.withColumn('myNewColumn', F.col('sourceColumn').like("%searched_string%"))
```

Créer une colonne booléenne indiquant si une autre colonne est NULL (ou non NULL) :

```python
df = df.withColumn('myNewColumn', F.col('sourceColumn').isNull())

df = df.withColumn('myNewColumn', F.col('sourceColumn').isNotNull())
```

Créer une colonne booléenne indiquant si 2 colonnes sont égales :

```python
df = df.withColumn('myNewColumn', F.col('column1') == F.col('column2'))
```

Créer à partir de la clé JSON : supposons que vous ayez un champ appelé 'branch', formaté en JSON comme suit

```python
df = df.withColumn('branch_name', F.col('branch.name'))
```

Créer une constante :

```python
df = df.withColumn('column_name', F.lit('my constant value'))
```

Créer plusieurs valeurs en fonction de plusieurs conditions (switch, if/else, case/when) :

```python
df = df.withColumn("colorName", F.when(df.colorCode == "R", "Red")

.when(df.colorCode == "G", "Green")

.when(df.colorCode.isNull(), "")

.otherwise(df.colorCode))
```

Concaténer plusieurs colonnes :

_\# Liste des colonnes fixes, y compris la constante :_

```python
df = df.withColumn('column_name', F.concat(df.a, F.lit('my constant value'), df.c))
```

_\# Liste des colonnes avec séparateur constant défini :_

```python
columns_to_concat = \['col1', 'col2'\]

df = df.withColumn('column_name', F.concat_ws('-', \*columns_to_concat))
```

Créer un tableau à partir d'une chaîne de caractères séparée par un caractère :

_\# par exemple, en divisant une chaîne de chemin complet comme /root/folder1/folder2/folder3/file :_

```python
df = df.withColumn('column_name', F.split(F.col('source_column'), '/'))
```

Obtenir le nième élément (le premier = 0, le deuxième = 1, ...) d'un tableau :

_\# En partant d'une colonne_source avec un tableau \['item1', 'item2', 'item3'], obtenez le 3e élément, c'est-à-dire 'item3' :_

```python
df = df.withColumn('column_name', F.col('source_column')\[2\])
```

_\# Pour obtenir le dernier élément, utilisez reverse :_

```python
df = df.withColumn('column_name', F.reverse(F.col('source_column'))\[0\])
```

## Décomposer le jeu de données

Lorsque un champ est un tableau et que vous souhaitez créer autant d'enregistrements qu'il y a d'éléments dans le tableau (en répétant toutes les colonnes telles qu'elles sont dans l'enregistrement source) :

En partant de:

| col1 | col2 | array col |

| v1 | v2 | \[1, 2, 3\] |

Obtenir:

| col1 | col2 | item col |

| v1 | v2 | 1 |

| v1 | v2 | 2 |

| v1 | v2 | 3 |

```python
df = df.withColumn(

"column_created_to_display_array_item",

F.explode(df.column_with_the_array_to_explode)

)
```

### Explode_outer

Lorsque le tableau que vous souhaitez décomposer contient des valeurs vides, explode_outer conservera la ligne ou la colonne et ajoutera des valeurs "null".

En partant de:

| col1 | col2 | array col |

| v1 | v2 | \[1, 2, null\] |

| v3 | v4 | null |

Obtenir:

| col1 | col2 | item col |

| v1 | v2 | 1 |

| v1 | v2 | 2 |

| v1 | v2 | null |

| v3 | v4 | null |

```python
df = df.withColumn(

"column_created_to_display_array_item",

F.explode_outer(df.column_with_the_array_to_explode)

)
```

## Collecter une liste

Lorsque vous souhaitez regrouper plusieurs enregistrements et collecter les valeurs d'une colonne sous forme de liste :

En partant de :

col_1 | value

X | 1

X | 2

Obtenir :

col_1 | values_list

X | \[1, 2\]

```python
df = df.groupby('col_1').agg(

F.collect_list(F.col('value')).alias('values_list')

)
```

Si vous ne voulez pas de doublons dans la valeur collectée, utilisez collect_set() au lieu de collect_list(). Veuillez noter que le résultat n'est pas déterministe, ce qui signifie que l'ordre des valeurs n'est pas garanti. 

:::info

Veuillez vous référer ici pour plus d'informations sur le formatage de la date :

[`Spark Docs`](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html)
