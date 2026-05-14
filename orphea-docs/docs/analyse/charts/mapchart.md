import ReactPlayer from "react-player"

# Map

## **Map Chart**
> A **Map Chart** is a geographic visualization that displays data on a map, representing different data points or areas. This type of chart is ideal for visualizing spatial distributions, patterns, and trends across different regions. This documentation will guide you through the configuration options available for creating a map chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Geographic Levels:** The **Geographic Levels** option allows you to configure the different levels of geography displayed on the map chart. You can choose from the following parameters:
>> **Country Level**: Define the country or top-level region.
**Region/Sub-Region Configuration**: Customize the appearance and labels of each geographic level, including:
        - State/Province: The next level of geographic hierarchy.
        - City/Town: A more granular level within the selected region.

> **Data Points:** The **Data Points** option allows you to configure how data is represented on the map. You can choose from the following parameters:
>> **Metrics**: Select the data fields or metrics to represent on the map, such as Population, Sales, or Crime Rates.
**Aggregation Method**: Provides methods to summarize the data, including:
        - None: Displays raw data.
        - Sum: Total sum of the selected metric.
        - Average: Mean value of the selected metric.
        - Count: Total count of the data points.

> **Labels and Colors:** The **Labels and Colors** option allows you to customize the labels and colors on the map. You can configure:
>> **Label Position**: Define where labels are positioned on the map, such as directly on the data points or in a legend.
**Color Scheme**: Choose or customize the color scheme used for different data points or geographic levels.

### **Creating a map chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-mapChart.mp4" width="100%" />
   </div>
