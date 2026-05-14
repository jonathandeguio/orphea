# Notebook

The notebook constitutes an integral component of data science workflows. Within Orphea, we've seamlessly integrated the notebook functionality into our code repository infrastructure.

This notebook facilitates access to secure datasets hosted on the platform, empowering users to conduct analyses alongside code execution.

Moreover, the notebook serves as a valuable environment for testing code prior to its integration into the pipeline, ensuring robustness and reliability in data processing workflows.

You have to use some of the internal orphea functions to access the datasets.

Here is the example of a cell:

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
In above exmaple user is reading a dataset from a project within the platform.
:::
