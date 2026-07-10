# PySpark Basics

This guide will help you transform different datasets within MoveToData. Here's a step-by step tutorial to transform data:

- Log in to your account
- Select Projects on the sidebar menu
- Select your folder under the Projects Table
- Select your particular folders to open the dataset
- On the top right of the screen click on repository
- You will be redirected to the Code Workbook and Repository page.
- You can write code in Python.

In Code Repositories, at the beginning of your Python script, you typically need to include an import statement to access various functions provided by external libraries or modules.

Here is how your page would look like:

```python
 from movetodata.functions import funnel
 import pyspark.sql.functions as F
```

<code>source_dataset</code>: references a DataFrame that represents a Dataset stored within MoveToData.

<code>target_dataset</code>: within this function is where you may define a series of transformations you want to see applied to <code>source_dataset</code>. Once you trigger a build with your code, the results are saved into a new Dataset file in MoveToData, which you can explore once the build completes.

## Sample example Code

The following code snippet illustrates the utilization of multiple datasets as both sources and targets, with the ability to pass variables through the decorator:

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

## Filtering

This below code will filter the dataframe on Column Payment_Type for "Visa":

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type =="Visa")

    target.write_dataframe(target_df)
```

This below code will filter the dataframe on Column Payment_Type for "MasterCard":

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type =="MasterCard")

    target.write_dataframe(target_df)
```

This code below will filter the dataframe on the column to "MasterCard" and change the column name to "Transaction Date":

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type =="MasterCard")
    target_df = target.dfwithColumn("Transaction Date", F.to_date(target_df["Transaction date"], "M/d/yyyy HH:mm"))

    target.write_dataframe(target_df)
```

This code below will filter the dataframe on the State column for "England":

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().State == "England")

    target.write_dataframe(target_df)
```

This code below will filter the dataframe on the State column for "Scotland":

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().State == "Scotland")

    target.write_dataframe(target_df)
```

## Joins

This code below will join the two dataframes (sources) into a new dataframe.

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1),
        source2=source_dataset2)
def user_transform_function4(source1, source 2):
    target_df = source2.dataframe().union(source1.dataframe()) #réunissant les deux

    target.write_dataframe(target_df)
```

:::info
In data processing, it is possible to have `multiple source datasets` at one time; and also it is possible to have multiple targets. You can even pass variables in the decorator.
:::

## Column Changes

This code below will change the column name of the dataframe:

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type =="MasterCard")
    target_df = target_df.withColumnRenamed("Product", F.to_number(target_df["Product no."], "Product"))

    target.write_dataframe(target_df)
```

## Date and Timestamp

This code below will be converted to the date and timestamp of the dataframe:

```python
@funnel(target=Target(target_dataset),
        source1=Soruce(source_dataset1))
def user_transform_function(source1):
    target_df = source1.dataframe().filter(source1.dataframe().Payment_Type =="MasterCard")
    target_df = target_df.withColumn("Transaction_date", F.to_date(target_df["Transaction date"], "M/d/yyyy HH:mm"))

    target.write_dataframe(target_df)
```

# Data Transform - cheat sheet (code snippets)

This report offers some code snippets for code repository apps, to ease development and share best practices.

Imports

SQL functions enable a lot of basic transform actions, add this line at the beginning of your transform file

```python
from pyspark.sql import functions as F
```

## Manipulate columns

Select only a subset of columns (including columns reordering)

```python
myColumns = \['name1', 'name2', 'name10'\]

df = df.select(\*myColumns)
```

Select only a subset of columns (by specifying exclusion list)

```python
myUnwantedColumns = \['name1', 'name2', 'name10'\]

df = df.drop(\*myUnwantedColumns)
```

Filter on a column value

Equal:

```python
myUnwantedColumns = \['name1', 'name2', 'name10'\]

df = df.drop(\*myUnwantedColumns)

df = df.filter(F.col('myColumnName') == 'myValue') # if string dataType

df = df.filter(F.col('myColumnName') == True) # if boolean dataType

df = df.filter(F.col('myColumnName').isNull()) # to keep only records with null value in 'myColumnName' (or isNotNull())
```

\# or in list:

```python
inclusion_list = \['myValue1', 'myValue2', 'myValue3'\]

df = df.filter(F.col('myColumnName').isin(inclusion_list))
```

not equal:

```python
df = df.filter(F.col('myColumnName') != 'myValue')
```

\# or NOT in list:

```python
exclusion_list = \['myValue1', 'myValue2', 'myValue3'\]

df = df.filter(~F.col('myColumnName').isin(exclusion_list))
```

Be very careful with Null values (by default assessed as not matching)

\# this way also filters out records with Null:

```python
df = df.filter(F.col('Name') != 'John Doe')
```

\# this way preserves Null (not excluded):

```python
df = df.filter(~F.col('Name').eqNullSafe('John Doe')
```

Rename columns

single column:

```python
df = df.withColumnRenamed('old_name', 'new_name')
```

multiple columns based on a generic modification (like adding prefix or suffix):

```python
df = df.select(\*\[F.col(c).alias(f"myPrefix\_{c}\_mySuffix") for c in df.columns\])
```

multiple columns based on user decision defined in a dictionary

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

Main Join types (additional ones can be found in documentation):

- left: all records from df1 and only records from df2 matching
- inner: all records that are in both df1 and df2 and join matchs
- full: all records from both df1 and df2 whatever the match

### Complex case where column names do not match or if multiple conditions are needed to define the join

```python
df = df1.join(df2,

(F.col('name1') == F.col('name2')) & (F.col('name3') == F.col('name4')),

'left'

)
```

Main Multiple conditions operands (additional ones can be found in documentation):

- &: AND (condition1 and condition2 must match
- |: OR (at least condition1 or condition2 must match)

### Complex case with all columns from df1 and df2 renamed

```python
df = df1.select(\*\[F.col(c).alias(f"df1\_{c}") for c in df1.columns\]).join(

df2.select(\*\[F.col(c).alias(f"df2\_{c}") for c in df2.columns\]),

(F.col('name1') == F.col('name2')) & (F.col('name3') == F.col('name4')),

'left'

)
```

## Pivot dataset

If you want to pivot table just like in below example

### Starting table

| Name     | Tag_name    | Tag_value |
| -------- | ----------- | --------- |
| ProjectA | Status      | Open      |
| ---      | ---         | ---       |
| ProjectA | Sponsorship | IM        |
| ---      | ---         | ---       |

### Final table

| Name     | Status | Sponsorship |
| -------- | ------ | ----------- |
| ProjectA | Open   | IM          |
| ---      | ---    | ---         |

Please use this code

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

## Create new calculated column

Create a boolean column stating if another column has value from a list

```python
df = df.withColumn('myNewColumn', F.col('sourceColumn').isin(inclusion_list))
```

Create a boolean column stating if another column contains a searched_string:

```python
df = df.withColumn('myNewColumn', F.col('sourceColumn').like("%searched_string%"))
```

Create a boolean column stating if another column is NULL (or not NULL):

```python
df = df.withColumn('myNewColumn', F.col('sourceColumn').isNull())

df = df.withColumn('myNewColumn', F.col('sourceColumn').isNotNull())
```

Create a boolean column stating if 2 columns are equals:

```python
df = df.withColumn('myNewColumn', F.col('column1') == F.col('column2'))
```

Create from JSON key: let say you have a field, called 'branch', JSON formatted like

```python
df = df.withColumn('branch_name', F.col('branch.name'))
```

Create a constant:

```python
df = df.withColumn('column_name', F.lit('my constant value'))
```

Create a multiple values depending on multiple conditions (switch, if/else, case/when equivalent):

```python
df = df.withColumn("colorName", F.when(df.colorCode == "R", "Red")

.when(df.colorCode == "G", "Green")

.when(df.colorCode.isNull(), "")

.otherwise(df.colorCode))
```

Concatenate multiple columns:

_\# fixed columns list, including constant:_

```python
df = df.withColumn('column_name', F.concat(df.a, F.lit('my constant value'), df.c))
```

_\# columns list with defined constant separator:_

```python
columns_to_concat = \['col1', 'col2'\]

df = df.withColumn('column_name', F.concat_ws('-', \*columns_to_concat))
```

Create an array from a character separated string:

_\# for example, splitting a fullpath string like /root/folder1/folder2/folder3/file:_

```python
df = df.withColumn('column_name', F.split(F.col('source_column'), '/'))
```

Get the nth item (first = 0, second = 1, ...) of an array:

_\# starting from a source_column with array \['item1', 'item2', 'item3'\], get the 3rd item, ie 'item3':_

```python
df = df.withColumn('column_name', F.col('source_column')\[2\])
```

_\# to get last item, use reverse:_

```python
df = df.withColumn('column_name', F.reverse(F.col('source_column'))\[0\])
```

## Explode dataset

When one field is an array and you want to create from one record as many records as items in the array (repeating all columns as they are in the source record).

Starting from:

| col1 | col2 | array col |

| v1 | v2 | \[1, 2, 3\] |

Getting:

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

When the array you want to explode contains empty values, "explode_outer" will keep the row or column and add "null" values.

Starting from:

| col1 | col2 | array col |

| v1 | v2 | \[1, 2, null\] |

| v3 | v4 | null |

Getting:

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

## Collect List

When you want to group multiple records and collect one column values as a list

Starting from :

col_1 | value

X | 1

X | 2

Getting :

col_1 | values_list

X | \[1, 2\]

```python
df = df.groupby('col_1').agg(

F.collect_list(F.col('value')).alias('values_list')

)
```

If you don't want duplicates in the collected value, use collect_set() instead of collect_list(). Please note that the result is not deterministic meaning that the order of the values is not deterministic.

:::info

Please refer here for more information about date formatting :

[`Spark Docs`](https://spark.apache.org/docs/latest/sql-ref-datetime-pattern.html)

:::
