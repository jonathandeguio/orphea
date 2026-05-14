from orphea.functions import funnel, Source, Target

print("hello world")


@funnel(nomral_input=10, normal_input2=20, real_input=Source("bb81d44d-ef21-4391-aa20-5113cfc17b1e", "master"), real_output=Target("/Projects/Test/Données/orders_new"))
def myFunc(nomral_input, normal_input2, real_input, real_output):
    print(real_input.alias)
    print(real_input.dataframe().show())
    real_output.write_dataframe(real_input.dataframe())

