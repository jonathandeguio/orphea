import ReactPlayer from "react-player"

# Treemap

## **Treemap Chart**
> A **Treemap Chart** is a hierarchical visualization that displays data as nested rectangles. Each rectangle represents a category and its size is proportional to the value it represents. This chart type is useful for visualizing large amounts of hierarchical data and understanding the proportions and relationships between different categories. This documentation will guide you through the configuration options available for creating a treemap chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Hierarchy Levels:** The **Hierarchy Levels** option allows you to configure the different levels of the hierarchy displayed in the treemap chart. You can choose from the following parameters:
>> **Root Level**: Define the root or top level of the hierarchy.
**Sub-Level Configuration**: Customize the appearance and labels of each sub-level, including:
        - Level 1: First level of hierarchy.
        - Level 2: Second level of hierarchy, and so on.

> **Rectangle Sizes:** The **Rectangle Sizes** option allows you to configure the size of each rectangle based on the data values. You can choose from the following parameters:
>> **Values**: Select the data fields or metrics to determine the size of each rectangle, such as Sales, Revenue, or Count.
**Aggregation Method**: Provides methods to summarize the data, including:
        - Count: Total count of rows.
        - Distinct: Count of distinct/unique values.
        - Average: Mean of all values.
        - Sum: Sum of all values.
        - Min: The minimum value.
        - Max: The maximum value.
        - Variance: Sample variance of values.
        - Standard Deviation: Dispersion of a dataset relative to its mean.

> **Labels and Colors:** The **Labels and Colors** option allows you to customize the labels and colors of the rectangles in the treemap chart. You can configure:
>> **Label Position**: Define where labels are positioned on the rectangles, such as inside or outside each rectangle.
**Color Scheme**: Choose or customize the color scheme used for different categories and levels.

### **Creating a treemap chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-treemapChart.mp4" width="100%" />
   </div>
