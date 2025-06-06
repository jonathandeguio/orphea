from bosler.functions import funnel, Source, Target
from bosler.internal import spark_session

print("hello world")
df = spark_session().read \
        .format("jdbc") \
        .option("url", 'jdbc:postgresql://boson-db:5432/boson') \
        .option("query", 'SELECT * from build_log limit 100') \
        .option("user", 'postgres') \
        .option("password", 'solaris') \
        .option("driver", 'org.postgresql.Driver') \
        .load()

@funnel(nomral_input=10, normal_input2=20, real_input=Source("752bec36-26af-4b3d-9b73-592cad84c2b2", "master"), real_output=Target("/Projects/Test/Données/orders_new"))
def myFunc(nomral_input, normal_input2, real_input, real_output):
    print(real_input.alias)
    print(real_input.dataframe().show())
    real_output.write_dataframe(real_input.dataframe())

