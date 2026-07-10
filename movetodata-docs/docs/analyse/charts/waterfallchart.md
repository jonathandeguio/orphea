import ReactPlayer from "react-player"

# Waterfall

## **Waterfall Chart**
> A **Waterfall Chart** is a graphical representation that shows the incremental changes of a value over time or categories, highlighting the cumulative effect of sequentially occurring positive or negative values. This chart type is useful for understanding the contribution of individual elements to a total value. This documentation will guide you through the configuration options available for creating a waterfall chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Categories:** The **Categories** option allows you to configure the different categories displayed in the waterfall chart. You can choose from the following parameters:
>> **Steps**: Define the sequential steps or categories to show, such as Time Periods, Phases, or Stages.
**Sorting**: Define how categories are sorted, including:
        - Ascending: Sort categories in ascending order.
        - Descending: Sort categories in descending order.

> **Values:** The **Values** option allows you to configure the data values displayed in the waterfall chart. You can choose from the following parameters:
>> **Data Fields**: Select the specific data fields to display, such as Revenue, Costs, or Profit.
**Aggregation Method**: Provides methods to summarize the data, including:
        - Count: Total count of rows.
        - Distinct: Count of distinct/unique values.
        - Average: Mean of all values.
        - Sum: Sum of all values.
        - Min: The minimum value.
        - Max: The maximum value.
        - Variance: Sample variance of values.
        - Standard Deviation: Dispersion of a dataset relative to its mean.

> **Labels and Colors:** The **Labels and Colors** option allows you to customize the labels and colors of the bars in the waterfall chart. You can configure:
>> **Label Position**: Define where labels are positioned on the chart, such as above or below each bar.
**Color Scheme**: Choose or customize the color scheme used for different types of bars, such as positive or negative changes.

### **Creating a waterfall chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-waterfallChart.mp4" width="100%" />
   </div>
