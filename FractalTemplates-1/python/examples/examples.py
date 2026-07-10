# first

from movetodata import funnel

source_dataset1 = "/Projects/Test/Data/SalesRecords"
target_dataset = "/Projects/Test/Data/Sales_Visa"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "Visa")

    return target_df


# second
from movetodata import funnel

source_dataset1 = "/Projects/Test/Data/SalesRecords"
target_dataset = "/Projects/Test/Data/Sales_Mastercard"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "Mastercard")

    return target_df


# third
from movetodata import funnel

source_dataset1 = "/Projects/Test/Data/Sales_Visa"
target_dataset = "/Projects/Test/Data/Sales_England"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "England")

    return target_df


# fourth
from movetodata import funnel

source_dataset1 = "/Projects/Test/Data/Sales_Mastercard"
target_dataset = "/Projects/Test/Data/Sales_Scotland"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


# fifth
from movetodata import funnel

source_dataset1 = "/Projects/Test/Data/Sales_England"
source_dataset2 = "/Projects/Test/Data/Sales_Scotland"
target_dataset = "/Projects/Test/Data/Sales_Final"


@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2
        )
def user_transform_function(source1, source2):
    target_df = source2.union(source1)  # join together both

    return target_df


# Transformation 2
source_dataset1 = "/Projects/Test/Data/SalesRecords"
target_dataset = "/Projects/Test/Data/Sales_Mastercard"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "Mastercard")

    return target_df


# Transformation 3
source_dataset1 = "/Projects/Test/Data/Sales_Visa"
target_dataset = "/Projects/Test/Data/Sales_England"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "England")

    return target_df


# Transformation 4
source_dataset1 = "/Projects/Test/Data/Sales_Mastercard"
target_dataset = "/Projects/Test/Data/Sales_Scotland"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


# Transformation 5
source_dataset1 = "/Projects/Test/Data/Sales_England"
source_dataset2 = "/Projects/Test/Data/Sales_Scotland"
target_dataset = "/Projects/Test/Data/Sales_Final"


@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2
        )
def user_transform_function(source1, source2):
    target_df = source2.union(source1)  # join together both

    return target_df


# Multi parent test
from movetodata import funnel

source_dataset1 = "/Projects/Test/Data/Multi_Parent_Test/Multi_Parent_Test_Dataset1"
source_dataset2 = "/Projects/Test/Data/Multi_Parent_Test/Multi_Parent_Test_Dataset2"
target_dataset = "/Projects/Test/Data/Dataset_Intermediate"


@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2
        )
def user_transform_function(source1, source2):
    target_df = source2.union(source1)  # join together both

    return target_df


source_dataset1 = "/Projects/Test/Data/Dataset_Intermediate"
target_dataset = "/Projects/Test/Data/Dataset_Final"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


# more complex
from movetodata import funnel

# Transformation 1
for i in range(4):
    source_dataset1 = "/Projects/Test/Data/SalesRecords"
    target_dataset = f"/Projects/Test/Data/Sales_Visa{i}"


    @funnel(target=target_dataset,
            source1=source_dataset1)
    def user_transform_function(source1):
        target_df = source1.filter(source1.Payment_Type == "Visa")

        return target_df
source_dataset1 = "/Projects/Test/Data/Sales_Visa1"
for i in range(3):
    target_dataset = f"/Projects/Test/Data/child{i}"


    @funnel(target=target_dataset,
            source1=source_dataset1)
    def user_transform_function(source1):
        target_df = source1.filter(source1.Payment_Type == "Visa")

        return target_df

source_dataset1 = "/Projects/Test/Data/child1"
target_dataset = "/Projects/Test/Data/childX"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "Visa")

    return target_df


# added complexity with intermediate parents

from movetodata import funnel

source_dataset1 = "/Projects/Test/Data/multiparents_and_complex_interemediate/Parent1"
source_dataset2 = "/Projects/Test/Data/multiparents_and_complex_interemediate/Parent2"
target_dataset = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Intermediate"


@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2
        )
def user_transform_function(source1, source2):

    target_df = source2.union(source1)  # join together both

    return target_df


source_dataset1 = "/Projects/Test/Data/multiparents_and_complex_interemediate/Intermediate1"
target_dataset = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Final"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


source_dataset1 = "/Projects/Test/Data/multiparents_and_complex_interemediate/Intermediate1"
target_dataset = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Final"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2a(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


source_dataset1 = "/Projects/Test/Data/multiparents_and_complex_interemediate/before_final"
target_dataset = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Final"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2b(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


source_dataset1 = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Intermediate"
target_dataset = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Final"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2c(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


source_dataset1 = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Intermediate"
target_dataset = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Final2"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2c(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df



source_dataset1 = "/Projects/Test/Data/multiparents_and_complex_interemediate/Parent2"
source_dataset2 = "/Projects/Test/Data/multiparents_and_complex_interemediate/before_final"
target_dataset = "/Projects/Test/Data/multiparents_and_complex_interemediate/Dataset_Final2"


@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2)
def user_transform_function2d(source1, source2):
    target_df = source2.union(source1)

    return target_df