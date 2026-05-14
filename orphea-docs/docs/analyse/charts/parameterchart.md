import ReactPlayer from "react-player"

# Parameter

## **Parameter Chart**
> A **Parameter Chart** is a type of chart used to visualize the relationships between various parameters or variables. It often displays data in a way that highlights how changes in one parameter affect others. This chart type is useful for analyzing dependencies, trends, and correlations within datasets. This documentation will guide you through the configuration options available for creating a parameter chart in our application and provide instructions on how to create one effectively.

### **Operations available in charts**

> **Parameter Selection:** The **Parameter Selection** option allows you to configure which parameters or variables will be visualized in the chart. You can choose from the following parameters:
>> **Independent Variables**: Select the main parameters that drive the changes in the chart (e.g., time, temperature).
**Dependent Variables**: Choose the parameters that respond to changes in the independent variables (e.g., sales, performance).

> **Axis Configuration:** The **Axis Configuration** option allows you to customize the axes of the chart based on the selected parameters. You can choose from the following parameters:
>> **X-Axis Configuration**: Define the parameter displayed on the X-axis, including options for scale (linear, logarithmic) and label customization.

> **Chart Types:** The **Chart Types** option allows you to select the specific type of parameter chart to visualize your data. You can choose from the following chart types:
>> **Line Chart**: Display the relationship between parameters as a line graph.
**Scatter Plot**: Visualize the correlation between two variables with individual data points.
**Bubble Chart**: Represent data points with varying sizes based on an additional parameter.

> **Labels and Colors:** The **Labels and Colors** option allows you to customize the appearance of the chart, including labels and color schemes. You can configure:
>> **Label Position**: Define where labels are positioned on the chart, such as on the data points or axes.
**Color Scheme**: Choose or customize the color scheme used to represent different parameters or data points.

### **Creating a parameter chart**
   <div className="video__wrapper">
      <ReactPlayer className="video__player" controls height="100%" url="/learn/charts/Create-parameterChart.mp4" width="100%" />
   </div>
