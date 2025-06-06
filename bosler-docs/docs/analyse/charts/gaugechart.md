import ReactPlayer from "react-player"

# Gauge

## **Gauge Chart**
> A **Gauge Chart** is a graphical representation that uses a dial or gauge to display a single value within a range, often used to show progress towards a goal or to represent performance metrics. This chart type is useful for visualizing how close a value is to a target. This documentation will guide you through the configuration options available for creating a gauge chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Gauge Range:** The **Gauge Range** option allows you to configure the ranges and thresholds displayed on the gauge chart. You can choose from the following parameters:
>> **Min Value**: Set the minimum value of the gauge.
**Max Value**: Set the maximum value of the gauge.
**Thresholds**: Define different ranges to indicate various levels, such as:
        - Low: Range indicating low performance.
        - Medium: Range indicating moderate performance.
        - High: Range indicating high performance.

> **Display Value:** The **Display Value** option allows you to choose the key metric or value that will be prominently shown on the gauge. You can select from the following parameters:
>> **Metric**: Choose the specific value to display on the gauge, such as Current Value, Target Value, or Percentage Complete.

> **Labels and Indicators:** The **Labels and Indicators** option allows you to customize the labels and indicators displayed on the gauge chart. You can configure:
>> **Label Position**: Define where labels are positioned on the gauge, such as inside, outside, or at specific points.
**Indicator Style**: Customize the appearance of the indicator or needle, including color and thickness.

### **Creating a gauge chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-gaugeChart.mp4" width="100%" />
   </div>
