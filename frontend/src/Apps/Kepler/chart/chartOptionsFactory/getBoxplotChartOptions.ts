import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from "./index";

/**
 * Boxplot chart factory.
 *
 * Expected data format from backend:
 * chartData.data = {
 *   categories: string[],          // one label per box (can be ["All"] when no groupBy)
 *   data: [[min, Q1, median, Q3, max], ...]  // one inner array per category
 * }
 */
const getBoxplotChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getBoxplotChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  const boxData = chartData.data;

  const categories: string[] = isDefined(boxData.categories) ? boxData.categories : [];
  const rawData: number[][] = isDefined(boxData.data) ? boxData.data : [];

  const colors = getColorTheme(chartCustomization.colorTheme, chartCustomization).color;

  return {
    color: colors,
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "item",
      axisPointer: { type: "shadow" },
    },
    grid: {
      top: "10%",
      left: "10%",
      right: "10%",
      bottom: "15%",
    },
    xAxis: {
      type: "category",
      data: categories,
      boundaryGap: true,
      nameGap: 30,
      splitArea: { show: false },
      splitLine: { show: false },
    },
    yAxis: {
      type: "value",
      splitArea: { show: true },
    },
    series: [
      {
        type: "boxplot",
        data: rawData,
        itemStyle: {
          color: colors[0] ?? "#4a8ede",
          borderColor: colors[1] ?? "#2c5f9e",
        },
        silent: !editMode,
      },
    ],
  };
};

export default getBoxplotChartOptions;
