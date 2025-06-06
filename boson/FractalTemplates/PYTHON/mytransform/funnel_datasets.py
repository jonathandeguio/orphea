from bosler.functions import funnel, Source, Target
from pyspark.sql import functions as F


@funnel(
    var1="test",
    first_source=Source('/path/to/first/source/dataset'),
    second_source=Source('/path/to/second/source/dataset'),
    first_target=Target('/path/to/first/target/dataset'),
    second_target=Target('/path/to/second/target/dataset'),
)
def my_compute_function(var1, first_source, second_source, first_target, second_target):

    first_source_df = first_source.dataframe()
    second_source_df = second_source.dataframe()

    first_source_df = first_source_df.filter(first_source_df.ORDER_STATUS == "completed")

    first_target.write_dataframe(first_source_df)
    second_target.write_dataframe(second_source_df)
