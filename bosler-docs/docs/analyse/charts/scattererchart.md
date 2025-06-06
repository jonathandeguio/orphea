import ReactPlayer from "react-player"

# Scatter

## **Scatter Chart**
> A **Scatter Chart** is a graphical representation that uses points to display the relationship between two numerical variables. Each point represents an observation in the dataset, making it useful for identifying correlations or patterns between variables. This documentation will guide you through the configuration options available for creating a scatter chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **X-Axis:** The **X-Axis** option allows you to configure the horizontal axis of the scatter chart based on various parameters. You can choose from the following parameters:
>> **Time Unit**: Facilitates changes in date and year, crucial for time-series data.  
**Sort**: Offers two sorting options - Ascending and Descending, useful for ordering data points.

> **Y-Axis:** The **Y-Axis** option allows you to configure the vertical axis of the scatter chart. The available parameters include:
>> **Aggregate**: Provides methods to summarize the data, including:
        - Count: Total count of rows.
        - Distinct: Count of distinct/unique values.
        - Average: Mean of all values.
        - Sum: Sum of all values.
        - Min: The minimum value.
        - Max: The maximum value.
        - Variance: Sample variance of values.
        - Standard Deviation: Dispersion of a dataset relative to its mean.
>> **Group By**: Enables grouping data by specific parameters, such as 
        - Category, Date, Symbol, etc., to display relationships across these groups.

### **Creating a scatter chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-scatterChart.mp4" width="100%" />
   </div>
