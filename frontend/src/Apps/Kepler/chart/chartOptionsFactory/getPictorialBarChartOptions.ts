import { getColorTheme, isDefined, isEmpty } from "utils/utilities";
import { chartDataConfigProps } from "./index";

/**
 * PictorialBar chart factory.
 *
 * Reuses the same data format as VerticalAxisChart / barChart:
 * chartData.data = {
 *   xAxisData: string[],
 *   series: [{ id, seriesData: { [groupKey]: [[xVal, yVal], ...] }, ... }]
 * }
 */
const getPictorialBarChartOptions = ({
  chartData,
  chartCustomization,
  dimensions,
  editMode,
}: chartDataConfigProps) => {
  if (!chartData || !chartData.data) {
    console.error("getPictorialBarChartOptions: chartData or chartData.data is undefined");
    return {};
  }

  if (isEmpty(chartData.data.series)) {
    return {};
  }

  const xAxisData: string[] = isDefined(chartData.data.xAxisData)
    ? chartData.data.xAxisData
    : [];

  const symbol: string = chartCustomization.pictorialBarSymbol ?? "circle";
  const symbolRepeat: boolean = chartCustomization.pictorialBarSymbolRepeat ?? false;

  const colorArr = getColorTheme(chartCustomization.colorTheme, chartCustomization).color;
  let colorIndex = 0;

  const series: any[] = [];

  chartData.data.series.forEach((s: any) => {
    if (!isDefined(s.seriesData)) return;

    Object.keys(s.seriesData).forEach((groupKey: string) => {
      const rawPoints: [string, number][] = s.seriesData[groupKey];

      const dataValues: number[] = xAxisData.map((x: string) => {
        const found = rawPoints.find((p) => String(p[0]) === String(x));
        return found ? Number(found[1]) : 0;
      });

      series.push({
        type: "pictorialBar",
        symbol,
        symbolRepeat,
        symbolSize: ["80%", "60%"],
        data: dataValues,
        itemStyle: {
          color: colorArr[colorIndex % colorArr.length],
        },
        silent: !editMode,
      });

      colorIndex += 1;
    });
  });

  return {
    color: colorArr,
    tooltip: {
      show: chartCustomization.tooltip ?? true,
      trigger: "axis",
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
      data: xAxisData,
    },
    yAxis: {
      type: "value",
    },
    series,
  };
};

export default getPictorialBarChartOptions;
