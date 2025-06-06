# SQL Basics

This guide will help you transform different datasets within Bosler. Here's a step-by step tutorial to transform data:

- Log in to your account
- Select Projects on the sidebar menu
- Select your folder under the Projects Table
- Select your particular folders to open the dataset
- On the top right of the screen click on repository
- You will be redirected to the Code Workbook and Repository page.
- Here you can choose SQL.

Here is how your page would look like:

```sql straight_copy.sql
 CREATE TABLE `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices_out4`
 AS
    SELECT *
    FROM `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices`
```

:::info
<code>car_prices</code>: in the above example, car_prices references a DataFrame that represents a Dataset stored within Bosler and is used a source.

<code>car_prices_out</code>: is the output dataset from <code>source_dataset</code>. Once you trigger a build with your code, the results are saved into a new Dataset file in Bosler, which you can explore once the build completes.
:::

## Filtering

This below code will filter the dataframe based on where statements:

```sql filter.sql
CREATE TABLE `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices_out4`
AS
    SELECT model,kilometrage,coulor
    FROM       `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices`
        WHERE Lower(coulor)="red"
          OR Lower(coulor)="white"
          AND kilometrage < 30000
```

## Joins

This code below will join the two dataframes (sources) into a new dataframe.

```sql simple_joins.sql
CREATE TABLE `/Projects/TestSQL/Data/Join_Test/Customers_Orders_Joined`
AS
    SELECT Name,EMail,Phone,Product,Price
        FROM `/Projects/TestSQL/Data/Join_Test/Customers`
            JOIN `/Projects/TestSQL/Data/Join_Test/Orders`
                ON `/Projects/TestSQL/Data/Join_Test/Customers`.ID = `/Projects/TestSQL/Data/Join_Test/Orders`.CustomerID;

```

## Complex

This is more complex example:

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
In above exmaple user is joining two tables and filtering same time first table. The output table will have column name renamed also.
:::
