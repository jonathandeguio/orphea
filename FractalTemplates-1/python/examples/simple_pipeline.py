from orphea import funnel



"""


https://www.orphea.io
"""

# Transformation 1
source_dataset1 = "/Projects/Test/Data/Simple_Pipeline/SalesRecords"
target_dataset = "/Projects/Test/Data/Simple_Pipeline/Sales_Visa"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "Visa")

    return target_df


# Transformation 2
source_dataset1 = "/Projects/Test/Data/Simple_Pipeline/SalesRecords"
target_dataset = "/Projects/Test/Data/Simple_Pipeline/Sales_Mastercard"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function1(source1):
    target_df = source1.filter(source1.Payment_Type == "Mastercard")

    return target_df


# Transformation 3
source_dataset1 = "/Projects/Test/Data/Simple_Pipeline/Sales_Visa"
target_dataset = "/Projects/Test/Data/Simple_Pipeline/Sales_England"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "England")

    return target_df


# Transformation 4
source_dataset1 = "/Projects/Test/Data/Simple_Pipeline/Sales_Mastercard"
target_dataset = "/Projects/Test/Data/Simple_Pipeline/Sales_Scotland"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


# Transformation 5
source_dataset1 = "/Projects/Test/Data/Simple_Pipeline/Sales_England"
source_dataset2 = "/Projects/Test/Data/Simple_Pipeline/Sales_Scotland"
target_dataset = "/Projects/Test/Data/Simple_Pipeline/Sales_Final"


@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2
        )
def user_transform_function(source1, source2):

    target_df = source2.union(source1)  # join together both

    return target_df