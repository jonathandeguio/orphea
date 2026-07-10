import ReactPlayer from "react-player"

# Radar

## **Radar Chart**
> A **Radar Chart** is a graphical representation that uses a circular layout to display multivariate data, with each axis representing a different variable. This chart type is useful for comparing multiple variables across different categories or entities. This documentation will guide you through the configuration options available for creating a radar chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Axes:** The **Axes** option allows you to configure the individual axes of the radar chart. You can choose from the following parameters:
>> **Variables**: Select the specific variables to display as axes, such as Performance Metrics, Categories, or Attributes.
**Scale**: Define the scale of each axis, including:
        - Min Value: Set the minimum value for the axis.
        - Max Value: Set the maximum value for the axis.
        - Step Size: Define the interval between scale values.
>> **Aggregate**: Provides methods to summarize the data, including:
        - Count: Total count of rows.
        - Distinct: Count of distinct/unique values.
        - Average: Mean of all values.
        - Sum: Sum of all values.
        - Min: The minimum value.
        - Max: The maximum value.
        - Variance: Sample variance of values.
        - Standard Deviation: Dispersion of a dataset relative to its mean.
> **Series:** The **Series** option allows you to configure the data series displayed on the radar chart. You can choose from the following parameters:
>> **Data Series**: Select the data series to display, such as different categories or groups.
**Color and Style**: Customize the appearance of each series, including color, line style, and marker style.

> **Labels and Legends:** The **Labels and Legends** option allows you to customize the labels and legends displayed on the radar chart. You can configure:
>> **Label Position**: Define where labels are positioned on the radar chart, such as on the axes or inside the chart.
**Legend**: Customize the legend to indicate what each series represents, including position and style.

### **Creating a radar chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-radarChart.mp4" width="100%" />
   </div>
