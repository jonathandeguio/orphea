import ReactPlayer from "react-player"

# Pie

## **Pie Chart**
> A **Pie Chart** is a circular statistical graphic used to represent numerical proportions. This documentation will guide you through the configuration options available for creating a pie chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Size By:** 
>> The **Size By** option allows you to determine the size of each segment of the pie chart based on various parameters. You can choose from the following parameters:
      - Symbol: Represents different categories or items.
      - Series: Represents different data series.
      - Open: The opening value of the data points.
      - High: The highest value in the data points.
      - Low: The lowest value in the data points.
      - Close: The closing value of the data points.
      - Volume: The volume of transactions.

> **Aggregate:** 
>> The **Aggregate** option provides several methods to summarize the data. The available methods include:
      - Count: Total count of rows.
      - Distinct: Count of distinct/unique values.
      - Average: Mean of all values.
      - Sum: Sum of all values.
      - Min: The minimum value.
      - Max: The maximum value.
      - Variance: Sample variance of values.
      - Standard Deviation: Dispersion of a dataset relative to its mean.

> **Group By:** 
>> The **Group By** option allows you to group data by specific parameters. This can help in organizing the data into meaningful segments. *You can group by*:
      - Symbol
      - Series
      - Open
      - High
      - Low
      - Close
      - Volume

### **Creating a pie chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-pieChart.mp4" width="100%" />
   </div>
