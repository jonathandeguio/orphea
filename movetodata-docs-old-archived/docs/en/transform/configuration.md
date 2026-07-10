# Overview

<div style="text-align: justify">Code Repositories is a web-based IDE that allows users to write and collaborate on production-ready code within MoveToData. The platform features a user-friendly interface for interacting with Git repositories, and offers various additional capabilities such as:

- Performing common Git version control actions like branching, committing, and tagging releases through the web interface.

- Collaboration and code review support through pull requests, with customizable permissions to ensure code quality.

- Integrated tools to enhance the code authoring experience, including IntelliSense, code linting, error checking, and rich help dialogs.

# Types of Repositories

Code Repositories offers support for creating various types of repositories, each with its own set of features.

- Transforms repositories are designed for authoring data transformation logic. They provide tools for previewing and debugging transforms, and support multiple programming languages such as Python, Java, and SQL.

- Functions repositories are intended for writing business logic that can be executed quickly in an operational setting. They come with built-in support for accessing data from the MoveToData Ontology, and include TypeScript autocomplete based on Ontology data types.

<p style="text-indent: 20px;">Additionally, the environment allows for previewing Functions while they are being authored, and all Functions are written in TypeScript. Code Repositories also provides support for developing models.</p>

- [Learn more about developing models.](../analyse/overview.md)

# Transforming data

Transforming datasets refers to the process of manipulating, modifying, or converting data from one form to another in order to make it more suitable for analysis, modeling, or other purposes.

<p style="text-indent: 20px;">MoveToData uses PySpark, a programming language that provides a way to interact with Apache Spark, a powerful data processing framework. With PySpark, you can quickly and easily work with very large datasets across multiple servers, which can provide significant improvements in performance and reliability.</p>

<p style="text-indent: 20px;">DataFrame: A DataFrame is a table-like data structure that consists of named columns and rows. It resembles an SQL database in structure, but it is not relational. Once created, a DataFrame cannot be changed, but it can be used to create a new DataFrame with transformed data. Although datasets can be overwritten, MoveToData keeps track of version history, so you can always go back to previous builds. DataFrame transformations are lazily evaluated, meaning that a series of tasks are evaluated as a single action and executed only when a build is initiated.</p>

<p style="text-indent: 20px;">RDD: Resilient Distributed Datasets is the fundamental data structure that supports DataFrame operations. By breaking down the DataFrame into non-overlapping subsets and distributing them across a cluster of computers (nodes). PySpark can execute transformations in parallel across multiple nodes. Though this process happens in the background, it's crucial to keep in mind when working with PySpark.</p>

<p style="text-indent: 20px;">Spark DataFrames are specifically designed and optimized to handle massive amounts of structured data that could range from petabytes to even larger data sets. This feature allows DataFrames to process and manipulate data in a distributed computing environment, which makes them ideal for big data applications.</p>

<p style="text-indent: 20px;">PySpark generates entirely new datasets as opposed to SQL, which produces virtual table result-sets. This feature allows for the creation of new datasets based on derived datasets. Furthermore, MoveToData, a data operating system, automatically links datasets through directed tree relationships, which helps in tracking the data lineage of Spark transformations via Bezier. This provides a means of exploring the dependencies that go into creating a dataset and where those datasets originated.</p>

## PySpark Code Basics

This guide will help you transform different datasets within MoveToData. Here's a step-by step tutorial to transform data:

- Log in to your account
- Select Projects on the sidebar menu
- Select your folder under the Projects Table
- Select your particular folders to open the dataset
- On the top right of the screen click on repository
- You will be redirected to the Code Workbook and Repository page.
- You can write code in Python, PySpark or R.

<p style="text-indent: 20px;">In Code Repositories, at the beginning of your Python script, you typically need to include an import statement to access various functions provided by external libraries or modules.</p>

<p style="text-indent: 20px;"><b>Here is how your page would look like:</b></p>

```python
 from movetodata import funnel
 import pyspark.sql.functions as F
```
<p style="text-indent: 20px;"><code>source_dataset</code>: references a DataFrame that represents a Dataset stored within MoveToData.</p>

<p style="text-indent: 20px;"><code>target_dataset</code>: within this function is where you may define a series of transformations you want to see applied to <code>source_dataset</code>. Once you trigger a build with your code, the results are saved into a new Dataset file in MoveToData, which you can explore once the build completes.</p></div>

## Filtering

This below code will filter the dataframe on Column Payment_Type for "Visa":

```python
@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "Visa")

    return target_df
```
This below code will filter the dataframe on Column Payment_Type for "MasterCard":

```python
@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "MasterCard")

    return target_df
```

This code below will filter the dataframe on the column to "MasterCard" and change the column name to "Transaction Date":

```python
@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "MasterCard")
    target_df = target.dfwithColumn("Transaction Date", F.to_date(target_df["Transaction date"], "M/d/yyyy HH:mm"))

    return target_df
```

This code below will filter the dataframe on the State column for "England":

```python
@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "England")

    return target_df
```

This code below will filter the dataframe on the State column for "Scotland":

```python
@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df
```

## Joins

This code below will join the two dataframes (sources) into a new dataframe.

```python
@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2)
def user_transform_function4(source1, source 2):
    target_df = source2.union(source1) #réunissant les deux

    return target_df
```

## Column Changes

This code below will change the column name of the dataframe:

```python
@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "MasterCard")
    target_df = target_df.withColumnRenamed("Product", F.to_number(target_df["Product no."], "Product"))

    return target_df
```


## Date and Timestamp

This code below will be converted to the date and timestamp of the dataframe:

```python
@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "MasterCard")
    target_df = target_df.withColumn("Transaction_date", F.to_date(target_df["Transaction date"], "M/d/yyyy HH:mm"))

    return target_df
```

