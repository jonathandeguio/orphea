import ReactPlayer from "react-player"

# Sunburst

## **Sunburst Chart**
> A **Sunburst Chart** is a hierarchical visualization that displays data as a series of concentric rings. Each ring represents a level in the hierarchy, with the center representing the root level and the outer rings representing sub-levels. This chart type is useful for visualizing hierarchical data and understanding the relationships between different levels of the hierarchy. This documentation will guide you through the configuration options available for creating a sunburst chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Hierarchy Levels:** The **Hierarchy Levels** option allows you to configure the different levels of the hierarchy displayed in the sunburst chart. You can choose from the following parameters:
>> **Root Level**: Define the root or starting level of the hierarchy.
**Sub-Level Configuration**: Customize the appearance and labels of each sub-level, including:
        - Level 1: First level of hierarchy.
        - Level 2: Second level of hierarchy, and so on.

> **Segment Values:** The **Segment Values** option allows you to configure the data displayed within each segment of the sunburst chart. You can choose from the following parameters:
>> **Values**: Select the data fields or metrics to display within each segment, such as Sales, Revenue, or Count.
**Aggregation Method**: Provides methods to summarize the data, including:
        - Count: Total count of rows.
        - Distinct: Count of distinct/unique values.
        - Average: Mean of all values.
        - Sum: Sum of all values.
        - Min: The minimum value.
        - Max: The maximum value.
        - Variance: Sample variance of values.
        - Standard Deviation: Dispersion of a dataset relative to its mean.

> **Labels and Colors:** The **Labels and Colors** option allows you to customize the labels and colors of the segments in the sunburst chart. You can configure:
>> **Label Position**: Define where labels are positioned on the chart, such as inside or outside each segment.
**Color Scheme**: Choose or customize the color scheme used for different segments and levels.

### **Creating a sunburst chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-sunburstChart.mp4" width="100%" />
   </div>
