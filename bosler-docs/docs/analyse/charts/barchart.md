import ReactPlayer from "react-player"

# Bar

## **Bar Chart**
> A **Bar Chart** is a graphical representation that uses bars to show comparisons among categories or items. This documentation will guide you through the configuration options available for creating a bar chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **X-Axis:** 
>> The **X-Axis** option allows you to configure the horizontal axis of the bar chart based on various parameters. You can choose from the following parameters:
      - Time Unit: Facilitates changes in date and year.
      - Sort: Offers two sorting options - Ascending and Descending.

> **Y-Axis:** 
>> The **Y-Axis** option allows you to configure the vertical axis of the bar chart. The available parameters include:
      - Aggregate: Provides methods to summarize the data, including:
        - Count: Total count of rows.
        - Distinct: Count of distinct/unique values.
        - Average: Mean of all values.
        - Sum: Sum of all values.
        - Min: The minimum value.
        - Max: The maximum value.
        - Variance: Sample variance of values.
        - Standard Deviation: Dispersion of a dataset relative to its mean.
      - Group By: Enables grouping data by specific parameters similar to those in the Pie Chart, such as Symbol, Series, Open, High, Low, Close, and Volume.

### **Creating a bar chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-barChart.mp4" width="100%" />
   </div>
