# Bases SQL

Ce guide vous aidera à transformer différents ensembles de données dans Bosler. Voici un tutoriel étape par étape pour transformer des données :

- Connectez-vous à votre compte
- Sélectionnez Projets dans le menu de la barre latérale
- Sélectionnez votre dossier sous le tableau des projets
- Sélectionnez vos dossiers particuliers pour ouvrir l'ensemble de données
- En haut à droite de l'écran cliquez sur repository
- Vous serez redirigé vers la page Code Workbook and Repository.
- Ici, vous pouvez choisir SQL.

Voici à quoi ressemblerait votre page :

```sql
 CREATE TABLE `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices_out4`
 AS
    SELECT *
    FROM `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices`
```

:::info
<code>car_prices</code> : dans cet exemple, car_prices fait référence à un DataFrame qui représente un ensemble de données stocké dans Bosler et est utilisé comme source.

<code>car_prices_out</code> : est l'ensemble de données de sortie de <code>source_dataset</code>. Une fois que vous avez déclenché une génération avec votre code, les résultats sont enregistrés dans un nouveau fichier Dataset dans Bosler, que vous pouvez explorer une fois la génération terminée.
:::

## Filtrage

Ce code ci-dessous filtrera la trame de données en fonction des instructions where :

```sql
CREATE TABLE `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices_out4`
AS
    SELECT model,kilometrage,coulor
    FROM       `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices`
        WHERE Lower(coulor)="red"
          OR Lower(coulor)="white"
          AND kilometrage < 30000
```

## Jointures

Ce code ci-dessous joindra les deux dataframes (sources) dans un nouveau dataframe.

```sql
CREATE TABLE `/Projects/TestSQL/Data/Join_Test/Customers_Orders_Joined`
AS
    SELECT Name,EMail,Phone,Product,Price
        FROM `/Projects/TestSQL/Data/Join_Test/Customers`
            JOIN `/Projects/TestSQL/Data/Join_Test/Orders`
                ON `/Projects/TestSQL/Data/Join_Test/Customers`.ID = `/Projects/TestSQL/Data/Join_Test/Orders`.CustomerID;

```

## Complexe

Voici un exemple plus complexe :

```sql more_complex.sql
CREATE TABLE `/Projects/TestSQL/Data/Join_Test/Customers_Orders_Joined`
AS
   SELECT Name as FirstName, Email,Phone, Product, Price as Prix  FROM
        ( SELECT *
            FROM `/Projects/TestSQL/Data/Join_Test/Customers`
            where Name = 'John Doe'
        ) customers
        JOIN `/Projects/TestSQL/Data/Join_Test/Orders` orders
        ON customers.ID = orders.CustomerID

```

:::info
Dans l'exemple ci-dessus, l'utilisateur joint deux tables et filtre en même temps la première table. La table de sortie aura également un nom de colonne renommé.
:::
