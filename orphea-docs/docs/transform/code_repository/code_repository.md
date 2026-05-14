# What is Code Repository ?

Code Repositories is a web-based IDE that allows users to write and collaborate on production-ready code within Orphea. The platform features a user-friendly interface for interacting with Git repositories, and offers various additional capabilities such as:

- Performing common Git version control actions like branching, committing, and tagging releases through the web interface.

- Collaboration and code review support through pull requests, with customizable permissions to ensure code quality.

- Integrated tools to enhance the code authoring experience, including IntelliSense, code linting, error checking, and rich help dialogs.

# Types of Repositories

Code Repositories offers support for creating various types of repositories, each with its own set of features.

- Transforms repositories are designed for authoring data transformation logic. They provide tools for previewing and debugging transforms, and support multiple programming languages such as Python, SQL and R.

- Functions repositories are intended for writing business logic that can be executed quickly in an operational setting. They come with built-in support for accessing data from the Orphea Datasets.

Additionally, the environment allows for previewing Functions while they are being authored, and all Functions are written in PySpark or SQL. Code Repositories also provides support for developing models.

# Transforming data

Transforming datasets refers to the process of manipulating, modifying, or converting data from one form to another in order to make it more suitable for analysis, modeling, or other purposes.

Orphea uses PySpark, a programming language that provides a way to interact with Apache Spark, a powerful data processing framework. With PySpark, you can quickly and easily work with very large datasets across multiple servers, which can provide significant improvements in performance and reliability.

DataFrame: A DataFrame is a table-like data structure that consists of named columns and rows. It resembles an SQL database in structure, but it is not relational. Once created, a DataFrame cannot be changed, but it can be used to create a new DataFrame with transformed data. Although datasets can be overwritten, Orphea keeps track of version history, so you can always go back to previous builds. DataFrame transformations are lazily evaluated, meaning that a series of tasks are evaluated as a single action and executed only when a build is initiated.

RDD: Resilient Distributed Datasets is the fundamental data structure that supports DataFrame operations. By breaking down the DataFrame into non-overlapping subsets and distributing them across a cluster of computers (nodes). PySpark can execute transformations in parallel across multiple nodes. Though this process happens in the background, it's crucial to keep in mind when working with PySpark.

Spark DataFrames are specifically designed and optimized to handle massive amounts of structured data that could range from petabytes to even larger data sets. This feature allows DataFrames to process and manipulate data in a distributed computing environment, which makes them ideal for big data applications.

PySpark generates entirely new datasets as opposed to SQL, which produces virtual table result-sets. This feature allows for the creation of new datasets based on derived datasets. Furthermore, Orphea, a data operating system, automatically links datasets through directed tree relationships, which helps in tracking the data lineage of Spark transformations via Bezier. This provides a means of exploring the dependencies that go into creating a dataset and where those datasets originated.
