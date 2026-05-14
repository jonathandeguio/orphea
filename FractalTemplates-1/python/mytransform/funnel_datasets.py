from orphea import funnel

# Transformation 1
source_dataset1 = "/Projects/Test/Data/SalesRecords"
target_dataset = "/Projects/Test/Data/Sales_Visa"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function(source1):
    target_df = source1.filter(source1.Payment_Type == "Visa")

    return target_df
