import ReactPlayer from "react-player"

# Line

## **Line Chart**
> A **Line Chart** is a graphical representation that uses a series of data points connected by a continuous line to show trends over time or other continuous variables. This documentation will guide you through the configuration options available for creating a line chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **X-Axis:** The **X-Axis** option allows you to configure the horizontal axis of the line chart based on various parameters. You can choose from the following parameters:
>> **Time Unit**: Facilitates changes in date and year, crucial for time-series data.  
**Sort**: Offers two sorting options - Ascending and Descending, useful for ordering data points.

> **Y-Axis:**  The **Y-Axis** option allows you to configure the vertical axis of the line chart. The available parameters include:
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
        - Category, Date, Symbol, etc., to display trends across these groups.

### **Creating a line chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-lineChart.mp4" width="100%" />
   </div>
