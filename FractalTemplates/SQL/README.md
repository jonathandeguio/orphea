# SQL transformation

To create SQL transformation file, make sure to create with extension .sql

When creating a SQL transformation file, it is important to ensure that it is saved with the correct file extension, 
which is ".sql". The purpose of a SQL transformation is to manipulate data stored in one or more database tables and 
generate a new output.

For example, consider the following SQL code that creates a new table named "car_prices_out" and populates it with data 
from an existing table named "car_prices" located in the directory "/Projects/TestSQL/Data/SQL_Tranform_Test/":

Here is an example :

```SQL
CREATE TABLE `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices_out`
AS
    SELECT * FROM `/Projects/TestSQL/Data/SQL_Tranform_Test/car_prices`
```

This code creates a new table with the same columns and data as the original "car_prices" table. The output is saved in 
the directory "/Projects/TestSQL/Data/SQL_Tranform_Test/" with the file name "car_prices_out.sql".

In summary, SQL transformations are a powerful tool for manipulating data and generating new outputs. By following best 
practices such as using the correct file extension and carefully crafting SQL code, users can create efficient and 
effective transformations that meet their specific needs.