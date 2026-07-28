import { getColorTheme, isDefined } from "utils/utilities";
import { chartDataConfigProps } from "./index";

/**
 * Parallel coordinates chart factory.
 *
 * Expected data format from backend:
 * chartData.data = {
 *   axes: string[],       // dimension names (N columns)
 *   data: number[][]      // each row = one record, each value = value on each axis
 * }
 */
const getParallelChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getParallelChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  const parallelData = chartData.data;

  const axes: string[] = isDefined(parallelData.axes) ? parallelData.axes : [];
  const rawData: number[][] = isDefined(parallelData.data) ? parallelData.data : [];

  const opacity: number = chartCustomization.parallelLineOpacity ?? 0.5;
  const lineColor: string =
    getColorTheme(chartCustomization.colorTheme, chartCustomization).color[0] ??
    "#24527a";

  const parallelAxis = axes.map((name: string, index: number) => ({
    dim: index,
    name,
  }));

  return {
    color: getColorTheme(chartCustomization.colorTheme, chartCustomization).color,
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "item",
    },
    parallelAxis,
    series: [
      {
        type: "parallel",
        lineStyle: {
          width: 1,
          opacity,
          color: lineColor,
        },
        data: rawData,
        silent: !editMode,
      },
    ],
  };
};

export default getParallelChartOptions;
