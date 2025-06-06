from bosler import funnel

for i in range(15):
    source_dataset1 = "/Projects/Test/Data/Complex_Pipeline/Parent1"
    source_dataset2 = "/Projects/Test/Data/Complex_Pipeline/Parent2"
    target_dataset = f"/Projects/Test/Data/Complex_Pipeline/Dataset_Intermediate_{i}"

    @funnel(target=target_dataset,
            source1=source_dataset1,
            source2=source_dataset2
            )
    def user_transform_function(source1, source2):

        target_df = source2.union(source1)  # join together both

        return target_df

sources = dict()
for i in range(15):
    @funnel(target="/Projects/Test/Data/Complex_Pipeline/Dataset_Final",
            sourcedf1=f"/Projects/Test/Data/Complex_Pipeline/Dataset_Intermediate_{i}")
    def user_transform_function2(sourcedf1):
        return sourcedf1

source_dataset1 = "/Projects/Test/Data/Complex_Pipeline/Intermediate1"
target_dataset = "/Projects/Test/Data/Complex_Pipeline/Dataset_Final"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2v(source1):
    return source1


source_dataset1 = "/Projects/Test/Data/Complex_Pipeline/Intermediate1"
target_dataset = "/Projects/Test/Data/Complex_Pipeline/Dataset_Final"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2a(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


for i in range(15):
    source_dataset1 = "/Projects/Test/Data/Complex_Pipeline/before_final"
    target_dataset = f"/Projects/Test/Data/Complex_Pipeline/Dataset_Final_{i}"

    @funnel(target=target_dataset,
            source1=source_dataset1)
    def user_transform_function2b(source1):
        target_df = source1.filter(source1.State == "Scotland")

        return target_df


source_dataset1 = "/Projects/Test/Data/Complex_Pipeline/Dataset_Intermediate"
target_dataset = "/Projects/Test/Data/Complex_Pipeline/Dataset_Final_1"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2c(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


source_dataset1 = "/Projects/Test/Data/Complex_Pipeline/Dataset_Intermediate"
target_dataset = "/Projects/Test/Data/Complex_Pipeline/Dataset_Final2"


@funnel(target=target_dataset,
        source1=source_dataset1)
def user_transform_function2d(source1):
    target_df = source1.filter(source1.State == "Scotland")

    return target_df


source_dataset1 = "/Projects/Test/Data/Complex_Pipeline/Parent2"
source_dataset2 = "/Projects/Test/Data/Complex_Pipeline/before_final"
target_dataset = "/Projects/Test/Data/Complex_Pipeline/Dataset_Final2"


@funnel(target=target_dataset,
        source1=source_dataset1,
        source2=source_dataset2)
def user_transform_function2e(source1, source2):
    target_df = source2.union(source1)

    return target_df
