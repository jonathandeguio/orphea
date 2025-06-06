import ReactPlayer from "react-player"

# Area

## **Area Chart**
> An **Area Chart** is a graphical representation that displays quantitative data using a series of data points connected by lines, with the area below the line filled in. This visualization is useful for showing trends over time or other continuous variables. This documentation will guide you through the configuration options available for creating an area chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **X-Axis:** The **X-Axis** option allows you to configure the horizontal axis of the area chart based on various parameters. You can choose from the following parameters:
>> **Time Unit**: Facilitates changes in date and year, crucial for time-series data.  
**Sort**: Offers two sorting options - Ascending and Descending, useful for ordering data points.

> **Y-Axis:** The **Y-Axis** option allows you to configure the vertical axis of the area chart. The available parameters include:
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

### **Creating an area chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-areaChart.mp4" width="100%" />
   </div>
