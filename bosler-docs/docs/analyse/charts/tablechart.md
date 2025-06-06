import ReactPlayer from "react-player"

# Table

## **Table Chart**
> A **Table Chart** is a tabular representation of data where each cell displays a value, allowing for easy comparison and detailed examination of data points across different dimensions. This chart type is useful for displaying raw data in a structured format. This documentation will guide you through the configuration options available for creating a table chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Columns:** The **Columns** option allows you to configure which data fields are displayed as columns in the table chart. You can choose from the following parameters:
>> **Fields**: Select the specific fields or metrics to display as columns, such as Date, Symbol, Open, High, Low, Close, Volume, etc.
**Sorting**: Define how columns are sorted, including:
        - Ascending: Sort columns in ascending order.
        - Descending: Sort columns in descending order.

> **Rows:** The **Rows** option allows you to configure which data fields are displayed as rows in the table chart. You can choose from the following parameters:
>> **Fields**: Select the specific fields or metrics to display as rows, similar to columns.

>> **Pagination**: Manage how many rows are displayed per page if the dataset is large, including:
        - Page Size: Define the number of rows per page.
        - Navigation: Options for navigating between pages.
>> **Aggregate**: Provides methods to summarize the data, including:
        - Count: Total count of rows.
        - Distinct: Count of distinct/unique values.
        - Average: Mean of all values.
        - Sum: Sum of all values.
        - Min: The minimum value.
        - Max: The maximum value.
        - Variance: Sample variance of values.
        - Standard Deviation: Dispersion of a dataset relative to its mean.
> **Filters:** The **Filters** option allows you to apply conditions to refine the displayed data. You can filter based on parameters such as:
>> **Date Range**: Select a specific time period to display data for that range.
**Category**: Filter by categories relevant to the data, such as Product Lines, Regions, etc.

### **Creating a table chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-tableChart.mp4" width="100%" />
   </div>
