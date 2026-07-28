import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from "./index";

const getHeatmapChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getHeatmapChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  // Expected data format from backend:
  // chartData.data = {
  //   xCategories: string[],
  //   yCategories: string[],
  //   data: [[xIndex, yIndex, value], ...]
  // }
  const heatmapData = chartData.data;

  const xCategories: string[] = isDefined(heatmapData.xCategories)
    ? heatmapData.xCategories
    : [];
  const yCategories: string[] = isDefined(heatmapData.yCategories)
    ? heatmapData.yCategories
    : [];
  const rawData: [number, number, number][] = isDefined(heatmapData.data)
    ? heatmapData.data
    : [];

  const values = rawData.map((item) => item[2]);
  const minValue = values.length > 0 ? Math.min(...values) : 0;
  const maxValue = values.length > 0 ? Math.max(...values) : 100;

  const colorRange: string[] = chartCustomization.heatmapColors ?? [
    "#e0f3f8",
    "#abd9e9",
    "#74add1",
    "#24527a",
  ];

  return {
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "item",
      position: "top",
    },
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization).color,
    grid: {
      top: "10%",
      left: "15%",
      right: "15%",
      bottom: "20%",
    },
    xAxis: {
      type: "category",
      data: xCategories,
      splitArea: { show: true },
    },
    yAxis: {
      type: "category",
      data: yCategories,
      splitArea: { show: true },
    },
    visualMap: {
      min: isDefined(chartCustomization.heatmapMin) ? chartCustomization.heatmapMin : minValue,
      max: isDefined(chartCustomization.heatmapMax) ? chartCustomization.heatmapMax : maxValue,
      calculable: true,
      orient: "horizontal",
      left: "center",
      bottom: "2%",
      inRange: {
        color: colorRange,
      },
    },
    series: [
      {
        type: "heatmap",
        data: rawData,
        label: {
          show: chartCustomization.heatmapShowLabel ?? false,
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowColor: "rgba(0, 0, 0, 0.5)",
          },
        },
        silent: !editMode,
      },
    ],
  };
};

export default getHeatmapChartOptions;
